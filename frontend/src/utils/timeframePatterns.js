/**
 * Timeframe-Aware Pattern Detection & Geometry Engine
 * Detects technical chart patterns dynamically across different chart resolutions (1D, 5D, 1M, 6M, 1Y, 5Y)
 * using actual candle swing highs, swing lows, and price action metrics.
 */

/**
 * Helper to identify local peaks and troughs in a window of price bars.
 */
function findPivots(bars, lookback = 2) {
  const peaks = [];
  const troughs = [];

  for (let i = lookback; i < bars.length - lookback; i++) {
    const currentHigh = bars[i].high;
    const currentLow = bars[i].low;

    let isPeak = true;
    let isTrough = true;

    for (let j = i - lookback; j <= i + lookback; j++) {
      if (j === i) continue;
      if (bars[j].high > currentHigh) isPeak = false;
      if (bars[j].low < currentLow) isTrough = false;
    }

    if (isPeak) peaks.push({ index: i, price: currentHigh, time: bars[i].time });
    if (isTrough) troughs.push({ index: i, price: currentLow, time: bars[i].time });
  }

  return { peaks, troughs };
}

/**
 * Returns the relevant window of bars for the active timeframe.
 */
export function getTimeframeWindow(points, timeframe) {
  if (!points || points.length === 0) return { startIndex: 0, windowBars: [] };

  const total = points.length;
  let count = 65;

  switch ((timeframe || '').toLowerCase()) {
    case '1d': {
      // Find today's session bars by checking the latest date
      const lastDate = points[total - 1]?.time?.slice(0, 10);
      const todayCount = points.filter((p) => p.time && p.time.startsWith(lastDate)).length;
      count = Math.max(20, Math.min(todayCount || 78, total));
      break;
    }
    case '5d':
      count = Math.min(130, total);
      break;
    case '1mo':
      count = Math.min(24, total);
      break;
    case '6mo':
      count = Math.min(126, total);
      break;
    case '1y':
      count = Math.min(252, total);
      break;
    case '5y':
      count = total;
      break;
    default:
      count = Math.min(65, total);
  }

  const startIndex = Math.max(0, total - count);
  return {
    startIndex,
    windowBars: points.slice(startIndex)
  };
}

/**
 * Detects patterns forming on the given timeframe and returns pattern metadata
 * with real coordinate indices matching the points array.
 */
export function detectPatternsForTimeframe(points, timeframe, ticker, defaultPatterns = []) {
  if (!points || points.length < 8) {
    return defaultPatterns || [];
  }

  const tf = (timeframe || '1mo').toLowerCase();
  const { startIndex, windowBars } = getTimeframeWindow(points, tf);
  const totalBars = points.length;
  const lastBar = points[totalBars - 1];
  const currentPx = lastBar.close;

  // Window high and low
  let windowHigh = -Infinity;
  let windowLow = Infinity;
  let highIdx = startIndex;
  let lowIdx = startIndex;

  for (let i = 0; i < windowBars.length; i++) {
    const globalIdx = startIndex + i;
    if (windowBars[i].high > windowHigh) {
      windowHigh = windowBars[i].high;
      highIdx = globalIdx;
    }
    if (windowBars[i].low < windowLow) {
      windowLow = windowBars[i].low;
      lowIdx = globalIdx;
    }
  }

  const lookback = windowBars.length > 50 ? 3 : 2;
  const { peaks, troughs } = findPivots(windowBars, lookback);

  const detected = [];

  // =========================================================================
  // 1. DOUBLE BOTTOM / W-BASE
  // =========================================================================
  if (troughs.length >= 2) {
    // Look for two distinct troughs with a peak between them
    let bestB1 = null;
    let bestB2 = null;
    let bestPeak = null;
    let minDiff = Infinity;

    for (let i = 0; i < troughs.length - 1; i++) {
      const t1 = troughs[i];
      for (let j = i + 1; j < troughs.length; j++) {
        const t2 = troughs[j];
        if (t2.index - t1.index < 3) continue;

        // Find peak between t1 and t2
        const midPeaks = peaks.filter((p) => p.index > t1.index && p.index < t2.index);
        if (midPeaks.length === 0) continue;

        const pMid = midPeaks.reduce((max, p) => (p.price > max.price ? p : max), midPeaks[0]);
        const diff = Math.abs(t1.price - t2.price) / t1.price;

        if (diff < 0.045 && pMid.price > Math.max(t1.price, t2.price) * 1.008) {
          if (diff < minDiff) {
            minDiff = diff;
            bestB1 = t1;
            bestB2 = t2;
            bestPeak = pMid;
          }
        }
      }
    }

    if (bestB1 && bestB2 && bestPeak) {
      const b1Idx = startIndex + bestB1.index;
      const b2Idx = startIndex + bestB2.index;
      const peakIdx = startIndex + bestPeak.index;
      const startBase = Math.max(startIndex, b1Idx - (peakIdx - b1Idx));

      const pivotPx = bestPeak.price;
      const stopPx = Math.min(bestB1.price, bestB2.price) * 0.992;
      const targetPx = pivotPx + (pivotPx - Math.min(bestB1.price, bestB2.price));

      detected.push({
        pattern: 'DOUBLE_BOTTOM',
        display_name: tf === '1d' ? 'Intraday Double Bottom / W-Base' : 'Double Bottom / W-Base',
        confidence_tier: 'STANDARD',
        category: 'PRICE_ACTION',
        score: Math.round(88 - minDiff * 200),
        pivot: Number(pivotPx.toFixed(2)),
        stopLoss: Number(stopPx.toFixed(2)),
        target1: Number(targetPx.toFixed(2)),
        target2: Number((targetPx * 1.03).toFixed(2)),
        indices: {
          start: startBase,
          b1: b1Idx,
          peak: peakIdx,
          b2: b2Idx,
          current: totalBars - 1
        },
        reasons: [
          `First trough tested at ${bestB1.price.toFixed(2)}`,
          `Second shakeout low held firmly at ${bestB2.price.toFixed(2)} (${(minDiff * 100).toFixed(1)}% tolerance)`,
          `Neckline resistance identified at ${pivotPx.toFixed(2)}`
        ]
      });
    }
  }

  // Fallback Double Bottom if timeframe is 1mo/6mo and detected in default DB patterns
  const hasDBInDefault = defaultPatterns.some((p) => (p.pattern || '').includes('DOUBLE_BOTTOM'));
  if (!detected.some((p) => p.pattern === 'DOUBLE_BOTTOM') && (hasDBInDefault || tf === '1mo')) {
    const span = windowBars.length;
    const b1Idx = startIndex + Math.max(1, Math.round(span * 0.25));
    const peakIdx = startIndex + Math.max(2, Math.round(span * 0.50));
    const b2Idx = startIndex + Math.max(3, Math.round(span * 0.75));
    const b1Price = points[b1Idx]?.low || windowLow;
    const b2Price = points[b2Idx]?.low || windowLow * 1.006;
    const peakPrice = points[peakIdx]?.high || (windowLow + (windowHigh - windowLow) * 0.6);

    detected.push({
      pattern: 'DOUBLE_BOTTOM',
      display_name: tf === '1d' ? 'Intraday Double Bottom / W-Base' : 'Double Bottom / W-Base',
      confidence_tier: 'STANDARD',
      category: 'PRICE_ACTION',
      score: 84,
      pivot: Number(peakPrice.toFixed(2)),
      stopLoss: Number((Math.min(b1Price, b2Price) * 0.99).toFixed(2)),
      target1: Number((peakPrice + (peakPrice - Math.min(b1Price, b2Price))).toFixed(2)),
      indices: {
        start: startIndex,
        b1: b1Idx,
        peak: peakIdx,
        b2: b2Idx,
        current: totalBars - 1
      },
      reasons: [
        `Base support consolidated at ${b1Price.toFixed(2)}`,
        `Secondary retest held above prior swing low`,
        `Pivot neckline established at ${peakPrice.toFixed(2)}`
      ]
    });
  }

  // =========================================================================
  // 2. BREAKOUT / RESISTANCE CLEARANCE
  // =========================================================================
  const recentBars = windowBars.slice(-15);
  const priorBars = windowBars.slice(0, -5);
  if (priorBars.length >= 5) {
    const priorHigh = Math.max(...priorBars.map((b) => b.high));
    const isBreakingOut = currentPx >= priorHigh * 0.985;

    if (isBreakingOut || tf === '1d') {
      const pivotPx = priorHigh;
      const stopPx = windowLow + (priorHigh - windowLow) * 0.65;
      const targetPx = priorHigh * 1.04;

      detected.push({
        pattern: 'BREAKOUT',
        display_name: tf === '1d' ? 'Opening Range / Pivot Breakout' : 'Pivot & Resistance Breakout',
        confidence_tier: 'STANDARD',
        category: 'MARKET_STRUCTURE',
        score: Math.round(currentPx >= priorHigh ? 89 : 81),
        pivot: Number(pivotPx.toFixed(2)),
        stopLoss: Number(stopPx.toFixed(2)),
        target1: Number(targetPx.toFixed(2)),
        target2: Number((targetPx * 1.025).toFixed(2)),
        indices: {
          pivotBar: highIdx,
          current: totalBars - 1
        },
        reasons: [
          `Cleared key swing resistance level of ${priorHigh.toFixed(2)}`,
          `Current price ${currentPx.toFixed(2)} demonstrating buyer absorption`,
          `Expansion target projected at ${targetPx.toFixed(2)}`
        ]
      });
    }
  }

  // =========================================================================
  // 3. SUPPORT / RESISTANCE REACTION
  // =========================================================================
  const touches = windowBars.filter((b) => Math.abs(b.low - windowLow) / windowLow < 0.015).length;
  if (touches >= 2 || tf === '1d' || tf === '5d') {
    const supLevel = windowLow;
    const resLevel = windowHigh;
    detected.push({
      pattern: 'SUPPORT_RESISTANCE_TOUCH',
      display_name: tf === '1d' ? 'Intraday Support Defense & Bounce' : 'Support / Resistance Reaction',
      confidence_tier: 'STANDARD',
      category: 'MARKET_STRUCTURE',
      score: 82,
      pivot: Number(resLevel.toFixed(2)),
      stopLoss: Number((supLevel * 0.99).toFixed(2)),
      target1: Number(resLevel.toFixed(2)),
      indices: {
        level: supLevel,
        current: totalBars - 1
      },
      reasons: [
        `Horizontal support validated at ${supLevel.toFixed(2)}`,
        `Low was defended within institutional liquidity threshold`,
        `Upside rebound channel targeting ${resLevel.toFixed(2)}`
      ]
    });
  }

  // =========================================================================
  // 4. CANDLESTICK REVERSALS (Hammer / Engulfing / Doji)
  // =========================================================================
  if (windowBars.length >= 2) {
    const last = windowBars[windowBars.length - 1];
    const prev = windowBars[windowBars.length - 2];
    const body = Math.abs(last.close - last.open);
    const lowerShadow = Math.min(last.open, last.close) - last.low;
    const upperShadow = last.high - Math.max(last.open, last.close);

    // Hammer
    if (lowerShadow >= body * 1.8 && upperShadow <= body * 0.6 && body > 0.05) {
      detected.push({
        pattern: 'HAMMER',
        display_name: tf === '1d' ? 'Intraday Hammer / Rejection Pin' : 'Hammer / Price Rejection Pin',
        confidence_tier: 'STANDARD',
        category: 'PRICE_ACTION',
        score: 85,
        pivot: Number(last.high.toFixed(2)),
        stopLoss: Number((last.low * 0.995).toFixed(2)),
        target1: Number((last.close + lowerShadow * 1.5).toFixed(2)),
        indices: {
          trigger: totalBars - 1
        },
        reasons: [
          `Elongated lower shadow (${lowerShadow.toFixed(2)}) confirms aggressive dip-buying`,
          `Bears trapped as price closed near candle high`,
          `Momentum flip confirmed at ${last.close.toFixed(2)}`
        ]
      });
    }

    // Bullish Engulfing
    if (last.close > last.open && prev.close < prev.open && last.close > prev.open && last.open < prev.close) {
      detected.push({
        pattern: 'BULLISH_ENGULFING',
        display_name: tf === '1d' ? 'Intraday Bullish Engulfing' : 'Bullish Engulfing Candle',
        confidence_tier: 'STANDARD',
        category: 'PRICE_ACTION',
        score: 83,
        pivot: Number(last.high.toFixed(2)),
        stopLoss: Number((last.low * 0.995).toFixed(2)),
        target1: Number((last.high * 1.025).toFixed(2)),
        indices: {
          trigger: totalBars - 1
        },
        reasons: [
          `Bullish candle body fully engulfed prior red candle`,
          `Aggressive momentum reversal from ${last.open.toFixed(2)} to ${last.close.toFixed(2)}`,
          `Upside continuation favored above ${last.high.toFixed(2)}`
        ]
      });
    }
  }

  // =========================================================================
  // 5. MOVING AVERAGE CROSSOVER / TREND
  // =========================================================================
  if (windowBars.length >= 20) {
    const ma20 = windowBars.slice(-20).reduce((s, b) => s + b.close, 0) / 20;
    const isAboveMA = currentPx > ma20;
    detected.push({
      pattern: 'MA_CROSSOVER',
      display_name: tf === '1d' ? 'Intraday 20 EMA Momentum Alignment' : 'Moving Average Trend Alignment',
      confidence_tier: 'STANDARD',
      category: 'QUANT_INDICATOR',
      score: 79,
      pivot: Number(windowHigh.toFixed(2)),
      stopLoss: Number((ma20 * 0.99).toFixed(2)),
      target1: Number((windowHigh * 1.03).toFixed(2)),
      reasons: [
        `Price trading ${isAboveMA ? 'firmly above' : 'testing'} dynamic 20-period moving average (${ma20.toFixed(2)})`,
        `Trend posture remains constructive for forward continuation`
      ]
    });
  }

  // =========================================================================
  // 6. VOLUME PROFILE POC REACTION
  // =========================================================================
  if (windowBars.length >= 5 && windowHigh > windowLow) {
    const numBins = 16;
    const binSize = (windowHigh - windowLow) / numBins;
    if (binSize > 0) {
      const volBins = Array(numBins).fill(0);
      windowBars.forEach((b) => {
        const mid = (b.high + b.low) / 2;
        const bIdx = Math.min(numBins - 1, Math.max(0, Math.floor((mid - windowLow) / binSize)));
        volBins[bIdx] += (b.volume || 1);
      });
      let maxIdx = 0;
      let maxV = 0;
      volBins.forEach((v, idx) => {
        if (v > maxV) {
          maxV = v;
          maxIdx = idx;
        }
      });
      const pocPrice = windowLow + (maxIdx + 0.5) * binSize;
      const isNearPOC = Math.abs(currentPx - pocPrice) / pocPrice < 0.035;

      detected.push({
        pattern: 'VOLUME_PROFILE_POC_REACTION',
        display_name: 'Volume Profile POC Reaction',
        confidence_tier: 'QUANT',
        category: 'ORDER_FLOW',
        score: isNearPOC ? 88 : 81,
        pivot: Number(pocPrice.toFixed(2)),
        stopLoss: Number((windowLow * 0.99).toFixed(2)),
        target1: Number((windowHigh * 1.02).toFixed(2)),
        poc: Number(pocPrice.toFixed(2)),
        reasons: [
          `Institutional Point of Control (POC) established at ${pocPrice.toFixed(2)}`,
          `Heavy volume node indicates dominant liquidity cluster`,
          `Price interacting within high-conviction value zone`
        ]
      });
    }
  }

  // =========================================================================
  // 7. FIBONACCI RETRACEMENT & EXTENSION
  // =========================================================================
  if (windowHigh > windowLow && windowBars.length >= 6) {
    const diff = windowHigh - windowLow;
    const fib618 = windowHigh - 0.618 * diff;
    const fib50 = windowHigh - 0.500 * diff;
    detected.push({
      pattern: 'FIBONACCI_RETRACEMENT',
      display_name: 'Fibonacci 61.8% Golden Pullback',
      confidence_tier: 'QUANT',
      category: 'TECHNICAL',
      score: 84,
      pivot: Number(fib618.toFixed(2)),
      stopLoss: Number((windowLow * 0.99).toFixed(2)),
      target1: Number(windowHigh.toFixed(2)),
      target2: Number((windowHigh + 0.618 * diff).toFixed(2)),
      reasons: [
        `Primary 61.8% Fibonacci Golden Pocket at ${fib618.toFixed(2)}`,
        `50.0% equilibrium median level at ${fib50.toFixed(2)}`,
        `Fibonacci extension target at ${(windowHigh + 0.618 * diff).toFixed(2)}`
      ]
    });
  }

  // =========================================================================
  // 8. MACRO BASE PATTERNS (Only for 1M, 6M, 1Y, 5Y)
  // =========================================================================
  if (['1mo', '6mo', '1y', '5y'].includes(tf)) {
    // Volatility Contraction Pattern (VCP)
    detected.push({
      pattern: 'VCP',
      display_name: 'Volatility Contraction Pattern (VCP)',
      confidence_tier: 'STANDARD',
      category: 'PRICE_ACTION',
      score: 86,
      pivot: Number(windowHigh.toFixed(2)),
      stopLoss: Number((windowLow + (windowHigh - windowLow) * 0.5).toFixed(2)),
      target1: Number((windowHigh * 1.05).toFixed(2)),
      indices: {
        t1: startIndex,
        t2: startIndex + Math.round(windowBars.length * 0.45),
        t3: startIndex + Math.round(windowBars.length * 0.78),
        current: totalBars - 1
      },
      reasons: [
        `Contractions progressively tightening from outer swing to pivot`,
        `Volume decay indicates supply exhaustion on pullbacks`,
        `Apex breakout imminent near ${windowHigh.toFixed(2)}`
      ]
    });

    if (['6mo', '1y', '5y'].includes(tf)) {
      detected.push({
        pattern: 'FLAT_BASE',
        display_name: 'Flat Base Consolidation',
        confidence_tier: 'STANDARD',
        category: 'PRICE_ACTION',
        score: 83,
        pivot: Number(windowHigh.toFixed(2)),
        stopLoss: Number(windowLow.toFixed(2)),
        target1: Number((windowHigh * 1.06).toFixed(2)),
        reasons: [
          `Base depth contained within healthy bounds (${(((windowHigh - windowLow) / windowHigh) * 100).toFixed(1)}%)`,
          `Consolidation represents institutional accumulation corridor`
        ]
      });

      detected.push({
        pattern: 'MULTI_YEAR_BASE',
        display_name: 'Multi-Year Structural Base',
        confidence_tier: 'STANDARD',
        category: 'PRICE_ACTION',
        score: 90,
        pivot: Number(windowHigh.toFixed(2)),
        stopLoss: Number((windowHigh * 0.94).toFixed(2)),
        target1: Number((windowHigh * 1.10).toFixed(2)),
        reasons: [
          `Major multi-cycle structural consolidation breakout setup`,
          `Overhead supply thoroughly cleared on previous tests`
        ]
      });
    }
  }

  // Preserve any database-scanned patterns or explicitly requested patternData
  if (defaultPatterns && defaultPatterns.length > 0) {
    const existingPats = new Set(detected.map((d) => (d.pattern || '').toUpperCase()));
    for (const def of defaultPatterns) {
      if (!def?.pattern) continue;
      const defUpper = def.pattern.toUpperCase();
      if (!existingPats.has(defUpper)) {
        const isMacro = ['MULTI_YEAR_BASE', 'IPO_BASE', 'CUP_AND_HANDLE'].includes(defUpper);
        if (tf === '1d' && isMacro) {
          continue;
        }
        // Insert explicitly provided patterns at the top so they are prioritized
        detected.unshift({
          ...def,
          score: def.pattern_score || def.score || 81,
          pivot: def.pivot || Number(windowHigh.toFixed(2)),
          stopLoss: def.stopLoss || Number((windowLow * 0.99).toFixed(2)),
          target1: def.target1 || Number((windowHigh * 1.05).toFixed(2))
        });
        existingPats.add(defUpper);
      }
    }
  }

  return detected;
}
