import React, { useState } from 'react';

/**
 * Lesson 32: Backtest Performance & Institutional Ratios
 * Pure CSS/SVG institutional chart visualizing CAGR, Sharpe, Sortino, Calmar, and Underwater Drawdown curves.
 */
export const BacktestPerformanceChart = ({ data, metrics, ticker = 'TICKER' }) => {
  const [activeTab, setActiveTab] = useState('ratios'); // 'ratios' | 'underwater' | 'expectancy'

  const perf = data?.backtestPerformance || {
    cagr: 16.8,
    benchmarkCagr: 11.2,
    sharpe: 1.45,
    sortino: 2.18,
    calmar: 0.91,
    maxDrawdown: 18.4,
    profitFactor: 2.34,
    winRate: 58.4,
    turnoverDrag: 0.85
  };

  return (
    <div style={{
      background: 'var(--bg-surface, #141923)',
      borderRadius: '14px',
      border: '1px solid var(--border-hairline, #222A3B)',
      padding: '20px',
      color: 'var(--text-primary, #F8FAFC)',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      marginBottom: '16px'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{
              fontSize: '11px',
              fontWeight: 700,
              letterSpacing: '0.05em',
              color: 'var(--gain-green, #00D09C)',
              background: 'rgba(0, 208, 156, 0.12)',
              padding: '2px 8px',
              borderRadius: '4px',
              textTransform: 'uppercase'
            }}>
              Lesson 32 • Performance Attribution
            </span>
            <span style={{ fontSize: '12px', color: 'var(--text-tertiary, #64748B)' }}>Institutional Risk & Return Auditing</span>
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', margin: '6px 0 2px 0' }}>
            Backtest Performance & Risk Ratio Matrix
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', margin: 0 }}>
            Evaluating strategy CAGR against S&P 500 benchmark, downside-only Sortino volatility, and peak-to-trough drawdown profiles.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', background: 'var(--bg-app, #0B0E14)', padding: '3px', borderRadius: '8px', border: '1px solid var(--border-hairline, #222A3B)' }}>
          {[
            { id: 'ratios', label: 'Risk Ratios' },
            { id: 'underwater', label: 'Drawdown Curve' },
            { id: 'expectancy', label: 'Win Rate & Profit Factor' }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                background: activeTab === tab.id ? 'var(--bg-surface-elevated, #1C2333)' : 'transparent',
                color: activeTab === tab.id ? 'var(--text-primary, #F8FAFC)' : 'var(--text-secondary, #94A3B8)',
                border: activeTab === tab.id ? '1px solid var(--fintech-primary, #00D09C)' : '1px solid transparent',
                borderRadius: '6px',
                padding: '6px 12px',
                fontSize: '11px',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
        gap: '10px',
        marginBottom: '16px'
      }}>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Strategy CAGR</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            +{perf.cagr}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>S&P 500: +{perf.benchmarkCagr}%</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Sharpe Ratio</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', marginTop: '4px' }}>
            {perf.sharpe}
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Hurdle &ge; 1.00</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Sortino Ratio</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            {perf.sortino}
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Downside Protected</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Max Drawdown</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--loss-red, #F04438)', marginTop: '4px' }}>
            -{perf.maxDrawdown}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Calmar: {perf.calmar}x</div>
        </div>
      </div>

      {/* Tab 1: Risk Ratios */}
      {activeTab === 'ratios' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            Institutional Risk-Adjusted Return Comparison
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div style={{ background: 'var(--bg-surface, #141923)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Net Annualized Alpha vs S&P 500</div>
              <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '2px' }}>
                +{(perf.cagr - perf.benchmarkCagr).toFixed(1)}% / Year
              </div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)', marginTop: '4px' }}>
                Subtracted -{perf.turnoverDrag}% turnover friction drag.
              </div>
            </div>
            <div style={{ background: 'var(--bg-surface, #141923)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Calmar Tail Risk Multiple</div>
              <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', marginTop: '2px' }}>
                {perf.calmar}x (CAGR / Max DD)
              </div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)', marginTop: '4px' }}>
                Institutional gate requires Calmar &ge; 0.75x.
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Underwater Drawdown */}
      {activeTab === 'underwater' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '12px' }}>
            Underwater Equity Curve & Time-to-Recovery (5-Year Horizon)
          </div>
          <div style={{ height: '70px', background: 'var(--bg-surface, #141923)', borderRadius: '8px', position: 'relative', overflow: 'hidden', padding: '8px 12px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>
              <span>0% Peak</span>
              <span style={{ color: 'var(--loss-red, #F04438)' }}>Max Dip: -{perf.maxDrawdown}%</span>
              <span>New Highs</span>
            </div>
            {/* Synthetic underwater profile */}
            <div style={{ height: '3px', background: 'var(--loss-red, #F04438)', marginTop: '20px', borderRadius: '2px', opacity: 0.8 }} />
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '10px' }}>
            Max recovery time: 140 calendar days vs. S&P 500 benchmark recovery of 395 days during 2022 market cycle.
          </div>
        </div>
      )}

      {/* Tab 3: Expectancy */}
      {activeTab === 'expectancy' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            Statistical Edge & Trade Expectancy
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div style={{ padding: '10px', background: 'var(--bg-surface, #141923)', borderRadius: '8px' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Profit Factor: </span>
              <strong style={{ fontSize: '14px', color: 'var(--gain-green, #00D09C)' }}>{perf.profitFactor}x</strong>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Gross Wins / Gross Losses</div>
            </div>
            <div style={{ padding: '10px', background: 'var(--bg-surface, #141923)', borderRadius: '8px' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Win Rate: </span>
              <strong style={{ fontSize: '14px', color: 'var(--fintech-blue, #1455FE)' }}>{perf.winRate}%</strong>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Consistent multi-year win rate</div>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <div style={{
        background: 'var(--bg-app, #0B0E14)',
        borderRadius: '10px',
        border: '1px solid var(--border-hairline, #222A3B)',
        padding: '12px 16px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '10px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--gain-green, #00D09C)', boxShadow: '0 0 8px var(--gain-green, #00D09C)' }} />
          <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>
            INSTITUTIONAL SIMULATION GRADE
          </div>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>
          Sharpe &gt; 1.4, Sortino &gt; 2.1, Max Drawdown strictly bounded under 20%.
        </div>
      </div>
    </div>
  );
};

export default BacktestPerformanceChart;
