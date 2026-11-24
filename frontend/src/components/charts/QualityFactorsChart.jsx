import React, { useState } from 'react';
import { ShieldCheck, CheckCircle2, XCircle, Award, Activity, TrendingUp, Percent, Info, AlertCircle } from 'lucide-react';

export default function QualityFactorsChart({ data, qualityScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('piotroskiAudit'); // 'piotroskiAudit' | 'novyMarx' | 'drawdownDefense'

  const scorecard = qualityScorecard || {
    ticker: 'AAPL',
    score: 88.9,
    rating: 'FORTRESS QUALITY COMPOUNDER',
    fScore: 8,
    novyMarx: 48.0,
    roic: 64.8,
    isAccrualHealthy: true
  };

  const checklist = data?.piotroskiChecklist || [
    {
      category: 'Profitability (4 Pts)',
      items: [
        { name: 'Positive Return on Assets (ROA > 0)', passed: true },
        { name: 'Positive Operating Cash Flow (CFO > 0)', passed: true },
        { name: 'ROA Expansion YoY', passed: true },
        { name: 'Accruals Check (CFO > Net Income)', passed: true }
      ],
      subscore: 4,
      maxSubscore: 4
    },
    {
      category: 'Leverage & Dilution (3 Pts)',
      items: [
        { name: 'Long-Term Debt Prudence / Reduction', passed: true },
        { name: 'Current Ratio Liquidity Health', passed: true },
        { name: 'No Share Dilution (Buyback Discipline)', passed: true }
      ],
      subscore: 3,
      maxSubscore: 3
    },
    {
      category: 'Operating Efficiency (2 Pts)',
      items: [
        { name: 'Gross Margin Defense', passed: true },
        { name: 'Asset Turnover Productivity', passed: false }
      ],
      subscore: 1,
      maxSubscore: 2
    }
  ];

  const novyMarx = data?.novyMarx || {
    ratio: 48.0,
    benchmark: 30.0,
    status: 'Superior Moat Alpha'
  };

  const shield = data?.drawdownShield || {
    tier: 'Fortress Shield',
    historicalRelativeAlpha: '+12.4% Down-Market Outperformance'
  };

  const isElite = scorecard.fScore >= 7;

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
              Chapter 20 Quantitative Factor
            </span>
            <span style={{
              background: isElite ? 'rgba(0, 208, 156, 0.15)' : 'rgba(240, 68, 56, 0.15)',
              color: isElite ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {scorecard.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            {scorecard.ticker} Quality Factor & The Piotroski F-Score
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Joseph Piotroski 9-Point Financial Health Audit, Novy-Marx Gross Profitability, and Drawdown Defense
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
            { id: 'piotroskiAudit', label: '9-Point F-Score' },
            { id: 'novyMarx', label: 'Gross Profitability' },
            { id: 'drawdownDefense', label: 'Drawdown Shield' }
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
            Piotroski F-Score
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.fScore >= 7 ? 'var(--gain-green, #00D09C)' : 'var(--warning-amber, #F59E0B)', marginTop: '4px' }}>
            {scorecard.fScore} / 9 Points
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            {scorecard.fScore >= 8 ? 'Elite Fortress Quality' : scorecard.fScore >= 6 ? 'Solid Quality' : 'Fragile / High Risk'}
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
            Novy-Marx Profitability
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.novyMarx >= 30 ? 'var(--gain-green, #00D09C)' : 'var(--text-primary, #F8FAFC)', marginTop: '4px' }}>
            {scorecard.novyMarx.toFixed(1)}%
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Gross Profit / Total Assets
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
            Return on Capital (ROIC)
          </div>
          <div style={{ fontSize: '20px', fontWeight: 700, fontFamily: 'monospace', color: scorecard.roic >= 15 ? 'var(--fintech-blue, #1455FE)' : 'var(--text-primary, #F8FAFC)', marginTop: '4px' }}>
            {scorecard.roic.toFixed(1)}%
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Economic Moat Hurdle &gt; 12%
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
            Drawdown Shield
          </div>
          <div style={{
            fontSize: '20px',
            fontWeight: 700,
            fontFamily: 'monospace',
            color: shield.tier.includes('Fortress') ? 'var(--gain-green, #00D09C)' : 'var(--warning-amber, #F59E0B)',
            marginTop: '4px'
          }}>
            {shield.tier.toUpperCase()}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '2px' }}>
            Down-Market Defense Alpha
          </div>
        </div>
      </div>

      {/* Main Tab Content */}
      {activeTab === 'piotroskiAudit' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <span style={{ fontSize: '13px', fontWeight: 700 }}>
              Joseph Piotroski 9-Point Financial Health Checklist
            </span>
            <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)' }}>
              Total Passed: {scorecard.fScore} / 9
            </span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px' }}>
            {checklist.map(group => (
              <div key={group.category} style={{
                background: 'var(--fintech-surface-elevated, #1C2333)',
                borderRadius: '8px',
                padding: '12px 14px',
                border: '1px solid var(--fintech-border, #222A3B)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px', borderBottom: '1px solid var(--fintech-border, #222A3B)', paddingBottom: '6px' }}>
                  <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>
                    {group.category}
                  </span>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: group.subscore === group.maxSubscore ? 'var(--gain-green, #00D09C)' : 'var(--text-secondary, #94A3B8)' }}>
                    {group.subscore} / {group.maxSubscore}
                  </span>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {group.items.map(item => (
                    <div key={item.name} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '11px' }}>
                      <span style={{ color: 'var(--text-secondary, #94A3B8)', maxWidth: '80%' }}>
                        {item.name}
                      </span>
                      {item.passed ? (
                        <CheckCircle2 size={15} color="var(--gain-green, #00D09C)" />
                      ) : (
                        <XCircle size={15} color="var(--loss-red, #F04438)" />
                      )}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'novyMarx' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
            <Award size={18} color="var(--fintech-primary, #00D09C)" />
            <span style={{ fontSize: '14px', fontWeight: 700 }}>
              Novy-Marx Gross Profitability Factor: Pricing Power & Asset Productivity
            </span>
          </div>

          <p style={{ fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: 1.5, margin: '0 0 14px 0' }}>
            Robert Novy-Marx (2013) proved that <strong>Gross Profit / Total Assets</strong> possesses roughly the same power in explaining cross-sectional stock returns as Book-to-Market. Because gross profit sits cleanly at the top of the income statement, it isolates pure product pricing power and competitive moat before capital allocation or tax differences.
          </p>

          <div style={{
            background: 'var(--fintech-surface-elevated, #1C2333)',
            padding: '14px',
            borderRadius: '8px',
            border: '1px solid var(--fintech-border, #222A3B)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
              <span>Novy-Marx Gross Profitability Ratio</span>
              <span style={{ fontFamily: 'monospace', fontWeight: 700, color: novyMarx.ratio >= 30 ? 'var(--gain-green, #00D09C)' : 'var(--text-primary, #F8FAFC)' }}>
                {novyMarx.ratio.toFixed(1)}% (Benchmark: {novyMarx.benchmark.toFixed(1)}%)
              </span>
            </div>
            <div style={{ height: '14px', background: 'var(--fintech-background, #0B0E14)', borderRadius: '7px', overflow: 'hidden' }}>
              <div style={{
                height: '100%',
                width: `${Math.min(100, novyMarx.ratio * 1.5)}%`,
                background: 'var(--fintech-primary, #00D09C)',
                borderRadius: '7px'
              }} />
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', marginTop: '8px' }}>
              Status: <strong style={{ color: 'var(--gain-green, #00D09C)' }}>{novyMarx.status}</strong>. Top quintile of operating productivity.
            </div>
          </div>
        </div>
      )}

      {activeTab === 'drawdownDefense' && (
        <div style={{
          background: 'var(--fintech-background, #0B0E14)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '10px',
          padding: '16px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
            <ShieldCheck size={18} color="var(--gain-green, #00D09C)" />
            <span style={{ fontSize: '14px', fontWeight: 700 }}>
              The Quality Moat Shield: Drawdown Defense During Bear Markets
            </span>
          </div>

          <p style={{ fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: 1.5, margin: '0 0 14px 0' }}>
            Empirical factor backtests across four decades demonstrate that high-quality companies (Piotroski F-Score 8-9, high ROIC, low debt) suffer significantly shallower drawdowns during market crashes (2008 GFC, 2020 Covid, 2022 Tech contraction) and recover to new all-time highs faster than speculative peers.
          </p>

          <div style={{
            background: 'var(--fintech-surface-elevated, #1C2333)',
            padding: '12px 16px',
            borderRadius: '8px',
            borderLeft: '4px solid var(--fintech-primary, #00D09C)'
          }}>
            <div style={{ fontWeight: 700, fontSize: '13px', color: 'var(--text-primary, #F8FAFC)' }}>
              Historical Relative Alpha: {shield.historicalRelativeAlpha}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', marginTop: '4px' }}>
              Holding fortress-quality equities minimizes permanent capital impairment risk, allowing multi-decade compound interest to operate uninterrupted.
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
