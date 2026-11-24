import React, { useState } from 'react';
import { Calculator, TrendingUp, TrendingDown, ShieldCheck, AlertTriangle, Layers, BarChart3, Activity, ArrowDownRight, ArrowUpRight } from 'lucide-react';

export default function PeValuationChart({ data, peScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('peBands'); // 'peBands' | 'earningsYield' | 'pegMatrix'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  const scorecard = peScorecard || {
    ticker: 'GOOGL',
    currentPrice: 192.20,
    ttmEps: 8.04,
    forwardEps: 9.80,
    trailingPe: 23.9,
    forwardPe: 19.6,
    earningsYield: 4.18,
    treasuryYield: 4.25,
    equityRiskPremium: -0.07,
    pegRatio: 1.09,
    growthRate: 22.0,
    forwardCompressionPct: 18.0,
    score: 78.5,
    classification: 'FAIR VALUE SECULAR COMPOUNDER'
  };

  const chartData = data && data.length > 0 ? data : [
    { year: '2020', eps: 2.93, pe15: 43.95, pe20: 58.60, pe25: 73.25, pe30: 87.90, stockPrice: 87.63, trailingPe: 29.9 },
    { year: '2021', eps: 5.61, pe15: 84.15, pe20: 112.20, pe25: 140.25, pe30: 168.30, stockPrice: 144.68, trailingPe: 25.8 },
    { year: '2022', eps: 4.56, pe15: 68.40, pe20: 91.20, pe25: 114.00, pe30: 136.80, stockPrice: 88.73, trailingPe: 19.5 },
    { year: '2023', eps: 5.80, pe15: 87.00, pe20: 116.00, pe25: 145.00, pe30: 174.00, stockPrice: 140.93, trailingPe: 24.3 },
    { year: '2024', eps: 8.04, pe15: 120.60, pe20: 160.80, pe25: 201.00, pe30: 241.20, stockPrice: 192.20, trailingPe: 23.9 }
  ];

  const isBargain = scorecard.pegRatio <= 1.2 && scorecard.equityRiskPremium >= 0;
  const isFair = scorecard.pegRatio <= 2.0;

  // SVG Chart Geometry
  const width = 640;
  const height = 250;
  const padLeft = 55;
  const padRight = 35;
  const padTop = 30;
  const padBottom = 40;
  const plotW = width - padLeft - padRight;
  const plotH = height - padTop - padBottom;

  const allPrices = chartData.flatMap(d => [d.pe15, d.pe20, d.pe25, d.pe30, d.stockPrice]);
  const minVal = Math.max(0, Math.min(...allPrices) * 0.85);
  const maxVal = Math.max(...allPrices) * 1.1;
  const range = (maxVal - minVal) || 1;

  const getX = (idx) => padLeft + (idx / (chartData.length - 1 || 1)) * plotW;
  const getY = (val) => padTop + plotH - (((val - minVal) / range) * plotH);

  // Corridor Paths
  const pe15Points = chartData.map((d, i) => `${getX(i)},${getY(d.pe15)}`);
  const pe20Points = chartData.map((d, i) => `${getX(i)},${getY(d.pe20)}`);
  const pe25Points = chartData.map((d, i) => `${getX(i)},${getY(d.pe25)}`);
  const pe30Points = chartData.map((d, i) => `${getX(i)},${getY(d.pe30)}`);

  const reversePoints = (pts) => pts.slice().reverse();

  // Band Area 1: 15x to 20x (Value Channel)
  const band15_20D = `M ${pe15Points.join(' L ')} L ${reversePoints(pe20Points).join(' L ')} Z`;
  // Band Area 2: 20x to 25x (Fair Value Channel)
  const band20_25D = `M ${pe20Points.join(' L ')} L ${reversePoints(pe25Points).join(' L ')} Z`;
  // Band Area 3: 25x to 30x (Growth Premium Channel)
  const band25_30D = `M ${pe25Points.join(' L ')} L ${reversePoints(pe30Points).join(' L ')} Z`;

  // Stock Price Line
  const pricePathD = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.stockPrice)}`).join(' ');

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Header & Rating Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#00D09C]/10 border border-[#00D09C]/30 flex items-center justify-center text-[#00D09C]">
            <Calculator className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">P/E Ratio & Earnings Yield Engine</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 12 • Stage 3
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Audits historical P/E bands, consensus forward multiples, bond-equivalent Earnings Yield ($E/P$), and Peter Lynch PEG ratio.
            </p>
          </div>
        </div>

        <div className={`self-start md:self-auto px-3.5 py-1.5 rounded-xl border flex items-center gap-2 ${
          isBargain
            ? 'bg-[#00D09C]/10 border-[#00D09C]/40 text-[#00D09C]'
            : isFair
            ? 'bg-[#1455FE]/10 border-[#1455FE]/40 text-[#1455FE]'
            : 'bg-[#F04438]/10 border-[#F04438]/40 text-[#F04438]'
        }`}>
          {isBargain ? (
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
        {/* Metric 1: Trailing P/E */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Trailing P/E (TTM)</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.trailingPe <= 22 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.trailingPe <= 32 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.trailingPe <= 22 ? 'Value' : scorecard.trailingPe <= 32 ? 'Fair' : 'Premium'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.trailingPe.toFixed(1)}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            TTM EPS: {currencySymbol}{scorecard.ttmEps.toFixed(2)}
          </div>
        </div>

        {/* Metric 2: Forward P/E */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Forward P/E (NTM)</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.forwardCompressionPct >= 10 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#1C2333] text-[#94A3B8]'
            }`}>
              {scorecard.forwardCompressionPct >= 0 ? '-' : '+'}{Math.abs(scorecard.forwardCompressionPct).toFixed(1)}% Comp
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#1455FE] mt-1.5">
            {scorecard.forwardPe.toFixed(1)}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Forward EPS: {currencySymbol}{scorecard.forwardEps.toFixed(2)}
          </div>
        </div>

        {/* Metric 3: Earnings Yield */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Earnings Yield (E/P)</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.equityRiskPremium >= 0 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.equityRiskPremium >= 0 ? '+' : ''}{scorecard.equityRiskPremium.toFixed(2)}% ERP
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {scorecard.earningsYield.toFixed(2)}%
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            10Y US Treasury: {scorecard.treasuryYield.toFixed(2)}%
          </div>
        </div>

        {/* Metric 4: Peter Lynch PEG Ratio */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Peter Lynch PEG</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.pegRatio <= 1.2 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.pegRatio <= 2.0 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.pegRatio <= 1.2 ? 'GARP' : scorecard.pegRatio <= 2.0 ? 'Fair' : 'Stretched'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.pegRatio.toFixed(2)}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Growth Rate: {scorecard.growthRate.toFixed(1)}% CAGR
          </div>
        </div>
      </div>

      {/* View Switcher Tabs */}
      <div className="flex items-center justify-between gap-2 border-b border-[#222A3B] pb-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('peBands')}
            className={`fintech-tab-btn ${activeTab === 'peBands' ? 'active' : ''}`}
          >
            <BarChart3 className="w-3.5 h-3.5" />
            <span>Historical P/E Bands</span>
          </button>
          <button
            onClick={() => setActiveTab('earningsYield')}
            className={`fintech-tab-btn ${activeTab === 'earningsYield' ? 'active' : ''}`}
          >
            <Calculator className="w-3.5 h-3.5" />
            <span>Earnings Yield vs 10Y Yield</span>
          </button>
          <button
            onClick={() => setActiveTab('pegMatrix')}
            className={`fintech-tab-btn ${activeTab === 'pegMatrix' ? 'active' : ''}`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>Peter Lynch PEG Matrix</span>
          </button>
        </div>

        {/* Legend for P/E Bands */}
        {activeTab === 'peBands' && (
          <div className="hidden sm:flex items-center gap-3 text-xs font-medium text-[#94A3B8]">
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded-full bg-[#F8FAFC]"></span>
              <span className="text-[#F8FAFC]">Actual Price</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#1455FE]"></span>
              <span>15x</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#00D09C]"></span>
              <span>20x</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#F59E0B]"></span>
              <span>25x</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#F04438]"></span>
              <span>30x</span>
            </div>
          </div>
        )}
      </div>

      {/* Main Tab 1: Historical P/E Valuation Bands SVG */}
      {activeTab === 'peBands' && (
        <div className="relative w-full overflow-hidden">
          <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-auto select-none overflow-visible">
            <defs>
              <linearGradient id="band15_20Grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#1455FE" stopOpacity="0.15" />
                <stop offset="100%" stopColor="#1455FE" stopOpacity="0.05" />
              </linearGradient>
              <linearGradient id="band20_25Grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#00D09C" stopOpacity="0.15" />
                <stop offset="100%" stopColor="#00D09C" stopOpacity="0.05" />
              </linearGradient>
              <linearGradient id="band25_30Grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#F59E0B" stopOpacity="0.15" />
                <stop offset="100%" stopColor="#F59E0B" stopOpacity="0.05" />
              </linearGradient>
            </defs>

            {/* Gridlines */}
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
                    x={padLeft - 8}
                    y={y + 3}
                    fill="#64748B"
                    fontSize="10"
                    fontFamily="monospace"
                    textAnchor="end"
                  >
                    {currencySymbol}{val.toFixed(0)}
                  </text>
                </g>
              );
            })}

            {/* Shaded Channel Bands */}
            <path d={band15_20D} fill="url(#band15_20Grad)" />
            <path d={band20_25D} fill="url(#band20_25Grad)" />
            <path d={band25_30D} fill="url(#band25_30Grad)" />

            {/* Multiple Contour Lines */}
            <path d={`M ${pe15Points.join(' L ')}`} fill="none" stroke="#1455FE" strokeWidth="1.2" strokeDasharray="3 3" opacity="0.8" />
            <path d={`M ${pe20Points.join(' L ')}`} fill="none" stroke="#00D09C" strokeWidth="1.2" strokeDasharray="3 3" opacity="0.8" />
            <path d={`M ${pe25Points.join(' L ')}`} fill="none" stroke="#F59E0B" strokeWidth="1.2" strokeDasharray="3 3" opacity="0.8" />
            <path d={`M ${pe30Points.join(' L ')}`} fill="none" stroke="#F04438" strokeWidth="1.2" strokeDasharray="3 3" opacity="0.8" />

            {/* Actual Stock Price Path */}
            <path
              d={pricePathD}
              fill="none"
              stroke="#F8FAFC"
              strokeWidth="3.2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />

            {/* Data Points and Interaction Targets */}
            {chartData.map((d, idx) => {
              const x = getX(idx);
              const yPrice = getY(d.stockPrice);
              const isHovered = hoveredIdx === idx;

              return (
                <g key={idx}>
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

                  {/* Stock Price Node */}
                  <circle
                    cx={x}
                    cy={yPrice}
                    r={isHovered ? 6 : 4}
                    fill="var(--fintech-background, #0B0E14)"
                    stroke="var(--text-primary, #F8FAFC)"
                    strokeWidth="2.5"
                    className="transition-all duration-150"
                  />

                  {/* X-axis Year Label */}
                  <text
                    x={x}
                    y={padTop + plotH + 20}
                    fill={isHovered ? 'var(--fintech-primary, #00D09C)' : 'var(--text-secondary, #94A3B8)'}
                    fontSize="11"
                    fontWeight={isHovered ? 'bold' : 'normal'}
                    fontFamily="monospace"
                    textAnchor="middle"
                  >
                    {d.year}
                  </text>

                  {/* Touch/Hover Target */}
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

          {/* Multi-Year P/E Breakdown Cards Strip */}
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
                    <span style={{ fontSize: '11px', color: 'var(--fintech-primary)', fontWeight: '800', fontFamily: 'var(--font-mono)' }}>
                      {d.trailingPe}x P/E
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-primary)' }}>
                      <span>Price:</span>
                      <strong>{currencySymbol}{d.stockPrice.toFixed(2)}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                      <span>EPS:</span>
                      <strong>{currencySymbol}{d.eps.toFixed(2)}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-tertiary)' }}>
                      <span>20x Target:</span>
                      <strong>{currencySymbol}{d.pe20.toFixed(2)}</strong>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Tab 2: Earnings Yield vs 10Y US Treasury Risk-Free Rate */}
      {activeTab === 'earningsYield' && (
        <div className="bg-[#0B0E14] border border-[#222A3B] rounded-xl p-5 my-2">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold text-[#F8FAFC]">Earnings Yield vs Risk-Free Treasury Hurdle</span>
            <span className={`text-xs font-mono font-bold ${
              scorecard.equityRiskPremium >= 0 ? 'text-[#00D09C]' : 'text-[#F04438]'
            }`}>
              Equity Risk Premium: {scorecard.equityRiskPremium >= 0 ? '+' : ''}{scorecard.equityRiskPremium.toFixed(2)}%
            </span>
          </div>

          <div className="space-y-3 my-4">
            {/* Stock Earnings Yield Bar */}
            <div>
              <div className="flex items-center justify-between text-xs mb-1 font-mono">
                <span className="text-[#94A3B8]">{scorecard.ticker} Earnings Yield (E/P):</span>
                <span className="font-bold text-[#00D09C]">{scorecard.earningsYield.toFixed(2)}%</span>
              </div>
              <div className="w-full h-3.5 bg-[#1C2333] rounded-full overflow-hidden flex">
                <div
                  className="h-full bg-[#00D09C] rounded-full transition-all duration-500"
                  style={{ width: `${Math.min(100, (scorecard.earningsYield / 8.0) * 100)}%` }}
                />
              </div>
            </div>

            {/* 10Y Treasury Benchmark Bar */}
            <div>
              <div className="flex items-center justify-between text-xs mb-1 font-mono">
                <span className="text-[#94A3B8]">10-Year US Treasury Benchmark (Risk-Free):</span>
                <span className="font-bold text-[#1455FE]">{scorecard.treasuryYield.toFixed(2)}%</span>
              </div>
              <div className="w-full h-3.5 bg-[#1C2333] rounded-full overflow-hidden flex">
                <div
                  className="h-full bg-[#1455FE] rounded-full transition-all duration-500"
                  style={{ width: `${Math.min(100, (scorecard.treasuryYield / 8.0) * 100)}%` }}
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs text-[#94A3B8] mt-4 pt-3 border-t border-[#222A3B]">
            <div>
              <span className="font-bold text-[#F8FAFC] block mb-0.5">The Earnings Yield Reciprocal (1 / (P/E)):</span>
              At {scorecard.trailingPe.toFixed(1)}x P/E, every $100 invested generates ${scorecard.earningsYield.toFixed(2)} in annual net income return on capital.
            </div>
            <div>
              <span className="font-bold text-[#F8FAFC] block mb-0.5">Equity Risk Premium Verdict:</span>
              {scorecard.equityRiskPremium >= 0 ? (
                <span className="text-[#00D09C]">
                  Positive ERP (+{scorecard.equityRiskPremium.toFixed(2)}%): Equities provide surplus return over guaranteed government bonds.
                </span>
              ) : (
                <span className="text-[#F04438]">
                  Negative ERP ({scorecard.equityRiskPremium.toFixed(2)}%): Equities trade at an aggressive valuation; requires strong EPS compounding to justify holding volatility.
                </span>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Peter Lynch PEG Growth Matrix */}
      {activeTab === 'pegMatrix' && (
        <div className="bg-[#0B0E14] border border-[#222A3B] rounded-xl p-4 my-2">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold text-[#F8FAFC]">Peter Lynch PEG Growth Spectrum ((P/E) / g)</span>
            <span className="text-xs font-mono font-bold text-[#00D09C]">
              {scorecard.pegRatio.toFixed(2)}x PEG @ {scorecard.growthRate.toFixed(1)}% Growth
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-2.5 my-3">
            <div className={`p-3 rounded-xl border ${
              scorecard.pegRatio < 1.0 ? 'border-[#00D09C] bg-[#00D09C]/10' : 'border-[#222A3B] bg-[#1C2333]'
            }`}>
              <div className="text-xs font-bold text-[#00D09C]">&lt; 1.0x PEG</div>
              <div className="text-[11px] font-medium text-[#F8FAFC] mt-0.5">Undervalued Bargain</div>
              <div className="text-[10px] text-[#94A3B8] mt-1">Growth rate exceeds valuation multiple. Deep margin of safety.</div>
            </div>

            <div className={`p-3 rounded-xl border ${
              scorecard.pegRatio >= 1.0 && scorecard.pegRatio <= 1.8 ? 'border-[#00D09C] bg-[#00D09C]/10' : 'border-[#222A3B] bg-[#1C2333]'
            }`}>
              <div className="text-xs font-bold text-[#00D09C]">1.0x – 1.8x PEG</div>
              <div className="text-[11px] font-medium text-[#F8FAFC] mt-0.5">Growth At Fair Price (GARP)</div>
              <div className="text-[10px] text-[#94A3B8] mt-1">Standard corridor for high-ROIC compounders like Alphabet and Apple.</div>
            </div>

            <div className={`p-3 rounded-xl border ${
              scorecard.pegRatio > 1.8 && scorecard.pegRatio <= 2.5 ? 'border-[#F59E0B] bg-[#F59E0B]/10' : 'border-[#222A3B] bg-[#1C2333]'
            }`}>
              <div className="text-xs font-bold text-[#F59E0B]">1.8x – 2.5x PEG</div>
              <div className="text-[11px] font-medium text-[#F8FAFC] mt-0.5">Elevated Premium</div>
              <div className="text-[10px] text-[#94A3B8] mt-1">Requires flawless execution; vulnerable to growth deceleration shocks.</div>
            </div>

            <div className={`p-3 rounded-xl border ${
              scorecard.pegRatio > 2.5 ? 'border-[#F04438] bg-[#F04438]/10' : 'border-[#222A3B] bg-[#1C2333]'
            }`}>
              <div className="text-xs font-bold text-[#F04438]">&gt; 2.5x PEG</div>
              <div className="text-[11px] font-medium text-[#F8FAFC] mt-0.5">Overstretched Multiples</div>
              <div className="text-[10px] text-[#94A3B8] mt-1">Severe multiple compression hazard if growth mean-reverts.</div>
            </div>
          </div>
        </div>
      )}

      {/* Institutional Audit Summary Card */}
      <div className="mt-4 p-4 rounded-xl bg-[#1C2333]/70 border border-[#222A3B] flex flex-col md:flex-row items-start md:items-center justify-between gap-3 text-xs">
        <div className="space-y-1">
          <div className="font-bold text-[#F8FAFC] flex items-center gap-2">
            <span>Institutional Valuation Verdict:</span>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#00D09C]/10 text-[#00D09C] border border-[#00D09C]/30">
              {scorecard.classification}
            </span>
          </div>
          <p className="text-[#94A3B8]">
            Trading at {scorecard.trailingPe.toFixed(1)}x trailing earnings with a {scorecard.pegRatio.toFixed(2)}x PEG ratio. Forward estimates indicate a {scorecard.forwardCompressionPct >= 0 ? '-' : '+'}{Math.abs(scorecard.forwardCompressionPct).toFixed(1)}% compression to {scorecard.forwardPe.toFixed(1)}x as consensus net income materializes.
          </p>
        </div>

        <div className="shrink-0 flex items-center gap-2">
          <span className="text-[11px] font-mono text-[#64748B]">Forward Multiple:</span>
          <span className="font-mono font-bold text-[#00D09C] px-2 py-1 rounded bg-[#0B0E14] border border-[#222A3B]">
            {scorecard.forwardPe.toFixed(1)}x FWD P/E
          </span>
        </div>
      </div>
    </div>
  );
}
