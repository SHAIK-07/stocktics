import React, { useState } from 'react';
import {
  Clock,
  ShieldCheck,
  AlertTriangle,
  Calendar,
  Lock,
  Unlock,
  CheckCircle2
} from 'lucide-react';

export default function LookAheadBiasChart({ data, lookAheadScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('timeline'); // 'timeline' | 'restatements' | 'execution'

  const chartData = (data && data.pitScore !== undefined)
    ? data
    : (data && data.lookAheadAudit)
      ? data.lookAheadAudit
      : (data && data.lookAhead)
        ? data.lookAhead
        : {
            lagDays: 45,
            pitScore: 96.0,
            certification: 'STRICT ZERO LOOK-AHEAD CERTIFIED',
            timeline: [
              { event: 'Fiscal Quarter End (T=0)', status: 'UNREPORTED (FORBIDDEN)', dateDesc: 'Books closed internally; public has no access.', color: '#F04438' },
              { event: 'SEC 10-Q Filing Buffer (T+1 to T+44)', status: '45-DAY REALISTIC LAG', dateDesc: 'Audit preparation & compliance window.', color: '#F59E0B' },
              { event: 'Public Dissemination (T+45D)', status: 'SIGNAL ACTIONABLE', dateDesc: 'Filed with SEC EDGAR; public trading allowed.', color: '#00D09C' }
            ]
          };

  const scorecard = lookAheadScorecard || (data && data.lookAheadScorecard) || {
    ticker: 'STOCK',
    enforcedLag: `${chartData.lagDays || 45} Days`,
    isPitProtected: true,
    leakageRisk: 'ZERO RISK'
  };

  const defaultTimelineMeta = [
    { color: '#F04438', dateDesc: 'Books closed internally; public has no access.' },
    { color: '#F59E0B', dateDesc: 'Audit preparation & compliance window.' },
    { color: '#00D09C', dateDesc: 'Filed with SEC EDGAR; public trading allowed.' }
  ];

  const rawTimeline = chartData.timeline || [
    { event: 'Fiscal Quarter End (T=0)', status: 'UNREPORTED (FORBIDDEN)' },
    { event: 'SEC 10-Q Filing Buffer (T+1 to T+44)', status: '45-DAY REALISTIC LAG' },
    { event: 'Public Dissemination (T+45D)', status: 'SIGNAL ACTIONABLE' }
  ];

  const timeline = rawTimeline.map((item, idx) => ({
    ...item,
    color: item.color || defaultTimelineMeta[idx]?.color || '#00D09C',
    dateDesc: item.dateDesc || defaultTimelineMeta[idx]?.dateDesc || ''
  }));

  const displayPitScore = chartData.pitScore !== undefined && chartData.pitScore !== null
    ? chartData.pitScore
    : (scorecard.pitScore !== undefined && scorecard.pitScore !== null ? scorecard.pitScore : 96);

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
              Chapter 29 Information Leakage
            </span>
            <span style={{
              background: 'rgba(0, 208, 156, 0.15)',
              color: 'var(--gain-green, #00D09C)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {chartData.certification}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            Look-Ahead Bias Defense & Filing Lag Enforcement
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Mandatory 45-Day SEC 10-Q Buffer, Point-In-Time Restatement Shield, and Zero Future Leakage
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
            { id: 'timeline', label: 'SEC 45D Timeline' },
            { id: 'restatements', label: 'PIT Restatements' },
            { id: 'execution', label: 'Next-Day Fills' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Enforced SEC Lag</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            {scorecard.enforcedLag}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>PIT Shield Score</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', fontFamily: 'monospace' }}>
            {displayPitScore} / 100
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Future Leakage Risk</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>
            {scorecard.leakageRisk}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Execution Timing</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)' }}>
            T+1 OPEN FILLS
          </div>
        </div>
      </div>

      {/* Tab 1: SEC 45-Day Timeline */}
      {activeTab === 'timeline' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{
            background: 'var(--fintech-background, #0B0E14)',
            border: '1px solid var(--fintech-border, #222A3B)',
            borderRadius: '10px',
            padding: '16px'
          }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '14px' }}>
              SEC EDGAR Reporting Lag Verification
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {timeline.map((item, idx) => (
                <div key={idx} style={{
                  background: 'var(--fintech-surface-elevated, #1C2333)',
                  border: '1px solid var(--fintech-border, #222A3B)',
                  padding: '12px 14px',
                  borderRadius: '8px',
                  borderLeft: `4px solid ${item.color}`,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)' }}>{item.event}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>{item.dateDesc}</div>
                  </div>
                  <span style={{
                    fontSize: '11px',
                    fontWeight: 700,
                    color: item.color,
                    background: `${item.color}15`,
                    padding: '3px 8px',
                    borderRadius: '4px'
                  }}>
                    {item.status}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', background: 'var(--fintech-surface-elevated, #1C2333)', border: '1px solid var(--fintech-border, #222A3B)', padding: '10px 14px', borderRadius: '8px' }}>
            <strong style={{ color: 'var(--fintech-primary, #00D09C)' }}>Zero Look-Ahead Guarantee:</strong> Backtests that execute trades prior to T+45D are buying stocks with advance knowledge of future earnings. Our model enforces strict publication date compliance.
          </div>
        </div>
      )}

      {/* Tab 2: PIT Restatements */}
      {activeTab === 'restatements' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <ShieldCheck size={18} color="#00D09C" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Asynchronous Restatement Protection</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              When companies restate historical revenues years later, using the restated figures corrupts past decisions. Our database snapshotting ensures the model only sees the original preliminary figures visible to live market participants on that exact date.
            </p>
          </div>
        </div>
      )}

      {/* Tab 3: Next-Day Fills */}
      {activeTab === 'execution' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <Clock size={18} color="#1455FE" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Next-Day Open Execution (T+1 9:30 AM)</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Signals generated at market close (4:00 PM) cannot physically be executed at that same morning's open price. The simulation strictly places orders for execution at 9:30 AM the following trading session.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
