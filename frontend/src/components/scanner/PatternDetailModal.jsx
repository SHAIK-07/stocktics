import React, { useState, useEffect } from 'react';
import {
  X,
  TrendingUp,
  ShieldCheck,
  AlertTriangle,
  BarChart2,
  CheckCircle2,
  ArrowRight,
  Info,
  Calendar,
  Layers
} from 'lucide-react';
import { fetchPatternMatches, fetchPatternBacktest } from '../../services/api';
import PatternSparklineChart from './PatternSparklineChart';
import StockLogo from './StockLogo';
import FintechLoader from '../common/FintechLoader';

export default function PatternDetailModal({ patternInfo, onClose, onSelectStock }) {
  const [matches, setMatches] = useState([]);
  const [backtest, setBacktest] = useState(null);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [isLoading, setIsLoading] = useState(true);

  const pattern = patternInfo?.pattern;
  const isExperimental = patternInfo?.confidence_tier === 'EXPERIMENTAL';

  useEffect(() => {
    if (!pattern) return;

    const loadDetail = async () => {
      setIsLoading(true);
      try {
        const [matchData, btData] = await Promise.all([
          fetchPatternMatches(pattern, null, page, 24),
          fetchPatternBacktest(pattern)
        ]);
        setMatches(matchData.results || []);
        setTotal(matchData.total || 0);
        setBacktest(btData);
      } catch (err) {
        console.error('Error loading pattern detail:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadDetail();
  }, [pattern, page]);

  if (!patternInfo) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card pattern-detail-modal" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div>
            <div className="modal-subtitle-row">
              <span className="modal-cat-tag">{patternInfo.category || 'TECHNICAL PATTERN'}</span>
              {isExperimental && (
                <span className="tier-experimental-pill">EXPERIMENTAL LAB</span>
              )}
            </div>
            <h2 className="modal-title">{patternInfo.display_name}</h2>
          </div>
          <button className="modal-close-btn" onClick={onClose} title="Close">
            <X size={20} />
          </button>
        </div>

        <div className="modal-body-scroll">
          {/* Historical Backtest Transparency Card */}
          <div className="backtest-stats-card">
            <div className="backtest-card-header">
              <div className="backtest-title-wrap">
                <BarChart2 size={18} style={{ color: 'var(--fintech-primary)' }} />
                <span className="backtest-title">Historical Evidence & Forward Returns</span>
              </div>
              <div className="sample-size-pill font-mono">
                Sample Size: {backtest?.sample_size || 0} signals
              </div>
            </div>

            <div className="backtest-metrics-row font-mono">
              <div className="backtest-col">
                <div className="horizon-tag">+5 TRADING DAYS</div>
                <div className="win-rate-val">
                  <span className="rate-num">{backtest?.win_rate_5d || 50.0}%</span>
                  <span className="rate-lbl">WIN RATE</span>
                </div>
                <div className={`return-sub ${backtest?.avg_return_5d >= 0 ? 'pos' : 'neg'}`}>
                  {backtest?.avg_return_5d >= 0 ? '+' : ''}{backtest?.avg_return_5d || 0.0}% avg
                </div>
              </div>

              <div className="backtest-divider" />

              <div className="backtest-col">
                <div className="horizon-tag">+10 TRADING DAYS</div>
                <div className="win-rate-val">
                  <span className="rate-num">{backtest?.win_rate_10d || 50.0}%</span>
                  <span className="rate-lbl">WIN RATE</span>
                </div>
                <div className={`return-sub ${backtest?.avg_return_10d >= 0 ? 'pos' : 'neg'}`}>
                  {backtest?.avg_return_10d >= 0 ? '+' : ''}{backtest?.avg_return_10d || 0.0}% avg
                </div>
              </div>

              <div className="backtest-divider" />

              <div className="backtest-col">
                <div className="horizon-tag">+20 TRADING DAYS</div>
                <div className="win-rate-val">
                  <span className="rate-num">{backtest?.win_rate_20d || 50.0}%</span>
                  <span className="rate-lbl">WIN RATE</span>
                </div>
                <div className={`return-sub ${backtest?.avg_return_20d >= 0 ? 'pos' : 'neg'}`}>
                  {backtest?.avg_return_20d >= 0 ? '+' : ''}{backtest?.avg_return_20d || 0.0}% avg
                </div>
              </div>
            </div>

            {/* Non-survivorship Bias Caveat Banner */}
            <div className="backtest-caveat-banner">
              <Info size={14} className="caveat-icon" />
              <span>
                <strong>Methodology Transparency:</strong> Backtests evaluate knowable forward returns from trigger dates.
                No survivorship-bias correction in v0.3.
                {isExperimental && ' This pattern is tagged EXPERIMENTAL and is excluded from composite ranking math.'}
              </span>
            </div>
          </div>

          {/* Current Active Matches Grid */}
          <div className="modal-section-heading">
            <h3>Current Market Matches ({total})</h3>
            <span className="heading-hint">Click any stock to inspect audited financial statements & metrics</span>
          </div>

          {isLoading ? (
            <FintechLoader
              title={`Scanning ${patternInfo?.display_name || 'Pattern'} Setups...`}
              subtitle="Querying institutional daily scanner database"
            />
          ) : matches.length === 0 ? (
            <div className="empty-matches-box">
              <AlertTriangle size={28} className="empty-icon" />
              <div className="empty-title">No Active Matches Today</div>
              <p>None of the tracked securities in the universe met the strict criteria for {patternInfo.display_name} today.</p>
            </div>
          ) : (
            <div className="modal-matches-grid">
              {matches.map((stock) => (
                <div
                  key={`${pattern}-${stock.ticker}`}
                  className="stock-pattern-card in-grid"
                  onClick={() => {
                    onClose();
                    onSelectStock(stock.ticker);
                  }}
                  role="button"
                  tabIndex={0}
                >
                  <div className="stock-card-top">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <StockLogo ticker={stock.ticker} name={stock.name} size={34} />
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

                  {/* Real Pattern Chart with Pivot Line */}
                  <PatternSparklineChart
                    ticker={stock.ticker}
                    market={stock.market}
                    pivot={stock.pivot}
                    pattern={pattern}
                    score={stock.pattern_score}
                  />

                  <div className="reasons-pill-list">
                    {(stock.reasons || []).map((r, i) => (
                      <div key={i} className="reason-bullet">
                        <CheckCircle2 size={11} className="reason-check-icon" />
                        <span>{r}</span>
                      </div>
                    ))}
                  </div>

                  <div className="card-footer-action">
                    <span>Inspect Story</span>
                    <ArrowRight size={12} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
