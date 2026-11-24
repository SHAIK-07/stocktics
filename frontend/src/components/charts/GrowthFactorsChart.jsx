import React, { useState } from 'react';
import {
  TrendingUp,
  Percent,
  Activity,
  Zap,
  ShieldAlert,
  CheckCircle2,
  AlertCircle,
  ArrowUpRight,
  RefreshCw,
  BarChart2
} from 'lucide-react';

export default function GrowthFactorsChart({ data, growthScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('trajectory'); // 'trajectory' | 'sgr' | 'acceleration'

  const scorecard = growthScorecard || {
    ticker: 'STOCK',
    score: 78.5,
    rating: 'ROBUST SECULAR GROWTH',
    revCagr3Y: 14.8,
    epsCagr3Y: 18.2,
    sgr: 16.5,
    acceleration: 2.4
  };

  const chartData = data || {
    revCagr3Y: 14.8,
    epsCagr3Y: 18.2,
    sgr: 16.5,
    latestYoY: 17.2,
    acceleration: 2.4,
    reinvestmentRate: 85.0,
    roe: 19.4,
    score: 78.5,
    classification: 'ROBUST SECULAR GROWTH',
    trend: [
      { period: '3Y Rev CAGR', rate: 14.8, type: 'Baseline Growth' },
      { period: '3Y EPS CAGR', rate: 18.2, type: 'Operating Leverage' },
      { period: 'Latest YoY Rev', rate: 17.2, type: 'Current Velocity' },
      { period: 'Sustainable SGR', rate: 16.5, type: 'Self-Funded Cap' }
    ]
  };

  const trendData = chartData.trend || [
    { period: '3Y Rev CAGR', rate: chartData.revCagr3Y, type: 'Baseline Growth' },
    { period: '3Y EPS CAGR', rate: chartData.epsCagr3Y, type: 'Operating Leverage' },
    { period: 'Latest YoY Rev', rate: chartData.latestYoY, type: 'Current Velocity' },
    { period: 'Sustainable SGR', rate: chartData.sgr, type: 'Self-Funded Cap' }
  ];

  const isAccelerating = chartData.acceleration >= 0;
  const isHighQualityGrowth = chartData.epsCagr3Y >= chartData.revCagr3Y;

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
              Chapter 21 Quantitative Factor
            </span>
            <span style={{
              background: scorecard.score >= 65 ? 'rgba(0, 208, 156, 0.15)' : 'rgba(240, 68, 56, 0.15)',
              color: scorecard.score >= 65 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {scorecard.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            {scorecard.ticker} Growth Factors & Sustainable Growth Rate (SGR)
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            3-Year Top-Line & EPS Compounding, Internal Reinvestment Capacity, and Growth Inflections
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
            { id: 'trajectory', label: 'CAGR Comparison' },
            { id: 'sgr', label: 'SGR Reinvestment' },
            { id: 'acceleration', label: 'Acceleration Delta' }
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

      {/* KPI Summary Tiles */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
        gap: '10px',
        marginBottom: '18px'
      }}>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>3Y Rev CAGR</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            {chartData.revCagr3Y > 0 ? `+${chartData.revCagr3Y}%` : `${chartData.revCagr3Y}%`}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>3Y EPS CAGR</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', fontFamily: 'monospace' }}>
            {chartData.epsCagr3Y > 0 ? `+${chartData.epsCagr3Y}%` : `${chartData.epsCagr3Y}%`}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Sustainable SGR</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', fontFamily: 'monospace' }}>
            {chartData.sgr}%
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>YoY Acceleration</div>
          <div style={{
            fontSize: '18px',
            fontWeight: 700,
            color: isAccelerating ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)',
            fontFamily: 'monospace'
          }}>
            {chartData.acceleration > 0 ? `+${chartData.acceleration}%` : `${chartData.acceleration}%`}
          </div>
        </div>
      </div>

      {/* Tab 1: CAGR Trajectory Comparison */}
      {activeTab === 'trajectory' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{
            background: 'var(--fintech-background, #0B0E14)',
            border: '1px solid var(--fintech-border, #222A3B)',
            borderRadius: '10px',
            padding: '16px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>
                Multi-Horizon Compounding Rates vs 10% Hurdle
              </span>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
                Dashed line: 10.0% Institutional Hurdle
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {trendData.map((item, idx) => {
                const barWidth = Math.min(100, Math.max(8, Math.abs(item.rate) * 2.8));
                const isPositive = item.rate >= 0;
                return (
                  <div key={idx}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                      <span style={{ fontWeight: 600, color: '#F8FAFC' }}>
                        {item.period} ({item.type})
                      </span>
                      <span style={{ fontFamily: 'monospace', fontWeight: 700, color: isPositive ? '#00D09C' : '#F04438' }}>
                        {isPositive ? '+' : ''}{item.rate}%
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
                        width: `${barWidth}%`,
                        background: idx === 0 ? '#00D09C' : idx === 1 ? '#1455FE' : idx === 2 ? '#38BDF8' : '#F59E0B',
                        borderRadius: '7px',
                        transition: 'width 0.4s ease'
                      }} />
                      {/* 10% Hurdle Line */}
                      <div style={{
                        position: 'absolute',
                        left: '28%',
                        top: 0,
                        bottom: 0,
                        width: '2px',
                        background: '#64748B',
                        zIndex: 2
                      }} />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px' }}>
            {isHighQualityGrowth ? (
              <span style={{ color: 'var(--gain-green, #00D09C)', fontWeight: 600 }}>
                ✓ Positive Operating Leverage: EPS CAGR ({chartData.epsCagr3Y}%) outpaces Revenue CAGR ({chartData.revCagr3Y}%), demonstrating expanding operational margins.
              </span>
            ) : (
              <span style={{ color: 'var(--warning-amber, #F59E0B)', fontWeight: 600 }}>
                ⚠ Revenue growth outpaces EPS expansion, indicating operating margin compression or rising cost structures.
              </span>
            )}
          </div>
        </div>
      )}

      {/* Tab 2: SGR Reinvestment */}
      {activeTab === 'sgr' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Sustainable Growth Equation</span>
              <span style={{ fontSize: '12px', color: 'var(--fintech-primary, #00D09C)', fontWeight: 600, fontFamily: 'monospace' }}>
                SGR = ROE × (1 - Payout)
              </span>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px', textAlign: 'center' }}>
              <div style={{ background: 'rgba(255,255,255,0.02)', padding: '10px', borderRadius: '8px' }}>
                <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>Return on Equity</div>
                <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', fontFamily: 'monospace' }}>{chartData.roe}%</div>
              </div>
              <div style={{ background: 'rgba(255,255,255,0.02)', padding: '10px', borderRadius: '8px' }}>
                <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>Retention Rate</div>
                <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', fontFamily: 'monospace' }}>{chartData.reinvestmentRate}%</div>
              </div>
              <div style={{ background: 'rgba(0, 208, 156, 0.08)', padding: '10px', borderRadius: '8px' }}>
                <div style={{ fontSize: '11px', color: 'var(--fintech-primary, #00D09C)' }}>Max Self-Funded SGR</div>
                <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>{chartData.sgr}%</div>
              </div>
            </div>
          </div>
          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
            The Sustainable Growth Rate of <strong style={{ color: '#F8FAFC' }}>{chartData.sgr}%</strong> proves the company can compound its operational asset base internally without needing debt issuance or diluting equity holders.
          </div>
        </div>
      )}

      {/* Tab 3: Acceleration Delta */}
      {activeTab === 'acceleration' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <Zap size={18} color={isAccelerating ? '#00D09C' : '#F04438'} />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>
                {isAccelerating ? 'Positive Second Derivative (Growth Acceleration)' : 'Growth Deceleration Trend'}
              </span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Latest YoY revenue growth is <strong style={{ color: '#F8FAFC' }}>{chartData.latestYoY}%</strong> compared to the 3-Year baseline of <strong style={{ color: '#F8FAFC' }}>{chartData.revCagr3Y}%</strong> (Delta: <span style={{ color: isAccelerating ? '#00D09C' : '#F04438', fontWeight: 700 }}>{chartData.acceleration > 0 ? `+${chartData.acceleration}%` : `${chartData.acceleration}%`}</span>).
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
