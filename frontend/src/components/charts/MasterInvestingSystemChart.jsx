import React, { useState } from 'react';
import { Award, CheckCircle2, ChevronRight, Layers, Workflow, ShieldCheck, Sparkles } from 'lucide-react';

export default function MasterInvestingSystemChart({ data, masterSystemScorecard, ticker = 'STOCK' }) {
  const [selectedStage, setSelectedStage] = useState(0);

  const stages = data?.stages || [
    { stage: 'Stage 1: Foundations', coverage: 'Scale, Solvency, Cash Flow (Ch 1-7)', status: 'COMPLETE' },
    { stage: 'Stage 2: Moat & Allocation', coverage: 'ROIC, Moats, Allocation, Sectors (Ch 8-11)', status: 'COMPLETE' },
    { stage: 'Stage 3: Intrinsic Valuation', coverage: 'P/E, EV, FCF, DCF, Relative (Ch 12-16)', status: 'COMPLETE' },
    { stage: 'Stage 4: Factor Models', coverage: 'Style, Mom, Val, Qual, Growth, Multi (Ch 17-22)', status: 'COMPLETE' },
    { stage: 'Stage 5: Data Science & PIT', coverage: 'PIT Data, EDA, IC, Ranking, Patterns (Ch 23-27)', status: 'COMPLETE' },
    { stage: 'Stage 6: Backtesting Engine', coverage: 'Architecture, Look-Ahead, Survivorship, Overfit, Performance (Ch 28-32)', status: 'COMPLETE' },
    { stage: 'Stage 7: Portfolio & Sizing', coverage: 'Diversification, Sizing, Pipeline Engine (Ch 33-35)', status: 'COMPLETE' },
    { stage: 'Stage 8: Real Investing Process', coverage: 'Research, Buy, Hold, Sell, Master Synthesis (Ch 36-40)', status: 'COMPLETE' }
  ];

  const systemScore = data?.systemScore || 98;
  const pipelineFlow = data?.pipelineFlow || 'UNIVERSE -> DATA -> FUNDAMENTALS -> VALUATION -> SIZING -> BUY -> HOLD -> SELL -> AUDIT';

  return (
    <div className="chart-container-card" style={{ margin: 0, padding: '18px 20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Award size={18} color="var(--fintech-primary)" />
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              The Complete US Stock Master Investing Operating System
            </span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', marginTop: '2px' }}>
            Lesson 40 // Grand Finale: 40-Chapter Closed-Loop System Synthesis
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span className="exchange-chip font-mono" style={{ background: 'var(--gain-green-bg)', color: 'var(--gain-green)', border: '1px solid var(--gain-green)' }}>
            CURRICULUM 100% COMPLETE
          </span>
          <span className="exchange-chip font-mono">{ticker}</span>
        </div>
      </div>

      {/* Graduation Creed Banner */}
      <div style={{ background: 'linear-gradient(135deg, var(--fintech-surface-elevated), var(--bg-surface))', border: '1px solid var(--border-hairline)', borderRadius: '10px', padding: '14px 16px', marginBottom: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{ width: '46px', height: '46px', borderRadius: '10px', background: 'var(--gain-green-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1.5px solid var(--gain-green)' }}>
            <Sparkles size={24} color="var(--gain-green)" />
          </div>
          <div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', fontWeight: '700' }}>
              Master System Operating Integrity
            </div>
            <div style={{ fontSize: '18px', fontWeight: '900', color: 'var(--text-primary)' }} className="font-mono">
              {systemScore}/100 Institutional Grade
            </div>
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '10.5px', color: 'var(--text-tertiary)' }}>Analytical Coverage</div>
          <div style={{ fontSize: '12.5px', fontWeight: '800', color: 'var(--fintech-primary)' }} className="font-mono">
            40 Chapters // 186+ Audited Metrics
          </div>
        </div>
      </div>

      {/* Pipeline Flow Bar */}
      <div style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-hairline)', borderRadius: '8px', padding: '10px 14px', marginBottom: '16px' }}>
        <div style={{ fontSize: '10px', fontWeight: '800', color: 'var(--text-tertiary)', textTransform: 'uppercase', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <Workflow size={12} color="var(--fintech-blue)" />
          <span>Closed-Loop Quantitative Fundamental Factory</span>
        </div>
        <div className="font-mono" style={{ fontSize: '10.5px', color: 'var(--text-primary)', fontWeight: '700', letterSpacing: '0.3px', overflowX: 'auto', whiteSpace: 'nowrap' }}>
          {pipelineFlow}
        </div>
      </div>

      {/* 8 Curriculum Stages Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px', marginBottom: '16px' }}>
        {stages.map((s, idx) => (
          <div
            key={idx}
            onClick={() => setSelectedStage(idx)}
            style={{
              padding: '9px 10px',
              borderRadius: '6px',
              background: selectedStage === idx ? 'var(--bg-surface)' : 'var(--fintech-surface-elevated)',
              border: selectedStage === idx ? '1.5px solid var(--fintech-primary)' : '1px solid var(--border-hairline)',
              cursor: 'pointer',
              transition: 'all 0.15s ease'
            }}
          >
            <div style={{ fontSize: '10px', fontWeight: '800', color: selectedStage === idx ? 'var(--fintech-primary)' : 'var(--text-secondary)' }}>
              {s.stage}
            </div>
            <div style={{ fontSize: '9px', color: 'var(--text-tertiary)', marginTop: '2px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {s.coverage}
            </div>
            <div style={{ marginTop: '5px' }}>
              <span className="font-mono" style={{ fontSize: '8.5px', fontWeight: '800', padding: '1px 5px', borderRadius: '3px', background: 'var(--gain-green-bg)', color: 'var(--gain-green)' }}>
                {s.status}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Bottom Creed */}
      <div style={{ background: 'var(--fintech-surface-elevated)', borderLeft: '3px solid var(--fintech-primary)', borderRadius: '0 8px 8px 0', padding: '10px 14px' }}>
        <div style={{ fontSize: '10.5px', fontWeight: '800', color: 'var(--fintech-primary)', textTransform: 'uppercase' }}>
          The Stocktics Graduation Creed
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>
          {'"DATA → UNDERSTANDING → HYPOTHESIS → TEST → EVIDENCE → DECISION → AUDIT. You are now an evidence-based capital allocator operating with institutional precision."'}
        </div>
      </div>
    </div>
  );
}
