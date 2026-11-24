import React, { useState } from 'react';
import { ShieldCheck } from 'lucide-react';

export default function SolvencyStructureChart({ data, currencySymbol = '$' }) {
  const [viewMode, setViewMode] = useState('both'); // 'both' | 'bar' | 'line'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  if (!data || data.length === 0) return null;

  // Layout parameters for SVG
  const svgWidth = 720;
  const svgHeight = 260;
  const padLeft = 70;
  const padRight = 40;
  const padTop = 30;
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
      return { ticks: [0, 25, 50, 75, 100], minVal: 0, maxVal: 100 };
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

    const ticks = [];
    for (let t = niceMin; t <= niceMax + step * 0.0001; t += step) {
      const rounded = Math.abs(t) < step * 0.0001 ? 0 : Number(t.toFixed(4));
      ticks.push(rounded);
    }
    return { ticks, minVal: ticks[0], maxVal: ticks[ticks.length - 1] };
  };

  // Extract all values (Assets, Liabilities, Equity)
  const allValues = data.flatMap(d => [d.assets, d.liabilities, d.equity].filter(v => v != null && !isNaN(v)));
  const dataMax = allValues.length > 0 ? Math.max(...allValues) : 1;
  const dataMin = allValues.length > 0 ? Math.min(...allValues) : 0;

  const { ticks, minVal, maxVal } = getNiceTicks(dataMin, dataMax, 5);
  const valRange = maxVal - minVal || 1;

  // Y-scale helper
  const getY = (val) => {
    const clamped = Math.max(minVal, Math.min(maxVal, val || 0));
    return padTop + ((maxVal - clamped) / valRange) * plotHeight;
  };

  const yZero = getY(0);

  // Bar geometry calculation for positive/negative values
  const getBarGeom = (val) => {
    const num = val || 0;
    const yVal = getY(num);
    const top = Math.min(yZero, yVal);
    const height = Math.max(4, Math.abs(yVal - yZero));
    return { top, height, isNegative: num < 0 };
  };

  // X positions for each year
  const colStep = plotWidth / data.length;
  const getXCenter = (idx) => padLeft + colStep * idx + colStep / 2;

  // Generate path data for lines
  const assetsPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.assets)}`).join(' ');
  const equityPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.equity)}`).join(' ');
  const liabPoints = data.map((d, i) => `${getXCenter(i)},${getY(d.liabilities)}`).join(' ');

  // Bar dimensions
  const barWidth = Math.min(32, (colStep - 28) / 2);

  return (
    <div className="chart-container-card full-width-chart">
      {/* Header with Title, Mode Switcher & Legend */}
      <div className="chart-header" style={{ flexWrap: 'wrap', gap: '12px' }}>
        <div className="chart-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ShieldCheck size={18} color="var(--fintech-primary)" />
          <span>Balance Sheet Capital Structure (Assets vs Equity & Liabilities)</span>
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
          {(viewMode === 'bar' || viewMode === 'both') && (
            <>
              <div className="legend-item">
                <span className="legend-color-dot" style={{ background: '#38BDF8' }} />
                <span>Equity (Bar)</span>
              </div>
              <div className="legend-item">
                <span className="legend-color-dot" style={{ background: '#F04438' }} />
                <span>Liabilities (Bar)</span>
              </div>
            </>
          )}
          {(viewMode === 'line' || viewMode === 'both') && (
            <div className="legend-item">
              <span style={{ width: '14px', height: '3px', background: '#00D09C', borderRadius: '2px', display: 'inline-block' }} />
              <span style={{ color: '#00D09C', fontWeight: '700' }}>Total Assets (Line)</span>
            </div>
          )}
        </div>
      </div>

      {/* SVG Chart */}
      <div style={{ position: 'relative', width: '100%', overflowX: 'auto' }}>
        <svg
          viewBox={`0 0 ${svgWidth} ${svgHeight}`}
          style={{ width: '100%', height: 'auto', minWidth: '560px', display: 'block' }}
        >
          {/* Horizontal Gridlines */}
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
                  {fmt(val)}
                </text>
              </g>
            );
          })}

          {/* Grouped Vertical Bars (Side-by-Side: Equity on left, Liabilities on right) */}
          {(viewMode === 'bar' || viewMode === 'both') &&
            data.map((item, idx) => {
              const xCenter = getXCenter(idx);
              const eqGeom = getBarGeom(item.equity);
              const liabGeom = getBarGeom(item.liabilities);
              const isHovered = hoveredIdx === idx;

              const eqX = xCenter - barWidth - 3;
              const liabX = xCenter + 3;

              return (
                <g
                  key={`bars-${item.year}`}
                  onMouseEnter={() => setHoveredIdx(idx)}
                  onMouseLeave={() => setHoveredIdx(null)}
                  style={{ cursor: 'pointer' }}
                >
                  {/* Subtle column background highlight on hover */}
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

                  {/* Equity Bar (Left) */}
                  <rect
                    x={eqX}
                    y={eqGeom.top}
                    width={barWidth}
                    height={eqGeom.height}
                    fill={eqGeom.isNegative ? (isHovered ? '#FB7185' : '#E11D48') : (isHovered ? '#60A5FA' : '#38BDF8')}
                    rx="4"
                    opacity={isHovered ? 1 : 0.9}
                  />

                  {/* Liabilities Bar (Right) */}
                  <rect
                    x={liabX}
                    y={liabGeom.top}
                    width={barWidth}
                    height={liabGeom.height}
                    fill={isHovered ? '#F87171' : '#F04438'}
                    rx="4"
                    opacity={isHovered ? 1 : 0.9}
                  />
                </g>
              );
            })}

          {/* Line Charts (Overlay) */}
          {(viewMode === 'line' || viewMode === 'both') && (
            <>
              {/* Total Assets Line (Top line connecting total assets) */}
              <polyline
                points={assetsPoints}
                fill="none"
                stroke="#00D09C"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                style={{ filter: 'drop-shadow(0 2px 6px rgba(0, 208, 156, 0.45))' }}
              />

              {/* In Line-only mode, also draw Equity & Liabilities lines */}
              {viewMode === 'line' && (
                <>
                  <polyline
                    points={equityPoints}
                    fill="none"
                    stroke="#38BDF8"
                    strokeWidth="2"
                    strokeDasharray="4,3"
                  />
                  <polyline
                    points={liabPoints}
                    fill="none"
                    stroke="#F04438"
                    strokeWidth="2"
                    strokeDasharray="4,3"
                  />
                </>
              )}

              {/* Data Node Dots */}
              {data.map((item, idx) => {
                const x = getXCenter(idx);
                const y = getY(item.assets);
                const isHovered = hoveredIdx === idx;
                return (
                  <g key={`dot-${item.year}`}>
                    <circle
                      cx={x}
                      cy={y}
                      r={isHovered ? 6 : 4.5}
                      fill="var(--fintech-background, #0B0E14)"
                      stroke="#00D09C"
                      strokeWidth="2.5"
                    />
                    {/* Top Value Label for Total Assets */}
                    <text
                      x={x}
                      y={y - 9}
                      textAnchor="middle"
                      fill="#00D09C"
                      fontSize="10.5"
                      fontWeight="700"
                      fontFamily="var(--font-mono)"
                    >
                      {item.displayAssets}
                    </text>
                  </g>
                );
              })}
            </>
          )}

          {/* X-Axis Baseline */}
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
              >
                FY{item.year}
              </text>
            );
          })}
        </svg>
      </div>

      {/* Metric Breakdown Cards Strip Below Chart */}
      <div style={{ display: 'grid', gridTemplateColumns: `repeat(${data.length}, 1fr)`, gap: '12px', marginTop: '14px' }}>
        {data.map((item, idx) => {
          const total = item.assets || 1;
          const eqPct = Math.min(100, Math.max(0, (item.equity / total) * 100));
          const liabPct = Math.min(100, Math.max(0, (item.liabilities / total) * 100));
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
                <span style={{ fontSize: '10.5px', color: '#00D09C', fontWeight: '700', fontFamily: 'var(--font-mono)' }}>
                  {item.displayAssets}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: (item.equity || 0) < 0 ? 'var(--loss-red)' : '#38BDF8' }}>
                  <span>Equity:</span>
                  <strong>{item.displayEquity || fmt(item.equity)} ({eqPct.toFixed(1)}%)</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#F04438' }}>
                  <span>Liabilities:</span>
                  <strong>{item.displayLiabilities} ({liabPct.toFixed(1)}%)</strong>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
