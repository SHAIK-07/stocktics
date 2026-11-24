"""
Technical Indicator Layer for Stocktics Pattern Scanner (Section 4 Build Spec)
Calculates all required quantitative indicators and derived features for each security:
- True Range & Wilder's ATR(14)
- SMA(20, 50, 200) & EMA(20, 50, 200)
- Wilder's RSI(14)
- Stochastic %K/%D
- Relative Volume (V_t / SMA20(V))
- Benchmark Relative Strength (1m, 3m, 6m, 12m vs NIFTY 50 / S&P 500)
- Supertrend Direction (BULL / BEAR)
- Swing Highs / Lows (window k, signal knowable at t+k)
- Daily Volume Profile POC approximation
- Anchored VWAP approximation
- Moon phase angle (theta in [0, 360))
"""

import math
from datetime import datetime
import numpy as np
import pandas as pd
from typing import Dict, Any, Optional, Tuple


def calculate_moon_phase_angle(date_val) -> float:
    """
    Calculates the astronomical moon phase angle theta in [0, 360) degrees.
    0 = New Moon, 90 = First Quarter, 180 = Full Moon, 270 = Last Quarter.
    Based on the standard synodic month cycle (29.53058867 days) referenced from known New Moon.
    """
    if isinstance(date_val, str):
        dt = datetime.strptime(date_val[:10], "%Y-%m-%d")
    elif isinstance(date_val, (pd.Timestamp, datetime)):
        dt = date_val
    else:
        return 0.0

    # Reference known New Moon: Jan 11, 2024, 11:57 UTC
    ref_date = datetime(2024, 1, 11, 11, 57)
    delta_days = (dt - ref_date).total_seconds() / 86400.0
    synodic_month = 29.53058867
    cycle_fraction = (delta_days % synodic_month) / synodic_month
    angle = (cycle_fraction * 360.0) % 360.0
    return round(angle, 2)


def compute_indicators(df: pd.DataFrame, benchmark_df: Optional[pd.DataFrame] = None) -> pd.DataFrame:
    """
    Computes all Section 4 indicators for the given OHLCV dataframe.
    Ensures input is sorted by date ascending.
    """
    if df.empty or len(df) < 15:
        return df

    out = df.copy()
    out = out.sort_values(by="date").reset_index(drop=True)

    c = out["close"].astype(float)
    h = out["high"].astype(float)
    l = out["low"].astype(float)
    v = out["volume"].astype(float)

    # 1. True Range & Wilder's ATR(14)
    prev_close = c.shift(1)
    tr1 = h - l
    tr2 = (h - prev_close).abs()
    tr3 = (l - prev_close).abs()
    tr = pd.concat([tr1, tr2, tr3], axis=1).max(axis=1)
    out["tr"] = tr

    # Wilder's smoothing for ATR(14)
    atr = pd.Series(index=out.index, dtype=float)
    if len(tr) >= 14:
        atr.iloc[13] = tr.iloc[:14].mean()
        for i in range(14, len(tr)):
            atr.iloc[i] = ((atr.iloc[i - 1] * 13) + tr.iloc[i]) / 14.0
    out["atr14"] = atr

    # 2. Moving Averages: SMA(20, 50, 200) & EMA(20, 50, 200)
    out["sma20"] = c.rolling(window=20, min_periods=5).mean()
    out["sma50"] = c.rolling(window=50, min_periods=10).mean()
    out["sma200"] = c.rolling(window=200, min_periods=20).mean()

    out["ema20"] = c.ewm(span=20, adjust=False).mean()
    out["ema50"] = c.ewm(span=50, adjust=False).mean()
    out["ema200"] = c.ewm(span=200, adjust=False).mean()

    # 3. Wilder's RSI(14)
    delta = c.diff()
    gain = delta.clip(lower=0)
    loss = (-delta).clip(lower=0)

    avg_gain = pd.Series(index=out.index, dtype=float)
    avg_loss = pd.Series(index=out.index, dtype=float)
    rsi = pd.Series(index=out.index, dtype=float)

    if len(delta) >= 14:
        avg_gain.iloc[13] = gain.iloc[1:14].mean()
        avg_loss.iloc[13] = loss.iloc[1:14].mean()
        for i in range(14, len(delta)):
            avg_gain.iloc[i] = (avg_gain.iloc[i - 1] * 13 + gain.iloc[i]) / 14.0
            avg_loss.iloc[i] = (avg_loss.iloc[i - 1] * 13 + loss.iloc[i]) / 14.0
            if avg_loss.iloc[i] == 0:
                rsi.iloc[i] = 100.0
            else:
                rs = avg_gain.iloc[i] / avg_loss.iloc[i]
                rsi.iloc[i] = 100.0 - (100.0 / (1.0 + rs))
    out["rsi14"] = rsi

    # 4. Stochastic %K and %D (14, 3)
    lowest_low14 = l.rolling(window=14, min_periods=5).min()
    highest_high14 = h.rolling(window=14, min_periods=5).max()
    denom = (highest_high14 - lowest_low14).replace(0, np.nan)
    stoch_k = (100.0 * (c - lowest_low14) / denom).clip(0, 100)
    stoch_d = stoch_k.rolling(window=3, min_periods=1).mean()
    out["stoch_k"] = stoch_k
    out["stoch_d"] = stoch_d

    # 5. Relative Volume (V_t / SMA20(V))
    vol_sma20 = v.rolling(window=20, min_periods=5).mean().replace(0, np.nan)
    out["rel_volume"] = (v / vol_sma20).fillna(1.0).round(2)

    # 6. Relative Strength (vs Benchmark at 1M, 3M, 6M, 12M)
    # 1M ~ 21 bars, 3M ~ 63 bars, 6M ~ 126 bars, 12M ~ 252 bars
    for period_name, bars in [("1m", 21), ("3m", 63), ("6m", 126), ("12m", 252)]:
        stock_ret = c.pct_change(periods=bars) * 100.0
        if benchmark_df is not None and not benchmark_df.empty and len(benchmark_df) >= bars:
            bm_c = benchmark_df["close"].astype(float)
            bm_ret = bm_c.pct_change(periods=bars) * 100.0
            # Align by index or use latest relative
            rel_strength = stock_ret - bm_ret.reindex(out.index, method="ffill")
        else:
            rel_strength = stock_ret
        out[f"rel_strength_{period_name}"] = rel_strength.round(2)

    # 7. Supertrend (Period 10, Multiplier 3.0)
    atr10 = pd.Series(index=out.index, dtype=float)
    if len(tr) >= 10:
        atr10.iloc[9] = tr.iloc[:10].mean()
        for i in range(10, len(tr)):
            atr10.iloc[i] = ((atr10.iloc[i - 1] * 9) + tr.iloc[i]) / 10.0

    hl2 = (h + l) / 2.0
    upper_band = hl2 + (3.0 * atr10)
    lower_band = hl2 - (3.0 * atr10)

    supertrend_dir = ["BULL"] * len(out)
    in_uptrend = True

    for i in range(1, len(out)):
        if c.iloc[i] > upper_band.iloc[i - 1]:
            in_uptrend = True
        elif c.iloc[i] < lower_band.iloc[i - 1]:
            in_uptrend = False
        else:
            if in_uptrend and lower_band.iloc[i] < lower_band.iloc[i - 1]:
                lower_band.iloc[i] = lower_band.iloc[i - 1]
            if not in_uptrend and upper_band.iloc[i] > upper_band.iloc[i - 1]:
                upper_band.iloc[i] = upper_band.iloc[i - 1]

        supertrend_dir[i] = "BULL" if in_uptrend else "BEAR"

    out["supertrend_dir"] = supertrend_dir

    # 8. Swing Highs & Lows (Window k=5, strictly knowable at t+k)
    k = 5
    is_swing_high = [False] * len(out)
    is_swing_low = [False] * len(out)
    swing_high_level = [np.nan] * len(out)
    swing_low_level = [np.nan] * len(out)

    for t in range(k, len(out) - k):
        target_h = h.iloc[t]
        target_l = l.iloc[t]
        left_h = h.iloc[t - k:t]
        right_h = h.iloc[t + 1:t + k + 1]
        left_l = l.iloc[t - k:t]
        right_l = l.iloc[t + 1:t + k + 1]

        knowable_idx = t + k
        if target_h >= left_h.max() and target_h >= right_h.max():
            is_swing_high[knowable_idx] = True
            swing_high_level[knowable_idx] = target_h

        if target_l <= left_l.min() and target_l <= right_l.min():
            is_swing_low[knowable_idx] = True
            swing_low_level[knowable_idx] = target_l

    out["is_swing_high"] = is_swing_high
    out["is_swing_low"] = is_swing_low
    out["swing_high_level"] = swing_high_level
    out["swing_low_level"] = swing_low_level

    # 9. Volume Profile POC (Point of Control) Daily Approximation over lookback
    # Bins the day's range [L, H], assigns volume proportionally, accumulates over 30 days
    extra_data = []
    for idx, row in out.iterrows():
        dt_str = str(row["date"])
        moon_angle = calculate_moon_phase_angle(dt_str)

        # Approximate Volume Profile POC over last 30 bars
        start_idx = max(0, idx - 30)
        window_df = out.iloc[start_idx:idx + 1]
        min_p = window_df["low"].min()
        max_p = window_df["high"].max()

        poc_price = float(row["close"])
        if max_p > min_p and len(window_df) > 5:
            bins = 20
            bin_edges = np.linspace(min_p, max_p, bins + 1)
            bin_vols = np.zeros(bins)
            for _, w_row in window_df.iterrows():
                b_low = w_row["low"]
                b_high = w_row["high"]
                b_vol = w_row["volume"]
                # Distribute bar volume across overlapping price bins
                for b_i in range(bins):
                    e_low = bin_edges[b_i]
                    e_high = bin_edges[b_i + 1]
                    overlap = max(0.0, min(b_high, e_high) - max(b_low, e_low))
                    if b_high > b_low:
                        bin_vols[b_i] += b_vol * (overlap / (b_high - b_low))
            max_bin = int(np.argmax(bin_vols))
            poc_price = round((bin_edges[max_bin] + bin_edges[max_bin + 1]) / 2.0, 2)

        # Anchored VWAP daily approximation from last swing low or 30-day low
        typical_price = (window_df["high"] + window_df["low"] + window_df["close"]) / 3.0
        cum_vp = (typical_price * window_df["volume"]).sum()
        cum_vol = window_df["volume"].sum()
        anchored_vwap = round(cum_vp / cum_vol, 2) if cum_vol > 0 else float(row["close"])

        extra_data.append({
            "poc_price": poc_price,
            "anchored_vwap": anchored_vwap,
            "moon_phase_angle": moon_angle,
            "swing_high": float(row["swing_high_level"]) if not math.isnan(row["swing_high_level"]) else None,
            "swing_low": float(row["swing_low_level"]) if not math.isnan(row["swing_low_level"]) else None
        })

    out["extra"] = extra_data
    return out
