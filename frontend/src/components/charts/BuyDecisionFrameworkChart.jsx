import React from 'react';
import { Target, CheckCircle2, AlertCircle, TrendingUp, DollarSign, Layers } from 'lucide-react';

export default function BuyDecisionFrameworkChart({ data, buyScorecard, ticker = 'STOCK' }) {
  const gates = data?.gates || [
    { gate: 'Quality Hurdle (ROIC >= 12%)', value: '45.2%', passed: true },
    { gate: 'Solvency Fortress (Net Debt/EBITDA <= 2.5x)', value: '0.8x', passed: true },
    { gate: 'Margin of Safety Discount (MoS >= 0%)', value: '+14.2%', passed: true },
    { gate: 'Expected 5Y IRR Hurdle (IRR >= 10%)', value: '13.8%', passed: true }
  ];

  const tranches = data?.tranches || [
    { tranche: 'Tranche 1 (35%)', trigger: 'Immediate Execution at Current Price', status: 'EXECUTABLE' },
    { tranche: 'Tranche 2 (35%)', trigger: 'Execute on -8% Volatility Dip (MoS +8%)', status: 'PENDING DIP' },
    { tranche: 'Tranche 3 (30%)', trigger: 'Execute on Next Confirmed 10-Q Earnings Print', status: 'RESERVED' }
  ];

  const canBuy = buyScorecard?.canBuy ?? true;
  const mosPct = data?.mosPct ?? 14.2;
  const fairValue = data?.fairValue ?? 245.0;
  const currentPrice = data?.currentPrice ?? 220.0;

  return (
    <div className="chart-container-card" style={{ margin: 0, padding: '18px 20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Target size={16} color="var(--fintech-primary)" />
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              How to Decide When to Buy: Multi-Pillar Framework
            </span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', marginTop: '2px' }}>
            Lesson 37 // 4 Gate Confluence & Staged Tranche Sizing
          </div>
        </div>
        <span className="exchange-chip font-mono" style={{ background: canBuy ? 'var(--gain-green-bg)' : 'var(--loss-red-bg)', color: canBuy ? 'var(--gain-green)' : 'var(--loss-red)', border: `1px solid ${canBuy ? 'var(--gain-green)' : 'var(--loss-red)'}` }}>
          {canBuy ? 'BUY CRITERIA SATISFIED' : 'WAIT FOR DIP'}
        </span>
      </div>

      {/* Valuation & MoS Comparison Bar */}
      <div style={{ background: 'var(--fintech-surface-elevated)', border: '1px solid var(--border-hairline)', borderRadius: '10px', padding: '14px 16px', marginBottom: '16px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
          <div>
            <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: '700' }}>
              Current Price vs Intrinsic Fair Value
            </div>
            <div style={{ fontSize: '16px', fontWeight: '900', color: 'var(--text-primary)', display: 'flex', alignItems: 'baseline', gap: '8px' }}>
              <span className="font-mono">${currentPrice.toFixed(2)}</span>
              <span style={{ fontSize: '12px', color: 'var(--text-tertiary)', fontWeight: '500' }}>vs DCF</span>
              <span className="font-mono" style={{ color: 'var(--fintech-primary)' }}>${fairValue.toFixed(2)}</span>
            </div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: '700' }}>
              Margin of Safety
            </div>
            <div className="font-mono" style={{ fontSize: '16px', fontWeight: '900', color: mosPct >= 0 ? 'var(--gain-green)' : 'var(--loss-red)' }}>
              {mosPct >= 0 ? `+${mosPct.toFixed(1)}%` : `${mosPct.toFixed(1)}%`}
            </div>
          </div>
        </div>

        {/* Visual Range Bar */}
        <div style={{ width: '100%', height: '8px', background: 'var(--border-hairline)', borderRadius: '4px', overflow: 'hidden', position: 'relative' }}>
          <div style={{ width: `${Math.min(100, Math.max(10, (currentPrice / fairValue) * 100))}%`, height: '100%', background: 'linear-gradient(90deg, var(--gain-green), var(--fintech-blue))', borderRadius: '4px' }} />
        </div>
      </div>

      {/* 4 Gates Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '10px', marginBottom: '16px' }}>
        {gates.map((g, idx) => (
          <div key={idx} style={{ background: 'var(--fintech-surface-elevated)', border: '1px solid var(--border-hairline)', borderRadius: '8px', padding: '10px 12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>{g.gate}</div>
              <div className="font-mono" style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', marginTop: '2px' }}>
                {g.value}
              </div>
            </div>
            {g.passed ? (
              <CheckCircle2 size={16} color="var(--gain-green)" />
            ) : (
              <AlertCircle size={16} color="var(--loss-red)" />
            )}
          </div>
        ))}
      </div>

      {/* Tranche Scaling Execution Cards */}
      <div style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-hairline)', borderRadius: '8px', padding: '12px 14px' }}>
        <div style={{ fontSize: '11px', fontWeight: '800', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <Layers size={13} color="var(--fintech-primary)" />
          <span>Disciplined 3-Tranche Capital Deployment</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px' }}>
          {tranches.map((t, idx) => (
            <div key={idx} style={{ background: 'var(--fintech-surface-elevated)', border: '1px solid var(--border-hairline)', borderRadius: '6px', padding: '8px 10px' }}>
              <div style={{ fontSize: '11px', fontWeight: '800', color: 'var(--fintech-primary)' }}>{t.tranche}</div>
              <div style={{ fontSize: '9.5px', color: 'var(--text-secondary)', margin: '3px 0' }}>{t.trigger}</div>
              <span className="font-mono" style={{ fontSize: '9px', fontWeight: '700', padding: '1px 5px', borderRadius: '3px', background: idx === 0 ? 'var(--gain-green-bg)' : 'var(--fintech-surface)', color: idx === 0 ? 'var(--gain-green)' : 'var(--text-tertiary)' }}>
                {t.status}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
