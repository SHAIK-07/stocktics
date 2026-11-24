import React, { useState } from 'react';

/**
 * Lesson 35: Building a Quantitative Portfolio Engine
 * Pure CSS/SVG institutional chart visualizing the closed-loop 6-stage systematic factory:
 * Universe -> Quality Gate -> Factor Ranking -> Selection -> Weighting -> Drift Rebalancing.
 */
export const QuantitativePortfolioChart = ({ data, metrics, ticker = 'TICKER' }) => {
  const [activeTab, setActiveTab] = useState('pipeline'); // 'pipeline' | 'rebalance' | 'integrity'

  const qPort = data?.quantitativePortfolio || {
    readinessScore: 96.0,
    driftPct: 3.2,
    selectionCount: 25,
    turnoverDrag: 0.85,
    rebalanceSchedule: 'Quarterly with 5% Drift Tolerance',
    qualityGatePassed: true,
    pipelineStages: [
      { step: '1. Universe', rule: 'US Liquid Equities (ADV > $15M, MCap > $2B)', status: 'Passed' },
      { step: '2. Quality Gate', rule: 'ROIC > 12%, Altman Z > 2.5, Accruals < 5%', status: 'Passed' },
      { step: '3. Multi-Factor', rule: '40% Quality + 30% Value + 30% Momentum', status: 'Passed' },
      { step: '4. Selection', rule: 'Top 25 Factor Leaders with 25% Sector Cap', status: 'Passed' },
      { step: '5. Weighting', rule: 'Vol-Parity Half-Kelly (Max 8% per stock)', status: 'Passed' },
      { step: '6. Rebalance', rule: 'Quarterly Execution with 5% Drift Bands', status: 'Passed' }
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
              color: 'var(--gain-green, #00D09C)',
              background: 'rgba(0, 208, 156, 0.12)',
              padding: '2px 8px',
              borderRadius: '4px',
              textTransform: 'uppercase'
            }}>
              Lesson 35 • Systematic Execution Engine
            </span>
            <span style={{ fontSize: '12px', color: 'var(--text-tertiary, #64748B)' }}>Stage 7: Complete Portfolio Factory</span>
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', margin: '6px 0 2px 0' }}>
            End-to-End Quantitative Portfolio Pipeline
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', margin: 0 }}>
            The closed-loop factory translating factors, quality gates, and position sizing into an automated rule-based investing engine.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', background: 'var(--bg-app, #0B0E14)', padding: '3px', borderRadius: '8px', border: '1px solid var(--border-hairline, #222A3B)' }}>
          {[
            { id: 'pipeline', label: '6-Stage Factory' },
            { id: 'rebalance', label: 'Rebalance Drift Band' },
            { id: 'integrity', label: 'System Integrity' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Engine Readiness</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            {qPort.readinessScore}/100
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Systematic Engine Ready</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Factor Leaders</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', marginTop: '4px' }}>
            Top {qPort.selectionCount} Stocks
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Quality + Value + Mom</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Current Drift</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            +{qPort.driftPct}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Within 5.0% tolerance</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Turnover Drag</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            -{qPort.turnoverDrag}% / Yr
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Retains 92%+ gross alpha</div>
        </div>
      </div>

      {/* Tab 1: 6-Stage Factory */}
      {activeTab === 'pipeline' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '12px' }}>
            The 6-Stage Quantitative Assembly Line
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '8px' }}>
            {qPort.pipelineStages.map((st, idx) => (
              <div key={idx} style={{ background: 'var(--bg-surface, #141923)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '10px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)' }}>{st.step}</span>
                  <span style={{ fontSize: '10px', color: 'var(--gain-green, #00D09C)', fontWeight: 700 }}>&check;</span>
                </div>
                <div style={{ fontSize: '10px', color: 'var(--text-secondary, #94A3B8)', marginTop: '4px', lineHeight: '1.4' }}>
                  {st.rule}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab 2: Rebalance Drift */}
      {activeTab === 'rebalance' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '8px' }}>
            Quarterly Calendar Rebalancing & Drift Threshold Corridor
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', margin: '0 0 10px 0' }}>
            Executing rebalances on strict quarterly dates (or when any single stock deviates by &gt; 5% from its target weight) harvests volatility premiums and prevents transaction churn from eating portfolio returns.
          </p>
        </div>
      )}

      {/* Tab 3: System Integrity */}
      {activeTab === 'integrity' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '8px' }}>
            Zero-Discretion Operating Mandate
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', margin: '0 0 10px 0' }}>
            Emotional discretion is completely prohibited. The quantitative engine systematically executes purchases, position trims, and factor rotations based on empirical mathematical rules.
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
            CLOSED-LOOP SYSTEMATIC FACTORY ACTIVE
          </div>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>
          Governed by code and empirical evidence; zero emotional bias.
        </div>
      </div>
    </div>
  );
};

export default QuantitativePortfolioChart;
