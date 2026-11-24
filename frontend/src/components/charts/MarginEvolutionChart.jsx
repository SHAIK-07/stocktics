import React, { useState } from 'react';
import { Activity } from 'lucide-react';

export default function MarginEvolutionChart({ data, currencySymbol = '$' }) {
  const [viewMode, setViewMode] = useState('both'); // 'both' | 'bar' | 'line'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  // Currency formatting helper matching fintech standards
  const fmt = (v) => {
    if (v == null || isNaN(v)) return null;
    if (Math.abs(v) < 0.001) return `${currencySymbol}0`;
    const isNeg = v < 0;
    const abs = Math.abs(v);
    const sign = isNeg ? '-' : '';

    if (abs >= 1e12) return `${sign}${currencySymbol}${(abs / 1e12).toFixed(2)}T`;
    if (abs >= 1e9) return `${sign}${currencySymbol}${(abs / 1e9).toFixed(2)}B`;
    if (abs >= 1e6) return `${sign}${currencySymbol}${(abs / 1e6).toFixed(2)}M`;
    if (abs >= 1e3) return `${sign}${currencySymbol}${(abs / 1e3).toFixed(1)}k`;
    return `${sign}${currencySymbol}${abs.toFixed(2)}`;
  };

  if (!data || data.length === 0) return null;

  const svgWidth = 720;
  const svgHeight = 260;
  const padLeft = 60;
  const padRight = 40;
  const padTop = 35;
  const padBottom = 45;
  const plotWidth = svgWidth - padLeft - padRight;
  const plotHeight = svgHeight - padTop - padBottom;

  // Helper to generate nice round percentage ticks ensuring 0% is an exact tick
  const getNiceTicks = (min, max, targetCount = 5) => {
    let low = Math.min(0, min);
    let high = Math.max(30, max);

    const rawRange = high - low;
    const rawStep = rawRange / (targetCount - 1);

    let step = 10;
    if (rawStep <= 7) step = 5;
    else if (rawStep <= 14) step = 10;
    else if (rawStep <= 22) step = 20;
    else if (rawStep <= 35) step = 25;
    else step = 50;

    let niceMin = Math.floor(low / step) * step;
    let niceMax = Math.ceil(high / step) * step;

    const ticks = [];
    for (let t = niceMin; t <= niceMax + step * 0.0001; t += step) {
      ticks.push(Math.round(t));
    }
    return { ticks, minVal: ticks[0], maxVal: ticks[ticks.length - 1] };
  };

  // Maximum & Minimum margin percentage (handles negative operating & net margins)
  const allMargins = data.flatMap(d => [d.grossMargin, d.operatingMargin, d.netMargin].filter(v => v != null && !isNaN(v)));
  const dataMax = allMargins.length > 0 ? Math.max(...allMargins) : 40;
  const dataMin = allMargins.length > 0 ? Math.min(...allMargins) : 0;

  const { ticks, minVal, maxVal } = getNiceTicks(dataMin, dataMax, 5);
  const valRange = maxVal - minVal || 1;

  const colStep = plotWidth / data.length;
  const getXCenter = (idx) => padLeft + colStep * idx + colStep / 2;

  const getY = (pct) => {
    const clamped = Math.max(minVal, Math.min(maxVal, pct || 0));
    return padTop + ((maxVal - clamped) / valRange) * plotHeight;
  };

  const yZero = getY(0);

  // Grouped bar width (3 bars side-by-side)
  const barWidth = Math.min(22, (colStep - 24) / 3);

  // Bar geometry calculation
  const getBarGeom = (val) => {
    const yVal = getY(val);
    const top = Math.min(yZero, yVal);
    const height = Math.max(3, Math.abs(yVal - yZero));
    return { top, height, isNegative: (val || 0) < 0 };
  };

  // Line paths
  const grossPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.grossMargin)}`).join(' ');
  const operPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.operatingMargin)}`).join(' ');
  const netPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.netMargin)}`).join(' ');

  return (
    <div className="chart-container-card full-width-chart">
      {/* Header with Title, Mode Switcher & Legend */}
      <div className="chart-header" style={{ flexWrap: 'wrap', gap: '12px' }}>
        <div className="chart-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Activity size={18} color="var(--fintech-primary)" />
          <span>Multi-Year Profit Margin Structure (Gross vs Operating vs Net)</span>
        </div>

        {/* View Mode Toggle Strip */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '4px', background: 'var(--bg-surface-elevated)', padding: '3px', borderRadius: '8px', border: '1px solid var(--border-hairline)' }}>
          <button
            className={`timeframe-btn ${viewMode === 'both' ? 'active' : ''}`}
            onClick={() => setViewMode('both')}
            style={{ fontSize: '11px', padding: '3px 9px' }}
          >
            Combo (Bar + Line)
          </button>
          <button
            className={`timeframe-btn ${viewMode === 'bar' ? 'active' : ''}`}
            onClick={() => setViewMode('bar')}
            style={{ fontSize: '11px', padding: '3px 9px' }}
          >
            Bars Only
          </button>
          <button
            className={`timeframe-btn ${viewMode === 'line' ? 'active' : ''}`}
            onClick={() => setViewMode('line')}
            style={{ fontSize: '11px', padding: '3px 9px' }}
          >
            Lines Only
          </button>
        </div>

        {/* Legend */}
        <div className="chart-legend" style={{ flexWrap: 'wrap' }}>
          <div className="legend-item">
            <span className="legend-color-dot" style={{ background: '#00D09C' }} />
            <span>Gross Margin</span>
          </div>
          <div className="legend-item">
            <span className="legend-color-dot" style={{ background: '#38BDF8' }} />
            <span>Operating Margin (EBIT)</span>
          </div>
          <div className="legend-item">
            <span className="legend-color-dot" style={{ background: '#A855F7' }} />
            <span>Net Margin</span>
          </div>
        </div>
      </div>

      {/* SVG Plot */}
      <div style={{ position: 'relative', width: '100%', overflowX: 'auto' }}>
        <svg
          viewBox={`0 0 ${svgWidth} ${svgHeight}`}
          style={{ width: '100%', height: 'auto', minWidth: '560px', display: 'block' }}
        >
          {/* Horizontal Gridlines & Y-Axis Labels */}
          {ticks.map((val, i) => {
            const y = getY(val);
            const isZero = val === 0;
            const isNeg = val < 0;
            return (
              <g key={i}>
                <line
                  x1={padLeft}
                  y1={y}
                  x2={svgWidth - padRight}
                  y2={y}
                  stroke={isZero ? 'rgba(255, 255, 255, 0.45)' : 'var(--border-subtle)'}
                  strokeDasharray={isZero ? 'none' : '4,4'}
                  strokeWidth={isZero ? '1.5' : '1'}
                />
                <text
                  x={padLeft - 10}
                  y={y + 4}
                  textAnchor="end"
                  fill={isZero ? 'var(--text-primary)' : isNeg ? 'rgba(240, 68, 56, 0.85)' : 'var(--text-tertiary)'}
                  fontSize="10.5"
                  fontWeight={isZero ? '800' : '500'}
                  fontFamily="var(--font-mono)"
                >
                  {val}%
                </text>
              </g>
            );
          })}

          {/* Grouped Vertical Bars (Gross, Operating, Net side-by-side) */}
          {(viewMode === 'bar' || viewMode === 'both') &&
            data.map((item, idx) => {
              const xCenter = getXCenter(idx);
              const isHovered = hoveredIdx === idx;

              const grossGeom = getBarGeom(item.grossMargin);
              const operGeom = getBarGeom(item.operatingMargin);
              const netGeom = getBarGeom(item.netMargin);

              const bGap = 3;
              const grossX = xCenter - barWidth * 1.5 - bGap;
              const operX = xCenter - barWidth * 0.5;
              const netX = xCenter + barWidth * 0.5 + bGap;

              return (
                <g
                  key={`bars-${item.year}`}
                  onMouseEnter={() => setHoveredIdx(idx)}
                  onMouseLeave={() => setHoveredIdx(null)}
                  style={{ cursor: 'pointer' }}
                >
                  {isHovered && (
                    <rect
                      x={xCenter - colStep / 2 + 6}
                      y={padTop}
                      width={colStep - 12}
                      height={plotHeight}
                      fill="rgba(255, 255, 255, 0.04)"
                      rx="6"
                    />
                  )}

                  {/* Gross Margin Bar (Green matching legend) */}
                  <rect
                    x={grossX}
                    y={grossGeom.top}
                    width={barWidth}
                    height={grossGeom.height}
                    fill={isHovered ? '#00E5AC' : '#00D09C'}
                    rx="3"
                    opacity={isHovered ? 1 : 0.9}
                  />

                  {/* Operating Margin Bar (Blue matching legend) */}
                  <rect
                    x={operX}
                    y={operGeom.top}
                    width={barWidth}
                    height={operGeom.height}
                    fill={isHovered ? '#60A5FA' : '#38BDF8'}
                    rx="3"
                    opacity={isHovered ? 1 : 0.9}
                  />

                  {/* Net Margin Bar (Purple matching legend) */}
                  <rect
                    x={netX}
                    y={netGeom.top}
                    width={barWidth}
                    height={netGeom.height}
                    fill={isHovered ? '#C084FC' : '#A855F7'}
                    rx="3"
                    opacity={isHovered ? 1 : 0.9}
                  />
                </g>
              );
            })}

          {/* Line Chart Overlay */}
          {(viewMode === 'line' || viewMode === 'both') && (
            <>
              {/* Gross Margin Line */}
              <polyline
                points={grossPoints}
                fill="none"
                stroke="#00D09C"
                strokeWidth="2.2"
                strokeLinecap="round"
                strokeLinejoin="round"
                opacity="0.8"
              />
              {/* Operating Margin Line */}
              <polyline
                points={operPoints}
                fill="none"
                stroke="#38BDF8"
                strokeWidth="2.2"
                strokeLinecap="round"
                strokeLinejoin="round"
                opacity="0.85"
              />
              {/* Net Margin Line (Solid with Glow) */}
              <polyline
                points={netPoints}
                fill="none"
                stroke="#A855F7"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                style={{ filter: 'drop-shadow(0 2px 8px rgba(168, 85, 247, 0.5))' }}
              />

              {data.map((item, idx) => {
                const x = getXCenter(idx);
                const y = getY(item.netMargin);
                const isHovered = hoveredIdx === idx;
                const isNeg = (item.netMargin || 0) < 0;
                return (
                  <circle
                    key={`dot-net-${item.year}`}
                    cx={x}
                    cy={y}
                    r={isHovered ? 6 : 4.5}
                    fill="var(--fintech-background, #0B0E14)"
                    stroke={isNeg ? '#F04438' : '#A855F7'}
                    strokeWidth="2.5"
                  />
                );
              })}
            </>
          )}

          {/* Bottom Chart Baseline */}
          <line
            x1={padLeft}
            y1={padTop + plotHeight}
            x2={svgWidth - padRight}
            y2={padTop + plotHeight}
            stroke="var(--border-hairline)"
            strokeWidth="1.5"
          />

          {/* X-Axis Fiscal Year Labels */}
          {data.map((item, idx) => {
            const x = getXCenter(idx);
            const isHovered = hoveredIdx === idx;

            return (
              <text
                key={`year-${item.year}`}
                x={x}
                y={padTop + plotHeight + 22}
                textAnchor="middle"
                fill={isHovered ? 'var(--fintech-primary)' : 'var(--text-primary)'}
                fontSize="12.5"
                fontWeight="800"
                fontFamily="var(--font-mono)"
                style={{ cursor: 'pointer' }}
                onMouseEnter={() => setHoveredIdx(idx)}
                onMouseLeave={() => setHoveredIdx(null)}
              >
                FY{item.year}
              </text>
            );
          })}
        </svg>
      </div>

      {/* Breakdown Cards Strip Below Chart */}
      <div style={{ display: 'grid', gridTemplateColumns: `repeat(${data.length}, 1fr)`, gap: '12px', marginTop: '14px' }}>
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
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span style={{ fontSize: '12px', fontWeight: '800', color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
                    FY{item.year}
                  </span>
                  <span style={{ fontSize: '9.5px', color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)', padding: '1px 5px', background: 'rgba(255,255,255,0.04)', borderRadius: '3px' }}>
                    {item.netMargin >= 15 ? 'Elite Moat' : item.netMargin > 0 ? 'Profitable' : 'Loss'}
                  </span>
                </div>
                {(item.displayRevenue || (item.revenue && fmt(item.revenue))) && (
                  <span style={{ fontSize: '10.5px', color: '#00D09C', fontWeight: '700', fontFamily: 'var(--font-mono)' }}>
                    {item.displayRevenue || fmt(item.revenue)}
                  </span>
                )}
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                  <span>Gross:</span>
                  <strong>
                    {item.displayGrossProfit || (item.grossProfit != null ? fmt(item.grossProfit) : (item.revenue ? fmt(item.revenue * (item.grossMargin / 100)) : null))
                      ? `${item.displayGrossProfit || (item.grossProfit != null ? fmt(item.grossProfit) : fmt(item.revenue * (item.grossMargin / 100)))} (${item.grossMargin}%)`
                      : `${item.grossMargin}%`}
                  </strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#38BDF8' }}>
                  <span>Oper:</span>
                  <strong>
                    {item.displayOperatingIncome || (item.operatingIncome != null ? fmt(item.operatingIncome) : (item.revenue ? fmt(item.revenue * (item.operatingMargin / 100)) : null))
                      ? `${item.displayOperatingIncome || (item.operatingIncome != null ? fmt(item.operatingIncome) : fmt(item.revenue * (item.operatingMargin / 100)))} (${item.operatingMargin}%)`
                      : `${item.operatingMargin}%`}
                  </strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: (item.netMargin || 0) < 0 ? 'var(--loss-red)' : '#A855F7' }}>
                  <span>Net:</span>
                  <strong>
                    {item.displayNetIncome || (item.netIncome != null ? fmt(item.netIncome) : (item.revenue ? fmt(item.revenue * (item.netMargin / 100)) : null))
                      ? `${item.displayNetIncome || (item.netIncome != null ? fmt(item.netIncome) : fmt(item.revenue * (item.netMargin / 100)))} (${item.netMargin}%)`
                      : `${item.netMargin}%`}
                  </strong>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
