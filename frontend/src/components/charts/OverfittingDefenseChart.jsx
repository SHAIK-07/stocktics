import React, { useState } from 'react';

/**
 * Lesson 31: Overfitting & Curve-Fitting Defense
 * Pure CSS/SVG institutional chart visualizing model complexity, In-Sample vs. Out-of-Sample gap,
 * and Walk-Forward Efficiency (WFE).
 */
export const OverfittingDefenseChart = ({ data, metrics, ticker = 'TICKER' }) => {
  const [activeTab, setActiveTab] = useState('curve_fit'); // 'curve_fit' | 'splits' | 'scorecard'

  const audit = data?.overfittingAudit || {
    paramCount: 4,
    wfeRatio: 74.2,
    split: '70% Train / 15% OOS / 15% Walk-Forward',
    verdict: 'ROBUST SIGNAL VALIDATED',
    inSampleCagr: 18.5,
    outSampleCagr: 13.7,
    inSampleSharpe: 1.65,
    outSampleSharpe: 1.38
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
              color: 'var(--loss-red, #F04438)',
              background: 'rgba(240, 68, 56, 0.12)',
              padding: '2px 8px',
              borderRadius: '4px',
              textTransform: 'uppercase'
            }}>
              Lesson 31 • Backtesting Defense
            </span>
            <span style={{ fontSize: '12px', color: 'var(--text-tertiary, #64748B)' }}>Occam's Razor & Walk-Forward Math</span>
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', margin: '6px 0 2px 0' }}>
            Overfitting & Curve-Fitting Defense Matrix
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', margin: 0 }}>
            Audits parameter parsimony, out-of-sample data partitioning, and walk-forward efficiency to prevent historical noise memorization.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', background: 'var(--bg-app, #0B0E14)', padding: '3px', borderRadius: '8px', border: '1px solid var(--border-hairline, #222A3B)' }}>
          {[
            { id: 'curve_fit', label: 'Curve-Fit Trap' },
            { id: 'splits', label: 'Temporal Splits' },
            { id: 'scorecard', label: 'Walk-Forward WFE' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Tuned Parameters</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            {audit.paramCount} Factors
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Target: &le; 5 (Parsimony)</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>In-Sample CAGR</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', marginTop: '4px' }}>
            +{audit.inSampleCagr}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Sharpe: {audit.inSampleSharpe}</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Out-of-Sample CAGR</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            +{audit.outSampleCagr}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Sharpe: {audit.outSampleSharpe}</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Walk-Forward WFE</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            {audit.wfeRatio}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Retains 74% of IS power</div>
        </div>
      </div>

      {/* Tab 1: Curve Fit Trap */}
      {activeTab === 'curve_fit' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            The Curve-Fitting Trap: Economic Law vs. Polynomial Overfitting
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
            <div style={{ background: 'var(--bg-surface, #141923)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--gain-green, #00D09C)' }} />
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>Robust Model (Occam's Razor)</span>
              </div>
              <p style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', margin: '0 0 8px 0' }}>
                Uses 3-5 intuitive factors (ROIC, FCF yield, 12-1M Momentum). Preserves statistical degrees of freedom. Generalizes across regimes.
              </p>
              <div style={{ fontSize: '11px', color: 'var(--gain-green, #00D09C)', fontWeight: 600 }}>
                &bull; Live Sharpe: 1.38 &bull; Max Drawdown: -18.4%
              </div>
            </div>

            <div style={{ background: 'var(--bg-surface, #141923)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--loss-red, #F04438)' }} />
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--loss-red, #F04438)' }}>Overfitted Model (Curve-Fitted)</span>
              </div>
              <p style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', margin: '0 0 8px 0' }}>
                Uses 25 complex rules (e.g., 'Buy only if RSI=47.2 and VIX&lt;19 on Tuesdays'). Memorizes historical noise that never repeats.
              </p>
              <div style={{ fontSize: '11px', color: 'var(--loss-red, #F04438)', fontWeight: 600 }}>
                &bull; Live Sharpe: -0.25 (Collapses upon live capital deployment)
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Temporal Splits */}
      {activeTab === 'splits' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '12px' }}>
            Data Segregation Protocol: Train vs. OOS vs. Walk-Forward
          </div>
          <div style={{ display: 'flex', height: '36px', borderRadius: '8px', overflow: 'hidden', marginBottom: '14px' }}>
            <div style={{ width: '70%', background: 'var(--fintech-blue, #1455FE)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700, color: '#fff' }}>
              In-Sample Training (70%)
            </div>
            <div style={{ width: '15%', background: 'var(--warning-amber, #F59E0B)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700, color: '#0B0E14' }}>
              Validation (15%)
            </div>
            <div style={{ width: '15%', background: 'var(--gain-green, #00D09C)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700, color: '#0B0E14' }}>
              Blind OOS (15%)
            </div>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
            Strictly freezes model parameters at the conclusion of In-Sample calibration. Any subsequent tweaking after viewing OOS data triggers immediate rejection.
          </div>
        </div>
      )}

      {/* Tab 3: Walk-Forward Scorecard */}
      {activeTab === 'scorecard' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            Walk-Forward Efficiency (WFE) Standard
          </div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', background: 'var(--bg-surface, #141923)', borderRadius: '8px' }}>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Current WFE Performance</div>
              <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>
                {audit.wfeRatio}% (Passed Gate &ge; 65.0%)
              </div>
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', textAlign: 'right' }}>
              Deflated Sharpe Ratio (DSR): <strong>1.42</strong> (p &lt; 0.01)
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
            {audit.verdict}
          </div>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>
          Model strictly adheres to 4 core parameters; zero multiple hypothesis snooping.
        </div>
      </div>
    </div>
  );
};

export default OverfittingDefenseChart;
