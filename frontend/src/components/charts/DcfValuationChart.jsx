import React, { useState } from 'react';
import { Calculator, ShieldCheck, Activity, BarChart3, TrendingUp, TrendingDown, DollarSign, Grid, Info, Landmark } from 'lucide-react';

export default function DcfValuationChart({ data, dcfScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('cashFlowTimeline'); // 'cashFlowTimeline' | 'sensitivityMatrix' | 'marginOfSafety'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  const scorecard = dcfScorecard || {
    ticker: 'AAPL',
    currentPrice: 224.50,
    fairValue: 242.80,
    marginOfSafety: 7.5,
    wacc: 8.50,
    terminalGrowth: 2.5,
    tvWeightPct: 76.2,
    sumPvFcf: 432000000000,
    score: 75.0,
    classification: 'FAIR VALUE ACCUMULATION'
  };

  const projections = data && data.projections && data.projections.length > 0 ? data.projections : [
    { year: '+1Y', projectedFcf: 115000000000, discountFactor: 0.922, pvFcf: 106000000000 },
    { year: '+2Y', projectedFcf: 122000000000, discountFactor: 0.849, pvFcf: 103600000000 },
    { year: '+3Y', projectedFcf: 129000000000, discountFactor: 0.783, pvFcf: 101000000000 },
    { year: '+4Y', projectedFcf: 135000000000, discountFactor: 0.722, pvFcf: 97500000000 },
    { year: '+5Y', projectedFcf: 141000000000, discountFactor: 0.665, pvFcf: 93800000000 }
  ];

  const waccSteps = data && data.waccSteps ? data.waccSteps : [7.5, 8.0, 8.5, 9.0, 9.5];
  const growthSteps = data && data.growthSteps ? data.growthSteps : [1.5, 2.0, 2.5, 3.0, 3.5];
  const sensitivityMatrix = data && data.sensitivityMatrix ? data.sensitivityMatrix : [
    [275.2, 290.4, 308.5, 330.1, 356.2],
    [252.1, 264.8, 279.6, 297.0, 317.8],
    [232.4, 242.8, 255.0, 269.1, 285.6],
    [215.3, 224.1, 234.2, 245.8, 259.0],
    [200.5, 208.0, 216.5, 226.1, 237.1]
  ];

  const formatB = (val) => {
    if (!val || isNaN(val)) return `${currencySymbol}0.0B`;
    const abs = Math.abs(val);
    const sign = val < 0 ? '-' : '';
    if (abs >= 1e12) return `${sign}${currencySymbol}${(abs / 1e12).toFixed(2)}T`;
    if (abs >= 1e9) return `${sign}${currencySymbol}${(abs / 1e9).toFixed(1)}B`;
    if (abs >= 1e6) return `${sign}${currencySymbol}${(abs / 1e6).toFixed(1)}M`;
    return `${sign}${currencySymbol}${abs.toFixed(0)}`;
  };

  const isPositiveMargin = (scorecard.marginOfSafety || 0) >= 0;
  const isHealthyScore = (scorecard.score || 0) >= 65;

  // SVG Geometry for Cash Flow Timeline
  const svgW = 640;
  const svgH = 240;
  const padL = 50;
  const padR = 25;
  const padT = 30;
  const padB = 40;
  const plotW = svgW - padL - padR;
  const plotH = svgH - padT - padB;

  const maxVal = Math.max(100, ...projections.map(p => p.projectedFcf)) * 1.15;
  const barWidth = (plotW / (projections.length + 1)) * 0.55;

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Header & Scorecard Badge */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#00D09C]/10 border border-[#00D09C]/30 flex items-center justify-center text-[#00D09C]">
            <Calculator className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">Discounted Cash Flow (DCF) Engine</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 15 • Stage 3
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Audits 5-year discrete cash flows, WACC discount rates, Gordon Growth terminal value, and Margin of Safety.
            </p>
          </div>
        </div>

        <div className={`self-start md:self-auto px-3.5 py-1.5 rounded-xl border flex items-center gap-2 ${
          isHealthyScore
            ? 'bg-[#00D09C]/10 border-[#00D09C]/40 text-[#00D09C]'
            : isPositiveMargin
            ? 'bg-[#1455FE]/10 border-[#1455FE]/40 text-[#1455FE]'
            : 'bg-[#F04438]/10 border-[#F04438]/40 text-[#F04438]'
        }`}>
          {isHealthyScore ? <ShieldCheck className="w-4 h-4" /> : <Activity className="w-4 h-4" />}
          <span className="text-xs font-bold tracking-wider">{scorecard.classification}</span>
          <span className="text-[11px] font-mono opacity-80">({scorecard.score}/100)</span>
        </div>
      </div>

      {/* 4 Core Fintech KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 my-5">
        {/* Metric 1: Intrinsic Fair Value */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>DCF Fair Value</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              isPositiveMargin ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {isPositiveMargin ? 'Intrinsic Upside' : 'Premium'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {currencySymbol}{scorecard.fairValue ? scorecard.fairValue.toFixed(2) : '0.00'}
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Market Price: {currencySymbol}{scorecard.currentPrice ? scorecard.currentPrice.toFixed(2) : '0.00'}
          </div>
        </div>

        {/* Metric 2: Margin of Safety */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Margin of Safety</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.marginOfSafety >= 20 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.marginOfSafety >= 0 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.marginOfSafety >= 20 ? 'Target Hurdle' : scorecard.marginOfSafety >= 0 ? 'Narrow' : 'Negative'}
            </span>
          </div>
          <div className={`text-xl font-bold font-mono mt-1.5 ${isPositiveMargin ? 'text-[#00D09C]' : 'text-[#F04438]'}`}>
            {scorecard.marginOfSafety > 0 ? '+' : ''}{scorecard.marginOfSafety ? scorecard.marginOfSafety.toFixed(1) : '0.0'}%
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Target Buffer: &ge; 25.0%
          </div>
        </div>

        {/* Metric 3: WACC Discount Rate */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>WACC Hurdle Rate</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#1C2333] text-[#94A3B8]">
              Cost of Capital
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#1455FE] mt-1.5">
            {scorecard.wacc ? scorecard.wacc.toFixed(2) : '8.50'}%
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Terminal Growth (g): {scorecard.terminalGrowth ? scorecard.terminalGrowth.toFixed(1) : '2.5'}%
          </div>
        </div>

        {/* Metric 4: Terminal Value Weight */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Terminal Value %</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.tvWeightPct <= 75 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F59E0B]/10 text-[#F59E0B]'
            }`}>
              {scorecard.tvWeightPct <= 75 ? 'Balanced' : 'High Sensitivity'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.tvWeightPct ? scorecard.tvWeightPct.toFixed(1) : '75.0'}%
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            5Y FCF Sum: {formatB(scorecard.sumPvFcf)}
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center justify-between gap-2 border-b border-[#222A3B] pb-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('cashFlowTimeline')}
            className={`fintech-tab-btn ${activeTab === 'cashFlowTimeline' ? 'active' : ''}`}
          >
            <BarChart3 className="w-3.5 h-3.5" />
            <span>5-Year DCF Timeline</span>
          </button>
          <button
            onClick={() => setActiveTab('sensitivityMatrix')}
            className={`fintech-tab-btn ${activeTab === 'sensitivityMatrix' ? 'active' : ''}`}
          >
            <Grid className="w-3.5 h-3.5" />
            <span>WACC vs Terminal Growth Matrix</span>
          </button>
          <button
            onClick={() => setActiveTab('marginOfSafety')}
            className={`fintech-tab-btn ${activeTab === 'marginOfSafety' ? 'active' : ''}`}
          >
            <Landmark className="w-3.5 h-3.5" />
            <span>Margin of Safety Gauge</span>
          </button>
        </div>

        {activeTab === 'cashFlowTimeline' && (
          <div className="hidden sm:flex items-center gap-3 text-xs font-medium text-[#94A3B8]">
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded bg-[#1455FE]"></span>
              <span>Projected FCF</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded bg-[#00D09C]"></span>
              <span className="text-[#F8FAFC]">Discounted PV</span>
            </div>
          </div>
        )}
      </div>

      {/* Tab 1: 5-Year Cash Flow Timeline SVG */}
      {activeTab === 'cashFlowTimeline' && (
        <div className="space-y-4">
          <div className="w-full bg-[#0B0E14] border border-[#222A3B] rounded-xl p-4">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-2">
              Discrete 5-Year Cash Flow Projections vs Discounted Present Value ($)
            </div>
            <div className="w-full overflow-x-auto">
              <svg viewBox={`0 0 ${svgW} ${svgH}`} className="w-full h-auto select-none">
                {/* Horizontal Grid lines */}
                {[0, 0.25, 0.5, 0.75, 1.0].map((ratio, i) => {
                  const y = padT + plotH * (1 - ratio);
                  const val = formatB(ratio * maxVal);
                  return (
                    <g key={i}>
                      <line x1={padL} y1={y} x2={svgW - padR} y2={y} stroke="#222A3B" strokeDasharray="3 3" />
                      <text x={padL - 8} y={y + 3} fill="#64748B" fontSize="10" textAnchor="end" fontFamily="monospace">
                        {val}
                      </text>
                    </g>
                  );
                })}

                {/* Bars for each projection year */}
                {projections.map((p, i) => {
                  const slotW = plotW / projections.length;
                  const cx = padL + i * slotW + slotW / 2;
                  const hProj = (p.projectedFcf / maxVal) * plotH;
                  const hPv = (p.pvFcf / maxVal) * plotH;
                  const yProj = padT + plotH - hProj;
                  const yPv = padT + plotH - hPv;
                  const isHov = hoveredIdx === i;

                  return (
                    <g key={i} onMouseEnter={() => setHoveredIdx(i)} onMouseLeave={() => setHoveredIdx(null)} className="cursor-pointer">
                      {/* Projected FCF bar (blue background) */}
                      <rect x={cx - barWidth - 2} y={yProj} width={barWidth} height={hProj} rx="3" fill="#1455FE" opacity={isHov ? 1.0 : 0.7} />
                      {/* Discounted PV bar (emerald foreground) */}
                      <rect x={cx + 2} y={yPv} width={barWidth} height={hPv} rx="3" fill="#00D09C" opacity={isHov ? 1.0 : 0.85} />
                      {/* Year label */}
                      <text x={cx} y={svgH - 12} fill={isHov ? '#F8FAFC' : '#94A3B8'} fontSize="11" textAnchor="middle" fontFamily="monospace">
                        {p.year}
                      </text>
                    </g>
                  );
                })}
              </svg>
            </div>

            {/* Hovered bar info */}
            {hoveredIdx !== null && projections[hoveredIdx] && (
              <div className="mt-3 p-2.5 rounded-lg bg-[#1C2333] border border-[#222A3B] flex items-center justify-between text-xs font-mono">
                <span className="text-[#94A3B8]">Year: <strong className="text-[#F8FAFC]">{projections[hoveredIdx].year}</strong></span>
                <span className="text-[#1455FE]">Projected FCF: {formatB(projections[hoveredIdx].projectedFcf)}</span>
                <span className="text-[#00D09C]">Present Value: {formatB(projections[hoveredIdx].pvFcf)}</span>
                <span className="text-[#94A3B8]">Discount Factor: {projections[hoveredIdx].discountFactor}</span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tab 2: WACC vs Terminal Growth Sensitivity Heatmap */}
      {activeTab === 'sensitivityMatrix' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl overflow-x-auto">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-3">
              Intrinsic Fair Value per Share across WACC and Terminal Growth Rates
            </div>
            <table className="w-full text-xs font-mono border-collapse">
              <thead>
                <tr className="border-b border-[#222A3B]">
                  <th className="p-2 text-left text-[#64748B]">WACC \ g</th>
                  {growthSteps.map((g, i) => (
                    <th key={i} className="p-2 text-center text-[#94A3B8]">g={g.toFixed(1)}%</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {sensitivityMatrix.map((row, rIdx) => {
                  const waccVal = waccSteps[rIdx] || 8.5;
                  return (
                    <tr key={rIdx} className="border-b border-[#222A3B]/50 hover:bg-[#1C2333]/50">
                      <td className="p-2 text-[#94A3B8] font-bold">WACC={waccVal.toFixed(1)}%</td>
                      {row.map((val, cIdx) => {
                        const isUnder = val >= scorecard.currentPrice;
                        return (
                          <td key={cIdx} className={`p-2 text-center font-bold ${
                            isUnder ? 'text-[#00D09C] bg-[#00D09C]/5' : 'text-[#F04438] bg-[#F04438]/5'
                          }`}>
                            {currencySymbol}{val.toFixed(0)}
                          </td>
                        );
                      })}
                    </tr>
                  );
                })}
              </tbody>
            </table>
            <div className="mt-3 text-[11px] text-[#64748B] flex items-center justify-between">
              <span>* Green cells indicate Intrinsic Fair Value &ge; Current Market Price (${scorecard.currentPrice.toFixed(2)})</span>
              <span>Central Base: WACC={scorecard.wacc.toFixed(1)}% • g={scorecard.terminalGrowth.toFixed(1)}%</span>
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Margin of Safety Gauge */}
      {activeTab === 'marginOfSafety' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-4">
              Margin of Safety vs Institutional Buffer Hurdle (&ge; 25.0%)
            </div>

            <div className="space-y-3">
              <div>
                <div className="flex justify-between text-xs mb-1 font-mono">
                  <span className="text-[#94A3B8]">Current Price vs Fair Value Discount</span>
                  <span className={`font-bold ${isPositiveMargin ? 'text-[#00D09C]' : 'text-[#F04438]'}`}>
                    {scorecard.marginOfSafety > 0 ? '+' : ''}{scorecard.marginOfSafety.toFixed(1)}%
                  </span>
                </div>
                <div className="w-full h-4 bg-[#141923] rounded-full overflow-hidden border border-[#222A3B]">
                  <div
                    className={`h-full ${isPositiveMargin ? 'bg-[#00D09C]' : 'bg-[#F04438]'}`}
                    style={{ width: `${Math.min(100, Math.max(5, (scorecard.marginOfSafety + 30) * 1.5))}%` }}
                  ></div>
                </div>
              </div>
            </div>

            <div className="mt-4 p-3 bg-[#141923] border border-[#222A3B] rounded-lg flex items-center justify-between text-xs">
              <span className="text-[#94A3B8]">
                Benjamin Graham & Warren Buffett Rule: <strong>&ge; 25% to 30% discount</strong> below conservative intrinsic DCF value.
              </span>
              <span className={`px-2.5 py-0.5 rounded font-mono text-[11px] ${
                scorecard.marginOfSafety >= 25 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F59E0B]/10 text-[#F59E0B]'
              }`}>
                {scorecard.marginOfSafety >= 25 ? 'Deep Safety Cushion' : 'Tight / Limited Buffer'}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Tier-1 Fintech Investor Takeaway Footer */}
      <div className="mt-5 p-4 rounded-xl bg-[#0B0E14] border border-[#222A3B] flex items-start gap-3">
        <Info className="w-5 h-5 text-[#00D09C] shrink-0 mt-0.5" />
        <div className="text-xs text-[#94A3B8] leading-relaxed">
          <strong className="text-[#F8FAFC]">DCF Valuation Verdict: </strong>
          {scorecard.classification.includes('MARGIN OF SAFETY') ? (
            <span>
              The equity trades at an attractive intrinsic discount of {scorecard.marginOfSafety.toFixed(1)}% below conservative fair value ({currencySymbol}{scorecard.fairValue.toFixed(2)}), providing a robust margin of safety against potential macroeconomic growth headwinds.
            </span>
          ) : (
            <span>
              Intrinsic fair value calculated at {currencySymbol}{scorecard.fairValue.toFixed(2)} per share.
              With terminal value comprising {scorecard.tvWeightPct.toFixed(1)}% of total enterprise worth, evaluate growth durability and sensitivity to interest rate fluctuations.
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
