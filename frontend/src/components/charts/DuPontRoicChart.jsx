import React, { useState } from 'react';
import { Award, TrendingUp, AlertTriangle, ShieldCheck, Zap } from 'lucide-react';

export default function DuPontRoicChart({ data, dupont, currencySymbol = '$' }) {
  const [hoveredIdx, setHoveredIdx] = useState(null);

  if (!data || data.length === 0) return null;

  // Layout parameters for SVG
  const svgWidth = 720;
  const svgHeight = 260;
  const padLeft = 60;
  const padRight = 40;
  const padTop = 30;
  const padBottom = 45;
  const plotWidth = svgWidth - padLeft - padRight;
  const plotHeight = svgHeight - padTop - padBottom;

  // Values range for ROIC, ROA, ROE
  const allValues = [0, 8.5]; // 8.5 is default WACC hurdle
  data.forEach((d) => {
    allValues.push(d.roic || 0);
    allValues.push(d.roa || 0);
    allValues.push(Math.min(100, d.roe || 0)); // Cap ROE at 100 for graph scaling if extreme buybacks
  });

  const minVal = Math.min(0, ...allValues);
  const maxVal = Math.max(25, ...allValues) * 1.15;
  const valRange = maxVal - minVal || 1;

  const getY = (val) => {
    const clamped = Math.max(minVal, Math.min(maxVal, val));
    return padTop + plotHeight - ((clamped - minVal) / valRange) * plotHeight;
  };

  const waccHurdle = dupont?.wacc || 8.5;
  const waccY = getY(waccHurdle);
  const zeroY = getY(0);

  // X Positions
  const numYears = data.length;
  const stepX = plotWidth / (numYears > 1 ? numYears - 1 : 1);
  const getX = (idx) => padLeft + idx * stepX;

  // SVG Paths
  const roicPoints = data.map((d, idx) => `${getX(idx)},${getY(d.roic)}`).join(' ');
  const roaPoints = data.map((d, idx) => `${getX(idx)},${getY(d.roa)}`).join(' ');

  // Value Creation Area (ROIC down to WACC)
  const roicAreaPoints = [
    `${getX(0)},${waccY}`,
    ...data.map((d, idx) => `${getX(idx)},${getY(d.roic)}`),
    `${getX(data.length - 1)},${waccY}`
  ].join(' ');

  // Latest datapoint
  const latest = data[data.length - 1] || {};
  const isElite = (latest.roic || 0) >= 15.0;
  const isValueAccretive = (latest.roic || 0) >= waccHurdle;

  // Active hover data
  const activeData = hoveredIdx !== null ? data[hoveredIdx] : latest;

  return (
    <div className="chart-container-card full-width-chart">
      {/* Header */}
      <div className="chart-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Award size={18} color="var(--fintech-primary)" />
          <span className="chart-title">Capital Efficiency & 3-Stage DuPont Breakdown</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '4px 10px',
              borderRadius: '6px',
              background: isValueAccretive
                ? 'var(--gain-green-bg, rgba(0, 208, 156, 0.12))'
                : 'var(--loss-red-bg, rgba(240, 68, 56, 0.12))',
              border: `1px solid ${
                isValueAccretive ? 'var(--gain-green)' : 'var(--loss-red)'
              }`
            }}
          >
            {isValueAccretive ? (
              <ShieldCheck size={14} color="var(--gain-green)" />
            ) : (
              <AlertTriangle size={14} color="var(--loss-red)" />
            )}
            <span
              style={{
                fontSize: '11px',
                fontWeight: 700,
                color: isValueAccretive ? 'var(--gain-green)' : 'var(--loss-red)',
                letterSpacing: '0.4px'
              }}
            >
              {isValueAccretive
                ? `EVA Spread: +${((latest.roic || 0) - waccHurdle).toFixed(1)}%`
                : `Value Destructive: ${((latest.roic || 0) - waccHurdle).toFixed(1)}%`}
            </span>
          </div>

          <span className="chart-tag" style={{ color: 'var(--text-secondary)' }}>
            Stage 2: Quality
          </span>
        </div>
      </div>

      {/* 4 KPI Summary Cards */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: '10px',
          padding: '12px 16px',
          background: 'var(--fintech-surface-elevated, #1C2333)',
          borderBottom: '1px solid var(--fintech-border, #222A3B)'
        }}
      >
        {/* KPI 1: ROIC */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            ROIC (Capital Efficiency)
          </div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--gain-green)', fontFamily: 'monospace' }}>
            {latest.roic != null ? `${latest.roic.toFixed(1)}%` : 'N/A'}
          </div>
          <div style={{ fontSize: '10px', color: isElite ? 'var(--gain-green)' : 'var(--text-secondary)' }}>
            {isElite ? 'Elite Compounder (>15%)' : 'Cost of capital target'}
          </div>
        </div>

        {/* KPI 2: DuPont ROE */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Return on Equity (ROE)
          </div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--fintech-primary)', fontFamily: 'monospace' }}>
            {latest.roe != null ? `${latest.roe.toFixed(1)}%` : 'N/A'}
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-secondary)' }}>
            {latest.equityMultiplier > 4 ? 'Amplified by leverage' : 'Operational return'}
          </div>
        </div>

        {/* KPI 3: ROA */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Return on Assets (ROA)
          </div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: '#0284C7', fontFamily: 'monospace' }}>
            {latest.roa != null ? `${latest.roa.toFixed(1)}%` : 'N/A'}
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-secondary)' }}>
            Total balance sheet yield
          </div>
        </div>

        {/* KPI 4: 3-Stage DuPont Equation */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            DuPont 3-Stage Driver
          </div>
          <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace', marginTop: '2px' }}>
            {latest.netMargin?.toFixed(0)}% × {latest.assetTurnover?.toFixed(2)}x × {latest.equityMultiplier?.toFixed(1)}x
          </div>
          <div style={{ fontSize: '10px', color: 'var(--warning-amber)' }}>
            Margin × Turnover × Leverage
          </div>
        </div>
      </div>

      {/* SVG Chart Area */}
      <div className="svg-chart-wrapper" style={{ padding: '8px 12px', position: 'relative' }}>
        <svg
          viewBox={`0 0 ${svgWidth} ${svgHeight}`}
          className="metric-svg-chart"
          style={{ width: '100%', height: 'auto', display: 'block', overflow: 'visible' }}
        >
          <defs>
            {/* Value Creation Corridor Gradient */}
            <linearGradient id="roicValueCorridorGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#00D09C" stopOpacity="0.25" />
              <stop offset="100%" stopColor="#00D09C" stopOpacity="0.02" />
            </linearGradient>

            {/* Subtle Grid Pattern */}
            <pattern id="gridPatternDuPont" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#222A3B" strokeWidth="0.5" strokeOpacity="0.4" />
            </pattern>
          </defs>

          <rect x={padLeft} y={padTop} width={plotWidth} height={plotHeight} fill="url(#gridPatternDuPont)" />

          {/* Horizontal Gridlines */}
          {[0, 10, 20, 30, 50, 75, 100].map((tickVal) => {
            if (tickVal < minVal || tickVal > maxVal) return null;
            const y = getY(tickVal);
            return (
              <g key={`grid-${tickVal}`}>
                <line
                  x1={padLeft}
                  y1={y}
                  x2={padLeft + plotWidth}
                  y2={y}
                  stroke="#222A3B"
                  strokeDasharray="4 4"
                  strokeWidth="0.8"
                />
                <text
                  x={padLeft - 8}
                  y={y + 4}
                  textAnchor="end"
                  fill="#64748B"
                  fontSize="10"
                  fontFamily="monospace"
                >
                  {tickVal}%
                </text>
              </g>
            );
          })}

          {/* WACC Hurdle Line (8.5%) */}
          <line
            x1={padLeft}
            y1={waccY}
            x2={padLeft + plotWidth}
            y2={waccY}
            stroke="#F59E0B"
            strokeWidth="1.5"
            strokeDasharray="6 3"
          />
          <text
            x={padLeft + plotWidth + 6}
            y={waccY + 3}
            fill="#F59E0B"
            fontSize="9"
            fontWeight="bold"
            fontFamily="monospace"
          >
            WACC ({waccHurdle}%)
          </text>

          {/* Value Creation Shaded Corridor */}
          <polygon points={roicAreaPoints} fill="url(#roicValueCorridorGrad)" />

          {/* ROA Line (Cyan) */}
          <polyline
            points={roaPoints}
            fill="none"
            stroke="#0284C7"
            strokeWidth="2.0"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* ROIC Line (Emerald Mint) */}
          <polyline
            points={roicPoints}
            fill="none"
            stroke="#00D09C"
            strokeWidth="3.0"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Interactive Column Hover Zones & Nodes */}
          {data.map((d, idx) => {
            const x = getX(idx);
            const roicY = getY(d.roic);
            const roaY = getY(d.roa);
            const isHovered = hoveredIdx === idx;

            return (
              <g
                key={`point-${d.year}`}
                onMouseEnter={() => setHoveredIdx(idx)}
                onMouseLeave={() => setHoveredIdx(null)}
                style={{ cursor: 'pointer' }}
              >
                {/* Invisible Hover Rect */}
                <rect
                  x={x - stepX / 2}
                  y={padTop}
                  width={stepX}
                  height={plotHeight}
                  fill={isHovered ? 'rgba(255, 255, 255, 0.04)' : 'transparent'}
                />

                {/* Vertical Guideline on hover */}
                {isHovered && (
                  <line
                    x1={x}
                    y1={padTop}
                    x2={x}
                    y2={padTop + plotHeight}
                    stroke="var(--fintech-primary)"
                    strokeWidth="1.2"
                    strokeDasharray="3 3"
                  />
                )}

                {/* ROA Node */}
                <circle
                  cx={x}
                  cy={roaY}
                  r={isHovered ? 5 : 3.5}
                  fill="#0284C7"
                  stroke="#0B0E14"
                  strokeWidth="2"
                />

                {/* ROIC Node */}
                <circle
                  cx={x}
                  cy={roicY}
                  r={isHovered ? 6.5 : 4.5}
                  fill="#00D09C"
                  stroke="#0B0E14"
                  strokeWidth="2"
                />

                {/* Value Label above ROIC Node */}
                <text
                  x={x}
                  y={roicY - 8}
                  textAnchor="middle"
                  fill="#F8FAFC"
                  fontSize="10"
                  fontWeight="bold"
                  fontFamily="monospace"
                >
                  {d.roic?.toFixed(1)}%
                </text>

                {/* X Axis Fiscal Year Label */}
                <text
                  x={x}
                  y={padTop + plotHeight + 18}
                  textAnchor="middle"
                  fill={isHovered ? '#00D09C' : '#94A3B8'}
                  fontSize="11"
                  fontWeight={isHovered ? 'bold' : 'normal'}
                >
                  {d.year}
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      {/* Interactive 3-Stage DuPont Breakdown Fact Sheet */}
      {/* Multi-Year DuPont & ROIC Breakdown Cards Strip */}
      <div style={{ display: 'grid', gridTemplateColumns: `repeat(${data.length}, 1fr)`, gap: '12px', margin: '8px 16px 14px' }}>
        {data.map((item, idx) => {
          const isHovered = hoveredIdx === idx;
          return (
            <div
              key={item.year}
              onMouseEnter={() => setHoveredIdx(idx)}
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
                  FY{item.year}
                </span>
                <span style={{
                  fontSize: '11px',
                  color: 'var(--gain-green)',
                  fontWeight: '800',
                  fontFamily: 'var(--font-mono)'
                }}>
                  ROE {item.roe?.toFixed(1)}%
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                  <span>ROIC:</span>
                  <strong>{item.roic?.toFixed(1)}%</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#0284C7' }}>
                  <span>Net Margin:</span>
                  <strong>{item.netMargin?.toFixed(1)}%</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                  <span>Turnover:</span>
                  <strong>{item.assetTurnover?.toFixed(2)}x</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                  <span>Leverage:</span>
                  <strong>{item.equityMultiplier?.toFixed(1)}x</strong>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Legend & Takeaway */}
      <div className="chart-legend-row" style={{ padding: '4px 16px 14px' }}>
        <div className="legend-items" style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
          <div className="legend-item" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '12px', height: '3px', background: '#00D09C', borderRadius: '2px' }} />
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>ROIC (Return on Invested Capital)</span>
          </div>

          <div className="legend-item" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '12px', height: '3px', background: '#0284C7', borderRadius: '2px' }} />
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>ROA (Return on Assets)</span>
          </div>

          <div className="legend-item" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '12px', height: '2px', background: '#F59E0B', borderTop: '1px dashed #F59E0B' }} />
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>WACC Hurdle ({waccHurdle}%)</span>
          </div>
        </div>

        <span style={{ fontSize: '11px', color: 'var(--text-tertiary)' }}>
          Source: SEC Form 10-K Audited Financial Statements
        </span>
      </div>
    </div>
  );
}
