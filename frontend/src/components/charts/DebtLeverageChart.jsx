import React, { useState } from 'react';
import { Scale, ShieldCheck, AlertTriangle } from 'lucide-react';

export default function DebtLeverageChart({ data, altmanZ, currencySymbol = '$' }) {
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

  // Format currency helpers
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

  // Calculate scales
  const allValues = [];
  data.forEach((d) => {
    allValues.push(d.totalDebt || 0);
    allValues.push(d.cash || 0);
    allValues.push(d.netDebt || 0);
    allValues.push(d.ebitda || 0);
  });

  const minVal = Math.min(0, ...allValues);
  const maxVal = Math.max(1, ...allValues) * 1.15;
  const valRange = maxVal - minVal || 1;

  const getY = (val) => {
    const clamped = Math.max(minVal, Math.min(maxVal, val));
    return padTop + plotHeight - ((clamped - minVal) / valRange) * plotHeight;
  };

  const zeroY = getY(0);

  // X Positions
  const numYears = data.length;
  const stepX = plotWidth / (numYears > 1 ? numYears - 1 : 1);
  const getX = (idx) => padLeft + idx * stepX;

  // Bar dimensions for grouped Total Debt & Cash
  const barWidth = Math.min(24, Math.max(12, plotWidth / (numYears * 3.2)));

  // SVG Line paths for Net Debt
  const netDebtPoints = data.map((d, idx) => `${getX(idx)},${getY(d.netDebt)}`).join(' ');

  // SVG Area path for Net Debt fill
  const netDebtAreaPoints = [
    `${getX(0)},${zeroY}`,
    ...data.map((d, idx) => `${getX(idx)},${getY(d.netDebt)}`),
    `${getX(data.length - 1)},${zeroY}`
  ].join(' ');

  // Latest datapoint
  const latest = data[data.length - 1] || {};
  const isNetCash = (latest.netDebt || 0) <= 0;

  return (
    <div className="chart-container-card full-width-chart">
      {/* Header */}
      <div className="chart-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Scale size={18} color="var(--fintech-primary)" />
          <span className="chart-title">Balance Sheet Leverage & Solvency Trajectory</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {altmanZ && (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '4px 10px',
                borderRadius: '6px',
                background:
                  altmanZ.zone === 'Safe Zone'
                    ? 'var(--gain-green-bg, rgba(0, 208, 156, 0.12))'
                    : 'var(--loss-red-bg, rgba(240, 68, 56, 0.12))',
                border: `1px solid ${
                  altmanZ.zone === 'Safe Zone' ? 'var(--gain-green)' : 'var(--loss-red)'
                }`
              }}
            >
              {altmanZ.zone === 'Safe Zone' ? (
                <ShieldCheck size={14} color="var(--gain-green)" />
              ) : (
                <AlertTriangle size={14} color="var(--loss-red)" />
              )}
              <span
                className="font-mono"
                style={{
                  fontSize: '11px',
                  fontWeight: '700',
                  color: altmanZ.zone === 'Safe Zone' ? 'var(--gain-green)' : 'var(--loss-red)'
                }}
              >
                Altman Z: {altmanZ.value} ({altmanZ.zone})
              </span>
            </div>
          )}

          <div className="chart-legend font-mono" style={{ display: 'flex', gap: '12px', fontSize: '11px' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
              <span style={{ width: '10px', height: '10px', backgroundColor: 'var(--loss-red)', borderRadius: '2px' }} />
              Total Debt
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
              <span style={{ width: '10px', height: '10px', backgroundColor: 'var(--gain-green)', borderRadius: '2px' }} />
              Cash & Equiv
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
              <span style={{ width: '12px', height: '3px', backgroundColor: '#38BDF8', borderRadius: '1px' }} />
              Net Debt
            </span>
          </div>
        </div>
      </div>

      {/* Interactive SVG Chart */}
      <div style={{ position: 'relative', width: '100%', overflowX: 'auto' }}>
        <svg
          viewBox={`0 0 ${svgWidth} ${svgHeight}`}
          style={{ width: '100%', height: 'auto', minWidth: '480px', display: 'block' }}
        >
          {/* Horizontal Gridlines */}
          {[0, 0.25, 0.5, 0.75, 1].map((pct, i) => {
            const val = minVal + pct * valRange;
            const y = getY(val);
            return (
              <g key={i}>
                <line
                  x1={padLeft}
                  y1={y}
                  x2={svgWidth - padRight}
                  y2={y}
                  stroke="var(--fintech-border, #222A3B)"
                  strokeDasharray={val === 0 ? 'none' : '3 3'}
                  strokeWidth={val === 0 ? '1.5' : '1'}
                />
                <text
                  x={padLeft - 10}
                  y={y + 4}
                  textAnchor="end"
                  fill="var(--text-tertiary, #64748B)"
                  fontSize="10"
                  fontFamily="monospace"
                >
                  {fmt(val)}
                </text>
              </g>
            );
          })}

          {/* Grouped Bars: Total Debt (Red) & Cash (Green) */}
          {data.map((d, idx) => {
            const centerX = getX(idx);
            const debtY = getY(d.totalDebt);
            const cashY = getY(d.cash);

            const debtH = Math.max(2, Math.abs(zeroY - debtY));
            const cashH = Math.max(2, Math.abs(zeroY - cashY));

            return (
              <g key={`bars-${idx}`}>
                {/* Total Debt Bar */}
                <rect
                  x={centerX - barWidth - 2}
                  y={Math.min(zeroY, debtY)}
                  width={barWidth}
                  height={debtH}
                  fill="var(--loss-red, #F04438)"
                  opacity={hoveredIdx === idx ? '1' : '0.85'}
                  rx="3"
                />

                {/* Cash Bar */}
                <rect
                  x={centerX + 2}
                  y={Math.min(zeroY, cashY)}
                  width={barWidth}
                  height={cashH}
                  fill="var(--gain-green, #00D09C)"
                  opacity={hoveredIdx === idx ? '1' : '0.85'}
                  rx="3"
                />

                {/* X-Axis Year Labels */}
                <text
                  x={centerX}
                  y={padTop + plotHeight + 20}
                  textAnchor="middle"
                  fill={hoveredIdx === idx ? 'var(--text-primary)' : 'var(--text-secondary)'}
                  fontSize="11"
                  fontWeight={hoveredIdx === idx ? '700' : '500'}
                  fontFamily="monospace"
                >
                  {d.year}
                </text>
              </g>
            );
          })}

          {/* Net Debt Trendline & Area */}
          <polygon
            points={netDebtAreaPoints}
            fill="url(#netDebtGrad)"
            opacity="0.25"
          />

          <polyline
            points={netDebtPoints}
            fill="none"
            stroke="#38BDF8"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Data Points on Net Debt Curve */}
          {data.map((d, idx) => {
            const cx = getX(idx);
            const cy = getY(d.netDebt);
            const isHovered = hoveredIdx === idx;
            return (
              <g
                key={`point-${idx}`}
                onMouseEnter={() => setHoveredIdx(idx)}
                onMouseLeave={() => setHoveredIdx(null)}
                style={{ cursor: 'pointer' }}
              >
                <circle
                  cx={cx}
                  cy={cy}
                  r={isHovered ? 6 : 4}
                  fill="#38BDF8"
                  stroke="#0B0E14"
                  strokeWidth="2"
                />
              </g>
            );
          })}

          {/* Gradient definitions */}
          <defs>
            <linearGradient id="netDebtGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#38BDF8" stopOpacity="0.5" />
              <stop offset="100%" stopColor="#38BDF8" stopOpacity="0.0" />
            </linearGradient>
          </defs>
        </svg>

      </div>

      {/* Breakdown Cards Strip Below Chart */}
      <div style={{ display: 'grid', gridTemplateColumns: `repeat(${data.length}, 1fr)`, gap: '12px', marginTop: '14px' }}>
        {data.map((item, idx) => {
          const isHovered = hoveredIdx === idx;
          const isNetCashItem = (item.netDebt || 0) <= 0;
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
                  color: isNetCashItem ? 'var(--gain-green)' : '#38BDF8',
                  fontWeight: '800',
                  fontFamily: 'var(--font-mono)'
                }}>
                  {item.displayNetDebt || fmt(item.netDebt)}
                </span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', fontSize: '10.5px', fontFamily: 'var(--font-mono)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--loss-red)' }}>
                  <span>Debt:</span>
                  <strong>{item.displayTotalDebt || fmt(item.totalDebt)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--gain-green)' }}>
                  <span>Cash:</span>
                  <strong>{item.displayCash || fmt(item.cash)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                  <span>Debt/EBITDA:</span>
                  <strong>{item.debtToEbitda ? `${item.debtToEbitda}x` : 'N/A'}</strong>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Summary Footer Bar */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
          gap: '12px',
          marginTop: '12px',
          paddingTop: '12px',
          borderTop: '1px solid var(--fintech-border, #222A3B)'
        }}
      >
        <div style={{ background: 'var(--bg-surface, #141923)', padding: '8px 12px', borderRadius: '6px' }}>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', textTransform: 'uppercase' }}>
            Current Net Debt
          </div>
          <div
            className="font-mono"
            style={{
              fontSize: '13px',
              fontWeight: '700',
              color: isNetCash ? 'var(--gain-green)' : '#38BDF8'
            }}
          >
            {latest.displayNetDebt || fmt(latest.netDebt)}
          </div>
        </div>

        <div style={{ background: 'var(--bg-surface, #141923)', padding: '8px 12px', borderRadius: '6px' }}>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', textTransform: 'uppercase' }}>
            Solvency Status
          </div>
          <div
            className="font-mono"
            style={{
              fontSize: '13px',
              fontWeight: '700',
              color: isNetCash ? 'var(--gain-green)' : 'var(--warning-amber)'
            }}
          >
            {isNetCash ? 'Net Cash Fortress' : 'Leveraged Capital'}
          </div>
        </div>

        <div style={{ background: 'var(--bg-surface, #141923)', padding: '8px 12px', borderRadius: '6px' }}>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', textTransform: 'uppercase' }}>
            Debt to EBITDA Multiple
          </div>
          <div
            className="font-mono"
            style={{
              fontSize: '13px',
              fontWeight: '700',
              color: (latest.debtToEbitda || 0) <= 2.5 ? 'var(--gain-green)' : 'var(--loss-red)'
            }}
          >
            {latest.debtToEbitda ? `${latest.debtToEbitda}x` : 'N/A'}
          </div>
        </div>
      </div>
    </div>
  );
}
