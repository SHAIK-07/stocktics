import React from 'react';
import { AlertOctagon, CheckCircle2, ShieldAlert, ArrowRight, Ban, RefreshCw } from 'lucide-react';

export default function SellExitTriggersChart({ data, sellScorecard, ticker = 'STOCK' }) {
  const triggers = data?.triggers || [
    { name: 'Trigger 1: Thesis Broken', condition: 'Gross Margin Collapse > 400bps or Negative FCF', status: 'INACTIVE (SAFE)', fired: false },
    { name: 'Trigger 2: Extreme Multiple Euphoria', condition: 'P/E > 75x with Low Growth or P/FCF > 3x Median', status: 'INACTIVE (SAFE)', fired: false },
    { name: 'Trigger 3: Superior Opportunity Cost', condition: 'Alternative Asset Offers > 8.0% IRR Spread', status: 'INACTIVE (SAFE)', fired: false },
    { name: 'Trigger 4: Concentration Limit', condition: 'Position Weight Exceeds 25% Portfolio Ceiling', status: 'INACTIVE (SAFE)', fired: false }
  ];

  const sellVerdict = data?.sellVerdict || 'NO EXIT REQUIRED — HOLD POSITION';
  const triggersFiredCount = data?.triggersFiredCount || 0;

  return (
    <div className="chart-container-card" style={{ margin: 0, padding: '18px 20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <AlertOctagon size={16} color={triggersFiredCount === 0 ? 'var(--gain-green)' : 'var(--loss-red)'} />
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              How to Decide When to Sell: Rule-Based Exit Matrix
            </span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', marginTop: '2px' }}>
            Lesson 39 // 4 Objective Non-Emotional Exit Triggers
          </div>
        </div>
        <span className="exchange-chip font-mono" style={{ background: triggersFiredCount === 0 ? 'var(--gain-green-bg)' : 'var(--loss-red-bg)', color: triggersFiredCount === 0 ? 'var(--gain-green)' : 'var(--loss-red)', border: `1px solid ${triggersFiredCount === 0 ? 'var(--gain-green)' : 'var(--loss-red)'}` }}>
          {triggersFiredCount === 0 ? 'ALL TRIGGERS SAFE' : 'ACTION REQUIRED'}
        </span>
      </div>

      {/* Decision Status Card */}
      <div style={{ background: 'var(--fintech-surface-elevated)', border: '1px solid var(--border-hairline)', borderRadius: '10px', padding: '14px 16px', marginBottom: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: '700' }}>
            Systematic Exit Recommendation
          </div>
          <div style={{ fontSize: '15px', fontWeight: '900', color: triggersFiredCount === 0 ? 'var(--gain-green)' : 'var(--loss-red)' }} className="font-mono">
            {sellVerdict}
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: '700' }}>
            Active Triggers Fired
          </div>
          <div className="font-mono" style={{ fontSize: '16px', fontWeight: '900', color: triggersFiredCount === 0 ? 'var(--gain-green)' : 'var(--loss-red)' }}>
            {triggersFiredCount} / 4
          </div>
        </div>
      </div>

      {/* 4 Exit Trigger Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '10px', marginBottom: '16px' }}>
        {triggers.map((t, idx) => (
          <div
            key={idx}
            style={{
              background: 'var(--fintech-surface-elevated)',
              border: t.fired ? '1.5px solid var(--loss-red)' : '1px solid var(--border-hairline)',
              borderRadius: '8px',
              padding: '12px 14px'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span style={{ fontSize: '11.5px', fontWeight: '800', color: t.fired ? 'var(--loss-red)' : 'var(--text-primary)' }}>
                {t.name}
              </span>
              {t.fired ? (
                <ShieldAlert size={15} color="var(--loss-red)" />
              ) : (
                <CheckCircle2 size={15} color="var(--gain-green)" />
              )}
            </div>
            <div style={{ fontSize: '10px', color: 'var(--text-secondary)', marginTop: '4px' }}>
              {t.condition}
            </div>
            <div style={{ marginTop: '8px' }}>
              <span className="font-mono" style={{ fontSize: '9.5px', fontWeight: '700', padding: '2px 6px', borderRadius: '4px', background: t.fired ? 'var(--loss-red-bg)' : 'var(--gain-green-bg)', color: t.fired ? 'var(--loss-red)' : 'var(--gain-green)' }}>
                {t.status}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Anti-Arbitrary Stop Loss Rule */}
      <div style={{ background: 'var(--bg-surface)', borderLeft: '3px solid var(--loss-red)', borderRadius: '0 8px 8px 0', padding: '10px 14px' }}>
        <div style={{ fontSize: '10.5px', fontWeight: '800', color: 'var(--loss-red)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <Ban size={13} color="var(--loss-red)" />
          <span>The Anti-Arbitrary Stop Mandate</span>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>
          Never exit an investment purely because the stock price dropped 10%. Price moves are market auctions; exit only if the fundamental economic moat is broken or valuation is disconnected from reality.
        </div>
      </div>
    </div>
  );
}
