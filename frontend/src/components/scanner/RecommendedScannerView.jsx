import React, { useState, useEffect } from 'react';
import {
  Sparkles,
  TrendingUp,
  Award,
  ChevronRight,
  Globe,
  CheckCircle2,
  AlertTriangle,
  Flame,
  ArrowRight,
  Layers,
  ChevronLeft,
  Maximize2
} from 'lucide-react';
import { fetchScannerCategories, fetchTopWeek } from '../../services/api';
import PatternSparklineChart from './PatternSparklineChart';
import StockLogo from './StockLogo';
import PatternTheoryCard from './PatternTheoryCard';
import FintechLoader from '../common/FintechLoader';

export default function RecommendedScannerView({
  market = 'US',
  onMarketChange,
  onOpenPatternPage,
  onOpenPatternsCatalog,
  onInspectStock
}) {
  const [categories, setCategories] = useState([]);
  const [topWeek, setTopWeek] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadData = async (mkt) => {
    setIsLoading(true);
    setError(null);
    try {
      const [catRes, topRes] = await Promise.all([
        fetchScannerCategories(mkt),
        fetchTopWeek(mkt, 10)
      ]);
      setCategories(catRes?.categories || []);
      setTopWeek(topRes?.top_stocks || []);
    } catch (err) {
      console.error('Error loading scanner data:', err);
      setError('Unable to load quantified market setups. Please check connection.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData(market);
  }, [market]);

  const handleMarketChange = (newMarket) => {
    if (onMarketChange) {
      onMarketChange(newMarket);
    }
  };

  const scrollRow = (rowId, direction) => {
    const el = document.getElementById(rowId);
    if (el) {
      const scrollAmount = direction === 'left' ? -380 : 380;
      el.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  };

  // Rule 3: Filter out patterns with 0 matches today
  const activeCategories = categories.filter(
    (cat) => cat.top5 && cat.top5.length > 0
  );

  return (
    <div className="scanner-container">
      {/* Scanner Header & Global Filter Bar */}
      <div className="scanner-header-bar">
        <div className="scanner-header-title-zone">
          <div className="scanner-badge-pill">
            <Sparkles size={14} className="accent-icon" />
            <span>AI QUANT PATTERN RADAR</span>
          </div>
          <h1 className="scanner-main-title">Recommended Setups</h1>
          <p className="scanner-subtitle">
            Daily automated scans across 33 institutional technical patterns for Indian (NSE/BSE) and US (NASDAQ/NYSE) markets.
            Every match is audited with historical backtests and explainable evidence.
          </p>
        </div>

        {/* Right Action Zone: Patterns Library Button */}
        <div className="scanner-header-right-controls">
          <button
            className="scanner-patterns-catalog-btn"
            onClick={() => {
              if (onOpenPatternsCatalog) {
                onOpenPatternsCatalog();
              } else if (onOpenPatternPage) {
                onOpenPatternPage('ALL');
              }
            }}
            title="Browse all 33 institutional pattern definitions and backtest metrics"
          >
            <Layers size={14} className="accent-icon" />
            <span>Patterns Library & Catalog (33)</span>
            <ChevronRight size={13} />
          </button>
        </div>
      </div>

      {isLoading && (
        <FintechLoader
          title="Scanning 33 Technical & Base Patterns..."
          subtitle="Aggregating daily indicators, composite rankings, and audited backtest evidence"
        />
      )}

      {error && !isLoading && (
        <div className="scanner-error-box">
          <AlertTriangle size={32} className="error-icon" />
          <div className="error-title">{error}</div>
          <button className="stocktics-button" onClick={() => loadData(market)}>
            Retry Scanner
          </button>
        </div>
      )}

      {!isLoading && !error && (
        <>
          {/* Top 10 This Week Row - Netflix Style Vertical Cards */}
          <section className="top-week-section">
            <div className="section-row-header">
              <div className="row-header-title-wrap">
                <Award size={20} style={{ color: 'var(--fintech-primary)' }} />
                <h2 className="row-title">Top Setups This Week</h2>
                <span className="row-badge-count">Top 10 Composite Rank</span>
              </div>
              <div className="row-scroll-controls">
                <button
                  className="row-nav-btn"
                  onClick={() => scrollRow('top-week-scroll', 'left')}
                  title="Scroll Left"
                >
                  <ChevronLeft size={16} />
                </button>
                <button
                  className="row-nav-btn"
                  onClick={() => scrollRow('top-week-scroll', 'right')}
                  title="Scroll Right"
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>

            <div className="netflix-horizontal-row top-week-row-scroll" id="top-week-scroll">
              {(!topWeek || topWeek.length === 0) ? (
                <div className="empty-row-card">
                  <span>Calculating weekly rolling composite rankings...</span>
                </div>
              ) : (
                topWeek.map((stock, idx) => (
                  <div
                    key={`${stock.ticker || 'tw'}-${idx}`}
                    className="top-week-card-vertical"
                    onClick={() =>
                      onInspectStock({
                        ...stock,
                        pattern: stock.top_pattern,
                        display_name: stock.top_pattern?.replace(/_/g, ' '),
                        pattern_score: stock.avg_composite_score
                      })
                    }
                    role="button"
                    tabIndex={0}
                    title="Click to view maximized candlestick chart & pattern setup"
                  >
                    {/* Big Netflix-style outline numeral behind / at bottom-left */}
                    <div className="netflix-rank-outline font-mono">
                      {idx + 1}
                    </div>

                    {/* Top Section: Logo & Market */}
                    <div className="vertical-card-top">
                      <div className="vertical-card-logo-row">
                        <StockLogo ticker={stock.ticker} name={stock.name} size={42} />
                        <span className="card-market-pill">{stock.exchange || stock.market}</span>
                      </div>
                      <div className="vertical-ticker-block">
                        <span className="card-ticker font-mono">{stock.ticker}</span>
                        <span className="card-company-name-vertical" title={stock.name}>
                          {stock.name}
                        </span>
                      </div>
                    </div>

                    {/* Center: Composite Score */}
                    <div className="vertical-composite-badge font-mono">
                      <span className="score-val">{stock.avg_composite_score}</span>
                      <span className="score-lbl">COMPOSITE RANK</span>
                    </div>

                    {/* Metrics Grid */}
                    <div className="vertical-metrics-grid">
                      <div className="v-metric-item">
                        <span className="m-lbl">TREND</span>
                        <span className="m-val font-mono">{stock.trend_score || '--'}</span>
                      </div>
                      <div className="v-metric-item">
                        <span className="m-lbl">REL STR</span>
                        <span className="m-val font-mono">{stock.rel_strength_score || '--'}</span>
                      </div>
                      <div className="v-metric-item">
                        <span className="m-lbl">VOLUME</span>
                        <span className="m-val font-mono">{stock.volume_score || '--'}</span>
                      </div>
                    </div>

                    {/* Bottom: Lead Pattern Pill */}
                    {stock.top_pattern && (
                      <div className="vertical-top-pattern">
                        <Flame size={12} className="flame-icon" />
                        <span className="pattern-name">{stock.top_pattern.replace(/_/g, ' ')}</span>
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </section>

          {/* Active Pattern Rows (Filtered: 0-match rows hidden) */}
          <div className="categories-grid-container">
            {activeCategories.map((cat) => {
              const rowId = `pattern-row-${cat.pattern}`;
              const isExperimental = cat.confidence_tier === 'EXPERIMENTAL';
              const totalMatches = cat.total_matches || (cat.top5 ? cat.top5.length : 0);
              // Rule 4: Only show "View All" if more than 5 setups active
              const hasMoreThan5 = totalMatches > 5;

              return (
                <section key={cat.pattern} className="pattern-category-row-section">
                  <div className="section-row-header">
                    <div className="row-header-title-wrap">
                      <h3
                        className="row-title"
                        onClick={() => onOpenPatternPage(cat.pattern)}
                        style={{ cursor: 'pointer' }}
                        title="Click to open dedicated pattern explanation & backtest page"
                      >
                        {cat.display_name}
                      </h3>
                      {isExperimental && (
                        <span
                          className="tier-experimental-pill"
                          title="Astronomical or non-traditional setup. Visible for study, excluded from composite ranking math."
                        >
                          EXPERIMENTAL
                        </span>
                      )}
                      <span className="row-matches-count font-mono">
                        {totalMatches} setups active
                      </span>
                    </div>

                    <div className="row-actions-group">
                      {/* Rule 4: Show View All header link ONLY if > 5 setups */}
                      {hasMoreThan5 && (
                        <button
                          className="row-view-all-link"
                          onClick={() => onOpenPatternPage(cat.pattern)}
                          title="Open dedicated pattern page"
                        >
                          <span>View All ({totalMatches})</span>
                          <ChevronRight size={14} />
                        </button>
                      )}
                      <div className="row-scroll-controls">
                        <button
                          className="row-nav-btn"
                          onClick={() => scrollRow(rowId, 'left')}
                          title="Scroll Left"
                        >
                          <ChevronLeft size={16} />
                        </button>
                        <button
                          className="row-nav-btn"
                          onClick={() => scrollRow(rowId, 'right')}
                          title="Scroll Right"
                        >
                          <ChevronRight size={16} />
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* Horizontal Scroll Track */}
                  <div className="netflix-horizontal-row" id={rowId}>
                    {/* Rule 6: Card #1 is the Educational Pattern Theory Card */}
                    <PatternTheoryCard
                      pattern={cat.pattern}
                      displayName={cat.display_name}
                      category={cat.category}
                      onClickBacktest={() => onOpenPatternPage(cat.pattern)}
                    />

                    {/* Stock Setup Cards */}
                    {cat.top5.map((stock, sIdx) => (
                      <div
                        key={`${cat.pattern}-${stock.ticker}-${sIdx}`}
                        className="stock-pattern-card"
                        onClick={() =>
                          onInspectStock({
                            ...stock,
                            pattern: cat.pattern,
                            display_name: cat.display_name
                          })
                        }
                        role="button"
                        tabIndex={0}
                        title="Click to view maximized candlestick chart & pattern setup"
                      >
                        <div className="stock-card-top">
                          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            {/* Rule 5: Stock Logo avatar like in Deep Analysis */}
                            <StockLogo ticker={stock.ticker} name={stock.name} size={36} />
                            <div>
                              <div className="stock-sym-row">
                                <span className="card-ticker font-mono">{stock.ticker}</span>
                                <span className="card-market-pill">{stock.exchange || stock.market}</span>
                              </div>
                              <div className="card-company-name">{stock.name}</div>
                            </div>
                          </div>
                          <div className="pattern-score-pill font-mono">
                            <span className="score-digit">{stock.pattern_score}</span>
                            <span className="score-caption">SCORE</span>
                          </div>
                        </div>

                        {/* Visual Pattern Sparkline Chart with Pivot Level */}
                        <PatternSparklineChart
                          ticker={stock.ticker}
                          market={stock.market}
                          pivot={stock.pivot}
                          pattern={cat.pattern}
                          score={stock.pattern_score}
                        />

                        {stock.pivot && (
                          <div className="card-pivot-row font-mono">
                            <span className="pivot-lbl">PIVOT LEVEL</span>
                            <span className="pivot-val">
                              {stock.market === 'IN' ? '₹' : '$'}{stock.pivot.toFixed(2)}
                            </span>
                          </div>
                        )}

                        {/* Explainable Reasons Pills */}
                        <div className="reasons-pill-list">
                          {(stock.reasons || []).slice(0, 3).map((r, i) => (
                            <div key={i} className="reason-bullet">
                              <CheckCircle2 size={11} className="reason-check-icon" />
                              <span>{r}</span>
                            </div>
                          ))}
                        </div>

                        <div className="card-footer-action">
                          <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                            <Maximize2 size={12} style={{ color: 'var(--fintech-primary)' }} />
                            <span>Inspect Chart</span>
                          </div>
                          <ArrowRight size={12} />
                        </div>
                      </div>
                    ))}

                    {/* Rule 4: Only show "View All" card if setups > 5 */}
                    {hasMoreThan5 && (
                      <div
                        className="view-more-row-card"
                        onClick={() => onOpenPatternPage(cat.pattern)}
                        role="button"
                        tabIndex={0}
                        title="Open dedicated pattern page"
                      >
                        <Layers size={24} className="view-more-icon" />
                        <div className="view-more-title">View All ({totalMatches})</div>
                        <div className="view-more-sub">Historical Evidence & Grid</div>
                      </div>
                    )}
                  </div>
                </section>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
}
