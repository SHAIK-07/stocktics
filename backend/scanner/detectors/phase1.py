"""
Phase 1 Detectors — Market Structure
1. SUPPORT_RESISTANCE_TOUCH
2. TRENDLINE_BREAK
3. BREAKOUT & FAILED_BREAKOUT
4. MARKET_STRUCTURE_BOS (Break of Structure)
5. MARKET_STRUCTURE_CHOCH (Change of Character)
"""

import numpy as np
import pandas as pd
from typing import List, Dict, Any, Optional


def detect_support_resistance_touch(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 30:
        return None

    last_row = df.iloc[-1]
    close = float(last_row["close"])
    low = float(last_row["low"])
    high = float(last_row["high"])

    # Find swing highs and lows in trailing 60 bars
    window_df = df.iloc[-60:-1]
    swing_highs = window_df[window_df["is_swing_high"] == True]["high"].tolist()
    swing_lows = window_df[window_df["is_swing_low"] == True]["low"].tolist()

    tolerance = params.get("tolerance_pct", 0.015)

    # Check for Support Touch & Bounce
    for s_low in sorted(swing_lows, reverse=True):
        if abs(low - s_low) / s_low <= tolerance and close >= s_low:
            score = 75.0 + min(20.0, float(last_row.get("rel_volume", 1.0)) * 5)
            return {
                "pattern": "SUPPORT_RESISTANCE_TOUCH",
                "pattern_version": "SUPPORT_RESISTANCE_v1",
                "pattern_score": round(score, 1),
                "pivot": round(s_low, 2),
                "details": {
                    "type": "SUPPORT_BOUNCE",
                    "level": round(s_low, 2),
                    "reasons": [
                        f"Support bounce at {s_low:.2f}",
                        f"Low was within {abs(low - s_low) / s_low * 100:.1f}% of key horizontal level",
                        f"Confirmed by {last_row['supertrend_dir']} market posture"
                    ]
                }
            }

    # Check for Resistance Test
    for s_high in sorted(swing_highs):
        if abs(high - s_high) / s_high <= tolerance:
            score = 70.0 + min(20.0, float(last_row.get("rel_volume", 1.0)) * 4)
            return {
                "pattern": "SUPPORT_RESISTANCE_TOUCH",
                "pattern_version": "SUPPORT_RESISTANCE_v1",
                "pattern_score": round(score, 1),
                "pivot": round(s_high, 2),
                "details": {
                    "type": "RESISTANCE_TEST",
                    "level": round(s_high, 2),
                    "reasons": [
                        f"Testing horizontal resistance at {s_high:.2f}",
                        f"Price within {abs(high - s_high) / s_high * 100:.1f}% of ceiling",
                        f"Relative volume {last_row.get('rel_volume', 1.0):.1f}x"
                    ]
                }
            }

    return None


def detect_trendline_break(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 40:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]
    close = float(last_row["close"])
    prev_close = float(prev_row["close"])
    lookback = min(len(df) - 5, params.get("lookback_bars", 50))

    sub = df.iloc[-lookback:-1]
    sh_indices = sub.index[sub["is_swing_high"] == True].tolist()
    if len(sh_indices) < 2:
        return None

    # Fit descending trendline through the two most prominent swing highs
    i1, i2 = sh_indices[-2], sh_indices[-1]
    p1, p2 = float(df.loc[i1, "high"]), float(df.loc[i2, "high"])

    if p2 < p1:  # Descending trendline
        slope = (p2 - p1) / (i2 - i1)
        curr_trendline = p2 + slope * (len(df) - 1 - i2)
        prev_trendline = p2 + slope * (len(df) - 2 - i2)

        if prev_close <= prev_trendline and close > curr_trendline:
            rvol = float(last_row.get("rel_volume", 1.0))
            score = min(95.0, 72.0 + (rvol * 8.0))
            return {
                "pattern": "TRENDLINE_BREAK",
                "pattern_version": "TRENDLINE_BREAK_v1",
                "pattern_score": round(score, 1),
                "pivot": round(curr_trendline, 2),
                "details": {
                    "type": "BULLISH_TRENDLINE_BREAK",
                    "trendline_price": round(curr_trendline, 2),
                    "reasons": [
                        f"Broke above {lookback}-bar descending trendline",
                        f"Closed {((close - curr_trendline) / curr_trendline * 100):.1f}% above trendline",
                        f"Volume expansion {rvol:.1f}x 20-day average"
                    ]
                }
            }

    return None


def detect_breakout(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 30:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]
    close = float(last_row["close"])
    prev_close = float(prev_row["close"])

    lookback = params.get("lookback_high", 20)
    prior_high = float(df.iloc[-lookback - 1:-1]["high"].max())
    rvol = float(last_row.get("rel_volume", 1.0))

    # Standard Breakout
    if close > prior_high and prev_close <= prior_high * 1.01:
        score = 70.0 + min(25.0, rvol * 8.0)
        return {
            "pattern": "BREAKOUT",
            "pattern_version": "BREAKOUT_v1",
            "pattern_score": round(score, 1),
            "pivot": round(prior_high, 2),
            "details": {
                "type": "HIGH_BREAKOUT",
                "prior_high": round(prior_high, 2),
                "reasons": [
                    f"Cleared {lookback}-day high pivot of {prior_high:.2f}",
                    f"Closed {((close - prior_high) / prior_high * 100):.1f}% above pivot",
                    f"Surge volume at {rvol:.1f}x relative volume"
                ]
            }
        }

    # Failed Breakout (Trap)
    # Price probed above prior high in last 3 bars but closed below it
    if len(df) >= 4:
        recent_3 = df.iloc[-3:]
        probed_above = any(float(r["high"]) > prior_high for _, r in recent_3.iterrows())
        if probed_above and close < prior_high * 0.985:
            score = 76.0
            return {
                "pattern": "FAILED_BREAKOUT",
                "pattern_version": "FAILED_BREAKOUT_v1",
                "pattern_score": round(score, 1),
                "pivot": round(prior_high, 2),
                "details": {
                    "type": "BULL_TRAP_FAILURE",
                    "failed_level": round(prior_high, 2),
                    "reasons": [
                        f"Failed to hold {lookback}-day high of {prior_high:.2f}",
                        f"Bull trap rejection closing below breakout pivot",
                        f"Caution advised on follow-through"
                    ]
                }
            }

    return None


def detect_market_structure_bos(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Break of Structure (BOS): Trend continuation break above prior higher high."""
    if len(df) < 30:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]
    close = float(last_row["close"])
    prev_close = float(prev_row["close"])

    # Recent swing highs
    sub = df.iloc[-40:-1]
    sh_df = sub[sub["is_swing_high"] == True]
    if len(sh_df) < 1:
        return None

    last_sh_price = float(sh_df.iloc[-1]["high"])
    if prev_close <= last_sh_price and close > last_sh_price and last_row["supertrend_dir"] == "BULL":
        score = 82.0
        return {
            "pattern": "MARKET_STRUCTURE_BOS",
            "pattern_version": "BOS_v1",
            "pattern_score": round(score, 1),
            "pivot": round(last_sh_price, 2),
            "details": {
                "type": "BULLISH_BOS",
                "pivot_level": round(last_sh_price, 2),
                "reasons": [
                    f"Break of Structure (BOS) confirming bullish trend continuation",
                    f"Cleared structural swing high at {last_sh_price:.2f}",
                    f"Supertrend remaining firmly in BULL regime"
                ]
            }
        }
    return None


def detect_market_structure_choch(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Change of Character (CHoCH): Trend reversal shift breaking previous lower high."""
    if len(df) < 35:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]
    close = float(last_row["close"])
    prev_close = float(prev_row["close"])

    # Look for a downtrend context in the last 20 bars followed by a close above the last swing high
    sub = df.iloc[-30:-1]
    sh_df = sub[sub["is_swing_high"] == True]
    sl_df = sub[sub["is_swing_low"] == True]

    if len(sh_df) >= 1 and len(sl_df) >= 1:
        last_sh = float(sh_df.iloc[-1]["high"])
        # If previous posture was bear or SMA20 < SMA50
        was_bearish = prev_row["close"] < prev_row.get("sma50", prev_row["close"] * 1.05)
        if was_bearish and prev_close <= last_sh and close > last_sh:
            score = 80.0
            return {
                "pattern": "MARKET_STRUCTURE_CHOCH",
                "pattern_version": "CHOCH_v1",
                "pattern_score": round(score, 1),
                "pivot": round(last_sh, 2),
                "details": {
                    "type": "BULLISH_CHOCH",
                    "reversal_pivot": round(last_sh, 2),
                    "reasons": [
                        f"Change of Character (CHoCH) signaling early trend reversal",
                        f"Reclaimed last structural lower high at {last_sh:.2f}",
                        f"Breakout from defensive posture"
                    ]
                }
            }
    return None
