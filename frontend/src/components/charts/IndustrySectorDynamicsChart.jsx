import React, { useState } from 'react';
import { Compass, TrendingUp, TrendingDown, ShieldCheck, AlertTriangle, Layers, BarChart3, Activity } from 'lucide-react';

export default function IndustrySectorDynamicsChart({ data, sectorScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('corridor'); // 'corridor' | 'cycleClock' | 'hhiSpectrum'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  const scorecard = sectorScorecard || {
    sector: 'Information Technology',
    industry: 'Software — Infrastructure',
    beta: 1.15,
    medianMargin: 24.5,
    companyMargin: 42.8,
    marginSpread: 18.3,
    hhi: 2100,
    hhiZone: 'Highly Concentrated (Oligopoly)',
    cyclicality: 'Defensive / Secular Growth',
    normalizedPe: 27.5,
    score: 86.0,
    classification: 'TIER-1 STRUCTURAL SECULAR LEADER'
  };

  const chartData = data && data.length > 0 ? data : [
    { year: '2020', companyMargin: 37.0, sectorMedian: 22.0, topQuartile: 29.5, bottomQuartile: 14.5, marginSpread: 15.0 },
    { year: '2021', companyMargin: 41.6, sectorMedian: 24.0, topQuartile: 32.0, bottomQuartile: 15.5, marginSpread: 17.6 },
    { year: '2022', companyMargin: 42.1, sectorMedian: 24.5, topQuartile: 33.0, bottomQuartile: 16.0, marginSpread: 17.6 },
    { year: '2023', companyMargin: 41.8, sectorMedian: 23.5, topQuartile: 31.5, bottomQuartile: 15.0, marginSpread: 18.3 },
    { year: '2024', companyMargin: 44.6, sectorMedian: 24.5, topQuartile: 33.0, bottomQuartile: 16.0, marginSpread: 20.1 }
  ];

  const isLeader = scorecard.classification === 'TIER-1 STRUCTURAL SECULAR LEADER';
  const isOligopoly = scorecard.hhi >= 1500;
  const isHighBeta = scorecard.beta > 1.25;

  // Chart Dimensions & Geometry
  const width = 640;
  const height = 240;
  const padLeft = 55;
  const padRight = 30;
  const padTop = 30;
  const padBottom = 40;
  const plotW = width - padLeft - padRight;
  const plotH = height - padTop - padBottom;

  const allVals = chartData.flatMap(d => [d.companyMargin, d.sectorMedian, d.topQuartile, d.bottomQuartile]);
  const minVal = Math.max(0, Math.min(...allVals) - 5);
  const maxVal = Math.max(...allVals) + 8;
  const range = (maxVal - minVal) || 1;

  const getX = (idx) => padLeft + (idx / (chartData.length - 1 || 1)) * plotW;
  const getY = (val) => padTop + plotH - (((val - minVal) / range) * plotH);

  // Corridor Area Path (Upper Quartile to Lower Quartile)
  const topPoints = chartData.map((d, i) => `${getX(i)},${getY(d.topQuartile)}`);
  const bottomPoints = chartData.slice().reverse().map((d, i) => {
    const revIdx = chartData.length - 1 - i;
    return `${getX(revIdx)},${getY(d.bottomQuartile)}`;
  });
  const corridorAreaD = `M ${topPoints.join(' L ')} L ${bottomPoints.join(' L ')} Z`;

  // Sector Median Path (Dashed Slate)
  const medianPathD = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.sectorMedian)}`).join(' ');

  // Company Margin Path (Solid Emerald)
  const companyPathD = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.companyMargin)}`).join(' ');

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Header & Rating Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#00D09C]/10 border border-[#00D09C]/30 flex items-center justify-center text-[#00D09C]">
            <Compass className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">Industry & Sector Dynamics</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 11
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Audits macro business cycle sensitivity, GICS benchmark corridors, Herfindahl concentration, and normalized mid-cycle multiples.
            </p>
          </div>
        </div>

        <div className={`self-start md:self-auto px-3.5 py-1.5 rounded-xl border flex items-center gap-2 ${
          isLeader
            ? 'bg-[#00D09C]/10 border-[#00D09C]/40 text-[#00D09C]'
            : scorecard.score >= 65
            ? 'bg-[#1455FE]/10 border-[#1455FE]/40 text-[#1455FE]'
            : 'bg-[#F04438]/10 border-[#F04438]/40 text-[#F04438]'
        }`}>
          {isLeader ? (
            <ShieldCheck className="w-4 h-4" />
          ) : (
            <Activity className="w-4 h-4" />
          )}
          <span className="text-xs font-bold tracking-wider">{scorecard.classification}</span>
          <span className="text-[11px] font-mono opacity-80">({scorecard.score}/100)</span>
        </div>
      </div>

      {/* 4 Core Financial Metrics */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 my-5">
        {/* Metric 1: Sector Beta */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Sector Cyclicality Beta</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.beta <= 1.0 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.beta <= 1.0 ? 'Defensive' : 'Cyclical'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.beta.toFixed(2)}β
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Benchmark: β &lt; 1.20 (Stable Secular)
          </div>
        </div>

        {/* Metric 2: Relative Margin Spread */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Relative Margin Spread</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.marginSpread >= 3.0 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.marginSpread >= 0 ? '+' : ''}{scorecard.marginSpread.toFixed(1)}%
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {scorecard.companyMargin.toFixed(1)}% <span className="text-xs text-[#94A3B8] font-normal">vs {scorecard.medianMargin.toFixed(1)}% Med</span>
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Sector: {scorecard.sector}
          </div>
        </div>

        {/* Metric 3: Industry Concentration HHI */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Industry HHI Concentration</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.hhi >= 1800 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.hhi >= 1000 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.hhi >= 1800 ? 'Oligopoly' : scorecard.hhi >= 1000 ? 'Moderate' : 'Fragmented'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.hhi.toLocaleString()} <span className="text-xs text-[#94A3B8] font-normal">HHI</span>
          </div>
          <div className="text-[10px] text-[#64748B] mt-1 truncate">
            {scorecard.hhiZone}
          </div>
        </div>

        {/* Metric 4: Normalized Mid-Cycle P/E */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Normalized Mid-Cycle P/E</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
              Multi-Year Avg
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.normalizedPe.toFixed(1)}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Benchmark: &lt; 25.0x Mid-Cycle
          </div>
        </div>
      </div>

      {/* View Switcher Tabs */}
      <div className="flex items-center justify-between gap-2 border-b border-[#222A3B] pb-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('corridor')}
            className={`fintech-tab-btn ${activeTab === 'corridor' ? 'active' : ''}`}
          >
            <BarChart3 className="w-3.5 h-3.5" />
            <span>Sector Margin Corridor</span>
          </button>
          <button
            onClick={() => setActiveTab('cycleClock')}
            className={`fintech-tab-btn ${activeTab === 'cycleClock' ? 'active' : ''}`}
          >
            <Compass className="w-3.5 h-3.5" />
            <span>Macro Cycle Clock</span>
          </button>
          <button
            onClick={() => setActiveTab('hhiSpectrum')}
            className={`fintech-tab-btn ${activeTab === 'hhiSpectrum' ? 'active' : ''}`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>HHI Oligopoly Scale</span>
          </button>
        </div>

        {/* Legend for Corridor */}
        {activeTab === 'corridor' && (
          <div className="hidden sm:flex items-center gap-4 text-xs font-medium text-[#94A3B8]">
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded-sm bg-[#00D09C]"></span>
              <span className="text-[#F8FAFC]">Company Operating Margin</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#94A3B8] border-t border-dashed border-[#94A3B8]"></span>
              <span>Sector Median</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded-sm bg-[#1455FE]/20 border border-[#1455FE]/40"></span>
              <span>Peer 25th-75th Corridor</span>
            </div>
          </div>
        )}
      </div>

      {/* Main Visual Display */}
      {activeTab === 'corridor' && (
        <div className="relative w-full overflow-hidden">
          <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-auto select-none overflow-visible">
            <defs>
              <linearGradient id="corridorGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#1455FE" stopOpacity="0.25" />
                <stop offset="100%" stopColor="#1455FE" stopOpacity="0.05" />
              </linearGradient>
              <linearGradient id="companyLineGrad" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stopColor="#00D09C" />
                <stop offset="100%" stopColor="#10B981" />
              </linearGradient>
            </defs>

            {/* Horizontal Gridlines */}
            {[0, 0.25, 0.5, 0.75, 1.0].map((ratio, idx) => {
              const val = minVal + ratio * range;
              const y = padTop + plotH - ratio * plotH;
              return (
                <g key={idx}>
                  <line
                    x1={padLeft}
                    y1={y}
                    x2={width - padRight}
                    y2={y}
                    stroke="#222A3B"
                    strokeDasharray="3 3"
                    strokeWidth="1"
                  />
                  <text
                    x={padLeft - 10}
                    y={y + 3}
                    fill="#64748B"
                    fontSize="10"
                    fontFamily="monospace"
                    textAnchor="end"
                  >
                    {val.toFixed(0)}%
                  </text>
                </g>
              );
            })}

            {/* Sector Corridor Area (Interquartile Range) */}
            <path d={corridorAreaD} fill="url(#corridorGrad)" />

            {/* Sector Median Dashed Line */}
            <path
              d={medianPathD}
              fill="none"
              stroke="#94A3B8"
              strokeWidth="2"
              strokeDasharray="4 4"
            />

            {/* Company Margin Solid Line */}
            <path
              d={companyPathD}
              fill="none"
              stroke="url(#companyLineGrad)"
              strokeWidth="3.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />

            {/* Data Points and Interaction Rects */}
            {chartData.map((d, idx) => {
              const x = getX(idx);
              const yCo = getY(d.companyMargin);
              const yMed = getY(d.sectorMedian);
              const isHovered = hoveredIdx === idx;

              return (
                <g key={idx}>
                  {/* Hover vertical guide */}
                  {isHovered && (
                    <line
                      x1={x}
                      y1={padTop}
                      x2={x}
                      y2={padTop + plotH}
                      stroke="#00D09C"
                      strokeWidth="1"
                      strokeDasharray="2 2"
                      opacity="0.6"
                    />
                  )}

                  {/* Company Margin Node */}
                  <circle
                    cx={x}
                    cy={yCo}
                    r={isHovered ? 6 : 4}
                    fill="#0B0E14"
                    stroke="#00D09C"
                    strokeWidth="2.5"
                    className="transition-all duration-150"
                  />

                  {/* Sector Median Node */}
                  <circle
                    cx={x}
                    cy={yMed}
                    r={3}
                    fill="#94A3B8"
                  />

                  {/* X-axis Label */}
                  <text
                    x={x}
                    y={padTop + plotH + 20}
                    fill={isHovered ? '#00D09C' : '#94A3B8'}
                    fontSize="11"
                    fontWeight={isHovered ? 'bold' : 'normal'}
                    fontFamily="monospace"
                    textAnchor="middle"
                  >
                    {d.year}
                  </text>

                  {/* Invisible touch/hover target */}
                  <rect
                    x={x - 30}
                    y={padTop}
                    width={60}
                    height={plotH + 30}
                    fill="transparent"
                    onMouseEnter={() => setHoveredIdx(idx)}
                    onMouseLeave={() => setHoveredIdx(null)}
                    className="cursor-pointer"
                  />
                </g>
              );
            })}
          </svg>

          {/* Floating Hover Tooltip */}
          {hoveredIdx !== null && chartData[hoveredIdx] && (
            <div
              className="absolute pointer-events-none bg-[#0B0E14]/95 border border-[#00D09C]/40 rounded-xl p-3 shadow-xl backdrop-blur-md text-xs z-20"
              style={{
                left: `${(hoveredIdx / (chartData.length - 1)) * 75 + 10}%`,
                top: '10%'
              }}
            >
              <div className="font-bold text-[#F8FAFC] pb-1 border-b border-[#222A3B] flex items-center justify-between gap-4">
                <span>Fiscal {chartData[hoveredIdx].year}</span>
                <span className="font-mono text-[#00D09C]">
                  +{chartData[hoveredIdx].marginSpread.toFixed(1)}% Spread
                </span>
              </div>
              <div className="space-y-1 mt-1.5 font-mono">
                <div className="flex items-center justify-between text-[#F8FAFC] gap-4">
                  <span>Company Margin:</span>
                  <span className="font-bold text-[#00D09C]">{chartData[hoveredIdx].companyMargin.toFixed(1)}%</span>
                </div>
                <div className="flex items-center justify-between text-[#94A3B8] gap-4">
                  <span>Sector Median:</span>
                  <span>{chartData[hoveredIdx].sectorMedian.toFixed(1)}%</span>
                </div>
                <div className="flex items-center justify-between text-[#64748B] gap-4 text-[10px]">
                  <span>Corridor Range:</span>
                  <span>{chartData[hoveredIdx].bottomQuartile.toFixed(1)}% – {chartData[hoveredIdx].topQuartile.toFixed(1)}%</span>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Macro Cycle Clock / 4-Quadrant View */}
      {activeTab === 'cycleClock' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 py-2">
          {/* Quadrant 1: Early Cycle */}
          <div className="bg-[#0B0E14] border border-[#222A3B] rounded-xl p-4 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-[#F8FAFC]">1. Early Cycle (Recovery)</span>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1C2333] text-[#94A3B8]">GDP Accelerating</span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-2">
              Credit expands, industrial demand revives. Best sectors: Consumer Discretionary, Industrials, Materials.
            </p>
            <div className="mt-3 text-[11px] text-[#64748B] font-mono">
              Operating Leverage: High Fixed Absorption
            </div>
          </div>

          {/* Quadrant 2: Mid Cycle */}
          <div className={`bg-[#0B0E14] border rounded-xl p-4 flex flex-col justify-between ${
            scorecard.sector === 'Information Technology' || scorecard.sector === 'Communication Services'
              ? 'border-[#00D09C]/60 bg-[#00D09C]/5'
              : 'border-[#222A3B]'
          }`}>
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-[#F8FAFC]">2. Mid Cycle (Peak Expansion)</span>
              {scorecard.sector === 'Information Technology' && (
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#00D09C]/20 text-[#00D09C] border border-[#00D09C]/30 font-bold animate-pulse">
                  Current Sector Anchor
                </span>
              )}
            </div>
            <p className="text-xs text-[#94A3B8] mt-2">
              Broadest business cycle phase. Reinvestment surges. Best sectors: Information Technology, Industrials.
            </p>
            <div className="mt-3 text-[11px] text-[#00D09C] font-mono">
              Capital Spending & Secular Growth Peak
            </div>
          </div>

          {/* Quadrant 3: Late Cycle */}
          <div className={`bg-[#0B0E14] border rounded-xl p-4 flex flex-col justify-between ${
            scorecard.sector === 'Energy' || scorecard.sector === 'Materials'
              ? 'border-[#00D09C]/60 bg-[#00D09C]/5'
              : 'border-[#222A3B]'
          }`}>
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-[#F8FAFC]">3. Late Cycle (Overheating)</span>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1C2333] text-[#94A3B8]">Capacity Constraints</span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-2">
              Wage inflation, rate hikes, tight labor. Best sectors: Energy, Materials, Financials (asset yield expansion).
            </p>
            <div className="mt-3 text-[11px] text-[#F59E0B] font-mono">
              Commodity Price Spikes & Margin Compression
            </div>
          </div>

          {/* Quadrant 4: Recession */}
          <div className={`bg-[#0B0E14] border rounded-xl p-4 flex flex-col justify-between ${
            scorecard.sector === 'Consumer Staples' || scorecard.sector === 'Health Care' || scorecard.sector === 'Utilities'
              ? 'border-[#00D09C]/60 bg-[#00D09C]/5'
              : 'border-[#222A3B]'
          }`}>
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-[#F8FAFC]">4. Recession (Contraction)</span>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1C2333] text-[#94A3B8]">Earnings Trough</span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-2">
              GDP contracts, credit stress. Best sectors: Health Care, Consumer Staples, Utilities (essential demand).
            </p>
            <div className="mt-3 text-[11px] text-[#1455FE] font-mono">
              Defensive Moats & Free Cash Flow Flight to Safety
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: HHI Oligopoly Spectrum */}
      {activeTab === 'hhiSpectrum' && (
        <div className="bg-[#0B0E14] border border-[#222A3B] rounded-xl p-5 my-2">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-[#F8FAFC]">DOJ Herfindahl-Hirschman Concentration Scale</span>
            <span className="text-xs font-mono font-bold text-[#00D09C]">
              {scorecard.industry}: {scorecard.hhi} HHI
            </span>
          </div>

          {/* Spectrum Bar */}
          <div className="relative w-full h-7 rounded-xl overflow-hidden flex my-4 border border-[#222A3B]">
            <div className="w-[33.3%] bg-[#F04438]/20 border-r border-[#222A3B] flex items-center justify-center text-[10px] font-mono text-[#F04438]">
              Unconcentrated (&lt;1,000)
            </div>
            <div className="w-[26.7%] bg-[#F59E0B]/20 border-r border-[#222A3B] flex items-center justify-center text-[10px] font-mono text-[#F59E0B]">
              Moderate (1,000–1,800)
            </div>
            <div className="w-[40.0%] bg-[#00D09C]/20 flex items-center justify-center text-[10px] font-mono text-[#00D09C]">
              Consolidated Oligopoly (&gt;1,800)
            </div>

            {/* Pointer Marker */}
            <div
              className="absolute top-0 bottom-0 w-1 bg-[#F8FAFC] shadow-[0_0_8px_#F8FAFC] flex flex-col items-center justify-center"
              style={{ left: `${Math.min(96, Math.max(4, (scorecard.hhi / 3000) * 100))}%` }}
            >
              <div className="w-2.5 h-2.5 rounded-full bg-[#00D09C] border border-[#F8FAFC] -mt-6 shadow-md" />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs text-[#94A3B8] mt-3 pt-3 border-t border-[#222A3B]">
            <div>
              <span className="font-bold text-[#F04438] block mb-0.5">&lt; 1,000 HHI: Fragmented</span>
              Price wars, high customer churn, zero pricing power (e.g. Restaurants, Apparel Retail).
            </div>
            <div>
              <span className="font-bold text-[#F59E0B] block mb-0.5">1,000 – 1,800 HHI: Moderate</span>
              Balanced competition; occasional promotional discounting; moderate gross margin moat.
            </div>
            <div>
              <span className="font-bold text-[#00D09C] block mb-0.5">&gt; 1,800 HHI: Rational Oligopoly</span>
              Structural barriers to entry, disciplined capacity, premium multi-year pricing power (e.g. Cloud, Rail).
            </div>
          </div>
        </div>
      )}

      {/* Institutional Audit Summary Card */}
      <div className="mt-4 p-4 rounded-xl bg-[#1C2333]/70 border border-[#222A3B] flex flex-col md:flex-row items-start md:items-center justify-between gap-3 text-xs">
        <div className="space-y-1">
          <div className="font-bold text-[#F8FAFC] flex items-center gap-2">
            <span>Institutional Industry Audit Takeaway:</span>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#00D09C]/10 text-[#00D09C] border border-[#00D09C]/30">
              {scorecard.cyclicality}
            </span>
          </div>
          <p className="text-[#94A3B8]">
            {scorecard.marginSpread >= 3.0
              ? `Demonstrates a formidable ${scorecard.marginSpread.toFixed(1)}% margin spread over the ${scorecard.sector} median, verifying that its pricing power is driven by structural firm-specific moats rather than general sector tailwinds.`
              : `Operating margins trade in-line with the sector benchmark (${scorecard.medianMargin.toFixed(1)}%), indicating that profitability is largely governed by broader macro industry cycles and sector pricing tides.`}
          </p>
        </div>

        <div className="shrink-0 flex items-center gap-2">
          <span className="text-[11px] font-mono text-[#64748B]">Mid-Cycle Valuation:</span>
          <span className="font-mono font-bold text-[#F8FAFC] px-2 py-1 rounded bg-[#0B0E14] border border-[#222A3B]">
            {scorecard.normalizedPe.toFixed(1)}x P/E
          </span>
        </div>
      </div>
    </div>
  );
}
