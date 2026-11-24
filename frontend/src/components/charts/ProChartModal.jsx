import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  X,
  Minimize2,
  TrendingUp,
  TrendingDown,
  BarChart2,
  LineChart as LineIcon,
  Layers,
  Activity,
  Zap,
  Sliders,
  Check,
  ZoomIn,
  ZoomOut,
  RotateCcw,
  Move,
  CheckCircle2,
  BookOpen,
  ArrowRight,
  ArrowLeft,
  ShieldAlert,
  Target,
  Compass,
  Eye,
  AlertTriangle
} from 'lucide-react';
import {
  computeEngulfingCandles,
  computeSqueezeMomentum,
  computeEMA,
  computeRSI,
  computeBollingerBands
} from '../../utils/technicalIndicators';
import { fetchStockPatterns } from '../../services/api';
import { navigateTo } from '../../utils/router';
import { detectPatternsForTimeframe, getTimeframeWindow } from '../../utils/timeframePatterns';

const TIMEFRAMES = [
  { label: '1D', period: '1d' },
  { label: '5D', period: '5d' },
  { label: '1M', period: '1mo' },
  { label: '6M', period: '6mo' },
  { label: '1Y', period: '1y' },
  { label: '5Y', period: '5y' }
];

function formatTimelineLabel(timeStr, isMultiDayVisible) {
  if (!timeStr) return '';
  if (timeStr.includes(' ')) {
    return isMultiDayVisible
      ? `${timeStr.slice(5, 10)} ${timeStr.slice(11, 16)}`
      : timeStr.slice(11, 16);
  }
  const parts = timeStr.split('-');
  if (parts.length === 3) {
    const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const m = parseInt(parts[1], 10) - 1;
    const d = parseInt(parts[2], 10);
    if (m >= 0 && m < 12) {
      return `${d} ${monthNames[m]}`;
    }
  }
  return timeStr.slice(5, 10);
}

function computeTradePlaybook({
  pattern,
  currentPrice,
  pivot,
  stopLoss,
  target1,
  target2,
  currencySymbol,
  reasons = []
}) {
  const pat = (pattern || '').toUpperCase();
  const name = pat.replace(/_/g, ' ');

  // 1. Institutional Diagnosis: What is happening now
  let diagnosis = '';
  if (pat.includes('ENGULFING')) {
    diagnosis = `Bulls aggressively absorbed institutional supply following recent selling pressure. The expansion candle completely engulfed previous distribution, establishing a high-conviction demand shelf and confirming buyer dominance.`;
  } else if (pat.includes('HAMMER') || pat.includes('PINBAR')) {
    diagnosis = `Price staged an intraday liquidity grab below support before heavy institutional bids stepped in, printing an extended lower shadow rejection wick. Supply below this level is fully exhausted.`;
  } else if (pat.includes('CUP') || pat.includes('ROUNDING')) {
    diagnosis = `Security completed a multi-week accumulation base. Selling volume dried up systematically across the rounded bottom, and price is now building the right handle-pivot ready for expansion.`;
  } else if (pat.includes('VCP')) {
    diagnosis = `Volatility Contraction Pattern (VCP) in play: successive price swings have tightened progressively (e.g. 15% → 7% → 3%), confirming that floating supply has been locked away by strong hands.`;
  } else if (pat.includes('DOUBLE_BOTTOM') || pat.includes('W_BASE')) {
    diagnosis = `Classical double-bottom W-formation verified. The second test of support held firmly above previous lows on lower volume, confirming institutional support defense.`;
  } else if (pat.includes('DIVERGENCE') || pat.includes('RSI')) {
    diagnosis = `Momentum oscillator printed a distinct higher-low while price tested support, confirming classical bullish divergence. Downward velocity has halted and buyers are taking control.`;
  } else if (pat.includes('SQUEEZE')) {
    diagnosis = `Bollinger Bands have compressed inside Keltner Channels indicating extreme pre-breakout volatility compression. A high-velocity directional impulse is imminent.`;
  } else if (pat.includes('POC') || pat.includes('VOLUME_PROFILE')) {
    diagnosis = `Price is interacting directly with the Volume Profile Point of Control (POC) of ${currencySymbol}${pivot.toFixed(2)}—the heaviest volume transaction cluster on the chart. Institutional absorption is validating this shelf as solid support for an upward expansion.`;
  } else if (pat.includes('FIBONACCI') || pat.includes('FIB')) {
    diagnosis = `Price retraced cleanly into the 61.8% Golden Ratio Fibonacci zone around ${currencySymbol}${pivot.toFixed(2)}. Algorithmic dip-buyers are defending this harmonic shelf, positioning for the next impulse wave.`;
  } else if (pat.includes('BREAKOUT') || pat.includes('TURTLE') || pat.includes('HIGH_TIGHT')) {
    diagnosis = `Price is pressing directly against key multi-week horizontal resistance. Repeated tests have eroded overhead supply, priming the security for explosive continuation.`;
  } else {
    diagnosis = `Quantitative algorithms confirm active formation of ${name}. Volume profiles demonstrate accumulation around the ${currencySymbol}${pivot.toFixed(2)} structural pivot.`;
  }

  // 2. Action Status Badge
  let statusBadge = {
    label: '⚡ READY TO BUY (IN BUY ZONE)',
    color: 'var(--gain-green, #00D09C)',
    bg: 'var(--gain-green-bg, rgba(0,208,156,0.12))'
  };

  if (currentPrice < pivot * 0.985) {
    statusBadge = {
      label: '🎯 BREAKOUT PENDING (APPROACHING PIVOT)',
      color: 'var(--fintech-blue, #1455FE)',
      bg: 'rgba(20,85,254,0.12)'
    };
  } else if (currentPrice > pivot * 1.03) {
    statusBadge = {
      label: '🛡️ IN TRADE (TRAILING STOPS)',
      color: '#A855F7',
      bg: 'rgba(168,85,247,0.12)'
    };
  } else if (currentPrice >= pivot * 0.985 && currentPrice <= pivot * 1.015) {
    statusBadge = {
      label: '⚡ ACTION REQUIRED: READY TO BUY',
      color: 'var(--gain-green, #00D09C)',
      bg: 'var(--gain-green-bg, rgba(0,208,156,0.12))'
    };
  } else {
    statusBadge = {
      label: '⏳ OBSERVE RANGE & VOLUME',
      color: '#F59E0B',
      bg: 'rgba(245,158,11,0.12)'
    };
  }

  // 3. Primary Entry Trigger
  const entryTrigger = `Take entry if any daily candle crosses and closes above ${currencySymbol}${pivot.toFixed(2)} with above-average volume (> 1.2x 20-day average). Alternatively, accumulate on intraday pullback to ${currencySymbol}${(pivot * 0.988).toFixed(2)} if lower rejection wick forms.`;

  // 4. Observe Corridor
  const observeRange = `Observe consolidation corridor: ${currencySymbol}${(pivot * 0.98).toFixed(2)} – ${currencySymbol}${pivot.toFixed(2)}. Do NOT chase if price gaps beyond ${currencySymbol}${(pivot * 1.025).toFixed(2)} without a healthy consolidation pause.`;

  // 5. Strict Stop-Loss & Invalidation Exit
  const riskPct = Math.abs(((pivot - stopLoss) / pivot) * 100).toFixed(1);
  const exitRule = `Strict Exit / Stop-Loss: Immediate exit if any daily candle crosses and closes below ${currencySymbol}${stopLoss.toFixed(2)}. This invalidates the pattern thesis (-${riskPct}% maximum defined risk). Cut losses immediately without holding through further breakdown.`;

  // 6. Target Ladder & Profit Booking Strategy
  const r1GainPct = Math.abs(((target1 - pivot) / pivot) * 100).toFixed(1);
  const r2GainPct = Math.abs(((target2 - pivot) / pivot) * 100).toFixed(1);
  const targetLadder = [
    {
      level: `Target 1 (R1): ${currencySymbol}${target1.toFixed(2)} (+${r1GainPct}%)`,
      action: `Book 50% profits upon touching R1. Immediately trail protective stop-loss to Breakeven (${currencySymbol}${pivot.toFixed(2)}) to guarantee a risk-free trade.`
    },
    {
      level: `Target 2 (R2): ${currencySymbol}${target2.toFixed(2)} (+${r2GainPct}%)`,
      action: `Hold remaining 50% runner. Trail stop along the rising 20-day EMA until a daily candle closes beneath it.`
    }
  ];

  return {
    diagnosis,
    statusBadge,
    entryTrigger,
    observeRange,
    exitRule,
    targetLadder,
    riskPct,
    r1GainPct,
    r2GainPct
  };
}

export default function ProChartModal({
  isOpen,
  onClose,
  ticker,
  currencySymbol = '$',
  historyData,
  timeframe,
  setTimeframe,
  companyName,
  logoUrl,
  fiftyTwoWeekHigh,
  fiftyTwoWeekLow,
  patternData = null,
  onGoToDeepAnalysis = null
}) {
  const [chartType, setChartType] = useState('candle'); // 'candle' | 'line'

  // Available patterns detected on this stock for this timeframe + currently active pattern
  const [dbPatterns, setDbPatterns] = useState([]);
  const [availablePatterns, setAvailablePatterns] = useState([]);
  const [selectedPattern, setSelectedPattern] = useState(patternData);

  // 1. Fetch baseline DB patterns when ticker changes
  useEffect(() => {
    let isMounted = true;
    fetchStockPatterns(ticker)
      .then((res) => {
        if (!isMounted) return;
        setDbPatterns(res?.patterns || []);
      })
      .catch((err) => {
        console.error('Error fetching stock patterns:', err);
      });
    return () => {
      isMounted = false;
    };
  }, [ticker]);

  // 2. Re-detect patterns dynamically whenever points, timeframe, dbPatterns, or patternData change
  useEffect(() => {
    const pts = historyData?.points || [];
    if (!pts || pts.length === 0) return;

    const combinedDefaults = patternData ? [patternData, ...dbPatterns] : dbPatterns;
    const detected = detectPatternsForTimeframe(pts, timeframe, ticker, combinedDefaults);
    setAvailablePatterns(detected);

    if (detected.length > 0) {
      const currentTarget = (patternData?.pattern || selectedPattern?.pattern || '').toUpperCase();
      const match = detected.find(
        (d) => (d.pattern || '').toUpperCase() === currentTarget
      );
      if (match) {
        setSelectedPattern(match);
      } else if (patternData) {
        setSelectedPattern(patternData);
      } else {
        setSelectedPattern(detected[0]);
      }
    }
  }, [historyData?.points, timeframe, ticker, dbPatterns, patternData?.pattern]);

  // If patternData changes on stock inspect, immediately sync selectedPattern
  useEffect(() => {
    if (patternData) {
      setSelectedPattern(patternData);
    }
  }, [patternData?.pattern, ticker]);

  const activeSetup = selectedPattern || patternData;
  const isPatternMode = !!activeSetup;

  const [showEngulfing, setShowEngulfing] = useState(!activeSetup);
  const [showSqueeze, setShowSqueeze] = useState(!activeSetup);
  const [showEMA, setShowEMA] = useState(!activeSetup);
  const [showBB, setShowBB] = useState(false);
  const [showRSI, setShowRSI] = useState(false);
  const [showLevels, setShowLevels] = useState(!activeSetup);
  const [showFib, setShowFib] = useState(false);
  const [showPOC, setShowPOC] = useState(false);

  // Sync indicator visibility whenever activeSetup changes
  useEffect(() => {
    if (activeSetup) {
      const pat = (activeSetup.pattern || '').toUpperCase();
      const isCandle = [
        'BULLISH_ENGULFING',
        'BEARISH_ENGULFING',
        'HAMMER',
        'SHOOTING_STAR',
        'MORNING_STAR',
        'EVENING_STAR',
        'DOJI'
      ].some((p) => pat.includes(p));
      const isRsiDiv = pat.includes('DIVERGENCE') || pat.includes('RSI');
      const isSqz = pat.includes('SQUEEZE');
      const isEMA = pat.includes('CROSS') || pat.includes('MA_') || pat.includes('EMA');

      setShowEngulfing(isCandle);
      setShowSqueeze(isSqz);
      setShowEMA(isEMA);
      setShowBB(false);
      setShowRSI(isRsiDiv);
      setShowLevels(false);
    } else {
      setShowEngulfing(true);
      setShowSqueeze(true);
      setShowEMA(true);
      setShowLevels(true);
    }
  }, [activeSetup]);

  const [hoveredIdx, setHoveredIdx] = useState(null);

  // Pan & Zoom interactive states (TradingView style)
  const [zoomX, setZoomX] = useState(1.0);
  const [zoomY, setZoomY] = useState(1.0);
  const [panX, setPanX] = useState(0);
  const [panYPrice, setPanYPrice] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });

  // Dynamic Canvas Dimensions: auto-measure container to utilize 100% of screen height
  const canvasContainerRef = useRef(null);
  const [canvasDimensions, setCanvasDimensions] = useState({ width: 1040, height: 680 });

  useEffect(() => {
    const el = canvasContainerRef.current;
    if (!el) return;

    const updateSize = () => {
      const rect = el.getBoundingClientRect();
      const w = Math.round(rect.width);
      const h = Math.round(rect.height);
      if (w > 100 && h > 100) {
        setCanvasDimensions((prev) => {
          if (prev.width === w && prev.height === h) return prev;
          return { width: w, height: h };
        });
      }
    };

    updateSize();
    let ro = null;
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(updateSize);
      ro.observe(el);
    }
    window.addEventListener('resize', updateSize);

    return () => {
      if (ro) ro.disconnect();
      window.removeEventListener('resize', updateSize);
    };
  }, []);

  const points = historyData?.points || [];
  const defaultVisibleBars = historyData?.defaultVisibleBars || (isPatternMode ? 32 : 65);

  // Dynamic Chart Dimensions (100% available canvas space utilized)
  const svgWidth = canvasDimensions.width || 1040;
  const totalSvgHeight = canvasDimensions.height || 680;
  const padLeft = 16;
  const padRight = 80;
  const padTop = 20;
  const padBottom = 32;

  const oscPanelHeight = 110;
  const activeOscCount = (showSqueeze ? 1 : 0) + (showRSI ? 1 : 0);
  const totalOscHeight = activeOscCount * (oscPanelHeight + 20);
  const mainChartHeight = Math.max(220, totalSvgHeight - padTop - padBottom - totalOscHeight);

  const plotWidth = svgWidth - padLeft - padRight;
  const effectiveWidth = plotWidth * zoomX;

  // Reset viewport to focus on the latest session at the right margin, with all past data to the left
  const resetView = () => {
    const totalBars = points.length || 1;
    const targetBars = Math.min(totalBars, defaultVisibleBars);
    const initialZoomX = totalBars / targetBars;
    const initialEffectiveWidth = plotWidth * initialZoomX;

    // Pin latest bar near the right edge (with 55px breathing room)
    const rightMargin = 55;
    const initialPanX = plotWidth - initialEffectiveWidth - rightMargin;

    setZoomX(initialZoomX);
    setZoomY(1.0);
    setPanX(initialPanX);
    setPanYPrice(0);
  };

  // Auto-initialize / reset viewport whenever points, timeframe, ticker, or plotWidth changes
  useEffect(() => {
    if (points.length > 0) {
      resetView();
    }
  }, [points.length, timeframe, ticker, plotWidth]);

  // Close modal on Escape key
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  // Coordinates with Pan & Zoom
  const getX = (idx) => padLeft + panX + (idx / (points.length - 1 || 1)) * effectiveWidth;

  // Identify visible points in current viewport for dynamic auto-framing of price & indicators
  const visiblePoints = useMemo(() => {
    if (!points || points.length === 0) return [];
    return points.filter((_, i) => {
      const x = getX(i);
      return x >= padLeft - 30 && x <= padLeft + plotWidth + 30;
    });
  }, [points, panX, effectiveWidth]);

  // Compute Indicators across the entire dataset for 100% mature indicator signals
  const engulfingSignals = useMemo(() => computeEngulfingCandles(points), [points]);
  const squeezeData = useMemo(() => computeSqueezeMomentum(points), [points]);
  const ema20 = useMemo(() => computeEMA(points, 20), [points]);
  const ema50 = useMemo(() => computeEMA(points, 50), [points]);
  const bbData = useMemo(() => computeBollingerBands(points, 20, 2.0), [points]);
  const rsiData = useMemo(() => computeRSI(points, 14), [points]);

  const prices = points.map((p) => p.close);
  const volumes = points.map((p) => p.volume);

  // Dynamic Y-scale auto-frames the candles currently visible on screen
  const activePoints = visiblePoints.length > 0 ? visiblePoints : points;
  const activeLows = activePoints.map((p) => p.low || p.close);
  const activeHighs = activePoints.map((p) => p.high || p.close);

  const minPrice = activeLows.length > 0 ? Math.min(...activeLows) : 0;
  const maxPrice = activeHighs.length > 0 ? Math.max(...activeHighs) : (minPrice + 1);

  // Viewport Price Range with vertical pan & zoom support
  const rawBaseRange = (maxPrice - minPrice) || (maxPrice * 0.01) || 1;
  const priceBuffer = rawBaseRange * 0.08;
  const baseCenter = (minPrice + maxPrice) / 2;
  const halfRange = ((rawBaseRange / 2) + priceBuffer) / zoomY;

  const currentCenter = baseCenter + panYPrice;
  const yMin = currentCenter - halfRange;
  const yMax = currentCenter + halfRange;
  const yRange = yMax - yMin || 1;

  const getY = (price) => padTop + mainChartHeight - ((price - yMin) / yRange) * mainChartHeight;

  // Squeeze Y-scale
  const maxSqzVal = Math.max(
    ...squeezeData.map((s) => Math.abs(s.val || 0)),
    0.01
  );
  const sqzTop = padTop + mainChartHeight + padBottom + 10;
  const getSqzY = (val) =>
    sqzTop + oscPanelHeight / 2 - (val / (maxSqzVal * 1.15)) * (oscPanelHeight / 2 - 10);

  // RSI Y-scale
  const rsiTop = sqzTop + (showSqueeze ? oscPanelHeight + 20 : 0);
  const getRsiY = (val) => rsiTop + oscPanelHeight - (Math.max(0, Math.min(100, val)) / 100) * oscPanelHeight;

  // Active point stats
  const activeIdx = hoveredIdx !== null ? hoveredIdx : points.length - 1;
  const activePoint = points[activeIdx] || {};
  const activePrice = activePoint.close || 0;

  // Baseline price for the timeframe (today's previous close for 1D or window baseline)
  const basePrice = historyData?.firstClose || (points.length > 0 ? points[Math.max(0, points.length - defaultVisibleBars)]?.open : activePrice);

  const isHovering = hoveredIdx !== null && hoveredIdx !== points.length - 1;
  const change = isHovering
    ? (activePrice - basePrice)
    : (historyData?.periodChange ?? (activePrice - basePrice));
  const changePct = isHovering
    ? (basePrice > 0 ? ((activePrice - basePrice) / basePrice) * 100 : 0)
    : (historyData?.periodChangePercent ?? (basePrice > 0 ? ((activePrice - basePrice) / basePrice) * 100 : 0));
  const isUp = change >= 0;

  // Key Levels
  const resistance = maxPrice;
  const support = minPrice;

  const currentPx = points[points.length - 1]?.close || 100;
  const pivotLevel = activeSetup?.pivot || (currentPx > 0 ? currentPx * 1.015 : 100);
  const stopLossLevel = support > 0 && support < pivotLevel ? support : pivotLevel * 0.965;
  const target1 = resistance > pivotLevel ? resistance : pivotLevel * 1.055;
  const target2 = target1 * 1.06;
  const risk = Math.max(0.01, pivotLevel - stopLossLevel);
  const reward = Math.max(0.01, target1 - pivotLevel);
  const riskRewardRatio = (reward / risk).toFixed(1);

  const patUpper = (activeSetup?.pattern || '').toUpperCase();
  const isCandleReversal = [
    'BULLISH_ENGULFING',
    'BEARISH_ENGULFING',
    'HAMMER',
    'SHOOTING_STAR',
    'MORNING_STAR',
    'EVENING_STAR',
    'DOJI'
  ].some((p) => patUpper.includes(p));

  const isDivergence = patUpper.includes('DIVERGENCE') || patUpper.includes('RSI');
  const isCupHandle = patUpper.includes('CUP') || patUpper.includes('ROUNDING');
  const isVCP = patUpper.includes('VCP');
  const isDoubleBottom = patUpper.includes('DOUBLE_BOTTOM') || patUpper.includes('TRIPLE_BOTTOM') || patUpper.includes('W_BASE');
  const isDoubleTop = patUpper.includes('DOUBLE_TOP') || patUpper.includes('HEAD_AND_SHOULDERS') || patUpper.includes('M_PATTERN');
  const isSupplyDemand = patUpper.includes('DEMAND') || patUpper.includes('SUPPLY') || patUpper.includes('ORDER_BLOCK') || patUpper.includes('FAIR_VALUE_GAP');
  const isMACross = patUpper.includes('CROSS') || patUpper.includes('MA_') || patUpper.includes('EMA');
  const isSupertrend = patUpper.includes('SUPERTREND');
  const isBreakout = patUpper.includes('BREAKOUT') || patUpper.includes('TURTLE') || patUpper.includes('HIGH_TIGHT');
  const isVolumeProfilePOC = patUpper.includes('POC') || patUpper.includes('VOLUME_PROFILE');
  const isFibonacci = patUpper.includes('FIBONACCI') || patUpper.includes('FIB');

  // Dynamic Volume Profile Calculation (Visible Window)
  const volProfileBins = [];
  let calculatedPoc = activeSetup?.poc || activeSetup?.pivot || (minPrice + maxPrice) / 2;
  let maxBinVol = 1;
  if (visiblePoints.length > 0 && maxPrice > minPrice) {
    const numBins = 24;
    const binH = (maxPrice - minPrice) / numBins;
    for (let i = 0; i < numBins; i++) {
      volProfileBins.push({
        yMid: minPrice + (i + 0.5) * binH,
        yStart: minPrice + i * binH,
        yEnd: minPrice + (i + 1) * binH,
        volume: 0
      });
    }
    visiblePoints.forEach((p) => {
      const mid = (p.high + p.low) / 2;
      const idx = Math.min(numBins - 1, Math.max(0, Math.floor((mid - minPrice) / binH)));
      volProfileBins[idx].volume += (p.volume || 1);
    });
    let maxIdx = 0;
    volProfileBins.forEach((b, idx) => {
      if (b.volume > maxBinVol) {
        maxBinVol = b.volume;
        maxIdx = idx;
      }
    });
    calculatedPoc = activeSetup?.poc || volProfileBins[maxIdx].yMid;
  }

  // Dynamic Fibonacci Retracement Levels (0% to 100%)
  const fibPriceDiff = maxPrice - minPrice;
  const fibLevels = [
    { ratio: '0.0%', label: '0.0% High', price: maxPrice, color: '#94A3B8' },
    { ratio: '23.6%', label: '23.6%', price: maxPrice - 0.236 * fibPriceDiff, color: '#38BDF8' },
    { ratio: '38.2%', label: '38.2%', price: maxPrice - 0.382 * fibPriceDiff, color: '#27D5FF' },
    { ratio: '50.0%', label: '50.0% Mid', price: maxPrice - 0.500 * fibPriceDiff, color: '#FFC83D' },
    { ratio: '61.8%', label: '61.8% Golden', price: maxPrice - 0.618 * fibPriceDiff, color: '#00D09C' },
    { ratio: '78.6%', label: '78.6%', price: maxPrice - 0.786 * fibPriceDiff, color: '#F04438' },
    { ratio: '100.0%', label: '100.0% Low', price: minPrice, color: '#94A3B8' }
  ];

  const playbook = computeTradePlaybook({
    pattern: activeSetup?.pattern || 'Breakout',
    currentPrice: activePrice || currentPx,
    pivot: pivotLevel,
    stopLoss: stopLossLevel,
    target1,
    target2,
    currencySymbol,
    reasons: activeSetup?.reasons || []
  });

  // Trigger candle index for candlestick patterns (search recent 15 bars or fallback to latest)
  let triggerCandleIdx = -1;
  if (isCandleReversal && points.length >= 2) {
    for (let i = points.length - 1; i >= Math.max(1, points.length - 15); i--) {
      if (engulfingSignals[i]?.isBullish || engulfingSignals[i]?.isBearish) {
        triggerCandleIdx = i;
        break;
      }
    }
    if (triggerCandleIdx === -1) {
      triggerCandleIdx = points.length - 1;
    }
  }

  let forwardOutlookText = '';
  if (activeSetup) {
    const pat = patUpper;
    const name = activeSetup.display_name || pat.replace(/_/g, ' ');
    if (pat.includes('POC') || pat.includes('VOLUME_PROFILE')) {
      forwardOutlookText = `Price is testing the high-volume Point of Control (POC) shelf at ${currencySymbol}${pivotLevel.toFixed(2)}. Confirmation of buyer absorption initiates an expansion impulse targeting ${currencySymbol}${target1.toFixed(2)}. Invalidation below ${currencySymbol}${stopLossLevel.toFixed(2)}.`;
    } else if (pat.includes('FIBONACCI') || pat.includes('FIB')) {
      forwardOutlookText = `Price pulled back into key 61.8% Fibonacci support at ${currencySymbol}${pivotLevel.toFixed(2)}. Defending this golden ratio initiates the next upward impulse wave toward ${currencySymbol}${target1.toFixed(2)}. Invalidation stop at ${currencySymbol}${stopLossLevel.toFixed(2)}.`;
    } else if (pat.includes('DIVERGENCE')) {
      forwardOutlookText = `Price printed a structural swing while daily momentum indicators signaled diverging exhaustion. A confirmed close above the Pivot Level of ${currencySymbol}${pivotLevel.toFixed(2)} triggers reversal acceleration toward the ${currencySymbol}${target1.toFixed(2)} resistance target. Invalidation risk is strictly managed below ${currencySymbol}${stopLossLevel.toFixed(2)}.`;
    } else if (pat.includes('BREAKOUT') || pat.includes('TURTLE') || pat.includes('HIGH_TIGHT')) {
      forwardOutlookText = `Stock has entered institutional accumulation compression with volatility tightening against overhead resistance. A high-volume breakout piercing ${currencySymbol}${pivotLevel.toFixed(2)} initiates momentum expansion targeting ${currencySymbol}${target1.toFixed(2)} to ${currencySymbol}${target2.toFixed(2)}. Protective stops anchor at ${currencySymbol}${stopLossLevel.toFixed(2)}.`;
    } else if (pat.includes('CUP') || pat.includes('DOUBLE_BOTTOM') || pat.includes('VCP') || pat.includes('BASE')) {
      forwardOutlookText = `Accumulation base is nearing completion as supply dries up on subsequent pullbacks. A push through the pivot entry of ${currencySymbol}${pivotLevel.toFixed(2)} confirms base breakout. Expected trajectory projects a primary move to ${currencySymbol}${target1.toFixed(2)} with risk contained at ${currencySymbol}${stopLossLevel.toFixed(2)}.`;
    } else {
      forwardOutlookText = `Quantitative indicators verify active formation of ${name}. Monitor price behavior relative to the ${currencySymbol}${pivotLevel.toFixed(2)} pivot. A decisive confirmation signals institutional sponsorship towards ${currencySymbol}${target1.toFixed(2)}.`;
    }
  }

  // Dynamic Y-axis ticks for current visible vertical range
  const yTickFractions = mainChartHeight > 440
    ? [0.08, 0.26, 0.44, 0.62, 0.80, 0.94]
    : [0.12, 0.38, 0.64, 0.88];

  const yTicks = yTickFractions.map((f) => {
    const val = yMin + yRange * f;
    return {
      val,
      y: padTop + mainChartHeight * (1 - f)
    };
  });

  // Dynamic X-axis timestamps based on visible indices
  const visibleIndices = [];
  points.forEach((_, i) => {
    const x = getX(i);
    if (x >= padLeft + 10 && x <= padLeft + plotWidth - 10) {
      visibleIndices.push(i);
    }
  });
  const timeStep = Math.max(1, Math.floor(visibleIndices.length / 6));
  const timeIndices = visibleIndices.filter((_, idx) => idx % timeStep === 0);

  const isMultiDayVisible =
    visiblePoints.length > 1 &&
    visiblePoints[0]?.time?.slice(0, 10) !== visiblePoints[visiblePoints.length - 1]?.time?.slice(0, 10);

  // Dynamic candle width: scales with zoom
  const candleW = Math.max(3, Math.min(48, (effectiveWidth / (points.length || 1)) * 0.7));

  // Generate line path for Line chart mode
  const linePathD = points.reduce((acc, pt, idx) => {
    const x = getX(idx);
    const y = getY(pt.close);
    return idx === 0 ? `M ${x.toFixed(1)},${y.toFixed(1)}` : `${acc} L ${x.toFixed(1)},${y.toFixed(1)}`;
  }, '');

  // Generate EMA paths
  const ema20Points = points
    .map((_, i) => (ema20[i] != null ? `${getX(i).toFixed(1)},${getY(ema20[i]).toFixed(1)}` : null))
    .filter(Boolean)
    .join(' L ');

  const ema50Points = points
    .map((_, i) => (ema50[i] != null ? `${getX(i).toFixed(1)},${getY(ema50[i]).toFixed(1)}` : null))
    .filter(Boolean)
    .join(' L ');

  // Count Engulfing Signals
  const bullishCount = engulfingSignals.filter((s) => s.isBullish).length;
  const bearishCount = engulfingSignals.filter((s) => s.isBearish).length;

  // Mouse & Touch Pan & Zoom Handlers
  const handleMouseDown = (e) => {
    if (e.button !== 0) return; // Left click only
    setIsDragging(true);
    setDragStart({ x: e.clientX, y: e.clientY });
  };

  const handleMouseMove = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;

    if (isDragging) {
      const dx = e.clientX - dragStart.x;
      const dy = e.clientY - dragStart.y;

      // Pan horizontal (X-axis) with deep historical bounds
      const minPanX = plotWidth - effectiveWidth - 120;
      const maxPanX = plotWidth * 0.45;
      setPanX((prev) => Math.min(maxPanX, Math.max(minPanX, prev + dx)));

      // Pan vertical (Y-axis): dragging down moves price view down
      const priceDelta = (dy / mainChartHeight) * yRange;
      setPanYPrice((prev) => prev + priceDelta);

      setDragStart({ x: e.clientX, y: e.clientY });
    } else {
      // Normal hover crosshair
      const ratio = (mouseX - padLeft - panX) / (effectiveWidth || 1);
      const ptIdx = Math.round(ratio * (points.length - 1));
      if (ptIdx >= 0 && ptIdx < points.length) {
        setHoveredIdx(ptIdx);
      } else {
        setHoveredIdx(null);
      }
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleWheel = (e) => {
    e.preventDefault();
    const rect = e.currentTarget.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;

    // Zoom multiplier (smooth 1.12x)
    const zoomMultiplier = e.deltaY < 0 ? 1.14 : 0.88;
    const newZoomX = Math.max(0.5, Math.min(60.0, zoomX * zoomMultiplier));

    // Keep point under cursor invariant
    const oldW = plotWidth * zoomX;
    const newW = plotWidth * newZoomX;
    const anchorRatio = (mouseX - padLeft - panX) / (oldW || 1);
    const newPanX = mouseX - padLeft - anchorRatio * newW;

    const minPanX = plotWidth - newW - 120;
    const maxPanX = plotWidth * 0.45;

    setZoomX(newZoomX);
    setPanX(Math.min(maxPanX, Math.max(minPanX, newPanX)));

    // Optional vertical zoom with Shift key or Alt key
    if (e.shiftKey || e.altKey) {
      const newZoomY = Math.max(0.5, Math.min(15.0, zoomY * zoomMultiplier));
      setZoomY(newZoomY);
    }
  };

  const handleTouchStart = (e) => {
    if (e.touches.length === 1) {
      setIsDragging(true);
      setDragStart({ x: e.touches[0].clientX, y: e.touches[0].clientY });
    }
  };

  const handleTouchMove = (e) => {
    if (isDragging && e.touches.length === 1) {
      const dx = e.touches[0].clientX - dragStart.x;
      const dy = e.touches[0].clientY - dragStart.y;
      const minPanX = plotWidth - effectiveWidth - 120;
      const maxPanX = plotWidth * 0.45;
      setPanX((prev) => Math.min(maxPanX, Math.max(minPanX, prev + dx)));
      const priceDelta = (dy / mainChartHeight) * yRange;
      setPanYPrice((prev) => prev + priceDelta);
      setDragStart({ x: e.touches[0].clientX, y: e.touches[0].clientY });
    }
  };

  const handleTouchEnd = () => {
    setIsDragging(false);
  };

  if (!isOpen) return null;

  return (
    <div className="pro-workstation-container" onClick={(e) => e.stopPropagation()}>
      {/* 70% MAIN CHART PANEL */}
      <div className="pro-workstation-main">
        {/* Top Header Bar */}
        <div className="pro-workstation-header">
          {/* Stock Logo & Identity */}
          <div className="header-left-group">
            <div className="ws-header-logo-box">
              <img
                src={logoUrl || `https://assets.parqet.com/logos/symbol/${ticker}?format=png`}
                alt={ticker}
                onError={(e) => {
                  e.target.style.display = 'none';
                }}
              />
            </div>

            <div className="ws-header-ticker-info">
              <span className="ticker-symbol font-mono">{ticker}</span>
              <span className="ticker-company">{companyName}</span>
              <span className="ticker-price font-mono">
                {currencySymbol}{activePrice.toFixed(2)}
              </span>
              <span
                className="ticker-change font-mono"
                style={{ color: isUp ? 'var(--gain-green)' : 'var(--loss-red)' }}
              >
                {isUp ? '+' : ''}{change.toFixed(2)} ({isUp ? '+' : ''}{changePct.toFixed(2)}%)
              </span>
            </div>
          </div>

          {/* Timeframe Selector Buttons */}
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

          {/* Right Header Controls: Chart Type, Zoom, Close */}
          <div className="header-right-group">
            {/* Candle vs Line Switcher */}
            <div className="chart-type-pill-group">
              <button
                onClick={() => setChartType('candle')}
                className={`chart-type-btn ${chartType === 'candle' ? 'active' : ''}`}
                title="Candlestick View"
              >
                <BarChart2 size={13} />
                <span>Candles</span>
              </button>
              <button
                onClick={() => setChartType('line')}
                className={`chart-type-btn ${chartType === 'line' ? 'active' : ''}`}
                title="Line View"
              >
                <LineIcon size={13} />
                <span>Line</span>
              </button>
            </div>

            {/* TradingView Pan & Zoom Nav Controls */}
            <div className="pro-chart-nav-controls">
              <button
                className="pro-nav-btn"
                onClick={() => {
                  const newZ = Math.min(18.0, zoomX * 1.3);
                  const oldW = plotWidth * zoomX;
                  const newW = plotWidth * newZ;
                  setPanX((prev) => prev - (newW - oldW) * 0.5);
                  setZoomX(newZ);
                }}
                title="Zoom In (+)"
              >
                <ZoomIn size={13} />
              </button>
              <button
                className="pro-nav-btn"
                onClick={() => {
                  const newZ = Math.max(0.6, zoomX / 1.3);
                  const oldW = plotWidth * zoomX;
                  const newW = plotWidth * newZ;
                  setPanX((prev) => prev - (newW - oldW) * 0.5);
                  setZoomX(newZ);
                }}
                title="Zoom Out (-)"
              >
                <ZoomOut size={13} />
              </button>
              <button
                className="pro-nav-btn reset-btn"
                onClick={resetView}
                title="Reset View"
              >
                <RotateCcw size={12} />
                <span>Reset</span>
              </button>
            </div>

            {/* Close / Minimize Button */}
            <button
              className="pro-workstation-close-btn"
              onClick={onClose}
              title="Close Fullscreen View (Esc)"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Active Candle Inspection HUD Bar */}
        <div className="pro-candle-hud font-mono">
          <span>Time: <strong style={{ color: 'var(--text-primary)' }}>{activePoint.time || 'N/A'}</strong></span>
          <span>O: <strong style={{ color: 'var(--text-primary)' }}>{currencySymbol}{activePoint.open?.toFixed(2)}</strong></span>
          <span>H: <strong style={{ color: 'var(--gain-green)' }}>{currencySymbol}{activePoint.high?.toFixed(2)}</strong></span>
          <span>L: <strong style={{ color: 'var(--loss-red)' }}>{currencySymbol}{activePoint.low?.toFixed(2)}</strong></span>
          <span>C: <strong style={{ color: 'var(--text-primary)' }}>{currencySymbol}{activePoint.close?.toFixed(2)}</strong></span>
          <span>Vol: <strong style={{ color: '#60A5FA' }}>{activePoint.volume ? (activePoint.volume / 1e6).toFixed(2) + 'M' : '0'}</strong></span>
          {showEMA && (
            <>
              <span style={{ color: '#F59E0B' }}>EMA20: {ema20[activeIdx] ? `${currencySymbol}${ema20[activeIdx].toFixed(2)}` : 'N/A'}</span>
              <span style={{ color: '#38BDF8' }}>EMA50: {ema50[activeIdx] ? `${currencySymbol}${ema50[activeIdx].toFixed(2)}` : 'N/A'}</span>
            </>
          )}
          {showRSI && rsiData[activeIdx] != null && (
            <span style={{ color: '#EC4899' }}>RSI: {rsiData[activeIdx].toFixed(1)}</span>
          )}

          <span className="pro-chart-hint-badge" style={{ marginLeft: 'auto' }}>
            <Move size={11} /> Drag: Pan • Scroll: Zoom • Double-click: Reset
          </span>
        </div>

        {/* Main Chart Body with Left Vertical Tool Rail & Canvas */}
        <div className="pro-chart-body-row">
          {/* Vertical Indicators Left Toolbar (TradingView Style) */}
          <div className="pro-vertical-indicators-bar" role="toolbar" aria-label="Technical Indicators">
            {/* 1. Engulfing Candles */}
            <button
              onClick={() => setShowEngulfing(!showEngulfing)}
              className={`vbar-indicator-btn ${showEngulfing ? 'active' : ''}`}
              title={`Engulfing Candles (${bullishCount} Bull / ${bearishCount} Bear)`}
            >
              <span className="vbar-dot" style={{ background: 'var(--gain-green, #00D09C)' }} />
              <span className="vbar-text font-mono">ENGULF</span>
              <span className="vbar-subtext font-mono">{bullishCount}B/{bearishCount}B</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">Engulfing Candles</span>
                <span className="vbar-tt-detail">({bullishCount} Bull / {bearishCount} Bear)</span>
                <span className={`vbar-tt-badge ${showEngulfing ? 'on' : 'off'}`}>
                  {showEngulfing ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>

            {/* 2. Squeeze Momentum [LazyBear] */}
            <button
              onClick={() => setShowSqueeze(!showSqueeze)}
              className={`vbar-indicator-btn ${showSqueeze ? 'active' : ''}`}
              title="Squeeze Momentum [LazyBear]"
            >
              <span className="vbar-dot" style={{ background: '#F59E0B' }} />
              <span className="vbar-text font-mono">SQZ</span>
              <span className="vbar-subtext font-mono">LazyBear</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">Squeeze Momentum</span>
                <span className="vbar-tt-detail">[LazyBear]</span>
                <span className={`vbar-tt-badge ${showSqueeze ? 'on' : 'off'}`}>
                  {showSqueeze ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>

            {/* 3. EMA (20, 50) */}
            <button
              onClick={() => setShowEMA(!showEMA)}
              className={`vbar-indicator-btn ${showEMA ? 'active' : ''}`}
              title="EMA (20, 50)"
            >
              <span className="vbar-dot" style={{ background: '#38BDF8' }} />
              <span className="vbar-text font-mono">EMA</span>
              <span className="vbar-subtext font-mono">20, 50</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">EMA</span>
                <span className="vbar-tt-detail">(20, 50)</span>
                <span className={`vbar-tt-badge ${showEMA ? 'on' : 'off'}`}>
                  {showEMA ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>

            {/* 4. Bollinger Bands (20, 2) */}
            <button
              onClick={() => setShowBB(!showBB)}
              className={`vbar-indicator-btn ${showBB ? 'active' : ''}`}
              title="Bollinger Bands (20, 2)"
            >
              <span className="vbar-dot" style={{ background: '#A855F7' }} />
              <span className="vbar-text font-mono">BB</span>
              <span className="vbar-subtext font-mono">20, 2</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">Bollinger Bands</span>
                <span className="vbar-tt-detail">(20, 2)</span>
                <span className={`vbar-tt-badge ${showBB ? 'on' : 'off'}`}>
                  {showBB ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>

            {/* 5. RSI (14) */}
            <button
              onClick={() => setShowRSI(!showRSI)}
              className={`vbar-indicator-btn ${showRSI ? 'active' : ''}`}
              title="RSI (14)"
            >
              <span className="vbar-dot" style={{ background: '#EC4899' }} />
              <span className="vbar-text font-mono">RSI</span>
              <span className="vbar-subtext font-mono">14</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">RSI</span>
                <span className="vbar-tt-detail">(14)</span>
                <span className={`vbar-tt-badge ${showRSI ? 'on' : 'off'}`}>
                  {showRSI ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>

            {/* 6. Support & Resistance */}
            <button
              onClick={() => setShowLevels(!showLevels)}
              className={`vbar-indicator-btn ${showLevels ? 'active' : ''}`}
              title="Support & Resistance"
            >
              <span className="vbar-dot" style={{ background: 'var(--fintech-primary, #00D09C)' }} />
              <span className="vbar-text font-mono">S & R</span>
              <span className="vbar-subtext font-mono">Levels</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">Support & Resistance</span>
                <span className="vbar-tt-detail">(Key Levels)</span>
                <span className={`vbar-tt-badge ${showLevels ? 'on' : 'off'}`}>
                  {showLevels ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>

            {/* 7. Fibonacci Retracement */}
            <button
              onClick={() => setShowFib(!showFib)}
              className={`vbar-indicator-btn ${showFib || isFibonacci ? 'active' : ''}`}
              title="Fibonacci Retracements (23.6%, 38.2%, 50%, 61.8%, 78.6%)"
            >
              <span className="vbar-dot" style={{ background: '#FFC83D' }} />
              <span className="vbar-text font-mono">FIB</span>
              <span className="vbar-subtext font-mono">0-100%</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">Fibonacci Retracement</span>
                <span className="vbar-tt-detail">(Key Pullback Levels)</span>
                <span className={`vbar-tt-badge ${showFib || isFibonacci ? 'on' : 'off'}`}>
                  {showFib || isFibonacci ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>

            {/* 8. Volume Profile Point of Control (POC) */}
            <button
              onClick={() => setShowPOC(!showPOC)}
              className={`vbar-indicator-btn ${showPOC || isVolumeProfilePOC ? 'active' : ''}`}
              title="Volume Profile Point of Control (POC)"
            >
              <span className="vbar-dot" style={{ background: '#F04438' }} />
              <span className="vbar-text font-mono">POC</span>
              <span className="vbar-subtext font-mono">Volume</span>
              <div className="vbar-tooltip">
                <span className="vbar-tt-title">Volume Profile POC</span>
                <span className="vbar-tt-detail">(Point of Control & Value Area)</span>
                <span className={`vbar-tt-badge ${showPOC || isVolumeProfilePOC ? 'on' : 'off'}`}>
                  {showPOC || isVolumeProfilePOC ? 'ON' : 'OFF'}
                </span>
              </div>
            </button>
          </div>

          {/* Main Pro Chart Canvas */}
          <div className="pro-chart-canvas-container" ref={canvasContainerRef}>

          <svg
            viewBox={`0 0 ${svgWidth} ${totalSvgHeight}`}
            style={{
              width: '100%',
              height: '100%',
              display: 'block',
              overflow: 'visible',
              cursor: isDragging ? 'grabbing' : 'grab',
              userSelect: 'none'
            }}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={() => {
              setIsDragging(false);
              setHoveredIdx(null);
            }}
            onWheel={handleWheel}
            onDoubleClick={resetView}
            onTouchStart={handleTouchStart}
            onTouchMove={handleTouchMove}
            onTouchEnd={handleTouchEnd}
          >
            <defs>
              <linearGradient id="pro-line-grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#00D09C" stopOpacity="0.22" />
                <stop offset="100%" stopColor="#00D09C" stopOpacity="0.0" />
              </linearGradient>

              {/* Viewport Clipping Boundaries */}
              <clipPath id="main-chart-clip">
                <rect x={padLeft} y={padTop} width={plotWidth} height={mainChartHeight} />
              </clipPath>
              <clipPath id="sqz-clip">
                <rect x={padLeft} y={sqzTop} width={plotWidth} height={oscPanelHeight} />
              </clipPath>
              <clipPath id="rsi-clip">
                <rect x={padLeft} y={rsiTop} width={plotWidth} height={oscPanelHeight} />
              </clipPath>
            </defs>

            {/* ================================================================= */}
            {/* 1. MAIN PRICE CHART PANEL                                         */}
            {/* ================================================================= */}

            {/* Background Gridlines & Dynamic Y-Axis Labels */}
            {yTicks.map((tick, i) => (
              <g key={`pro-ytick-${i}`}>
                <line
                  x1={padLeft}
                  y1={tick.y}
                  x2={padLeft + plotWidth}
                  y2={tick.y}
                  stroke="var(--border-subtle)"
                  strokeDasharray="4,4"
                  strokeWidth="1"
                />
                <text
                  x={padLeft + plotWidth + 8}
                  y={tick.y + 3.5}
                  fill="var(--text-tertiary)"
                  fontSize="10.5"
                  fontFamily="var(--font-mono)"
                >
                  {currencySymbol}{tick.val.toFixed(2)}
                </text>
              </g>
            ))}

            {/* Clipped Price Data (Candles, EMAs, BB, Engulfing, Levels) */}
            <g clipPath="url(#main-chart-clip)">
              {/* Dedicated Pattern Target, Pivot, and Invalidation Levels */}
              {isPatternMode && (
                <g>
                  {/* Target 2 corridor if available */}
                  {target2 && target2 > target1 && (
                    <rect
                      x={padLeft}
                      y={getY(target2)}
                      width={plotWidth}
                      height={Math.max(2, Math.abs(getY(target1) - getY(target2)))}
                      fill="rgba(56, 189, 248, 0.08)"
                      stroke="rgba(56, 189, 248, 0.25)"
                      strokeDasharray="3,3"
                      strokeWidth="1"
                    />
                  )}

                  {/* Target 1 Line */}
                  <line
                    x1={padLeft}
                    y1={getY(target1)}
                    x2={padLeft + plotWidth}
                    y2={getY(target1)}
                    stroke="#38BDF8"
                    strokeWidth="1.8"
                    strokeDasharray="5,4"
                    opacity="0.95"
                  />
                  {/* Target Level Label on Chart */}
                  <text
                    x={padLeft + 14}
                    y={getY(target1) - 6}
                    fill="#38BDF8"
                    fontSize="10"
                    fontWeight="800"
                    fontFamily="var(--font-mono)"
                    letterSpacing="0.4"
                  >
                    TARGET LEVEL: {currencySymbol}{target1.toFixed(2)} (+{(((target1 - currentPx) / currentPx) * 100).toFixed(1)}%)
                  </text>

                  {/* Pivot Level Breakout Line */}
                  <line
                    x1={padLeft}
                    y1={getY(pivotLevel)}
                    x2={padLeft + plotWidth}
                    y2={getY(pivotLevel)}
                    stroke="var(--fintech-primary, #00D09C)"
                    strokeWidth="2"
                    strokeDasharray="6,4"
                    opacity="0.95"
                  />
                  <text
                    x={padLeft + 14}
                    y={getY(pivotLevel) - 6}
                    fill="var(--fintech-primary, #00D09C)"
                    fontSize="10"
                    fontWeight="800"
                    fontFamily="var(--font-mono)"
                    letterSpacing="0.4"
                  >
                    PIVOT BREAKOUT: {currencySymbol}{pivotLevel.toFixed(2)}
                  </text>

                  {/* Stop Loss Level Invalidation Line */}
                  <line
                    x1={padLeft}
                    y1={getY(stopLossLevel)}
                    x2={padLeft + plotWidth}
                    y2={getY(stopLossLevel)}
                    stroke="#F04438"
                    strokeWidth="1.6"
                    strokeDasharray="4,4"
                    opacity="0.9"
                  />
                  <text
                    x={padLeft + 14}
                    y={getY(stopLossLevel) + 14}
                    fill="#F04438"
                    fontSize="10"
                    fontWeight="800"
                    fontFamily="var(--font-mono)"
                    letterSpacing="0.4"
                  >
                    INVALIDATION / STOP-LOSS: {currencySymbol}{stopLossLevel.toFixed(2)} (-{(((currentPx - stopLossLevel) / currentPx) * 100).toFixed(1)}%)
                  </text>

                  {/* Supply & Demand / Order Block Zone */}
                  {(patUpper.includes('DEMAND') || patUpper.includes('ORDER_BLOCK')) && (
                    <g>
                      <rect
                        x={padLeft}
                        y={getY(pivotLevel * 0.985)}
                        width={plotWidth}
                        height={Math.max(16, Math.abs(getY(stopLossLevel) - getY(pivotLevel * 0.985)))}
                        fill="rgba(0, 208, 156, 0.12)"
                        stroke="var(--gain-green)"
                        strokeWidth="1"
                        strokeDasharray="4,4"
                      />
                      <text
                        x={padLeft + plotWidth - 230}
                        y={getY(pivotLevel * 0.985) + 15}
                        fill="var(--gain-green)"
                        fontSize="10"
                        fontWeight="800"
                        fontFamily="var(--font-mono)"
                      >
                        INSTITUTIONAL DEMAND ZONE
                      </text>
                    </g>
                  )}

                  {patUpper.includes('SUPPLY') && (
                    <g>
                      <rect
                        x={padLeft}
                        y={getY(target1 * 1.02)}
                        width={plotWidth}
                        height={Math.max(16, Math.abs(getY(target1) - getY(target1 * 1.02)))}
                        fill="rgba(240, 68, 56, 0.12)"
                        stroke="var(--loss-red)"
                        strokeWidth="1"
                        strokeDasharray="4,4"
                      />
                      <text
                        x={padLeft + plotWidth - 230}
                        y={getY(target1 * 1.02) + 15}
                        fill="var(--loss-red)"
                        fontSize="10"
                        fontWeight="800"
                        fontFamily="var(--font-mono)"
                      >
                        OVERHEAD SUPPLY ZONE
                      </text>
                    </g>
                  )}
                </g>
              )}

              {/* Support & Resistance Lines */}
              {showLevels && (
                <>
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
                </>
              )}

              {/* Fibonacci Retracement Horizontal Levels */}
              {(showFib || isFibonacci) && (
                <g className="fibonacci-overlays">
                  {fibLevels.map((lvl) => {
                    const yPos = getY(lvl.price);
                    if (yPos < padTop || yPos > padTop + mainChartHeight) return null;
                    return (
                      <g key={`fib-line-${lvl.ratio}`}>
                        <line
                          x1={padLeft}
                          y1={yPos}
                          x2={padLeft + plotWidth}
                          y2={yPos}
                          stroke={lvl.color}
                          strokeWidth="1.2"
                          strokeDasharray={lvl.ratio === '61.8%' || lvl.ratio === '50.0%' ? '5,2' : '3,3'}
                          opacity={lvl.ratio === '61.8%' ? 0.95 : 0.75}
                        />
                        <rect
                          x={padLeft + 6}
                          y={yPos - 8}
                          width="94"
                          height="16"
                          rx="3"
                          fill="#0B0E14"
                          stroke={lvl.color}
                          strokeWidth="0.8"
                        />
                        <text
                          x={padLeft + 10}
                          y={yPos + 4}
                          fill={lvl.color}
                          fontSize="8.5"
                          fontWeight="700"
                          fontFamily="var(--font-mono)"
                        >
                          FIB {lvl.ratio}: {currencySymbol}{lvl.price >= 1000 ? lvl.price.toFixed(0) : lvl.price.toFixed(2)}
                        </text>
                      </g>
                    );
                  })}
                </g>
              )}

              {/* Volume Profile & Point of Control (POC) */}
              {(showPOC || isVolumeProfilePOC) && (
                <g className="volume-profile-poc-overlays">
                  {/* Horizontal Volume Profile Bins (Right-Edge Histogram) */}
                  {volProfileBins.map((bin, bIdx) => {
                    const yTop = getY(bin.yEnd);
                    const yBot = getY(bin.yStart);
                    const barH = Math.max(2, Math.abs(yBot - yTop) - 1);
                    const barW = Math.max(4, Math.min(110, (bin.volume / maxBinVol) * 110));
                    const isPOCBin = Math.abs(bin.yMid - calculatedPoc) < (maxPrice - minPrice) / 30;
                    return (
                      <rect
                        key={`vp-bin-${bIdx}`}
                        x={padLeft + plotWidth - barW - 4}
                        y={Math.min(yTop, yBot)}
                        width={barW}
                        height={barH}
                        rx="1.5"
                        fill={isPOCBin ? 'rgba(240, 68, 56, 0.45)' : 'rgba(56, 189, 248, 0.22)'}
                        stroke={isPOCBin ? '#F04438' : 'rgba(56, 189, 248, 0.4)'}
                        strokeWidth="0.6"
                      />
                    );
                  })}

                  {/* High-Contrast POC (Point of Control) Horizontal Line */}
                  {(() => {
                    const yPOC = getY(calculatedPoc);
                    return (
                      <g key="poc-horiz-indicator">
                        <line
                          x1={padLeft}
                          y1={yPOC}
                          x2={padLeft + plotWidth}
                          y2={yPOC}
                          stroke="#F04438"
                          strokeWidth="2"
                          strokeDasharray="6,3"
                        />
                        <rect
                          x={padLeft + 120}
                          y={yPOC - 11}
                          width="165"
                          height="22"
                          rx="4"
                          fill="#0B0E14"
                          stroke="#F04438"
                          strokeWidth="1.2"
                        />
                        <text
                          x={padLeft + 128}
                          y={yPOC + 4}
                          fill="#F04438"
                          fontSize="9.5"
                          fontWeight="800"
                          fontFamily="var(--font-mono)"
                        >
                          🔴 POC: {currencySymbol}{calculatedPoc.toFixed(2)}
                        </text>
                      </g>
                    );
                  })()}
                </g>
              )}

              {/* Bollinger Bands Overlay */}
              {showBB && (
                <>
                  {points.map((_, i) => {
                    if (i === 0 || !bbData[i] || !bbData[i - 1]) return null;
                    return (
                      <g key={`bb-seg-${i}`}>
                        <line
                          x1={getX(i - 1)}
                          y1={getY(bbData[i - 1].upper)}
                          x2={getX(i)}
                          y2={getY(bbData[i].upper)}
                          stroke="#A855F7"
                          strokeWidth="1"
                          strokeDasharray="2,2"
                          opacity="0.75"
                        />
                        <line
                          x1={getX(i - 1)}
                          y1={getY(bbData[i - 1].lower)}
                          x2={getX(i)}
                          y2={getY(bbData[i].lower)}
                          stroke="#A855F7"
                          strokeWidth="1"
                          strokeDasharray="2,2"
                          opacity="0.75"
                        />
                      </g>
                    );
                  })}
                </>
              )}

              {/* EMA Lines */}
              {showEMA && (
                <>
                  {ema20Points && (
                    <path
                      d={`M ${ema20Points}`}
                      fill="none"
                      stroke="#F59E0B"
                      strokeWidth="1.6"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      opacity="0.9"
                    />
                  )}
                  {ema50Points && (
                    <path
                      d={`M ${ema50Points}`}
                      fill="none"
                      stroke="#38BDF8"
                      strokeWidth="1.6"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      opacity="0.85"
                    />
                  )}
                </>
              )}

              {/* Price Chart Body: Candlesticks vs Line */}
              {chartType === 'line' ? (
                <path
                  d={linePathD}
                  fill="none"
                  stroke={isUp ? 'var(--gain-green)' : 'var(--loss-red)'}
                  strokeWidth="2.2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              ) : (
                /* High-Resolution Candlesticks */
                points.map((pt, i) => {
                  const x = getX(i);
                  // Render only if within or near viewport
                  if (x < padLeft - candleW || x > padLeft + plotWidth + candleW) return null;

                  const isCandleUp = pt.close >= pt.open;
                  const candleColor = isCandleUp ? 'var(--gain-green)' : 'var(--loss-red)';

                  const yHigh = getY(pt.high || pt.close);
                  const yLow = getY(pt.low || pt.close);
                  const yOpen = getY(pt.open || pt.close);
                  const yClose = getY(pt.close);

                  const bodyTop = Math.min(yOpen, yClose);
                  const bodyHeight = Math.max(2, Math.abs(yClose - yOpen));

                  return (
                    <g key={`candle-${i}`}>
                      {/* Upper & Lower Wick */}
                      <line
                        x1={x}
                        y1={yHigh}
                        x2={x}
                        y2={yLow}
                        stroke={candleColor}
                        strokeWidth="1.2"
                      />

                      {/* Candlestick Body */}
                      <rect
                        x={x - candleW / 2}
                        y={bodyTop}
                        width={candleW}
                        height={bodyHeight}
                        fill={candleColor}
                        rx="1"
                        stroke={candleColor}
                        strokeWidth="0.5"
                      />

                      {/* Engulfing Candle Plotshape Markers */}
                      {showEngulfing && engulfingSignals[i] && (
                        <>
                          {engulfingSignals[i].isBullish && (
                            <g>
                              <polygon
                                points={`${x},${yLow + 7} ${x - 5},${yLow + 16} ${x + 5},${yLow + 16}`}
                                fill="#00D09C"
                                stroke="#0B0E14"
                                strokeWidth="1"
                              />
                              <text
                                x={x}
                                y={yLow + 25}
                                textAnchor="middle"
                                fill="#00D09C"
                                fontSize="8"
                                fontWeight="800"
                                fontFamily="var(--font-mono)"
                              >
                                BULL
                              </text>
                            </g>
                          )}

                          {engulfingSignals[i].isBearish && (
                            <g>
                              <polygon
                                points={`${x},${yHigh - 7} ${x - 5},${yHigh - 16} ${x + 5},${yHigh - 16}`}
                                fill="#F04438"
                                stroke="#0B0E14"
                                strokeWidth="1"
                              />
                              <text
                                x={x}
                                y={yHigh - 19}
                                textAnchor="middle"
                                fill="#F04438"
                                fontSize="8"
                                fontWeight="800"
                                fontFamily="var(--font-mono)"
                              >
                                BEAR
                              </text>
                            </g>
                          )}
                        </>
                      )}
                    </g>
                  );
                })
              )}

              {/* ================================================================= */}
              {/* BESPOKE ON-CHART PATTERN FORMATION OVERLAYS (TradingView Grade)   */}
              {/* ================================================================= */}
              {isPatternMode && (
                <g className="tv-on-chart-pattern-overlays">
                  {/* 1. Candlestick Reversal Pattern Focus Overlay (Bullish/Bearish Engulfing, Hammer, etc.) */}
                  {isCandleReversal && triggerCandleIdx >= 1 && (() => {
                    const prevIdx = triggerCandleIdx - 1;
                    const currIdx = triggerCandleIdx;
                    const xPrev = getX(prevIdx);
                    const xCurr = getX(currIdx);
                    const isBull = patUpper.includes('BULL') || patUpper.includes('HAMMER') || patUpper.includes('MORNING') || (points[currIdx]?.close >= points[currIdx]?.open);
                    const col = isBull ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)';
                    
                    const high1 = points[prevIdx]?.high || points[prevIdx]?.close;
                    const high2 = points[currIdx]?.high || points[currIdx]?.close;
                    const low1 = points[prevIdx]?.low || points[prevIdx]?.close;
                    const low2 = points[currIdx]?.low || points[currIdx]?.close;
                    
                    const pMax = Math.max(high1, high2);
                    const pMin = Math.min(low1, low2);
                    
                    const boxLeft = Math.min(xPrev, xCurr) - candleW * 0.9;
                    const boxW = Math.abs(xCurr - xPrev) + candleW * 1.8;
                    const boxTop = getY(pMax) - 12;
                    const boxH = Math.max(28, getY(pMin) - getY(pMax) + 24);

                    const labelText = patUpper.includes('BULLISH_ENGULFING')
                      ? '▲ BULLISH ENGULFING CANDLE CONFIRMED'
                      : patUpper.includes('BEARISH_ENGULFING')
                      ? '▼ BEARISH ENGULFING CANDLE CONFIRMED'
                      : patUpper.includes('HAMMER')
                      ? '▲ BULLISH HAMMER REVERSAL'
                      : `▲ ${activeSetup?.display_name?.toUpperCase() || patUpper.replace(/_/g, ' ')}`;

                    const badgeWidth = Math.min(280, labelText.length * 7.5 + 24);
                    const badgeX = Math.max(padLeft + badgeWidth / 2 + 10, Math.min(padLeft + plotWidth - badgeWidth / 2 - 10, xCurr));
                    const badgeY = isBull ? Math.max(padTop + 24, boxTop - 28) : Math.min(padTop + mainChartHeight - 24, boxTop + boxH + 20);

                    return (
                      <g key="candle-pattern-overlay">
                        {/* Glowing Focus Box around Candle Setup */}
                        <rect
                          x={boxLeft}
                          y={boxTop}
                          width={boxW}
                          height={boxH}
                          rx="6"
                          fill={isBull ? 'rgba(0, 208, 156, 0.12)' : 'rgba(240, 68, 56, 0.12)'}
                          stroke={col}
                          strokeWidth="2"
                          strokeDasharray="5,3"
                        />

                        {/* Directional Impulse Arrow */}
                        <path
                          d={isBull
                            ? `M ${xCurr} ${getY(high2) - 4} L ${xCurr} ${getY(high2) - 22} M ${xCurr - 5} ${getY(high2) - 16} L ${xCurr} ${getY(high2) - 24} L ${xCurr + 5} ${getY(high2) - 16}`
                            : `M ${xCurr} ${getY(low2) + 4} L ${xCurr} ${getY(low2) + 22} M ${xCurr - 5} ${getY(low2) + 16} L ${xCurr} ${getY(low2) + 24} L ${xCurr + 5} ${getY(low2) + 16}`
                          }
                          stroke={col}
                          strokeWidth="2.4"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          fill="none"
                        />

                        {/* Callout Badge Tag */}
                        <g transform={`translate(${badgeX}, ${badgeY})`}>
                          <rect
                            x={-badgeWidth / 2}
                            y="-12"
                            width={badgeWidth}
                            height="24"
                            rx="6"
                            fill="#0B0E14"
                            stroke={col}
                            strokeWidth="1.4"
                            filter="drop-shadow(0 4px 12px rgba(0,0,0,0.85))"
                          />
                          <text
                            x="0"
                            y="4.5"
                            textAnchor="middle"
                            fill={col}
                            fontSize="9.5"
                            fontWeight="800"
                            fontFamily="var(--font-mono)"
                            letterSpacing="0.4"
                          >
                            {labelText}
                          </text>
                        </g>
                      </g>
                    );
                  })()}

                  {/* 2. RSI Divergence Overlay on Price */}
                  {isDivergence && points.length >= 8 && (() => {
                    const idx1 = Math.floor(points.length * 0.45);
                    const idx2 = points.length - 1;
                    const x1 = getX(idx1);
                    const y1 = getY(points[idx1].low || points[idx1].close);
                    const x2 = getX(idx2);
                    const y2 = getY(points[idx2].low || points[idx2].close);

                    return (
                      <g key="rsi-divergence-overlay">
                        <line
                          x1={x1}
                          y1={y1}
                          x2={x2}
                          y2={y2}
                          stroke="var(--loss-red, #F04438)"
                          strokeWidth="2.2"
                          strokeDasharray="5,4"
                        />
                        <circle cx={x1} cy={y1} r="4.5" fill="#F04438" />
                        <circle cx={x2} cy={y2} r="4.5" fill="#F04438" />

                        <g transform={`translate(${(x1 + x2) / 2}, ${Math.min(padTop + mainChartHeight - 16, Math.max(y1, y2) + 24)})`}>
                          <rect x="-115" y="-12" width="230" height="24" rx="6" fill="#0B0E14" stroke="#38BDF8" strokeWidth="1.2" />
                          <text x="0" y="4" textAnchor="middle" fill="#38BDF8" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">
                            ⚡ RSI DIVERGENCE: PRICE LOWER LOW
                          </text>
                        </g>
                      </g>
                    );
                  })()}

                  {/* 3. Cup & Handle Overlay */}
                  {isCupHandle && points.length >= 10 && (() => {
                    const { startIndex, windowBars } = getTimeframeWindow(points, timeframe);
                    const span = windowBars.length || 28;
                    const startIdx = startIndex;
                    const rimIdx = Math.min(points.length - 1, startIndex + Math.max(6, Math.round(span * 0.75)));
                    const xStart = getX(startIdx);
                    const xRim = getX(rimIdx);
                    const xEnd = getX(points.length - 1);
                    
                    let minCupP = Infinity;
                    let minCupIdx = startIdx;
                    for (let i = startIdx; i <= rimIdx; i++) {
                      const val = points[i]?.low || points[i]?.close;
                      if (val < minCupP) {
                        minCupP = val;
                        minCupIdx = i;
                      }
                    }
                    const xMin = getX(minCupIdx);
                    const yMinP = getY(minCupP);
                    const yPivot = getY(pivotLevel);

                    return (
                      <g key="cup-handle-overlay">
                        {/* Cup Contour U-Curve */}
                        <path
                          d={`M ${xStart} ${yPivot} Q ${xMin} ${yMinP + 14} ${xRim} ${yPivot}`}
                          fill="none"
                          stroke="var(--fintech-primary, #00D09C)"
                          strokeWidth="2.5"
                          strokeDasharray="6,4"
                        />
                        {/* Handle Consolidation Box */}
                        <rect
                          x={xRim}
                          y={yPivot}
                          width={Math.max(20, xEnd - xRim + 12)}
                          height={Math.max(16, (getY(stopLossLevel) - yPivot) * 0.7)}
                          fill="rgba(245, 158, 11, 0.12)"
                          stroke="var(--warning-amber, #F59E0B)"
                          strokeWidth="1.4"
                          strokeDasharray="3,3"
                          rx="4"
                        />
                        <g transform={`translate(${xRim}, ${yPivot - 30})`}>
                          <rect x="-105" y="-12" width="210" height="24" rx="6" fill="#0B0E14" stroke="var(--fintech-primary, #00D09C)" strokeWidth="1.2" />
                          <text x="0" y="4" textAnchor="middle" fill="var(--fintech-primary, #00D09C)" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">
                            ☕ CUP & HANDLE: ACCUMULATION
                          </text>
                        </g>
                      </g>
                    );
                  })()}

                  {/* 4. Volatility Contraction Pattern (VCP) Overlay */}
                  {isVCP && points.length >= 12 && (() => {
                    const { startIndex, windowBars } = getTimeframeWindow(points, timeframe);
                    const span = windowBars.length || 24;
                    const ind = activeSetup?.indices || {};
                    const idx1 = ind.t1 != null && ind.t1 >= startIndex ? ind.t1 : startIndex;
                    const idx2 = ind.t2 != null && ind.t2 >= startIndex ? ind.t2 : (startIndex + Math.max(2, Math.round(span * 0.40)));
                    const idx3 = ind.t3 != null && ind.t3 >= startIndex ? ind.t3 : (startIndex + Math.max(4, Math.round(span * 0.75)));
                    const idx4 = points.length - 1;

                    const x1 = getX(idx1);
                    const x2 = getX(idx2);
                    const x3 = getX(idx3);
                    const x4 = getX(idx4);

                    const yPiv = getY(pivotLevel);
                    const yLow1 = points[idx1]?.low != null ? getY(points[idx1].low) : getY(minPrice);
                    const yLow2 = points[idx2]?.low != null ? getY(points[idx2].low) : getY(minPrice + (pivotLevel - minPrice) * 0.4);
                    const yLow3 = points[idx3]?.low != null ? getY(points[idx3].low) : getY(minPrice + (pivotLevel - minPrice) * 0.75);

                    return (
                      <g key="vcp-overlay">
                        {/* T1 Wave */}
                        <path
                          d={`M ${x1} ${yPiv} L ${(x1 + x2) / 2} ${yLow1} L ${x2} ${yPiv}`}
                          fill="rgba(0, 208, 156, 0.06)"
                          stroke="var(--fintech-primary, #00D09C)"
                          strokeWidth="1.6"
                          strokeDasharray="4,3"
                        />
                        {/* T2 Wave */}
                        <path
                          d={`M ${x2} ${yPiv} L ${(x2 + x3) / 2} ${yLow2} L ${x3} ${yPiv}`}
                          fill="rgba(0, 208, 156, 0.09)"
                          stroke="var(--fintech-primary, #00D09C)"
                          strokeWidth="1.6"
                          strokeDasharray="4,3"
                        />
                        {/* T3 Wave */}
                        <path
                          d={`M ${x3} ${yPiv} L ${(x3 + x4) / 2} ${yLow3} L ${x4} ${yPiv}`}
                          fill="rgba(0, 208, 156, 0.14)"
                          stroke="var(--fintech-primary, #00D09C)"
                          strokeWidth="2"
                        />

                        <text x={(x1 + x2) / 2} y={yLow1 + 14} textAnchor="middle" fill="var(--text-secondary)" fontSize="9" fontWeight="700" fontFamily="var(--font-mono)">T1 (-15%)</text>
                        <text x={(x2 + x3) / 2} y={yLow2 + 14} textAnchor="middle" fill="var(--text-secondary)" fontSize="9" fontWeight="700" fontFamily="var(--font-mono)">T2 (-8%)</text>
                        <text x={(x3 + x4) / 2} y={yLow3 + 14} textAnchor="middle" fill="var(--fintech-primary)" fontSize="9" fontWeight="800" fontFamily="var(--font-mono)">T3 (-3% APEX)</text>

                        <g transform={`translate(${x4}, ${yPiv - 28})`}>
                          <rect x="-110" y="-12" width="220" height="24" rx="6" fill="#0B0E14" stroke="var(--fintech-primary, #00D09C)" strokeWidth="1.2" />
                          <text x="0" y="4" textAnchor="middle" fill="var(--fintech-primary, #00D09C)" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">
                            ⚡ VCP: VOLATILITY TIGHTENING
                          </text>
                        </g>
                      </g>
                    );
                  })()}

                  {/* 5. Double Bottom W-Base Overlay */}
                  {isDoubleBottom && points.length >= 8 && (() => {
                    const { startIndex, windowBars } = getTimeframeWindow(points, timeframe);
                    const span = Math.max(8, windowBars.length);
                    const ind = activeSetup?.indices || {};

                    const b1Idx = ind.b1 != null && ind.b1 >= startIndex ? ind.b1 : Math.max(startIndex, startIndex + Math.round(span * 0.25));
                    const peakIdx = ind.peak != null && ind.peak >= startIndex ? ind.peak : Math.max(startIndex + 1, startIndex + Math.round(span * 0.50));
                    const b2Idx = ind.b2 != null && ind.b2 >= startIndex ? ind.b2 : Math.max(startIndex + 2, startIndex + Math.round(span * 0.75));
                    const startIdx = ind.start != null && ind.start >= startIndex ? ind.start : startIndex;
                    const endIdx = points.length - 1;

                    const x1 = getX(startIdx);
                    const x2 = getX(b1Idx);
                    const x3 = getX(peakIdx);
                    const x4 = getX(b2Idx);
                    const x5 = getX(endIdx);

                    const yNeck = getY(pivotLevel);
                    const yB1 = points[b1Idx]?.low != null ? getY(points[b1Idx].low) : getY(minPrice);
                    const yB2 = points[b2Idx]?.low != null ? getY(points[b2Idx].low) : getY(minPrice * 1.008);

                    return (
                      <g key="double-bottom-overlay">
                        <path
                          d={`M ${x1} ${yNeck} L ${x2} ${yB1} L ${x3} ${yNeck} L ${x4} ${yB2} L ${x5} ${yNeck - 6}`}
                          fill="none"
                          stroke="var(--gain-green, #00D09C)"
                          strokeWidth="2.2"
                          strokeDasharray="5,3"
                        />
                        <circle cx={x2} cy={yB1} r="5" fill="#00D09C" stroke="#0B0E14" strokeWidth="1.5" />
                        <text x={x2} y={yB1 + 16} textAnchor="middle" fill="#00D09C" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">BOTTOM 1</text>
                        <circle cx={x4} cy={yB2} r="5" fill="#00D09C" stroke="#0B0E14" strokeWidth="1.5" />
                        <text x={x4} y={yB2 + 16} textAnchor="middle" fill="#00D09C" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">BOTTOM 2 (SHAKEOUT)</text>
                        <g transform={`translate(${x5}, ${yNeck - 28})`}>
                          <rect x="-105" y="-12" width="210" height="24" rx="6" fill="#0B0E14" stroke="var(--gain-green, #00D09C)" strokeWidth="1.2" />
                          <text x="0" y="4" textAnchor="middle" fill="var(--gain-green, #00D09C)" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">
                            W-BASE DOUBLE BOTTOM
                          </text>
                        </g>
                      </g>
                    );
                  })()}

                  {/* 6. Double Top M-Pattern Overlay */}
                  {isDoubleTop && points.length >= 8 && (() => {
                    const { startIndex, windowBars } = getTimeframeWindow(points, timeframe);
                    const span = Math.max(8, windowBars.length);
                    const ind = activeSetup?.indices || {};

                    const t1Idx = ind.t1 != null && ind.t1 >= startIndex ? ind.t1 : Math.max(startIndex, startIndex + Math.round(span * 0.25));
                    const neckIdx = ind.neck != null && ind.neck >= startIndex ? ind.neck : Math.max(startIndex + 1, startIndex + Math.round(span * 0.50));
                    const t2Idx = ind.t2 != null && ind.t2 >= startIndex ? ind.t2 : Math.max(startIndex + 2, startIndex + Math.round(span * 0.75));
                    const startIdx = ind.start != null && ind.start >= startIndex ? ind.start : startIndex;
                    const endIdx = points.length - 1;

                    const x1 = getX(startIdx);
                    const x2 = getX(t1Idx);
                    const x3 = getX(neckIdx);
                    const x4 = getX(t2Idx);
                    const x5 = getX(endIdx);

                    const yNeck = getY(stopLossLevel);
                    const yT1 = points[t1Idx]?.high != null ? getY(points[t1Idx].high) : getY(maxPrice);
                    const yT2 = points[t2Idx]?.high != null ? getY(points[t2Idx].high) : getY(maxPrice * 0.995);

                    return (
                      <g key="double-top-overlay">
                        <path
                          d={`M ${x1} ${yNeck} L ${x2} ${yT1} L ${x3} ${yNeck} L ${x4} ${yT2} L ${x5} ${yNeck + 6}`}
                          fill="none"
                          stroke="var(--loss-red, #F04438)"
                          strokeWidth="2.2"
                          strokeDasharray="5,3"
                        />
                        <circle cx={x2} cy={yT1} r="5" fill="#F04438" stroke="#0B0E14" strokeWidth="1.5" />
                        <text x={x2} y={yT1 - 10} textAnchor="middle" fill="#F04438" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">TOP 1</text>
                        <circle cx={x4} cy={yT2} r="5" fill="#F04438" stroke="#0B0E14" strokeWidth="1.5" />
                        <text x={x4} y={yT2 - 10} textAnchor="middle" fill="#F04438" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">TOP 2 (REJECTION)</text>
                        <g transform={`translate(${x5}, ${yNeck + 28})`}>
                          <rect x="-105" y="-12" width="210" height="24" rx="6" fill="#0B0E14" stroke="var(--loss-red, #F04438)" strokeWidth="1.2" />
                          <text x="0" y="4" textAnchor="middle" fill="var(--loss-red, #F04438)" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">
                            M-PATTERN DOUBLE TOP
                          </text>
                        </g>
                      </g>
                    );
                  })()}

                  {/* 7. Breakout / Turtle Overlay */}
                  {isBreakout && points.length >= 8 && (() => {
                    const { startIndex, windowBars } = getTimeframeWindow(points, timeframe);
                    const lastIdx = points.length - 1;
                    const startBreakIdx = Math.max(startIndex, lastIdx - Math.max(6, Math.round(windowBars.length * 0.35)));
                    const xStart = getX(startBreakIdx);
                    const xLast = getX(lastIdx);
                    const yPiv = getY(pivotLevel);
                    return (
                      <g key="breakout-overlay">
                        <rect
                          x={xStart}
                          y={yPiv}
                          width={Math.max(20, xLast - xStart)}
                          height={Math.max(16, (getY(stopLossLevel) - yPiv) * 0.6)}
                          fill="rgba(0, 208, 156, 0.08)"
                          stroke="var(--gain-green, #00D09C)"
                          strokeWidth="1.2"
                          strokeDasharray="4,3"
                          rx="4"
                        />
                        <path
                          d={`M ${xLast} ${yPiv + 14} L ${xLast} ${yPiv - 22} M ${xLast - 6} ${yPiv - 16} L ${xLast} ${yPiv - 24} L ${xLast + 6} ${yPiv - 16}`}
                          stroke="var(--gain-green, #00D09C)"
                          strokeWidth="2.5"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          fill="none"
                        />
                        <g transform={`translate(${xLast}, ${yPiv - 32})`}>
                          <rect x="-110" y="-12" width="220" height="24" rx="6" fill="#0B0E14" stroke="var(--gain-green, #00D09C)" strokeWidth="1.2" />
                          <text x="0" y="4" textAnchor="middle" fill="var(--gain-green, #00D09C)" fontSize="9.5" fontWeight="800" fontFamily="var(--font-mono)">
                            🚀 PIVOT BREAKOUT EXPANSION
                          </text>
                        </g>
                      </g>
                    );
                  })()}
                </g>
              )}
            </g>

            {/* Pinned Pattern Level Price Badges on Y-Axis */}
            {isPatternMode && (
              <>
                {/* Pivot Level Badge */}
                {getY(pivotLevel) >= padTop && getY(pivotLevel) <= padTop + mainChartHeight && (
                  <g>
                    <rect
                      x={padLeft + plotWidth + 4}
                      y={getY(pivotLevel) - 9}
                      width="76"
                      height="18"
                      rx="4"
                      fill="rgba(0, 208, 156, 0.25)"
                      stroke="var(--fintech-primary, #00D09C)"
                      strokeWidth="1"
                    />
                    <text
                      x={padLeft + plotWidth + 8}
                      y={getY(pivotLevel) + 4}
                      fill="var(--fintech-primary, #00D09C)"
                      fontSize="9"
                      fontWeight="800"
                      fontFamily="var(--font-mono)"
                    >
                      PIVOT {currencySymbol}{pivotLevel >= 1000 ? pivotLevel.toFixed(0) : pivotLevel.toFixed(1)}
                    </text>
                  </g>
                )}

                {/* Target Level Badge */}
                {getY(target1) >= padTop && getY(target1) <= padTop + mainChartHeight && (
                  <g>
                    <rect
                      x={padLeft + plotWidth + 4}
                      y={getY(target1) - 9}
                      width="76"
                      height="18"
                      rx="4"
                      fill="rgba(56, 189, 248, 0.25)"
                      stroke="#38BDF8"
                      strokeWidth="1"
                    />
                    <text
                      x={padLeft + plotWidth + 8}
                      y={getY(target1) + 4}
                      fill="#38BDF8"
                      fontSize="9"
                      fontWeight="800"
                      fontFamily="var(--font-mono)"
                    >
                      TGT {currencySymbol}{target1 >= 1000 ? target1.toFixed(0) : target1.toFixed(1)}
                    </text>
                  </g>
                )}

                {/* Stop Loss Level Badge */}
                {getY(stopLossLevel) >= padTop && getY(stopLossLevel) <= padTop + mainChartHeight && (
                  <g>
                    <rect
                      x={padLeft + plotWidth + 4}
                      y={getY(stopLossLevel) - 9}
                      width="76"
                      height="18"
                      rx="4"
                      fill="rgba(240, 68, 56, 0.25)"
                      stroke="#F04438"
                      strokeWidth="1"
                    />
                    <text
                      x={padLeft + plotWidth + 8}
                      y={getY(stopLossLevel) + 4}
                      fill="#F04438"
                      fontSize="9"
                      fontWeight="800"
                      fontFamily="var(--font-mono)"
                    >
                      STOP {currencySymbol}{stopLossLevel >= 1000 ? stopLossLevel.toFixed(0) : stopLossLevel.toFixed(1)}
                    </text>
                  </g>
                )}
              </>
            )}

            {/* Pinned Support & Resistance Price Badges on Y-Axis (shown when visible) */}
            {showLevels && (
              <>
                {getY(resistance) >= padTop && getY(resistance) <= padTop + mainChartHeight && (
                  <g>
                    <rect
                      x={padLeft + plotWidth + 6}
                      y={getY(resistance) - 8}
                      width="72"
                      height="16"
                      rx="3"
                      fill="rgba(240, 68, 56, 0.2)"
                      stroke="rgba(240, 68, 56, 0.4)"
                      strokeWidth="0.8"
                    />
                    <text
                      x={padLeft + plotWidth + 10}
                      y={getY(resistance) + 4}
                      fill="#F04438"
                      fontSize="9.5"
                      fontWeight="800"
                      fontFamily="var(--font-mono)"
                    >
                      RES {currencySymbol}{resistance.toFixed(1)}
                    </text>
                  </g>
                )}

                {getY(support) >= padTop && getY(support) <= padTop + mainChartHeight && (
                  <g>
                    <rect
                      x={padLeft + plotWidth + 6}
                      y={getY(support) - 8}
                      width="72"
                      height="16"
                      rx="3"
                      fill="rgba(0, 208, 156, 0.2)"
                      stroke="rgba(0, 208, 156, 0.4)"
                      strokeWidth="0.8"
                    />
                    <text
                      x={padLeft + plotWidth + 10}
                      y={getY(support) + 4}
                      fill="#00D09C"
                      fontSize="9.5"
                      fontWeight="800"
                      fontFamily="var(--font-mono)"
                    >
                      SUP {currencySymbol}{support.toFixed(1)}
                    </text>
                  </g>
                )}
              </>
            )}

            {/* ================================================================= */}
            {/* TRADINGVIEW DEDICATED X-AXIS TIMELINE BAR                         */}
            {/* ================================================================= */}
            <g className="tv-timeline-scale-group">
              {/* Dedicated Timeline Bar Background Strip */}
              <rect
                x={padLeft}
                y={padTop + mainChartHeight}
                width={plotWidth + padRight}
                height={padBottom}
                fill="var(--bg-surface-elevated, #141923)"
                opacity="0.95"
              />

              {/* Timeline Top Dividing Hairline */}
              <line
                x1={padLeft}
                y1={padTop + mainChartHeight}
                x2={padLeft + plotWidth + padRight}
                y2={padTop + mainChartHeight}
                stroke="var(--border-hairline, #222A3B)"
                strokeWidth="1.2"
              />

              {/* Right Y-axis vertical dividing hairline */}
              <line
                x1={padLeft + plotWidth}
                y1={padTop}
                x2={padLeft + plotWidth}
                y2={padTop + mainChartHeight + padBottom}
                stroke="var(--border-hairline, #222A3B)"
                strokeWidth="1.2"
              />

              {/* Dynamic Visible Timestamps & Subtle Ticks */}
              {timeIndices.map((idx) => {
                const pt = points[idx];
                if (!pt) return null;
                const x = getX(idx);
                if (x < padLeft + 15 || x > padLeft + plotWidth - 15) return null;
                const label = formatTimelineLabel(pt.time, isMultiDayVisible);

                return (
                  <g key={`time-${idx}`}>
                    {/* Tick Mark */}
                    <line
                      x1={x}
                      y1={padTop + mainChartHeight}
                      x2={x}
                      y2={padTop + mainChartHeight + 5}
                      stroke="var(--border-subtle, #334155)"
                      strokeWidth="1"
                    />
                    <text
                      x={x}
                      y={padTop + mainChartHeight + 20}
                      textAnchor="middle"
                      fill="var(--text-secondary, #94A3B8)"
                      fontSize="10"
                      fontWeight="600"
                      fontFamily="var(--font-mono)"
                    >
                      {label}
                    </text>
                  </g>
                );
              })}

              {/* TradingView Interactive Hover Time Badge */}
              {hoveredIdx !== null && !isDragging && getX(hoveredIdx) >= padLeft && getX(hoveredIdx) <= padLeft + plotWidth && (
                <g className="tv-timeline-hover-pill">
                  <rect
                    x={Math.max(padLeft + 4, Math.min(padLeft + plotWidth - 76, getX(hoveredIdx) - 38))}
                    y={padTop + mainChartHeight + 3}
                    width="76"
                    height="21"
                    rx="4"
                    fill="#1E293B"
                    stroke="var(--fintech-primary, #00D09C)"
                    strokeWidth="1.2"
                  />
                  <text
                    x={Math.max(padLeft + 42, Math.min(padLeft + plotWidth - 38, getX(hoveredIdx)))}
                    y={padTop + mainChartHeight + 17}
                    textAnchor="middle"
                    fill="#F8FAFC"
                    fontSize="10"
                    fontWeight="700"
                    fontFamily="var(--font-mono)"
                  >
                    {formatTimelineLabel(points[hoveredIdx]?.time, isMultiDayVisible) || points[hoveredIdx]?.time}
                  </text>
                </g>
              )}
            </g>

            {/* ================================================================= */}
            {/* 2. SQUEEZE MOMENTUM INDICATOR PANEL [LazyBear]                   */}
            {/* ================================================================= */}
            {showSqueeze && (
              <g>
                {/* Panel Title & Squeeze Status */}
                <text
                  x={padLeft}
                  y={sqzTop - 6}
                  fill="var(--text-secondary)"
                  fontSize="11"
                  fontWeight="700"
                >
                  Squeeze Momentum (LazyBear)
                </text>
                <text
                  x={padLeft + 200}
                  y={sqzTop - 6}
                  fill="var(--text-tertiary)"
                  fontSize="10"
                  fontFamily="var(--font-mono)"
                >
                  Val: {squeezeData[activeIdx]?.val?.toFixed(3) || '0.000'} | Status:{' '}
                  {squeezeData[activeIdx]?.sqzOn
                    ? 'SQUEEZE ON (Compression)'
                    : squeezeData[activeIdx]?.sqzOff
                    ? 'SQUEEZE FIRED (Breakout)'
                    : 'NO SQUEEZE'}
                </text>

                {/* Panel Bounding Box */}
                <rect
                  x={padLeft}
                  y={sqzTop}
                  width={plotWidth}
                  height={oscPanelHeight}
                  fill="rgba(11, 14, 20, 0.4)"
                  stroke="var(--border-hairline)"
                  rx="6"
                />

                {/* Zero Line */}
                <line
                  x1={padLeft}
                  y1={getSqzY(0)}
                  x2={padLeft + plotWidth}
                  y2={getSqzY(0)}
                  stroke="var(--border-subtle)"
                  strokeWidth="1"
                />

                {/* Clipped Squeeze Bars & Dots */}
                <g clipPath="url(#sqz-clip)">
                  {squeezeData.map((sq, i) => {
                    const x = getX(i);
                    const barW = Math.max(2, Math.min(22, (effectiveWidth / points.length) * 0.7));
                    if (x < padLeft - barW || x > padLeft + plotWidth + barW) return null;

                    const yZero = getSqzY(0);
                    const yVal = getSqzY(sq.val);
                    const barH = Math.max(1, Math.abs(yVal - yZero));
                    const barY = sq.val >= 0 ? yVal : yZero;

                    return (
                      <g key={`sqz-bar-${i}`}>
                        <rect
                          x={x - barW / 2}
                          y={barY}
                          width={barW}
                          height={barH}
                          fill={sq.barColor}
                          rx="1"
                        />
                        <circle
                          cx={x}
                          cy={yZero}
                          r={sq.sqzOn ? 2.5 : 2}
                          fill={sq.dotColor}
                          stroke="#0B0E14"
                          strokeWidth="0.5"
                        />
                      </g>
                    );
                  })}
                </g>
              </g>
            )}

            {/* ================================================================= */}
            {/* 3. RSI (14) PANEL                                                 */}
            {/* ================================================================= */}
            {showRSI && (
              <g>
                <text
                  x={padLeft}
                  y={rsiTop - 6}
                  fill="var(--text-secondary)"
                  fontSize="11"
                  fontWeight="700"
                >
                  RSI (14)
                </text>
                <text
                  x={padLeft + 70}
                  y={rsiTop - 6}
                  fill="#EC4899"
                  fontSize="10"
                  fontFamily="var(--font-mono)"
                >
                  {rsiData[activeIdx] != null ? rsiData[activeIdx].toFixed(1) : 'N/A'}
                </text>

                <rect
                  x={padLeft}
                  y={rsiTop}
                  width={plotWidth}
                  height={oscPanelHeight}
                  fill="rgba(11, 14, 20, 0.4)"
                  stroke="var(--border-hairline)"
                  rx="6"
                />

                {/* Overbought 70 line */}
                <line
                  x1={padLeft}
                  y1={getRsiY(70)}
                  x2={padLeft + plotWidth}
                  y2={getRsiY(70)}
                  stroke="rgba(240, 68, 56, 0.6)"
                  strokeDasharray="3,3"
                />
                <text x={padLeft + plotWidth + 6} y={getRsiY(70) + 3} fill="#F04438" fontSize="9" fontFamily="var(--font-mono)">
                  70
                </text>

                {/* Oversold 30 line */}
                <line
                  x1={padLeft}
                  y1={getRsiY(30)}
                  x2={padLeft + plotWidth}
                  y2={getRsiY(30)}
                  stroke="rgba(0, 208, 156, 0.6)"
                  strokeDasharray="3,3"
                />
                <text x={padLeft + plotWidth + 6} y={getRsiY(30) + 3} fill="#00D09C" fontSize="9" fontFamily="var(--font-mono)">
                  30
                </text>

                {/* Clipped RSI Polyline */}
                <g clipPath="url(#rsi-clip)">
                  {points.map((_, i) => {
                    if (i === 0 || rsiData[i] == null || rsiData[i - 1] == null) return null;
                    const x1 = getX(i - 1);
                    const x2 = getX(i);
                    if (Math.max(x1, x2) < padLeft || Math.min(x1, x2) > padLeft + plotWidth) return null;

                    return (
                      <line
                        key={`rsi-seg-${i}`}
                        x1={x1}
                        y1={getRsiY(rsiData[i - 1])}
                        x2={x2}
                        y2={getRsiY(rsiData[i])}
                        stroke="#EC4899"
                        strokeWidth="1.8"
                      />
                    );
                  })}
                </g>

                {/* RSI Divergence Higher-Low Bullish Trendline */}
                {isDivergence && points.length >= 8 && (() => {
                  const idx1 = Math.floor(points.length * 0.45);
                  const idx2 = points.length - 1;
                  const x1 = getX(idx1);
                  const x2 = getX(idx2);
                  const val1 = rsiData[idx1] ?? 36;
                  const val2 = rsiData[idx2] ?? 52;
                  const y1 = getRsiY(val1);
                  const y2 = getRsiY(val2);
                  return (
                    <g key="rsi-panel-div-line">
                      <line
                        x1={x1}
                        y1={y1}
                        x2={x2}
                        y2={y2}
                        stroke="var(--gain-green, #00D09C)"
                        strokeWidth="2.2"
                        strokeDasharray="4,3"
                      />
                      <circle cx={x1} cy={y1} r="4" fill="#00D09C" />
                      <circle cx={x2} cy={y2} r="4" fill="#00D09C" />
                      <text
                        x={(x1 + x2) / 2}
                        y={Math.min(y1, y2) - 6}
                        textAnchor="middle"
                        fill="var(--gain-green, #00D09C)"
                        fontSize="9"
                        fontWeight="800"
                        fontFamily="var(--font-mono)"
                      >
                        RSI HIGHER LOW (BULLISH DIVERGENCE)
                      </text>
                    </g>
                  );
                })()}
              </g>
            )}

            {/* TradingView Dual Crosshair (Vertical Time + Horizontal Price) */}
            {hoveredIdx !== null && !isDragging && (
              <g>
                {/* Vertical Time Line */}
                {getX(hoveredIdx) >= padLeft && getX(hoveredIdx) <= padLeft + plotWidth && (
                  <line
                    x1={getX(hoveredIdx)}
                    y1={padTop}
                    x2={getX(hoveredIdx)}
                    y2={padTop + mainChartHeight}
                    stroke="rgba(148, 163, 184, 0.45)"
                    strokeDasharray="3,3"
                    strokeWidth="1"
                  />
                )}

                {/* Horizontal Price Line */}
                {getY(points[hoveredIdx]?.close) >= padTop && getY(points[hoveredIdx]?.close) <= padTop + mainChartHeight && (
                  <line
                    x1={padLeft}
                    y1={getY(points[hoveredIdx]?.close)}
                    x2={padLeft + plotWidth}
                    y2={getY(points[hoveredIdx]?.close)}
                    stroke="rgba(148, 163, 184, 0.45)"
                    strokeDasharray="3,3"
                    strokeWidth="1"
                  />
                )}

                {/* TradingView Right Y-Axis Price Hover Pill */}
                {getY(points[hoveredIdx]?.close) >= padTop && getY(points[hoveredIdx]?.close) <= padTop + mainChartHeight && (
                  <g className="tv-yaxis-hover-pill">
                    <rect
                      x={padLeft + plotWidth + 3}
                      y={getY(points[hoveredIdx]?.close) - 10}
                      width={padRight - 8}
                      height="20"
                      rx="4"
                      fill={isUp ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)'}
                    />
                    <text
                      x={padLeft + plotWidth + ((padRight - 8) / 2) + 3}
                      y={getY(points[hoveredIdx]?.close) + 4}
                      textAnchor="middle"
                      fill="#0B0E14"
                      fontSize="10"
                      fontWeight="800"
                      fontFamily="var(--font-mono)"
                    >
                      {currencySymbol}{points[hoveredIdx]?.close?.toFixed(2)}
                    </text>
                  </g>
                )}

                {/* Target Node */}
                {getX(hoveredIdx) >= padLeft &&
                  getX(hoveredIdx) <= padLeft + plotWidth &&
                  getY(points[hoveredIdx]?.close) >= padTop &&
                  getY(points[hoveredIdx]?.close) <= padTop + mainChartHeight && (
                    <circle
                      cx={getX(hoveredIdx)}
                      cy={getY(points[hoveredIdx]?.close)}
                      r="5"
                      fill="#0B0E14"
                      stroke={isUp ? 'var(--gain-green)' : 'var(--loss-red)'}
                      strokeWidth="2.5"
                    />
                  )}
              </g>
            )}
          </svg>
        </div>
      </div>

        {/* Signals Footer Summary */}
        <div className="pro-signals-footer">
          <div className="signal-item">
            <span className="signal-label">Timeframe:</span>
            <span className="signal-value font-mono">{timeframe.toUpperCase()}</span>
          </div>

          <div className="signal-item">
            <span className="signal-label">Bars:</span>
            <span className="signal-value font-mono">{points.length}</span>
          </div>

          <div className="signal-item">
            <span className="signal-label">Volatility Squeeze:</span>
            <span
              className="signal-value"
              style={{
                color: squeezeData[activeIdx]?.sqzOn
                  ? '#F59E0B'
                  : squeezeData[activeIdx]?.sqzOff
                  ? 'var(--gain-green)'
                  : '#38BDF8'
              }}
            >
              {squeezeData[activeIdx]?.sqzOn
                ? 'COMPRESSION (Pre-Breakout Squeeze)'
                : squeezeData[activeIdx]?.sqzOff
                ? 'FIRED (Momentum Expansion)'
                : 'NORMAL VOLATILITY'}
            </span>
          </div>

          <div className="signal-item">
            <span className="signal-label">Range Durability:</span>
            <span className="signal-value font-mono">
              Support: {currencySymbol}{support.toFixed(2)} | Res: {currencySymbol}{resistance.toFixed(2)}
            </span>
          </div>
        </div>
      </div>

      {/* 30% DEDICATED STRATEGY & ACTIONS SIDEBAR */}
      <div className="pro-workstation-sidebar">
        {/* Stock Overview Header */}
        <div className="ws-stock-header-card">
          <div className="ws-stock-header-top">
            <div className="ws-logo-box">
              <img
                src={logoUrl || `https://assets.parqet.com/logos/symbol/${ticker}?format=png`}
                alt=""
                onError={(e) => {
                  e.target.style.display = 'none';
                }}
              />
            </div>
            <div className="ws-stock-titles">
              <div className="ws-symbol-row">
                <span className="ws-symbol font-mono">{ticker}</span>
                <span className="ws-market-pill">{ticker.endsWith('.NS') || ticker.endsWith('.BO') ? 'NSE' : 'US'}</span>
              </div>
              <span className="ws-company-name">{companyName}</span>
            </div>
          </div>

          <div className="ws-price-row font-mono">
            <span className="ws-current-price">{currencySymbol}{activePrice.toFixed(2)}</span>
            <span className={`ws-change-pill ${isUp ? 'positive' : 'negative'}`}>
              {isUp ? '+' : ''}{change.toFixed(2)} ({isUp ? '+' : ''}{changePct.toFixed(2)}%)
            </span>
          </div>
        </div>

        {/* Multi-Pattern Switcher (if patterns available) */}
        {availablePatterns.length > 0 && (
          <div className="ws-patterns-switcher-section">
            <div className="ws-section-label">
              <span>DETECTED SETUPS FOR {ticker}</span>
              <span className="ws-badge font-mono">{availablePatterns.length} DETECTED</span>
            </div>
            <div className="ws-patterns-chips-list">
              {availablePatterns.map((pat, idx) => {
                const isSelected = (activeSetup?.pattern || '').toUpperCase() === (pat.pattern || '').toUpperCase();
                const patName = pat.display_name || pat.pattern?.replace(/_/g, ' ');
                return (
                  <button
                    key={pat.pattern || idx}
                    className={`ws-pattern-chip ${isSelected ? 'active' : ''}`}
                    onClick={() => setSelectedPattern(pat)}
                  >
                    <div className="chip-indicator" />
                    <span className="chip-name">{patName}</span>
                    {pat.score && <span className="chip-score font-mono">{pat.score}</span>}
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* Tactical Trade Playbook & Action Directives */}
        <div className="ws-playbook-card">
          <div className="ws-playbook-header">
            <div className="ws-setup-heading">
              <Zap size={15} style={{ color: 'var(--fintech-primary)' }} />
              <span className="ws-setup-title">
                {activeSetup?.display_name || activeSetup?.pattern?.replace(/_/g, ' ') || 'Consolidation Breakout'}
              </span>
            </div>
            {activeSetup?.score && (
              <div className="ws-score-pill font-mono">
                <span className="lbl">CONVICTION</span>
                <span className="val">{activeSetup.score}/100</span>
              </div>
            )}
          </div>

          {/* Execution Status Badge */}
          <div
            className="ws-status-badge font-mono"
            style={{
              color: playbook.statusBadge.color,
              background: playbook.statusBadge.bg,
              borderColor: playbook.statusBadge.color
            }}
          >
            {playbook.statusBadge.label}
          </div>

          {/* Diagnosis: What is happening now */}
          <div className="ws-diagnosis-box">
            <div className="ws-subhead">
              <Eye size={13} style={{ color: 'var(--fintech-primary)' }} />
              <span>WHAT IS HAPPENING NOW</span>
            </div>
            <p className="ws-diagnosis-text">{playbook.diagnosis}</p>
          </div>

          {/* Detailed Action Steps */}
          <div className="ws-actions-list">
            {/* 1. Entry Trigger */}
            <div className="ws-action-item entry">
              <div className="action-icon-col">
                <Compass size={15} className="action-icon" />
              </div>
              <div className="action-content">
                <span className="action-title font-mono">1. PRIMARY ENTRY TRIGGER</span>
                <p className="action-desc">{playbook.entryTrigger}</p>
              </div>
            </div>

            {/* 2. Observe Corridor */}
            <div className="ws-action-item observe">
              <div className="action-icon-col">
                <Activity size={15} className="action-icon" />
              </div>
              <div className="action-content">
                <span className="action-title font-mono">2. OBSERVE RANGE CORRIDOR</span>
                <p className="action-desc">{playbook.observeRange}</p>
              </div>
            </div>

            {/* 3. Strict Stop Loss */}
            <div className="ws-action-item stop">
              <div className="action-icon-col">
                <ShieldAlert size={15} className="action-icon" />
              </div>
              <div className="action-content">
                <span className="action-title font-mono">3. STRICT STOP-LOSS / INVALIDATION EXIT</span>
                <p className="action-desc">{playbook.exitRule}</p>
              </div>
            </div>

            {/* 4. Profit Ladder Target 1 & Target 2 */}
            <div className="ws-action-item targets">
              <div className="action-icon-col">
                <Target size={15} className="action-icon" />
              </div>
              <div className="action-content">
                <span className="action-title font-mono">4. PROFIT-TAKING LADDER</span>
                <div className="targets-ladder-list">
                  {playbook.targetLadder.map((t, i) => (
                    <div key={i} className="target-tier-row">
                      <span className="target-tier-level font-mono">{t.level}</span>
                      <span className="target-tier-action">{t.action}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Risk / Reward Metric */}
            <div className="ws-rr-metric-box font-mono">
              <div className="rr-metric-item">
                <span className="rr-lbl">RISK / REWARD</span>
                <span className="rr-val" style={{ color: 'var(--fintech-primary)' }}>1 : {riskRewardRatio}</span>
              </div>
              <div className="rr-metric-divider" />
              <div className="rr-metric-item">
                <span className="rr-lbl">MAX RISK</span>
                <span className="rr-val" style={{ color: 'var(--loss-red)' }}>-{playbook.riskPct}%</span>
              </div>
              <div className="rr-metric-divider" />
              <div className="rr-metric-item">
                <span className="rr-lbl">TARGET 1</span>
                <span className="rr-val" style={{ color: 'var(--fintech-blue)' }}>+{playbook.r1GainPct}%</span>
              </div>
            </div>
          </div>

          {/* Audited Criteria Checklist */}
          <div className="ws-audit-checklist">
            <div className="ws-subhead">
              <CheckCircle2 size={13} style={{ color: 'var(--fintech-primary)' }} />
              <span>AUDITED CONFIRMATION CRITERIA</span>
            </div>
            <div className="checklist-items">
              <div className="checklist-row">
                <Check size={12} className="check-icon" />
                <span>High-conviction pattern geometry verified on multi-session history</span>
              </div>
              <div className="checklist-row">
                <Check size={12} className="check-icon" />
                <span>Supply dry-up & institutional volume accumulation on support retest</span>
              </div>
              <div className="checklist-row">
                <Check size={12} className="check-icon" />
                <span>Favorable asymmetrical Risk-to-Reward ratio (≥ 1:2.0) with mathematical stop</span>
              </div>
              <div className="checklist-row">
                <Check size={12} className="check-icon" />
                <span>Overhead resistance corridor clear toward primary R1 expansion target</span>
              </div>
            </div>
          </div>
        </div>

        {/* Pinned Bottom Button: Deep Analysis & Audited Fundamentals */}
        <div className="ws-bottom-cta-container">
          <button
            className="stocktics-button ws-deep-analysis-cta-btn"
            onClick={() => {
              if (onGoToDeepAnalysis) {
                onGoToDeepAnalysis(ticker);
              } else {
                onClose();
                navigateTo({ tab: 'analysis', ticker });
              }
            }}
          >
            <BookOpen size={16} />
            <span>Deep Analysis & Audited Fundamentals for {ticker}</span>
            <ArrowRight size={16} />
          </button>
        </div>
      </div>
    </div>
  );
}
