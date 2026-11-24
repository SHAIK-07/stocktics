import React, { useState } from 'react';
import { Coins, ShieldCheck, Activity, BarChart3, TrendingUp, TrendingDown, ArrowDownRight, DollarSign, PieChart, Info, Landmark, Layers } from 'lucide-react';

export default function FcfValuationChart({ data, fcfScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('cashFunnel'); // 'cashFunnel' | 'spreadGauge' | 'fcfHistory'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  const scorecard = fcfScorecard || {
    ticker: 'AAPL',
    currentPrice: 224.50,
    marketCap: 3400000000000,
    operatingCashFlow: 118000000000,
    capex: 9500000000,
    freeCashFlow: 108500000000,
    netIncome: 93700000000,
    fcfYield: 3.19,
    priceToFcf: 31.3,
    evToFcf: 31.7,
    conversionRatio: 115.8,
    ownerEarnings: 105000000000,
    ownerEarningsYield: 3.09,
    fcfSpreadVsTreasury: -1.06,
    score: 82.0,
    classification: 'TIER-1 DISCRETIONARY CASH MACHINE'
  };

  const chartData = Array.isArray(data) && data.length > 0
    ? data.map(d => ({
        period: String(d.year || d.period || '2024'),
        operatingCashFlow: d.cfo ?? d.operatingCashFlow ?? 0,
        capex: d.capex ?? 0,
        freeCashFlow: d.fcf ?? d.freeCashFlow ?? 0,
        netIncome: d.netIncome ?? 0,
        conversionRatio: d.conversionPct ?? d.conversionRatio ?? 0,
      }))
    : (data && data.periods && data.periods.length > 0)
    ? data.periods.map((p, i) => ({
        period: p,
        operatingCashFlow: data.operatingCashFlow ? data.operatingCashFlow[i] : 0,
        capex: data.capex ? data.capex[i] : 0,
        freeCashFlow: data.freeCashFlow ? data.freeCashFlow[i] : 0,
        netIncome: data.netIncome ? data.netIncome[i] : 0,
        conversionRatio: data.conversionRatio ? data.conversionRatio[i] : 0
      }))
    : [
        { period: '2021', operatingCashFlow: 104000000000, capex: 11000000000, freeCashFlow: 93000000000, netIncome: 94600000000, conversionRatio: 98.3 },
        { period: '2022', operatingCashFlow: 122000000000, capex: 10700000000, freeCashFlow: 111300000000, netIncome: 99800000000, conversionRatio: 111.5 },
        { period: '2023', operatingCashFlow: 110000000000, capex: 10900000000, freeCashFlow: 99100000000, netIncome: 96900000000, conversionRatio: 102.3 },
        { period: '2024', operatingCashFlow: 118000000000, capex: 9500000000, freeCashFlow: 108500000000, netIncome: 93700000000, conversionRatio: 115.8 }
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

  const isHealthyScore = (scorecard.score || 0) >= 60;
  const isModerateScore = (scorecard.score || 0) >= 40;
  const isHighConversion = (scorecard.conversionRatio || 0) >= 95;
  const treasury10y = 4.25;

  // Maintenance vs Growth CapEx estimation (65% maint, 35% growth)
  const maintCapex = (scorecard.capex || 0) * 0.65;
  const growthCapex = (scorecard.capex || 0) * 0.35;

  // SVG Geometry for FCF vs Net Income Trajectory
  const svgW = 640;
  const svgH = 240;
  const padL = 50;
  const padR = 25;
  const padT = 30;
  const padB = 40;
  const plotW = svgW - padL - padR;
  const plotH = svgH - padT - padB;

  const maxCash = Math.max(1000, ...chartData.flatMap(d => [d.operatingCashFlow, d.freeCashFlow, d.netIncome])) * 1.15;
  const getX = (idx) => padL + (idx / Math.max(1, chartData.length - 1)) * plotW;
  const getY = (val) => padT + plotH - ((val / maxCash) * plotH);

  const fcfPath = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.freeCashFlow)}`).join(' ');
  const niPath = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.netIncome)}`).join(' ');

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Header & Scorecard Badge */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#00D09C]/10 border border-[#00D09C]/30 flex items-center justify-center text-[#00D09C]">
            <Coins className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">Free Cash Flow & Owner Earnings Engine</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 14 • Stage 3
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Audits real discretionary cash yield ($FCF = CFO − CapEx$), Buffett Owner Earnings, and Treasury spreads.
            </p>
          </div>
        </div>

        <div className={`self-start md:self-auto px-3.5 py-1.5 rounded-xl border flex items-center gap-2 ${
          isHealthyScore
            ? 'bg-[#00D09C]/10 border-[#00D09C]/40 text-[#00D09C]'
            : isModerateScore
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
        {/* Metric 1: FCF Yield */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Free Cash Flow Yield</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.fcfYield >= 5.0 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.fcfYield >= 3.0 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.fcfYield >= 5.0 ? 'Attractive' : scorecard.fcfYield >= 3.0 ? 'Fair' : 'Low Yield'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {scorecard.fcfYield ? scorecard.fcfYield.toFixed(2) : '0.00'}%
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Spread vs 10Y: {scorecard.fcfSpreadVsTreasury ? `${scorecard.fcfSpreadVsTreasury > 0 ? '+' : ''}${scorecard.fcfSpreadVsTreasury.toFixed(2)}%` : 'N/A'}
          </div>
        </div>

        {/* Metric 2: Price / FCF */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Price / FCF Multiple</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.priceToFcf <= 20 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.priceToFcf <= 30 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.priceToFcf <= 20 ? 'Value' : scorecard.priceToFcf <= 30 ? 'Quality' : 'Premium'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#1455FE] mt-1.5">
            {scorecard.priceToFcf ? scorecard.priceToFcf.toFixed(1) : '0.0'}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            EV / FCF: {scorecard.evToFcf ? scorecard.evToFcf.toFixed(1) : '0.0'}x
          </div>
        </div>

        {/* Metric 3: FCF Conversion Ratio */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Cash Conversion</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              isHighConversion ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {isHighConversion ? '> 100% Cash Quality' : 'Accrual Gap'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.conversionRatio ? scorecard.conversionRatio.toFixed(1) : '0.0'}%
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            FCF / Net Income (Target &gt; 90%)
          </div>
        </div>

        {/* Metric 4: Buffett Owner Earnings */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Buffett Owner Earnings</span>
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#00D09C]/10 text-[#00D09C]">
              Distributable
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {formatB(scorecard.ownerEarnings)}
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Owner Yield: {scorecard.ownerEarningsYield ? scorecard.ownerEarningsYield.toFixed(2) : '0.00'}%
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center justify-between gap-2 border-b border-[#222A3B] pb-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('cashFunnel')}
            className={`fintech-tab-btn ${activeTab === 'cashFunnel' ? 'active' : ''}`}
          >
            <PieChart className="w-3.5 h-3.5" />
            <span>Cash Conversion Funnel</span>
          </button>
          <button
            onClick={() => setActiveTab('spreadGauge')}
            className={`fintech-tab-btn ${activeTab === 'spreadGauge' ? 'active' : ''}`}
          >
            <Landmark className="w-3.5 h-3.5" />
            <span>FCF vs Treasury Spread</span>
          </button>
          <button
            onClick={() => setActiveTab('fcfHistory')}
            className={`fintech-tab-btn ${activeTab === 'fcfHistory' ? 'active' : ''}`}
          >
            <BarChart3 className="w-3.5 h-3.5" />
            <span>FCF vs Net Income Trajectory</span>
          </button>
        </div>

        {activeTab === 'fcfHistory' && (
          <div className="hidden sm:flex items-center gap-3 text-xs font-medium text-[#94A3B8]">
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#00D09C]"></span>
              <span className="text-[#F8FAFC]">Free Cash Flow</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#1455FE]"></span>
              <span>Net Income</span>
            </div>
          </div>
        )}
      </div>

      {/* Tab 1: Cash Conversion Funnel */}
      {activeTab === 'cashFunnel' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-4">
              Discretionary Cash Funnel: From Operating Cash to Owner Distributions
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              {/* Box 1: Operating Cash Flow */}
              <div className="bg-[#141923] border border-[#222A3B] p-4 rounded-xl relative">
                <div className="text-[10px] font-mono text-[#1455FE] uppercase">Step 1 • Inflows</div>
                <div className="text-sm font-bold text-[#F8FAFC] mt-1">Operating Cash Flow (CFO)</div>
                <div className="text-lg font-mono font-bold text-[#1455FE] mt-2">
                  {formatB(scorecard.operatingCashFlow)}
                </div>
                <div className="text-[11px] text-[#64748B] mt-1">
                  Cash collected from customers minus operating costs & taxes
                </div>
                <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 w-6 h-6 rounded-full bg-[#1C2333] border border-[#222A3B] items-center justify-center text-[#94A3B8] text-xs font-bold">
                  −
                </div>
              </div>

              {/* Box 2: Total CapEx */}
              <div className="bg-[#141923] border border-[#222A3B] p-4 rounded-xl relative">
                <div className="text-[10px] font-mono text-[#F04438] uppercase">Step 2 • Reinvestment</div>
                <div className="text-sm font-bold text-[#F8FAFC] mt-1">Capital Expenditures (CapEx)</div>
                <div className="text-lg font-mono font-bold text-[#F04438] mt-2">
                  −{formatB(scorecard.capex)}
                </div>
                <div className="text-[11px] text-[#64748B] mt-1">
                  Maint: ~{formatB(maintCapex)} • Growth: ~{formatB(growthCapex)}
                </div>
                <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 w-6 h-6 rounded-full bg-[#1C2333] border border-[#222A3B] items-center justify-center text-[#94A3B8] text-xs font-bold">
                  =
                </div>
              </div>

              {/* Box 3: Free Cash Flow */}
              <div className="bg-[#1C2333] border border-[#00D09C]/40 p-4 rounded-xl bg-gradient-to-b from-[#1C2333] to-[#0B0E14]">
                <div className="text-[10px] font-mono text-[#00D09C] uppercase">Step 3 • Discretionary Cash</div>
                <div className="text-sm font-bold text-[#00D09C] mt-1">Free Cash Flow (FCF)</div>
                <div className="text-lg font-mono font-bold text-[#00D09C] mt-2">
                  {formatB(scorecard.freeCashFlow)}
                </div>
                <div className="text-[11px] text-[#94A3B8] mt-1">
                  Cash available for share buybacks, dividends, and M&A
                </div>
              </div>
            </div>

            {/* Buffett Owner Earnings Breakdown Box */}
            <div className="mt-4 p-3.5 rounded-lg bg-[#141923] border border-[#222A3B]">
              <div className="flex items-center justify-between text-xs pb-2 border-b border-[#222A3B]">
                <span className="font-bold text-[#F8FAFC] flex items-center gap-1.5">
                  <Landmark className="w-4 h-4 text-[#00D09C]" />
                  Warren Buffett Owner Earnings Formula:
                </span>
                <span className="font-mono text-[#00D09C] font-bold">
                  {formatB(scorecard.ownerEarnings)} ({scorecard.ownerEarningsYield ? scorecard.ownerEarningsYield.toFixed(2) : '0.00'}% Yield)
                </span>
              </div>
              <div className="text-[11px] text-[#94A3B8] mt-2 leading-relaxed">
                $$\text&#123;Owner Earnings&#125; = \text&#123;Net Income&#125; + \text&#123;D&A&#125; - \text&#123;Maintenance CapEx&#125; \pm \Delta\text&#123;Working Capital&#125;$$
                Unlike standard CapEx which penalizes visionary expansion, Buffett Owner Earnings subtracts <em>only the capital required to maintain current competitive unit volume</em>, revealing true long-term cash compound power.
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: FCF Yield vs 10Y US Treasury Spread */}
      {activeTab === 'spreadGauge' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-3">
              Equity Cash Yield vs Risk-Free Sovereign Benchmark (10Y US Treasury: {treasury10y.toFixed(2)}%)
            </div>

            {/* Visual Spread Bar Comparison */}
            <div className="space-y-3 my-4">
              {/* 10Y Treasury Bar */}
              <div>
                <div className="flex justify-between text-xs mb-1 font-mono">
                  <span className="text-[#94A3B8]">10-Year US Treasury Yield (Guaranteed Sovereign)</span>
                  <span className="text-[#F8FAFC] font-bold">{treasury10y.toFixed(2)}%</span>
                </div>
                <div className="w-full h-4 bg-[#141923] rounded-full overflow-hidden border border-[#222A3B]">
                  <div className="h-full bg-[#1455FE]" style={{ width: `${Math.min(100, (treasury10y / 8) * 100)}%` }}></div>
                </div>
              </div>

              {/* Equity FCF Yield Bar */}
              <div>
                <div className="flex justify-between text-xs mb-1 font-mono">
                  <span className="text-[#94A3B8]">{scorecard.ticker} Free Cash Flow Yield (Discretionary Equity Cash)</span>
                  <span className="text-[#00D09C] font-bold">{scorecard.fcfYield ? scorecard.fcfYield.toFixed(2) : '0.00'}%</span>
                </div>
                <div className="w-full h-4 bg-[#141923] rounded-full overflow-hidden border border-[#222A3B]">
                  <div className="h-full bg-[#00D09C]" style={{ width: `${Math.min(100, ((scorecard.fcfYield || 0) / 8) * 100)}%` }}></div>
                </div>
              </div>
            </div>

            {/* Spread Takeaway Pill */}
            <div className="p-3 bg-[#141923] border border-[#222A3B] rounded-lg flex items-center justify-between text-xs">
              <span className="text-[#94A3B8]">
                Net FCF Spread vs Treasury: <strong className={scorecard.fcfSpreadVsTreasury >= 0 ? 'text-[#00D09C]' : 'text-[#F04438]'}>
                  {scorecard.fcfSpreadVsTreasury > 0 ? '+' : ''}{scorecard.fcfSpreadVsTreasury ? scorecard.fcfSpreadVsTreasury.toFixed(2) : '0.00'}%
                </strong>
              </span>
              <span className={`px-2.5 py-0.5 rounded font-mono text-[11px] ${
                scorecard.fcfSpreadVsTreasury >= 0 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
              }`}>
                {scorecard.fcfSpreadVsTreasury >= 0 ? 'Positive Equity Cash Premium' : 'Requires Growth Compounding to Beat Bonds'}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Multi-Year FCF & Net Income Trajectory SVG */}
      {activeTab === 'fcfHistory' && (
        <div className="space-y-4">
          <div className="w-full bg-[#0B0E14] border border-[#222A3B] rounded-xl p-4">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-2">
              Multi-Year Cash Flow Trajectory (Free Cash Flow vs Reported Net Income)
            </div>
            <div className="w-full overflow-x-auto">
              <svg viewBox={`0 0 ${svgW} ${svgH}`} className="w-full h-auto select-none">
                {/* Horizontal Grid lines */}
                {[0, 0.25, 0.5, 0.75, 1.0].map((ratio, i) => {
                  const y = padT + plotH * (1 - ratio);
                  const val = formatB(ratio * maxCash);
                  return (
                    <g key={i}>
                      <line x1={padL} y1={y} x2={svgW - padR} y2={y} stroke="#222A3B" strokeDasharray="3 3" />
                      <text x={padL - 8} y={y + 3} fill="#64748B" fontSize="10" textAnchor="end" fontFamily="monospace">
                        {val}
                      </text>
                    </g>
                  );
                })}

                {/* Path Lines */}
                <path d={fcfPath} fill="none" stroke="#00D09C" strokeWidth="2.5" />
                <path d={niPath} fill="none" stroke="#1455FE" strokeWidth="2" strokeDasharray="4 3" />

                {/* Data Points */}
                {chartData.map((d, i) => {
                  const cx = getX(i);
                  const cyFcf = getY(d.freeCashFlow);
                  const isHov = hoveredIdx === i;
                  return (
                    <g key={i} onMouseEnter={() => setHoveredIdx(i)} onMouseLeave={() => setHoveredIdx(null)} className="cursor-pointer">
                      {isHov && (
                        <line x1={cx} y1={padT} x2={cx} y2={padT + plotH} stroke="#222A3B" strokeWidth="1.5" strokeDasharray="2 2" />
                      )}
                      <circle cx={cx} cy={cyFcf} r={isHov ? 6 : 4} fill="#00D09C" stroke="#0B0E14" strokeWidth="2" />
                      <text x={cx} y={svgH - 12} fill={isHov ? '#F8FAFC' : '#94A3B8'} fontSize="11" textAnchor="middle" fontFamily="monospace">
                        {d.period}
                      </text>
                    </g>
                  );
                })}
              </svg>
            </div>

            {/* Multi-Period Breakdown Cards Strip */}
            <div style={{ display: 'grid', gridTemplateColumns: `repeat(${chartData.length}, 1fr)`, gap: '12px', marginTop: '14px' }}>
              {chartData.map((d, i) => {
                const isHovered = hoveredIdx === i;
                return (
                  <div
                    key={d.period}
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
                        {d.period}
                      </span>
                      <span style={{ fontSize: '11px', color: '#00D09C', fontWeight: '800', fontFamily: 'var(--font-mono)' }}>
                        {formatB(d.freeCashFlow)}
                      </span>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                        <span>FCF:</span>
                        <strong>{formatB(d.freeCashFlow)}</strong>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: '#1455FE' }}>
                        <span>Net Income:</span>
                        <strong>{formatB(d.netIncome)}</strong>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                        <span>Cash Conv:</span>
                        <strong style={{ color: (d.conversionRatio || 0) >= 100 ? 'var(--gain-green)' : 'var(--text-primary)' }}>
                          {d.conversionRatio ? d.conversionRatio.toFixed(1) : '0.0'}%
                        </strong>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Tier-1 Fintech Investor Takeaway Footer */}
      <div className="mt-5 p-4 rounded-xl bg-[#0B0E14] border border-[#222A3B] flex items-start gap-3">
        <Info className="w-5 h-5 text-[#00D09C] shrink-0 mt-0.5" />
        <div className="text-xs text-[#94A3B8] leading-relaxed">
          <strong className="text-[#F8FAFC]">Cash Machine Verdict: </strong>
          {scorecard.classification === 'TIER-1 DISCRETIONARY CASH MACHINE' ? (
            <span>
              Generates exceptional cash conversion ({scorecard.conversionRatio ? scorecard.conversionRatio.toFixed(1) : '0.0'}% of GAAP Net Income), confirming earnings are 100% backed by cold hard liquidity with negligible accrual drag.
              Distributable Buffett Owner Earnings stand at {formatB(scorecard.ownerEarnings)}.
            </span>
          ) : (
            <span>
              FCF yield is {scorecard.fcfYield ? scorecard.fcfYield.toFixed(2) : '0.00'}% with {scorecard.conversionRatio ? scorecard.conversionRatio.toFixed(1) : '0.0'}% cash conversion.
              Analyze whether heavy CapEx is genuinely driving moat-widening growth or merely repairing legacy depreciation.
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
