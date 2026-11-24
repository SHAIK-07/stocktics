import React, { useState } from 'react';

/**
 * Lesson 30: Survivorship Bias & The Delisting Graveyard
 * Institutional fintech visualization comparing naive survivor performance vs. survivorship-free reality,
 * listing historical corporate bankruptcies/delistings, and displaying liquidation haircuts.
 */
export const SurvivorshipBiasChart = ({ data, metrics, ticker = 'TICKER' }) => {
  const [activeTab, setActiveTab] = useState('alpha_gap'); // 'alpha_gap' | 'graveyard' | 'audit'

  const defaultDelisted = [
    { ticker: 'ENRNQ', name: 'Enron Corporation', year: 2001, reason: 'Off-balance sheet SPE fraud & bankruptcy', finalReturn: -100.0, sector: 'Energy' },
    { ticker: 'WCOM', name: 'WorldCom Inc.', year: 2002, reason: 'Line cost capitalization fraud ($11B)', finalReturn: -100.0, sector: 'Telecom' },
    { ticker: 'LEH', name: 'Lehman Brothers Holdings', year: 2008, reason: 'Subprime MBS liquidity collapse (Repo 105)', finalReturn: -99.9, sector: 'Financials' },
    { ticker: 'WMI', name: 'Washington Mutual', year: 2008, reason: 'Largest US bank failure in history (OTS seizure)', finalReturn: -98.7, sector: 'Financials' },
    { ticker: 'SIVB', name: 'Silicon Valley Bank', year: 2023, reason: 'Duration mismatch & unhedged HTM bank run', finalReturn: -99.5, sector: 'Financials' },
    { ticker: 'BBBY', name: 'Bed Bath & Beyond', year: 2023, reason: 'Severe cash burn, debt overhang & liquidation', finalReturn: -100.0, sector: 'Consumer' }
  ];

  const defaultHaircuts = [
    { scenario: 'Chapter 7 Liquidation', haircut: '-100.0%', description: 'Common equity wiped out, zero recovery rate.' },
    { scenario: 'Chapter 11 Reorganization', haircut: '-95.0% to -99.0%', description: 'Existing shares cancelled; debtholders receive new equity.' },
    { scenario: 'SEC Regulatory Trading Halt', haircut: '-85.0% Haircut', description: 'Immediate illiquidity discount applied on first OTC pink sheet open.' },
    { scenario: 'M&A Cash Buyout / Take-Under', haircut: '+5.0% to -40.0%', description: 'Acquisition price finalized, terminal cash distribution.' }
  ];

  const rawAudit = data?.survivorshipAudit;
  const audit = {
    headline: rawAudit?.headline || 'Survivorship Bias & Ghost Alpha Defense Matrix',
    naiveCagr: rawAudit?.naiveCagr || 18.4,
    realisticCagr: rawAudit?.realisticCagr || 12.6,
    ghostAlpha: rawAudit?.ghostAlpha || rawAudit?.ghostAlphaDrag || 5.8,
    universeInitial: rawAudit?.universeInitial || 3000,
    delistedCount: rawAudit?.delistedCount || 1140,
    survivalRate: rawAudit?.survivalRate || 62.0,
    delistedCompanies: (rawAudit?.delistedCompanies && Array.isArray(rawAudit.delistedCompanies)) 
      ? rawAudit.delistedCompanies 
      : defaultDelisted,
    haircutRules: (rawAudit?.haircutRules && Array.isArray(rawAudit.haircutRules)) 
      ? rawAudit.haircutRules 
      : defaultHaircuts
  };

  const scorecard = data?.survivorshipScorecard || {
    verdict: 'POINT-IN-TIME POINT VERIFIED',
    ghostAlphaHazard: 'EXTREME RISK MITIGATED',
    cagrOverstatement: '+5.8% / Year',
    recommendation: 'Incorporate active & defunct historical universes via point-in-time CRSP/Compustat survivorship datasets.'
  };

  return (
    <div className="fintech-chart-card" style={{
      background: 'var(--fintech-surface, #141923)',
      borderRadius: '14px',
      border: '1px solid var(--fintech-border, #222A3B)',
      padding: '20px',
      color: 'var(--text-primary, #F8FAFC)',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{
              fontSize: '11px',
              fontWeight: 700,
              letterSpacing: '0.05em',
              color: '#F04438',
              background: '#1AF04438',
              padding: '2px 8px',
              borderRadius: '4px',
              textTransform: 'uppercase'
            }}>
              Lesson 30 • Survival Simulation
            </span>
            <span style={{ fontSize: '12px', color: 'var(--text-tertiary, #64748B)' }}>CRSP & Compustat Defunct Modeling</span>
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)', margin: '6px 0 2px 0' }}>
            {audit.headline}
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', margin: 0 }}>
            Visualizing the fatal flaw of screening only currently active tickers and ignoring the Delisting Graveyard.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', background: 'var(--fintech-background, #0B0E14)', padding: '3px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          {[
            { id: 'alpha_gap', label: 'Ghost Alpha Gap' },
            { id: 'graveyard', label: 'Delisting Graveyard' },
            { id: 'audit', label: 'Terminal Haircuts' }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                background: activeTab === tab.id ? 'var(--fintech-surface-elevated, #1C2333)' : 'transparent',
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

      {/* Primary Visual Display */}
      {activeTab === 'alpha_gap' && (
        <div>
          {/* Metrics summary banner */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
            gap: '10px',
            marginBottom: '16px'
          }}>
            <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', border: '1px solid var(--fintech-border, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Flawed / Naive CAGR</div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#F04438', marginTop: '4px' }}>
                +{audit.naiveCagr}%
              </div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Only surviving winners</div>
            </div>
            <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', border: '1px solid var(--fintech-border, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Realistic (Point-in-Time)</div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#00D09C', marginTop: '4px' }}>
                +{audit.realisticCagr}%
              </div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Includes defunct & bankrupted</div>
            </div>
            <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', border: '1px solid var(--fintech-border, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Phantom / Ghost Alpha</div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#F59E0B', marginTop: '4px' }}>
                +{audit.ghostAlpha}%
              </div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>Annualized fake outperformance</div>
            </div>
            <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', border: '1px solid var(--fintech-border, #222A3B)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>Historical Delisting Rate</div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#1455FE', marginTop: '4px' }}>
                {((audit.delistedCount / audit.universeInitial) * 100).toFixed(1)}%
              </div>
              <div style={{ fontSize: '10px', color: 'var(--text-tertiary, #64748B)' }}>{audit.delistedCount} of {audit.universeInitial} tickers perished</div>
            </div>
          </div>

          {/* Comparative visual bar diagram */}
          <div style={{ background: 'var(--fintech-background, #0B0E14)', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)', padding: '16px', marginBottom: '14px' }}>
            <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '14px' }}>
              Historical Compounded Return Comparison (15-Year Horizon)
            </div>

            {/* Naive Bar */}
            <div style={{ marginBottom: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', marginBottom: '4px' }}>
                <span style={{ color: '#F04438', fontWeight: 600 }}>Naive Survivor-Only Backtest (Illusion)</span>
                <span style={{ color: '#F04438', fontWeight: 700 }}>+{audit.naiveCagr}% Annualized (+1,120% Cumulative)</span>
              </div>
              <div style={{ width: '100%', height: '22px', background: 'var(--fintech-surface-elevated, #1C2333)', borderRadius: '6px', overflow: 'hidden', position: 'relative' }}>
                <div style={{ width: '92%', height: '100%', background: 'linear-gradient(90deg, #F04438 0%, #F59E0B 100%)', borderRadius: '6px' }} />
                <span style={{ position: 'absolute', right: '10px', top: '3px', fontSize: '10px', fontWeight: 700, color: '#0B0E14' }}>
                  Dead stocks deleted from history
                </span>
              </div>
            </div>

            {/* Realistic Bar */}
            <div style={{ marginBottom: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', marginBottom: '4px' }}>
                <span style={{ color: '#00D09C', fontWeight: 600 }}>Point-in-Time Survivor-Free Backtest (Truth)</span>
                <span style={{ color: '#00D09C', fontWeight: 700 }}>+{audit.realisticCagr}% Annualized (+495% Cumulative)</span>
              </div>
              <div style={{ width: '100%', height: '22px', background: 'var(--fintech-surface-elevated, #1C2333)', borderRadius: '6px', overflow: 'hidden', position: 'relative' }}>
                <div style={{ width: '58%', height: '100%', background: '#00D09C', borderRadius: '6px' }} />
                <span style={{ position: 'absolute', right: '10px', top: '3px', fontSize: '10px', fontWeight: 700, color: 'var(--text-secondary, #94A3B8)' }}>
                  Includes Enron, Lehman, SVB total losses
                </span>
              </div>
            </div>

            {/* The Delta Gap */}
            <div style={{
              background: '#1AF59E0B',
              border: '1px dashed #F59E0B',
              borderRadius: '8px',
              padding: '10px 14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <div>
                <span style={{ color: '#F59E0B', fontWeight: 700, fontSize: '12px' }}>Phantom Alpha Destruction Gap: </span>
                <span style={{ color: 'var(--text-primary, #F8FAFC)', fontSize: '12px' }}>56% of paper backtest wealth vanishes when dead stocks are reintroduced.</span>
              </div>
              <span style={{ color: '#F59E0B', fontWeight: 700, fontSize: '14px', whiteSpace: 'nowrap' }}>Δ -5.8% / Yr</span>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'graveyard' && (
        <div style={{ background: 'var(--fintech-background, #0B0E14)', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            The Corporate Graveyard: Titans Once in S&P 500 & Russell 1000
          </div>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '11px', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--fintech-border, #222A3B)', color: 'var(--text-secondary, #94A3B8)' }}>
                  <th style={{ padding: '8px 10px' }}>Ticker</th>
                  <th style={{ padding: '8px 10px' }}>Company Name</th>
                  <th style={{ padding: '8px 10px' }}>Year</th>
                  <th style={{ padding: '8px 10px' }}>Sector</th>
                  <th style={{ padding: '8px 10px' }}>Terminal Return</th>
                  <th style={{ padding: '8px 10px' }}>Primary Cause of Death</th>
                </tr>
              </thead>
              <tbody>
                {audit.delistedCompanies.map((item, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid var(--fintech-border, #1C2333)', background: idx % 2 === 0 ? 'var(--fintech-surface, #141923)' : 'var(--fintech-background, #0B0E14)' }}>
                    <td style={{ padding: '8px 10px', fontWeight: 700, color: '#F04438' }}>{item.ticker}</td>
                    <td style={{ padding: '8px 10px', color: 'var(--text-primary, #F8FAFC)' }}>{item.name}</td>
                    <td style={{ padding: '8px 10px', color: 'var(--text-secondary, #94A3B8)' }}>{item.year}</td>
                    <td style={{ padding: '8px 10px', color: 'var(--text-tertiary, #64748B)' }}>{item.sector}</td>
                    <td style={{ padding: '8px 10px', fontWeight: 700, color: '#F04438' }}>{item.finalReturn}%</td>
                    <td style={{ padding: '8px 10px', color: 'var(--text-secondary, #94A3B8)' }}>{item.reason}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'audit' && (
        <div style={{ background: 'var(--fintech-background, #0B0E14)', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)', padding: '16px', marginBottom: '14px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary, #F8FAFC)', marginBottom: '10px' }}>
            Terminal Liquidation Haircut Matrix for Institutional Models
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '10px' }}>
            {audit.haircutRules.map((rule, idx) => (
              <div key={idx} style={{ background: 'var(--fintech-surface, #141923)', border: '1px solid var(--fintech-border, #222A3B)', borderRadius: '8px', padding: '12px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>{rule.scenario}</span>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: '#F04438', background: '#1AF04438', padding: '1px 6px', borderRadius: '4px' }}>
                    {rule.haircut}
                  </span>
                </div>
                <p style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', margin: 0 }}>
                  {rule.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Footer Institutional Scorecard */}
      <div style={{
        background: 'var(--fintech-background, #0B0E14)',
        borderRadius: '10px',
        border: '1px solid var(--fintech-border, #222A3B)',
        padding: '12px 16px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '10px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            background: '#00D09C',
            boxShadow: '0 0 8px #00D09C'
          }} />
          <div>
            <div style={{ fontSize: '10px', color: 'var(--text-secondary, #94A3B8)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Institutional Integrity Audit
            </div>
            <div style={{ fontSize: '12px', fontWeight: 700, color: '#00D09C' }}>
              {scorecard.verdict} • {scorecard.ghostAlphaHazard}
            </div>
          </div>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', maxWidth: '400px', textAlign: 'right' }}>
          {scorecard.recommendation}
        </div>
      </div>
    </div>
  );
};

export default SurvivorshipBiasChart;
