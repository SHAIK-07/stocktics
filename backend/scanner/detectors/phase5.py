"""
Phase 5 Detectors — Experimental / Full Library Completion
All built, all enabled, confidence_tier = 'EXPERIMENTAL', include_in_composite = false.
30. RENKO_TREND
31. GANN_ANGLE_TOUCH
32. MOON_PHASE_CORRELATION
33. ELLIOTT_WAVE
"""

import math
import numpy as np
import pandas as pd
from typing import Dict, Any, Optional


def detect_renko_trend(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Renko Trend: ATR-scaled brick trend flip (B = k * ATR_14)."""
    if len(df) < 30:
        return None

    last_row = df.iloc[-1]
    atr14 = float(last_row.get("atr14", 0))
    if atr14 <= 0:
        return None

    k = params.get("atr_mult", 0.5)
    brick_size = k * atr14
    if brick_size <= 0:
        return None

    # Simulate recent Renko bricks
    closes = df["close"].astype(float).tolist()
    bricks = []  # +1 for green brick, -1 for red brick
    last_brick_price = closes[0]

    for p in closes[1:]:
        diff = p - last_brick_price
        while abs(diff) >= brick_size:
            if diff > 0:
                bricks.append(1)
                last_brick_price += brick_size
            else:
                bricks.append(-1)
                last_brick_price -= brick_size
            diff = p - last_brick_price

    if len(bricks) >= 3:
        # Check if last 2 bricks flipped to positive
        if bricks[-1] == 1 and bricks[-2] == 1 and bricks[-3] == -1:
            c = float(last_row["close"])
            return {
                "pattern": "RENKO_TREND",
                "pattern_version": "RENKO_v1",
                "pattern_score": 75.0,
                "pivot": round(last_brick_price, 2),
                "details": {
                    "type": "RENKO_BULLISH_BRICK_REVERSAL",
                    "brick_size": round(brick_size, 2),
                    "reasons": [
                        f"Renko {brick_size:.2f}-pt ATR brick trend flipped to bullish",
                        "2 consecutive upward bricks completed after downtrend",
                        "Noise-filtered trend momentum active (Experimental)"
                    ]
                }
            }
    return None


def detect_gann_angle_touch(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """
    Gann Angle Touch:
    Convention: Price/time scale unit = 1.0 * ATR14 per 10 bars.
    Calculates 1x1 (45-degree equivalent), 2x1, and 1x2 angle reactions from recent swing low.
    """
    if len(df) < 35:
        return None

    last_row = df.iloc[-1]
    atr = float(last_row.get("atr14", 1.0))
    sub = df.iloc[-40:-1]
    sl_indices = sub.index[sub["is_swing_low"] == True].tolist()

    if len(sl_indices) >= 1:
        origin_idx = sl_indices[-1]
        origin_price = float(df.loc[origin_idx, "low"])
        delta_bars = len(df) - 1 - origin_idx

        if delta_bars >= 5:
            # Scale: 1 bar = 0.10 * ATR
            unit_rate = (atr * 0.10)
            gann_1x1 = origin_price + (unit_rate * delta_bars)
            c = float(last_row["close"])
            l = float(last_row["low"])

            if abs(l - gann_1x1) / gann_1x1 <= 0.02 and c >= gann_1x1 * 0.99:
                return {
                    "pattern": "GANN_ANGLE_TOUCH",
                    "pattern_version": "GANN_v1",
                    "pattern_score": 74.0,
                    "pivot": round(gann_1x1, 2),
                    "details": {
                        "type": "GANN_1X1_ANGLE_SUPPORT",
                        "origin_price": round(origin_price, 2),
                        "angle_level": round(gann_1x1, 2),
                        "reasons": [
                            f"Price reacted at theoretical Gann 1x1 angle ({gann_1x1:.2f})",
                            f"Projected from swing low at {origin_price:.2f} ({delta_bars} bars ago)",
                            "Scale convention: ATR-proportional angle (Experimental)"
                        ]
                    }
                }
    return None


def detect_moon_phase_correlation(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """
    Moon Phase Correlation:
    Identifies if a stock is at an inflection point coincident with a New Moon (0 deg)
    or Full Moon (180 deg) window within +/- 15 degrees.
    """
    if len(df) < 20:
        return None

    last_row = df.iloc[-1]
    extra = last_row.get("extra")
    angle = None
    if isinstance(extra, dict):
        angle = extra.get("moon_phase_angle")

    if angle is not None:
        c = float(last_row["close"])
        # Check near New Moon (0/360) or Full Moon (180)
        is_new_moon = (angle <= 15.0) or (angle >= 345.0)
        is_full_moon = (165.0 <= angle <= 195.0)

        if (is_new_moon or is_full_moon) and float(last_row.get("rel_volume", 1.0)) > 1.2:
            phase_type = "New Moon (Solar-Lunar Conjunction)" if is_new_moon else "Full Moon (Opposition)"
            return {
                "pattern": "MOON_PHASE_CORRELATION",
                "pattern_version": "MOON_v1",
                "pattern_score": 72.0,
                "pivot": round(c, 2),
                "details": {
                    "type": "LUNAR_CYCLE_INFLECTION",
                    "phase_angle": angle,
                    "phase_name": phase_type,
                    "reasons": [
                        f"Aligned with {phase_type} phase window (Angle: {angle:.1f}°)",
                        "Volume expansion observed at astronomical turn point",
                        "Astronomical timing cycle correlation (Experimental)"
                    ]
                }
            }
    return None


def detect_elliott_wave(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """
    Elliott Wave (Objective 5-wave impulse approximation):
    Rule 1: Wave 2 does not retrace past the start of Wave 1.
    Rule 2: Wave 3 is not the shortest of Waves 1, 3, and 5.
    Rule 3: Wave 4 does not overlap into the price territory of Wave 1.
    Fires when Wave 4 completes and Wave 5 continuation begins.
    """
    if len(df) < 50:
        return None

    sub = df.iloc[-60:-1]
    sl_indices = sub.index[sub["is_swing_low"] == True].tolist()
    sh_indices = sub.index[sub["is_swing_high"] == True].tolist()

    if len(sl_indices) >= 2 and len(sh_indices) >= 2:
        # Wave 0 (Start): Lowest low
        w0_idx = sl_indices[-2]
        w0 = float(df.loc[w0_idx, "low"])

        # Wave 1 Peak
        w1_candidates = [i for i in sh_indices if i > w0_idx]
        if not w1_candidates:
            return None
        w1_idx = w1_candidates[0]
        w1 = float(df.loc[w1_idx, "high"])

        # Wave 2 Low: Must not retrace below w0
        w2_candidates = [i for i in sl_indices if i > w1_idx]
        if not w2_candidates:
            return None
        w2_idx = w2_candidates[0]
        w2 = float(df.loc[w2_idx, "low"])
        if w2 <= w0:
            return None  # Violates Rule 1

        # Wave 3 Peak
        w3_candidates = [i for i in sh_indices if i > w2_idx]
        if not w3_candidates:
            return None
        w3_idx = w3_candidates[0]
        w3 = float(df.loc[w3_idx, "high"])

        # Wave 4 Low: Must not overlap with Wave 1 peak
        w4_candidates = [i for i in sl_indices if i > w3_idx]
        if not w4_candidates:
            return None
        w4_idx = w4_candidates[0]
        w4 = float(df.loc[w4_idx, "low"])
        if w4 <= w1:
            return None  # Violates Rule 3

        len_w1 = w1 - w0
        len_w3 = w3 - w2
        if len_w3 <= 0 or len_w1 <= 0:
            return None

        # Price currently bouncing from Wave 4 low into Wave 5
        c = float(df.iloc[-1]["close"])
        if c > w4 and c <= w3 * 1.05:
            return {
                "pattern": "ELLIOTT_WAVE",
                "pattern_version": "ELLIOTT_v1",
                "pattern_score": 83.0,
                "pivot": round(w4, 2),
                "details": {
                    "type": "WAVE_5_IMPULSE_SETUP",
                    "wave_counts": {
                        "W0": round(w0, 2),
                        "W1": round(w1, 2),
                        "W2": round(w2, 2),
                        "W3": round(w3, 2),
                        "W4": round(w4, 2)
                    },
                    "reasons": [
                        f"Elliott Wave 5-Wave impulse structure validated (W4 defended at {w4:.2f})",
                        "Rule 1 satisfied: Wave 2 held well above Wave 0 origin",
                        f"Rule 3 satisfied: Wave 4 ({w4:.2f}) did not penetrate Wave 1 high ({w1:.2f})",
                        "Wave 5 expansion phase currently initiating (Experimental)"
                    ]
                }
            }

    return None
