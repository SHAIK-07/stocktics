import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  GitCompare,
  Plus,
  X,
  Search,
  RefreshCw,
  TrendingUp,
  TrendingDown,
  Activity,
  Layers,
  BarChart2,
  ExternalLink,
  ShieldCheck,
  Zap,
  Info
} from 'lucide-react';
import { fetchStockComparison, searchStocks } from '../../services/api';

// Harmonious Institutional 12-Color Palette
const PALETTE = [
  '#00D09C', // Groww Mint Emerald
  '#1455FE', // INDmoney Tech Blue
  '#F59E0B', // Amber
  '#EC4899', // Rose Pink
  '#8B5CF6', // Violet
  '#06B6D4', // Cyan
  '#F97316', // Orange
  '#10B981', // Emerald
  '#6366F1', // Indigo
  '#EAB308', // Yellow
  '#14B8A6', // Teal
  '#F43F5E', // Red-Rose
];

const DEFAULT_SYMBOLS_US = ['NVDA', 'AAPL', 'MSFT', 'AMZN', 'TSLA'];
const DEFAULT_SYMBOLS_IN = ['RELIANCE.NS', 'TCS.NS', 'HDFCBANK.NS', 'ICICIBANK.NS', 'INFY.NS'];

export default function StockComparisonView({ onInspectStock, market = 'IN' }) {
  const [selectedSymbols, setSelectedSymbols] = useState(
    market === 'IN' ? DEFAULT_SYMBOLS_IN : DEFAULT_SYMBOLS_US
  );

  useEffect(() => {
    setSelectedSymbols(market === 'IN' ? DEFAULT_SYMBOLS_IN : DEFAULT_SYMBOLS_US);
  }, [market]);
  const [period, setPeriod] = useState('6mo'); // '1mo', '3mo', '6mo', '1y', '5y'
  const [scaleMode, setScaleMode] = useState('percentage'); // 'percentage' | 'price'
  
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  // Search & add stock state
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showSearchDropdown, setShowSearchDropdown] = useState(false);
  const searchContainerRef = useRef(null);

  // Correlation hover highlight
  const [hoveredCorrelationPair, setHoveredCorrelationPair] = useState(null);

  // Chart Container dimensions
  const chartWrapperRef = useRef(null);
  const [dimensions, setDimensions] = useState({ width: 900, height: 420 });

  // Hover crosshair tracker
  const [hoverIndex, setHoverIndex] = useState(null);

  // Update chart dimensions on resize
  useEffect(() => {
    const handleResize = () => {
      if (chartWrapperRef.current) {
        const rect = chartWrapperRef.current.getBoundingClientRect();
        if (rect.width > 200) {
          setDimensions({
            width: Math.floor(rect.width),
            height: Math.max(380, Math.floor(rect.width * 0.45)),
          });
        }
      }
    };
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Fetch comparison data
  const loadComparison = useCallback(async () => {
    if (selectedSymbols.length === 0) {
      setData(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const res = await fetchStockComparison(selectedSymbols, period);
      setData(res);
    } catch (err) {
      console.error('Failed to load comparison data:', err);
      setError('Unable to load comparison historical series. Check your network connection.');
    } finally {
      setLoading(false);
    }
  }, [selectedSymbols, period]);

  useEffect(() => {
    loadComparison();
  }, [loadComparison]);

  // Debounced search for adding new stocks
  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      setIsSearching(false);
      return;
    }
    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const results = await searchStocks(searchQuery.trim());
        setSearchResults(results.slice(0, 6));
      } catch (err) {
        console.error(err);
      } finally {
        setIsSearching(false);
      }
    }, 250);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  // Handle outside click to close search dropdown
  useEffect(() => {
    function handleClickOutside(e) {
      if (searchContainerRef.current && !searchContainerRef.current.contains(e.target)) {
        setShowSearchDropdown(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleAddSymbol = (sym) => {
    const clean = sym.trim().toUpperCase();
    if (!selectedSymbols.includes(clean)) {
      if (selectedSymbols.length >= 12) {
        alert('Maximum 12 stocks can be compared simultaneously.');
        return;
      }
      setSelectedSymbols([...selectedSymbols, clean]);
    }
    setSearchQuery('');
    setShowSearchDropdown(false);
  };

  const handleRemoveSymbol = (sym) => {
    setSelectedSymbols(selectedSymbols.filter((s) => s !== sym));
  };

  const handleSetPreset = (marketType) => {
    if (marketType === 'US') {
      setSelectedSymbols(DEFAULT_SYMBOLS_US);
    } else if (marketType === 'IN') {
      setSelectedSymbols(DEFAULT_SYMBOLS_IN);
    }
  };

  // Color mapping
  const symbolColors = useMemo(() => {
    const map = {};
    selectedSymbols.forEach((sym, idx) => {
      map[sym] = PALETTE[idx % PALETTE.length];
    });
    return map;
  }, [selectedSymbols]);

  // Chart coordinate calculations
  const chartData = useMemo(() => {
    if (!data || !data.series || !data.dates || data.dates.length === 0) return null;

    const { dates, series } = data;
    const padding = { top: 20, right: 30, bottom: 40, left: 55 };
    const innerW = dimensions.width - padding.left - padding.right;
    const innerH = dimensions.height - padding.top - padding.bottom;

    // Find Min and Max values for Y-axis
    let minY = Infinity;
    let maxY = -Infinity;

    Object.keys(series).forEach((sym) => {
      const pts = series[sym];
      pts.forEach((p) => {
        const val = scaleMode === 'percentage' ? p.percent : p.price;
        if (val < minY) minY = val;
        if (val > maxY) maxY = val;
      });
    });

    if (minY === Infinity) minY = -10;
    if (maxY === -Infinity) maxY = 10;

    // Add 10% breathing room
    const range = maxY - minY;
    minY = minY - (range * 0.08 || 2);
    maxY = maxY + (range * 0.08 || 2);

    const getX = (idx) => padding.left + (idx / (dates.length - 1 || 1)) * innerW;
    const getY = (val) => padding.top + innerH - ((val - minY) / (maxY - minY || 1)) * innerH;

    // Build SVG paths for each symbol
    const paths = {};
    Object.keys(series).forEach((sym) => {
      const pts = series[sym];
      if (pts.length === 0) return;

      // Build date to point lookup
      const lookup = {};
      pts.forEach((p) => {
        lookup[p.date] = p;
      });

      let d = '';
      let isFirst = true;

      dates.forEach((dt, idx) => {
        const p = lookup[dt];
        if (p) {
          const val = scaleMode === 'percentage' ? p.percent : p.price;
          const x = getX(idx);
          const y = getY(val);
          if (isFirst) {
            d += `M ${x.toFixed(1)} ${y.toFixed(1)}`;
            isFirst = false;
          } else {
            d += ` L ${x.toFixed(1)} ${y.toFixed(1)}`;
          }
        }
      });

      paths[sym] = d;
    });

    // Zero horizontal baseline (for percentage mode)
    const zeroY = scaleMode === 'percentage' && minY <= 0 && maxY >= 0 ? getY(0) : null;

    // Grid tick lines
    const yTicks = [];
    const numYTicks = 5;
    for (let i = 0; i <= numYTicks; i++) {
      const val = minY + (i / numYTicks) * (maxY - minY);
      yTicks.push({
        value: val,
        y: getY(val),
        label: scaleMode === 'percentage' ? `${val >= 0 ? '+' : ''}${val.toFixed(1)}%` : val.toFixed(1),
      });
    }

    const xTicks = [];
    const step = Math.max(1, Math.floor(dates.length / 5));
    for (let i = 0; i < dates.length; i += step) {
      xTicks.push({
        x: getX(i),
        label: dates[i].slice(5), // MM-DD
      });
    }

    return {
      padding,
      innerW,
      innerH,
      minY,
      maxY,
      zeroY,
      yTicks,
      xTicks,
      paths,
      dates,
      getX,
      getY,
    };
  }, [data, dimensions, scaleMode]);

  // Handle Mouse Move over chart canvas
  const handleMouseMove = (e) => {
    if (!chartData || !chartWrapperRef.current) return;
    const rect = chartWrapperRef.current.getBoundingClientRect();
    const mouseX = e.clientX - rect.left - chartData.padding.left;
    const progress = Math.max(0, Math.min(1, mouseX / chartData.innerW));
    const idx = Math.round(progress * (chartData.dates.length - 1));
    setHoverIndex(idx);
  };

  const hoveredDate = chartData && hoverIndex !== null ? chartData.dates[hoverIndex] : null;

  return (
    <div className="compare-view-wrapper">
      {/* Top Header */}
      <div className="compare-top-bar">
        <div className="compare-title-group">
          <div className="compare-badge">
            <GitCompare size={12} />
            <span>CROSS-EQUITY CORRELATION & PERFORMANCE WORKSTATION</span>
          </div>
          <h1 className="compare-headline">Stock Comparison Tool</h1>
          <p className="compare-subtitle">
            Superimposed normalized performance returns, multi-asset volatility, and statistical Pearson correlation matrix.
          </p>
        </div>

        {/* Quick Market Presets */}
        <div className="compare-preset-buttons">
          <button
            className="compare-preset-btn"
            onClick={() => handleSetPreset('US')}
            title="Load Top 5 US Tech Compounders"
          >
            🇺🇸 Top US Tech
          </button>
          <button
            className="compare-preset-btn"
            onClick={() => handleSetPreset('IN')}
            title="Load Top 5 Indian Nifty Leaders"
          >
            🇮🇳 Nifty Leaders
          </button>
        </div>
      </div>

      {/* Ticker Management Bar & Search */}
      <div className="compare-tickers-bar">
        <div className="compare-chips-list">
          {selectedSymbols.map((sym) => {
            const color = symbolColors[sym] || '#64748B';
            const isHighlighted =
              hoveredCorrelationPair &&
              (hoveredCorrelationPair.includes(sym));

            return (
              <div
                key={sym}
                className={`compare-stock-chip ${isHighlighted ? 'is-highlighted' : ''}`}
                style={{ borderColor: isHighlighted ? color : 'var(--border-hairline)' }}
              >
                <span className="stock-chip-dot" style={{ backgroundColor: color }} />
                <span
                  className="stock-chip-sym font-mono"
                  onClick={() => onInspectStock && onInspectStock(sym)}
                  title="Click to view full analysis"
                >
                  {sym.replace('.NS', '').replace('.BO', '')}
                </span>
                <button
                  className="stock-chip-remove"
                  onClick={() => handleRemoveSymbol(sym)}
                  title={`Remove ${sym}`}
                >
                  <X size={12} />
                </button>
              </div>
            );
          })}
        </div>

        {/* Search & Add Stock Dropdown */}
        <div className="compare-add-container" ref={searchContainerRef}>
          <div className="compare-search-input-wrap">
            <Search size={14} className="compare-search-icon" />
            <input
              type="text"
              placeholder="+ Add stock (e.g. AAPL, TCS.NS)..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setShowSearchDropdown(true);
              }}
              onFocus={() => setShowSearchDropdown(true)}
            />
          </div>

          {showSearchDropdown && searchResults.length > 0 && (
            <div className="compare-search-dropdown">
              {searchResults.map((item) => (
                <div
                  key={item.symbol}
                  className="compare-dropdown-item"
                  onClick={() => handleAddSymbol(item.symbol)}
                >
                  <div className="dropdown-item-info">
                    <span className="dropdown-item-symbol font-mono">{item.symbol}</span>
                    <span className="dropdown-item-name">{item.name}</span>
                  </div>
                  <span className="dropdown-item-exchange">{item.exchange}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Main Chart Card */}
      <div className="compare-chart-card">
        {/* Controls Toolbar */}
        <div className="compare-toolbar">
          {/* Timeframe Switcher */}
          <div className="compare-timeframe-selector">
            {['1mo', '3mo', '6mo', '1y', '5y'].map((p) => (
              <button
                key={p}
                className={`timeframe-pill ${period === p ? 'active' : ''}`}
                onClick={() => setPeriod(p)}
              >
                {p.toUpperCase()}
              </button>
            ))}
          </div>

          {/* Scale Mode Switcher */}
          <div className="compare-scale-selector">
            <button
              className={`scale-pill ${scaleMode === 'percentage' ? 'active' : ''}`}
              onClick={() => setScaleMode('percentage')}
              title="Compare percentage returns normalized to 0% at period start"
            >
              Normalized % (0% Baseline)
            </button>
            <button
              className={`scale-pill ${scaleMode === 'price' ? 'active' : ''}`}
              onClick={() => setScaleMode('price')}
              title="Display raw absolute share prices"
            >
              Raw Price ($ / ₹)
            </button>
          </div>

          {/* Refresh button */}
          <button className="compare-refresh-btn" onClick={loadComparison} title="Reload Data">
            <RefreshCw size={13} className={loading ? 'spin-animation' : ''} />
          </button>
        </div>

        {/* SVG Multi-Line Chart Canvas */}
        <div
          className="compare-svg-wrapper"
          ref={chartWrapperRef}
          style={{ height: `${dimensions.height}px` }}
          onMouseMove={handleMouseMove}
          onMouseLeave={() => setHoverIndex(null)}
        >
          {loading ? (
            <div className="compare-loading-state">
              <RefreshCw size={28} className="spin-animation fintech-primary" />
              <span>Fetching and normalizing cross-asset time series...</span>
            </div>
          ) : error ? (
            <div className="compare-error-state">
              <span>{error}</span>
              <button onClick={loadComparison}>Retry</button>
            </div>
          ) : !chartData ? (
            <div className="compare-empty-state">
              <Plus size={36} />
              <span>Add at least 2 stocks above to begin comparative analysis.</span>
            </div>
          ) : (
            <svg
              width={dimensions.width}
              height={dimensions.height}
              className="compare-svg"
            >
              {/* Horizontal Grid Lines */}
              {chartData.yTicks.map((t, idx) => (
                <g key={idx}>
                  <line
                    x1={chartData.padding.left}
                    y1={t.y}
                    x2={dimensions.width - chartData.padding.right}
                    y2={t.y}
                    stroke="var(--border-hairline)"
                    strokeDasharray="3 3"
                    strokeWidth="1"
                  />
                  <text
                    x={chartData.padding.left - 8}
                    y={t.y + 3}
                    textAnchor="end"
                    fill="var(--text-tertiary)"
                    fontSize="10"
                    fontFamily="monospace"
                  >
                    {t.label}
                  </text>
                </g>
              ))}

              {/* Zero Baseline (in percentage mode) */}
              {chartData.zeroY !== null && (
                <line
                  x1={chartData.padding.left}
                  y1={chartData.zeroY}
                  x2={dimensions.width - chartData.padding.right}
                  y2={chartData.zeroY}
                  stroke="#94A3B8"
                  strokeWidth="1.2"
                  strokeDasharray="4 2"
                />
              )}

              {/* Vertical Date Grid Lines */}
              {chartData.xTicks.map((t, idx) => (
                <g key={idx}>
                  <line
                    x1={t.x}
                    y1={chartData.padding.top}
                    x2={t.x}
                    y2={dimensions.height - chartData.padding.bottom}
                    stroke="var(--border-hairline)"
                    strokeWidth="0.8"
                    opacity="0.5"
                  />
                  <text
                    x={t.x}
                    y={dimensions.height - chartData.padding.bottom + 18}
                    textAnchor="middle"
                    fill="var(--text-tertiary)"
                    fontSize="10"
                    fontFamily="monospace"
                  >
                    {t.label}
                  </text>
                </g>
              ))}

              {/* Stock Multi-Lines */}
              {Object.keys(chartData.paths).map((sym) => {
                const isCorrelatedHover =
                  hoveredCorrelationPair && hoveredCorrelationPair.includes(sym);
                const isDimmed =
                  hoveredCorrelationPair && !hoveredCorrelationPair.includes(sym);
                const color = symbolColors[sym] || '#00D09C';

                return (
                  <path
                    key={sym}
                    d={chartData.paths[sym]}
                    fill="none"
                    stroke={color}
                    strokeWidth={isCorrelatedHover ? 3.5 : 2}
                    opacity={isDimmed ? 0.25 : 1}
                    className="compare-line-path"
                    style={{
                      transition: 'opacity 0.2s ease, stroke-width 0.2s ease',
                      filter: isCorrelatedHover ? `drop-shadow(0 0 6px ${color})` : 'none',
                    }}
                  />
                );
              })}

              {/* Hover Crosshair Vertical Line */}
              {hoverIndex !== null && chartData && (
                <line
                  x1={chartData.getX(hoverIndex)}
                  y1={chartData.padding.top}
                  x2={chartData.getX(hoverIndex)}
                  y2={dimensions.height - chartData.padding.bottom}
                  stroke="var(--text-secondary)"
                  strokeWidth="1.2"
                  strokeDasharray="2 2"
                />
              )}
            </svg>
          )}

          {/* Hover Crosshair Tooltip Card */}
          {hoverIndex !== null && data && data.series && hoveredDate && (
            <div
              className="compare-hover-tooltip"
              style={{
                left: `${Math.min(chartData.getX(hoverIndex) + 16, dimensions.width - 240)}px`,
                top: `${chartData.padding.top + 10}px`,
              }}
            >
              <div className="tooltip-date-header font-mono">
                📅 {hoveredDate}
              </div>
              <div className="tooltip-stocks-list">
                {Object.keys(data.series).map((sym) => {
                  const pts = data.series[sym] || [];
                  const p = pts.find((x) => x.date === hoveredDate) || pts[pts.length - 1];
                  if (!p) return null;
                  const color = symbolColors[sym] || '#00D09C';
                  const isGain = p.percent >= 0;

                  return (
                    <div key={sym} className="tooltip-stock-row">
                      <div className="tooltip-stock-label">
                        <span className="stock-dot" style={{ backgroundColor: color }} />
                        <span className="stock-sym font-mono">{sym.replace('.NS', '')}</span>
                      </div>
                      <div className="tooltip-stock-values font-mono">
                        <span className="stock-price">${p.price.toFixed(2)}</span>
                        <span className={`stock-pct ${isGain ? 'gain-green' : 'loss-red'}`}>
                          {isGain ? '+' : ''}{p.percent.toFixed(2)}%
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Two-Column Grid: Correlation Matrix & Leaderboard */}
      {data && (
        <div className="compare-insights-grid">
          {/* Left Column: Pearson Correlation Matrix */}
          <div className="compare-card correlation-card">
            <div className="card-header-row">
              <div className="card-header-title">
                <Activity size={16} className="fintech-primary" />
                <span>Pairwise Pearson Correlation Matrix</span>
              </div>
              <span className="card-header-badge">Daily Returns ($r$)</span>
            </div>
            <p className="card-description">
              Measures linear price co-movement between assets (from -1.0 to +1.0). Hover over any pair to illuminate both curves on the chart.
            </p>

            {data.correlations && data.correlations.length > 0 ? (
              <div className="correlation-pairs-table">
                {data.correlations.slice(0, 10).map((pair, idx) => {
                  const c1 = symbolColors[pair.symbol1] || '#94A3B8';
                  const c2 = symbolColors[pair.symbol2] || '#94A3B8';
                  const coeff = pair.coefficient;

                  const badgeClass =
                    coeff >= 0.7
                      ? 'pill-green'
                      : coeff >= 0.3
                      ? 'pill-emerald'
                      : coeff <= -0.3
                      ? 'pill-red'
                      : 'pill-neutral';

                  return (
                    <div
                      key={idx}
                      className="correlation-row"
                      onMouseEnter={() =>
                        setHoveredCorrelationPair([pair.symbol1, pair.symbol2])
                      }
                      onMouseLeave={() => setHoveredCorrelationPair(null)}
                    >
                      <div className="correlation-pair-symbols">
                        <div className="pair-item">
                          <span className="dot" style={{ backgroundColor: c1 }} />
                          <span className="font-mono">{pair.symbol1.replace('.NS', '')}</span>
                        </div>
                        <span className="pair-separator">↔</span>
                        <div className="pair-item">
                          <span className="dot" style={{ backgroundColor: c2 }} />
                          <span className="font-mono">{pair.symbol2.replace('.NS', '')}</span>
                        </div>
                      </div>

                      <div className="correlation-metrics">
                        <span className={`correlation-pill ${badgeClass} font-mono`}>
                          {coeff > 0 ? '+' : ''}{coeff.toFixed(3)}
                        </span>
                        <span className="correlation-strength">{pair.strength} {pair.direction}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="correlation-empty">
                Add 2 or more stocks to compute statistical Pearson correlation.
              </div>
            )}
          </div>

          {/* Right Column: Comparative Performance Leaderboard */}
          <div className="compare-card leaderboard-card">
            <div className="card-header-row">
              <div className="card-header-title">
                <BarChart2 size={16} className="fintech-primary" />
                <span>Performance & Volatility Leaderboard</span>
              </div>
              <span className="card-header-badge">{period.toUpperCase()} Timeframe</span>
            </div>

            <div className="leaderboard-table-wrap">
              <table className="leaderboard-table font-mono">
                <thead>
                  <tr>
                    <th>Security</th>
                    <th>Period Return</th>
                    <th>Peak Gain</th>
                    <th>Max Drawdown</th>
                    <th>Volatility (Ann.)</th>
                  </tr>
                </thead>
                <tbody>
                  {data.stats &&
                    data.stats.map((st, idx) => {
                      const color = symbolColors[st.symbol] || '#94A3B8';
                      const isGain = st.periodReturn >= 0;

                      return (
                        <tr
                          key={st.symbol}
                          onClick={() => onInspectStock && onInspectStock(st.symbol)}
                          className="leaderboard-row"
                          title="Click to view deep analysis"
                        >
                          <td className="security-col">
                            <span className="rank-num font-mono">#{idx + 1}</span>
                            <span className="table-dot" style={{ backgroundColor: color }} />
                            <span className="table-sym">{st.symbol.replace('.NS', '')}</span>
                          </td>
                          <td className={isGain ? 'gain-green font-bold' : 'loss-red font-bold'}>
                            {isGain ? '+' : ''}{st.periodReturn.toFixed(2)}%
                          </td>
                          <td className="gain-green">+{st.peakGain.toFixed(2)}%</td>
                          <td className="loss-red">{st.maxDrawdown.toFixed(2)}%</td>
                          <td className="text-secondary">{st.annualizedVolatility.toFixed(1)}%</td>
                        </tr>
                      );
                    })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
