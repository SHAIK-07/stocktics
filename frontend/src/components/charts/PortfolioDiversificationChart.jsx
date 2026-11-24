import React, { useState } from 'react';

/**
 * Lesson 33: Portfolio Basics & Diversification Mechanics
 * Pure CSS/SVG institutional chart visualizing the idiosyncratic risk reduction curve (1 to 30 stocks),
 * Effective N holdings, and sector concentration ceilings.
 */
export const PortfolioDiversificationChart = ({ data, metrics, ticker = 'TICKER' }) => {
  const [activeTab, setActiveTab] = useState('risk_curve'); // 'risk_curve' | 'ceilings' | 'covariance'

  const divData = data?.portfolioDiversification || {
    effectiveN: 24.5,
    idiosyncraticReduction: 91.5,
    maxSectorExposure: 22.0,
    meanCorrelation: 0.28,
    optimalHoldingsRange: '20 - 30 Stocks',
    sectorCeiling: 25.0
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
              color: 'var(--fintech-blue, #1455FE)',
              background: 'rgba(20, 85, 254, 0.12)',
              padding: '2px 8px',
              borderRadius: '4px',
              textTransform: 'uppercase'
            }}>
              Lesson 33 • Stage 7: Portfolio Construction
            </span>
            <span style={{ fontSize: '12px', color: 'var(--text-tertiary, #64748B)' }}>Modern Portfolio Theory & Covariance</span>
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', margin: '6px 0 2px 0' }}>
            Portfolio Diversification & Risk Elimination Matrix
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', margin: 0 }}>
            Visualizing the mathematical elimination of idiosyncratic single-company risk through low-covariance asset blending.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', background: 'var(--bg-app, #0B0E14)', padding: '3px', borderRadius: '8px', border: '1px solid var(--border-hairline, #222A3B)' }}>
          {[
            { id: 'risk_curve', label: 'Risk Decay Curve' },
            { id: 'ceilings', label: 'Sector & Stock Caps' },
            { id: 'covariance', label: 'Covariance Matrix' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Effective N (Holdings)</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            {divData.effectiveN} Stocks
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Optimal: 20 to 30</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Idiosyncratic Risk Cut</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            {divData.idiosyncraticReduction}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Company risk eliminated</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Max Sector Exposure</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', marginTop: '4px' }}>
            {divData.maxSectorExposure}%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Ceiling: &le; 25.0%</div>
        </div>
        <div style={{ background: 'var(--bg-app, #0B0E14)', border: '1px solid var(--border-hairline, #222A3B)', borderRadius: '8px', padding: '12px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Mean Pairwise Correl</div>
          <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', marginTop: '4px' }}>
            r = {divData.meanCorrelation}
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Low cross-asset correlation</div>
        </div>
      </div>

      {/* Tab 1: Risk Curve */}
      {activeTab === 'risk_curve' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            The 1-to-30 Stock Risk Elimination Curve (Markowitz Free Lunch)
          </div>
          <div style={{ height: '80px', background: 'var(--bg-surface, #141923)', borderRadius: '8px', position: 'relative', padding: '10px 14px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px' }}>
              <span style={{ color: 'var(--loss-red, #F04438)', fontWeight: 600 }}>1 Stock: 100% Firm Risk</span>
              <span style={{ color: 'var(--warning-amber, #F59E0B)', fontWeight: 600 }}>10 Stocks: 70% Cut</span>
              <span style={{ color: 'var(--gain-green, #00D09C)', fontWeight: 700 }}>25 Stocks: 91.5% Cut (Sweet Spot)</span>
            </div>
            {/* Visual decay bar */}
            <div style={{ width: '100%', height: '8px', background: 'var(--bg-surface-elevated, #1C2333)', borderRadius: '4px', marginTop: '16px', overflow: 'hidden' }}>
              <div style={{ width: '91.5%', height: '100%', background: 'linear-gradient(90deg, #F04438 0%, #F59E0B 40%, #00D09C 100%)' }} />
            </div>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '8px' }}>
            Holding beyond 30 stocks yields negligible further risk reduction while introducing analytical dilution ('diworsification').
          </div>
        </div>
      )}

      {/* Tab 2: Sector & Stock Caps */}
      {activeTab === 'ceilings' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            Institutional Position & Sector Concentration Ceilings
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div style={{ background: 'var(--bg-surface, #141923)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border-hairline, #222A3B)' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Single Stock Hard Cap</div>
              <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>8.0% Maximum</div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)', marginTop: '4px' }}>
                Prevents catastrophic single-company fraud or bankruptcy from wrecking the fund.
              </div>
            </div>
            <div style={{ background: 'var(--bg-surface, #141923)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border-hairline, #222A3B)' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>GICS Sector Hard Cap</div>
              <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)' }}>25.0% Maximum</div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)', marginTop: '4px' }}>
                Shields portfolio from macroeconomic sector-wide rotation crashes (e.g. 2022 tech drop).
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Covariance Matrix */}
      {activeTab === 'covariance' && (
        <div style={{ background: 'var(--bg-app, #0B0E14)', borderRadius: '10px', border: '1px solid var(--border-hairline, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            Cross-Sector Correlation Matrix (r &lt; 0.40 Target)
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.6' }}>
            &bull; Tech vs. Healthcare: <strong>r = 0.22</strong> (Low Correlation)<br/>
            &bull; Tech vs. Energy: <strong>r = -0.05</strong> (Negative / Uncorrelated Hedge)<br/>
            &bull; Consumer Defensive vs. Financials: <strong>r = 0.31</strong> (Moderate Stability)
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
            OPTIMALLY DIVERSIFIED CORE (N=25)
          </div>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)' }}>
          Idiosyncratic risk virtually eliminated; systematic market beta preserved.
        </div>
      </div>
    </div>
  );
};

export default PortfolioDiversificationChart;
