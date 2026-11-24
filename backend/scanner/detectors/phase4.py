"""
Phase 4 Detectors — Advanced
24. DIVERGENCE (Bullish / Bearish RSI divergence)
25. FIBONACCI_RETRACEMENT_REACTION
26. FIBONACCI_EXTENSION_TARGET
27. HARMONIC_PATTERN (Gartley / Bat / Butterfly with confluence check)
28. HEIKIN_ASHI_TREND
29. VOLUME_PROFILE_POC_REACTION
"""

import numpy as np
import pandas as pd
from typing import Dict, Any, Optional


def detect_divergence(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Detects Bullish RSI divergence: Lower price low accompanied by higher RSI low."""
    if len(df) < 35:
        return None

    sub = df.iloc[-40:-1]
    sl_indices = sub.index[sub["is_swing_low"] == True].tolist()

    if len(sl_indices) >= 2:
        i1, i2 = sl_indices[-2], sl_indices[-1]
        p1, p2 = float(df.loc[i1, "low"]), float(df.loc[i2, "low"])
        rsi1, rsi2 = float(df.loc[i1, "rsi14"]), float(df.loc[i2, "rsi14"])

        # Regular Bullish Divergence: Price Lower Low, RSI Higher Low
        if p2 < p1 and rsi2 > rsi1 and rsi2 <= 45:
            last_c = float(df.iloc[-1]["close"])
            return {
                "pattern": "DIVERGENCE",
                "pattern_version": "RSI_DIV_v1",
                "pattern_score": 85.0,
                "pivot": round(p2, 2),
                "details": {
                    "type": "BULLISH_RSI_DIVERGENCE",
                    "price_low1": round(p1, 2),
                    "price_low2": round(p2, 2),
                    "rsi1": round(rsi1, 1),
                    "rsi2": round(rsi2, 1),
                    "reasons": [
                        "Regular Bullish Divergence between Price and RSI",
                        f"Price printed lower low ({p1:.2f} → {p2:.2f}) while RSI formed higher low ({rsi1:.1f} → {rsi2:.1f})",
                        f"Selling momentum exhausted; current close at {last_c:.2f}"
                    ]
                }
            }
    return None


def detect_fibonacci_reaction(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Fibonacci Retracement Reaction: Testing the 50% or 61.8% golden pocket pullback."""
    if len(df) < 40:
        return None

    sub = df.iloc[-50:-1]
    sh_indices = sub.index[sub["is_swing_high"] == True].tolist()
    sl_indices = sub.index[sub["is_swing_low"] == True].tolist()

    if len(sh_indices) >= 1 and len(sl_indices) >= 1:
        # Prior impulsive leg
        sw_low = float(df.loc[sl_indices[-1], "low"])
        sw_high = float(df.loc[sh_indices[-1], "high"])

        if sw_high > sw_low and (sw_high - sw_low) / sw_low >= 0.08:
            range_span = sw_high - sw_low
            fib_50 = sw_high - (0.50 * range_span)
            fib_618 = sw_high - (0.618 * range_span)

            c = float(df.iloc[-1]["close"])
            l = float(df.iloc[-1]["low"])

            # Check if low bounced in the 50%-61.8% golden pocket
            if fib_618 * 0.985 <= l <= fib_50 * 1.015 and c >= fib_618:
                return {
                    "pattern": "FIBONACCI_RETRACEMENT_REACTION",
                    "pattern_version": "FIB_REACTION_v1",
                    "pattern_score": 84.0,
                    "pivot": round(fib_618, 2),
                    "details": {
                        "type": "GOLDEN_POCKET_REACTION",
                        "fib_50": round(fib_50, 2),
                        "fib_618": round(fib_618, 2),
                        "reasons": [
                            f"Price defended 0.618 Fibonacci golden pocket at {fib_618:.2f}",
                            f"Healthy pullback from impulse leg [{sw_low:.2f} → {sw_high:.2f}]",
                            f"Current price confirmed above pocket at {c:.2f}"
                        ]
                    }
                }
    return None


def detect_fibonacci_extension(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Fibonacci Extension Target: Price approaching 1.272 or 1.618 extension target."""
    if len(df) < 40:
        return None

    c = float(df.iloc[-1]["close"])
    sub = df.iloc[-40:-1]
    sh_indices = sub.index[sub["is_swing_high"] == True].tolist()
    sl_indices = sub.index[sub["is_swing_low"] == True].tolist()

    if len(sh_indices) >= 1 and len(sl_indices) >= 1:
        sw_low = float(df.loc[sl_indices[-1], "low"])
        sw_high = float(df.loc[sh_indices[-1], "high"])
        if sw_high > sw_low and c > sw_high:
            range_span = sw_high - sw_low
            ext_1272 = sw_high + (0.272 * range_span)
            ext_1618 = sw_high + (0.618 * range_span)

            if abs(c - ext_1272) / ext_1272 <= 0.02 or abs(c - ext_1618) / ext_1618 <= 0.02:
                target_ext = ext_1272 if abs(c - ext_1272) < abs(c - ext_1618) else ext_1618
                ratio_name = "1.272" if target_ext == ext_1272 else "1.618"
                return {
                    "pattern": "FIBONACCI_EXTENSION_TARGET",
                    "pattern_version": "FIB_EXT_v1",
                    "pattern_score": 77.0,
                    "pivot": round(target_ext, 2),
                    "details": {
                        "type": "FIBONACCI_EXTENSION",
                        "extension_level": round(target_ext, 2),
                        "reasons": [
                            f"Approaching {ratio_name} Fibonacci extension target at {target_ext:.2f}",
                            f"Measured expansion from impulse range {range_span:.2f}",
                            f"Current price: {c:.2f}"
                        ]
                    }
                }
    return None


def detect_harmonic_pattern(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Harmonic Pattern (Gartley / Bat / Butterfly): Requires confluence with Phase 1-2 signals."""
    if len(df) < 40:
        return None

    # Confluence requirement per spec
    last_row = df.iloc[-1]
    rvol = float(last_row.get("rel_volume", 1.0))
    rsi = float(last_row.get("rsi14", 50))
    has_confluence = (rvol > 1.2) or (rsi < 40) or (last_row.get("supertrend_dir") == "BULL")

    if not has_confluence:
        return None

    sub = df.iloc[-50:-1]
    sl_indices = sub.index[sub["is_swing_low"] == True].tolist()
    sh_indices = sub.index[sub["is_swing_high"] == True].tolist()

    if len(sl_indices) >= 2 and len(sh_indices) >= 2:
        # XABCD geometric approximation
        X = float(df.loc[sl_indices[-2], "low"])
        A = float(df.loc[sh_indices[-2], "high"])
        B = float(df.loc[sl_indices[-1], "low"])
        C = float(df.loc[sh_indices[-1], "high"])
        D = float(df.iloc[-1]["close"])

        xa_range = A - X
        if xa_range > 0:
            b_ratio = (A - B) / xa_range
            # Gartley B ratio ~ 0.618 (tolerance 0.08)
            if 0.53 <= b_ratio <= 0.70:
                d_ratio = (A - D) / xa_range
                if 0.70 <= d_ratio <= 0.88:
                    return {
                        "pattern": "HARMONIC_PATTERN",
                        "pattern_version": "HARMONIC_v1",
                        "pattern_score": 83.0,
                        "pivot": round(D, 2),
                        "details": {
                            "type": "BULLISH_GARTLEY_HARMONIC",
                            "xa_span": round(xa_range, 2),
                            "reasons": [
                                f"Bullish Gartley Harmonic formation completing in D-PRZ zone",
                                f"B-point retraced {b_ratio:.2f} of XA (ideal 0.618)",
                                f"D-point completed at {d_ratio:.2f} retracement level ({D:.2f})",
                                "Confluence verified with indicator momentum"
                            ]
                        }
                    }
    return None


def detect_heikin_ashi_trend(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Heikin-Ashi Trend: Consecutive green bars with flat lower shadows (no lower wick)."""
    if len(df) < 15:
        return None

    # Calculate last 5 Heikin-Ashi candles
    ha_close = (df["open"] + df["high"] + df["low"] + df["close"]) / 4.0
    ha_open = pd.Series(index=df.index, dtype=float)
    ha_open.iloc[0] = (df["open"].iloc[0] + df["close"].iloc[0]) / 2.0
    for i in range(1, len(df)):
        ha_open.iloc[i] = (ha_open.iloc[i - 1] + ha_close.iloc[i - 1]) / 2.0

    ha_low = pd.concat([df["low"], ha_open, ha_close], axis=1).min(axis=1)

    # Check last 3 bars
    consecutive_bull = True
    no_lower_wick_count = 0
    for i in range(len(df) - 3, len(df)):
        o, c, l = ha_open.iloc[i], ha_close.iloc[i], ha_low.iloc[i]
        if c <= o:
            consecutive_bull = False
            break
        if abs(min(o, c) - l) / (c - o) < 0.15:
            no_lower_wick_count += 1

    if consecutive_bull and no_lower_wick_count >= 2:
        c_real = float(df.iloc[-1]["close"])
        return {
            "pattern": "HEIKIN_ASHI_TREND",
            "pattern_version": "HA_TREND_v1",
            "pattern_score": 82.0,
            "pivot": round(c_real, 2),
            "details": {
                "type": "HEIKIN_ASHI_TREND_SURGE",
                "reasons": [
                    "3 consecutive bullish Heikin-Ashi directional bars",
                    f"{no_lower_wick_count} bars with flat bottom (no lower shadow), signaling pure directional velocity",
                    f"Trend acceleration confirmed at {c_real:.2f}"
                ]
            }
        }
    return None


def detect_volume_profile_poc_reaction(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Volume Profile POC Reaction: Price bouncing off or retesting the Point of Control."""
    if len(df) < 30:
        return None

    last_row = df.iloc[-1]
    extra = last_row.get("extra")
    poc = None
    if isinstance(extra, dict):
        poc = extra.get("poc_price")

    if poc and poc > 0:
        c = float(last_row["close"])
        l = float(last_row["low"])
        tol = params.get("tolerance_pct", 0.015)

        if abs(l - poc) / poc <= tol and c >= poc:
            return {
                "pattern": "VOLUME_PROFILE_POC_REACTION",
                "pattern_version": "POC_REACTION_v1",
                "pattern_score": 81.0,
                "pivot": round(poc, 2),
                "details": {
                    "type": "POC_SUPPORT_REACTION",
                    "poc_price": round(poc, 2),
                    "reasons": [
                        f"Reaction at Volume Profile Point of Control (POC) of {poc:.2f}",
                        f"Price tested heavy liquidity node within {abs(l - poc) / poc * 100:.1f}% tolerance",
                        f"Absorption confirmed with close at {c:.2f}"
                    ]
                }
            }
    return None
