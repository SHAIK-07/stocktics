import React, { useState, useEffect, useMemo } from 'react';
import {
  RefreshCw, ShieldCheck,
  ShoppingBag, Sparkles, AlertCircle, ArrowUpRight,
  TrendingUp, TrendingDown, ChevronDown, ChevronUp, Bookmark, Filter, GraduationCap
} from 'lucide-react';
import { portfolioService, watchlistService, authService } from '../../services/supabase';
import { fetchBatchQuotes, fetchTopWeek, syncIndmoneyPortfolio } from '../../services/api';
import MarketHeatmapView from '../heatmap/MarketHeatmapView';
import { subscribeToIndmoneySync, notifyIndmoneySynced } from '../../utils/syncEvents';
import cacheService from '../../services/cacheService';


function formatDisplayName(user) {
  if (!user) return 'Trader';
  if (user.user_metadata?.first_name) {
    const fn = user.user_metadata.first_name.trim();
    return fn.charAt(0).toUpperCase() + fn.slice(1).toLowerCase();
  }
  if (user.user_metadata?.full_name) {
    const fn = user.user_metadata.full_name.trim().split(' ')[0];
    return fn.charAt(0).toUpperCase() + fn.slice(1).toLowerCase();
  }
  const rawEmail = user.email?.split('@')[0] || 'Investor';
  const part = rawEmail.split(/[._\-\d]/)[0];
  if (part && part.length > 1) {
    return part.charAt(0).toUpperCase() + part.slice(1).toLowerCase();
  }
  return rawEmail.charAt(0).toUpperCase() + rawEmail.slice(1);
}

export default function DashboardView({
  market = 'IN',
  onSelectStock,
  onNavigateTab,
  onOpenAuth
}) {
  const [user, setUser] = useState(authService.getCurrentUser());
  const isIndian = market === 'IN';
  const currencySymbol = isIndian ? '₹' : '$';

  const [isRefreshing, setIsRefreshing] = useState(false);

  // Synchronously initialize from local cache (0ms instant render - no zero flash)
  const holdingsCacheKey = cacheService.keys.dashboardHoldings(user?.userId, market);
  const watchlistCacheKey = cacheService.keys.dashboardWatchlist(user?.userId, market);
  const txCacheKey = cacheService.keys.dashboardTransactions(user?.userId);
  const moversCacheKey = cacheService.keys.dashboardMovers(market);

  const [watchlist, setWatchlist] = useState(() => cacheService.get(watchlistCacheKey, []));
  const [holdings, setHoldings] = useState(() => cacheService.get(holdingsCacheKey, []));
  const [transactions, setTransactions] = useState(() => cacheService.get(txCacheKey, []));
  const [topMovers, setTopMovers] = useState(() => cacheService.get(moversCacheKey, []));
  const [lastUpdated, setLastUpdated] = useState(() => {
    const meta = cacheService.getWithMeta(holdingsCacheKey);
    return meta ? meta.timestamp : null;
  });

  const [performanceFilter, setPerformanceFilter] = useState('all'); // 'all' | 'top' | 'under'
  const [expandedCategory, setExpandedCategory] = useState(null);


  // Market status calculation
  const isMarketOpen = () => {
    const now = new Date();
    const utcHours = now.getUTCHours();
    const utcMins = now.getUTCMinutes();
    const totalUtc = utcHours * 60 + utcMins;
    const day = now.getUTCDay(); // 0 is Sunday, 6 is Saturday
    if (day === 0 || day === 6) return false;

    if (isIndian) {
      // Check National Public Holidays (e.g. Oct 2 Gandhi Jayanti, Jan 26, Aug 15, Dec 25)
      const istDate = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Kolkata' }));
      const month = istDate.getMonth() + 1;
      const date = istDate.getDate();
      if ((month === 10 && date === 2) || (month === 1 && date === 26) || (month === 8 && date === 15) || (month === 12 && date === 25)) {
        return false;
      }
      // IST is UTC + 5:30. Indian market 09:15 to 15:30 IST = 03:45 to 10:00 UTC
      return totalUtc >= 225 && totalUtc <= 600;
    } else {
      // US market 09:30 to 16:00 EST = 14:30 to 21:00 UTC
      return totalUtc >= 870 && totalUtc <= 1260;
    }
  };

  const loadData = async () => {
    setIsRefreshing(true);
    try {
      const currentUserId = user?.userId;

      // 1. Fetch Watchlist & Holdings
      const [wlData, hData, txData] = await Promise.all([
        watchlistService.fetchWatchlist(currentUserId, isIndian),
        portfolioService.fetchHoldings(currentUserId, isIndian),
        portfolioService.fetchTransactions(currentUserId)
      ]);

      // 2. Fetch live quotes for watchlist and holdings symbols in batch
      const allSymbols = Array.from(new Set([
        ...wlData.map(s => s.symbol),
        ...hData.map(h => h.symbol)
      ]));

      let quotes = {};
      if (allSymbols.length > 0) {
        quotes = await fetchBatchQuotes(allSymbols);
      }

      // Enrich Watchlist
      const enrichedWl = wlData.map(item => {
        const q = quotes[item.symbol] || quotes[item.symbol?.replace('.NS', '')];
        if (q && q.price > 0) {
          return {
            ...item,
            price: q.price,
            change: `${q.change >= 0 ? '+' : ''}${q.changePercent}%`,
            isPositive: q.isPositive,
            name: item.name || q.name
          };
        }
        return item;
      });

      // Enrich Holdings
      const enrichedHoldings = hData.map(item => {
        const q = quotes[item.symbol] || quotes[item.symbol?.replace('.NS', '')];
        if (q && q.price > 0) {
          return {
            ...item,
            currentPrice: q.price,
            previousClose: q.previousClose || item.entryPrice
          };
        }
        return item;
      });

      // Deduplicate watchlist by symbol
      const seenWl = new Set();
      const distinctWl = enrichedWl.filter((s) => {
        if (!s.symbol || seenWl.has(s.symbol)) return false;
        seenWl.add(s.symbol);
        return true;
      });

      setWatchlist(distinctWl);
      setHoldings(enrichedHoldings);
      setTransactions(txData);
      setTopMovers(distinctMovers);

      // Persist to local cache for instant 0ms hydration on next visit
      cacheService.set(cacheService.keys.dashboardWatchlist(currentUserId, market), distinctWl);
      cacheService.set(cacheService.keys.dashboardHoldings(currentUserId, market), enrichedHoldings);
      cacheService.set(cacheService.keys.dashboardTransactions(currentUserId), txData);
      cacheService.set(cacheService.keys.dashboardMovers(market), distinctMovers);
      setLastUpdated(Date.now());
    } catch (err) {
      console.error('Error loading dashboard data:', err);
    } finally {
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    const unsub = authService.onAuthStateChange((newUser) => {
      setUser(newUser);
    });
    return () => unsub();
  }, []);



  const handleManualSyncAndRefresh = async () => {
    setIsRefreshing(true);
    try {
      if (user?.userId) {
        try {
          await syncIndmoneyPortfolio(user.userId);
          notifyIndmoneySynced({ type: 'MANUAL_DASHBOARD_SYNC' });
        } catch (e) {
          console.warn('Manual INDmoney sync error:', e);
        }
      }
      await loadData();
    } finally {
      setIsRefreshing(false);
    }
  };

  const categoryAnalytics = useMemo(() => {
    const groups = {};
    watchlist.forEach((s) => {
      const cat = s.category || s.watchlistName || 'General';
      if (!groups[cat]) groups[cat] = { name: cat, stocks: [], totalChange: 0, gainerCount: 0, loserCount: 0 };
      groups[cat].stocks.push(s);
      const changeVal = typeof s.change === 'string' ? parseFloat(s.change) : (s.changePercent || 0);
      const isPos = typeof s.change === 'string' ? !s.change.startsWith('-') : (s.isPositive ?? changeVal >= 0);
      groups[cat].totalChange += (isNaN(changeVal) ? 0 : changeVal);
      if (isPos) groups[cat].gainerCount++;
      else groups[cat].loserCount++;
    });

    return Object.values(groups).map(g => {
      const sortedStocks = [...g.stocks].sort((a, b) => {
        const aVal = typeof a.change === 'string' ? parseFloat(a.change) : (a.changePercent || 0);
        const bVal = typeof b.change === 'string' ? parseFloat(b.change) : (b.changePercent || 0);
        return (isNaN(bVal) ? 0 : bVal) - (isNaN(aVal) ? 0 : aVal);
      });
      return {
        ...g,
        stocks: sortedStocks,
        topStock: sortedStocks.length > 0 ? sortedStocks[0] : null,
        bottomStock: sortedStocks.length > 1 ? sortedStocks[sortedStocks.length - 1] : null,
        count: g.stocks.length,
        avgChange: g.stocks.length > 0 ? g.totalChange / g.stocks.length : 0,
        isPositive: (g.totalChange / Math.max(g.stocks.length, 1)) >= 0
      };
    }).sort((a, b) => b.avgChange - a.avgChange);
  }, [watchlist]);

  const filteredCategories = useMemo(() => {
    let cats = [...categoryAnalytics];
    if (performanceFilter === 'top') {
      cats = cats.filter(c => c.avgChange > 0).sort((a, b) => b.avgChange - a.avgChange);
    } else if (performanceFilter === 'under') {
      cats = cats.filter(c => c.avgChange < 0).sort((a, b) => a.avgChange - b.avgChange);
    }
    return cats;
  }, [categoryAnalytics, performanceFilter]);

  useEffect(() => {
    // Immediate instant cache hydration when market or user changes
    const curHKey = cacheService.keys.dashboardHoldings(user?.userId, market);
    const curWKey = cacheService.keys.dashboardWatchlist(user?.userId, market);
    const curMKey = cacheService.keys.dashboardMovers(market);

    const cachedH = cacheService.get(curHKey);
    if (cachedH && cachedH.length > 0) setHoldings(cachedH);
    const cachedW = cacheService.get(curWKey);
    if (cachedW && cachedW.length > 0) setWatchlist(cachedW);
    const cachedM = cacheService.get(curMKey);
    if (cachedM && cachedM.length > 0) setTopMovers(cachedM);
    const meta = cacheService.getWithMeta(curHKey);
    if (meta) setLastUpdated(meta.timestamp);

    loadData();
    const unsub = subscribeToIndmoneySync(() => {
      loadData();
    });
    return () => unsub();
  }, [market, user]);



  // Portfolio Summary Calculations
  const activeCount = holdings.length;
  const totalInvested = holdings.reduce((sum, h) => sum + (h.entryPrice * h.qty), 0);
  const currentValue = holdings.reduce((sum, h) => sum + (h.currentPrice * h.qty), 0);
  const totalUnrealizedPnl = currentValue - totalInvested;
  const totalUnrealizedPnlPct = totalInvested > 0 ? (totalUnrealizedPnl / totalInvested) * 100 : 0;
  const isTotalPos = totalUnrealizedPnl >= 0;

  // 1-Day Return calculation
  const oneDayReturn = holdings.reduce((sum, h) => {
    const prevClose = h.previousClose || h.entryPrice;
    return sum + ((h.currentPrice - prevClose) * h.qty);
  }, 0);
  const prevDayTotal = holdings.reduce((sum, h) => sum + ((h.previousClose || h.entryPrice) * h.qty), 0);
  const oneDayReturnPct = prevDayTotal > 0 ? (oneDayReturn / prevDayTotal) * 100 : 0;
  const isOneDayPos = oneDayReturn >= 0;

  // Realized P&L from transactions
  const realizedPnl = transactions
    .filter(t => t.tx_type === 'SELL')
    .reduce((sum, t) => sum + (parseFloat(t.realized_pnl) || 0), 0);

  const marketLive = isMarketOpen();

  return (
    <div className="fintech-dashboard-container">
      {/* Top Header Bar */}
      <div className="fintech-dash-header">
        <div>
          <div className="fintech-greeting-row">
            <h1 className="fintech-greeting-title">
              Hello, {formatDisplayName(user)}
            </h1>
            <div className="fintech-market-status-pill">
              <span className={`status-dot ${marketLive ? 'active' : 'inactive'}`} />
              <span>{isIndian ? 'NSE/BSE' : 'US Markets'} • {marketLive ? 'Live Trading' : 'Closed'}</span>
            </div>
          </div>
          <p className="fintech-greeting-sub">Institutional Multi-Factor Quant Dashboard</p>
        </div>

        <div className="fintech-dash-actions" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {lastUpdated && (
            <span style={{ fontSize: '11px', color: '#64748B', fontFamily: 'monospace' }}>
              {isRefreshing ? 'Syncing...' : `Synced ${cacheService.formatTimeAgo(lastUpdated)}`}
            </span>
          )}
          <button
            className="fintech-icon-btn"
            onClick={handleManualSyncAndRefresh}
            title="Sync with INDmoney & Refresh Portfolio"
            disabled={isRefreshing}
          >
            <RefreshCw size={17} className={isRefreshing ? 'spin-anim' : ''} />
          </button>
        </div>
      </div>

      {/* Institutional Portfolio Summary Card */}
      <div className="fintech-portfolio-hero-card">
        <div className="hero-top-row">
          <div>
            <span className="hero-eyebrow">{isIndian ? 'Indian' : 'US'} Equities Net Portfolio</span>
            <div className="hero-main-val font-mono">
              {currencySymbol} {currentValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
          </div>
          <div className="hero-badge-group">
            <span className={`fintech-pill ${activeCount > 0 ? 'gain' : 'neutral'}`}>
              {activeCount} {activeCount === 1 ? 'Position' : 'Positions'}
            </span>
            {totalInvested > 0 && (
              <span className="hero-invested-hint font-mono">
                Invested: {currencySymbol}{totalInvested.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            )}
          </div>
        </div>

        {/* 3-Metric Return Strip */}
        <div className="hero-metrics-strip">
          <div className="metric-strip-item">
            <span className="strip-label">1-Day Return</span>
            <div className={`strip-val font-mono ${isOneDayPos ? 'text-gain' : 'text-loss'}`}>
              {isOneDayPos ? '+' : ''}{currencySymbol}{Math.abs(oneDayReturn).toFixed(2)} ({isOneDayPos ? '+' : ''}{oneDayReturnPct.toFixed(2)}%)
            </div>
          </div>

          <div className="strip-divider" />

          <div className="metric-strip-item">
            <span className="strip-label">Total Unrealized Return</span>
            <div className={`strip-val font-mono ${isTotalPos ? 'text-gain' : 'text-loss'}`}>
              {isTotalPos ? '+' : ''}{currencySymbol}{totalUnrealizedPnl.toFixed(2)} ({isTotalPos ? '+' : ''}{totalUnrealizedPnlPct.toFixed(2)}%)
            </div>
          </div>

          <div className="strip-divider" />

          <div className="metric-strip-item">
            <span className="strip-label">Realized P&L</span>
            <div className={`strip-val font-mono ${realizedPnl >= 0 ? 'text-gain' : 'text-loss'}`}>
              {realizedPnl >= 0 ? '+' : ''}{currencySymbol}{realizedPnl.toFixed(2)}
            </div>
          </div>
        </div>
      </div>

      {/* Quick Access Feature Navigation */}
      <div className="fintech-nav-shortcuts-grid">
        <div className="shortcut-card" onClick={() => onNavigateTab('buy')}>
          <div className="shortcut-icon-badge gain">
            <ShoppingBag size={20} />
          </div>
          <div className="shortcut-info">
            <h4>Buy Pipeline</h4>
            <p>Quant ML ranked watchlist candidates</p>
          </div>
          <ArrowUpRight size={18} className="shortcut-arrow" />
        </div>

        <div className="shortcut-card" onClick={() => onNavigateTab('sell')}>
          <div className="shortcut-icon-badge amber">
            <ShieldCheck size={20} />
          </div>
          <div className="shortcut-info">
            <h4>Holdings & Exit Risk</h4>
            <p>Time-to-Leave (TTL) signals & stop-loss radar</p>
          </div>
          <ArrowUpRight size={18} className="shortcut-arrow" />
        </div>

        <div className="shortcut-card" onClick={() => onNavigateTab('scanner')}>
          <div className="shortcut-icon-badge blue">
            <Sparkles size={20} />
          </div>
          <div className="shortcut-info">
            <h4>Pattern Scanner</h4>
            <p>Dual-market chart breakout detection</p>
          </div>
          <ArrowUpRight size={18} className="shortcut-arrow" />
        </div>

        <div className="shortcut-card" onClick={() => onNavigateTab('learning')}>
          <div className="shortcut-icon-badge primary" style={{ background: 'var(--gain-green-bg)', color: 'var(--fintech-primary)' }}>
            <GraduationCap size={20} />
          </div>
          <div className="shortcut-info">
            <h4>Quantitative Academy</h4>
            <p>40-lesson institutional financial curriculum</p>
          </div>
          <ArrowUpRight size={18} className="shortcut-arrow" />
        </div>
      </div>

      {/* Main Grid: Watchlist Categories & Market Movers */}
      <div className="fintech-dash-grid">
        {/* Left Column: Watchlist Categories Module */}
        <div className="fintech-dash-card watchlist-module">
          <div className="card-header-flex">
            <div>
              <h3 className="card-title">My Watchlist</h3>
              <p className="card-subtitle">Categories synced with INDmoney</p>
            </div>
            <span className="fintech-counter-badge">{watchlist.length}</span>
          </div>

          {/* Performance Filter Bar */}
          <div className="wl-perf-filter-bar">
            <button
              className={`wl-perf-filter-btn ${performanceFilter === 'all' ? 'active' : ''}`}
              onClick={() => setPerformanceFilter('all')}
            >
              <Filter size={12} />
              <span>All Categories</span>
              <span className="cat-count font-mono">{categoryAnalytics.length}</span>
            </button>
            <button
              className={`wl-perf-filter-btn gain-filter ${performanceFilter === 'top' ? 'active' : ''}`}
              onClick={() => setPerformanceFilter('top')}
            >
              <TrendingUp size={12} />
              <span>Top Performing</span>
            </button>
            <button
              className={`wl-perf-filter-btn loss-filter ${performanceFilter === 'under' ? 'active' : ''}`}
              onClick={() => setPerformanceFilter('under')}
            >
              <TrendingDown size={12} />
              <span>Underperforming</span>
            </button>
          </div>

          {/* Category Cards */}
          {filteredCategories.length === 0 ? (
            <div className="empty-state-box">
              <AlertCircle size={36} className="text-secondary" />
              <h4>{watchlist.length === 0 ? 'Watchlist is Empty' : 'No Categories Match Filter'}</h4>
              <p>
                {watchlist.length === 0
                  ? 'Your INDmoney synced watchlist will render here automatically.'
                  : `No watchlist categories are ${performanceFilter === 'top' ? 'performing positively' : 'underperforming'} today.`}
              </p>
              {watchlist.length === 0 && (
                <button className="fintech-btn-primary" onClick={() => onNavigateTab('scanner')}>
                  Explore Setups
                </button>
              )}
            </div>
          ) : (
            <div className="wl-category-cards-list">
              {filteredCategories.map((cat) => {
                const isExpanded = expandedCategory === cat.name;
                return (
                  <div key={cat.name} className="wl-category-card-wrapper">
                    <div
                      className={`wl-category-card ${isExpanded ? 'expanded' : ''}`}
                      onClick={() => setExpandedCategory(isExpanded ? null : cat.name)}
                    >
                      <div className="wl-cat-left">
                        <div className="wl-cat-info">
                          <span className="wl-cat-name">{cat.name}</span>
                          <span className="wl-cat-count-label font-mono">{cat.count} {cat.count === 1 ? 'stock' : 'stocks'}</span>
                        </div>
                      </div>
                      <div className="wl-cat-right">
                        <span className={`fintech-pill ${cat.isPositive ? 'gain' : 'loss'}`}>
                          {cat.isPositive ? <TrendingUp size={11} /> : <TrendingDown size={11} />}
                          {cat.isPositive ? '+' : ''}{cat.avgChange.toFixed(2)}%
                        </span>
                        <div className="wl-cat-gl-badges">
                          {cat.gainerCount > 0 && <span className="wl-gl-mini gain">{cat.gainerCount}▲</span>}
                          {cat.loserCount > 0 && <span className="wl-gl-mini loss">{cat.loserCount}▼</span>}
                        </div>
                        {isExpanded ? <ChevronUp size={14} className="wl-cat-chevron" /> : <ChevronDown size={14} className="wl-cat-chevron" />}
                      </div>
                    </div>

                    {/* Quick-Glance Top & Bottom Performer Horizontal Cards */}
                    {cat.topStock && (
                      <div className="wl-cat-preview-row">
                        <div
                          className="wl-cat-mini-card"
                          onClick={(e) => { e.stopPropagation(); onSelectStock(cat.topStock.symbol); }}
                        >
                          <img
                            className="wl-mini-logo"
                            src={cat.topStock.logoUrl || `https://assets.parqet.com/logos/symbol/${cat.topStock.symbol}?format=png`}
                            alt={cat.topStock.symbol}
                            onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
                          />
                          <div className="wl-mini-details">
                            <div className="wl-mini-top">
                              <span className="wl-mini-sym font-mono">{cat.topStock.symbol}</span>
                              <span className={`wl-mini-change font-mono ${cat.topStock.isPositive !== false ? 'gain' : 'loss'}`}>
                                {cat.topStock.change || '+0.00%'}
                              </span>
                            </div>
                            <div className="wl-mini-bot">
                              <span className="wl-mini-price font-mono">{currencySymbol}{cat.topStock.price}</span>
                              {cat.stocks.length > 1 && <span className="wl-mini-tag gain">TOP</span>}
                            </div>
                          </div>
                        </div>
                        {cat.bottomStock && cat.bottomStock !== cat.topStock && (
                          <div
                            className="wl-cat-mini-card"
                            onClick={(e) => { e.stopPropagation(); onSelectStock(cat.bottomStock.symbol); }}
                          >
                            <img
                              className="wl-mini-logo"
                              src={cat.bottomStock.logoUrl || `https://assets.parqet.com/logos/symbol/${cat.bottomStock.symbol}?format=png`}
                              alt={cat.bottomStock.symbol}
                              onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
                            />
                            <div className="wl-mini-details">
                              <div className="wl-mini-top">
                                <span className="wl-mini-sym font-mono">{cat.bottomStock.symbol}</span>
                                <span className={`wl-mini-change font-mono ${cat.bottomStock.isPositive !== false ? 'gain' : 'loss'}`}>
                                  {cat.bottomStock.change || '+0.00%'}
                                </span>
                              </div>
                              <div className="wl-mini-bot">
                                <span className="wl-mini-price font-mono">{currencySymbol}{cat.bottomStock.price}</span>
                                <span className={`wl-mini-tag ${cat.bottomStock.isPositive !== false ? 'amber' : 'loss'}`}>LOW</span>
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    )}

                    {/* Expandable stock list inside category */}
                    {isExpanded && (
                      <div className="wl-cat-expanded-stocks">
                        {cat.stocks.map((stock, idx) => {
                          const isPos = typeof stock.change === 'string'
                            ? !stock.change.startsWith('-')
                            : (stock.isPositive ?? true);
                          return (
                            <div
                              key={`${stock.symbol}-${stock.id || idx}`}
                              className="wl-cat-stock-row"
                              onClick={(e) => { e.stopPropagation(); onSelectStock(stock.symbol); }}
                            >
                              <div className="wl-cat-stock-left">
                                <img
                                  className="wl-cat-stock-logo"
                                  src={stock.logoUrl || `https://assets.parqet.com/logos/symbol/${stock.symbol}?format=png`}
                                  alt={stock.symbol}
                                  onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
                                />
                                <span className="font-mono wl-cat-stock-sym">{stock.symbol}</span>
                              </div>
                              <div className="wl-cat-stock-right">
                                <span className="font-mono wl-cat-stock-price">{currencySymbol}{typeof stock.price === 'number' ? stock.price.toFixed(2) : stock.price}</span>
                                <span className={`fintech-pill-sm ${isPos ? 'gain' : 'loss'}`}>{stock.change || '+0.00%'}</span>
                              </div>
                            </div>
                          );
                        })}
                        <button
                          className="wl-cat-view-pipeline-btn"
                          onClick={(e) => { e.stopPropagation(); onNavigateTab('buy'); }}
                        >
                          View in Buy Pipeline <ArrowUpRight size={13} />
                        </button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Right Column: Top Market Movers / High Conviction */}
        <div className="fintech-dash-card movers-module">
          <div className="card-header-flex">
            <div>
              <h3 className="card-title">Top Setups & Movers</h3>
              <p className="card-subtitle">Highest quantified momentum setups this week</p>
            </div>
            <button className="text-link-btn" onClick={() => onNavigateTab('scanner')}>
              View All
            </button>
          </div>

          <div className="movers-list">
            {topMovers.length === 0 ? (
              <div className="loading-placeholder">Loading top screened securities...</div>
            ) : (
              topMovers.map((mover, idx) => (
                <div
                  key={`${mover.ticker || 'mover'}-${idx}`}
                  className="mover-card"
                  onClick={() => onSelectStock(mover.ticker)}
                >
                  <div className="mover-rank">#{idx + 1}</div>
                  <div className="mover-info">
                    <div className="mover-top">
                      <span className="font-mono font-bold">{mover.ticker}</span>
                      <span className="fintech-pill gain">{mover.breakout_type || 'BREAKOUT'}</span>
                    </div>
                    <div className="mover-sub">
                      <span>Score: {mover.score || 88}</span> • <span>Win Rate: {mover.win_rate || '68%'}</span>
                    </div>
                  </div>
                  <div className="mover-action">
                    <ArrowUpRight size={16} />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Embedded Market Capital Heatmap Treemap */}
      <div className="fintech-dashboard-heatmap-section">
        <MarketHeatmapView
          onInspectStock={onSelectStock}
          user={user}
          isEmbedded={true}
        />
      </div>
    </div>
  );
}
