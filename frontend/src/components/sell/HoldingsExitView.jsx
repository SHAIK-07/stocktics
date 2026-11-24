import React, { useState, useEffect, useCallback } from 'react';
import {
  ShieldAlert, TrendingDown, RefreshCw, Search, Edit3, Trash2,
  Target, AlertCircle, X, Plus, ArrowRight
} from 'lucide-react';
import { portfolioService, authService } from '../../services/supabase';
import { fetchBatchQuotes, syncIndmoneyPortfolio } from '../../services/api';
import StockSellDetailView from './StockSellDetailView';
import IndmoneySyncButton from '../common/IndmoneySyncButton';
import { subscribeToIndmoneySync, notifyIndmoneySynced } from '../../utils/syncEvents';
import cacheService from '../../services/cacheService';



export default function HoldingsExitView({
  onSelectStock,
  onSelectHolding,
  onNavigateTab,
  onNavigateToBuy,
  selectedMarket: propMarket,
  onMarketChange: propMarketChange,
  isEmbedded = false
}) {
  const [user, setUser] = useState(authService.getCurrentUser());
  const [selectedMarket, setSelectedMarket] = useState(propMarket || 'US'); // 'IN' | 'US'
  const isIndian = selectedMarket === 'IN';
  const currencySymbol = isIndian ? '₹' : '$';

  const [selectedHolding, setSelectedHolding] = useState(null);

  useEffect(() => {
    if (propMarket) {
      setSelectedMarket(propMarket);
      setSelectedHolding(null);
    }
  }, [propMarket]);

  const handleMarketToggle = (m) => {
    setSelectedMarket(m);
    setSelectedHolding(null);
    if (propMarketChange) propMarketChange(m);
  };

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTab, setSelectedTab] = useState('All Positions'); // 'All Positions' | 'Exit Signals' | 'Safe Holds'
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Instant SWR cache hydration at 0ms
  const exitCacheKey = cacheService.keys.exitHoldings(user?.userId, selectedMarket);
  const [holdings, setHoldings] = useState(() => cacheService.get(exitCacheKey, []));
  const [transactions, setTransactions] = useState(() => cacheService.get(cacheService.keys.dashboardTransactions(user?.userId), []));

  useEffect(() => {
    const unsub = authService.onAuthStateChange((u) => setUser(u));
    return () => unsub();
  }, []);

  const loadHoldings = async () => {
    setIsRefreshing(true);
    try {
      const [hData, txData] = await Promise.all([
        portfolioService.fetchHoldings(user?.userId, isIndian),
        portfolioService.fetchTransactions(user?.userId)
      ]);

      if (hData.length > 0) {
        const symbols = hData.map(h => h.symbol);
        const quotes = await fetchBatchQuotes(symbols);

        const enriched = hData.map(item => {
          const q = quotes[item.symbol] || quotes[item.symbol?.replace('.NS', '')];
          const currPrice = q?.price > 0 ? q.price : item.entryPrice;
          const prevClose = q?.previousClose || item.entryPrice;
          const investedVal = item.entryPrice * item.qty;
          const currentVal = currPrice * item.qty;
          const unrealizedPnl = currentVal - investedVal;
          const unrealizedPnlPct = investedVal > 0 ? (unrealizedPnl / investedVal) * 100 : 0;

          // Days held calculation
          const entryDate = item.entryDate ? new Date(item.entryDate) : new Date();
          const today = new Date();
          const diffDays = Math.max(1, Math.floor((today - entryDate) / (1000 * 60 * 60 * 24)));

          // Dynamic TTL (Time to Leave) Exit Score
          let ttl = item.timeToLeaveScore || 20;
          if (unrealizedPnlPct <= -(item.stopLossPct || 5)) {
            ttl = 90; // Stop loss hit
          } else if (unrealizedPnlPct >= (item.targetPct || 15)) {
            ttl = 75; // Target achieved
          } else if (diffDays >= (item.maxHoldDays || 60)) {
            ttl = 65; // Time horizon reached
          }

          let exitVerdict = 'Safe Hold';
          if (ttl >= 80) exitVerdict = 'Exit Now';
          else if (ttl >= 60) exitVerdict = 'Consider Exiting';
          else if (ttl >= 35) exitVerdict = 'Hold Position';

          return {
            ...item,
            currentPrice: currPrice,
            previousClose: prevClose,
            investedValue: investedVal,
            currentValue: currentVal,
            unrealizedPnl: unrealizedPnl,
            unrealizedPnlPct: unrealizedPnlPct,
            isPositive: unrealizedPnl >= 0,
            daysHeld: diffDays,
            timeToLeaveScore: ttl,
            exitVerdict: exitVerdict
          };
        });

        setHoldings(enriched);
        cacheService.set(cacheService.keys.exitHoldings(user?.userId, selectedMarket), enriched);
      } else {
        setHoldings([]);
        cacheService.set(cacheService.keys.exitHoldings(user?.userId, selectedMarket), []);
      }
      setTransactions(txData);
      cacheService.set(cacheService.keys.dashboardTransactions(user?.userId), txData);
    } catch (err) {
      console.error('Error loading holdings:', err);
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleManualRefresh = async () => {
    setIsRefreshing(true);
    try {
      if (user?.userId) {
        try {
          await syncIndmoneyPortfolio(user.userId);
          notifyIndmoneySynced({ type: 'MANUAL_HOLDINGS_SYNC' });
        } catch (e) {
          console.warn('Manual INDmoney sync error:', e);
        }
      }
      await loadHoldings();
    } finally {
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    // Instant cache switch
    const curKey = cacheService.keys.exitHoldings(user?.userId, selectedMarket);
    const cached = cacheService.get(curKey);
    if (cached && cached.length > 0) setHoldings(cached);

    loadHoldings();
    const unsub = subscribeToIndmoneySync(() => {
      loadHoldings();
    });
    return () => unsub();
  }, [selectedMarket, user]);




  // Summary Metrics
  const totalInvested = holdings.reduce((sum, h) => sum + (h.investedValue || 0), 0);
  const totalCurrentValue = holdings.reduce((sum, h) => sum + (h.currentValue || 0), 0);
  const totalUnrealizedPnl = totalCurrentValue - totalInvested;
  const totalUnrealizedPnlPct = totalInvested > 0 ? (totalUnrealizedPnl / totalInvested) * 100 : 0;
  const isPos = totalUnrealizedPnl >= 0;

  const realizedPnl = transactions
    .filter(t => t.tx_type === 'SELL')
    .reduce((sum, t) => sum + (parseFloat(t.realized_pnl) || 0), 0);

  // Filter positions
  const filteredHoldings = holdings.filter(holding => {
    const matchesSearch = !searchQuery.trim() ||
      holding.symbol.toLowerCase().includes(searchQuery.toLowerCase().trim()) ||
      holding.name.toLowerCase().includes(searchQuery.toLowerCase().trim());

    const matchesTab = selectedTab === 'Exit Signals'
      ? holding.timeToLeaveScore >= 60
      : selectedTab === 'Safe Holds'
      ? holding.timeToLeaveScore < 60
      : true;

    return matchesSearch && matchesTab;
  });

  if (selectedHolding) {
    return (
      <div className={isEmbedded ? 'holdings-embedded-content' : 'fintech-view-container'}>
        <StockSellDetailView
          holding={selectedHolding}
          onBack={() => setSelectedHolding(null)}
          onSelectStock={onSelectStock}
          selectedMarket={selectedMarket}
        />
      </div>
    );
  }

  return (
    <div className={isEmbedded ? 'holdings-embedded-content' : 'fintech-view-container'}>
      {/* Page Header */}
      <div className="fintech-view-header">
        <div>
          <h1 className="fintech-view-title">Holdings & Exit Risk</h1>
          <p className="fintech-view-subtitle">
            Active positions monitored by Time-to-Leave (TTL) quantitative risk engine
          </p>
        </div>

        <div className="fintech-view-actions">
          {/* INDmoney MCP Auto-Sync */}
          <IndmoneySyncButton userId={user?.userId} onSyncComplete={loadHoldings} />

          <button
            className="fintech-icon-btn"
            onClick={handleManualRefresh}
            title="Manual Sync & Refresh Holdings"
            disabled={isRefreshing}
          >
            <RefreshCw size={17} className={isRefreshing ? 'spin-anim' : ''} />
          </button>
        </div>
      </div>


      {/* Metric Summary Strip (Always rendered for institutional portfolio context) */}
      <div className="fintech-portfolio-hero-card">
        <div className="hero-metrics-strip" style={{ marginTop: 0 }}>
          <div className="metric-strip-item">
            <span className="strip-label">Current Portfolio Value</span>
            <div className="strip-val font-mono">
              {currencySymbol} {totalCurrentValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
          </div>

          <div className="strip-divider" />

          <div className="metric-strip-item">
            <span className="strip-label">Total Unrealized P&L</span>
            <div className={`strip-val font-mono ${isPos ? 'text-gain' : 'text-loss'}`}>
              {isPos ? '+' : ''}{currencySymbol}{totalUnrealizedPnl.toFixed(2)} ({isPos ? '+' : ''}{totalUnrealizedPnlPct.toFixed(2)}%)
            </div>
          </div>

          <div className="strip-divider" />

          <div className="metric-strip-item">
            <span className="strip-label">Realized Profit/Loss</span>
            <div className={`strip-val font-mono ${realizedPnl >= 0 ? 'text-gain' : 'text-loss'}`}>
              {realizedPnl >= 0 ? '+' : ''}{currencySymbol}{realizedPnl.toFixed(2)}
            </div>
          </div>

          <div className="strip-divider" />

          <div className="metric-strip-item">
            <span className="strip-label">Active Holdings</span>
            <div className="strip-val font-mono">
              {holdings.length} {holdings.length === 1 ? 'Position' : 'Positions'}
            </div>
          </div>
        </div>
      </div>

      {/* Segmented Filter Tabs & Search */}
      <div className="fintech-filter-row">
        <div className="fintech-search-bar">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Search active positions..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <button className="clear-btn" onClick={() => setSearchQuery('')}>
              <X size={14} />
            </button>
          )}
        </div>

        <div className="fintech-segmented-pills">
          {['All Positions', 'Exit Signals', 'Safe Holds'].map((tab) => (
            <button
              key={tab}
              className={`segmented-pill ${selectedTab === tab ? 'active' : ''}`}
              onClick={() => setSelectedTab(tab)}
            >
              {tab}
            </button>
          ))}
        </div>
      </div>

      {/* Holdings List */}
      {filteredHoldings.length === 0 ? (
        <div className="empty-state-box">
          <AlertCircle size={40} className="text-secondary" />
          <h4>No Active Holdings Found</h4>
          <p>
            {holdings.length === 0
              ? `You do not have any logged ${isIndian ? 'Indian' : 'US'} positions yet. Log a buy from Buy Pipeline or Watchlist to track performance.`
              : 'No positions matched your search or selected filter.'}
          </p>
          <div style={{ display: 'flex', gap: '12px', marginTop: '8px', flexWrap: 'wrap', justifyContent: 'center', alignItems: 'center' }}>
            <IndmoneySyncButton userId={user?.userId} onSyncComplete={loadHoldings} />
            <button
              className="stocktics-button"
              onClick={() => {
                if (onNavigateToBuy) onNavigateToBuy();
                else if (onNavigateTab) onNavigateTab('buy');
              }}
            >
              <span>Explore Buy Pipeline</span>
              <ArrowRight size={15} />
            </button>
          </div>
        </div>
      ) : (
        <div className="holdings-grid">
          {filteredHoldings.map((holding, idx) => {
            const ttlScore = holding.timeToLeaveScore || 20;
            const ttlVariant = ttlScore >= 80 ? 'danger' : ttlScore >= 60 ? 'warning' : 'safe';

            return (
              <div
                key={`${holding.id || holding.symbol}-${idx}`}
                className="holding-card"
                onClick={() => {
                  if (onSelectHolding) onSelectHolding(holding);
                  else setSelectedHolding(holding);
                }}
                style={{ cursor: 'pointer' }}
              >
                {/* Card Top */}
                <div className="card-top-row">
                  <div
                    className="card-identity"
                  >
                    <div className="stock-avatar">
                      <img
                        src={holding.logoUrl}
                        alt={holding.symbol}
                        onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
                      />
                    </div>
                    <div>
                      <div className="symbol-row">
                        <span className="stock-symbol font-mono">{holding.symbol}</span>
                        <span className="exchange-badge">{holding.exchange}</span>
                        {(holding.isSynced || holding.syncSource === 'indmoney') && (
                          <span
                            className="exchange-badge"
                            style={{
                              background: 'rgba(0, 208, 156, 0.12)',
                              color: '#00D09C',
                              border: '1px solid rgba(0, 208, 156, 0.3)',
                              fontWeight: 700,
                              fontSize: '10px'
                            }}
                          >
                            {holding.externalBroker || 'INDmoney'}
                          </span>
                        )}
                      </div>
                      <div className="stock-name">{holding.name}</div>
                    </div>
                  </div>


                  <div className="card-price-block font-mono">
                    <div className="current-price">
                      {currencySymbol} {holding.currentPrice.toFixed(2)}
                    </div>
                    <span className={`fintech-pill ${holding.isPositive ? 'gain' : 'loss'}`}>
                      {holding.isPositive ? '+' : ''}{currencySymbol}{holding.unrealizedPnl.toFixed(2)} ({holding.isPositive ? '+' : ''}{holding.unrealizedPnlPct.toFixed(2)}%)
                    </span>
                  </div>
                </div>

                {/* Position Details Bar */}
                <div className="holding-stats-strip">
                  <div className="stat-item">
                    <span className="stat-label">Shares Owned</span>
                    <span className="stat-val font-mono">{holding.qty}</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-label">Entry Price</span>
                    <span className="stat-val font-mono">{currencySymbol}{holding.entryPrice.toFixed(2)}</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-label">Position Value</span>
                    <span className="stat-val font-mono">{currencySymbol}{(holding.currentPrice * holding.qty).toFixed(2)}</span>
                  </div>
                </div>

                {/* TTL Urgency Bar */}
                <div className={`ttl-status-box ${ttlVariant}`}>
                  <div className="ttl-header">
                    <div className="ttl-title">
                      <ShieldAlert size={14} />
                      <span>{ttlVariant === 'danger' ? 'EXIT NOW' : ttlVariant === 'warning' ? 'CONSIDER EXITING' : 'SAFE HOLD'}</span>
                    </div>
                    <span className="ttl-score font-mono">Exit Urgency Score: {ttlScore}/100</span>
                  </div>
                  <div className="ttl-bar-track">
                    <div className="ttl-bar-bg">
                      <div className="ttl-bar-fill" style={{ width: `${ttlScore}%` }} />
                    </div>
                  </div>
                </div>

                {/* Target & Stop Loss Pills */}
                <div className="targets-row">
                  <span className="target-pill gain font-mono">
                    <Target size={12} /> Target: +{holding.targetPct}% ({currencySymbol}{(holding.entryPrice * (1 + holding.targetPct / 100)).toFixed(2)})
                  </span>
                  <span className="target-pill loss font-mono">
                    <ShieldAlert size={12} /> Stop-Loss: -{holding.stopLossPct}% ({currencySymbol}{(holding.entryPrice * (1 - holding.stopLossPct / 100)).toFixed(2)})
                  </span>
                </div>

                {/* Clean Indicator */}
                <div className="card-actions-row" style={{ marginTop: '14px', justifyContent: 'flex-end' }}>
                  <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--fintech-primary)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                    Inspect Exit Rules →
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
