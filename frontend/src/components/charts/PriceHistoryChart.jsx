import React, { useState, useEffect } from 'react';
import { fetchStockHistory } from '../../services/api';
import { TrendingUp, TrendingDown, BarChart2, LineChart as LineIcon, Layers, Maximize2 } from 'lucide-react';
import ProChartModal from './ProChartModal';

const TIMEFRAMES = [
  { label: '1D', period: '1d' },
  { label: '5D', period: '5d' },
  { label: '1M', period: '1mo' },
  { label: '6M', period: '6mo' },
  { label: '1Y', period: '1y' },
  { label: '5Y', period: '5y' }
];

export default function PriceHistoryChart({
  ticker,
  currencySymbol = '$',
  fiftyTwoWeekHigh,
  fiftyTwoWeekLow,
  companyName,
  logoUrl
}) {
  // Default to 1D as requested by user
  const [timeframe, setTimeframe] = useState('1d');
  const [chartType, setChartType] = useState('line'); // 'line' | 'bar'
  const [showLevels, setShowLevels] = useState(true); // Toggle Support / Resistance & 52W levels
  const [isModalOpen, setIsModalOpen] = useState(false); // Maximize Pro Modal
  const [historyData, setHistoryData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hoveredPoint, setHoveredPoint] = useState(null);

  useEffect(() => {
    let isMounted = true;
    setIsLoading(true);
    fetchStockHistory(ticker, timeframe)
      .then((data) => {
        if (isMounted) {
          setHistoryData(data);
          setHoveredPoint(null);
          setIsLoading(false);
        }
      })
      .catch((err) => {
        console.error('History fetch error:', err);
        if (isMounted) setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [ticker, timeframe]);

  if (isLoading) {
    return (
      <div className="chart-container-card" style={{ height: '320px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className="spinner" style={{ width: '28px', height: '28px' }} />
      </div>
    );
  }

  if (!historyData || !historyData.points || historyData.points.length === 0) {
    return (
      <div className="chart-container-card" style={{ padding: '24px', textAlign: 'center', color: 'var(--text-tertiary)' }}>
        No historical price data available for {ticker} ({timeframe}).
      </div>
    );
  }

  // For the compact dashboard card view, show the focused window (e.g. today's session for 1D)
  const displayPoints = (historyData.defaultVisibleBars && historyData.points.length > historyData.defaultVisibleBars)
    ? historyData.points.slice(-historyData.defaultVisibleBars)
    : historyData.points;

  const points = displayPoints;
  const isUp = historyData.isUp;
  const strokeColor = isUp ? 'var(--gain-green)' : 'var(--loss-red)';

  const prices = points.map(p => p.close);
  const volumes = points.map(p => p.volume);
  const minPrice = Math.min(...prices);
  const maxPrice = Math.max(...prices);
  const maxVolume = Math.max(...volumes, 1);

  // Key Technical Levels
  const resistance = maxPrice;
  const support = minPrice;

  // Chart SVG Layout
  const svgWidth = 540;
  const svgHeight = 220;
  const padLeft = 12;
  const padRight = 68; // Space for right Y-axis labels & level badges
  const padTop = 16;
  const padBottom = 34; // Space for bottom X-axis time labels
  const volumeHeight = 32;

  const plotWidth = svgWidth - padLeft - padRight;
  const plotHeight = svgHeight - padTop - padBottom - volumeHeight;

  // Buffer Y range by 4% so lines don't hit the borders
  const pricePadding = (maxPrice - minPrice) * 0.05 || (maxPrice * 0.005) || 0.5;
  const yMin = minPrice - pricePadding;
  const yMax = maxPrice + pricePadding;
  const yRange = yMax - yMin || 1;

  // Coordinate helpers
  const getX = (idx) => padLeft + (idx / (points.length - 1 || 1)) * plotWidth;
  const getY = (price) => padTop + plotHeight - ((price - yMin) / yRange) * plotHeight;

  // Generate SVG path for line chart
  const pathD = points.reduce((acc, pt, idx) => {
    const x = getX(idx);
    const y = getY(pt.close);
    return idx === 0 ? `M ${x.toFixed(1)},${y.toFixed(1)}` : `${acc} L ${x.toFixed(1)},${y.toFixed(1)}`;
  }, '');

  const areaD = `${pathD} L ${(padLeft + plotWidth).toFixed(1)},${(padTop + plotHeight).toFixed(1)} L ${padLeft.toFixed(1)},${(padTop + plotHeight).toFixed(1)} Z`;

  // Active Point (hover or latest)
  const activePoint = hoveredPoint || points[points.length - 1];
  const activePrice = activePoint?.close ?? 0;
  const isHovering = hoveredPoint !== null && hoveredPoint !== points[points.length - 1];
  const basePrice = historyData?.firstClose || (points[0]?.close ?? activePrice);
  const activeChange = isHovering
    ? round(activePrice - basePrice, 2)
    : (historyData?.periodChange ?? round(activePrice - basePrice, 2));
  const activeChangePct = isHovering
    ? (basePrice > 0 ? round(((activePrice - basePrice) / basePrice) * 100, 2) : 0)
    : (historyData?.periodChangePercent ?? (basePrice > 0 ? round(((activePrice - basePrice) / basePrice) * 100, 2) : 0));
  const isActiveUp = activeChange >= 0;

  function round(val, dec) {
    return Number(Math.round(val + 'e' + dec) + 'e-' + dec);
  }

  const gradientId = `price-grad-${isUp ? 'up' : 'down'}`;

  // Y-axis tick values (4 clean grid levels)
  const yTicks = [0.08, 0.36, 0.64, 0.92].map(frac => {
    const val = yMin + yRange * frac;
    return {
      val,
      y: padTop + plotHeight * (1 - frac)
    };
  });

  // X-axis time tick points (5 evenly spaced timestamps)
  const xTickIndices = points.length <= 4
    ? points.map((_, i) => i)
    : [
        0,
        Math.floor(points.length * 0.25),
        Math.floor(points.length * 0.5),
        Math.floor(points.length * 0.75),
        points.length - 1
      ];

  const formatTimeLabel = (timeStr) => {
    if (!timeStr) return '';
    if (timeframe === '1d') {
      // Intraday: HH:mm (e.g. "12:45")
      if (timeStr.includes(' ')) return timeStr.split(' ')[1].slice(0, 5);
      if (timeStr.length >= 16) return timeStr.slice(11, 16);
      return timeStr;
    }
    if (timeframe === '5d') {
      // "09-11 12:45" -> "11/09 12:45"
      const parts = timeStr.split(' ');
      const datePart = parts[0] ? parts[0].slice(5) : '';
      const timePart = parts[1] ? parts[1].slice(0, 5) : '';
      return `${datePart} ${timePart}`;
    }
    // Multi-day/month: MM-DD or YYYY-MM
    return timeStr.slice(5, 10);
  };

  return (
    <div className="chart-container-card" style={{ padding: '18px 20px', margin: 0 }}>
      {/* Chart Controls Bar */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px', flexWrap: 'wrap', gap: '8px' }}>
        {/* Timeframe Selector Pills */}
        <div className="timeframe-pill-group">
          {TIMEFRAMES.map((tf) => (
            <button
              key={tf.period}
              onClick={() => setTimeframe(tf.period)}
              className={`timeframe-btn ${timeframe === tf.period ? 'active' : ''}`}
            >
              {tf.label}
            </button>
          ))}
        </div>

        {/* View Controls: Line vs Bar + Levels Toggle + Patterns Toggle + Maximize */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <button
            onClick={() => setShowLevels(!showLevels)}
            className={`chart-type-btn ${showLevels ? 'active' : ''}`}
            style={{ fontSize: '11px', padding: '3px 8px' }}
            title="Toggle Resistance, Support and 52W Levels"
          >
            <Layers size={12} /> {showLevels ? 'Levels: On' : 'Levels: Off'}
          </button>

          <div className="chart-type-pill-group">
            <button
              onClick={() => setChartType('line')}
              className={`chart-type-btn ${chartType === 'line' ? 'active' : ''}`}
            >
              <LineIcon size={12} /> Line
            </button>
            <button
              onClick={() => setChartType('bar')}
              className={`chart-type-btn ${chartType === 'bar' ? 'active' : ''}`}
            >
              <BarChart2 size={12} /> Bar
            </button>
          </div>

          <button
            onClick={() => setIsModalOpen(true)}
            className="chart-type-btn"
            style={{
              fontSize: '11px',
              padding: '3px 9px',
              color: 'var(--fintech-primary)',
              borderColor: 'rgba(0, 208, 156, 0.4)',
              background: 'rgba(0, 208, 156, 0.1)'
            }}
            title="Maximize Pro View & Indicators (Engulfing Candles, Squeeze Momentum, etc.)"
          >
            <Maximize2 size={12} /> Maximize
          </button>
        </div>
      </div>

      {/* Active Price & Change Header */}
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: '8px' }}>
        <div>
          <span style={{ fontSize: '24px', fontWeight: '800', color: 'var(--text-primary)', fontFamily: 'var(--font-mono)', letterSpacing: '-0.5px' }}>
            {currencySymbol}{activePrice.toFixed(2)}
          </span>
          <span style={{
            marginLeft: '10px',
            fontSize: '12.5px',
            color: isActiveUp ? 'var(--gain-green)' : 'var(--loss-red)',
            fontWeight: '700',
            fontFamily: 'var(--font-mono)'
          }}>
            {isActiveUp ? '+' : ''}{activeChange.toFixed(2)} ({isActiveUp ? '+' : ''}{activeChangePct.toFixed(2)}%)
          </span>
        </div>

        <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
          {activePoint?.time}
        </div>
      </div>

      {/* Technical Support / Resistance / 52W Levels Strip */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '8px', fontSize: '11px', fontFamily: 'var(--font-mono)' }}>
        <span style={{
          background: 'rgba(240, 68, 56, 0.12)',
          color: 'var(--loss-red)',
          border: '1px solid rgba(240, 68, 56, 0.25)',
          padding: '2px 7px',
          borderRadius: '4px',
          fontWeight: '700'
        }}>
          Resistance: {currencySymbol}{resistance.toFixed(2)}
        </span>
        <span style={{
          background: 'rgba(0, 208, 156, 0.12)',
          color: 'var(--gain-green)',
          border: '1px solid rgba(0, 208, 156, 0.25)',
          padding: '2px 7px',
          borderRadius: '4px',
          fontWeight: '700'
        }}>
          Support: {currencySymbol}{support.toFixed(2)}
        </span>
        {(fiftyTwoWeekLow && fiftyTwoWeekHigh) && (
          <span style={{
            background: 'var(--bg-surface-elevated)',
            color: 'var(--text-secondary)',
            border: '1px solid var(--border-hairline)',
            padding: '2px 7px',
            borderRadius: '4px'
          }}>
            52W: {currencySymbol}{fiftyTwoWeekLow.toFixed(2)} – {currencySymbol}{fiftyTwoWeekHigh.toFixed(2)}
          </span>
        )}
      </div>

      {/* Interactive SVG Chart */}
      <div style={{ position: 'relative', width: '100%', overflow: 'hidden' }}>
        <svg
          viewBox={`0 0 ${svgWidth} ${svgHeight}`}
          style={{ width: '100%', height: '210px', display: 'block', overflow: 'visible' }}
          onMouseMove={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            const mouseX = e.clientX - rect.left;
            const ratio = Math.max(0, Math.min(1, (mouseX - padLeft) / plotWidth));
            const ptIdx = Math.round(ratio * (points.length - 1));
            if (points[ptIdx]) setHoveredPoint(points[ptIdx]);
          }}
          onMouseLeave={() => setHoveredPoint(null)}
        >
          <defs>
            <linearGradient id="price-grad-up" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#00D09C" stopOpacity="0.25" />
              <stop offset="80%" stopColor="#00D09C" stopOpacity="0.03" />
              <stop offset="100%" stopColor="#00D09C" stopOpacity="0.0" />
            </linearGradient>
            <linearGradient id="price-grad-down" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#F04438" stopOpacity="0.25" />
              <stop offset="80%" stopColor="#F04438" stopOpacity="0.03" />
              <stop offset="100%" stopColor="#F04438" stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Y-Axis Horizontal Gridlines & Price Values on Right */}
          {yTicks.map((tick, i) => (
            <g key={`ytick-${i}`}>
              <line
                x1={padLeft}
                y1={tick.y}
                x2={padLeft + plotWidth}
                y2={tick.y}
                stroke="var(--border-subtle)"
                strokeDasharray="3,3"
                strokeWidth="1"
              />
              {/* Y-Axis Price Label */}
              <text
                x={padLeft + plotWidth + 6}
                y={tick.y + 3.5}
                fill="var(--text-tertiary)"
                fontSize="10"
                fontFamily="var(--font-mono)"
              >
                {currencySymbol}{tick.val >= 1000 ? tick.val.toFixed(0) : tick.val.toFixed(2)}
              </text>
            </g>
          ))}

          {/* Technical Resistance & Support Level Lines */}
          {showLevels && (
            <>
              {/* Resistance Horizontal Line & Badge */}
              <line
                x1={padLeft}
                y1={getY(resistance)}
                x2={padLeft + plotWidth}
                y2={getY(resistance)}
                stroke="#F04438"
                strokeWidth="1.2"
                strokeDasharray="4,3"
                opacity="0.9"
              />
              <rect
                x={padLeft + plotWidth + 4}
                y={getY(resistance) - 8}
                width="60"
                height="15"
                rx="3"
                fill="rgba(240, 68, 56, 0.2)"
                stroke="rgba(240, 68, 56, 0.4)"
                strokeWidth="0.8"
              />
              <text
                x={padLeft + plotWidth + 7}
                y={getY(resistance) + 3}
                fill="#F04438"
                fontSize="9"
                fontWeight="800"
                fontFamily="var(--font-mono)"
              >
                RES {currencySymbol}{resistance >= 1000 ? resistance.toFixed(0) : resistance.toFixed(1)}
              </text>

              {/* Support Horizontal Line & Badge */}
              <line
                x1={padLeft}
                y1={getY(support)}
                x2={padLeft + plotWidth}
                y2={getY(support)}
                stroke="#00D09C"
                strokeWidth="1.2"
                strokeDasharray="4,3"
                opacity="0.9"
              />
              <rect
                x={padLeft + plotWidth + 4}
                y={getY(support) - 8}
                width="60"
                height="15"
                rx="3"
                fill="rgba(0, 208, 156, 0.2)"
                stroke="rgba(0, 208, 156, 0.4)"
                strokeWidth="0.8"
              />
              <text
                x={padLeft + plotWidth + 7}
                y={getY(support) + 3}
                fill="#00D09C"
                fontSize="9"
                fontWeight="800"
                fontFamily="var(--font-mono)"
              >
                SUP {currencySymbol}{support >= 1000 ? support.toFixed(0) : support.toFixed(1)}
              </text>

              {/* 52-Week High (if within visible range) */}
              {fiftyTwoWeekHigh && fiftyTwoWeekHigh >= yMin && fiftyTwoWeekHigh <= yMax && (
                <g>
                  <line
                    x1={padLeft}
                    y1={getY(fiftyTwoWeekHigh)}
                    x2={padLeft + plotWidth}
                    y2={getY(fiftyTwoWeekHigh)}
                    stroke="#F59E0B"
                    strokeWidth="1"
                    strokeDasharray="2,2"
                    opacity="0.8"
                  />
                  <text
                    x={padLeft + plotWidth + 7}
                    y={getY(fiftyTwoWeekHigh) + 3}
                    fill="#F59E0B"
                    fontSize="8.5"
                    fontWeight="800"
                    fontFamily="var(--font-mono)"
                  >
                    52W-H
                  </text>
                </g>
              )}

              {/* 52-Week Low (if within visible range) */}
              {fiftyTwoWeekLow && fiftyTwoWeekLow >= yMin && fiftyTwoWeekLow <= yMax && (
                <g>
                  <line
                    x1={padLeft}
                    y1={getY(fiftyTwoWeekLow)}
                    x2={padLeft + plotWidth}
                    y2={getY(fiftyTwoWeekLow)}
                    stroke="#818CF8"
                    strokeWidth="1"
                    strokeDasharray="2,2"
                    opacity="0.8"
                  />
                  <text
                    x={padLeft + plotWidth + 7}
                    y={getY(fiftyTwoWeekLow) + 3}
                    fill="#818CF8"
                    fontSize="8.5"
                    fontWeight="800"
                    fontFamily="var(--font-mono)"
                  >
                    52W-L
                  </text>
                </g>
              )}
            </>
          )}

          {/* Main Chart Body: Line Mode vs Bar Mode */}
          {chartType === 'line' ? (
            <>
              {/* Area Gradient Fill */}
              <path d={areaD} fill={`url(#${gradientId})`} />
              {/* Main Line Stroke */}
              <path
                d={pathD}
                fill="none"
                stroke={strokeColor}
                strokeWidth="2.2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </>
          ) : (
            /* Bar Chart Mode */
            points.map((pt, idx) => {
              const x = getX(idx);
              const barH = Math.max(3, ((pt.close - yMin) / yRange) * plotHeight);
              const y = padTop + plotHeight - barH;
              const barWidth = Math.max(2, Math.min(6, plotWidth / points.length - 1));
              const isBarUp = idx > 0 ? pt.close >= points[idx - 1].close : true;

              return (
                <rect
                  key={idx}
                  x={x - barWidth / 2}
                  y={y}
                  width={barWidth}
                  height={barH}
                  fill={isBarUp ? 'var(--gain-green)' : 'var(--loss-red)'}
                  opacity={hoveredPoint && hoveredPoint.time === pt.time ? 1 : 0.8}
                  rx="1"
                />
              );
            })
          )}

          {/* Volume Bars at Bottom */}
          {points.map((pt, idx) => {
            const x = getX(idx);
            const vH = (pt.volume / maxVolume) * volumeHeight;
            const y = padTop + plotHeight + volumeHeight - vH;
            const barWidth = Math.max(1.5, Math.min(4, plotWidth / points.length - 1));

            return (
              <rect
                key={`vol-${idx}`}
                x={x - barWidth / 2}
                y={y}
                width={barWidth}
                height={vH}
                fill="rgba(148, 163, 184, 0.2)"
                rx="1"
              />
            );
          })}

          {/* X-Axis Baseline */}
          <line
            x1={padLeft}
            y1={padTop + plotHeight + volumeHeight}
            x2={padLeft + plotWidth}
            y2={padTop + plotHeight + volumeHeight}
            stroke="var(--border-hairline)"
            strokeWidth="1.2"
          />

          {/* X-Axis Time / Timestamp Labels */}
          {xTickIndices.map((idx) => {
            const pt = points[idx];
            if (!pt) return null;
            const x = getX(idx);
            const timeLabel = formatTimeLabel(pt.time);

            return (
              <g key={`xtick-${idx}`}>
                {/* Tick Mark */}
                <line
                  x1={x}
                  y1={padTop + plotHeight + volumeHeight}
                  x2={x}
                  y2={padTop + plotHeight + volumeHeight + 4}
                  stroke="var(--border-hairline)"
                  strokeWidth="1"
                />
                {/* Time Text */}
                <text
                  x={x}
                  y={padTop + plotHeight + volumeHeight + 16}
                  textAnchor={idx === 0 ? 'start' : idx === points.length - 1 ? 'end' : 'middle'}
                  fill="var(--text-tertiary)"
                  fontSize="9.5"
                  fontFamily="var(--font-mono)"
                >
                  {timeLabel}
                </text>
              </g>
            );
          })}

          {/* Hover Crosshair & Data Node */}
          {hoveredPoint && (
            <>
              {(() => {
                const idx = points.findIndex(p => p.time === hoveredPoint.time);
                if (idx === -1) return null;
                const hx = getX(idx);
                const hy = getY(hoveredPoint.close);
                return (
                  <g>
                    <line
                      x1={hx}
                      y1={padTop}
                      x2={hx}
                      y2={padTop + plotHeight + volumeHeight}
                      stroke="var(--text-tertiary)"
                      strokeDasharray="3,3"
                      strokeWidth="1"
                    />
                    <circle
                      cx={hx}
                      cy={hy}
                      r="5"
                      fill="var(--fintech-background, #0B0E14)"
                      stroke={strokeColor}
                      strokeWidth="3"
                    />
                  </g>
                );
              })()}
            </>
          )}
        </svg>
      </div>

      {/* High / Low Range Footer */}
      <div className="chart-range-footer font-mono" style={{ marginTop: '6px', paddingTop: '8px' }}>
        <div>Low: <strong style={{ color: 'var(--text-secondary)' }}>{currencySymbol}{historyData.lowest.toFixed(2)}</strong></div>
        <div>Volume: <strong style={{ color: 'var(--text-secondary)' }}>{activePoint?.volume ? (activePoint.volume / 1e6).toFixed(2) + 'M' : 'N/A'}</strong></div>
        <div>High: <strong style={{ color: 'var(--text-secondary)' }}>{currencySymbol}{historyData.highest.toFixed(2)}</strong></div>
      </div>

      {/* Fullscreen Pro Chart & Indicators Modal */}
      <ProChartModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        ticker={ticker}
        currencySymbol={currencySymbol}
        historyData={historyData}
        timeframe={timeframe}
        setTimeframe={setTimeframe}
        companyName={companyName}
        logoUrl={logoUrl}
        fiftyTwoWeekHigh={fiftyTwoWeekHigh}
        fiftyTwoWeekLow={fiftyTwoWeekLow}
        onGoToDeepAnalysis={() => setIsModalOpen(false)}
      />
    </div>
  );
}
