"""
Phase 2 Detectors — Core Quantitative Indicators as Signals
6. MA_CROSSOVER
7. RSI_EXTREME
8. STOCHASTIC_CROSS
9. MOMENTUM_ROC
10. RELATIVE_VOLUME_SPIKE
11. SUPERTREND_FLIP
"""

import pandas as pd
from typing import Dict, Any, Optional


def detect_ma_crossover(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 50:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]

    # Golden Cross: 50 crossing above 200
    if len(df) >= 200 and pd.notnull(last_row.get("sma200")) and pd.notnull(prev_row.get("sma200")):
        sma50_now, sma200_now = float(last_row["sma50"]), float(last_row["sma200"])
        sma50_prev, sma200_prev = float(prev_row["sma50"]), float(prev_row["sma200"])
        if sma50_prev <= sma200_prev and sma50_now > sma200_now:
            return {
                "pattern": "MA_CROSSOVER",
                "pattern_version": "MA_CROSS_v1",
                "pattern_score": 92.0,
                "pivot": round(sma200_now, 2),
                "details": {
                    "type": "GOLDEN_CROSS_50_200",
                    "sma50": round(sma50_now, 2),
                    "sma200": round(sma200_now, 2),
                    "reasons": [
                        "Classic Golden Cross: 50-day SMA crossed above 200-day SMA",
                        f"50 SMA: {sma50_now:.2f} vs 200 SMA: {sma200_now:.2f}",
                        "Macro institutional accumulation signature"
                    ]
                }
            }

    # Short-term Cross: 20 crossing above 50
    if pd.notnull(last_row.get("sma50")) and pd.notnull(prev_row.get("sma50")):
        sma20_now, sma50_now = float(last_row["sma20"]), float(last_row["sma50"])
        sma20_prev, sma50_prev = float(prev_row["sma20"]), float(prev_row["sma50"])
        if sma20_prev <= sma50_prev and sma20_now > sma50_now:
            return {
                "pattern": "MA_CROSSOVER",
                "pattern_version": "MA_CROSS_v1",
                "pattern_score": 78.0,
                "pivot": round(sma50_now, 2),
                "details": {
                    "type": "MA_CROSS_20_50",
                    "sma20": round(sma20_now, 2),
                    "sma50": round(sma50_now, 2),
                    "reasons": [
                        "Bullish 20-day SMA crossover above 50-day SMA",
                        f"Short-term momentum aligning above medium-term baseline",
                        f"Current Price: {last_row['close']:.2f}"
                    ]
                }
            }

    return None


def detect_rsi_extreme(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 20:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]
    rsi_now = float(last_row.get("rsi14", 50))
    rsi_prev = float(prev_row.get("rsi14", 50))

    # Oversold rebound: Crossed back above 30
    if rsi_prev < 32 and rsi_now >= 32:
        score = 80.0 + min(15.0, (40 - rsi_prev) * 1.5)
        return {
            "pattern": "RSI_EXTREME",
            "pattern_version": "RSI_EXTREME_v1",
            "pattern_score": round(score, 1),
            "pivot": round(float(last_row["close"]), 2),
            "details": {
                "type": "RSI_OVERSOLD_REBOUND",
                "rsi": round(rsi_now, 1),
                "reasons": [
                    f"RSI(14) exited deep oversold territory at {rsi_now:.1f}",
                    f"Prior RSI low: {rsi_prev:.1f}",
                    "Mean-reversion bounce setup active"
                ]
            }
        }

    # Momentum thrust: RSI firmly above 65 with high relative volume
    if rsi_now >= 68 and float(last_row.get("rel_volume", 1.0)) > 1.8:
        return {
            "pattern": "RSI_EXTREME",
            "pattern_version": "RSI_EXTREME_v1",
            "pattern_score": 75.0,
            "pivot": round(float(last_row["close"]), 2),
            "details": {
                "type": "RSI_POWER_MOMENTUM",
                "rsi": round(rsi_now, 1),
                "reasons": [
                    f"High-momentum RSI reading at {rsi_now:.1f}",
                    f"Supported by {last_row.get('rel_volume', 1.0):.1f}x volume surge",
                    "Strong institutional momentum regime"
                ]
            }
        }

    return None


def detect_stochastic_cross(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 20:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]

    k_now, d_now = float(last_row.get("stoch_k", 50)), float(last_row.get("stoch_d", 50))
    k_prev, d_prev = float(prev_row.get("stoch_k", 50)), float(prev_row.get("stoch_d", 50))

    # Bullish cross in lower territory (< 30)
    if k_prev <= d_prev and k_now > d_now and k_now <= 35:
        score = 82.0
        return {
            "pattern": "STOCHASTIC_CROSS",
            "pattern_version": "STOCH_CROSS_v1",
            "pattern_score": round(score, 1),
            "pivot": round(float(last_row["close"]), 2),
            "details": {
                "type": "STOCH_BULLISH_CROSS",
                "stoch_k": round(k_now, 1),
                "stoch_d": round(d_now, 1),
                "reasons": [
                    f"Stochastic %K crossed above %D in oversold zone (%K: {k_now:.1f}, %D: {d_now:.1f})",
                    "Cyclical momentum turning positive from depression zone",
                    f"Price closed at {last_row['close']:.2f}"
                ]
            }
        }
    return None


def detect_momentum_roc(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Rate of Change (ROC) expansion over 14 bars."""
    if len(df) < 25:
        return None

    period = params.get("roc_period", 14)
    c_now = float(df.iloc[-1]["close"])
    c_past = float(df.iloc[-period - 1]["close"])
    roc = ((c_now - c_past) / c_past) * 100.0

    threshold = params.get("roc_threshold", 4.0)
    rvol = float(df.iloc[-1].get("rel_volume", 1.0))

    if roc >= threshold and rvol > 1.3:
        score = min(92.0, 70.0 + (roc * 1.5))
        return {
            "pattern": "MOMENTUM_ROC",
            "pattern_version": "ROC_MOMENTUM_v1",
            "pattern_score": round(score, 1),
            "pivot": round(c_now, 2),
            "details": {
                "type": "BULLISH_ROC_SURGE",
                "roc_pct": round(roc, 2),
                "reasons": [
                    f"14-period Rate of Change (ROC) surging at +{roc:.2f}%",
                    f"Volume confirmation at {rvol:.1f}x relative volume",
                    "High velocity buying thrust"
                ]
            }
        }
    return None


def detect_relative_volume_spike(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 25:
        return None

    last_row = df.iloc[-1]
    rvol = float(last_row.get("rel_volume", 1.0))
    threshold = params.get("rvol_threshold", 2.2)

    if rvol >= threshold:
        close = float(last_row["close"])
        open_p = float(last_row["open"])
        is_bullish = close >= open_p
        score = min(95.0, 75.0 + (rvol * 5.0))
        return {
            "pattern": "RELATIVE_VOLUME_SPIKE",
            "pattern_version": "RVOL_SPIKE_v1",
            "pattern_score": round(score, 1),
            "pivot": round(close, 2),
            "details": {
                "type": "INSTITUTIONAL_VOLUME_SPIKE",
                "rvol": round(rvol, 1),
                "is_bullish": is_bullish,
                "reasons": [
                    f"Unusual volume spike: {rvol:.1f}x the 20-day average",
                    "Clear institutional positioning activity",
                    f"Closed {'green (bullish accumulation)' if is_bullish else 'red (heavy distribution)'} at {close:.2f}"
                ]
            }
        }
    return None


def detect_supertrend_flip(df: pd.DataFrame, params: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    if len(df) < 15:
        return None

    last_row = df.iloc[-1]
    prev_row = df.iloc[-2]

    curr_dir = last_row.get("supertrend_dir")
    prev_dir = prev_row.get("supertrend_dir")

    if prev_dir == "BEAR" and curr_dir == "BULL":
        score = 88.0
        return {
            "pattern": "SUPERTREND_FLIP",
            "pattern_version": "SUPERTREND_FLIP_v1",
            "pattern_score": score,
            "pivot": round(float(last_row["close"]), 2),
            "details": {
                "type": "SUPERTREND_BEAR_TO_BULL",
                "reasons": [
                    "Supertrend indicator flipped from BEAR to BULL regime",
                    f"Confirmed with ATR-band breakout above resistance",
                    f"Current closing price: {last_row['close']:.2f}"
                ]
            }
        }
    return None
