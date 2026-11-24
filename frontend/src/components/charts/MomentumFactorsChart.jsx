import React, { useState } from 'react';
import { TrendingUp, TrendingDown, Activity, AlertTriangle, ShieldCheck, Zap, Layers, ArrowUpRight, BarChart2 } from 'lucide-react';

export default function MomentumFactorsChart({ data, momentumScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('horizons'); // 'horizons' | 'trendlines' | 'crashRisk'

  const scorecard = momentumScorecard || {
    ticker: 'AAPL',
    score: 86.3,
    rating: 'STRONG BULL MOMENTUM',
    mom12_1m: 23.9,
    mom6m: 11.3,
    spread50: 2.8,
    spread200: 15.4,
    crashRisk: 'LOW',
    goldenCross: true
  };

  const horizons = data?.horizons || [
    { label: '12-1M', value: 23.9, benchmark: 15.0 },
    { label: '6M', value: 11.3, benchmark: 8.0 },
    { label: '3M', value: 5.6, benchmark: 3.0 },
    { label: '1M', value: 1.8, benchmark: 0.0 }
  ];

  const dma = data?.dmaGauges || {
    currentPrice: 224.50,
    dma50: 218.40,
    dma200: 194.50,
    spread50: 2.8,
    spread200: 15.4,
    goldenCross: true
  };

  const crashRisk = data?.crashRisk || {
    level: 'LOW',
    riskSpread: 15.4,
    warningThreshold: 35.0
  };

  const isBull = scorecard.score >= 65;
  const isHighCrashRisk = crashRisk.level === 'HIGH';

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
              background: 'rgba(20, 85, 254, 0.15)',
              color: 'var(--fintech-blue, #1455FE)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700,
              textTransform: 'uppercase'
            }}>
              Chapter 18 Quantitative Factor
            </span>
            <span style={{
              background: isBull ? 'rgba(0, 208, 156, 0.15)' : 'rgba(240, 68, 56, 0.15)',
              color: isBull ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {scorecard.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            {scorecard.ticker} Momentum Factor & Trend Persistence
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            12-Minus-1 Month Jegadeesh & Titman intermediate momentum, moving average confirmation & crash vulnerability audit
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
            { id: 'horizons', label: 'Return Horizons' },
            { id: 'trendlines', label: 'Moving Averages' },
            { id: 'crashRisk', label: 'Crash Risk Defense' }
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
                fontWeight: 700,
                cursor: 'pointer',
                transition: 'all 0.2s'
              }}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* KPI Cards Strip */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: '12px',
        marginBottom: '20px'
      }}>
        {/* KPI 1 */}
        <div style={{
          background: 'var(--fintech-surface-elevated, #1C2333)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '12px'
        }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', textTransform: 'uppercase', fontWeight: 600 }}>
            12-1M Momentum Return
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.mom12_1m >= 0 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)', marginTop: '4px' }}>
            {scorecard.mom12_1m >= 0 ? '+' : ''}{scorecard.mom12_1m.toFixed(1)}%
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Excludes Month t (Anti-Reversion)
          </div>
        </div>

        {/* KPI 2 */}
        <div style={{
          background: 'var(--fintech-surface-elevated, #1C2333)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '12px'
        }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', textTransform: 'uppercase', fontWeight: 600 }}>
            50 DMA Trend Spread
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.spread50 >= 0 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)', marginTop: '4px' }}>
            {scorecard.spread50 >= 0 ? '+' : ''}{scorecard.spread50.toFixed(1)}%
          </div>
          <div style={{ fontSize: '11px', color: scorecard.goldenCross ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)', marginTop: '2px', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Zap size={12} /> {scorecard.goldenCross ? 'Golden Cross Active' : 'Death Cross'}
          </div>
        </div>

        {/* KPI 3 */}
        <div style={{
          background: 'var(--fintech-surface-elevated, #1C2333)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '12px'
        }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', textTransform: 'uppercase', fontWeight: 600 }}>
            200 DMA Secular Gap
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.spread200 >= 0 ? 'var(--fintech-blue, #1455FE)' : 'var(--loss-red, #F04438)', marginTop: '4px' }}>
            {scorecard.spread200 >= 0 ? '+' : ''}{scorecard.spread200.toFixed(1)}%
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            200 DMA: {currencySymbol}{dma.dma200.toFixed(2)}
          </div>
        </div>

        {/* KPI 4 */}
        <div style={{
          background: 'var(--fintech-surface-elevated, #1C2333)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '12px'
        }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', textTransform: 'uppercase', fontWeight: 600 }}>
            Crash Risk Flag
          </div>
          <div style={{
            fontSize: '20px',
            fontWeight: 700,
            fontFamily: 'monospace',
            color: crashRisk.level === 'HIGH' ? 'var(--loss-red, #F04438)' : crashRisk.level === 'MODERATE' ? 'var(--warning-amber, #F59E0B)' : 'var(--gain-green, #00D09C)',
            marginTop: '4px'
          }}>
            {crashRisk.level} RISK
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Threshold: +35.0% extension
          </div>
        </div>
      </div>

      {/* Main Tab Content */}
      {activeTab === 'horizons' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>
              Cross-Sectional Factor Term Structure (Return vs Benchmark)
            </span>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
              Green: Outperforming Benchmark | Marker: Historical Benchmark Line
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {horizons.map(h => {
              const isBeating = h.value >= h.benchmark;
              const barWidth = Math.min(100, Math.max(5, Math.abs(h.value) * 2.5));

              return (
                <div key={h.label}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary, #F8FAFC)' }}>
                      {h.label} Return Window
                    </span>
                    <span style={{ fontFamily: 'monospace', fontWeight: 700, color: h.value >= 0 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)' }}>
                      {h.value >= 0 ? '+' : ''}{h.value.toFixed(1)}% (Benchmark: +{h.benchmark.toFixed(1)}%)
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
                      background: isBeating ? 'var(--fintech-primary, #00D09C)' : 'var(--fintech-blue, #1455FE)',
                      borderRadius: '7px',
                      transition: 'width 0.4s ease'
                    }} />
                    {/* Benchmark marker */}
                    <div style={{
                      position: 'absolute',
                      left: `${Math.min(95, h.benchmark * 2.5)}%`,
                      top: 0,
                      bottom: 0,
                      width: '2px',
                      background: '#F8FAFC',
                      opacity: 0.7
                    }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {activeTab === 'trendlines' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ fontSize: '13px', fontWeight: 700, marginBottom: '14px' }}>
            Moving Average Trend Hierarchy & Spatial Spread
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '14px' }}>
            <div style={{
              background: 'var(--fintech-surface-elevated, #1C2333)',
              padding: '14px',
              borderRadius: '8px',
              border: '1px solid var(--fintech-border, #222A3B)'
            }}>
              <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)' }}>Current Stock Price</div>
              <div style={{ fontSize: '22px', fontWeight: 700, fontFamily: 'monospace', color: 'var(--text-primary, #F8FAFC)', margin: '6px 0' }}>
                {currencySymbol}{dma.currentPrice.toFixed(2)}
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>Real-Time Price Anchor</div>
            </div>

            <div style={{
              background: 'var(--fintech-surface-elevated, #1C2333)',
              padding: '14px',
              borderRadius: '8px',
              border: '1px solid var(--fintech-border, #222A3B)'
            }}>
              <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)' }}>50-Day Moving Average</div>
              <div style={{ fontSize: '22px', fontWeight: 700, fontFamily: 'monospace', color: 'var(--fintech-primary, #00D09C)', margin: '6px 0' }}>
                {currencySymbol}{dma.dma50.toFixed(2)}
              </div>
              <div style={{ fontSize: '11px', color: dma.spread50 >= 0 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)' }}>
                {dma.spread50 >= 0 ? 'Price is Above 50 DMA (+)' : 'Price is Below 50 DMA (-)'}
              </div>
            </div>

            <div style={{
              background: 'var(--fintech-surface-elevated, #1C2333)',
              padding: '14px',
              borderRadius: '8px',
              border: '1px solid var(--fintech-border, #222A3B)'
            }}>
              <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)' }}>200-Day Moving Average</div>
              <div style={{ fontSize: '22px', fontWeight: 700, fontFamily: 'monospace', color: 'var(--fintech-blue, #1455FE)', margin: '6px 0' }}>
                {currencySymbol}{dma.dma200.toFixed(2)}
              </div>
              <div style={{ fontSize: '11px', color: dma.spread200 >= 0 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)' }}>
                {dma.spread200 >= 0 ? 'Secular Bull Regime (+)' : 'Secular Bear Trend (-)'}
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'crashRisk' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
            <AlertTriangle size={18} color={isHighCrashRisk ? 'var(--loss-red, #F04438)' : 'var(--warning-amber, #F59E0B)'} />
            <span style={{ fontSize: '14px', fontWeight: 700 }}>
              The Momentum Crash Diagnostic & Regime Inflection Check
            </span>
          </div>

          <p style={{ fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: 1.5, margin: '0 0 14px 0' }}>
            Pure price momentum exhibits fat-tailed crash risk when macroeconomic regimes pivot from recession to early-cycle expansion. When market bottoms occur, low-quality, high-beta laggards rally faster than past winners.
          </p>

          <div style={{
            background: 'var(--fintech-surface-elevated, #1C2333)',
            padding: '12px 16px',
            borderRadius: '8px',
            borderLeft: `4px solid ${isHighCrashRisk ? 'var(--loss-red, #F04438)' : 'var(--gain-green, #00D09C)'}`
          }}>
            <div style={{ fontWeight: 700, fontSize: '13px', color: 'var(--text-primary, #F8FAFC)' }}>
              Current Status: {crashRisk.level} VULNERABILITY ({scorecard.spread200.toFixed(1)}% above 200 DMA)
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', marginTop: '4px' }}>
              {isHighCrashRisk
                ? 'CAUTION: Extremely extended above 200 DMA (>35%). Vulnerable to violent mean-reversion pullbacks.'
                : 'HEALTHY: Momentum is technically anchored within safe statistical bounds (<35% 200 DMA extension).'}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
