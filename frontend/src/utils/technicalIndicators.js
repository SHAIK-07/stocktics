/**
 * Technical Indicators Engine for Stocktics Pro Chart
 * Implements Pine Script indicators:
 * 1. Bullish & Bearish Engulfing Candles
 * 2. Squeeze Momentum Indicator [LazyBear] (SQZMOM_LB)
 * 3. RSI (Relative Strength Index)
 * 4. EMA (Exponential Moving Average)
 * 5. Bollinger Bands
 */

/**
 * 1. Engulfing Candles (Pine Script implementation)
 * bullishEngulfing = (openBarCurrent <= closeBarPrevious) and (openBarCurrent < openBarPrevious) and (closeBarCurrent > openBarPrevious)
 * bearishEngulfing = (openBarCurrent >= closeBarPrevious) and (openBarCurrent > openBarPrevious) and (closeBarCurrent < openBarPrevious)
 */
export function computeEngulfingCandles(points) {
  if (!points || points.length < 2) return [];

  return points.map((curr, i) => {
    if (i === 0) return { isBullish: false, isBearish: false };
    const prev = points[i - 1];

    const openBarPrevious = prev.open ?? prev.close;
    const closeBarPrevious = prev.close;
    const openBarCurrent = curr.open ?? curr.close;
    const closeBarCurrent = curr.close;

    const isBullish =
      openBarCurrent <= closeBarPrevious &&
      openBarCurrent < openBarPrevious &&
      closeBarCurrent > openBarPrevious;

    const isBearish =
      openBarCurrent >= closeBarPrevious &&
      openBarCurrent > openBarPrevious &&
      closeBarCurrent < openBarPrevious;

    return {
      isBullish,
      isBearish
    };
  });
}

/**
 * 2. Squeeze Momentum Indicator [LazyBear] (SQZMOM_LB)
 * Replicates TradingView / LazyBear Pine script formula:
 * - Bollinger Bands (20, 2.0)
 * - Keltner Channels (20, 1.5 * TrueRange)
 * - Linear Regression momentum value
 * - Squeeze On (dots) when BB is inside KC
 */
export function computeSqueezeMomentum(points, { length = 20, mult = 2.0, lengthKC = 20, multKC = 1.5 } = {}) {
  if (!points || points.length === 0) return [];

  const n = points.length;
  const effLen = Math.min(lengthKC, Math.max(3, n));
  const effBBLen = Math.min(length, Math.max(3, n));

  const closes = points.map(p => p.close);
  const highs = points.map(p => p.high ?? p.close);
  const lows = points.map(p => p.low ?? p.close);

  // Helper: Simple Moving Average
  const sma = (arr, len, idx) => {
    const curLen = Math.min(len, idx + 1);
    if (curLen <= 0) return null;
    let sum = 0;
    for (let i = idx - curLen + 1; i <= idx; i++) sum += arr[i];
    return sum / curLen;
  };

  // Helper: Standard Deviation
  const stdev = (arr, len, idx, mean) => {
    const curLen = Math.min(len, idx + 1);
    if (curLen <= 1 || mean == null) return 0.001;
    let sumSq = 0;
    for (let i = idx - curLen + 1; i <= idx; i++) {
      const diff = arr[i] - mean;
      sumSq += diff * diff;
    }
    return Math.sqrt(sumSq / curLen);
  };

  // True Range
  const tr = [];
  for (let i = 0; i < n; i++) {
    if (i === 0) {
      tr.push(highs[0] - lows[0] || 0.01);
    } else {
      const hl = highs[i] - lows[i];
      const hc = Math.abs(highs[i] - closes[i - 1]);
      const lc = Math.abs(lows[i] - closes[i - 1]);
      tr.push(Math.max(hl, hc, lc));
    }
  }

  // Linear Regression at offset 0
  const linregAt0 = (arr, len, idx) => {
    const curLen = Math.min(len, idx + 1);
    if (curLen <= 1) return arr[idx] || 0;
    let sumX = 0;
    let sumY = 0;
    let sumXY = 0;
    let sumXX = 0;
    for (let i = 0; i < curLen; i++) {
      const x = i;
      const y = arr[idx - curLen + 1 + i];
      sumX += x;
      sumY += y;
      sumXY += x * y;
      sumXX += x * x;
    }
    const slope = (curLen * sumXY - sumX * sumY) / (curLen * sumXX - sumX * sumX || 1);
    const intercept = (sumY - slope * sumX) / curLen;
    return intercept + slope * (curLen - 1);
  };

  // Precompute delta for momentum
  const deltaArr = [];
  for (let i = 0; i < n; i++) {
    const curLen = Math.min(effLen, i + 1);
    let highestHigh = -Infinity;
    let lowestLow = Infinity;
    for (let j = i - curLen + 1; j <= i; j++) {
      if (highs[j] > highestHigh) highestHigh = highs[j];
      if (lows[j] < lowestLow) lowestLow = lows[j];
    }
    const smaClose = sma(closes, curLen, i) ?? closes[i];
    const donchianMid = (highestHigh + lowestLow) / 2;
    const avgVal = (donchianMid + smaClose) / 2;
    deltaArr.push(closes[i] - avgVal);
  }

  const results = [];
  for (let i = 0; i < n; i++) {
    const basis = sma(closes, effBBLen, i) ?? closes[i];
    const dev = mult * stdev(closes, effBBLen, i, basis);
    const upperBB = basis + dev;
    const lowerBB = basis - dev;

    const maKC = sma(closes, effLen, i) ?? closes[i];
    const rangeMa = sma(tr, effLen, i) ?? tr[i];
    const upperKC = maKC + rangeMa * multKC;
    const lowerKC = maKC - rangeMa * multKC;

    const sqzOn = lowerBB > lowerKC && upperBB < upperKC;
    const sqzOff = lowerBB < lowerKC && upperBB > upperKC;
    const noSqz = !sqzOn && !sqzOff;

    const val = linregAt0(deltaArr, effLen, i);
    const prevVal = i > 0 ? results[i - 1].val : 0;

    // LazyBear color classification:
    // val >= 0: if val >= val[1] -> lime, else green
    // val < 0: if val <= val[1] -> red, else maroon
    let barColor = '#00D09C';
    if (val >= 0) {
      barColor = val >= prevVal ? '#00D09C' : '#007A5A';
    } else {
      barColor = val <= prevVal ? '#F04438' : '#8B1E17';
    }

    const dotColor = sqzOn ? '#F59E0B' : sqzOff ? '#64748B' : '#38BDF8';

    results.push({
      val,
      sqzOn,
      sqzOff,
      noSqz,
      barColor,
      dotColor
    });
  }

  return results;
}

/**
 * 3. Exponential Moving Average (EMA)
 */
export function computeEMA(points, period) {
  if (!points || points.length === 0) return [];
  const n = points.length;
  const effPeriod = Math.min(period, n);
  const k = 2 / (effPeriod + 1);
  const result = new Array(n).fill(null);

  // Initialize with first point or average
  let prevEma = points[0].close;
  result[0] = prevEma;

  for (let i = 1; i < n; i++) {
    const currentEma = points[i].close * k + prevEma * (1 - k);
    result[i] = currentEma;
    prevEma = currentEma;
  }
  return result;
}

/**
 * 4. Relative Strength Index (RSI 14)
 */
export function computeRSI(points, period = 14) {
  if (!points || points.length === 0) return [];
  const n = points.length;
  if (n < 2) return new Array(n).fill(50.0);

  const effPeriod = Math.min(period, n - 1);
  const result = new Array(n).fill(null);

  let gains = 0;
  let losses = 0;

  for (let i = 1; i <= effPeriod; i++) {
    const diff = points[i].close - points[i - 1].close;
    if (diff >= 0) gains += diff;
    else losses += Math.abs(diff);
  }

  let avgGain = gains / effPeriod;
  let avgLoss = losses / effPeriod;
  let rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
  result[effPeriod] = 100 - 100 / (1 + rs);

  for (let i = effPeriod + 1; i < n; i++) {
    const diff = points[i].close - points[i - 1].close;
    const currentGain = diff >= 0 ? diff : 0;
    const currentLoss = diff < 0 ? Math.abs(diff) : 0;

    avgGain = (avgGain * (effPeriod - 1) + currentGain) / effPeriod;
    avgLoss = (avgLoss * (effPeriod - 1) + currentLoss) / effPeriod;

    rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
    result[i] = 100 - 100 / (1 + rs);
  }

  // Backfill early points so it's continuous
  for (let i = 0; i < effPeriod; i++) {
    result[i] = result[effPeriod];
  }

  return result;
}

/**
 * 5. Bollinger Bands (20, 2.0)
 */
export function computeBollingerBands(points, period = 20, mult = 2.0) {
  if (!points || points.length === 0) return [];
  const n = points.length;
  const result = new Array(n).fill(null);

  for (let i = 0; i < n; i++) {
    const curLen = Math.min(period, i + 1);
    let sum = 0;
    for (let j = i - curLen + 1; j <= i; j++) sum += points[j].close;
    const mean = sum / curLen;

    let sumSq = 0;
    for (let j = i - curLen + 1; j <= i; j++) {
      const diff = points[j].close - mean;
      sumSq += diff * diff;
    }
    const std = curLen > 1 ? Math.sqrt(sumSq / curLen) : (mean * 0.01);

    result[i] = {
      upper: mean + mult * std,
      middle: mean,
      lower: mean - mult * std
    };
  }
  return result;
}
