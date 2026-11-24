import React, { useState } from 'react';
import { Layers, ShieldCheck, Activity, BarChart3, TrendingUp, TrendingDown, ArrowRight, DollarSign, PieChart, Info, Landmark } from 'lucide-react';

export default function EvValuationChart({ data, evScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('evBridge'); // 'evBridge' | 'multiplesHistory' | 'capitalStructureAudit'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  const scorecard = evScorecard || {
    ticker: 'AAPL',
    currentPrice: 224.50,
    marketCap: 3400000000000,
    totalDebt: 105000000000,
    cash: 67000000000,
    enterpriseValue: 3438000000000,
    priceToSales: 8.7,
    evToSales: 8.8,
    evToEbitda: 24.2,
    evToEbit: 27.9,
    ebitdaMargin: 34.3,
    netDebt: 38000000000,
    score: 72.0,
    classification: 'BALANCED PRICING & MODERATE LEVERAGE'
  };

  const chartData = Array.isArray(data) && data.length > 0
    ? data.map(d => ({
        period: String(d.year || d.period || '2024'),
        enterpriseValue: d.ev ?? d.enterpriseValue ?? 0,
        marketCap: d.marketCap ?? 0,
        totalDebt: d.netDebt != null ? (d.netDebt > 0 ? d.netDebt : 0) : (d.totalDebt ?? 0),
        cash: d.cash ?? 0,
        evToSales: d.evSales ?? d.evToSales ?? 0,
        evToEbitda: d.evEbitda ?? d.evToEbitda ?? 0,
        priceToSales: d.ps ?? d.priceToSales ?? 0,
      }))
    : (data && data.periods && data.periods.length > 0)
    ? data.periods.map((p, i) => ({
        period: p,
        enterpriseValue: data.enterpriseValue ? data.enterpriseValue[i] : 0,
        marketCap: data.marketCap ? data.marketCap[i] : 0,
        totalDebt: data.totalDebt ? data.totalDebt[i] : 0,
        cash: data.cash ? data.cash[i] : 0,
        evToSales: data.evToSales ? data.evToSales[i] : 0,
        evToEbitda: data.evToEbitda ? data.evToEbitda[i] : 0,
        priceToSales: data.priceToSales ? data.priceToSales[i] : 0,
      }))
    : [
        { period: '2021', enterpriseValue: 2450000000000, marketCap: 2360000000000, evToSales: 6.7, evToEbitda: 20.4, priceToSales: 6.5 },
        { period: '2022', enterpriseValue: 2280000000000, marketCap: 2183000000000, evToSales: 5.8, evToEbitda: 17.5, priceToSales: 5.5 },
        { period: '2023', enterpriseValue: 2950000000000, marketCap: 2870000000000, evToSales: 7.7, evToEbitda: 23.6, priceToSales: 7.5 },
        { period: '2024', enterpriseValue: 3438000000000, marketCap: 3400000000000, evToSales: 8.8, evToEbitda: 24.2, priceToSales: 8.7 }
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

  const isNetCash = (scorecard.cash || 0) >= (scorecard.totalDebt || 0);
  const netDebtAbs = Math.abs((scorecard.totalDebt || 0) - (scorecard.cash || 0));
  const isHealthyScore = (scorecard.score || 0) >= 60;
  const isModerateScore = (scorecard.score || 0) >= 40;

  // SVG Geometry for Multiples Chart
  const svgW = 640;
  const svgH = 240;
  const padL = 45;
  const padR = 25;
  const padT = 30;
  const padB = 40;
  const plotW = svgW - padL - padR;
  const plotH = svgH - padT - padB;

  const maxMultiple = Math.max(15, ...chartData.flatMap(d => [d.evToEbitda, d.evToSales, d.priceToSales])) * 1.2;
  const getX = (idx) => padL + (idx / Math.max(1, chartData.length - 1)) * plotW;
  const getY = (val) => padT + plotH - ((val / maxMultiple) * plotH);

  // SVG Multiples Paths
  const evEbitdaPath = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.evToEbitda)}`).join(' ');
  const evSalesPath = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.evToSales)}`).join(' ');
  const psPath = chartData.map((d, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)},${getY(d.priceToSales)}`).join(' ');

  return (
    <div className="w-full bg-[#141923] border border-[#222A3B] rounded-2xl p-5 md:p-6 text-[#F8FAFC]">
      {/* Header & Scorecard Badge */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-5 border-b border-[#222A3B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#00D09C]/10 border border-[#00D09C]/30 flex items-center justify-center text-[#00D09C]">
            <Layers className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-[#F8FAFC]">Price-to-Sales & EV Multiples Engine</h3>
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-[#1C2333] text-[#94A3B8] border border-[#222A3B]">
                Chapter 13 • Stage 3
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Audits capital structure neutrality: Market Cap + Total Debt − Cash = Enterprise Value ($EV$), EV/EBITDA, and revenue multiples.
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
        {/* Metric 1: Enterprise Value */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Enterprise Value (EV)</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              isNetCash ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#1455FE]/10 text-[#1455FE]'
            }`}>
              {isNetCash ? 'Net Cash' : 'Net Debt'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {formatB(scorecard.enterpriseValue)}
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Market Cap: {formatB(scorecard.marketCap)}
          </div>
        </div>

        {/* Metric 2: Price to Sales (P/S) */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>Price to Sales (P/S)</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.priceToSales <= 3 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.priceToSales <= 8 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.priceToSales <= 3 ? 'Cheap' : scorecard.priceToSales <= 8 ? 'Fair' : 'Rich'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#1455FE] mt-1.5">
            {scorecard.priceToSales ? scorecard.priceToSales.toFixed(1) : '0.0'}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            EV / Sales: {scorecard.evToSales ? scorecard.evToSales.toFixed(1) : '0.0'}x
          </div>
        </div>

        {/* Metric 3: EV / EBITDA */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>EV / EBITDA</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.evToEbitda <= 14 ? 'bg-[#00D09C]/10 text-[#00D09C]' : scorecard.evToEbitda <= 22 ? 'bg-[#1455FE]/10 text-[#1455FE]' : 'bg-[#F04438]/10 text-[#F04438]'
            }`}>
              {scorecard.evToEbitda <= 14 ? 'Undervalued' : scorecard.evToEbitda <= 22 ? 'Quality' : 'Premium'}
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#00D09C] mt-1.5">
            {scorecard.evToEbitda ? scorecard.evToEbitda.toFixed(1) : '0.0'}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            Target Hurdle: &lt; 16.0x
          </div>
        </div>

        {/* Metric 4: EV / EBIT (Operating Multiplier) */}
        <div className="bg-[#1C2333] border border-[#222A3B] rounded-xl p-3.5">
          <div className="text-[11px] text-[#94A3B8] font-medium flex items-center justify-between">
            <span>EV / EBIT</span>
            <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
              scorecard.evToEbit <= 18 ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#1C2333] text-[#94A3B8]'
            }`}>
              True Earnings
            </span>
          </div>
          <div className="text-xl font-bold font-mono text-[#F8FAFC] mt-1.5">
            {scorecard.evToEbit ? scorecard.evToEbit.toFixed(1) : '0.0'}x
          </div>
          <div className="text-[10px] text-[#64748B] mt-1">
            EBITDA Margin: {scorecard.ebitdaMargin ? scorecard.ebitdaMargin.toFixed(1) : '0.0'}%
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center justify-between gap-2 border-b border-[#222A3B] pb-3 mb-4">
        <div className="fintech-tab-group">
          <button
            onClick={() => setActiveTab('evBridge')}
            className={`fintech-tab-btn ${activeTab === 'evBridge' ? 'active' : ''}`}
          >
            <PieChart className="w-3.5 h-3.5" />
            <span>EV Bridge Waterfall</span>
          </button>
          <button
            onClick={() => setActiveTab('multiplesHistory')}
            className={`fintech-tab-btn ${activeTab === 'multiplesHistory' ? 'active' : ''}`}
          >
            <BarChart3 className="w-3.5 h-3.5" />
            <span>EV/EBITDA vs Sales Trajectory</span>
          </button>
          <button
            onClick={() => setActiveTab('capitalStructureAudit')}
            className={`fintech-tab-btn ${activeTab === 'capitalStructureAudit' ? 'active' : ''}`}
          >
            <Landmark className="w-3.5 h-3.5" />
            <span>Capital Structure Neutrality Audit</span>
          </button>
        </div>

        {activeTab === 'multiplesHistory' && (
          <div className="hidden sm:flex items-center gap-3 text-xs font-medium text-[#94A3B8]">
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#00D09C]"></span>
              <span className="text-[#F8FAFC]">EV/EBITDA</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#1455FE]"></span>
              <span>EV/Sales</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-[#94A3B8] border-b border-dashed"></span>
              <span>P/S</span>
            </div>
          </div>
        )}
      </div>

      {/* Tab 1: EV Bridge Waterfall Diagram */}
      {activeTab === 'evBridge' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-4">
              Enterprise Value Bridge: What An Acquirer Truly Pays
            </div>

            {/* Waterfall Flow Columns */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
              {/* Step 1: Market Cap */}
              <div className="bg-[#141923] border border-[#222A3B] p-4 rounded-xl relative">
                <div className="text-[10px] font-mono text-[#94A3B8] uppercase">Step 1 • Equity Value</div>
                <div className="text-sm font-bold text-[#F8FAFC] mt-1">Market Capitalization</div>
                <div className="text-lg font-mono font-bold text-[#F8FAFC] mt-2">
                  {formatB(scorecard.marketCap)}
                </div>
                <div className="text-[11px] text-[#64748B] mt-1">
                  100% of outstanding public common stock
                </div>
                <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 w-6 h-6 rounded-full bg-[#1C2333] border border-[#222A3B] items-center justify-center text-[#94A3B8] text-xs font-bold">
                  +
                </div>
              </div>

              {/* Step 2: Total Debt */}
              <div className="bg-[#141923] border border-[#222A3B] p-4 rounded-xl relative">
                <div className="text-[10px] font-mono text-[#F04438] uppercase">Step 2 • Funded Debt</div>
                <div className="text-sm font-bold text-[#F8FAFC] mt-1">Total Funded Debt</div>
                <div className="text-lg font-mono font-bold text-[#F04438] mt-2">
                  +{formatB(scorecard.totalDebt)}
                </div>
                <div className="text-[11px] text-[#64748B] mt-1">
                  Notes, bonds & bank credit lines assumed by acquirer
                </div>
                <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 w-6 h-6 rounded-full bg-[#1C2333] border border-[#222A3B] items-center justify-center text-[#94A3B8] text-xs font-bold">
                  −
                </div>
              </div>

              {/* Step 3: Cash & Equivalents */}
              <div className="bg-[#141923] border border-[#222A3B] p-4 rounded-xl relative">
                <div className="text-[10px] font-mono text-[#00D09C] uppercase">Step 3 • Liquid Reserves</div>
                <div className="text-sm font-bold text-[#F8FAFC] mt-1">Cash & Short-Term Inv.</div>
                <div className="text-lg font-mono font-bold text-[#00D09C] mt-2">
                  −{formatB(scorecard.cash)}
                </div>
                <div className="text-[11px] text-[#64748B] mt-1">
                  Pocketed immediately by buyer to offset purchase price
                </div>
                <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 w-6 h-6 rounded-full bg-[#1C2333] border border-[#222A3B] items-center justify-center text-[#94A3B8] text-xs font-bold">
                  =
                </div>
              </div>

              {/* Step 4: Enterprise Value */}
              <div className="bg-[#1C2333] border border-[#00D09C]/40 p-4 rounded-xl bg-gradient-to-b from-[#1C2333] to-[#0B0E14]">
                <div className="text-[10px] font-mono text-[#00D09C] uppercase">Final Result • True Price</div>
                <div className="text-sm font-bold text-[#00D09C] mt-1">Enterprise Value (EV)</div>
                <div className="text-lg font-mono font-bold text-[#00D09C] mt-2">
                  {formatB(scorecard.enterpriseValue)}
                </div>
                <div className="text-[11px] text-[#94A3B8] mt-1">
                  {isNetCash ? 'Trades below Market Cap (Net Cash Fortress)' : 'Trades above Market Cap (Debt Premium)'}
                </div>
              </div>
            </div>

            {/* Net Cash vs Net Debt Explainer Pill */}
            <div className="mt-4 p-3 rounded-lg bg-[#141923] border border-[#222A3B] flex items-center justify-between text-xs">
              <div className="flex items-center gap-2">
                <Info className="w-4 h-4 text-[#1455FE]" />
                <span className="text-[#94A3B8]">
                  Net Debt Position: <strong className="text-[#F8FAFC]">{formatB(netDebtAbs)}</strong> ({isNetCash ? 'Excess Cash Reserves' : 'Funded Leverage Burden'})
                </span>
              </div>
              <span className={`px-2 py-0.5 rounded font-mono text-[11px] ${
                isNetCash ? 'bg-[#00D09C]/10 text-[#00D09C]' : 'bg-[#F04438]/10 text-[#F04438]'
              }`}>
                {isNetCash ? 'Acquirer Discount: Cash > Debt' : 'Acquirer Burden: Debt > Cash'}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: EV Multiples Historical Trajectory SVG */}
      {activeTab === 'multiplesHistory' && (
        <div className="space-y-4">
          <div className="w-full bg-[#0B0E14] border border-[#222A3B] rounded-xl p-4">
            <div className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-2">
              Multi-Year Multiple Comparison (EV/EBITDA vs EV/Sales vs P/S)
            </div>
            <div className="w-full overflow-x-auto">
              <svg viewBox={`0 0 ${svgW} ${svgH}`} className="w-full h-auto select-none">
                {/* Horizontal Grid lines */}
                {[0, 0.25, 0.5, 0.75, 1.0].map((ratio, i) => {
                  const y = padT + plotH * (1 - ratio);
                  const val = (ratio * maxMultiple).toFixed(1);
                  return (
                    <g key={i}>
                      <line x1={padL} y1={y} x2={svgW - padR} y2={y} stroke="#222A3B" strokeDasharray="3 3" />
                      <text x={padL - 8} y={y + 3} fill="#64748B" fontSize="10" textAnchor="end" fontFamily="monospace">
                        {val}x
                      </text>
                    </g>
                  );
                })}

                {/* Path Lines */}
                <path d={evEbitdaPath} fill="none" stroke="#00D09C" strokeWidth="2.5" />
                <path d={evSalesPath} fill="none" stroke="#1455FE" strokeWidth="2" />
                <path d={psPath} fill="none" stroke="#94A3B8" strokeWidth="1.5" strokeDasharray="4 3" />

                {/* Data Points */}
                {chartData.map((d, i) => {
                  const cx = getX(i);
                  const cyEbitda = getY(d.evToEbitda);
                  const isHov = hoveredIdx === i;
                  return (
                    <g key={i} onMouseEnter={() => setHoveredIdx(i)} onMouseLeave={() => setHoveredIdx(null)} className="cursor-pointer">
                      {/* Vertical highlight line */}
                      {isHov && (
                        <line x1={cx} y1={padT} x2={cx} y2={padT + plotH} stroke="#222A3B" strokeWidth="1.5" strokeDasharray="2 2" />
                      )}
                      {/* EV/EBITDA point */}
                      <circle cx={cx} cy={cyEbitda} r={isHov ? 6 : 4} fill="#00D09C" stroke="#0B0E14" strokeWidth="2" />
                      {/* Year label */}
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
                        {d.evToEbitda ? d.evToEbitda.toFixed(1) : '0.0'}x
                      </span>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                        <span>EV/EBITDA:</span>
                        <strong>{d.evToEbitda ? d.evToEbitda.toFixed(1) : '0.0'}x</strong>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: '#1455FE' }}>
                        <span>EV/Sales:</span>
                        <strong>{d.evToSales ? d.evToSales.toFixed(1) : '0.0'}x</strong>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                        <span>P/S:</span>
                        <strong>{d.priceToSales ? d.priceToSales.toFixed(1) : '0.0'}x</strong>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Capital Structure Neutrality Audit */}
      {activeTab === 'capitalStructureAudit' && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Left Card: Why P/S Fails on Leveraged Firms */}
            <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
              <div className="flex items-center gap-2 text-xs font-bold text-[#F04438] uppercase tracking-wider mb-2">
                <TrendingDown className="w-4 h-4" />
                The P/S Blindspot (Leverage Trap)
              </div>
              <p className="text-xs text-[#94A3B8] leading-relaxed">
                A heavily leveraged capital-intensive company can show a deceptively low <strong>P/S of 0.3x</strong> because Market Cap ignores funded debt.
                However, once funded debt is bridged, <strong>EV/Sales surges to 2.2x</strong>, proving the company is substantially more expensive than equity multiples imply.
              </p>
              <div className="mt-3 p-3 bg-[#141923] border border-[#222A3B] rounded-lg text-xs space-y-1 font-mono">
                <div className="flex justify-between text-[#94A3B8]">
                  <span>P/S Formula:</span>
                  <span className="text-[#F8FAFC]">Market Cap / Revenue</span>
                </div>
                <div className="flex justify-between text-[#94A3B8]">
                  <span>EV/Sales Formula:</span>
                  <span className="text-[#00D09C]">(Market Cap + Debt − Cash) / Revenue</span>
                </div>
              </div>
            </div>

            {/* Right Card: The Enterprise Value Neutrality Advantage */}
            <div className="p-4 bg-[#0B0E14] border border-[#222A3B] rounded-xl">
              <div className="flex items-center gap-2 text-xs font-bold text-[#00D09C] uppercase tracking-wider mb-2">
                <ShieldCheck className="w-4 h-4" />
                Capital Structure Invariance
              </div>
              <p className="text-xs text-[#94A3B8] leading-relaxed">
                Whether a company funds operations with <strong>100% Equity</strong> or <strong>60% Debt</strong>, Enterprise Value measures the entire operating cash engine.
                EV/EBITDA allows direct apples-to-apples valuation between an un-leveraged tech firm and a leveraged industrial compounder.
              </p>
              <div className="mt-3 p-3 bg-[#141923] border border-[#222A3B] rounded-lg text-xs space-y-1 font-mono">
                <div className="flex justify-between text-[#94A3B8]">
                  <span>Current EV / EBITDA:</span>
                  <span className="text-[#00D09C] font-bold">{scorecard.evToEbitda ? scorecard.evToEbitda.toFixed(1) : '0.0'}x</span>
                </div>
                <div className="flex justify-between text-[#94A3B8]">
                  <span>Benchmark Quality Corridor:</span>
                  <span className="text-[#1455FE]">10.0x − 18.0x</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tier-1 Fintech Investor Takeaway Footer */}
      <div className="mt-5 p-4 rounded-xl bg-[#0B0E14] border border-[#222A3B] flex items-start gap-3">
        <Info className="w-5 h-5 text-[#00D09C] shrink-0 mt-0.5" />
        <div className="text-xs text-[#94A3B8] leading-relaxed">
          <strong className="text-[#F8FAFC]">Institutional Takeaway: </strong>
          {scorecard.classification === 'TIER-1 NET CASH FORTRESS / ATTRACTIVE EV' ? (
            <span>
              The company maintains a fortress net cash balance sheet, rendering Enterprise Value lower than Market Capitalization.
              Operating multiples (EV/EBITDA {scorecard.evToEbitda != null && !isNaN(scorecard.evToEbitda) ? scorecard.evToEbitda.toFixed(1) : 'N/A'}x) reflect authentic cash-flow valuation insulated from debt refinance risk.
            </span>
          ) : (
            <span>
              Capital structure audit reflects {formatB(scorecard.enterpriseValue)} in Enterprise Value.
              Monitor EV/EBITDA multiple stability and ensure operational cash generation comfortably amortizes existing debt burdens.
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
