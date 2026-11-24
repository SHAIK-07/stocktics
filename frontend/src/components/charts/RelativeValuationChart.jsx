import React, { useState } from 'react';
import { BarChart3, ShieldCheck, Activity, TrendingUp, TrendingDown, Layers, Users, Info, AlertTriangle, ArrowRight } from 'lucide-react';

export default function RelativeValuationChart({ data, relativeScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('peerComparison'); // 'peerComparison' | 'historicalCorridor' | 'greatCompanyTrap'
  const [hoveredPeer, setHoveredPeer] = useState(null);

  const scorecard = relativeScorecard || {
    ticker: 'AAPL',
    trailingPe: 33.5,
    peerMedianPe: 26.0,
    peerPePremium: 28.8,
    historicalPercentile: 72.0,
    relativeEvEbitdaSpread: 22.5,
    relativePeg: 1.85,
    score: 65.0,
    classification: 'QUALITY GROWTH BIAS (JUSTIFIED PREMIUM)'
  };

  const peers = data && data.peers && data.peers.length > 0 ? data.peers : [
    { ticker: 'AAPL', pe: 33.5, evEbitda: 24.2, ps: 8.7, growth: 8.0 },
    { ticker: 'MSFT', pe: 35.2, evEbitda: 23.5, ps: 12.1, growth: 15.0 },
    { ticker: 'GOOGL', pe: 23.9, evEbitda: 17.5, ps: 6.8, growth: 14.0 },
    { ticker: 'PEER_MEDIAN', pe: 26.0, evEbitda: 18.0, ps: 7.2, growth: 12.0 },
    { ticker: 'SECTOR_MIN', pe: 16.5, evEbitda: 11.2, ps: 3.1, growth: 4.0 }
  ];

  const corridor = data && data.historicalCorridor ? data.historicalCorridor : {
    minPe: 18.2,
    p25Pe: 23.5,
    medianPe: 27.8,
    p75Pe: 34.0,
    maxPe: 38.5,
    currentPe: 33.5,
    percentile: 72.0
  };

  const isHealthyScore = (scorecard.score || 0) >= 65;
  const isPeerDiscount = (scorecard.peerPePremium || 0) <= 0;

  // SVG Geometry for Peer Multiple Comparison Bar Chart
  const svgW = 640;
  const svgH = 240;
  const padL = 60;
  const padR = 25;
  const padT = 30;
  const padB = 40;
  const plotW = svgW - padL - padR;
  const plotH = svgH - padT - padB;

  const maxPe = Math.max(40, ...peers.map(p => p.pe)) * 1.15;
  const barSlotW = plotW / peers.length;
  const barW = barSlotW * 0.45;

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Header & Rating Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#1455FE]/10 border border-[#1455FE]/30 flex items-center justify-center text-[#1455FE]">
            <Users className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">Relative Valuation & Peer Benchmarking</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 16 • Stage 3
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Audits peer group multiples, 5-year historical valuation corridor percentiles, and growth-adjusted metrics.
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
          <span className="text-[11px] font-mono opacity-80">({scorecard.score}/100)</span>
        </div>
      </div>

      {/* 4 Core Fintech KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 my-5">
        {/* Metric 1: Trailing P/E vs Peer Median */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Trailing P/E vs Peers</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              isPeerDiscount ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#1455FE]/10 text-[#1455FE]'
            }`}>
              {isPeerDiscount ? 'Discount' : 'Premium'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.trailingPe ? scorecard.trailingPe.toFixed(1) : '0.0'}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Peer Median: {scorecard.peerMedianPe ? scorecard.peerMedianPe.toFixed(1) : '0.0'}x
          </div>
        </div>

        {/* Metric 2: Peer Premium / Discount % */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Peer Premium / Discount</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              isPeerDiscount ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              Spread
            </span>
          </div>
          <div className={`text-xl font-bold font-mono mt-1.5 ${isPeerDiscount ? 'text-[#00D09C]' : 'text-[#F04438]'}`}>
            {scorecard.peerPePremium > 0 ? '+' : ''}{scorecard.peerPePremium ? scorecard.peerPePremium.toFixed(1) : '0.0'}%
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            EV/EBITDA Spread: {scorecard.relativeEvEbitdaSpread > 0 ? '+' : ''}{scorecard.relativeEvEbitdaSpread ? scorecard.relativeEvEbitdaSpread.toFixed(1) : '0.0'}%
          </div>
        </div>

        {/* Metric 3: 5-Year Historical Percentile */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>5Y Hist. Percentile</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.historicalPercentile <= 50 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#1455FE]/10 text-[#1455FE]'
            }`}>
              Corridor Rank
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#1455FE] mt-1.5">
            {scorecard.historicalPercentile ? scorecard.historicalPercentile.toFixed(0) : '50'}th %ile
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Historical Median: {corridor.medianPe.toFixed(1)}x
          </div>
        </div>

        {/* Metric 4: Growth-Adjusted PEG Relative */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Relative PEG</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.relativePeg <= 1.5 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              GARP Rule
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {scorecard.relativePeg ? scorecard.relativePeg.toFixed(2) : '1.50'}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Target Hurdle: &lt; 1.50x
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center justify-between gap-2 border-b border-[#222A3B] pb-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('peerComparison')}
            className={`fintech-tab-btn ${activeTab === 'peerComparison' ? 'active' : ''}`}
          >
            <BarChart3 className="w-3.5 h-3.5" />
            <span>Cross-Sectional Peers</span>
          </button>
          <button
            onClick={() => setActiveTab('historicalCorridor')}
            className={`fintech-tab-btn ${activeTab === 'historicalCorridor' ? 'active' : ''}`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>5-Year Valuation Corridor</span>
          </button>
          <button
            onClick={() => setActiveTab('greatCompanyTrap')}
            className={`fintech-tab-btn ${activeTab === 'greatCompanyTrap' ? 'active' : ''}`}
          >
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>Great Company ≠ Great Investment</span>
          </button>
        </div>
      </div>

      {/* Tab 1: Peer Multiples Bar Chart SVG */}
      {activeTab === 'peerComparison' && (
        <div className="space-y-4">
          <div className="w-full bg-[#0B0E14] border border-[#222A3B] rounded-xl p-4">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-2">
              Cross-Sectional Peer P/E Multiple Comparison
            </div>
            <div className="w-full overflow-x-auto">
              <svg viewBox={`0 0 ${svgW} ${svgH}`} className="w-full h-auto select-none">
                {/* Horizontal Grid lines */}
                {[0, 0.25, 0.5, 0.75, 1.0].map((ratio, i) => {
                  const y = padT + plotH * (1 - ratio);
                  const val = (ratio * maxPe).toFixed(1);
                  return (
                    <g key={i}>
                      <line x1={padL} y1={y} x2={svgW - padR} y2={y} stroke="#222A3B" strokeDasharray="3 3" />
                      <text x={padL - 8} y={y + 3} fill="#64748B" fontSize="10" textAnchor="end" fontFamily="monospace">
                        {val}x
                      </text>
                    </g>
                  );
                })}

                {/* Peer Median benchmark horizontal line */}
                <line
                  x1={padL}
                  y1={padT + plotH - ((scorecard.peerMedianPe / maxPe) * plotH)}
                  x2={svgW - padR}
                  y2={padT + plotH - ((scorecard.peerMedianPe / maxPe) * plotH)}
                  stroke="#1455FE"
                  strokeWidth="1.5"
                  strokeDasharray="4 4"
                />

                {/* Bars for each peer */}
                {peers.map((p, i) => {
                  const cx = padL + i * barSlotW + barSlotW / 2;
                  const h = (p.pe / maxPe) * plotH;
                  const y = padT + plotH - h;
                  const isTarget = p.ticker === scorecard.ticker;
                  const isHov = hoveredPeer === i;

                  return (
                    <g key={i} onMouseEnter={() => setHoveredPeer(i)} onMouseLeave={() => setHoveredPeer(null)} className="cursor-pointer">
                      <rect
                        x={cx - barW / 2}
                        y={y}
                        width={barW}
                        height={h}
                        rx="4"
                        fill={isTarget ? '#00D09C' : '#1C2333'}
                        stroke={isTarget ? '#00D09C' : isHov ? '#1455FE' : '#222A3B'}
                        strokeWidth="1.5"
                      />
                      <text x={cx} y={y - 6} fill={isTarget ? '#00D09C' : '#F8FAFC'} fontSize="10" textAnchor="middle" fontFamily="monospace" fontWeight="bold">
                        {p.pe.toFixed(1)}x
                      </text>
                      <text x={cx} y={svgH - 12} fill={isTarget ? '#00D09C' : '#94A3B8'} fontSize="11" textAnchor="middle" fontFamily="monospace">
                        {p.ticker}
                      </text>
                    </g>
                  );
                })}
              </svg>
            </div>

            {/* Legend / Info Pill */}
            <div className="mt-3 p-2.5 rounded-lg bg-[#1C2333] border border-[#222A3B] flex items-center justify-between text-xs font-mono">
              <span className="text-[#94A3B8]">Dashed Blue Line: Sector Peer Median ({scorecard.peerMedianPe.toFixed(1)}x)</span>
              <span className="text-[#00D09C]">Green Bar: Target Stock ({scorecard.ticker})</span>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: 5-Year Valuation Corridor Band */}
      {activeTab === 'historicalCorridor' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-4">
              5-Year Historical P/E Multiple Trading Corridor ({corridor.minPe.toFixed(1)}x to {corridor.maxPe.toFixed(1)}x)
            </div>

            {/* Visual Corridor Bar */}
            <div className="relative pt-6 pb-8">
              {/* Main Range Track */}
              <div className="w-full h-4 bg-[#141923] rounded-full overflow-hidden border border-[#222A3B] relative">
                {/* 25th to 75th percentile core zone */}
                <div
                  className="absolute h-full bg-[#1455FE]/30 border-x border-[#1455FE]/60"
                  style={{
                    left: `${((corridor.p25Pe - corridor.minPe) / (corridor.maxPe - corridor.minPe)) * 100}%`,
                    width: `${((corridor.p75Pe - corridor.p25Pe) / (corridor.maxPe - corridor.minPe)) * 100}%`
                  }}
                ></div>
              </div>

              {/* Current P/E Indicator Marker */}
              <div
                className="absolute top-2 -translate-x-1/2 flex flex-col items-center"
                style={{
                  left: `${Math.min(95, Math.max(5, ((corridor.currentPe - corridor.minPe) / (corridor.maxPe - corridor.minPe)) * 100))}%`
                }}
              >
                <span className="text-[11px] font-mono font-bold text-[#00D09C] bg-[#1C2333] px-2 py-0.5 rounded border border-[#00D09C]">
                  Current: {corridor.currentPe.toFixed(1)}x
                </span>
                <div className="w-0.5 h-6 bg-[#00D09C] mt-0.5"></div>
              </div>

              {/* Ticks along bottom */}
              <div className="flex justify-between text-[11px] font-mono text-[#64748B] mt-2">
                <span>5Y Min: {corridor.minPe.toFixed(1)}x</span>
                <span>25th %ile: {corridor.p25Pe.toFixed(1)}x</span>
                <span className="text-[#1455FE] font-bold">5Y Median: {corridor.medianPe.toFixed(1)}x</span>
                <span>75th %ile: {corridor.p75Pe.toFixed(1)}x</span>
                <span>5Y Peak: {corridor.maxPe.toFixed(1)}x</span>
              </div>
            </div>

            <div className="p-3 bg-[#141923] border border-[#222A3B] rounded-lg flex items-center justify-between text-xs">
              <span className="text-[#94A3B8]">
                Currently priced at the <strong>{corridor.percentile.toFixed(0)}th percentile</strong> of its multi-year range.
              </span>
              <span className={`px-2 py-0.5 rounded font-mono text-[11px] ${
                corridor.percentile <= 50 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#1455FE]/10 text-[#1455FE]'
              }`}>
                {corridor.percentile <= 50 ? 'Historical Value Zone' : 'Upper Half Valuation'}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: The Great Company Trap */}
      {activeTab === 'greatCompanyTrap' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
            <div className="flex items-center gap-2 text-xs font-bold text-[#F59E0B] uppercase tracking-wider mb-2">
              <AlertTriangle className="w-4 h-4" />
              The Great Company ≠ Great Investment Paradigm
            </div>
            <p className="text-xs text-[#94A3B8] leading-relaxed">
              In March 2000, <strong>Cisco Systems (CSCO)</strong> was globally recognized as one of the highest quality companies on Earth, growing revenues 40%+ with monopolistic routers.
              However, because investors paid <strong>130x P/E</strong>, multiple contraction annihilated investor returns: the stock took over 20 years just to recover its nominal peak.
            </p>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-3 text-xs font-mono">
              <div className="p-3 rounded-lg bg-[#141923] border border-[#222A3B]">
                <div className="text-[#1455FE] font-bold mb-1">Cisco Peak (March 2000):</div>
                <div className="text-[#94A3B8]">Trailing P/E: 130x</div>
                <div className="text-[#94A3B8]">Price-to-Sales: 35x</div>
                <div className="text-[#F04438] mt-1">20-Year Return: ~0% Annualized</div>
              </div>
              <div className="p-3 rounded-lg bg-[#141923] border border-[#222A3B]">
                <div className="text-[#00D09C] font-bold mb-1">The Institutional Math:</div>
                <div className="text-[#94A3B8]">$$\text&#123;Total Return&#125; = \text&#123;EPS Growth&#125; \pm \Delta\text&#123;Multiple&#125; + \text&#123;Div&#125;$$</div>
                <div className="text-[#00D09C] mt-1">Multiple contraction overrides business quality!</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tier-1 Fintech Investor Takeaway Footer */}
      <div className="mt-5 p-4 rounded-xl bg-[#0B0E14] border border-[#222A3B] flex items-start gap-3">
        <Info className="w-5 h-5 text-[#00D09C] shrink-0 mt-0.5" />
        <div className="text-xs text-[#94A3B8] leading-relaxed">
          <strong className="text-[#F8FAFC]">Relative Valuation Verdict: </strong>
          {scorecard.peerPePremium <= 15 ? (
            <span>
              Trades aligned with peer group medians ({scorecard.trailingPe.toFixed(1)}x vs {scorecard.peerMedianPe.toFixed(1)}x peer median), offering balanced relative risk with no excessive bubble premium.
            </span>
          ) : (
            <span>
              Trades at a {scorecard.peerPePremium.toFixed(1)}% multiple premium to industry peers. Verify that superior ROIC and organic pricing power fully justify the premium multiple to mitigate contraction risk.
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
