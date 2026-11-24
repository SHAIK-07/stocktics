import React, { useState } from 'react';
import {
  Search,
  Sparkles,
  TrendingUp,
  CheckCircle2,
  AlertCircle,
  Zap,
  Target,
  Layers
} from 'lucide-react';

export default function PatternDiscoveryChart({ data, patternScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('quadrants'); // 'quadrants' | 'falsification' | 'confluence'

  const scorecard = patternScorecard || {
    ticker: 'STOCK',
    score: 75.0,
    rating: 'HIGH-CONVICTION COMPOUNDER CONFLUENCE',
    confluencesPassed: 3,
    totalPatterns: 4
  };

  const chartData = data || {
    score: 75.0,
    rating: 'HIGH-CONVICTION COMPOUNDER CONFLUENCE',
    isCompounder: true,
    isCashBargain: true,
    isTurnaround: false,
    isShareCannibal: true,
    confluences: [
      { pattern: 'High Growth + High ROIC Compounder', status: 'CONFIRMED', passed: true, desc: 'ROIC >= 18% and Revenue CAGR >= 12%' },
      { pattern: 'Deep Value Cash Machine (High FCF)', status: 'CONFIRMED', passed: true, desc: 'FCF Yield >= 5% and EV/EBITDA <= 14x' },
      { pattern: 'Turnaround Margin Inflection', status: 'STABLE', passed: false, desc: 'Positive revenue acceleration and gross margin defense' },
      { pattern: 'Capital Allocation Confluence', status: 'CONFIRMED', passed: true, desc: 'Debt deleveraging paired with net share count reduction' }
    ]
  };

  const confluences = chartData.confluences || [
    { pattern: 'High Growth + High ROIC Compounder', status: 'CONFIRMED', passed: true, desc: 'ROIC >= 18% and Revenue CAGR >= 12%' },
    { pattern: 'Deep Value Cash Machine (High FCF)', status: 'CONFIRMED', passed: true, desc: 'FCF Yield >= 5% and EV/EBITDA <= 14x' },
    { pattern: 'Turnaround Margin Inflection', status: 'STABLE', passed: false, desc: 'Positive revenue acceleration and gross margin defense' },
    { pattern: 'Capital Allocation Confluence', status: 'CONFIRMED', passed: true, desc: 'Debt deleveraging paired with net share count reduction' }
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
              Chapter 27 Pattern Discovery
            </span>
            <span style={{
              background: scorecard.score >= 50 ? 'rgba(0, 208, 156, 0.15)' : 'rgba(240, 68, 56, 0.15)',
              color: scorecard.score >= 50 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {scorecard.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            {scorecard.ticker} Fundamental Anomaly & Pattern Confluence
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Compounder Confluence, Cash Flow Divergence, Turnaround Inflections, and Capital Allocation Screens
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
            { id: 'quadrants', label: 'Anomaly Quadrants' },
            { id: 'falsification', label: 'Falsification Gate' },
            { id: 'confluence', label: 'Confluence Audit' }
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
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Anomaly Score</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            {chartData.score} / 100
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Compounder</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: chartData.isCompounder ? 'var(--gain-green, #00D09C)' : 'var(--text-tertiary, #64748B)' }}>
            {chartData.isCompounder ? 'CONFIRMED' : 'NEUTRAL'}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Cash Machine</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: chartData.isCashBargain ? 'var(--gain-green, #00D09C)' : 'var(--text-tertiary, #64748B)' }}>
            {chartData.isCashBargain ? 'CONFIRMED' : 'MODERATE'}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Share Cannibal</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: chartData.isShareCannibal ? 'var(--gain-green, #00D09C)' : 'var(--text-tertiary, #64748B)' }}>
            {chartData.isShareCannibal ? 'ACTIVE' : 'STABLE'}
          </div>
        </div>
      </div>

      {/* Tab 1: Anomaly Quadrants */}
      {activeTab === 'quadrants' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '10px', marginBottom: '14px' }}>
          {confluences.map((c, idx) => (
            <div key={idx} style={{
              background: 'var(--fintech-surface-elevated, #1C2333)',
              padding: '14px',
              borderRadius: '8px',
              border: c.passed ? '1px solid rgba(0, 208, 156, 0.4)' : '1px solid var(--fintech-border, #222A3B)',
              display: 'flex',
              flexDirection: 'column',
              gap: '6px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-primary)' }}>{c.pattern}</span>
                <span style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  color: c.passed ? '#00D09C' : '#94A3B8',
                  background: c.passed ? 'rgba(0, 208, 156, 0.15)' : 'rgba(255, 255, 255, 0.05)',
                  padding: '2px 6px',
                  borderRadius: '4px'
                }}>
                  {c.status}
                </span>
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.4' }}>
                {c.desc}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Tab 2: Falsification Gate */}
      {activeTab === 'falsification' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <Zap size={18} color="#00D09C" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Scientific Hypothesis Falsification Standard</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              A quantitative researcher seeks falsification, not confirmation. Before entering any fundamental confluence trade, document the exact operational threshold (e.g. ROIC dropping below 15% or Gross Margin contracting by 100 bps) that would prove your thesis wrong and trigger immediate capital liquidation.
            </p>
          </div>
        </div>
      )}

      {/* Tab 3: Confluence Audit */}
      {activeTab === 'confluence' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ fontSize: '14px', fontWeight: 700, marginBottom: '8px' }}>Multi-Anomaly Confluence Strength</div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              Confirmed <strong style={{ color: '#00D09C' }}>{scorecard.confluencesPassed} of 4</strong> fundamental anomaly patterns. Multi-pattern confluence significantly increases the statistical probability of forward market outperformance compared to isolated single-factor triggers.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
