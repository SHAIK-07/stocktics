import React, { useState } from 'react';
import { BarChart2, TrendingUp } from 'lucide-react';

export default function RevenueGrowthChart({ data, currencySymbol = '$' }) {
  const [viewMode, setViewMode] = useState('both'); // 'both' | 'bar' | 'line'
  const [hoveredIdx, setHoveredIdx] = useState(null);

  if (!data || data.length === 0) return null;

  const svgWidth = 720;
  const svgHeight = 260;
  const padLeft = 70;
  const padRight = 50;
  const padTop = 35;
  const padBottom = 45;
  const plotWidth = svgWidth - padLeft - padRight;
  const plotHeight = svgHeight - padTop - padBottom;

  const maxRevenue = Math.max(...data.map(d => d.revenue || 0), 1);
  const colStep = plotWidth / data.length;
  const getXCenter = (idx) => padLeft + colStep * idx + colStep / 2;

  // Y-scale for Revenue
  const getYRev = (val) => padTop + plotHeight - (Math.max(0, val) / maxRevenue) * plotHeight;

  // Line points for Revenue
  const revPoints = data.map((d, i) => `${getXCenter(i)},${getYRev(d.revenue)}`).join(' ');

  const barWidth = Math.min(52, colStep - 36);

  return (
    <div className="chart-container-card full-width-chart">
      {/* Header with Title, Mode Switcher & Legend */}
      <div className="chart-header" style={{ flexWrap: 'wrap', gap: '12px' }}>
        <div className="chart-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <BarChart2 size={18} color="var(--fintech-primary)" />
          <span>Multi-Year Audited Revenue & YoY Growth Trajectory</span>
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
          {(viewMode === 'bar' || viewMode === 'both') && (
            <div className="legend-item">
              <span className="legend-color-dot" style={{ background: '#00D09C' }} />
              <span>Audited Revenue (Bar)</span>
            </div>
          )}
          {(viewMode === 'line' || viewMode === 'both') && (
            <div className="legend-item">
              <span style={{ width: '14px', height: '3px', background: '#38BDF8', borderRadius: '2px', display: 'inline-block' }} />
              <span style={{ color: '#38BDF8', fontWeight: '700' }}>Revenue Trend (Line)</span>
            </div>
          )}
        </div>
      </div>

      {/* SVG Plot */}
      <div style={{ position: 'relative', width: '100%', overflowX: 'auto' }}>
        <svg
          viewBox={`0 0 ${svgWidth} ${svgHeight}`}
          style={{ width: '100%', height: 'auto', minWidth: '560px', display: 'block' }}
        >
          {/* Horizontal Gridlines */}
          {[0, 0.25, 0.5, 0.75, 1.0].map((frac, i) => {
            const y = padTop + plotHeight * (1 - frac);
            const val = maxRevenue * frac;
            return (
              <g key={i}>
                <line
                  x1={padLeft}
                  y1={y}
                  x2={svgWidth - padRight}
                  y2={y}
                  stroke="var(--border-subtle)"
                  strokeDasharray="4,4"
                  strokeWidth="1"
                />
                <text
                  x={padLeft - 10}
                  y={y + 4}
                  textAnchor="end"
                  fill="var(--text-tertiary)"
                  fontSize="10.5"
                  fontFamily="var(--font-mono)"
                >
                  {val >= 1e12 ? `${(val / 1e12).toFixed(1)}T` : val >= 1e9 ? `${(val / 1e9).toFixed(0)}B` : val >= 1e7 ? `${(val / 1e7).toFixed(0)}Cr` : val.toFixed(0)}
                </text>
              </g>
            );
          })}

          {/* Vertical Bars for Revenue */}
          {(viewMode === 'bar' || viewMode === 'both') &&
            data.map((item, idx) => {
              const xCenter = getXCenter(idx);
              const barH = Math.max(6, (item.revenue / maxRevenue) * plotHeight);
              const barX = xCenter - barWidth / 2;
              const barY = padTop + plotHeight - barH;
              const isHovered = hoveredIdx === idx;

              return (
                <g
                  key={`bar-${item.year}`}
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

                  <rect
                    x={barX}
                    y={barY}
                    width={barWidth}
                    height={barH}
                    fill={isHovered ? '#00E5AC' : 'url(#revBarGradient)'}
                    rx="6"
                    stroke="rgba(0, 208, 156, 0.7)"
                    strokeWidth="1"
                    style={{
                      filter: isHovered ? 'drop-shadow(0 4px 14px rgba(0, 208, 156, 0.4))' : 'drop-shadow(0 2px 8px rgba(0, 208, 156, 0.15))',
                      transition: 'all 0.2s ease'
                    }}
                  />

                  {/* Revenue Amount Label atop bar */}
                  <text
                    x={xCenter}
                    y={barY - 8}
                    textAnchor="middle"
                    fill="var(--text-primary)"
                    fontSize="11"
                    fontWeight="800"
                    fontFamily="var(--font-mono)"
                  >
                    {item.displayRevenue}
                  </text>
                </g>
              );
            })}

          {/* Line Chart Overlay */}
          {(viewMode === 'line' || viewMode === 'both') && (
            <>
              <polyline
                points={revPoints}
                fill="none"
                stroke="#38BDF8"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                style={{ filter: 'drop-shadow(0 2px 8px rgba(56, 189, 248, 0.5))' }}
              />

              {data.map((item, idx) => {
                const x = getXCenter(idx);
                const y = getYRev(item.revenue);
                const isHovered = hoveredIdx === idx;
                return (
                  <circle
                    key={`dot-${item.year}`}
                    cx={x}
                    cy={y}
                    r={isHovered ? 6 : 4.5}
                    fill="var(--fintech-background, #0B0E14)"
                    stroke="#38BDF8"
                    strokeWidth="2.5"
                  />
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

          {/* X-Axis Year Labels & YoY Badges */}
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

          <defs>
            <linearGradient id="revBarGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#00D09C" stopOpacity="0.95" />
              <stop offset="100%" stopColor="#00B386" stopOpacity="0.45" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      {/* Cards Strip Below Chart */}
      <div style={{ display: 'grid', gridTemplateColumns: `repeat(${data.length}, 1fr)`, gap: '12px', marginTop: '14px' }}>
        {data.map((item, idx) => {
          const isHovered = hoveredIdx === idx;
          const isPos = item.yoyGrowth > 0;
          const isNeg = item.yoyGrowth < 0;

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
                  fontSize: '10.5px',
                  fontWeight: '800',
                  fontFamily: 'var(--font-mono)',
                  color: isPos ? 'var(--gain-green)' : isNeg ? 'var(--loss-red)' : 'var(--text-tertiary)',
                  background: isPos ? 'var(--gain-green-bg)' : isNeg ? 'var(--loss-red-bg)' : 'transparent',
                  padding: '1px 5px',
                  borderRadius: '3px'
                }}>
                  {idx === 0 ? 'Base' : isPos ? `+${item.yoyGrowth}%` : `${item.yoyGrowth}%`}
                </span>
              </div>

              <div style={{ fontSize: '13px', fontWeight: '800', color: 'var(--fintech-primary)', fontFamily: 'var(--font-mono)' }}>
                {item.displayRevenue}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
