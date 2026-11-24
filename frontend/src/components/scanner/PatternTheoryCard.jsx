import React from 'react';
import { BookOpen, ArrowUpRight, Sparkles, ShieldCheck } from 'lucide-react';
import PatternCanvasDiagram from './PatternCanvasDiagram';

// Theory definitions and diagrams modeled after institutional Technical Analysis Toolbox
export const PATTERN_THEORY_MAP = {
  BREAKOUT: {
    number: '02',
    name: 'Breakout Pattern',
    subtitle: 'Price Action & Volume Expansion',
    rule: 'Price closes decisively above established multi-week resistance accompanied by surge volume (>1.5x 50-day SMA).',
    trigger: 'Enter on confirmed daily close above resistance with stop-loss below the breakout pivot level.',
    type: 'breakout'
  },
  PIVOT_BREAKOUT: {
    number: '02',
    name: 'Pivot Breakout',
    subtitle: 'Price Action & Volume Expansion',
    rule: 'Price breaks through critical horizontal pivot resistance where sellers were previously exhausted.',
    trigger: 'Enter on daily close above pivot level. Invalidation below pivot support.',
    type: 'breakout'
  },
  FAILED_BREAKOUT: {
    number: '02B',
    name: 'Failed Breakout (Bull Trap)',
    subtitle: 'Liquidity Grab & Reversal',
    rule: 'Price initially pierces resistance to trigger buy stops, but swiftly collapses back into the prior trading range.',
    trigger: 'Bearish reversal indicator; institutions absorb breakout liquidity and reject higher prices.',
    type: 'breakout'
  },
  CUP_AND_HANDLE: {
    number: '03',
    name: 'Cup with Handle',
    subtitle: "William O'Neil Institutional Base",
    rule: 'Rounded U-shaped accumulation base (12-33% depth) followed by a tight downward handle on dry volume.',
    trigger: 'Buy point is 10 cents above the handle peak. Volume must expand by 40%+ over 50-day average.',
    type: 'cup_handle'
  },
  CUP_WITH_HANDLE: {
    number: '03',
    name: 'Cup with Handle',
    subtitle: "William O'Neil Institutional Base",
    rule: 'Rounded U-shaped accumulation base (12-33% depth) followed by a tight downward handle on dry volume.',
    trigger: 'Buy point is 10 cents above the handle peak. Volume must expand by 40%+ over 50-day average.',
    type: 'cup_handle'
  },
  DOUBLE_BOTTOM: {
    number: '03B',
    name: 'Double Bottom (W-Base)',
    subtitle: 'Bullish Reversal Structure',
    rule: 'Classic "W" shape where 2nd trough undercuts or matches 1st trough to shake out weak hands before neckline breakout.',
    trigger: 'Trigger on confirmed close above middle pivot neckline. Target measured by base height.',
    type: 'double_bottom'
  },
  DOUBLE_TOP: {
    number: '03C',
    name: 'Double Top (M-Pattern)',
    subtitle: 'Bearish Reversal Structure',
    rule: 'Classic "M" formation where price twice tests overhead resistance and fails to make higher high.',
    trigger: 'Breakdown trigger below neckline confirmation point.',
    type: 'double_top'
  },
  VCP: {
    number: '04',
    name: 'Volatility Contraction Pattern',
    subtitle: 'Mark Minervini Institutional Accumulation',
    rule: 'Successive price swings contract from left to right (e.g. 25% → 12% → 5%) with volume progressively drying up.',
    trigger: 'Pivot breakout occurs at the apex of the tightest rightmost contraction with volume surge.',
    type: 'vcp'
  },
  FLAT_BASE: {
    number: '04B',
    name: 'Flat Base',
    subtitle: 'Mid-Trend Consolidation',
    rule: 'Horizontal price digestion with maximum depth under 15% occurring after an initial 20%+ upward thrust.',
    trigger: 'Enter on expansion above the flat base resistance ceiling.',
    type: 'flat_base'
  },
  MULTI_YEAR_BASE: {
    number: '04C',
    name: 'Multi-Year Base',
    subtitle: 'Macro Structural Breakout',
    rule: 'Extended multi-year institutional accumulation (1 to 5+ years) breaking out into fresh all-time or decade highs.',
    trigger: 'Exceptional structural breakout; long-term supply overhang is completely absorbed.',
    type: 'multi_year'
  },
  IPO_BASE: {
    number: '04D',
    name: 'IPO Base',
    subtitle: 'Early Institutional Discovery',
    rule: 'First consolidation base formed within 25 to 65 days of public listing before initial supply overhang clears.',
    trigger: 'Buy on breakout above the IPO high with strong volume expansion.',
    type: 'breakout'
  },
  FAIR_VALUE_GAP: {
    number: '05',
    name: 'Fair Value Gap (FVG)',
    subtitle: 'ICT & Smart Money Concepts',
    rule: 'Three-candle imbalance where candle 1 high and candle 3 low do not overlap, leaving an unfilled liquidity void.',
    trigger: 'Price returns to mitigate the FVG zone, providing institutional low-risk entry.',
    type: 'fvg'
  },
  BULLISH_ENGULFING: {
    number: '06',
    name: 'Bullish Engulfing',
    subtitle: 'Candlestick Reversal Pattern',
    rule: 'Large green candle body completely engulfs the previous red candle body following a downtrend or pullback.',
    trigger: 'Signal of aggressive buyer dominance. Entry on candle close with stop below candle low.',
    type: 'bullish_engulfing'
  },
  BEARISH_ENGULFING: {
    number: '06B',
    name: 'Bearish Engulfing',
    subtitle: 'Candlestick Distribution Pattern',
    rule: 'Large red candle body completely eclipses the prior green candle body near key resistance.',
    trigger: 'Bearish momentum trigger; sellers overwhelm bids across the entire preceding session range.',
    type: 'bearish_engulfing'
  },
  HAMMER: {
    number: '06C',
    name: 'Hammer Candlestick',
    subtitle: 'Intraday Rejection of Lows',
    rule: 'Small real body near the top of the range with a long lower wick at least twice the length of the body.',
    trigger: 'Bears tested lower prices but institutional buying stepped in to drive price back to the highs.',
    type: 'hammer'
  },
  SHOOTING_STAR: {
    number: '06D',
    name: 'Shooting Star',
    subtitle: 'Overhead Supply Rejection',
    rule: 'Small body near the lower range with a long upper wick at least 2x body height occurring at peak resistance.',
    trigger: 'Buyers attempted to push higher but aggressive supply forced prices back down.',
    type: 'shooting_star'
  },
  MORNING_STAR: {
    number: '06E',
    name: 'Morning Star',
    subtitle: '3-Candle Bottom Reversal',
    rule: 'Tall bearish candle, followed by an indecision star/doji, followed by a strong bullish candle closing >50% of candle 1.',
    trigger: 'Institutional reversal pattern signaling end of downward impulse.',
    type: 'morning_star'
  },
  EVENING_STAR: {
    number: '06F',
    name: 'Evening Star',
    subtitle: '3-Candle Top Reversal',
    rule: 'Tall bullish candle, followed by indecision star gap, followed by a dark candle closing deep in candle 1 body.',
    trigger: 'Exhaustion of upward trend and shift to distribution.',
    type: 'morning_star'
  },
  DOJI: {
    number: '06G',
    name: 'Doji Candlestick',
    subtitle: 'Equilibrium & Inflection',
    rule: 'Open and close prices are virtually identical with upper and lower shadows, marking absolute equilibrium.',
    trigger: 'Sign of impending directional breakout when confirmed by next session candle.',
    type: 'hammer'
  },
  SUPPORT_RESISTANCE_TOUCH: {
    number: '11',
    name: 'Support & Resistance',
    subtitle: 'Key Price Reaction Levels',
    rule: 'Identifies horizontal price shelves where institutional supply or demand repeatedly reverses price trajectory.',
    trigger: 'Enter in direction of bounce from validated support with tight stop-loss.',
    type: 'sr_touch'
  },
  TRENDLINE_BREAK: {
    number: '13',
    name: 'Trend Line Breakout',
    subtitle: 'Dynamic Structural Shift',
    rule: 'Decisive breach of descending resistance line connecting at least 3 distinct swing highs.',
    trigger: 'Confirms shift from corrective downtrend into newly developing upward momentum wave.',
    type: 'trendline'
  },
  MA_CROSSOVER: {
    number: '16',
    name: 'Moving Average Crossover',
    subtitle: 'Trend Trend-Following Signal',
    rule: 'Faster moving average (e.g. 20 EMA or 50 SMA) crosses decisively above slower baseline (50 EMA or 200 SMA).',
    trigger: 'Golden Cross confirms broad institutional regime change toward sustained bull trend.',
    type: 'ma_cross'
  },
  SUPERTREND_FLIP: {
    number: '17',
    name: 'Supertrend Flip',
    subtitle: 'Volatility Trend Filter',
    rule: 'Price closes above ATR-based trailing stop band, flipping Supertrend indicator from Red (Bear) to Green (Bull).',
    trigger: 'Systematic trend-following entry; trail stop along the green lower band.',
    type: 'supertrend'
  },
  RSI_EXTREME: {
    number: '18',
    name: 'RSI Mean Reversion',
    subtitle: 'Overbought / Oversold Oscillators',
    rule: '14-period RSI enters extreme oversold zone (<30) or extreme momentum zone (>70) signaling inflection.',
    trigger: 'Look for curl back above 30 or bullish divergence to initiate high-probability bounce.',
    type: 'rsi'
  },
  STOCHASTIC_CROSS: {
    number: '19',
    name: 'Stochastic Oscillator Cross',
    subtitle: 'Cyclical Momentum Turn',
    rule: '%K line crosses above %D line from below 20 oversold threshold while price establishes local support.',
    trigger: 'Early confirmation of momentum inflection.',
    type: 'rsi'
  },
  MOMENTUM_ROC: {
    number: '15',
    name: 'Momentum & Rate of Change',
    subtitle: 'Velocity Indicator',
    rule: 'Surge in Rate of Change (ROC) indicates acceleration of price velocity compared to recent baseline.',
    trigger: 'Momentum thrust confirms high probability of continuation into the next swing.',
    type: 'rsi'
  },
  DIVERGENCE: {
    number: '20',
    name: 'RSI Bullish Divergence',
    subtitle: 'Oscillator Structural Divergence',
    rule: 'Price prints a lower swing low while the RSI indicator forms a higher swing low, signaling weakening sell pressure.',
    trigger: 'Classic smart money accumulation signal preceding explosive trend reversals.',
    type: 'divergence'
  },
  RELATIVE_VOLUME_SPIKE: {
    number: '21',
    name: 'Volume Surge (RVOL)',
    subtitle: 'Institutional Footprint',
    rule: 'Daily trading volume exceeds 2.5x the 50-day average, signaling institutional capital deployment.',
    trigger: 'High volume accompanied by green candle confirms institutional accumulation.',
    type: 'volume'
  },
  VOLUME_PROFILE_POC_REACTION: {
    number: '23',
    name: 'Volume Profile POC',
    subtitle: 'Point of Control Value Area',
    rule: 'Price interacts with the Point of Control (POC)—the single price level with the greatest historical volume traded.',
    trigger: 'Acts as high-probability magnet and support anchor.',
    type: 'volume'
  },
  SUPPLY_DEMAND_ZONE: {
    number: '24',
    name: 'Supply & Demand Zones',
    subtitle: 'Order Block Imbalances',
    rule: 'Identifies institutional order blocks: Demand Zone where aggressive buying originated; Supply Zone where selling clustered.',
    trigger: 'Enter long on retest of pristine Demand Zone with invalidation below the zone boundary.',
    type: 'supply_demand'
  },
  MARKET_STRUCTURE_BOS: {
    number: '26',
    name: 'Break of Structure (BOS)',
    subtitle: 'Trend Continuation Signal',
    rule: 'Price closes beyond the most recent higher swing high in an uptrend, confirming trend continuation.',
    trigger: 'Wait for pullback into newly created demand to join ongoing directional trend.',
    type: 'bos'
  },
  MARKET_STRUCTURE_CHOCH: {
    number: '27',
    name: 'Change of Character (CHoCH)',
    subtitle: 'Trend Reversal Signal',
    rule: 'Price violates the key structural swing low that produced the highest high, signaling institutional regime shift.',
    trigger: 'First warning sign of macro trend reversal.',
    type: 'choch'
  },
  HARMONIC_PATTERN: {
    number: '10',
    name: 'Harmonic Patterns',
    subtitle: 'Fibonacci Geometry (Gartley/Bat)',
    rule: 'Geometric XABCD price structure adhering to exact Fibonacci retracement and extension ratios.',
    trigger: 'Potential Reversal Zone (PRZ) at point D offers asymmetric risk-reward entry.',
    type: 'harmonic'
  },
  HEIKIN_ASHI_TREND: {
    number: '07',
    name: 'Heikin Ashi Trend',
    subtitle: 'Noise Reduction Smoothing',
    rule: 'Consecutive flat-bottom green Heikin Ashi candles indicate clean uninterrupted directional bull momentum.',
    trigger: 'Stay in trade until first red candle or double-wick indecision appears.',
    type: 'heikin_ashi'
  },
  MOON_PHASE_CORRELATION: {
    number: '08',
    name: 'Moon Phase Cycle',
    subtitle: 'Astronomical Market Rhythms',
    rule: 'New Moon (trough inflection) and Full Moon (crest inflection) cycle correlation across global equity markets.',
    trigger: 'Experimental cyclical timing filter used in conjunction with classical technical pivots.',
    type: 'moon'
  },
  ELLIOTT_WAVE: {
    number: '04E',
    name: 'Elliott Wave Impulse',
    subtitle: 'Fractal Wave Theory',
    rule: '5-wave motive impulse structure (1-2-3-4-5) followed by a 3-wave corrective ABC consolidation.',
    trigger: 'Wave 3 breakout offers the strongest institutional momentum wave.',
    type: 'elliott'
  }
};

// Fallback generator for unmapped patterns
export function getTheory(pattern, displayName, category) {
  if (PATTERN_THEORY_MAP[pattern]) {
    return PATTERN_THEORY_MAP[pattern];
  }
  return {
    number: '01',
    name: displayName || pattern.replace(/_/g, ' '),
    subtitle: `${category || 'Technical'} Structure`,
    rule: 'Institutional technical price formation audited with historical backtest evidence and algorithmic rule filters.',
    trigger: 'Enter on confirmed setup trigger with disciplined risk-reward parameters.',
    type: 'breakout'
  };
}

// Maps technical pattern codes and types to high-definition canvas diagrams
export function getDiagramId(type, patternKey) {
  const p = (patternKey || '').toUpperCase();
  const t = (type || '').toLowerCase();

  if (p === 'FIBONACCI_RETRACEMENT' || t === 'fib') return 'fib';
  if (p === 'FAILED_BREAKOUT' || t === 'failed_breakout') return 'failed_breakout';
  if (p.includes('BREAKOUT') || t === 'breakout') return 'breakout';
  if (p.includes('CUP') || t === 'cup_handle') return 'cup_handle';
  if (p.includes('DOUBLE_BOTTOM') || p.includes('W_BASE') || t === 'reversal' || t === 'double_bottom') return 'reversal';
  if (p.includes('DOUBLE_TOP') || p.includes('M_PATTERN') || t === 'double_top') return 'double_top';
  if (p.includes('VCP') || t === 'vcp') return 'vcp';
  if (p === 'FLAT_BASE' || t === 'flat_base') return 'flat_base';
  if (p === 'MULTI_YEAR_BASE' || t === 'multi_year') return 'multi_year';
  if (p === 'IPO_BASE' || t === 'ipo_base') return 'ipo_base';
  if (p.includes('FVG') || p.includes('FAIR_VALUE') || t === 'fvg') return 'fvg';
  if (p.includes('BULLISH_ENGULFING') || t === 'bullish_engulfing') return 'bullish_engulfing';
  if (p.includes('BEARISH_ENGULFING') || t === 'bearish_engulfing') return 'bearish_engulfing';
  if (p.includes('HAMMER') || t === 'hammer') return 'hammer';
  if (p.includes('SHOOTING_STAR') || t === 'shooting_star') return 'shooting_star';
  if (p.includes('MORNING_STAR') || t === 'morning_star') return 'morning_star';
  if (p.includes('EVENING_STAR') || t === 'evening_star') return 'evening_star';
  if (p.includes('DOJI') || t === 'doji') return 'doji';
  if (p.includes('SUPPORT_RESISTANCE') || p.includes('SR') || t === 'sr' || t === 'sr_touch') return 'sr';
  if (p.includes('TREND') || t === 'trend' || t === 'trendline') return 'trend';
  if (p.includes('MA_CROSS') || p.includes('GOLDEN_CROSS') || t === 'ma' || t === 'ma_cross') return 'ma';
  if (p.includes('SUPERTREND') || t === 'supertrend') return 'supertrend';
  if (p.includes('STOCHASTIC') || t === 'stoch') return 'stoch';
  if (p.includes('DIVERGENCE') || t === 'divergence') return 'divergence';
  if (p.includes('RSI') || t === 'rsi') return 'rsi';
  if (p.includes('MOMENTUM') || p.includes('ROC') || t === 'momentum') return 'momentum';
  if (p.includes('VOLUME_PROFILE') || p.includes('POC') || t === 'profile') return 'profile';
  if (p.includes('VOLUME') || p.includes('RVOL') || t === 'volume') return 'volume';
  if (p.includes('SUPPLY') || p.includes('DEMAND') || t === 'supply' || t === 'supply_demand') return 'supply';
  if (p.includes('BOS') || t === 'bos') return 'bos';
  if (p.includes('CHOCH') || t === 'choch') return 'choch';
  if (p.includes('HARMONIC') || t === 'harmonic') return 'harmonic';
  if (p.includes('HEIKIN') || t === 'heikin' || t === 'heikin_ashi') return 'heikin';
  if (p.includes('MOON') || t === 'moon') return 'moon';
  if (p.includes('ELLIOTT') || t === 'elliott') return 'elliott';
  if (p.includes('RENKO') || t === 'renko') return 'renko';
  if (p.includes('GANN') || t === 'gann') return 'gann';
  if (p.includes('VWAP') || t === 'vwap') return 'vwap';

  return t || 'breakout';
}

// Render dynamic, high-fidelity canvas technical diagram
export function renderDiagram(type, patternKey = null) {
  const diagramId = getDiagramId(type, patternKey);
  return <PatternCanvasDiagram diagramId={diagramId} />;
}

export default function PatternTheoryCard({ pattern, displayName, category, onClickBacktest }) {
  const theory = getTheory(pattern, displayName, category);

  return (
    <div
      className="pattern-theory-card"
      onClick={onClickBacktest}
      role="button"
      tabIndex={0}
      title="Click to view full historical evidence and audited backtests"
    >
      {/* Header with Number & Category */}
      <div className="theory-card-header">
        <div className="theory-num-badge font-mono">
          <span>{theory.number}</span>
        </div>
        <div className="theory-header-text">
          <div className="theory-cat-tag">{theory.subtitle}</div>
          <div className="theory-title">{theory.name}</div>
        </div>
      </div>

      {/* Center Visual Diagram */}
      <div className="theory-diagram-box">
        {renderDiagram(theory.type, pattern)}
      </div>

      {/* Core Institutional Rule */}
      <div className="theory-rule-box">
        <div className="theory-rule-lbl">
          <Sparkles size={11} className="accent-icon" />
          <span>INSTITUTIONAL SETUP RULE</span>
        </div>
        <p className="theory-rule-text">{theory.rule}</p>
      </div>

      {/* Footer Trigger & Link */}
      <div className="theory-card-footer">
        <div className="theory-evidence-link">
          <ShieldCheck size={13} style={{ color: 'var(--fintech-primary)' }} />
          <span>Audited Backtest & Math</span>
          <ArrowUpRight size={13} />
        </div>
      </div>
    </div>
  );
}
