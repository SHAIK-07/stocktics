import React, { useState } from 'react';
import { DollarSign, ShieldAlert, ShieldCheck, Layers, Percent, ArrowDownRight, AlertTriangle, Info, CheckCircle2 } from 'lucide-react';

export default function ValueFactorsChart({ data, valueScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('yieldHierarchy'); // 'yieldHierarchy' | 'trapDefense' | 'intangibleShift'

  const scorecard = valueScorecard || {
    ticker: 'AAPL',
    score: 34.2,
    rating: 'GROWTH MULTIPLE / PREMIUM EXPENSIVE',
    compositeYield: 2.48,
    fcfYield: 3.19,
    evEbitda: 24.0,
    earningsYield: 2.99,
    pbRatio: 53.12,
    isValueTrap: false
  };

  const yieldHierarchy = data?.yieldHierarchy || [
    { name: 'FCF Yield (Cash)', yield: 3.19, rank: 1, tier: 'Gold Standard (Cash Yield)' },
    { name: 'EV/EBITDA Inverse', yield: 4.17, rank: 2, tier: 'Capital Structure Neutral' },
    { name: 'Earnings Yield (E/P)', yield: 2.99, rank: 3, tier: 'Accounting Earnings' },
    { name: 'Book Yield (1/PB)', yield: 1.88, rank: 4, tier: 'Decayed Metric' }
  ];

  const trapAudit = data?.trapAudit || {
    altmanZ: 6.2,
    isAltmanSafe: true,
    fcfPositive: true,
    isTrap: false,
    trapScore: 15.0
  };

  const isBargain = scorecard.score >= 65 && !scorecard.isValueTrap;

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
              Chapter 19 Quantitative Factor
            </span>
            <span style={{
              background: scorecard.isValueTrap ? 'rgba(240, 68, 56, 0.15)' : isBargain ? 'rgba(0, 208, 156, 0.15)' : 'rgba(245, 158, 11, 0.15)',
              color: scorecard.isValueTrap ? 'var(--loss-red, #F04438)' : isBargain ? 'var(--gain-green, #00D09C)' : 'var(--warning-amber, #F59E0B)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {scorecard.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            {scorecard.ticker} Quantitative Value & Cash Yield Hierarchy
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Free Cash Flow Yield prioritization, Enterprise Multiples, and Altman Z Value Trap Defense
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
            { id: 'yieldHierarchy', label: 'Yield Hierarchy' },
            { id: 'trapDefense', label: 'Value Trap Defense' },
            { id: 'intangibleShift', label: 'Intangible P/B Shift' }
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

      {/* KPI Cards */}
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
            Composite Value Yield
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.compositeYield >= 4.5 ? 'var(--gain-green, #00D09C)' : 'var(--fintech-blue, #1455FE)', marginTop: '4px' }}>
            {scorecard.compositeYield.toFixed(2)}%
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Weighted Multi-Metric Yield
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
            FCF Yield (FCF / EV)
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.fcfYield >= 5.0 ? 'var(--gain-green, #00D09C)' : 'var(--text-primary, #F8FAFC)', marginTop: '4px' }}>
            {scorecard.fcfYield.toFixed(2)}%
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Pure Discretionary Cash Flow
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
            EV / EBITDA Multiple
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.evEbitda <= 14 ? 'var(--gain-green, #00D09C)' : 'var(--warning-amber, #F59E0B)', marginTop: '4px' }}>
            {scorecard.evEbitda.toFixed(1)}x
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Capital-Structure Neutral
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
            Value Trap Defense
          </div>
          <div style={{
            fontSize: '20px',
            fontWeight: 700,
            fontFamily: 'monospace',
            color: scorecard.isValueTrap ? 'var(--loss-red, #F04438)' : 'var(--gain-green, #00D09C)',
            marginTop: '4px'
          }}>
            {scorecard.isValueTrap ? 'TRAP FLAG' : 'CLEAN BARGAIN'}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Altman Z: {trapAudit.altmanZ.toFixed(2)} (Safe {'>'} 1.8)
          </div>
        </div>
      </div>

      {/* Main Tab Content */}
      {activeTab === 'yieldHierarchy' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <span style={{ fontSize: '13px', fontWeight: 700 }}>
              The Empirical Cash Yield Hierarchy: Reliability of Value Factors
            </span>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
              Ordered by Historical Predictive Power
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {yieldHierarchy.map((item, idx) => (
              <div key={item.name} style={{
                background: 'var(--fintech-surface-elevated, #1C2333)',
                borderRadius: '8px',
                padding: '12px 14px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                border: '1px solid var(--fintech-border, #222A3B)'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <div style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    background: idx === 0 ? 'var(--fintech-primary, #00D09C)' : 'var(--fintech-border, #222A3B)',
                    color: idx === 0 ? '#0B0E14' : 'var(--text-primary)',
                    fontWeight: 700,
                    fontSize: '12px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}>
                    #{item.rank}
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '13px' }}>{item.name}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>{item.tier}</div>
                  </div>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'monospace', color: idx === 0 ? 'var(--gain-green, #00D09C)' : 'var(--text-primary, #F8FAFC)' }}>
                    {item.yield.toFixed(2)}%
                  </div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
                    {idx === 0 ? 'Highest Predictive Alpha' : idx === 3 ? 'Decayed Statistical Power' : 'Reliable Value Anchor'}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'trapDefense' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
            <ShieldAlert size={18} color={scorecard.isValueTrap ? 'var(--loss-red, #F04438)' : 'var(--gain-green, #00D09C)'} />
            <span style={{ fontSize: '14px', fontWeight: 700 }}>
              The Value Trap Defense Matrix: Solvency & Cash Reality
            </span>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: '12px',
            marginBottom: '14px'
          }}>
            <div style={{
              background: 'var(--fintech-surface-elevated, #1C2333)',
              padding: '12px',
              borderRadius: '8px',
              border: '1px solid var(--fintech-border, #222A3B)'
            }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Altman Z-Score Solvency</div>
              <div style={{ fontSize: '18px', fontWeight: 700, fontFamily: 'monospace', color: trapAudit.isAltmanSafe ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)', margin: '4px 0' }}>
                {trapAudit.altmanZ.toFixed(2)} ({trapAudit.isAltmanSafe ? 'Safe Zone' : 'Distress Risk'})
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>Cutoff: &gt; 1.81 avoids bankruptcy traps</div>
            </div>

            <div style={{
              background: 'var(--fintech-surface-elevated, #1C2333)',
              padding: '12px',
              borderRadius: '8px',
              border: '1px solid var(--fintech-border, #222A3B)'
            }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Free Cash Flow Generation</div>
              <div style={{ fontSize: '18px', fontWeight: 700, fontFamily: 'monospace', color: trapAudit.fcfPositive ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)', margin: '4px 0' }}>
                {trapAudit.fcfPositive ? 'Positive Cash Flow' : 'Negative Cash Burn'}
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>True value requires discretionary cash flow</div>
            </div>
          </div>

          <div style={{
            background: 'var(--fintech-surface-elevated, #1C2333)',
            padding: '12px 16px',
            borderRadius: '8px',
            borderLeft: `4px solid ${scorecard.isValueTrap ? 'var(--loss-red, #F04438)' : 'var(--gain-green, #00D09C)'}`
          }}>
            <div style={{ fontWeight: 700, fontSize: '13px', color: 'var(--text-primary, #F8FAFC)' }}>
              Quantitative Verdict: {scorecard.isValueTrap ? 'CRITICAL VALUE TRAP WARNING' : 'VERIFIED CASH FLOW BARGAIN'}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', marginTop: '4px' }}>
              {scorecard.isValueTrap
                ? 'High risk of capital impairment. Multiple is cheap because earnings and cash flows are decaying toward financial distress.'
                : 'Pristine balance sheet and positive FCF ensure this company is not a value trap. Safe for quantitative value factor allocation.'}
            </div>
          </div>
        </div>
      )}

      {activeTab === 'intangibleShift' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
            <Info size={18} color="var(--fintech-blue, #1455FE)" />
            <span style={{ fontSize: '14px', fontWeight: 700 }}>
              Why Accounting Price-to-Book (P/B) Broke in Modern Markets
            </span>
          </div>

          <p style={{ fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: 1.5, margin: '0 0 12px 0' }}>
            In traditional manufacturing economies, balance sheet assets (factories, machinery, inventory) reflected economic value. In modern knowledge economies, the most valuable assets (software codebases, patents, brand equity, algorithm architectures) are immediately expensed under GAAP rules as R&D on the income statement rather than capitalized on the balance sheet.
          </p>

          <div style={{
            background: 'var(--fintech-surface-elevated, #1C2333)',
            padding: '12px 14px',
            borderRadius: '8px',
            border: '1px solid var(--fintech-border, #222A3B)',
            fontSize: '12px',
            color: 'var(--text-primary, #F8FAFC)',
            lineHeight: 1.5
          }}>
            <strong style={{ color: 'var(--fintech-primary, #00D09C)' }}>Institutional Rule:</strong> Never use P/B as a primary value filter for tech, healthcare, or digital platforms. Focus on <strong>Free Cash Flow Yield</strong> and <strong>EV / EBITDA</strong>, which capture real operational monetization regardless of balance sheet accounting treatment.
          </div>
        </div>
      )}
    </div>
  );
}
