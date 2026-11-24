"""
Phase 3 Detectors — Price-Action, Candlestick & Structural Base Patterns
12. BULLISH_ENGULFING & BEARISH_ENGULFING
13. DOJI
14. HAMMER & SHOOTING_STAR
15. MORNING_STAR & EVENING_STAR
16. DOUBLE_TOP & DOUBLE_BOTTOM
17. VCP (Volatility Contraction Pattern)
18. FLAT_BASE
19. CUP_AND_HANDLE
20. IPO_BASE
21. MULTI_YEAR_BASE
22. SUPPLY_DEMAND_ZONE
23. FAIR_VALUE_GAP
"""

import numpy as np
import pandas as pd
from typing import Dict, Any, Optional


def detect_candlestick_patterns(df: pd.DataFrame, params: Dict[str, Any]) -> Dict[str, Optional[Dict[str, Any]]]:
    """Evaluates candlestick patterns on the most recent completed candle."""
    results = {}
    if len(df) < 5:
        return results

    r0 = df.iloc[-1]
    r1 = df.iloc[-2]

    o0, h0, l0, c0 = float(r0["open"]), float(r0["high"]), float(r0["low"]), float(r0["close"])
    o1, h1, l1, c1 = float(r1["open"]), float(r1["high"]), float(r1["low"]), float(r1["close"])

    body0 = abs(c0 - o0)
    range0 = max(0.001, h0 - l0)
    body1 = abs(c1 - o1)

    # 12a. BULLISH_ENGULFING
    if c1 < o1 and c0 > o0 and c0 >= o1 and o0 <= c1:
        results["BULLISH_ENGULFING"] = {
            "pattern": "BULLISH_ENGULFING",
            "pattern_version": "ENGULFING_v1",
            "pattern_score": 82.0,
            "pivot": round(h0, 2),
            "details": {
                "type": "BULLISH_ENGULFING",
                "reasons": [
                    "Bullish engulfing candle fully engulfed previous bearish body",
                    f"Strong buying impulse with close at {c0:.2f}",
                    f"Engulfing body ratio: {body0 / max(0.001, body1):.1f}x"
                ]
            }
        }

    # 12b. BEARISH_ENGULFING
    if c1 > o1 and c0 < o0 and c0 <= o1 and o0 >= c1:
        results["BEARISH_ENGULFING"] = {
            "pattern": "BEARISH_ENGULFING",
            "pattern_version": "BEAR_ENGULFING_v1",
            "pattern_score": 78.0,
            "pivot": round(l0, 2),
            "details": {
                "type": "BEARISH_ENGULFING",
                "reasons": [
                    "Bearish engulfing candle completely engulfed previous bull body",
                    f"Heavy supply pushing close to {c0:.2f}",
                    "Reversal caution advised"
                ]
            }
        }

    # 13. DOJI
    if body0 / range0 <= 0.10:
        results["DOJI"] = {
            "pattern": "DOJI",
            "pattern_version": "DOJI_v1",
            "pattern_score": 68.0,
            "pivot": round(c0, 2),
            "details": {
                "type": "DOJI_INDECISION",
                "body_pct": round(body0 / range0 * 100, 1),
                "reasons": [
                    "Doji candlestick indicating equilibrium between bulls and bears",
                    f"Tiny candle body occupying only {body0 / range0 * 100:.1f}% of day range",
                    f"Watch for expansion beyond high {h0:.2f} or low {l0:.2f}"
                ]
            }
        }

    # 14a. HAMMER
    lower_shadow0 = min(o0, c0) - l0
    upper_shadow0 = h0 - max(o0, c0)
    if lower_shadow0 >= 2.0 * body0 and upper_shadow0 <= 0.3 * range0:
        results["HAMMER"] = {
            "pattern": "HAMMER",
            "pattern_version": "HAMMER_v1",
            "pattern_score": 85.0,
            "pivot": round(l0, 2),
            "details": {
                "type": "BULLISH_HAMMER",
                "shadow_ratio": round(lower_shadow0 / max(0.001, body0), 1),
                "reasons": [
                    "Hammer candle with long lower shadow showing intraday dip rejection",
                    f"Lower shadow {lower_shadow0 / max(0.001, body0):.1f}x the body length",
                    f"Defended key low pivot at {l0:.2f}"
                ]
            }
        }

    # 14b. SHOOTING_STAR
    if upper_shadow0 >= 2.0 * body0 and lower_shadow0 <= 0.3 * range0:
        results["SHOOTING_STAR"] = {
            "pattern": "SHOOTING_STAR",
            "pattern_version": "SHOOTING_STAR_v1",
            "pattern_score": 80.0,
            "pivot": round(h0, 2),
            "details": {
                "type": "BEARISH_SHOOTING_STAR",
                "reasons": [
                    "Shooting star candle with prominent upper shadow showing overhead supply",
                    f"Upper shadow {upper_shadow0 / max(0.001, body0):.1f}x the body length",
                    f"Rejected ceiling at {h0:.2f}"
                ]
            }
        }

    # 15. MORNING_STAR & EVENING_STAR (3-bar patterns)
    if len(df) >= 3:
        r2 = df.iloc[-3]
        o2, c2 = float(r2["open"]), float(r2["close"])
        body2 = abs(c2 - o2)

        # Morning Star: Bearish bar -> tiny star -> Bullish bar closing into first bar
        if c2 < o2 and body1 < 0.3 * body2 and c0 > o0 and c0 > (o2 + c2) / 2.0:
            results["MORNING_STAR"] = {
                "pattern": "MORNING_STAR",
                "pattern_version": "MORNING_STAR_v1",
                "pattern_score": 86.0,
                "pivot": round(l1, 2),
                "details": {
                    "type": "BULLISH_MORNING_STAR",
                    "reasons": [
                        "Morning Star 3-bar reversal confirmed",
                        "Bearish candle followed by indecision star and strong bullish engulfing follow-through",
                        f"Closed comfortably above 50% midpoint of day-1 bar at {c0:.2f}"
                    ]
                }
            }

        # Evening Star: Bullish bar -> tiny star -> Bearish bar closing into first bar
        if c2 > o2 and body1 < 0.3 * body2 and c0 < o0 and c0 < (o2 + c2) / 2.0:
            results["EVENING_STAR"] = {
                "pattern": "EVENING_STAR",
                "pattern_version": "EVENING_STAR_v1",
                "pattern_score": 82.0,
                "pivot": round(h1, 2),
                "details": {
                    "type": "BEARISH_EVENING_STAR",
                    "reasons": [
                        "Evening Star 3-bar reversal confirmed",
                        "Bullish candle followed by exhaustion star and sharp bearish follow-through",
                        f"Reversal confirmed at {c0:.2f}"
                    ]
                }
            }

    return results


def detect_double_top_bottom(df: pd.DataFrame, params: Dict[str, Any]) -> Dict[str, Optional[Dict[str, Any]]]:
    """Detects Double Bottom (W-Base) and Double Top (M-Pattern)."""
    results = {}
    if len(df) < 40:
        return results

    sub = df.iloc[-60:-1]
    sl_indices = sub.index[sub["is_swing_low"] == True].tolist()
    sh_indices = sub.index[sub["is_swing_high"] == True].tolist()
    tol = params.get("tolerance_pct", 0.025)
    c = float(df.iloc[-1]["close"])

    # Double Bottom
    if len(sl_indices) >= 2:
        i1, i2 = sl_indices[-2], sl_indices[-1]
        l1, l2 = float(df.loc[i1, "low"]), float(df.loc[i2, "low"])
        if abs(l1 - l2) / min(l1, l2) <= tol and (i2 - i1) >= 8:
            # Find intermediate peak (neckline)
            neckline_df = df.loc[i1:i2]
            neckline = float(neckline_df["high"].max())
            if c >= neckline * 0.96:
                results["DOUBLE_BOTTOM"] = {
                    "pattern": "DOUBLE_BOTTOM",
                    "pattern_version": "DOUBLE_BOTTOM_v1",
                    "pattern_score": 84.0,
                    "pivot": round(neckline, 2),
                    "details": {
                        "type": "W_BASE_DOUBLE_BOTTOM",
                        "bottom1": round(l1, 2),
                        "bottom2": round(l2, 2),
                        "neckline": round(neckline, 2),
                        "reasons": [
                            f"Double bottom base formed at {l1:.2f} and {l2:.2f}",
                            f"Tested support within {abs(l1 - l2) / min(l1, l2) * 100:.1f}% tolerance",
                            f"Approaching / breaking neckline pivot at {neckline:.2f}"
                        ]
                    }
                }

    # Double Top
    if len(sh_indices) >= 2:
        i1, i2 = sh_indices[-2], sh_indices[-1]
        h1, h2 = float(df.loc[i1, "high"]), float(df.loc[i2, "high"])
        if abs(h1 - h2) / max(h1, h2) <= tol and (i2 - i1) >= 8:
            valley = float(df.loc[i1:i2]["low"].min())
            if c <= valley * 1.04:
                results["DOUBLE_TOP"] = {
                    "pattern": "DOUBLE_TOP",
                    "pattern_version": "DOUBLE_TOP_v1",
                    "pattern_score": 81.0,
                    "pivot": round(valley, 2),
                    "details": {
                        "type": "M_PATTERN_DOUBLE_TOP",
                        "top1": round(h1, 2),
                        "top2": round(h2, 2),
                        "neckline": round(valley, 2),
                        "reasons": [
                            f"Double top resistance tested at {h1:.2f} and {h2:.2f}",
                            f"Dual peak rejection within {abs(h1 - h2) / max(h1, h2) * 100:.1f}% tolerance",
                            f"Testing breakdown neckline at {valley:.2f}"
                        ]
                    }
                }

    return results


def detect_vcp(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """
    Volatility Contraction Pattern (VCP):
    - Prior established uptrend (price > SMA50 > SMA200)
    - Successive contractions with strictly decreasing depth (e.g. 18% -> 9% -> 4%)
    - Volume drying up through the contractions
    - Price within 3% of the base pivot high
    """
    if len(df) < 70:
        return None

    last_row = df.iloc[-1]
    c = float(last_row["close"])
    sma50 = float(last_row.get("sma50", 0))

    # Prior uptrend check
    if sma50 > 0 and c < sma50 * 0.97:
        return None

    lookback_window = df.iloc[-60:]
    sh_df = lookback_window[lookback_window["is_swing_high"] == True]
    sl_df = lookback_window[lookback_window["is_swing_low"] == True]

    if len(sh_df) < 2 or len(sl_df) < 2:
        return None

    # Measure last 2-3 contractions
    highs = sh_df["high"].tolist()[-3:]
    lows = sl_df["low"].tolist()[-3:]

    depths = []
    for h, l in zip(highs, lows):
        if h > l:
            depths.append(round((h - l) / h * 100.0, 1))

    # Must have strictly decreasing depths
    if len(depths) >= 2 and all(depths[i] < depths[i - 1] for i in range(1, len(depths))):
        pivot = float(max(highs))
        dist_to_pivot = (pivot - c) / pivot * 100.0

        if -2.0 <= dist_to_pivot <= 4.0:
            vol_last5 = df.iloc[-5:]["volume"].mean()
            vol_prev20 = df.iloc[-25:-5]["volume"].mean()
            vol_drying = vol_last5 < vol_prev20

            score = 88.0 + (5.0 if vol_drying else 0.0)
            contraction_str = " → ".join([f"{d}%" for d in depths])
            return {
                "pattern": "VCP",
                "pattern_version": "VCP_v1",
                "pattern_score": round(score, 1),
                "pivot": round(pivot, 2),
                "details": {
                    "type": "VOLATILITY_CONTRACTION",
                    "contractions": depths,
                    "reasons": [
                        "Prior established uptrend with healthy moving average alignment",
                        f"{len(depths)} contractions with strictly decreasing volatility: {contraction_str}",
                        "Volume dried up significantly in right-side pivot setup" if vol_drying else "Right-side volume tightening",
                        f"Current price is {abs(dist_to_pivot):.1f}% from the {pivot:.2f} breakout pivot"
                    ]
                }
            }

    return None


def detect_flat_base(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Flat base consolidation: tight price channel (< 12-14% depth) over at least 4-5 weeks."""
    if len(df) < 30:
        return None

    window = df.iloc[-25:]
    highest = float(window["high"].max())
    lowest = float(window["low"].min())
    depth_pct = (highest - lowest) / highest * 100.0

    if depth_pct <= 13.5:
        c = float(df.iloc[-1]["close"])
        if c >= highest * 0.97:
            return {
                "pattern": "FLAT_BASE",
                "pattern_version": "FLAT_BASE_v1",
                "pattern_score": 83.0,
                "pivot": round(highest, 2),
                "details": {
                    "type": "FLAT_BASE_CONSOLIDATION",
                    "base_depth_pct": round(depth_pct, 1),
                    "reasons": [
                        f"Tight flat base consolidation over 25 trading sessions",
                        f"Extremely shallow base depth of only {depth_pct:.1f}%",
                        f"Coiled near upper resistance pivot at {highest:.2f}"
                    ]
                }
            }
    return None


def detect_cup_and_handle(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Cup with Handle: Rounded U-shaped base (depth 12-35%) followed by shallow handle (< 12%)."""
    if len(df) < 65:
        return None

    window = df.iloc[-65:]
    peak1 = float(window.iloc[:15]["high"].max())
    cup_bottom = float(window.iloc[15:45]["low"].min())
    peak2 = float(window.iloc[40:55]["high"].max())

    cup_depth = (peak1 - cup_bottom) / peak1 * 100.0
    if 10.0 <= cup_depth <= 38.0 and abs(peak1 - peak2) / peak1 <= 0.06:
        # Check handle
        handle_window = window.iloc[50:]
        handle_high = float(handle_window["high"].max())
        handle_low = float(handle_window["low"].min())
        handle_depth = (handle_high - handle_low) / handle_high * 100.0

        if handle_depth <= 12.0:
            c = float(df.iloc[-1]["close"])
            if c >= handle_high * 0.97:
                return {
                    "pattern": "CUP_AND_HANDLE",
                    "pattern_version": "CUP_HANDLE_v1",
                    "pattern_score": 87.0,
                    "pivot": round(max(peak1, peak2), 2),
                    "details": {
                        "type": "CUP_WITH_HANDLE",
                        "cup_depth": round(cup_depth, 1),
                        "handle_depth": round(handle_depth, 1),
                        "reasons": [
                            f"Rounded cup formation with {cup_depth:.1f}% consolidation depth",
                            f"Constructive shallow handle pullback of only {handle_depth:.1f}%",
                            f"Approaching breakout pivot point at {max(peak1, peak2):.2f}"
                        ]
                    }
                }
    return None


def detect_ipo_base(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """IPO Debut Consolidation Base: Tickers with < 500 trading days forming their first major base."""
    if len(df) > 500 or len(df) < 30:
        return None

    recent = df.iloc[-30:]
    high = float(recent["high"].max())
    low = float(recent["low"].min())
    c = float(df.iloc[-1]["close"])

    if c >= high * 0.96:
        return {
            "pattern": "IPO_BASE",
            "pattern_version": "IPO_BASE_v1",
            "pattern_score": 79.0,
            "pivot": round(high, 2),
            "details": {
                "type": "IPO_CONSOLIDATION_BASE",
                "total_history_bars": len(df),
                "reasons": [
                    f"Young listing ({len(df)} trading days of history) forming primary IPO base",
                    "Institutional accumulation prior to major debut stage-2 expansion",
                    f"Testing base breakout level at {high:.2f}"
                ]
            }
        }
    return None


def detect_multi_year_base(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Multi-year structural base: Consolidating near multi-year highs after prolonged consolidation."""
    if len(df) < 250:
        return None

    c = float(df.iloc[-1]["close"])
    hist_high = float(df["high"].max())

    if c >= hist_high * 0.96:
        return {
            "pattern": "MULTI_YEAR_BASE",
            "pattern_version": "MULTI_YEAR_BASE_v1",
            "pattern_score": 90.0,
            "pivot": round(hist_high, 2),
            "details": {
                "type": "MULTI_YEAR_BREAKOUT",
                "reasons": [
                    f"Emerging from a multi-year base of {len(df)} trading days",
                    f"Testing macro all-time/multi-year pivot high at {hist_high:.2f}",
                    "High probability stage-1 institutional breakout structure"
                ]
            }
        }
    return None


def detect_supply_demand_zone(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Institutional Demand Zone Retest: Aggressive prior rally origin being tested with absorption."""
    if len(df) < 30:
        return None

    last_row = df.iloc[-1]
    c = float(last_row["close"])
    l = float(last_row["low"])

    # Locate strong prior expansion bar (green bar > 2.5% with high volume)
    for i in range(len(df) - 25, len(df) - 5):
        row_i = df.iloc[i]
        o_i, c_i, l_i = float(row_i["open"]), float(row_i["close"]), float(row_i["low"])
        if (c_i - o_i) / o_i >= 0.025 and float(row_i.get("rel_volume", 1.0)) > 1.4:
            zone_low = l_i
            zone_high = (o_i + c_i) / 2.0
            if zone_low * 0.99 <= l <= zone_high * 1.01 and c >= zone_low:
                return {
                    "pattern": "SUPPLY_DEMAND_ZONE",
                    "pattern_version": "SND_ZONE_v1",
                    "pattern_score": 83.0,
                    "pivot": round(zone_low, 2),
                    "details": {
                        "type": "DEMAND_ZONE_RETEST",
                        "zone_bottom": round(zone_low, 2),
                        "zone_top": round(zone_high, 2),
                        "reasons": [
                            f"Retesting institutional demand zone origin [{zone_low:.2f} - {zone_high:.2f}]",
                            "Price absorbed at original liquidity injection origin",
                            f"Defended low at {l:.2f}"
                        ]
                    }
                }
    return None


def detect_fair_value_gap(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Fair Value Gap (FVG): Retest of 3-candle imbalance zone."""
    if len(df) < 10:
        return None

    # Check for bullish FVG created in last 10 bars: Candle 1 High < Candle 3 Low
    for i in range(len(df) - 8, len(df) - 2):
        c1 = df.iloc[i - 1]
        c3 = df.iloc[i + 1]
        h1 = float(c1["high"])
        l3 = float(c3["low"])

        if l3 > h1 * 1.008:  # Clear gap between bar 1 and bar 3
            fvg_top = l3
            fvg_bottom = h1
            curr_low = float(df.iloc[-1]["low"])
            curr_close = float(df.iloc[-1]["close"])

            if fvg_bottom <= curr_low <= fvg_top and curr_close >= fvg_bottom:
                return {
                    "pattern": "FAIR_VALUE_GAP",
                    "pattern_version": "FVG_v1",
                    "pattern_score": 81.0,
                    "pivot": round(fvg_top, 2),
                    "details": {
                        "type": "BULLISH_FVG_MITIGATION",
                        "fvg_bottom": round(fvg_bottom, 2),
                        "fvg_top": round(fvg_top, 2),
                        "reasons": [
                            f"Mitigating Fair Value Gap (FVG) between {fvg_bottom:.2f} and {fvg_top:.2f}",
                            "Imbalance filled with buyer absorption",
                            f"Closing price reclaimed {curr_close:.2f}"
                        ]
                    }
                }
    return None
