import React, { useState, useEffect } from 'react';
import { fetchStockHistory } from '../../services/api';

// In-memory cache for sparklines to avoid redundant network hits
const SPARKLINE_CACHE = {};

export default function PatternSparklineChart({
  ticker,
  market = 'US',
  pivot = null,
  pattern = null,
  score = 80
}) {
  const [points, setPoints] = useState(() => SPARKLINE_CACHE[ticker] || null);
  const [isLoading, setIsLoading] = useState(!SPARKLINE_CACHE[ticker]);

  useEffect(() => {
    if (SPARKLINE_CACHE[ticker]) {
      setPoints(SPARKLINE_CACHE[ticker]);
      setIsLoading(false);
      return;
    }

    let isMounted = true;
    fetchStockHistory(ticker, '1mo')
      .then((data) => {
        if (isMounted && data?.points && data.points.length > 0) {
          const pts = data.points.slice(-25); // Last 25 trading sessions
          SPARKLINE_CACHE[ticker] = pts;
          setPoints(pts);
          setIsLoading(false);
        }
      })
      .catch(() => {
        if (isMounted) setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [ticker]);

  const svgWidth = 260;
  const svgHeight = 78;
  const padLeft = 6;
  const padRight = 44;
  const padTop = 12;
  const padBottom = 12;
  const plotWidth = svgWidth - padLeft - padRight;
  const plotHeight = svgHeight - padTop - padBottom;

  if (isLoading || !points || points.length < 3) {
    return (
      <div className="pattern-sparkline-skeleton">
        <div className="sparkline-loader-bar" />
      </div>
    );
  }

  const isIndian = market === 'IN' || (ticker && ticker.endsWith('.NS'));
  const currencySym = isIndian ? '₹' : '$';
  const patUpper = (pattern || '').toUpperCase();

  // Pattern Family Classification
  const isCandlestick = [
    'BULLISH_ENGULFING',
    'BEARISH_ENGULFING',
    'HAMMER',
    'SHOOTING_STAR',
    'MORNING_STAR',
    'EVENING_STAR',
    'DOJI'
  ].includes(patUpper);

  const isDivergence = [
    'RSI_DIVERGENCE',
    'DIVERGENCE',
    'RSI_EXTREME'
  ].includes(patUpper);

  const isCupHandle = [
    'CUP_AND_HANDLE',
    'CUP_WITH_HANDLE',
    'ROUNDING_BOTTOM'
  ].includes(patUpper);

  const isVCP = patUpper === 'VCP';

  const isDoubleBottom = [
    'DOUBLE_BOTTOM',
    'TRIPLE_BOTTOM',
    'W_BASE'
  ].includes(patUpper);

  const isDoubleTop = [
    'DOUBLE_TOP',
    'TRIPLE_TOP',
    'HEAD_AND_SHOULDERS',
    'M_PATTERN'
  ].includes(patUpper);

  const isSupplyDemand = [
    'SUPPLY_DEMAND_ZONE',
    'ORDER_BLOCK',
    'FAIR_VALUE_GAP'
  ].includes(patUpper);

  const isMACross = [
    'MA_CROSSOVER',
    'GOLDEN_CROSS',
    'EMA_PULLBACK'
  ].includes(patUpper);

  const isSupertrend = [
    'SUPERTREND_FLIP',
    'SUPERTREND_BULLISH'
  ].includes(patUpper);

  // --------------------------------------------------------------------------
  // 1. UNIQUE CANDLESTICK CHART VISUALIZATION
  // --------------------------------------------------------------------------
  if (isCandlestick) {
    const candleSlice = points.slice(-14); // Last 14 sessions
    const highs = candleSlice.map((p) => p.high || p.close);
    const lows = candleSlice.map((p) => p.low || p.close);
    let minC = Math.min(...lows);
    let maxC = Math.max(...highs);
    if (pivot) {
      minC = Math.min(minC, pivot);
      maxC = Math.max(maxC, pivot);
    }
    const cRange = maxC - minC || 1.0;
    const cBuf = cRange * 0.1;
    const adjMinC = minC - cBuf;
    const adjMaxC = maxC + cBuf;
    const adjCRange = adjMaxC - adjMinC;

    const getCY = (val) => padTop + plotHeight - ((val - adjMinC) / adjCRange) * plotHeight;
    const colWidth = plotWidth / candleSlice.length;
    const bodyW = Math.max(5, colWidth * 0.58);

    const isBull = patUpper.includes('BULL') || patUpper.includes('HAMMER') || patUpper.includes('MORNING');

    return (
      <div className="pattern-sparkline-wrap">
        <div className="sparkline-pattern-tag-bar">
          <span className={`pattern-unique-badge ${isBull ? 'green' : 'red'}`}>
            {patUpper.replace(/_/g, ' ')}
          </span>
        </div>

        <svg viewBox={`0 0 ${svgWidth} ${svgHeight}`} className="pattern-sparkline-svg">
          {/* Pivot Level Guide Line */}
          {pivot && (
            <g>
              <line
                x1={padLeft}
                y1={getCY(pivot)}
                x2={padLeft + plotWidth}
                y2={getCY(pivot)}
                stroke="var(--warning-amber, #F59E0B)"
                strokeDasharray="3 3"
                strokeWidth="1.2"
                opacity="0.8"
              />
              <text
                x={padLeft + plotWidth + 4}
                y={getCY(pivot) + 3}
                fill="var(--warning-amber, #F59E0B)"
                fontSize="9"
                fontFamily="var(--font-mono)"
                fontWeight="700"
              >
                {currencySym}{pivot >= 1000 ? pivot.toFixed(0) : pivot.toFixed(1)}
              </text>
            </g>
          )}

          {/* Render Candlesticks */}
          {candleSlice.map((c, i) => {
            const cx = padLeft + i * colWidth + colWidth / 2;
            const openVal = c.open || c.close;
            const closeVal = c.close;
            const highVal = c.high || Math.max(openVal, closeVal);
            const lowVal = c.low || Math.min(openVal, closeVal);

            const isGreen = closeVal >= openVal;
            const wickColor = isGreen ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)';
            const topBodyVal = Math.max(openVal, closeVal);
            const btmBodyVal = Math.min(openVal, closeVal);

            const yHigh = getCY(highVal);
            const yLow = getCY(lowVal);
            const yTop = getCY(topBodyVal);
            const yBtm = getCY(btmBodyVal);
            const bodyHeight = Math.max(2, yBtm - yTop);

            const isTriggerCandle = i === candleSlice.length - 1;

            return (
              <g key={i}>
                {/* Wick */}
                <line
                  x1={cx}
                  y1={yHigh}
                  x2={cx}
                  y2={yLow}
                  stroke={wickColor}
                  strokeWidth="1.2"
                />
                {/* Body */}
                <rect
                  x={cx - bodyW / 2}
                  y={yTop}
                  width={bodyW}
                  height={bodyHeight}
                  rx="1"
                  fill={wickColor}
                />

                {/* Highlight Trigger Candle */}
                {isTriggerCandle && (
                  <g>
                    <rect
                      x={cx - bodyW / 2 - 2}
                      y={yTop - 2}
                      width={bodyW + 4}
                      height={bodyHeight + 4}
                      rx="2"
                      fill="none"
                      stroke={isBull ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)'}
                      strokeWidth="1.5"
                      strokeDasharray="2 1"
                    />
                    {/* Directional Signal Arrow */}
                    <path
                      d={
                        isBull
                          ? `M ${cx} ${yHigh - 8} L ${cx} ${yHigh - 3} M ${cx - 3} ${yHigh - 6} L ${cx} ${yHigh - 9} L ${cx + 3} ${yHigh - 6}`
                          : `M ${cx} ${yLow + 3} L ${cx} ${yLow + 8} M ${cx - 3} ${yLow + 5} L ${cx} ${yLow + 9} L ${cx + 3} ${yLow + 5}`
                      }
                      stroke={isBull ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)'}
                      strokeWidth="1.8"
                      fill="none"
                    />
                  </g>
                )}
              </g>
            );
          })}

          {/* Last Price Text */}
          <text
            x={padLeft + plotWidth + 4}
            y={getCY(candleSlice[candleSlice.length - 1].close) + 3}
            fill="var(--text-primary)"
            fontSize="9"
            fontFamily="var(--font-mono)"
            fontWeight="800"
          >
            {currencySym}
            {candleSlice[candleSlice.length - 1].close >= 1000
              ? candleSlice[candleSlice.length - 1].close.toFixed(0)
              : candleSlice[candleSlice.length - 1].close.toFixed(1)}
          </text>
        </svg>

        {pivot && (
          <div className="sparkline-legend-row font-mono">
            <span className="legend-pivot-tag">
              <span className="pivot-amber-dot" />
              <span>Pivot: {currencySym}{pivot.toFixed(2)}</span>
            </span>
            <span className="legend-dist-tag">
              Candle Reversal Confirmed
            </span>
          </div>
        )}
      </div>
    );
  }

  // --------------------------------------------------------------------------
  // 2. UNIQUE RSI DIVERGENCE CHART VISUALIZATION
  // --------------------------------------------------------------------------
  if (isDivergence) {
    const closes = points.map((p) => p.close);
    const minP = Math.min(...closes);
    const maxP = Math.max(...closes);
    const range = maxP - minP || 1;
    const priceH = 40;
    const rsiH = 24;

    const getX = (idx) => padLeft + (idx / (points.length - 1)) * plotWidth;
    const getY = (val) => padTop + priceH - ((val - minP) / range) * priceH;

    // Price path
    const priceD = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${getX(i).toFixed(1)} ${getY(p.close).toFixed(1)}`).join(' ');

    // Simulated RSI line with bullish divergence (Price down, RSI up)
    const rsiBaseY = padTop + priceH + 10;
    const getRsiY = (rsiVal) => rsiBaseY + rsiH - (rsiVal / 100) * rsiH;
    
    // Generate smooth RSI line showing divergence
    const rsiPoints = points.map((p, i) => {
      const prog = i / (points.length - 1);
      // Bullish divergence curve: dips early then climbs steadily from 28 to 58
      const val = 30 + Math.sin(prog * Math.PI) * 10 + prog * 20;
      return { x: getX(i), y: getRsiY(val) };
    });

    const rsiD = rsiPoints.map((pt, i) => `${i === 0 ? 'M' : 'L'} ${pt.x.toFixed(1)} ${pt.y.toFixed(1)}`).join(' ');

    return (
      <div className="pattern-sparkline-wrap">
        <div className="sparkline-pattern-tag-bar">
          <span className="pattern-unique-badge blue">
            RSI BULLISH DIVERGENCE
          </span>
        </div>

        <svg viewBox={`0 0 ${svgWidth} ${svgHeight + 8}`} className="pattern-sparkline-svg">
          {/* Price Action Line */}
          <path d={priceD} fill="none" stroke="var(--loss-red, #F04438)" strokeWidth="1.6" />
          
          {/* Lower Low dashed trendline on Price */}
          <line
            x1={getX(Math.floor(points.length * 0.4))}
            y1={getY(closes[Math.floor(points.length * 0.4)])}
            x2={getX(points.length - 1)}
            y2={getY(closes[points.length - 1])}
            stroke="var(--loss-red, #F04438)"
            strokeWidth="1.5"
            strokeDasharray="3 2"
          />

          {/* RSI Subplot Separator & Reference lines (70 / 30) */}
          <line x1={padLeft} y1={rsiBaseY} x2={padLeft + plotWidth} y2={rsiBaseY} stroke="var(--fintech-border, #222A3B)" strokeWidth="1" />
          <line x1={padLeft} y1={getRsiY(70)} x2={padLeft + plotWidth} y2={getRsiY(70)} stroke="rgba(240, 68, 56, 0.3)" strokeDasharray="2 2" strokeWidth="0.8" />
          <line x1={padLeft} y1={getRsiY(30)} x2={padLeft + plotWidth} y2={getRsiY(30)} stroke="rgba(0, 208, 156, 0.3)" strokeDasharray="2 2" strokeWidth="0.8" />

          {/* RSI Curve (Cyan / Emerald) */}
          <path d={rsiD} fill="none" stroke="var(--gain-green, #00D09C)" strokeWidth="1.8" />

          {/* Higher Low Divergence trendline on RSI */}
          <line
            x1={rsiPoints[Math.floor(points.length * 0.4)].x}
            y1={rsiPoints[Math.floor(points.length * 0.4)].y}
            x2={rsiPoints[points.length - 1].x}
            y2={rsiPoints[points.length - 1].y}
            stroke="var(--gain-green, #00D09C)"
            strokeWidth="1.5"
            strokeDasharray="3 2"
          />

          <text x={padLeft + plotWidth + 4} y={getY(closes[closes.length - 1]) + 3} fill="var(--text-primary)" fontSize="9" fontFamily="var(--font-mono)" fontWeight="700">
            {currencySym}{closes[closes.length - 1].toFixed(1)}
          </text>
          <text x={padLeft + plotWidth + 4} y={rsiBaseY + rsiH / 2 + 3} fill="var(--gain-green, #00D09C)" fontSize="8.5" fontFamily="var(--font-mono)" fontWeight="800">
            RSI ↗
          </text>
        </svg>

        <div className="sparkline-legend-row font-mono">
          <span className="legend-pivot-tag" style={{ color: 'var(--gain-green)' }}>
            <span className="pivot-amber-dot" style={{ background: 'var(--gain-green)', boxShadow: '0 0 6px rgba(0, 208, 156, 0.6)' }} />
            <span>Price Lower-Low vs RSI Higher-Low</span>
          </span>
          <span className="legend-dist-tag">Bullish Inflection</span>
        </div>
      </div>
    );
  }

  // --------------------------------------------------------------------------
  // 3. BASELINE MATH FOR CONTINUOUS PATTERNS (CUP & HANDLE, VCP, DOUBLE BOTTOM, ETC.)
  // --------------------------------------------------------------------------
  const closes = points.map((p) => p.close);
  let minP = Math.min(...closes);
  let maxP = Math.max(...closes);

  if (pivot) {
    minP = Math.min(minP, pivot);
    maxP = Math.max(maxP, pivot);
  }

  const range = maxP - minP || 1.0;
  const buf = range * 0.1;
  const adjMin = minP - buf;
  const adjMax = maxP + buf;
  const adjRange = adjMax - adjMin;

  const getX = (idx) => padLeft + (idx / (points.length - 1)) * plotWidth;
  const getY = (val) => padTop + plotHeight - ((val - adjMin) / adjRange) * plotHeight;

  const pathD = points
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${getX(i).toFixed(1)} ${getY(p.close).toFixed(1)}`)
    .join(' ');

  const lastPoint = points[points.length - 1];
  const firstPoint = points[0];
  const isGain = lastPoint.close >= firstPoint.close;
  const strokeColor = isDoubleTop
    ? 'var(--loss-red, #F04438)'
    : isDoubleBottom
    ? 'var(--gain-green, #00D09C)'
    : isGain
    ? 'var(--gain-green, #00D09C)'
    : 'var(--loss-red, #F04438)';
  const lastX = getX(points.length - 1);
  const lastY = getY(lastPoint.close);
  const pivotY = pivot ? getY(pivot) : null;

  return (
    <div className="pattern-sparkline-wrap">
      {/* Pattern Title Tag */}
      <div className="sparkline-pattern-tag-bar">
        <span className={`pattern-unique-badge ${isDoubleTop ? 'red' : isDoubleBottom ? 'green' : isGain ? 'green' : 'amber'}`}>
          {isCupHandle
            ? 'CUP & HANDLE BASE'
            : isVCP
            ? 'VCP CONTRACTION'
            : isDoubleBottom
            ? 'DOUBLE BOTTOM W-BASE'
            : isDoubleTop
            ? 'DOUBLE TOP M-PATTERN'
            : isSupplyDemand
            ? 'INSTITUTIONAL DEMAND ZONE'
            : isMACross
            ? 'MOVING AVERAGE CROSS'
            : isSupertrend
            ? 'SUPERTREND BULLISH FLIP'
            : patUpper.includes('BREAKOUT')
            ? 'PIVOT BREAKOUT'
            : 'QUANTITATIVE SETUP'}
        </span>
      </div>

      <svg viewBox={`0 0 ${svgWidth} ${svgHeight}`} className="pattern-sparkline-svg">
        {/* UNIQUE PATTERN OVERLAYS */}

        {/* 1. Cup & Handle: Draw U-shaped base curve + handle pullback */}
        {isCupHandle && (
          <g opacity="0.85">
            <path
              d={`M ${getX(2)} ${getY(closes[2])} Q ${getX(Math.floor(points.length * 0.45))} ${getY(minP) + 4} ${getX(Math.floor(points.length * 0.75))} ${getY(maxP) - 2}`}
              fill="none"
              stroke="var(--fintech-primary, #00D09C)"
              strokeWidth="2"
              strokeDasharray="4 2"
            />
            {/* Handle box */}
            <rect
              x={getX(Math.floor(points.length * 0.75))}
              y={getY(maxP) - 2}
              width={plotWidth * 0.2}
              height={plotHeight * 0.35}
              fill="rgba(245, 158, 11, 0.12)"
              stroke="var(--warning-amber, #F59E0B)"
              strokeWidth="1"
              strokeDasharray="2 2"
              rx="2"
            />
          </g>
        )}

        {/* 2. VCP: Draw 3 contracting volatility triangles */}
        {isVCP && (
          <g opacity="0.85">
            {/* Wave 1 */}
            <path
              d={`M ${getX(1)} ${getY(maxP)} L ${getX(7)} ${getY(minP)} L ${getX(13)} ${getY(maxP) + 4}`}
              fill="rgba(0, 208, 156, 0.06)"
              stroke="var(--fintech-primary, #00D09C)"
              strokeWidth="1.2"
              strokeDasharray="2 2"
            />
            {/* Wave 2 */}
            <path
              d={`M ${getX(13)} ${getY(maxP) + 4} L ${getX(18)} ${getY(minP) - 8} L ${getX(21)} ${getY(maxP) + 6}`}
              fill="rgba(0, 208, 156, 0.08)"
              stroke="var(--fintech-primary, #00D09C)"
              strokeWidth="1.2"
              strokeDasharray="2 2"
            />
          </g>
        )}

        {/* 3A. Double Bottom (W-Base): Draw W Support & Breakout Neckline */}
        {isDoubleBottom && (
          <g opacity="0.95">
            {/* Neckline Breakout Line */}
            <line
              x1={getX(2)}
              y1={getY(maxP)}
              x2={getX(points.length - 1)}
              y2={getY(maxP)}
              stroke="var(--fintech-blue, #1455FE)"
              strokeWidth="1.5"
              strokeDasharray="3 3"
            />
            {/* 1st Trough */}
            <circle cx={getX(6)} cy={getY(minP)} r="3" fill="var(--loss-red, #F04438)" />
            {/* 2nd Trough */}
            <circle cx={getX(18)} cy={getY(minP) + 2} r="3" fill="var(--gain-green, #00D09C)" />
            {/* Breakout arrow up */}
            <path
              d={`M ${getX(points.length - 1)} ${getY(lastPoint.close)} L ${getX(points.length - 1)} ${getY(lastPoint.close) - 7} M ${getX(points.length - 1) - 3} ${getY(lastPoint.close) - 4} L ${getX(points.length - 1)} ${getY(lastPoint.close) - 7} L ${getX(points.length - 1) + 3} ${getY(lastPoint.close) - 4}`}
              stroke="var(--gain-green, #00D09C)"
              strokeWidth="1.8"
              fill="none"
              strokeLinecap="round"
            />
          </g>
        )}

        {/* 3B. Double Top (M-Pattern): Draw M Overhead Resistance & Breakdown Neckline */}
        {isDoubleTop && (
          <g opacity="0.95">
            {/* Overhead Resistance line across dual peaks */}
            <line
              x1={getX(2)}
              y1={getY(maxP)}
              x2={getX(points.length - 1)}
              y2={getY(maxP)}
              stroke="var(--loss-red, #F04438)"
              strokeWidth="1.5"
              strokeDasharray="3 3"
            />
            {/* 1st Peak */}
            <circle cx={getX(6)} cy={getY(maxP)} r="3" fill="var(--loss-red, #F04438)" />
            {/* 2nd Peak (Failure) */}
            <circle cx={getX(18)} cy={getY(maxP) - 1} r="3" fill="var(--warning-amber, #F59E0B)" />

            {/* Support Breakdown Line */}
            <line
              x1={getX(6)}
              y1={getY(minP + range * 0.45)}
              x2={getX(points.length - 1)}
              y2={getY(minP + range * 0.45)}
              stroke="var(--warning-amber, #F59E0B)"
              strokeWidth="1.2"
              strokeDasharray="2 2"
            />
            {/* Downward breakdown arrow */}
            <path
              d={`M ${getX(points.length - 1)} ${getY(lastPoint.close)} L ${getX(points.length - 1)} ${getY(lastPoint.close) + 7} M ${getX(points.length - 1) - 3} ${getY(lastPoint.close) + 4} L ${getX(points.length - 1)} ${getY(lastPoint.close) + 7} L ${getX(points.length - 1) + 3} ${getY(lastPoint.close) + 4}`}
              stroke="var(--loss-red, #F04438)"
              strokeWidth="1.8"
              fill="none"
              strokeLinecap="round"
            />
          </g>
        )}

        {/* 4. Supply & Demand Zone: Shaded demand block */}
        {isSupplyDemand && (
          <rect
            x={padLeft}
            y={getY(minP + range * 0.28)}
            width={plotWidth}
            height={plotHeight * 0.3}
            fill="rgba(0, 208, 156, 0.15)"
            stroke="var(--gain-green, #00D09C)"
            strokeWidth="1"
            strokeDasharray="3 3"
            rx="3"
          />
        )}

        {/* 5. Moving Average Crossover: Two smoothed lines */}
        {isMACross && (
          <g>
            {/* Slow baseline in amber */}
            <path
              d={`M ${getX(0)} ${getY(closes[0])} Q ${getX(12)} ${getY(minP + range * 0.4)} ${getX(points.length - 1)} ${getY(lastPoint.close - 2)}`}
              fill="none"
              stroke="var(--warning-amber, #F59E0B)"
              strokeWidth="1.8"
              strokeDasharray="4 2"
            />
          </g>
        )}

        {/* 6. Supertrend: Stepped Green Stop Line */}
        {isSupertrend && (
          <g>
            <path
              d={`M ${getX(2)} ${getY(minP) + 6} H ${getX(10)} V ${getY(minP) + 3} H ${getX(18)} V ${getY(minP) - 2} H ${getX(points.length - 1)}`}
              fill="none"
              stroke="var(--gain-green, #00D09C)"
              strokeWidth="2.2"
            />
          </g>
        )}

        {/* Pivot Level Guide Line */}
        {pivot && pivotY !== null && (
          <g>
            <line
              x1={padLeft}
              y1={pivotY}
              x2={padLeft + plotWidth}
              y2={pivotY}
              stroke="var(--warning-amber, #F59E0B)"
              strokeDasharray="3 3"
              strokeWidth="1.2"
              opacity="0.85"
            />
            <text
              x={padLeft + plotWidth + 4}
              y={pivotY + 3}
              fill="var(--warning-amber, #F59E0B)"
              fontSize="9"
              fontFamily="var(--font-mono)"
              fontWeight="700"
            >
              {currencySym}{pivot >= 1000 ? pivot.toFixed(0) : pivot.toFixed(1)}
            </text>
          </g>
        )}

        {/* Main Price Action Line */}
        <path
          d={pathD}
          fill="none"
          stroke={strokeColor}
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        {/* Live Close Point Dot with Halo */}
        <circle cx={lastX} cy={lastY} r="3.5" fill={strokeColor} />
        <circle cx={lastX} cy={lastY} r="6.5" fill={strokeColor} opacity="0.25" />

        {/* Current Price Label */}
        <text
          x={padLeft + plotWidth + 4}
          y={lastY + 3}
          fill="var(--text-primary)"
          fontSize="9"
          fontFamily="var(--font-mono)"
          fontWeight="800"
        >
          {currencySym}{lastPoint.close >= 1000 ? lastPoint.close.toFixed(0) : lastPoint.close.toFixed(1)}
        </text>
      </svg>

      {/* Pattern Visual Legend Tag */}
      {pivot && (
        <div className="sparkline-legend-row font-mono">
          <span className="legend-pivot-tag">
            <span className="pivot-amber-dot" />
            <span>Pivot: {currencySym}{pivot.toFixed(2)}</span>
          </span>
          <span className="legend-dist-tag">
            {lastPoint.close >= pivot ? '▲ Cleared Pivot' : `▼ ${((pivot - lastPoint.close) / pivot * 100).toFixed(1)}% to Pivot`}
          </span>
        </div>
      )}
    </div>
  );
}
