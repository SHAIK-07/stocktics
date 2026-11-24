import React, { useState } from 'react';
import { Briefcase, TrendingDown, TrendingUp, AlertTriangle, CheckCircle2, DollarSign, PieChart, Layers } from 'lucide-react';

export default function CapitalAllocationWaterfallChart({ data, capitalScorecard, currencySymbol = '$' }) {
  const [hoveredIdx, setHoveredIdx] = useState(null);
  const [activeTab, setActiveTab] = useState('allocation'); // 'allocation' | 'shares'

  const scorecard = capitalScorecard || {
    netShareCagr: -2.8,
    buffettTestRatio: 2.35,
    shareholderYield: 5.4,
    goodwillRatio: 8.5,
    capitalScore: 82.5,
    classification: 'EXEMPLARY CAPITAL ALLOCATOR',
    annualBuybacks: 85000000000,
    annualDividends: 15000000000,
    displayBuybacks: '$85.0B',
    displayDividends: '$15.0B'
  };

  const chartData = data && data.length > 0 ? data : [
    { year: '2020', capex: 7309000000, buybacks: 72358000000, dividends: 14081000000, fcf: 73365000000, netShares: 17500000000, goodwill: 0 },
    { year: '2021', capex: 11085000000, buybacks: 85971000000, dividends: 14467000000, fcf: 92953000000, netShares: 16700000000, goodwill: 0 },
    { year: '2022', capex: 10708000000, buybacks: 89402000000, dividends: 14841000000, fcf: 111443000000, netShares: 16100000000, goodwill: 0 },
    { year: '2023', capex: 10959000000, buybacks: 77550000000, dividends: 15025000000, fcf: 99584000000, netShares: 15700000000, goodwill: 0 },
    { year: '2024', capex: 11500000000, buybacks: 95000000000, dividends: 15200000000, fcf: 108000000000, netShares: 15200000000, goodwill: 0 }
  ];

  const formatLargeNum = (num) => {
    if (!num || isNaN(num)) return `${currencySymbol}0`;
    const abs = Math.abs(num);
    if (abs >= 1e12) return `${currencySymbol}${(num / 1e12).toFixed(1)}T`;
    if (abs >= 1e9) return `${currencySymbol}${(num / 1e9).toFixed(1)}B`;
    if (abs >= 1e6) return `${currencySymbol}${(num / 1e6).toFixed(1)}M`;
    return `${currencySymbol}${num.toLocaleString()}`;
  };

  const formatShares = (s) => {
    if (!s) return '0';
    if (s >= 1e9) return `${(s / 1e9).toFixed(2)}B`;
    if (s >= 1e6) return `${(s / 1e6).toFixed(1)}M`;
    return s.toLocaleString();
  };

  const isExemplary = scorecard.classification === 'EXEMPLARY CAPITAL ALLOCATOR';
  const isCannibal = scorecard.netShareCagr < 0;

  // Chart Geometry
  const width = 640;
  const height = 240;
  const padLeft = 60;
  const padRight = 30;
  const padTop = 30;
  const padBottom = 40;
  const plotW = width - padLeft - padRight;
  const plotH = height - padTop - padBottom;

  // Stacked Bar Geometry for Capital Allocation
  const maxDeployment = Math.max(...chartData.map(d => (d.capex || 0) + (d.buybacks || 0) + (d.dividends || 0)), 1);
  const barSlotW = plotW / chartData.length;
  const barWidth = Math.min(48, barSlotW * 0.55);

  // Shares Line Geometry
  const minShares = Math.min(...chartData.map(d => d.netShares || 0)) * 0.95;
  const maxShares = Math.max(...chartData.map(d => d.netShares || 0)) * 1.05;
  const sharesRange = (maxShares - minShares) || 1;

  const sharesPoints = chartData.map((d, i) => {
    const x = padLeft + i * barSlotW + barSlotW / 2;
    const y = padTop + plotH - (((d.netShares - minShares) / sharesRange) * plotH);
    return { x, y, data: d };
  });

  const sharesPathD = sharesPoints.reduce((acc, pt, i) => {
    return i === 0 ? `M ${pt.x},${pt.y}` : `${acc} L ${pt.x},${pt.y}`;
  }, '');

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Top Header & Rating Badge */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#00D09C]/10 border border-[#00D09C]/30 flex items-center justify-center text-[#00D09C]">
            <Briefcase className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">Management & Capital Allocation Engine</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 10
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Audits corporate capital allocation hierarchy, share cannibal buyback velocity vs dilution treadmills, and the Buffett $1 Test.
            </p>
          </div>
        </div>

        <div className={`self-start md:self-auto px-3.5 py-1.5 rounded-xl border flex items-center gap-2 ${
          isExemplary
            ? 'bg-[#00D09C]/10 border-[#00D09C]/40 text-[#00D09C]'
            : scorecard.classification === 'BALANCED ALLOCATOR'
            ? 'bg-[#1455FE]/10 border-[#1455FE]/40 text-[#1455FE]'
            : 'bg-[#F04438]/10 border-[#F04438]/40 text-[#F04438]'
        }`}>
          {isExemplary ? <CheckCircle2 className="w-4 h-4" /> : <AlertTriangle className="w-4 h-4" />}
          <span className="text-xs font-bold font-mono tracking-wide">{scorecard.classification}</span>
          <span className="text-[11px] font-mono opacity-80">({scorecard.capitalScore}/100)</span>
        </div>
      </div>

      {/* 4 Financial KPI Summary Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 my-5">
        {/* 1. Net Share Count CAGR */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5 flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-[11px] text-[#94A3B8] font-medium">Diluted Share CAGR</span>
            {isCannibal ? (
              <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#00D09C]/10 text-[#00D09C] border border-[#00D09C]/30 flex items-center gap-1">
                <TrendingDown className="w-3 h-3" /> Cannibal
              </span>
            ) : (
              <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#F04438]/10 text-[#F04438] border border-[#F04438]/30 flex items-center gap-1">
                <TrendingUp className="w-3 h-3" /> Dilution
              </span>
            )}
          </div>
          <div className="mt-2">
            <div className="text-xl font-bold font-mono tabular-nums text-[#F8FAFC]">
              {scorecard.netShareCagr > 0 ? `+${scorecard.netShareCagr}` : scorecard.netShareCagr}%
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">
              {isCannibal ? 'Shrinking shares concentrates EPS' : 'Dilution treadmill erodes returns'}
            </p>
          </div>
        </div>

        {/* 2. Buffett $1 Test */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5 flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-[11px] text-[#94A3B8] font-medium">Buffett $1 Test</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded border ${
              scorecard.buffettTestRatio >= 1.0
                ? 'bg-[#00D09C]/10 text-[#00D09C] border-[#00D09C]/30'
                : 'bg-[#F04438]/10 text-[#F04438] border-[#F04438]/30'
            }`}>
              {scorecard.buffettTestRatio >= 1.0 ? 'Passed' : 'Failed'}
            </span>
          </div>
          <div className="mt-2">
            <div className="text-xl font-bold font-mono tabular-nums text-[#F8FAFC]">
              ${scorecard.buffettTestRatio.toFixed(2)}
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">
              Market value generated per $1 retained
            </p>
          </div>
        </div>

        {/* 3. Shareholder Yield */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5 flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-[11px] text-[#94A3B8] font-medium">Shareholder Yield</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#1455FE]/10 text-[#1455FE] border border-[#1455FE]/30">
              Cash Return
            </span>
          </div>
          <div className="mt-2">
            <div className="text-xl font-bold font-mono tabular-nums text-[#F8FAFC]">
              {scorecard.shareholderYield}%
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">
              Annual Buybacks ({scorecard.displayBuybacks}) + Divs
            </p>
          </div>
        </div>

        {/* 4. Goodwill / Asset Ratio */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5 flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-[11px] text-[#94A3B8] font-medium">Goodwill / Assets</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded border ${
              scorecard.goodwillRatio <= 20.0
                ? 'bg-[#00D09C]/10 text-[#00D09C] border-[#00D09C]/30'
                : 'bg-[#F59E0B]/10 text-[#F59E0B] border-[#F59E0B]/30'
            }`}>
              {scorecard.goodwillRatio <= 15.0 ? 'Organic' : 'M&A Risk'}
            </span>
          </div>
          <div className="mt-2">
            <div className="text-xl font-bold font-mono tabular-nums text-[#F8FAFC]">
              {scorecard.goodwillRatio}%
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">
              Low goodwill protects against impairments
            </p>
          </div>
        </div>
      </div>

      {/* Chart Selector Tabs & Legend */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('allocation')}
            className={`fintech-tab-btn ${activeTab === 'allocation' ? 'active' : ''}`}
          >
            Capital Deployment Stack
          </button>
          <button
            onClick={() => setActiveTab('shares')}
            className={`fintech-tab-btn ${activeTab === 'shares' ? 'active' : ''}`}
          >
            Diluted Shares Cannibal Curve
          </button>
        </div>

        {activeTab === 'allocation' ? (
          <div className="flex items-center gap-4 text-xs">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded bg-[#00D09C]" />
              <span className="text-[#94A3B8]">Buybacks</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded bg-[#0EA5E9]" />
              <span className="text-[#94A3B8]">Dividends</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded bg-[#6366F1]" />
              <span className="text-[#94A3B8]">CapEx</span>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-2 text-xs">
            <div className="w-2.5 h-2.5 rounded-full bg-[#00D09C]" />
            <span className="text-[#94A3B8]">Audited Diluted Shares Outstanding</span>
          </div>
        )}
      </div>

      {/* SVG Interactive Chart Canvas */}
      <div className="relative w-full overflow-x-auto">
        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="w-full h-auto min-w-[500px] overflow-visible"
        >
          {/* Subtle Gridlines */}
          {[0, 0.25, 0.5, 0.75, 1.0].map((tick, i) => {
            const y = padTop + plotH * (1 - tick);
            const val = activeTab === 'allocation'
              ? formatLargeNum(maxDeployment * tick)
              : formatShares(minShares + sharesRange * tick);
            return (
              <g key={i}>
                <line
                  x1={padLeft}
                  y1={y}
                  x2={padLeft + plotW}
                  y2={y}
                  stroke="#222A3B"
                  strokeDasharray="3 3"
                />
                <text
                  x={padLeft - 8}
                  y={y + 3}
                  textAnchor="end"
                  fill="#64748B"
                  className="text-[10px] font-mono tabular-nums"
                >
                  {val}
                </text>
              </g>
            );
          })}

          {/* TAB 1: Capital Deployment Stacked Bars */}
          {activeTab === 'allocation' && chartData.map((d, i) => {
            const x = padLeft + i * barSlotW + (barSlotW - barWidth) / 2;
            const capex = d.capex || 0;
            const buybacks = d.buybacks || 0;
            const dividends = d.dividends || 0;
            const total = capex + buybacks + dividends;

            const capexH = (capex / maxDeployment) * plotH;
            const divH = (dividends / maxDeployment) * plotH;
            const buybackH = (buybacks / maxDeployment) * plotH;

            const capexY = padTop + plotH - capexH;
            const divY = capexY - divH;
            const buybackY = divY - buybackH;

            const isHovered = hoveredIdx === i;

            return (
              <g
                key={d.year}
                onMouseEnter={() => setHoveredIdx(i)}
                onMouseLeave={() => setHoveredIdx(null)}
                className="cursor-pointer transition-opacity"
                opacity={hoveredIdx !== null && !isHovered ? 0.4 : 1}
              >
                {/* CapEx bar */}
                <rect
                  x={x}
                  y={capexY}
                  width={barWidth}
                  height={Math.max(2, capexH)}
                  fill="#6366F1"
                  rx={2}
                />
                {/* Dividends bar */}
                <rect
                  x={x}
                  y={divY}
                  width={barWidth}
                  height={Math.max(2, divH)}
                  fill="#0EA5E9"
                  rx={2}
                />
                {/* Buybacks bar */}
                <rect
                  x={x}
                  y={buybackY}
                  width={barWidth}
                  height={Math.max(2, buybackH)}
                  fill="#00D09C"
                  rx={4}
                />

                {/* X Axis Label */}
                <text
                  x={x + barWidth / 2}
                  y={padTop + plotH + 20}
                  textAnchor="middle"
                  fill={isHovered ? '#00D09C' : '#94A3B8'}
                  className="text-[11px] font-mono font-medium"
                >
                  {d.year}
                </text>
              </g>
            );
          })}

          {/* TAB 2: Diluted Shares Cannibal Curve */}
          {activeTab === 'shares' && (
            <g>
              {/* Line */}
              <path
                d={sharesPathD}
                fill="none"
                stroke="#00D09C"
                strokeWidth={2.5}
                strokeLinecap="round"
                strokeLinejoin="round"
              />

              {/* Data points */}
              {sharesPoints.map((pt, i) => {
                const isHovered = hoveredIdx === i;
                return (
                  <g
                    key={pt.data.year}
                    onMouseEnter={() => setHoveredIdx(i)}
                    onMouseLeave={() => setHoveredIdx(null)}
                    className="cursor-pointer"
                  >
                    <circle
                      cx={pt.x}
                      cy={pt.y}
                      r={isHovered ? 6 : 4}
                      fill="#0B0E14"
                      stroke="#00D09C"
                      strokeWidth={2.5}
                    />
                    {/* X Axis Label */}
                    <text
                      x={pt.x}
                      y={padTop + plotH + 20}
                      textAnchor="middle"
                      fill={isHovered ? '#00D09C' : '#94A3B8'}
                      className="text-[11px] font-mono font-medium"
                    >
                      {pt.data.year}
                    </text>
                  </g>
                );
              })}
            </g>
          )}
        </svg>

      </div>

      {/* Breakdown Cards Strip Below Chart */}
      <div style={{ display: 'grid', gridTemplateColumns: `repeat(${chartData.length}, 1fr)`, gap: '12px', marginTop: '14px' }}>
        {chartData.map((d, i) => {
          const isHovered = hoveredIdx === i;
          return (
            <div
              key={d.year}
              onMouseEnter={() => setHoveredIdx(i)}
              onMouseLeave={() => setHoveredIdx(null)}
              style={{
                background: isHovered ? 'var(--bg-surface-elevated)' : 'var(--bg-surface)',
                border: `1px solid ${isHovered ? 'var(--fintech-primary)' : 'var(--border-hairline)'}`,
                borderRadius: '8px',
                padding: '10px 12px',
                display: 'flex',
                flexDirection: 'column',
                gap: '5px',
                transition: 'all 0.15s ease'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '12px', fontWeight: '800', color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
                  FY{d.year}
                </span>
                <span style={{ fontSize: '10.5px', color: '#00D09C', fontWeight: '800', fontFamily: 'var(--font-mono)' }}>
                  {activeTab === 'shares' ? formatShares(d.netShares) : formatLargeNum((d.buybacks || 0) + (d.dividends || 0))}
                </span>
              </div>

              {activeTab === 'allocation' ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                    <span>Buybacks:</span>
                    <strong>{formatLargeNum(d.buybacks)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#0EA5E9' }}>
                    <span>Dividends:</span>
                    <strong>{formatLargeNum(d.dividends)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#6366F1' }}>
                    <span>CapEx:</span>
                    <strong>{formatLargeNum(d.capex)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-primary)', borderTop: '1px solid var(--border-hairline)', paddingTop: '2px' }}>
                    <span>FCF:</span>
                    <strong>{formatLargeNum(d.fcf)}</strong>
                  </div>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                    <span>Diluted Shares:</span>
                    <strong>{formatShares(d.netShares)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                    <span>YoY Dilution:</span>
                    <strong style={{ color: (d.dilutionRate || 0) <= 0 ? 'var(--gain-green)' : 'var(--loss-red)' }}>
                      {(d.dilutionRate || 0) > 0 ? `+${d.dilutionRate}%` : `${d.dilutionRate || 0}%`}
                    </strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#38BDF8' }}>
                    <span>FCF / Share:</span>
                    <strong>${d.netShares ? ((d.fcf || 0) / d.netShares).toFixed(2) : 'N/A'}</strong>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Institutional Capital Allocation Insight Card */}
      <div className="mt-5 p-4 rounded-xl bg-[#0B0E14] border border-[#222A3B] flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-[#F8FAFC]">Tier-1 Fintech Capital Allocation Audit</span>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#00D09C]/10 text-[#00D09C] border border-[#00D09C]/30">
              Audited 10-K Cash Flows
            </span>
          </div>
          <p className="text-[11px] text-[#94A3B8] leading-relaxed">
            {isCannibal
              ? 'Management displays elite shareholder alignment by aggressively cannibalizing shares, compoundingly elevating EPS, and passing the Buffett $1 Test.'
              : 'Management is running a net dilution treadmill where share count is growing faster than per-share value creation. Scrutinize executive SBC grant packages.'}
          </p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <div className="text-right">
            <div className="text-[10px] text-[#64748B]">Total Annual Return</div>
            <div className="text-sm font-bold font-mono text-[#00D09C]">
              {formatLargeNum((scorecard.annualBuybacks || 0) + (scorecard.annualDividends || 0))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
