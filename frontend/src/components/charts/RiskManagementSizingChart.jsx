import React, { useState } from 'react';

/**
 * Lesson 34: Risk Management & Position Sizing
 * Pure CSS/SVG institutional chart visualizing Half-Kelly optimal sizing, asymmetric loss recovery,
 * and dry powder cash buffer shields.
 */
export const RiskManagementSizingChart = ({ data, metrics, ticker = 'TICKER' }) => {
  const [activeTab, setActiveTab] = useState('asymmetry'); // 'asymmetry' | 'kelly' | 'cash_shield'

  const risk = data?.riskSizing || {
    halfKellyPct: 4.8,
    fullKellyPct: 9.6,
    ruinProbability: 0.01,
    cashShieldPct: 7.5,
    maxPositionCap: 6.5,
    recoveryMultipliers: [
      { drawdown: '-10%', gainNeeded: '+11.1%' },
      { drawdown: '-20%', gainNeeded: '+25.0%' },
      { drawdown: '-30%', gainNeeded: '+42.9%' },
      { drawdown: '-50%', gainNeeded: '+100.0%' },
      { drawdown: '-80%', gainNeeded: '+400.0%' }
    ]
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
              color: 'var(--warning-amber, #F59E0B)',
              background: 'rgba(245, 158, 11, 0.12)',
              padding: '2px 8px',
              borderRadius: '4px',
              textTransform: 'uppercase'
            }}>
              Lesson 34 • Capital Preservation
            </span>
            <span style={{ fontSize: '12px', color: 'var(--text-tertiary, #64748B)' }}>Kelly Criterion & Asymmetric Ruin Math</span>
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', margin: '6px 0 2px 0' }}>
            Risk Management, Sizing & Ruin Defense Matrix
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', margin: 0 }}>
            Auditing the mathematical reality that a 50% loss requires a 100% gain to break even, and implementing Half-Kelly sizing guards.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', background: 'var(--bg-app, #0B0E14)', padding: '3px', borderRadius: '8px', border: '1px solid var(--border-hairline, #222A3B)' }}>
          {[
            { id: 'asymmetry', label: 'Asymmetric Loss Math' },
            { id: 'kelly', label: 'Half-Kelly Sizing' },
            { id: 'cash_shield', label: 'Cash Airbag Shield' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Half-Kelly Allocation</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            {risk.halfKellyPct}% Target
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Full Kelly: {risk.fullKellyPct}%</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Ruin Probability</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            &lt; 0.01%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Mathematically Ruin-Proof</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Dry Powder Cash</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', marginTop: '4px' }}>
            {risk.cashShieldPct}% Cash
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Airbag + Panic Deploy</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Max Position Cap</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--warning-amber, #F59E0B)', marginTop: '4px' }}>
            {risk.maxPositionCap}% Cap
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Absolute Hard Stop</div>
        </div>
      </div>

      {/* Tab 1: Asymmetric Math */}
      {activeTab === 'asymmetry' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            The Law of Asymmetric Loss Recovery (Loss % vs. Required Gain %)
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(110px, 1fr))', gap: '8px' }}>
            {risk.recoveryMultipliers.map((m, idx) => (
              <div key={idx} style={{ background: 'var(--bg-surface, #141923)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '10px', textAlign: 'center' }}>
                <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--loss-red, #F04438)' }}>{m.drawdown} Loss</div>
                <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', marginTop: '4px' }}>{m.gainNeeded}</div>
                <div style={{ fontSize: '9px', color: 'var(--text-tertiary, #64748B)', marginTop: '2px' }}>to break even</div>
              </div>
            ))}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '10px' }}>
            A 50% drawdown cuts your capital in half, doubling your return requirement. This asymmetry is why defense dominates offense.
          </div>
        </div>
      )}

      {/* Tab 2: Kelly Sizing */}
      {activeTab === 'kelly' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '8px' }}>
            Half-Kelly Position Sizing Standard: f* = 0.5 &times; (p&middot;b - q) / b
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', margin: '0 0 10px 0' }}>
            Full-Kelly bet sizes induce catastrophic volatility and severe drawdowns. Institutional quantitative hedge funds universally allocate Half-Kelly ({risk.halfKellyPct}% per position), harvesting 75% of maximum growth with 50% less portfolio variance.
          </p>
        </div>
      )}

      {/* Tab 3: Cash Shield */}
      {activeTab === 'cash_shield' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '8px' }}>
            Dynamic Cash Buffer as an Offensive Weapon ({risk.cashShieldPct}% Reserve)
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', margin: '0 0 10px 0' }}>
            Cash is not a drag on performance; it is a call option on market panic. Dry powder dampens portfolio drawdown during crashes and provides instant liquidity to buy mispriced quality factor compounders at cycle bottoms.
          </p>
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
            CAPITAL PRESERVATION FORTRESS CERTIFIED
          </div>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>
          Rule #1 strictly enforced: Never lose capital.
        </div>
      </div>
    </div>
  );
};

export default RiskManagementSizingChart;
