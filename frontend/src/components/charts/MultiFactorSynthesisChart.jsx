import React, { useState } from 'react';
import {
  Layers,
  Target,
  ShieldCheck,
  Zap,
  Activity,
  Award,
  Compass,
  CheckCircle2
} from 'lucide-react';

export default function MultiFactorSynthesisChart({ data, multiFactorScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('radar'); // 'radar' | 'dispersion' | 'diversification'

  const scorecard = multiFactorScorecard || {
    ticker: 'STOCK',
    score: 82.4,
    rating: 'TIER-1 ALL-WEATHER MULTI-FACTOR',
    dispersion: 12.8,
    orthoScore: 85.0,
    topFactor: 'Quality'
  };

  const FACTOR_META = {
    Quality: {
      color: '#00D09C',
      desc: 'Piotroski F-Score & Gross Profitability'
    },
    Value: {
      color: '#1455FE',
      desc: 'FCF Yield & EV/EBITDA Discount'
    },
    Momentum: {
      color: '#0EA5E9',
      desc: '12-1M Relative Price Persistence'
    },
    Growth: {
      color: '#F59E0B',
      desc: '3Y Revenue CAGR & Internal SGR'
    }
  };

  const chartData = data || {
    compositeAlpha: 82.4,
    dispersion: 12.8,
    diversificationScore: 85.0,
    classification: 'TIER-1 ALL-WEATHER MULTI-FACTOR',
    radar: [
      { factor: 'Quality', score: 88.0, weight: 30 },
      { factor: 'Value', score: 72.0, weight: 25 },
      { factor: 'Momentum', score: 85.0, weight: 25 },
      { factor: 'Growth', score: 79.0, weight: 20 }
    ]
  };

  const rawRadar = chartData.radar || [
    { factor: 'Quality', score: 88.0, weight: 30 },
    { factor: 'Value', score: 72.0, weight: 25 },
    { factor: 'Momentum', score: 85.0, weight: 25 },
    { factor: 'Growth', score: 79.0, weight: 20 }
  ];

  const radarData = rawRadar.map((rf, idx) => {
    const meta = FACTOR_META[rf.factor] || {
      color: idx === 0 ? '#00D09C' : idx === 1 ? '#1455FE' : idx === 2 ? '#0EA5E9' : '#F59E0B',
      desc: 'Quantitative Style Factor'
    };
    return {
      ...rf,
      color: rf.color || meta.color,
      desc: rf.desc || meta.desc
    };
  });

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
              Chapter 22 Quantitative Factor
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
            {scorecard.ticker} Multi-Factor Synthesis & Blend Model
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Systematic Blend of Quality (30%), Value (25%), Momentum (25%), and Growth (20%)
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
            { id: 'radar', label: 'Factor Blend' },
            { id: 'dispersion', label: 'Dispersion Balance' },
            { id: 'diversification', label: 'Orthogonal Shield' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Composite Alpha</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            {chartData.compositeAlpha} / 100
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Dominant Factor</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)' }}>
            {scorecard.topFactor}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Factor Dispersion</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
            ±{chartData.dispersion} pts
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Orthogonal Shield</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', fontFamily: 'monospace' }}>
            {chartData.diversificationScore} / 100
          </div>
        </div>
      </div>

      {/* Tab 1: Factor Blend Breakdown */}
      {activeTab === 'radar' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{
            background: 'var(--fintech-background, #0B0E14)',
            border: '1px solid var(--fintech-border, #222A3B)',
            borderRadius: '10px',
            padding: '16px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)' }}>
                Systematic Factor Exposures (0-100 Score Scale)
              </span>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
                Dashed line: 50.0 Neutral Benchmark
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {radarData.map((rf, idx) => (
                <div key={idx}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                    <div>
                      <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{rf.factor} Factor</span>
                      <span style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', marginLeft: '6px' }}>({rf.desc})</span>
                    </div>
                    <span style={{ fontFamily: 'monospace', fontWeight: 700, color: rf.color }}>
                      {rf.score} / 100 (Weight: {rf.weight}%)
                    </span>
                  </div>
                  <div style={{
                    height: '14px',
                    background: 'var(--fintech-surface-elevated, #1C2333)',
                    border: '1px solid var(--fintech-border, #222A3B)',
                    borderRadius: '7px',
                    overflow: 'hidden',
                    position: 'relative'
                  }}>
                    <div style={{
                      height: '100%',
                      width: `${Math.min(100, Math.max(3, rf.score))}%`,
                      background: rf.color,
                      borderRadius: '7px',
                      transition: 'width 0.4s ease'
                    }} />
                    {/* 50 Benchmark Line */}
                    <div style={{
                      position: 'absolute',
                      left: '50%',
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

          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', background: 'var(--fintech-surface-elevated, #1C2333)', border: '1px solid var(--fintech-border, #222A3B)', padding: '10px 14px', borderRadius: '8px' }}>
            Multi-factor models mitigate single-factor drawdowns. Combining Value with Momentum captures orthogonal anomalies, while Quality protects downside capital during market corrections.
          </div>
        </div>
      )}

      {/* Tab 2: Dispersion Balance */}
      {activeTab === 'dispersion' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ fontSize: '14px', fontWeight: 700, marginBottom: '8px' }}>Factor Balance Dispersion Analysis</div>
            <p style={{ margin: '0 0 12px 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
              Standard deviation across individual style ranks is <strong style={{ color: 'var(--text-primary)' }}>±{chartData.dispersion} points</strong>.
              {chartData.dispersion <= 18 ? (
                <span style={{ color: '#00D09C', fontWeight: 600 }}> Confirms a well-balanced all-weather factor profile without excessive dependence on any single style.</span>
              ) : (
                <span style={{ color: '#F59E0B', fontWeight: 600 }}> Displays elevated factor skew; portfolio performance will be heavily dominated by {scorecard.topFactor}.</span>
              )}
            </p>
          </div>
        </div>
      )}

      {/* Tab 3: Orthogonal Diversification */}
      {activeTab === 'diversification' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <ShieldCheck size={18} color="#00D09C" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Orthogonal Anti-Correlation Shield: {chartData.diversificationScore}/100</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Historically, Value and Momentum exhibit negative correlation (-0.35). By pairing high-quality compounding with momentum trend following, the multi-factor model dampens maximum portfolio drawdowns while preserving compound alpha.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
