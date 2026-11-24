import React, { useState } from 'react';
import {
  TrendingUp,
  Activity,
  Award,
  Target,
  CheckCircle2,
  AlertCircle,
  BarChart2
} from 'lucide-react';

export default function CorrelationSignalChart({ data, correlationScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('rollingIc'); // 'rollingIc' | 'spearman' | 'grinold'

  const corr = data || {
    ic: 0.065,
    spearmanRank: 0.62,
    infoRatio: 1.71,
    signalScore: 84.0,
    classification: 'ELITE ALPHA PREDICTIVE SIGNAL (IC >= 0.05)',
    rollingIc: [
      { month: 'M-6', ic: 0.077, benchmark: 0.050 },
      { month: 'M-5', ic: 0.057, benchmark: 0.050 },
      { month: 'M-4', ic: 0.080, benchmark: 0.050 },
      { month: 'M-3', ic: 0.061, benchmark: 0.050 },
      { month: 'M-2', ic: 0.074, benchmark: 0.050 },
      { month: 'M-1', ic: 0.065, benchmark: 0.050 }
    ]
  };

  const rollingData = corr.rollingIc || [
    { month: 'M-6', ic: 0.077, benchmark: 0.050 },
    { month: 'M-5', ic: 0.057, benchmark: 0.050 },
    { month: 'M-4', ic: 0.080, benchmark: 0.050 },
    { month: 'M-3', ic: 0.061, benchmark: 0.050 },
    { month: 'M-2', ic: 0.074, benchmark: 0.050 },
    { month: 'M-1', ic: 0.065, benchmark: 0.050 }
  ];

  const isEliteSignal = corr.ic >= 0.05;

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
              Chapter 25 Quantitative Alpha Signal
            </span>
            <span style={{
              background: isEliteSignal ? 'rgba(0, 208, 156, 0.15)' : 'rgba(20, 85, 254, 0.15)',
              color: isEliteSignal ? 'var(--gain-green, #00D09C)' : 'var(--fintech-blue, #1455FE)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {corr.classification}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            Factor Information Coefficient (IC) & Grinold Law
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Spearman Rank Correlation with Forward Returns, Information Ratio (IR), and Signal Persistence
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
            { id: 'rollingIc', label: 'Rolling IC' },
            { id: 'spearman', label: 'Spearman Rank' },
            { id: 'grinold', label: 'Grinold IR' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Information Coeff (IC)</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            +{corr.ic}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Information Ratio</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', fontFamily: 'monospace' }}>
            {corr.infoRatio} IR
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Spearman Rank ρ</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', fontFamily: 'monospace' }}>
            +{corr.spearmanRank}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Signal Score</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', fontFamily: 'monospace' }}>
            {corr.signalScore} / 100
          </div>
        </div>
      </div>

      {/* Tab 1: Rolling IC Bars */}
      {activeTab === 'rollingIc' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{
            background: 'var(--fintech-background, #0B0E14)',
            border: '1px solid var(--fintech-border, #222A3B)',
            borderRadius: '10px',
            padding: '16px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>
                6-Month Rolling Information Coefficient vs 0.050 Hurdle
              </span>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
                Dashed Marker: +0.050 Institutional Hurdle
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {rollingData.map((r, idx) => {
                const isPassing = r.ic >= r.benchmark;
                const barWidth = Math.min(100, (r.ic / 0.10) * 100);

                return (
                  <div key={idx}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '3px' }}>
                      <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Month {r.month}</span>
                      <span style={{ fontFamily: 'monospace', fontWeight: 700, color: isPassing ? '#00D09C' : '#38BDF8' }}>
                        +{r.ic.toFixed(3)} IC ({isPassing ? 'Hurdle Cleared' : 'Moderate Signal'})
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
                        background: isPassing ? '#00D09C' : '#1455FE',
                        borderRadius: '7px',
                        transition: 'width 0.4s ease'
                      }} />
                      {/* 0.050 Benchmark Marker at 50% */}
                      <div style={{
                        position: 'absolute',
                        left: '50%',
                        top: 0,
                        bottom: 0,
                        width: '2px',
                        background: '#F59E0B',
                        zIndex: 2
                      }} />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px' }}>
            In quantitative finance, an Information Coefficient consistently exceeding <strong style={{ color: '#00D09C' }}>+0.050</strong> demonstrates a top-tier predictive signal with strong empirical alpha persistence.
          </div>
        </div>
      )}

      {/* Tab 2: Spearman Rank Monotonicity */}
      {activeTab === 'spearman' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ fontSize: '14px', fontWeight: 700, marginBottom: '8px' }}>Spearman Rank Monotonicity: ρ = +{corr.spearmanRank}</div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Unlike Pearson correlation, Spearman rank correlation evaluates ordinal rank order, making it immune to non-linear fundamental and pricing outliers. A rank correlation of <strong style={{ color: 'var(--text-primary)' }}>+{corr.spearmanRank}</strong> confirms that higher factor scores monotonically translate into superior forward portfolio deciles.
            </p>
          </div>
        </div>
      )}

      {/* Tab 3: Grinold IR */}
      {activeTab === 'grinold' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Grinold's Fundamental Law of Active Management</span>
              <span style={{ fontSize: '12px', color: 'var(--fintech-primary, #00D09C)', fontWeight: 600, fontFamily: 'monospace' }}>
                IR = IC × √Breadth
              </span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Information Ratio measures consistency of value added per unit of risk. An IR of <strong style={{ color: '#00D09C' }}>{corr.infoRatio}</strong> (Mean IC / Std IC) places this strategy in the top institutional quintile for signal reliability.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
