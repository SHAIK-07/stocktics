import React, { useState } from 'react';
import { School, ShieldCheck, Activity, BarChart3, TrendingUp, TrendingDown, Layers, PieChart, Info, Sliders, Target, Sparkles } from 'lucide-react';

export default function FactorProfileChart({ data, factorScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('factorRadar'); // 'factorRadar' | 'pillarCards' | 'cyclicalityRegimes'
  const [hoveredFactor, setHoveredFactor] = useState(null);

  const scorecard = factorScorecard || {
    ticker: 'AAPL',
    compositeScore: 78.5,
    dominantFactor: 'Quality',
    scores: {
      Value: 52,
      Quality: 96,
      Momentum: 74,
      Size: 98,
      'Low Volatility': 68,
      Profitability: 94
    },
    classification: 'TIER-1 MULTI-FACTOR COMPOUNDER'
  };

  const categories = data && data.radarCategories ? data.radarCategories : [
    'Value', 'Quality', 'Momentum', 'Size', 'Low Volatility', 'Profitability'
  ];

  const scores = data && data.scores ? data.scores : [52, 96, 74, 98, 68, 94];
  const benchmark = data && data.benchmarkScores ? data.benchmarkScores : [50, 50, 50, 50, 50, 50];

  const isHealthyScore = (scorecard.compositeScore || 0) >= 65;

  // SVG Geometry for Polar / Radar Spider Chart
  const svgSize = 360;
  const center = svgSize / 2;
  const radius = 120;
  const numVars = categories.length;
  const angleStep = (Math.PI * 2) / numVars;

  const getCoordinates = (angle, val) => {
    const r = (val / 100) * radius;
    const x = center + r * Math.sin(angle);
    const y = center - r * Math.cos(angle);
    return { x, y };
  };

  // Build polygon points for company scores
  const scorePoints = scores.map((val, i) => {
    const angle = i * angleStep;
    const { x, y } = getCoordinates(angle, val);
    return `${x},${y}`;
  }).join(' ');

  // Build polygon points for benchmark (50% neutral polygon)
  const benchPoints = benchmark.map((val, i) => {
    const angle = i * angleStep;
    const { x, y } = getCoordinates(angle, val);
    return `${x},${y}`;
  }).join(' ');

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Header & Rating Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#00D09C]/10 border border-[#00D09C]/30 flex items-center justify-center text-[#00D09C]">
            <Target className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">Quantitative Factor Profile</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 17 • Stage 4
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Decomposes systematic stock returns into Fama-French style factors: Value, Quality, Momentum, Size, Low Volatility, and Profitability.
            </p>
          </div>
        </div>

        <div className={`self-start md:self-auto px-3.5 py-1.5 rounded-xl border flex items-center gap-2 ${
          isHealthyScore
            ? 'bg-[#00D09C]/10 border-[#00D09C]/40 text-[#00D09C]'
            : 'bg-[#1455FE]/10 border-[#1455FE]/40 text-[#1455FE]'
        }`}>
          {isHealthyScore ? <ShieldCheck className="w-4 h-4" /> : <Activity className="w-4 h-4" />}
          <span className="text-xs font-bold tracking-wider">{scorecard.classification}</span>
          <span className="text-[11px] font-mono opacity-80">({scorecard.compositeScore}/100)</span>
        </div>
      </div>

      {/* 4 Core Fintech KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 my-5">
        {/* Metric 1: Composite Factor Score */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Composite Score</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              isHealthyScore ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#1455FE]/10 text-[#1455FE]'
            }`}>
              Multi-Factor
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {scorecard.compositeScore ? scorecard.compositeScore.toFixed(0) : '75'}/100
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Systematic Alpha Hurdle &ge; 65
          </div>
        </div>

        {/* Metric 2: Dominant Factor Tilt */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Primary Factor Tilt</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#00D09C]/10 text-[#00D09C]">
              Top Driver
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.dominantFactor}
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Score: {scorecard.scores ? scorecard.scores[scorecard.dominantFactor] : '90'}/100
          </div>
        </div>

        {/* Metric 3: Quality Factor (RMW) */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Quality Rank</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#1455FE]/10 text-[#1455FE]">
              RMW
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#1455FE] mt-1.5">
            {scorecard.scores ? scorecard.scores.Quality : '90'}/100
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            ROIC + Operating Margin + Low Debt
          </div>
        </div>

        {/* Metric 4: Value Factor (HML) */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Value Rank</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#1C2333] text-[#94A3B8]">
              HML
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.scores ? scorecard.scores.Value : '50'}/100
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Earnings Yield + FCF Yield
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center justify-between gap-2 border-b border-[#222A3B] pb-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('factorRadar')}
            className={`fintech-tab-btn ${activeTab === 'factorRadar' ? 'active' : ''}`}
          >
            <Target className="w-3.5 h-3.5" />
            <span>Factor Spider Radar</span>
          </button>
          <button
            onClick={() => setActiveTab('pillarCards')}
            className={`fintech-tab-btn ${activeTab === 'pillarCards' ? 'active' : ''}`}
          >
            <Sliders className="w-3.5 h-3.5" />
            <span>6 Style Pillars</span>
          </button>
          <button
            onClick={() => setActiveTab('cyclicalityRegimes')}
            className={`fintech-tab-btn ${activeTab === 'cyclicalityRegimes' ? 'active' : ''}`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>Macro Factor Regimes</span>
          </button>
        </div>
      </div>

      {/* Tab 1: Radar Spider Chart SVG */}
      {activeTab === 'factorRadar' && (
        <div className="space-y-4">
          <div className="w-full bg-[#0B0E14] border border-[#222A3B] rounded-xl p-4 flex flex-col md:flex-row items-center justify-around gap-4">
            <div className="w-[300px] h-[300px] select-none">
              <svg viewBox={`0 0 ${svgSize} ${svgSize}`} className="w-full h-full">
                {/* Concentric Web Rings (25%, 50%, 75%, 100%) */}
                {[0.25, 0.5, 0.75, 1.0].map((level, idx) => (
                  <circle
                    key={idx}
                    cx={center}
                    cy={center}
                    r={radius * level}
                    fill="none"
                    stroke="#222A3B"
                    strokeDasharray={idx === 1 ? 'none' : '3 3'}
                    strokeWidth={idx === 1 ? '1.5' : '1'}
                  />
                ))}

                {/* Axis Radial Spokes */}
                {categories.map((_, i) => {
                  const angle = i * angleStep;
                  const { x, y } = getCoordinates(angle, 100);
                  return (
                    <line key={i} x1={center} y1={center} x2={x} y2={y} stroke="#222A3B" strokeWidth="1" />
                  );
                })}

                {/* Benchmark Polygon (50th percentile market median) */}
                <polygon points={benchPoints} fill="#1455FE" fillOpacity="0.1" stroke="#1455FE" strokeWidth="1.2" strokeDasharray="3 3" />

                {/* Company Factor Tilt Polygon */}
                <polygon points={scorePoints} fill="#00D09C" fillOpacity="0.25" stroke="#00D09C" strokeWidth="2.2" />

                {/* Vertex Data Points */}
                {scores.map((val, i) => {
                  const angle = i * angleStep;
                  const { x, y } = getCoordinates(angle, val);
                  return (
                    <circle key={i} cx={x} cy={y} r="4" fill="#00D09C" stroke="#0B0E14" strokeWidth="2" />
                  );
                })}

                {/* Text Labels */}
                {categories.map((cat, i) => {
                  const angle = i * angleStep;
                  const { x, y } = getCoordinates(angle, 122);
                  return (
                    <text
                      key={i}
                      x={x}
                      y={y + 4}
                      fill="#94A3B8"
                      fontSize="10"
                      fontWeight="bold"
                      textAnchor="middle"
                      fontFamily="monospace"
                    >
                      {cat}
                    </text>
                  );
                })}
              </svg>
            </div>

            {/* Factor Scores Mini Breakdown Column */}
            <div className="w-full md:w-64 space-y-2 text-xs font-mono">
              <div className="text-[11px] text-[#64748B] uppercase font-bold tracking-wider mb-2">
                Factor Exposure Deciles
              </div>
              {categories.map((cat, i) => (
                <div key={i} className="flex items-center justify-between p-1.5 rounded bg-[#141923] border border-[#222A3B]">
                  <span className="text-[#94A3B8]">{cat}:</span>
                  <span className="text-[#00D09C] font-bold">{scores[i]}/100</span>
                </div>
              ))}
              <div className="text-[10px] text-[#64748B] pt-1">
                Dashed Blue Line: S&P 500 Benchmark (50th %ile)
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: 6 Style Pillars */}
      {activeTab === 'pillarCards' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {categories.map((cat, i) => {
            const sc = scores[i];
            return (
              <div key={i} className="p-3.5 rounded-xl bg-[#0B0E14] border border-[#222A3B]">
                <div className="flex items-center justify-between text-xs mb-2">
                  <span className="font-bold text-[#F8FAFC]">{cat}</span>
                  <span className={`font-mono font-bold px-1.5 py-0.5 rounded text-[11px] ${
                    sc >= 70 ? 'bg-[#00D09C]/10 text-[#00D09C]' : sc >= 45 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
                  }`}>
                    {sc}/100
                  </span>
                </div>
                {/* Progress bar */}
                <div className="w-full h-2 bg-[#141923] rounded-full overflow-hidden border border-[#222A3B]">
                  <div
                    className={`h-full ${sc >= 70 ? 'bg-[#00D09C]' : sc >= 45 ? 'bg-[#1455FE]' : 'bg-[#F04438]'}`}
                    style={{ width: `${sc}%` }}
                  ></div>
                </div>
                <div className="text-[10px] text-[#64748B] mt-2">
                  {cat === 'Value' && 'Trailing earnings & FCF multiple discount relative to historical universe.'}
                  {cat === 'Quality' && 'Sustained ROIC, healthy operating margins, and low leverage solvency.'}
                  {cat === 'Momentum' && 'Trailing price persistence and institutional trend continuation.'}
                  {cat === 'Size' && 'Scale advantage, institutional liquidity, and balance sheet resilience.'}
                  {cat === 'Low Volatility' && 'Downside defense, beta invariance, and minimized drawdown risk.'}
                  {cat === 'Profitability' && 'Discretionary cash flow generation and conservative capital reinvestment.'}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Tab 3: Macro Factor Regimes */}
      {activeTab === 'cyclicalityRegimes' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl text-xs space-y-3">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-2">
              Factor Performance Across Macroeconomic Regimes
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 font-mono">
              <div className="p-3 bg-[#141923] border border-[#222A3B] rounded-lg">
                <span className="text-[#00D09C] font-bold">1. Early Expansion:</span>
                <p className="text-[11px] text-[#94A3B8] mt-1 font-sans">
                  Deep Value, High Beta, and Small Size lead out of market troughs as liquidity eases.
                </p>
              </div>
              <div className="p-3 bg-[#141923] border border-[#222A3B] rounded-lg">
                <span className="text-[#1455FE] font-bold">2. Late Expansion:</span>
                <p className="text-[11px] text-[#94A3B8] mt-1 font-sans">
                  Momentum and Quality dominate as growth becomes scarcer and capital concentrates in market winners.
                </p>
              </div>
              <div className="p-3 bg-[#141923] border border-[#222A3B] rounded-lg">
                <span className="text-[#F04438] font-bold">3. Contraction / Recession:</span>
                <p className="text-[11px] text-[#94A3B8] mt-1 font-sans">
                  Low Volatility, High Quality (low debt), and Dividends preserve capital as speculative beta collapses.
                </p>
              </div>
              <div className="p-3 bg-[#141923] border border-[#222A3B] rounded-lg">
                <span className="text-[#F59E0B] font-bold">4. Recovery:</span>
                <p className="text-[11px] text-[#94A3B8] mt-1 font-sans">
                  Factor mean-reversion sparks powerful rotation from crowded defensive names back into cyclical value.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tier-1 Fintech Investor Takeaway Footer */}
      <div className="mt-5 p-4 rounded-xl bg-[#0B0E14] border border-[#222A3B] flex items-start gap-3">
        <Info className="w-5 h-5 text-[#00D09C] shrink-0 mt-0.5" />
        <div className="text-xs text-[#94A3B8] leading-relaxed">
          <strong className="text-[#F8FAFC]">Quantitative Style Verdict: </strong>
          <span>
            {scorecard.ticker} displays a powerful <strong>{scorecard.dominantFactor} tilt</strong> with an institutional composite score of {scorecard.compositeScore ? scorecard.compositeScore.toFixed(0) : '75'}/100.
            Factor diversification protects against single-style drawdowns while harvesting systematic long-term equity premiums.
          </span>
        </div>
      </div>
    </div>
  );
}
