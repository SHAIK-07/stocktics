import React, { useState } from 'react';
import { ClipboardCheck, FileText, ShieldCheck, CheckCircle2, ChevronRight, Layers } from 'lucide-react';

export default function ResearchChecklistChart({ data, researchScorecard, ticker = 'STOCK' }) {
  const [activePillar, setActivePillar] = useState(0);

  const pillars = data?.pillars || [
    { pillar: '1. Business Mechanics & Moat', score: 92, weight: '30%', status: 'PASSED' },
    { pillar: '2. Financial Health & Solvency', score: 95, weight: '25%', status: 'PASSED' },
    { pillar: '3. Management Capital Allocation', score: 88, weight: '25%', status: 'PASSED' },
    { pillar: '4. Catalysts & Invalidation Risks', score: 90, weight: '20%', status: 'PASSED' }
  ];

  const checklistItems = data?.checklistItems || [
    { item: 'SEC Form 10-K Item 1 (Business Model & Unit Economics)', verified: true },
    { item: 'SEC Form 10-K Item 7 (MD&A Operational Growth Analysis)', verified: true },
    { item: '5-Year Gross Margin Stability (Pricing Power Proof)', verified: true },
    { item: 'FCF Conversion > 85% of Reported Net Income', verified: true },
    { item: 'Pre-Commitment Contract with 3 Invalidation Triggers Logged', verified: true }
  ];

  const score = data?.score || researchScorecard?.compositeScore || 92;

  return (
    <div className="chart-container-card" style={{ margin: 0, padding: '18px 20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ClipboardCheck size={16} color="var(--fintech-primary)" />
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              Company Research Checklist & Due Diligence Matrix
            </span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', marginTop: '2px' }}>
            Lesson 36 // Aviation-Grade 4-Pillar Due Diligence Framework
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span className="exchange-chip font-mono" style={{ background: 'var(--gain-green-bg)', color: 'var(--gain-green)', border: '1px solid var(--gain-green)' }}>
            CHECKLIST PASS
          </span>
          <span className="exchange-chip font-mono">{ticker}</span>
        </div>
      </div>

      {/* Top Banner KPI Card */}
      <div style={{ background: 'var(--fintech-surface-elevated)', border: '1px solid var(--border-hairline)', borderRadius: '10px', padding: '14px 16px', marginBottom: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{ width: '44px', height: '44px', borderRadius: '10px', background: 'var(--gain-green-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid var(--gain-green)' }}>
            <ShieldCheck size={22} color="var(--gain-green)" />
          </div>
          <div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', fontWeight: '700' }}>
              Due Diligence Composite Audit
            </div>
            <div style={{ fontSize: '18px', fontWeight: '900', color: 'var(--text-primary)' }} className="font-mono">
              {score}/100 Institutional Score
            </div>
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)' }}>Primary Source Status</div>
          <div style={{ fontSize: '12px', fontWeight: '700', color: 'var(--gain-green)', display: 'flex', alignItems: 'center', gap: '4px', justifyContent: 'flex-end' }}>
            <CheckCircle2 size={13} /> Audited Form 10-K Verified
          </div>
        </div>
      </div>

      {/* 4 Pillars Interactive Tabs */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px', marginBottom: '16px' }}>
        {pillars.map((p, idx) => (
          <div
            key={idx}
            onClick={() => setActivePillar(idx)}
            style={{
              padding: '10px 12px',
              borderRadius: '8px',
              background: activePillar === idx ? 'var(--bg-surface)' : 'var(--fintech-surface-elevated)',
              border: activePillar === idx ? '1.5px solid var(--fintech-primary)' : '1px solid var(--border-hairline)',
              cursor: 'pointer',
              transition: 'all 0.2s ease'
            }}
          >
            <div style={{ fontSize: '10.5px', color: activePillar === idx ? 'var(--fintech-primary)' : 'var(--text-tertiary)', fontWeight: '700' }}>
              PILLAR {idx + 1} ({p.weight})
            </div>
            <div style={{ fontSize: '11.5px', fontWeight: '800', color: 'var(--text-primary)', marginTop: '2px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {p.pillar.split('. ')[1] || p.pillar}
            </div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '6px' }}>
              <span className="font-mono" style={{ fontSize: '12px', fontWeight: '900', color: 'var(--gain-green)' }}>
                {p.score}%
              </span>
              <span style={{ fontSize: '9px', fontWeight: '800', padding: '1px 5px', borderRadius: '4px', background: 'var(--gain-green-bg)', color: 'var(--gain-green)' }}>
                {p.status}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Checklist Audit Table */}
      <div style={{ background: 'var(--fintech-surface-elevated)', border: '1px solid var(--border-hairline)', borderRadius: '8px', padding: '12px 14px' }}>
        <div style={{ fontSize: '11px', fontWeight: '800', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <FileText size={13} color="var(--fintech-blue)" />
          <span>Statutory 10-K Audit Checkpoints ({ticker})</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {checklistItems.map((chk, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '11.5px', padding: '6px 0', borderBottom: i < checklistItems.length - 1 ? '1px solid var(--border-subtle)' : 'none' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <CheckCircle2 size={14} color="var(--gain-green)" />
                <span style={{ color: 'var(--text-primary)' }}>{chk.item}</span>
              </div>
              <span className="font-mono" style={{ fontSize: '10px', fontWeight: '700', color: 'var(--gain-green)', background: 'var(--gain-green-bg)', padding: '2px 6px', borderRadius: '4px' }}>
                VERIFIED
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
