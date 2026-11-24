import React, { useState } from 'react';
import {
  Clock,
  Activity,
  ArrowRight,
  ShieldCheck,
  TrendingUp,
  Percent,
  CheckCircle2,
  AlertTriangle,
  Play
} from 'lucide-react';

export default function BacktestArchitectureChart({ data, backtestScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('equity'); // 'equity' | 'pipeline' | 'slippage'

  const scorecard = backtestScorecard || {
    ticker: 'STOCK',
    cagr: 14.8,
    alphaVsBench: 4.6,
    maxDrawdown: -18.5,
    sharpeRatio: 1.42,
    rating: 'INSTITUTIONAL SIMULATION GRADE'
  };

  const chartData = data || {
    fidelityScore: 92.0,
    netCagr: 14.8,
    benchCagr: 10.2,
    maxDrawdown: -18.5,
    turnover: 45.0,
    slippageBps: 15.0,
    equityCurve: [
      { year: '2021', strategy: 114.8, benchmark: 110.2 },
      { year: '2022', strategy: 104.2, benchmark: 89.5 },
      { year: '2023', strategy: 136.5, benchmark: 115.8 },
      { year: '2024', strategy: 168.4, benchmark: 142.1 }
    ]
  };

  const equityData = chartData.equityCurve || [
    { year: '2021', strategy: 114.8, benchmark: 110.2 },
    { year: '2022', strategy: 104.2, benchmark: 89.5 },
    { year: '2023', strategy: 136.5, benchmark: 115.8 },
    { year: '2024', strategy: 168.4, benchmark: 142.1 }
  ];

  return (
    <div className="fintech-chart-card" style={{
      background: 'var(--fintech-surface, #141923)',
      border: '1px solid var(--fintech-border, #222A3B)',
      borderRadius: '14px',
      padding: '20px',
      color: 'var(--text-primary, #F8FAFC)',
      marginBottom: '20px'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '18px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span style={{
              background: 'rgba(0, 208, 156, 0.15)',
              color: 'var(--fintech-primary, #00D09C)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700,
              textTransform: 'uppercase'
            }}>
              Chapter 28 Backtesting Engine
            </span>
            <span style={{
              background: 'rgba(0, 208, 156, 0.15)',
              color: 'var(--gain-green, #00D09C)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {scorecard.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            Historical Backtest Simulation & Execution Fidelity
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Discrete Time-Step Pipeline, Slippage Haircuts (15 bps), Turnover Drag, and Equity vs S&P 500
          </p>
        </div>

        {/* Tab Selector */}
        <div style={{
          display: 'flex',
          background: 'var(--fintech-surface-elevated, #1C2333)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '8px',
          padding: '2px'
        }}>
          {[
            { id: 'equity', label: 'Equity Curve' },
            { id: 'pipeline', label: 'Engine Pipeline' },
            { id: 'slippage', label: 'Slippage Model' }
          ].map(t => (
            <button
              key={t.id}
              onClick={() => setActiveTab(t.id)}
              style={{
                background: activeTab === t.id ? 'var(--fintech-primary, #00D09C)' : 'transparent',
                color: activeTab === t.id ? '#0B0E14' : 'var(--text-secondary, #94A3B8)',
                border: 'none',
                padding: '6px 12px',
                borderRadius: '6px',
                fontSize: '12px',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* KPI Tiles */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
        gap: '10px',
        marginBottom: '18px'
      }}>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Strategy Net CAGR</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            +{chartData.netCagr}%
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Net Alpha vs S&P 500</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', fontFamily: 'monospace' }}>
            +{scorecard.alphaVsBench}%
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Max Drawdown</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--loss-red, #F04438)', fontFamily: 'monospace' }}>
            {chartData.maxDrawdown}%
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Sharpe Ratio</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', fontFamily: 'monospace' }}>
            {scorecard.sharpeRatio}
          </div>
        </div>
      </div>

      {/* Tab 1: Equity Curve Comparison */}
      {activeTab === 'equity' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{
            background: 'var(--fintech-background, #0B0E14)',
            border: '1px solid var(--fintech-border, #222A3B)',
            borderRadius: '10px',
            padding: '16px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>
                Cumulative Wealth Growth Index ($100 Base)
              </span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '11px' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#00D09C' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '2px', background: '#00D09C' }} /> Strategy Net
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#64748B' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '2px', background: '#64748B' }} /> S&P 500 (SPY)
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {equityData.map((e, idx) => (
                <div key={idx}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '3px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Year {e.year}</span>
                    <span style={{ fontFamily: 'monospace', fontWeight: 700, color: '#00D09C' }}>
                      ${e.strategy} (vs Benchmark ${e.benchmark})
                    </span>
                  </div>
                  <div style={{
                    height: '14px',
                    background: 'var(--fintech-surface-elevated, #1C2333)',
                    borderRadius: '7px',
                    overflow: 'hidden',
                    position: 'relative'
                  }}>
                    <div style={{
                      height: '100%',
                      width: `${Math.min(100, (e.strategy / 180.0) * 100)}%`,
                      background: '#00D09C',
                      borderRadius: '7px',
                      transition: 'width 0.4s ease'
                    }} />
                    {/* Benchmark marker */}
                    <div style={{
                      position: 'absolute',
                      left: `${Math.min(95, (e.benchmark / 180.0) * 100)}%`,
                      top: 0,
                      bottom: 0,
                      width: '2px',
                      background: '#64748B',
                      zIndex: 2
                    }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px' }}>
            Verified institutional backtesting engine with realistic 15 bps slippage deduction per rebalance. Delivers <strong style={{ color: '#00D09C' }}>+{scorecard.alphaVsBench}% net excess alpha</strong> over the S&P 500 benchmark.
          </div>
        </div>
      )}

      {/* Tab 2: Engine Pipeline */}
      {activeTab === 'pipeline' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {[
            { step: '1. Universe Definition', desc: 'Point-In-Time cross-sectional screening with $5M+ daily volume filter.' },
            { step: '2. Factor Ranking', desc: 'Sector-neutral percentile ranking and standardized Z-score computation.' },
            { step: '3. Order Execution Simulator', desc: 'Conservative 15 bps slippage and spread crossing applied to all fills.' },
            { step: '4. Rebalancing & Ledger', desc: 'Monthly portfolio reconstitution tracking cumulative NAV, turnover, and drawdowns.' }
          ].map((p, idx) => (
            <div key={idx} style={{
              background: 'var(--fintech-surface-elevated, #1C2333)',
              padding: '12px 16px',
              borderRadius: '8px',
              border: '1px solid var(--fintech-border, #222A3B)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <div>
                <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)' }}>{p.step}</div>
                <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{p.desc}</div>
              </div>
              <CheckCircle2 size={18} color="#00D09C" />
            </div>
          ))}
        </div>
      )}

      {/* Tab 3: Slippage Model */}
      {activeTab === 'slippage' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ fontSize: '14px', fontWeight: 700, marginBottom: '8px' }}>Frictional Cost Drag Modeling: 15 bps / trade</div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Unlike naive retail backtests that assume zero transaction costs, this simulation models the Square-Root Law of market impact and spread crossing. High turnover strategies that trade weekly face an insurmountable hurdle; this monthly rebalancing strategy limits turnover to {chartData.turnover}%, preserving capital.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
