import React, { useState } from 'react';
import {
  Activity,
  AlertTriangle,
  BarChart2,
  TrendingDown,
  ShieldAlert,
  Info,
  CheckCircle2
} from 'lucide-react';

export default function DistributionAnalyticsChart({ data, distributionScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('histogram'); // 'histogram' | 'kurtosis' | 'divergence'

  const dist = data || {
    skewness: -0.38,
    excessKurtosis: 4.25,
    divergencePct: 6.2,
    defenseScore: 72.0,
    classification: 'LEPTOKURTIC FAT-TAIL DYNAMICS (BLACK SWAN EXPOSURE)',
    histogram: [
      { bucket: '<-3σ Extreme', observed: 2.1, normal: 0.1, isTail: true },
      { bucket: '-2σ Outlier', observed: 4.8, normal: 2.1, isTail: true },
      { bucket: '-1σ Drawdown', observed: 14.5, normal: 13.6, isTail: false },
      { bucket: 'Mean Return', observed: 48.2, normal: 68.2, isTail: false },
      { bucket: '+1σ Gain', observed: 18.6, normal: 13.6, isTail: false },
      { bucket: '+2σ Spike', observed: 8.5, normal: 2.1, isTail: true },
      { bucket: '>+3σ Surge', observed: 3.3, normal: 0.1, isTail: true }
    ]
  };

  const histogramData = dist.histogram || [
    { bucket: '<-3σ Extreme', observed: 2.1, normal: 0.1, isTail: true },
    { bucket: '-2σ Outlier', observed: 4.8, normal: 2.1, isTail: true },
    { bucket: '-1σ Drawdown', observed: 14.5, normal: 13.6, isTail: false },
    { bucket: 'Mean Return', observed: 48.2, normal: 68.2, isTail: false },
    { bucket: '+1σ Gain', observed: 18.6, normal: 13.6, isTail: false },
    { bucket: '+2σ Spike', observed: 8.5, normal: 2.1, isTail: true },
    { bucket: '>+3σ Surge', observed: 3.3, normal: 0.1, isTail: true }
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
              background: 'rgba(240, 68, 56, 0.15)',
              color: 'var(--loss-red, #F04438)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700,
              textTransform: 'uppercase'
            }}>
              Chapter 24 Exploratory Data Analysis
            </span>
            <span style={{
              background: 'rgba(245, 158, 11, 0.15)',
              color: 'var(--warning-amber, #F59E0B)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {dist.classification}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            Return Distribution Analytics & Leptokurtic Fat Tails
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Empirical Return Density vs Gaussian Bell Curve, Excess Kurtosis, and Negative Skew Asymmetry
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
            { id: 'histogram', label: 'Fat Tail Curve' },
            { id: 'kurtosis', label: 'Excess Kurtosis' },
            { id: 'divergence', label: 'Mean vs Median' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Distribution Skew</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: dist.skewness < 0 ? 'var(--loss-red, #F04438)' : 'var(--gain-green, #00D09C)', fontFamily: 'monospace' }}>
            {dist.skewness > 0 ? `+${dist.skewness}` : dist.skewness}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Excess Kurtosis</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--warning-amber, #F59E0B)', fontFamily: 'monospace' }}>
            +{dist.excessKurtosis}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Mean/Median Spread</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', fontFamily: 'monospace' }}>
            {dist.divergencePct}%
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Tail Defense Score</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', fontFamily: 'monospace' }}>
            {dist.defenseScore} / 100
          </div>
        </div>
      </div>

      {/* Tab 1: Fat Tail Histogram vs Gaussian Bell */}
      {activeTab === 'histogram' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{
            background: 'var(--fintech-background, #0B0E14)',
            border: '1px solid var(--fintech-border, #222A3B)',
            borderRadius: '10px',
            padding: '16px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>
                Observed Return Frequency vs Gaussian Normal Curve
              </span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '11px' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#F04438' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '2px', background: '#F04438' }} /> Fat Tail (&gt;2σ)
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#00D09C' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '2px', background: '#00D09C' }} /> Normal Body
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {histogramData.map((b, idx) => (
                <div key={idx}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '3px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{b.bucket}</span>
                    <span style={{ fontFamily: 'monospace', fontWeight: 700, color: b.isTail ? '#F04438' : '#00D09C' }}>
                      {b.observed}% observed (vs {b.normal}% normal)
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
                      width: `${Math.min(100, b.observed * 1.8)}%`,
                      background: b.isTail ? '#F04438' : '#00D09C',
                      borderRadius: '7px',
                      transition: 'width 0.4s ease'
                    }} />
                    {/* Theoretical Normal Marker */}
                    <div style={{
                      position: 'absolute',
                      left: `${Math.min(95, b.normal * 1.8)}%`,
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
            <strong style={{ color: '#F04438' }}>Fat Tails Highlighted (Red):</strong> Extreme ±3-sigma moves occur over 20x more frequently in financial markets than a Gaussian normal bell curve predicts.
          </div>
        </div>
      )}

      {/* Tab 2: Excess Kurtosis */}
      {activeTab === 'kurtosis' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <AlertTriangle size={18} color="#F59E0B" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Excess Kurtosis: +{dist.excessKurtosis} (Leptokurtic)</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Gaussian standard normal distributions have a kurtosis of 3.0 (excess kurtosis of 0). This asset's excess kurtosis of <strong style={{ color: 'var(--text-primary)' }}>+{dist.excessKurtosis}</strong> confirms that tail risk is clustered into sharp, discontinuous jump risks (Mandelbrot & Taleb black swans).
            </p>
          </div>
        </div>
      )}

      {/* Tab 3: Mean vs Median Divergence */}
      {activeTab === 'divergence' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ fontSize: '14px', fontWeight: 700, marginBottom: '8px' }}>Mean vs Median Outlier Audit: {dist.divergencePct}%</div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              A divergence under 10% indicates minimal outlier skew. The median accurately captures central tendency without being distorted by rare, non-recurring accounting spikes.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
