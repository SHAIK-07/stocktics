import React, { useState } from 'react';
import { Layers, DollarSign } from 'lucide-react';

export default function CashFlowDivergenceChart({ data, currencySymbol = '$' }) {
  const [viewMode, setViewMode] = useState('both'); // 'both' | 'bar' | 'line'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  if (!data || data.length === 0) return null;

  const svgWidth = 720;
  const svgHeight = 270;
  const padLeft = 75;
  const padRight = 40;
  const padTop = 35;
  const padBottom = 45;
  const plotWidth = svgWidth - padLeft - padRight;
  const plotHeight = svgHeight - padTop - padBottom;

  // Number formatting helper
  const fmt = (v) => {
    if (v == null || isNaN(v)) return 'N/A';
    if (Math.abs(v) < 0.001) return `${currencySymbol}0`;
    const isNeg = v < 0;
    const abs = Math.abs(v);
    const sign = isNeg ? '-' : '';

    if (abs >= 1e12) return `${sign}${currencySymbol}${(abs / 1e12).toFixed(2)}T`;
    if (abs >= 1e9) return `${sign}${currencySymbol}${(abs / 1e9).toFixed(2)}B`;
    if (abs >= 1e6) return `${sign}${currencySymbol}${(abs / 1e6).toFixed(2)}M`;
    if (abs >= 1e3) return `${sign}${currencySymbol}${(abs / 1e3).toFixed(1)}k`;
    return `${sign}${currencySymbol}${abs.toFixed(1)}`;
  };

  // Helper to generate nice round ticks that guarantee 0 is an exact tick
  const getNiceTicks = (min, max, targetCount = 5) => {
    let low = Math.min(0, min);
    let high = Math.max(0, max);

    if (low === 0 && high === 0) {
      return { ticks: [-100, -50, 0, 50, 100], minVal: -100, maxVal: 100 };
    }

    const rawRange = high - low;
    const rawStep = rawRange / (targetCount - 1);
    const mag = Math.pow(10, Math.floor(Math.log10(Math.max(1, rawStep))));
    const residual = rawStep / mag;

    let step;
    if (residual <= 1.2) step = 1 * mag;
    else if (residual <= 2.5) step = 2 * mag;
    else if (residual <= 5) step = 5 * mag;
    else step = 10 * mag;

    let niceMin = Math.floor(low / step) * step;
    let niceMax = Math.ceil(high / step) * step;

    // If all negative, add 1 step cushion above 0 so zero isn't jammed at the very top
    if (high <= 0 && niceMax === 0) {
      niceMax = step;
    }
    // If all positive, add 1 step cushion below 0 if needed
    if (low >= 0 && niceMin === 0) {
      // 0 at the bottom is standard for all-positive
    }

    const ticks = [];
    for (let t = niceMin; t <= niceMax + step * 0.0001; t += step) {
      const rounded = Math.abs(t) < step * 0.0001 ? 0 : Number(t.toFixed(4));
      ticks.push(rounded);
    }

    return { ticks, minVal: ticks[0], maxVal: ticks[ticks.length - 1] };
  };

  // Extract all values to find true min and max
  const allValues = data.flatMap(d => [d.cfo, d.fcf, d.netIncome].filter(v => v != null && !isNaN(v)));
  const dataMax = allValues.length > 0 ? Math.max(...allValues) : 0;
  const dataMin = allValues.length > 0 ? Math.min(...allValues) : 0;

  const { ticks, minVal, maxVal } = getNiceTicks(dataMin, dataMax, 5);
  const valRange = maxVal - minVal || 1;

  const colStep = plotWidth / data.length;
  const getXCenter = (idx) => padLeft + colStep * idx + colStep / 2;

  const getY = (val) => {
    const clamped = Math.max(minVal, Math.min(maxVal, val || 0));
    return padTop + ((maxVal - clamped) / valRange) * plotHeight;
  };

  const yZero = getY(0);

  // 3 bars side-by-side: CFO, FCF, Net Income
  const barWidth = Math.min(20, (colStep - 24) / 3);

  // Bar geometry calculation for positive/negative values
  const getBarGeom = (val) => {
    const num = val || 0;
    const yVal = getY(num);
    const top = Math.min(yZero, yVal);
    const height = Math.max(4, Math.abs(yVal - yZero));
    return { top, height, isNegative: num < 0 };
  };

  // Line points for FCF, CFO, Net Income
  const fcfPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.fcf)}`).join(' ');
  const cfoPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.cfo)}`).join(' ');
  const niPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.netIncome)}`).join(' ');

  return (
    <div className="chart-container-card full-width-chart">
      {/* Header with Title, Mode Switcher & Legend */}
      <div className="chart-header" style={{ flexWrap: 'wrap', gap: '12px' }}>
        <div className="chart-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Layers size={18} color="var(--fintech-primary)" />
          <span>The Cash Flow Machine (CFO vs Free Cash Flow vs Net Income)</span>
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
            Line Only
          </button>
        </div>

        {/* Legend */}
        <div className="chart-legend" style={{ flexWrap: 'wrap' }}>
          <div className="legend-item">
            <span className="legend-color-dot" style={{ background: '#00D09C' }} />
            <span>Operating Cash (CFO)</span>
          </div>
          <div className="legend-item">
            <span className="legend-color-dot" style={{ background: '#38BDF8' }} />
            <span>Free Cash Flow (FCF)</span>
          </div>
          <div className="legend-item">
            <span className="legend-color-dot" style={{ background: '#94A3B8' }} />
            <span>Net Income (Accrual)</span>
          </div>
        </div>
      </div>

      {/* SVG Plot */}
      <div style={{ position: 'relative', width: '100%', overflowX: 'auto' }}>
        <svg
          viewBox={`0 0 ${svgWidth} ${svgHeight}`}
          style={{ width: '100%', height: 'auto', minWidth: '560px', display: 'block' }}
        >
          {/* Horizontal Gridlines & Y-Axis Value Labels */}
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
                  y={y + 3.5}
                  textAnchor="end"
                  fill={isZero ? 'var(--text-primary)' : isNeg ? 'rgba(240, 68, 56, 0.85)' : 'var(--text-tertiary)'}
                  fontSize="10.5"
                  fontWeight={isZero ? '800' : '500'}
                  fontFamily="var(--font-mono)"
                >
                  {fmt(val)}
                </text>
              </g>
            );
          })}

          {/* Grouped Vertical Bars (CFO, FCF, Net Income side-by-side) */}
          {(viewMode === 'bar' || viewMode === 'both') &&
            data.map((item, idx) => {
              const xCenter = getXCenter(idx);
              const isHovered = hoveredIdx === idx;

              const cfoGeom = getBarGeom(item.cfo);
              const fcfGeom = getBarGeom(item.fcf);
              const niGeom = getBarGeom(item.netIncome);

              const bGap = 3;
              const cfoX = xCenter - barWidth * 1.5 - bGap;
              const fcfX = xCenter - barWidth * 0.5;
              const niX = xCenter + barWidth * 0.5 + bGap;

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

                  {/* CFO Bar (Green matching legend) */}
                  <rect
                    x={cfoX}
                    y={cfoGeom.top}
                    width={barWidth}
                    height={cfoGeom.height}
                    fill={isHovered ? '#00E5AC' : '#00D09C'}
                    rx="3"
                    opacity={isHovered ? 1 : 0.9}
                  />

                  {/* FCF Bar (Sky Blue matching legend) */}
                  <rect
                    x={fcfX}
                    y={fcfGeom.top}
                    width={barWidth}
                    height={fcfGeom.height}
                    fill={isHovered ? '#60A5FA' : '#38BDF8'}
                    rx="3"
                    opacity={isHovered ? 1 : 0.9}
                  />

                  {/* Net Income Bar (Slate matching legend) */}
                  <rect
                    x={niX}
                    y={niGeom.top}
                    width={barWidth}
                    height={niGeom.height}
                    fill={isHovered ? '#CBD5E1' : '#94A3B8'}
                    rx="3"
                    opacity={isHovered ? 1 : 0.9}
                  />
                </g>
              );
            })}

          {/* Line Chart Overlay: FCF Trend */}
          {(viewMode === 'line' || viewMode === 'both') && (
            <>
              {/* CFO Line in Line-only mode */}
              {viewMode === 'line' && (
                <polyline
                  points={cfoPoints}
                  fill="none"
                  stroke="#00D09C"
                  strokeWidth="2"
                  strokeDasharray="4,3"
                />
              )}

              {/* FCF Line (Solid with Glow) */}
              <polyline
                points={fcfPoints}
                fill="none"
                stroke="#38BDF8"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                style={{ filter: 'drop-shadow(0 2px 8px rgba(56, 189, 248, 0.5))' }}
              />

              {data.map((item, idx) => {
                const x = getXCenter(idx);
                const y = getY(item.fcf);
                const isHovered = hoveredIdx === idx;
                const isNeg = (item.fcf || 0) < 0;
                return (
                  <circle
                    key={`dot-fcf-${item.year}`}
                    cx={x}
                    cy={y}
                    r={isHovered ? 6 : 4.5}
                    fill="#0B0E14"
                    stroke={isHovered ? '#60A5FA' : '#38BDF8'}
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

          {/* X-Axis Year Labels */}
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
                <span style={{ fontSize: '12px', fontWeight: '800', color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
                  FY{item.year}
                </span>
                <span style={{
                  fontSize: '11px',
                  color: '#38BDF8',
                  fontWeight: '800',
                  fontFamily: 'var(--font-mono)'
                }}>
                  {fmt(item.fcf)}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00D09C' }}>
                  <span>CFO:</span>
                  <strong>{fmt(item.cfo)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#38BDF8' }}>
                  <span>FCF:</span>
                  <strong>{fmt(item.fcf)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#94A3B8' }}>
                  <span>Net Inc:</span>
                  <strong>{fmt(item.netIncome)}</strong>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
