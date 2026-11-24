import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  Layers,
  Search,
  RefreshCw,
  TrendingUp,
  TrendingDown,
  Maximize2,
  SlidersHorizontal,
  ChevronRight,
  ExternalLink,
  ShieldCheck,
  Bookmark,
  Grid
} from 'lucide-react';
import { fetchMarketHeatmap, fetchBatchQuotes } from '../../services/api';
import { watchlistService, holdingsService } from '../../services/supabase';

// Squarified Treemap Algorithm
function computeTreemap(nodes, width, height) {
  const sorted = [...nodes].filter(n => n.value > 0).sort((a, b) => b.value - a.value);
  const totalValue = sorted.reduce((sum, n) => sum + n.value, 0);
  if (totalValue <= 0 || width <= 0 || height <= 0) return [];

  const rects = [];

  function layout(x, y, w, h, currentNodes) {
    if (currentNodes.length === 0) return;
    if (w <= 1 || h <= 1) return;

    if (currentNodes.length === 1) {
      rects.push({
        id: currentNodes[0].id,
        x,
        y,
        w,
        h,
        data: currentNodes[0].data,
      });
      return;
    }

    const nodeValueSum = currentNodes.reduce((sum, n) => sum + n.value, 0);
    if (nodeValueSum <= 0) return;

    const isVertical = w >= h;
    const size = isVertical ? w : h;

    let bestSplitIndex = 1;
    let minMaxRatio = Infinity;

    for (let i = 1; i <= currentNodes.length; i++) {
      const leftNodes = currentNodes.slice(0, i);
      const leftSum = leftNodes.reduce((sum, n) => sum + n.value, 0);
      const leftRatio = leftSum / nodeValueSum;
      const thickness = size * leftRatio;

      if (thickness <= 0) continue;

      let maxRatio = 0;
      const otherDim = isVertical ? h : w;

      for (const node of leftNodes) {
        const nodeRatio = node.value / leftSum;
        const length = otherDim * nodeRatio;
        if (length <= 0) continue;

        const aspect = Math.max(thickness / length, length / thickness);
        if (aspect > maxRatio) maxRatio = aspect;
      }

      if (maxRatio < minMaxRatio) {
        minMaxRatio = maxRatio;
        bestSplitIndex = i;
      } else {
        break;
      }
    }

    const groupNodes = currentNodes.slice(0, bestSplitIndex);
    const remainingNodes = currentNodes.slice(bestSplitIndex);

    const groupSum = groupNodes.reduce((sum, n) => sum + n.value, 0);
    const groupRatio = groupSum / nodeValueSum;

    if (isVertical) {
      const groupWidth = w * groupRatio;
      let currentY = y;
      for (const node of groupNodes) {
        const nodeRatio = node.value / groupSum;
        const nodeHeight = h * nodeRatio;
        rects.push({
          id: node.id,
          x,
          y: currentY,
          w: groupWidth,
          h: nodeHeight,
          data: node.data,
        });
        currentY += nodeHeight;
      }
      layout(x + groupWidth, y, w - groupWidth, h, remainingNodes);
    } else {
      const groupHeight = h * groupRatio;
      let currentX = x;
      for (const node of groupNodes) {
        const nodeRatio = node.value / groupSum;
        const nodeWidth = w * nodeRatio;
        rects.push({
          id: node.id,
          x: currentX,
          y,
          w: nodeWidth,
          h: groupHeight,
          data: node.data,
        });
        currentX += nodeWidth;
      }
      layout(x, y + groupHeight, w, h - groupHeight, remainingNodes);
    }
  }

  layout(0, 0, width, height, sorted);
  return rects;
}

// Institutional color gradient based on percentage change
function getTileColor(pct) {
  if (pct >= 3.0) return '#00D09C'; // Strong Gain Green
  if (pct >= 1.5) return '#00B386'; // Moderate Gain Green
  if (pct >= 0.5) return '#00875A'; // Subtle Gain Green
  if (pct > -0.5) return '#1C2333'; // Neutral Slate
  if (pct > -1.5) return '#B91C1C'; // Subtle Loss Red
  if (pct > -3.0) return '#DC2626'; // Moderate Loss Red
  return '#F04438'; // Strong Loss Red
}

function formatLargeCurrency(num, currSymbol = '$') {
  if (!num || num === 0) return `${currSymbol}0`;
  if (num >= 1e12) return `${currSymbol}${(num / 1e12).toFixed(2)}T`;
  if (num >= 1e9) return `${currSymbol}${(num / 1e9).toFixed(2)}B`;
  if (num >= 1e6) return `${currSymbol}${(num / 1e6).toFixed(2)}M`;
  if (num >= 1e3) return `${currSymbol}${(num / 1e3).toFixed(2)}K`;
  return `${currSymbol}${num.toFixed(2)}`;
}

export default function MarketHeatmapView({ market: propMarket = 'IN', onInspectStock, user, isEmbedded = false }) {
  const [market, setMarket] = useState(propMarket); // 'US' | 'IN' | 'PORTFOLIO' | 'WATCHLIST'

  useEffect(() => {
    if (propMarket && (propMarket === 'IN' || propMarket === 'US')) {
      setMarket(propMarket);
    }
  }, [propMarket]);
  const [sortBy, setSortBy] = useState('marketCap'); // 'marketCap' | 'price' | 'changePercent'
  const [groupBySector, setGroupBySector] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState({ sectors: [], stocks: [] });
  const [hoveredStock, setHoveredStock] = useState(null);
  const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0 });

  const containerRef = useRef(null);
  const [dimensions, setDimensions] = useState({ width: 1100, height: 680 });

  // Update container dimensions on resize
  useEffect(() => {
    const handleResize = () => {
      if (containerRef.current) {
        const rect = containerRef.current.getBoundingClientRect();
        if (rect.width > 200) {
          setDimensions({
            width: Math.floor(rect.width),
            height: Math.max(560, Math.floor(rect.width * 0.58))
          });
        }
      }
    };
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      if (market === 'PORTFOLIO') {
        // Portfolio: only user holdings
        const [holdIn, holdUs] = await Promise.all([
          holdingsService.fetchHoldings(user?.userId, true).catch(() => []),
          holdingsService.fetchHoldings(user?.userId, false).catch(() => [])
        ]);
        const symbols = Array.from(new Set([
          ...holdIn.map(h => h.symbol),
          ...holdUs.map(h => h.symbol)
        ])).filter(Boolean);

        if (symbols.length === 0) {
          setData({ sectors: [], stocks: [] });
          setLoading(false);
          return;
        }

        const quotesMap = await fetchBatchQuotes(symbols);
        const stockList = Object.values(quotesMap).map(q => ({
          symbol: q.symbol,
          name: q.name || q.symbol,
          price: q.price || 0,
          change: q.change || 0,
          changePercent: q.changePercent || 0,
          marketCap: q.marketCap || (q.price * 10000000) || 100000000,
          pe: q.pe || 'N/A',
          sector: q.symbol.endsWith('.NS') ? 'Indian Equities' : 'US Equities',
          currency: q.currency || 'USD',
          currencySymbol: q.currencySymbol || '$',
          logoUrl: q.logoUrl
        }));

        setData({
          sectors: [{
            name: 'Portfolio Holdings',
            totalMarketCap: stockList.reduce((s, x) => s + x.marketCap, 0),
            stocks: stockList
          }],
          stocks: stockList
        });
      } else if (market === 'WATCHLIST') {
        // Watchlist: only watchlisted stocks, grouped by category
        const [watchIn, watchUs] = await Promise.all([
          watchlistService.fetchWatchlist(user?.userId, true).catch(() => []),
          watchlistService.fetchWatchlist(user?.userId, false).catch(() => [])
        ]);
        const allWatch = [...watchIn, ...watchUs];
        const symbols = Array.from(new Set(allWatch.map(w => w.symbol))).filter(Boolean);

        if (symbols.length === 0) {
          setData({ sectors: [], stocks: [] });
          setLoading(false);
          return;
        }

        // Build a symbol-to-category map
        const symCatMap = {};
        allWatch.forEach(w => {
          if (w.symbol) symCatMap[w.symbol] = w.category || w.watchlistName || 'General';
        });

        const quotesMap = await fetchBatchQuotes(symbols);
        const stockList = Object.values(quotesMap).map(q => ({
          symbol: q.symbol,
          name: q.name || q.symbol,
          price: q.price || 0,
          change: q.change || 0,
          changePercent: q.changePercent || 0,
          marketCap: q.marketCap || (q.price * 10000000) || 100000000,
          pe: q.pe || 'N/A',
          sector: symCatMap[q.symbol] || 'General',
          currency: q.currency || 'USD',
          currencySymbol: q.currencySymbol || '$',
          logoUrl: q.logoUrl
        }));

        // Group by watchlist category for sector display
        const catGroups = {};
        stockList.forEach(s => {
          const cat = s.sector;
          if (!catGroups[cat]) catGroups[cat] = { name: cat, totalMarketCap: 0, stocks: [] };
          catGroups[cat].stocks.push(s);
          catGroups[cat].totalMarketCap += s.marketCap;
        });

        setData({
          sectors: Object.values(catGroups).sort((a, b) => b.totalMarketCap - a.totalMarketCap),
          stocks: stockList
        });
      } else {
        const res = await fetchMarketHeatmap(market);
        setData(res);
      }
    } catch (err) {
      console.error('Failed to load heatmap data:', err);
    } finally {
      setLoading(false);
    }
  }, [market, user]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Compute treemap rectangles
  const layoutResult = useMemo(() => {
    if (!data.stocks || data.stocks.length === 0 || dimensions.width <= 0) {
      return { sectorRects: [], stockRects: [] };
    }

    // Determine weight function
    const getNodeValue = (stock) => {
      if (sortBy === 'price') return Math.max(stock.price, 1);
      if (sortBy === 'changePercent') return Math.max(Math.abs(stock.changePercent), 0.1) * 100000;
      return Math.max(stock.marketCap || 1000000, 10000);
    };

    if (groupBySector && data.sectors && data.sectors.length > 0) {
      // 1. Layout Sectors
      const sectorNodes = data.sectors.map(sec => ({
        id: `sec-${sec.name}`,
        value: sec.stocks.reduce((acc, st) => acc + getNodeValue(st), 0),
        data: sec
      })).filter(n => n.value > 0);

      const sectorRects = computeTreemap(sectorNodes, dimensions.width, dimensions.height);
      const stockRects = [];

      // 2. Layout stocks inside each sector
      sectorRects.forEach(sRect => {
        const headerH = 26; // Room for sector header
        const innerW = Math.max(sRect.w - 4, 0);
        const innerH = Math.max(sRect.h - headerH - 4, 0);

        if (innerW > 5 && innerH > 5) {
          const childNodes = sRect.data.stocks.map(st => ({
            id: `stk-${st.symbol}`,
            value: getNodeValue(st),
            data: st
          }));

          const childRects = computeTreemap(childNodes, innerW, innerH);
          childRects.forEach(c => {
            stockRects.push({
              id: c.id,
              x: sRect.x + c.x + 2,
              y: sRect.y + headerH + c.y + 2,
              w: c.w,
              h: c.h,
              data: c.data
            });
          });
        }
      });

      return { sectorRects, stockRects };
    } else {
      // Flat layout without sector grouping
      const stockNodes = data.stocks.map(st => ({
        id: `stk-${st.symbol}`,
        value: getNodeValue(st),
        data: st
      }));

      const stockRects = computeTreemap(stockNodes, dimensions.width, dimensions.height);
      return { sectorRects: [], stockRects };
    }
  }, [data, dimensions, groupBySector, sortBy]);

  // Handle Mouse Hover on Stock Tile
  const handleMouseEnter = (stock, e) => {
    const parentRect = containerRef.current?.getBoundingClientRect();
    if (parentRect) {
      let x = e.clientX - parentRect.left + 16;
      let y = e.clientY - parentRect.top + 16;
      if (x + 270 > dimensions.width) x -= 290;
      if (y + 180 > dimensions.height) y -= 190;
      setTooltipPos({ x: Math.max(10, x), y: Math.max(10, y) });
    }
    setHoveredStock(stock);
  };

  return (
    <div className={`heatmap-view-wrapper ${isEmbedded ? 'embedded-in-dash' : ''}`}>
      {/* Top Header & Telemetry Bar */}
      <div className={`heatmap-top-bar ${isEmbedded ? 'embedded-bar' : ''}`}>
        {!isEmbedded && (
          <div className="heatmap-title-group">
            <div className="heatmap-badge">
              <Grid size={12} />
              <span>INSTITUTIONAL MARKET SQUARIFY</span>
            </div>
            <h1 className="heatmap-headline">Market Treemap Heatmap</h1>
            <p className="heatmap-subtitle">
              Algorithmic squarified treemap visualizer tracking capital concentration and real-time market breadth.
            </p>
          </div>
        )}

        {/* Action Controls */}
        <div className="heatmap-controls-row">
          {/* Market / Index Selector */}
          <div className="heatmap-segmented-control">
            <button
              className={`heatmap-segment-btn ${market === 'US' ? 'active' : ''}`}
              onClick={() => setMarket('US')}
            >
              🇺🇸 S&P 500
            </button>
            <button
              className={`heatmap-segment-btn ${market === 'IN' ? 'active' : ''}`}
              onClick={() => setMarket('IN')}
            >
              🇮🇳 Nifty 50
            </button>
            <button
              className={`heatmap-segment-btn ${market === 'PORTFOLIO' ? 'active' : ''}`}
              onClick={() => setMarket('PORTFOLIO')}
            >
              <ShieldCheck size={13} />
              <span>Portfolio</span>
            </button>
            <button
              className={`heatmap-segment-btn ${market === 'WATCHLIST' ? 'active' : ''}`}
              onClick={() => setMarket('WATCHLIST')}
            >
              <Bookmark size={13} />
              <span>Watchlist</span>
            </button>
          </div>

          {/* Sizing Metric Selector */}
          <div className="heatmap-segmented-control">
            <button
              className={`heatmap-segment-btn ${sortBy === 'marketCap' ? 'active' : ''}`}
              onClick={() => setSortBy('marketCap')}
              title="Box size represents relative Market Capitalization"
            >
              Market Cap
            </button>
            <button
              className={`heatmap-segment-btn ${sortBy === 'changePercent' ? 'active' : ''}`}
              onClick={() => setSortBy('changePercent')}
              title="Box size represents Absolute Daily % Change"
            >
              Daily % Change
            </button>
            <button
              className={`heatmap-segment-btn ${sortBy === 'price' ? 'active' : ''}`}
              onClick={() => setSortBy('price')}
              title="Box size represents Share Price"
            >
              Price
            </button>
          </div>

          {/* Sector Grouping Toggle */}
          <button
            className={`heatmap-filter-pill ${groupBySector ? 'active' : ''}`}
            onClick={() => setGroupBySector(!groupBySector)}
            title="Group stocks by industry sectors"
          >
            <Layers size={13} />
            <span>Group Sectors</span>
          </button>

          {/* Search Filter */}
          <div className="heatmap-search-box">
            <Search size={14} className="heatmap-search-icon" />
            <input
              type="text"
              placeholder="Search ticker..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          {/* Refresh Button */}
          <button
            className="heatmap-refresh-btn"
            onClick={loadData}
            title="Refresh Live Data"
          >
            <RefreshCw size={14} className={loading ? 'spin-animation' : ''} />
          </button>
        </div>
      </div>

      {/* Main Treemap Canvas Container */}
      <div
        className="heatmap-canvas-container"
        ref={containerRef}
        style={{ height: `${dimensions.height}px` }}
        onMouseLeave={() => setHoveredStock(null)}
      >
        {loading ? (
          <div className="heatmap-loading-overlay">
            <RefreshCw size={32} className="spin-animation fintech-primary" />
            <span>Computing Squarified Capital Allocation...</span>
          </div>
        ) : data.stocks.length === 0 ? (
          <div className="heatmap-empty-state">
            <ShieldCheck size={40} className="empty-icon" />
            <h3>No Securities Available in Selected Universe</h3>
            <p>Add stocks to your Watchlist or Holdings to view them in the Portfolio Heatmap.</p>
          </div>
        ) : (
          <>
            {/* Sector Background Rectangles & Headers */}
            {groupBySector &&
              layoutResult.sectorRects.map((sRect) => (
                <div
                  key={sRect.id}
                  className="heatmap-sector-box"
                  style={{
                    left: `${sRect.x}px`,
                    top: `${sRect.y}px`,
                    width: `${sRect.w}px`,
                    height: `${sRect.h}px`,
                  }}
                >
                  <div className="heatmap-sector-header">
                    <span className="heatmap-sector-name">{sRect.data.name}</span>
                    <span className="heatmap-sector-mcap font-mono">
                      {formatLargeCurrency(sRect.data.totalMarketCap, '$')}
                    </span>
                  </div>
                </div>
              ))}

            {/* Individual Stock Tile Rectangles */}
            {layoutResult.stockRects.map((rect) => {
              const stock = rect.data;
              const isMatched = searchQuery
                ? stock.symbol.toUpperCase().includes(searchQuery.toUpperCase()) ||
                  stock.name.toUpperCase().includes(searchQuery.toUpperCase())
                : true;

              const tileBg = getTileColor(stock.changePercent);
              const isGain = stock.changePercent >= 0;

              return (
                <div
                  key={rect.id}
                  className={`heatmap-tile ${isMatched ? 'is-matched' : 'is-dimmed'}`}
                  style={{
                    left: `${rect.x + 1}px`,
                    top: `${rect.y + 1}px`,
                    width: `${Math.max(rect.w - 2, 0)}px`,
                    height: `${Math.max(rect.h - 2, 0)}px`,
                    backgroundColor: tileBg,
                  }}
                  onMouseEnter={(e) => handleMouseEnter(stock, e)}
                  onClick={() => onInspectStock && onInspectStock(stock.symbol)}
                  title={`Click to analyze ${stock.symbol}`}
                >
                  {/* Stock Symbol */}
                  <span
                    className="heatmap-tile-symbol font-mono"
                    style={{
                      fontSize: `${Math.max(Math.min(rect.w / 5.2, 14), 8.5)}px`,
                    }}
                  >
                    {stock.symbol.replace('.NS', '').replace('.BO', '')}
                  </span>

                  {/* Stock Price (if height permits) */}
                  {rect.h > 46 && rect.w > 48 && (
                    <span
                      className="heatmap-tile-price font-mono"
                      style={{
                        fontSize: `${Math.max(Math.min(rect.w / 7.5, 11), 8)}px`,
                      }}
                    >
                      {stock.currencySymbol}{stock.price.toFixed(2)}
                    </span>
                  )}

                  {/* Daily Change Percent */}
                  {rect.h > 58 && rect.w > 50 && (
                    <span
                      className="heatmap-tile-change font-mono"
                      style={{
                        fontSize: `${Math.max(Math.min(rect.w / 7.2, 11), 8)}px`,
                      }}
                    >
                      {isGain ? '+' : ''}{stock.changePercent.toFixed(2)}%
                    </span>
                  )}
                </div>
              );
            })}

            {/* Hover Detailed Tooltip Card */}
            {hoveredStock && (
              <div
                className="heatmap-hover-tooltip"
                style={{
                  left: `${tooltipPos.x}px`,
                  top: `${tooltipPos.y}px`,
                }}
              >
                <div className="tooltip-top-row">
                  <div className="tooltip-avatar">
                    <img
                      src={hoveredStock.logoUrl}
                      alt=""
                      onError={(e) => { e.target.style.display = 'none'; }}
                    />
                  </div>
                  <div className="tooltip-titles">
                    <div className="tooltip-sym-row">
                      <span className="tooltip-symbol font-mono">{hoveredStock.symbol}</span>
                      <span className="tooltip-tag">{hoveredStock.currency || 'USD'}</span>
                    </div>
                    <div className="tooltip-name">{hoveredStock.name}</div>
                  </div>
                </div>

                <div className="tooltip-metrics-grid">
                  <div className="tooltip-metric">
                    <span className="metric-label">Last Price</span>
                    <span className="metric-val font-mono">
                      {hoveredStock.currencySymbol}{hoveredStock.price.toFixed(2)}
                    </span>
                  </div>
                  <div className="tooltip-metric">
                    <span className="metric-label">24h Return</span>
                    <span
                      className={`metric-val font-mono ${
                        hoveredStock.changePercent >= 0 ? 'gain-green' : 'loss-red'
                      }`}
                    >
                      {hoveredStock.changePercent >= 0 ? '+' : ''}
                      {hoveredStock.changePercent.toFixed(2)}%
                    </span>
                  </div>
                  <div className="tooltip-metric">
                    <span className="metric-label">Market Cap</span>
                    <span className="metric-val font-mono">
                      {formatLargeCurrency(hoveredStock.marketCap, hoveredStock.currencySymbol)}
                    </span>
                  </div>
                  <div className="tooltip-metric">
                    <span className="metric-label">P/E Ratio</span>
                    <span className="metric-val font-mono">
                      {typeof hoveredStock.pe === 'number' ? hoveredStock.pe.toFixed(2) : hoveredStock.pe}
                    </span>
                  </div>
                </div>

                <div className="tooltip-footer">
                  <span className="tooltip-sector">{hoveredStock.sector}</span>
                  <span className="tooltip-action-hint">
                    Click for Deep Analysis <ExternalLink size={10} />
                  </span>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Heatmap Legend */}
      <div className="heatmap-legend-bar">
        <span className="legend-label">Daily Return Spread:</span>
        <div className="legend-gradient-track">
          <div className="legend-stop" style={{ background: '#F04438' }}>-3.0%</div>
          <div className="legend-stop" style={{ background: '#DC2626' }}>-1.5%</div>
          <div className="legend-stop" style={{ background: '#1C2333' }}>0.0%</div>
          <div className="legend-stop" style={{ background: '#00875A' }}>+1.0%</div>
          <div className="legend-stop" style={{ background: '#00D09C' }}>+3.0%</div>
        </div>
      </div>
    </div>
  );
}
