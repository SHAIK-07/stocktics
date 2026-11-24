import React from 'react';
import { Shield, Clock, TrendingUp, CheckCircle2, AlertTriangle, ArrowUpRight } from 'lucide-react';

export default function HoldThesisTrackingChart({ data, holdScorecard, ticker = 'STOCK' }) {
  const quarters = data?.quarters || [
    { quarter: 'Q1-TTM', revenueGrowth: '+11.4%', grossMargin: '45.8%', fcfPerShare: '$1.45', verdict: 'INTACT' },
    { quarter: 'Q2-TTM', revenueGrowth: '+12.1%', grossMargin: '46.2%', fcfPerShare: '$1.52', verdict: 'INTACT' },
    { quarter: 'Q3-TTM', revenueGrowth: '+10.8%', grossMargin: '46.0%', fcfPerShare: '$1.58', verdict: 'INTACT' },
    { quarter: 'Q4-TTM', revenueGrowth: '+13.5%', grossMargin: '46.5%', fcfPerShare: '$1.68', verdict: 'EXPANDING' }
  ];

  const roicPersistence = data?.roicPersistence || '45.2% (vs WACC 8.5%)';
  const taxAdvantage = data?.taxAdvantage || '100% Tax-Deferred Compounding Retained';

  return (
    <div className="chart-container-card" style={{ margin: 0, padding: '18px 20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Shield size={16} color="var(--fintech-primary)" />
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              How to Decide When to Hold: Thesis Scorecard
            </span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', marginTop: '2px' }}>
            Lesson 38 // 10-Q Health Audit & Charlie Munger Inactivity Law
          </div>
        </div>
        <span className="exchange-chip font-mono" style={{ background: 'var(--gain-green-bg)', color: 'var(--gain-green)', border: '1px solid var(--gain-green)' }}>
          HOLD FIRM (INTACT)
        </span>
      </div>

      {/* Top Banner KPI Card */}
      <div style={{ background: 'var(--fintech-surface-elevated)', border: '1px solid var(--border-hairline)', borderRadius: '10px', padding: '14px 16px', marginBottom: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: '700' }}>
            ROIC vs WACC Persistence
          </div>
          <div style={{ fontSize: '15px', fontWeight: '900', color: 'var(--text-primary)' }} className="font-mono">
            {roicPersistence}
          </div>
          <div style={{ fontSize: '10.5px', color: 'var(--gain-green)', marginTop: '2px' }}>
            Economic Value Added (EVA) spread is compounding steadily
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: '700' }}>
            Tax Advantage
          </div>
          <div style={{ fontSize: '11.5px', fontWeight: '800', color: 'var(--fintech-blue)' }}>
            {taxAdvantage}
          </div>
        </div>
      </div>

      {/* Quarterly KPI Table */}
      <div style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-hairline)', borderRadius: '8px', overflow: 'hidden', marginBottom: '14px' }}>
        <div style={{ padding: '10px 14px', borderBottom: '1px solid var(--border-hairline)', fontSize: '11px', fontWeight: '800', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>
          Rolling 4-Quarter 10-Q Fundamental Progression
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '11.5px' }}>
          <thead>
            <tr style={{ background: 'var(--fintech-surface-elevated)', color: 'var(--text-tertiary)', borderBottom: '1px solid var(--border-hairline)' }}>
              <th style={{ padding: '8px 12px', textAlign: 'left', fontWeight: '700' }}>Quarter</th>
              <th style={{ padding: '8px 12px', textAlign: 'right', fontWeight: '700' }}>Rev Growth</th>
              <th style={{ padding: '8px 12px', textAlign: 'right', fontWeight: '700' }}>Gross Margin</th>
              <th style={{ padding: '8px 12px', textAlign: 'right', fontWeight: '700' }}>FCF / Share</th>
              <th style={{ padding: '8px 12px', textAlign: 'center', fontWeight: '700' }}>Status</th>
            </tr>
          </thead>
          <tbody>
            {quarters.map((q, idx) => (
              <tr key={idx} style={{ borderBottom: idx < quarters.length - 1 ? '1px solid var(--border-subtle)' : 'none' }}>
                <td style={{ padding: '9px 12px', fontWeight: '700', color: 'var(--text-primary)' }} className="font-mono">{q.quarter}</td>
                <td style={{ padding: '9px 12px', textAlign: 'right', color: 'var(--gain-green)' }} className="font-mono">{q.revenueGrowth}</td>
                <td style={{ padding: '9px 12px', textAlign: 'right', color: 'var(--text-primary)' }} className="font-mono">{q.grossMargin}</td>
                <td style={{ padding: '9px 12px', textAlign: 'right', color: 'var(--fintech-primary)', fontWeight: '800' }} className="font-mono">{q.fcfPerShare}</td>
                <td style={{ padding: '9px 12px', textAlign: 'center' }}>
                  <span className="font-mono" style={{ fontSize: '9px', fontWeight: '800', padding: '2px 6px', borderRadius: '4px', background: 'var(--gain-green-bg)', color: 'var(--gain-green)' }}>
                    {q.verdict}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Munger Law Quote Card */}
      <div style={{ background: 'var(--fintech-surface-elevated)', borderLeft: '3px solid var(--fintech-primary)', borderRadius: '0 8px 8px 0', padding: '10px 14px' }}>
        <div style={{ fontSize: '10.5px', fontWeight: '800', color: 'var(--fintech-primary)', textTransform: 'uppercase' }}>
          Charlie Munger Inactivity Principle
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-secondary)', fontStyle: 'italic', marginTop: '2px' }}>
          "The first rule of compounding: Never interrupt it unnecessarily. The big money is not in the buying or the selling, but in the waiting."
        </div>
      </div>
    </div>
  );
}
