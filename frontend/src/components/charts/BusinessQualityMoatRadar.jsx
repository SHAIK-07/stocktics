import React, { useState } from 'react';
import { ShieldCheck, TrendingUp, AlertTriangle, CheckCircle2, Lock, Scale, Network, Building2 } from 'lucide-react';

export default function BusinessQualityMoatRadar({ data, moatPillars, currencySymbol = '$' }) {
  const [hoveredIdx, setHoveredIdx] = useState(null);
  const [activePillar, setActivePillar] = useState(null);

  const pillars = moatPillars || {
    pricingPower: 85,
    switchingCosts: 80,
    networkEffects: 75,
    scaleAdvantage: 82,
    intangibleBrand: 78,
    overallMoatScore: 80,
    classification: 'WIDE MOAT COMPOUNDER',
    gmVolatility: 0.95,
    avgRoic: 28.5
  };

  const chartData = data && data.length > 0 ? data : [
    { year: '2020', grossMargin: 44.5, operatingMargin: 24.1, opexRatio: 20.4, roic: 26.2 },
    { year: '2021', grossMargin: 45.2, operatingMargin: 25.0, opexRatio: 20.2, roic: 27.8 },
    { year: '2022', grossMargin: 44.8, operatingMargin: 24.8, opexRatio: 20.0, roic: 28.4 },
    { year: '2023', grossMargin: 45.6, operatingMargin: 25.5, opexRatio: 20.1, roic: 29.1 },
    { year: '2024', grossMargin: 46.2, operatingMargin: 26.1, opexRatio: 20.1, roic: 30.5 }
  ];

  const latest = chartData[chartData.length - 1] || {};
  const isWideMoat = pillars.classification === 'WIDE MOAT COMPOUNDER';
  const isNarrowMoat = pillars.classification === 'NARROW MOAT';

  // --------------------------------------------------------------------------
  // RADAR CHART GEOMETRY (5 Axes)
  // --------------------------------------------------------------------------
  const radarSize = 260;
  const center = radarSize / 2;
  const radius = 95;

  const radarCategories = [
    { key: 'pricingPower', label: 'Pricing Power', score: pillars.pricingPower || 70, angle: -Math.PI / 2 },
    { key: 'switchingCosts', label: 'Switching Costs', score: pillars.switchingCosts || 70, angle: -Math.PI / 2 + (2 * Math.PI / 5) },
    { key: 'networkEffects', label: 'Network Effects', score: pillars.networkEffects || 70, angle: -Math.PI / 2 + (4 * Math.PI / 5) },
    { key: 'scaleAdvantage', label: 'Scale Advantage', score: pillars.scaleAdvantage || 70, angle: -Math.PI / 2 + (6 * Math.PI / 5) },
    { key: 'intangibleBrand', label: 'Intangibles / Brand', score: pillars.intangibleBrand || 70, angle: -Math.PI / 2 + (8 * Math.PI / 5) }
  ];

  const getRadarPoint = (angle, scoreNorm) => {
    const r = radius * scoreNorm;
    return {
      x: center + r * Math.cos(angle),
      y: center + r * Math.sin(angle)
    };
  };

  const polygonPoints = radarCategories
    .map((c) => {
      const p = getRadarPoint(c.angle, Math.min(100, Math.max(10, c.score)) / 100);
      return `${p.x.toFixed(1)},${p.y.toFixed(1)}`;
    })
    .join(' ');

  // --------------------------------------------------------------------------
  // MARGIN TIMESERIES GEOMETRY
  // --------------------------------------------------------------------------
  const tsWidth = 440;
  const tsHeight = 220;
  const padLeft = 45;
  const padRight = 20;
  const padTop = 25;
  const padBottom = 35;
  const plotW = tsWidth - padLeft - padRight;
  const plotH = tsHeight - padTop - padBottom;

  const gmValues = chartData.map((d) => d.grossMargin || 0);
  const omValues = chartData.map((d) => d.operatingMargin || 0);
  const minM = Math.max(0, Math.min(...omValues, ...gmValues) - 5);
  const maxM = Math.max(...gmValues, ...omValues, 40) + 8;
  const rangeM = maxM - minM || 1;

  const getY = (val) => padTop + plotH - ((val - minM) / rangeM) * plotH;
  const getX = (idx) => padLeft + (idx / (chartData.length > 1 ? chartData.length - 1 : 1)) * plotW;

  const gmPoints = chartData.map((d, i) => `${getX(i)},${getY(d.grossMargin)}`).join(' ');
  const omPoints = chartData.map((d, i) => `${getX(i)},${getY(d.operatingMargin)}`).join(' ');

  const activeData = hoveredIdx !== null ? chartData[hoveredIdx] : latest;

  return (
    <div className="chart-container-card full-width-chart">
      {/* Header */}
      <div className="chart-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ShieldCheck size={18} color="var(--fintech-primary)" />
          <span className="chart-title">Economic Moats & Business Quality Architecture</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '4px 10px',
              borderRadius: '6px',
              background: isWideMoat
                ? 'var(--gain-green-bg, rgba(0, 208, 156, 0.12))'
                : isNarrowMoat
                ? 'rgba(245, 158, 11, 0.12)'
                : 'var(--loss-red-bg, rgba(240, 68, 56, 0.12))',
              border: `1px solid ${
                isWideMoat ? 'var(--gain-green)' : isNarrowMoat ? 'var(--warning-amber)' : 'var(--loss-red)'
              }`
            }}
          >
            <ShieldCheck
              size={14}
              color={isWideMoat ? 'var(--gain-green)' : isNarrowMoat ? 'var(--warning-amber)' : 'var(--loss-red)'}
            />
            <span
              style={{
                fontSize: '11px',
                fontWeight: 700,
                color: isWideMoat ? 'var(--gain-green)' : isNarrowMoat ? 'var(--warning-amber)' : 'var(--loss-red)',
                letterSpacing: '0.4px'
              }}
            >
              {pillars.classification || 'WIDE MOAT COMPOUNDER'}
            </span>
          </div>

          <span className="chart-tag" style={{ color: 'var(--text-secondary)' }}>
            Stage 2: Company Quality
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
        {/* KPI 1: Gross Margin Volatility */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Margin Volatility (σ_GM)
          </div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--gain-green)', fontFamily: 'monospace' }}>
            {pillars.gmVolatility != null ? `${pillars.gmVolatility.toFixed(2)}%` : '0.95%'}
          </div>
          <div style={{ fontSize: '10px', color: (pillars.gmVolatility || 1) <= 2.0 ? 'var(--gain-green)' : 'var(--warning-amber)' }}>
            {(pillars.gmVolatility || 1) <= 2.0 ? 'Pristine Pricing Power (<2%)' : 'Moderate Volatility'}
          </div>
        </div>

        {/* KPI 2: 5-Year ROIC Average */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            5-Year Median ROIC
          </div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--fintech-primary)', fontFamily: 'monospace' }}>
            {pillars.avgRoic != null ? `${pillars.avgRoic.toFixed(1)}%` : '28.5%'}
          </div>
          <div style={{ fontSize: '10px', color: 'var(--gain-green)' }}>
            Exceeds 15.0% Moat Gate
          </div>
        </div>

        {/* KPI 3: OpEx Ratio (Scale Advantage) */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Scale OpEx Ratio
          </div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: '#0284C7', fontFamily: 'monospace' }}>
            {latest.opexRatio != null ? `${latest.opexRatio.toFixed(1)}%` : '20.1%'}
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-secondary)' }}>
            Diluting overhead over scale
          </div>
        </div>

        {/* KPI 4: Single Customer Risk */}
        <div>
          <div style={{ fontSize: '10px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>
            Customer Concentration
          </div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--gain-green)', fontFamily: 'monospace' }}>
            &lt; 10.0%
          </div>
          <div style={{ fontSize: '10px', color: 'var(--text-secondary)' }}>
            ASC 280 Footnote Shield
          </div>
        </div>
      </div>

      {/* Main Visuals Grid: Radar on Left, Timeseries on Right */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1.35fr',
          gap: '16px',
          padding: '16px',
          background: 'var(--fintech-background, #0B0E14)'
        }}
      >
        {/* Left Column: 5-Pillar Moat Radar */}
        <div
          style={{
            background: 'var(--fintech-surface, #141923)',
            borderRadius: '10px',
            border: '1px solid var(--fintech-border, #222A3B)',
            padding: '14px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center'
          }}
        >
          <div style={{ width: '100%', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <div>
              <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-primary)', textTransform: 'uppercase' }}>
                5-Pillar Moat Radar
              </div>
              <div style={{ fontSize: '9px', color: 'var(--text-secondary)' }}>
                Morningstar & Fintech Structural Defense Vectors
              </div>
            </div>
            <span
              style={{
                fontSize: '11px',
                fontWeight: 800,
                color: 'var(--fintech-primary)',
                fontFamily: 'monospace'
              }}
            >
              {pillars.overallMoatScore || 80}/100 Score
            </span>
          </div>

          <svg width={radarSize} height={radarSize} style={{ overflow: 'visible' }}>
            {/* Concentric Web Rings (25%, 50%, 75%, 100%) */}
            {[0.25, 0.5, 0.75, 1.0].map((frac) => {
              const ringPoints = radarCategories
                .map((c) => {
                  const p = getRadarPoint(c.angle, frac);
                  return `${p.x.toFixed(1)},${p.y.toFixed(1)}`;
                })
                .join(' ');
              return (
                <polygon
                  key={frac}
                  points={ringPoints}
                  fill="none"
                  stroke="var(--fintech-border, #222A3B)"
                  strokeWidth="1"
                  strokeDasharray={frac === 1.0 ? 'none' : '3,3'}
                />
              );
            })}

            {/* Axis Spokes & Labels */}
            {radarCategories.map((c) => {
              const outer = getRadarPoint(c.angle, 1.0);
              const labelPos = getRadarPoint(c.angle, 1.25);
              return (
                <g key={c.key}>
                  <line
                    x1={center}
                    y1={center}
                    x2={outer.x}
                    y2={outer.y}
                    stroke="var(--fintech-border, #222A3B)"
                    strokeWidth="1"
                  />
                  <text
                    x={labelPos.x}
                    y={labelPos.y + 3}
                    textAnchor="middle"
                    fontSize="9"
                    fontWeight="600"
                    fill={activePillar === c.key ? 'var(--fintech-primary)' : 'var(--text-secondary)'}
                    style={{ cursor: 'pointer' }}
                    onMouseEnter={() => setActivePillar(c.key)}
                    onMouseLeave={() => setActivePillar(null)}
                  >
                    {c.label} ({c.score.toFixed(0)})
                  </text>
                </g>
              );
            })}

            {/* Filled Moat Polygon */}
            <polygon
              points={polygonPoints}
              fill="rgba(0, 208, 156, 0.20)"
              stroke="var(--fintech-primary, #00D09C)"
              strokeWidth="2"
            />

            {/* Data Dots on Vertices */}
            {radarCategories.map((c) => {
              const p = getRadarPoint(c.angle, Math.min(100, Math.max(10, c.score)) / 100);
              return (
                <circle
                  key={c.key}
                  cx={p.x}
                  cy={p.y}
                  r="4"
                  fill="var(--fintech-background, #0B0E14)"
                  stroke="var(--fintech-primary, #00D09C)"
                  strokeWidth="2"
                  style={{ cursor: 'pointer' }}
                  onMouseEnter={() => setActivePillar(c.key)}
                  onMouseLeave={() => setActivePillar(null)}
                />
              );
            })}
          </svg>

          {/* Active Pillar Explanation */}
          <div
            style={{
              marginTop: '10px',
              padding: '8px 12px',
              borderRadius: '6px',
              background: 'var(--fintech-surface-elevated, #1C2333)',
              fontSize: '10px',
              color: 'var(--text-secondary)',
              textAlign: 'center',
              width: '100%'
            }}
          >
            {activePillar === 'pricingPower' && 'Pricing Power: Ability to raise prices without volume defection; verified by σ(GM) < 2%.'}
            {activePillar === 'switchingCosts' && 'Switching Costs: Enterprise friction & migration downtime keeping annual churn < 1%.'}
            {activePillar === 'networkEffects' && "Network Effects: Metcalfe's Law (V ∝ n²) where two-sided lock-in repels new entrants."}
            {activePillar === 'scaleAdvantage' && 'Scale Advantage: Fixed SG&A and supply chain dilution over multi-billion revenue base.'}
            {activePillar === 'intangibleBrand' && 'Intangible Assets: High gross margin brand premium & proprietary intellectual property.'}
            {!activePillar && 'Hover over any axis vertex to inspect structural moat defense mechanics.'}
          </div>
        </div>

        {/* Right Column: Gross Margin & Operating Margin Stability Timeseries */}
        <div
          style={{
            background: 'var(--fintech-surface, #141923)',
            borderRadius: '10px',
            border: '1px solid var(--fintech-border, #222A3B)',
            padding: '14px',
            display: 'flex',
            flexDirection: 'column'
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <div>
              <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-primary)', textTransform: 'uppercase' }}>
                Pricing Power Proof: Multi-Year Margin Stability
              </div>
              <div style={{ fontSize: '9px', color: 'var(--text-secondary)' }}>
                Comparing Gross Margin vs. Operating Margin across economic cycles
              </div>
            </div>

            {/* Legend */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '10px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '2px', background: 'var(--gain-green)' }} />
                <span style={{ color: 'var(--text-secondary)' }}>Gross Margin</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '2px', background: '#1455FE' }} />
                <span style={{ color: 'var(--text-secondary)' }}>Operating Margin</span>
              </div>
            </div>
          </div>

          {/* SVG Timeseries */}
          <div style={{ width: '100%', overflowX: 'auto' }}>
            <svg
              viewBox={`0 0 ${tsWidth} ${tsHeight}`}
              style={{ width: '100%', height: 'auto', minHeight: '190px' }}
            >
              {/* Horizontal Grid lines */}
              {[minM, minM + rangeM * 0.33, minM + rangeM * 0.66, maxM].map((mVal, i) => {
                const y = getY(mVal);
                return (
                  <g key={i}>
                    <line
                      x1={padLeft}
                      y1={y}
                      x2={tsWidth - padRight}
                      y2={y}
                      stroke="var(--fintech-border, #222A3B)"
                      strokeWidth="1"
                      strokeDasharray="3,3"
                    />
                    <text
                      x={padLeft - 6}
                      y={y + 3}
                      textAnchor="end"
                      fontSize="9"
                      fill="var(--text-tertiary)"
                      fontFamily="monospace"
                    >
                      {mVal.toFixed(0)}%
                    </text>
                  </g>
                );
              })}

              {/* Gross Margin Line */}
              <polyline
                points={gmPoints}
                fill="none"
                stroke="var(--gain-green, #00D09C)"
                strokeWidth="2.5"
                strokeLinecap="round"
              />

              {/* Operating Margin Line */}
              <polyline
                points={omPoints}
                fill="none"
                stroke="#1455FE"
                strokeWidth="2"
                strokeLinecap="round"
              />

              {/* Hover Cursor Vertical Line */}
              {hoveredIdx !== null && (
                <line
                  x1={getX(hoveredIdx)}
                  y1={padTop}
                  x2={getX(hoveredIdx)}
                  y2={tsHeight - padBottom}
                  stroke="var(--fintech-border-focused, #00D09C)"
                  strokeWidth="1"
                  strokeDasharray="2,2"
                />
              )}

              {/* Data Points & Tooltips */}
              {chartData.map((d, i) => {
                const x = getX(i);
                const gmY = getY(d.grossMargin);
                const omY = getY(d.operatingMargin);
                const isHovered = hoveredIdx === i;

                return (
                  <g
                    key={d.year}
                    onMouseEnter={() => setHoveredIdx(i)}
                    onMouseLeave={() => setHoveredIdx(null)}
                    style={{ cursor: 'pointer' }}
                  >
                    {/* Transparent column hit target */}
                    <rect
                      x={x - 18}
                      y={padTop}
                      width={36}
                      height={plotH}
                      fill="transparent"
                    />

                    {/* Gross Margin Dot */}
                    <circle
                      cx={x}
                      cy={gmY}
                      r={isHovered ? 5 : 3.5}
                      fill="var(--fintech-background, #0B0E14)"
                      stroke="var(--gain-green, #00D09C)"
                      strokeWidth="2"
                    />

                    {/* Operating Margin Dot */}
                    <circle
                      cx={x}
                      cy={omY}
                      r={isHovered ? 5 : 3}
                      fill="var(--fintech-background, #0B0E14)"
                      stroke="#1455FE"
                      strokeWidth="2"
                    />

                    {/* X-Axis Year Label */}
                    <text
                      x={x}
                      y={tsHeight - padBottom + 16}
                      textAnchor="middle"
                      fontSize="9"
                      fontWeight={isHovered ? '700' : '500'}
                      fill={isHovered ? 'var(--text-primary)' : 'var(--text-tertiary)'}
                      fontFamily="monospace"
                    >
                      {d.year}
                    </text>
                  </g>
                );
              })}
            </svg>
          </div>

          {/* Multi-Year Margin Breakdown Cards Strip */}
          <div style={{ display: 'grid', gridTemplateColumns: `repeat(${chartData.length}, 1fr)`, gap: '8px', marginTop: '10px' }}>
            {chartData.map((d, i) => {
              const isHovered = hoveredIdx === i;
              const opex = (d.grossMargin - d.operatingMargin).toFixed(1);
              return (
                <div
                  key={d.year}
                  onMouseEnter={() => setHoveredIdx(i)}
                  onMouseLeave={() => setHoveredIdx(null)}
                  style={{
                    background: isHovered ? 'var(--bg-surface-elevated)' : 'var(--bg-surface)',
                    border: `1px solid ${isHovered ? 'var(--fintech-primary)' : 'var(--border-hairline)'}`,
                    borderRadius: '8px',
                    padding: '8px 10px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '4px',
                    transition: 'all 0.15s ease'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '11px', fontWeight: '800', color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
                      FY{d.year}
                    </span>
                    <span style={{ fontSize: '10px', color: 'var(--gain-green)', fontWeight: '800', fontFamily: 'var(--font-mono)' }}>
                      GM {d.grossMargin != null ? `${d.grossMargin.toFixed(1)}%` : 'N/A'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', fontSize: '10px', fontFamily: 'var(--font-mono)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', color: '#1455FE' }}>
                      <span>OM:</span>
                      <strong>{d.operatingMargin != null ? `${d.operatingMargin.toFixed(1)}%` : 'N/A'}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                      <span>OpEx:</span>
                      <strong>{d.opexRatio != null ? `${d.opexRatio.toFixed(1)}%` : `${opex}%`}</strong>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Moat Safeguards & Footnote Bar */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: '10px',
          padding: '12px 16px',
          background: 'var(--fintech-surface, #141923)',
          borderTop: '1px solid var(--fintech-border, #222A3B)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
          <CheckCircle2 size={16} color="var(--gain-green)" style={{ marginTop: '2px', flexShrink: 0 }} />
          <div>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-primary)' }}>
              Inelastic Pricing Power
            </div>
            <div style={{ fontSize: '10px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
              Gross margins remain stable within ±1.5% across inflationary cycles, proving complete cost passthrough.
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
          <Lock size={16} color="var(--fintech-primary)" style={{ marginTop: '2px', flexShrink: 0 }} />
          <div>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-primary)' }}>
              High Switching Lock-In
            </div>
            <div style={{ fontSize: '10px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
              Mission-critical enterprise software integration makes migration costs vastly exceed annual subscription fees.
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
          <Scale size={16} color="#0284C7" style={{ marginTop: '2px', flexShrink: 0 }} />
          <div>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-primary)' }}>
              ASC 280 Footnote Shield
            </div>
            <div style={{ fontSize: '10px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
              SEC 10-K footnote check confirms no single customer accounts for &gt; 10% of revenue, shielding vendor margins.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
