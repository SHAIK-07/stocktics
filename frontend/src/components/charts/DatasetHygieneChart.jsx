import React, { useState } from 'react';
import {
  Database,
  Calendar,
  ShieldCheck,
  Filter,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Layers
} from 'lucide-react';

export default function DatasetHygieneChart({ data, datasetScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('audit'); // 'audit' | 'timeline' | 'winsorization'

  const hygiene = data || {
    hygieneScore: 94.0,
    rating: 'INSTITUTIONAL GRADE (PIT & SURVIVORSHIP FREE)',
    pitScore: 92.0,
    winsorizeBound: 96.5,
    survivorshipShield: 94.0,
    auditPillars: [
      { pillar: 'Point-in-Time Timestamps (45D Lag)', score: 92, status: 'COMPLIANT' },
      { pillar: 'Zero Look-Ahead Restatements', score: 100, status: 'VERIFIED' },
      { pillar: 'Winsorization (1st/99th Bounds)', score: 97, status: 'BOUNDED' },
      { pillar: 'Survivorship Bias Immunity', score: 94, status: 'SHIELDED' }
    ]
  };

  const pillars = hygiene.auditPillars || [
    { pillar: 'Point-in-Time Timestamps', score: 92, status: 'COMPLIANT' },
    { pillar: 'Zero Look-Ahead Restatements', score: 100, status: 'VERIFIED' },
    { pillar: 'Winsorization (1st/99th)', score: 97, status: 'BOUNDED' },
    { pillar: 'Survivorship Bias Immunity', score: 94, status: 'SHIELDED' }
  ];

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
              background: 'rgba(20, 85, 254, 0.15)',
              color: 'var(--fintech-blue, #1455FE)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700,
              textTransform: 'uppercase'
            }}>
              Chapter 23 Data Engineering
            </span>
            <span style={{
              background: 'rgba(0, 208, 156, 0.15)',
              color: 'var(--fintech-primary, #00D09C)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {hygiene.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            Stock Dataset Architecture & Point-In-Time (PIT) Hygiene
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Look-Ahead Bias Elimination, 45-Day SEC Filing Lags, 1st/99th Winsorization, and Survivorship Protection
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
            { id: 'audit', label: 'Hygiene Pillars' },
            { id: 'timeline', label: 'PIT 45D Lag' },
            { id: 'winsorization', label: 'Winsorization' }
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
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* KPI Tiles */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
        gap: '10px',
        marginBottom: '18px'
      }}>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Hygiene Score</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            {hygiene.hygieneScore} / 100
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Look-Ahead Bias</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>
            ZERO LEAK
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Winsorization</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', fontFamily: 'monospace' }}>
            1st / 99th %ile
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Survivorship Bias</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>
            SHIELDED
          </div>
        </div>
      </div>

      {/* Tab 1: Hygiene Audit Pillars */}
      {activeTab === 'audit' && (
        <div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '10px', marginBottom: '14px' }}>
            {pillars.map((p, idx) => (
              <div key={idx} style={{
                background: 'var(--fintech-surface-elevated, #1C2333)',
                padding: '14px',
                borderRadius: '8px',
                border: '1px solid var(--fintech-border, #222A3B)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}>
                <div>
                  <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '4px' }}>{p.pillar}</div>
                  <div style={{ fontSize: '11px', color: 'var(--fintech-primary, #00D09C)', fontWeight: 700 }}>
                    {p.status}
                  </div>
                </div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: '#00D09C', fontFamily: 'monospace' }}>
                  {p.score}%
                </div>
              </div>
            ))}
          </div>
          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px' }}>
            Institutional backtests fail when future data leaks into historical decision dates. Strict Point-in-Time (PIT) data stamping ensures real-world quantitative signal replicability.
          </div>
        </div>
      )}

      {/* Tab 2: PIT 45D Lag */}
      {activeTab === 'timeline' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' }}>
              <Clock size={18} color="#1455FE" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Point-in-Time (PIT) SEC 10-Q Disclosure Timeline</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--fintech-border, #222A3B)' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Fiscal Quarter Closes (T=0)</span>
              <span style={{ fontSize: '12px', color: '#F04438', fontWeight: 600 }}>Unreported Financials (Inaccessible to Market)</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>SEC 10-Q Filing Public Dissemination (T+45D)</span>
              <span style={{ fontSize: '12px', color: '#00D09C', fontWeight: 600 }}>Signal Execution Permitted (Zero Look-Ahead Bias)</span>
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Winsorization */}
      {activeTab === 'winsorization' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <Filter size={18} color="#00D09C" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Cross-Sectional Winsorization (1st & 99th Percentiles)</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Extreme corporate one-offs (e.g. a one-time tax write-down creating a P/E of 2,500x) can distort factor regressions. Winsorization clamps extreme tails to the 1st and 99th percentiles, preserving statistical robustness without discarding data rows.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
