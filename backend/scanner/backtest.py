"""
Historical Backtest Engine for Technical Patterns (Section 7 Build Spec)
Evaluates forward returns at +5, +10, +20 trading days from the knowable signal date.
"""

from datetime import datetime
import numpy as np
import pandas as pd
from typing import List, Dict, Any


def backtest_pattern_signals(
    pattern: str,
    pattern_version: str,
    market: str,
    signals: List[Dict[str, Any]],  # List of {security_id, date, signal_index}
    prices_by_sec: Dict[int, pd.DataFrame]
) -> Dict[str, Any]:
    """
    Computes +5d, +10d, +20d forward returns for all occurrences of a pattern.
    """
    fwd_5, fwd_10, fwd_20 = [], [], []

    for sig in signals:
        sec_id = sig["security_id"]
        sig_date = sig["date"]
        df_sec = prices_by_sec.get(sec_id)
        if df_sec is None or df_sec.empty:
            continue

        dates = df_sec["date"].astype(str).str[:10].tolist()
        if sig_date not in dates:
            continue

        idx = dates.index(sig_date)
        c0 = float(df_sec.iloc[idx]["close"])
        if c0 <= 0:
            continue

        if idx + 5 < len(df_sec):
            c5 = float(df_sec.iloc[idx + 5]["close"])
            fwd_5.append((c5 - c0) / c0 * 100.0)

        if idx + 10 < len(df_sec):
            c10 = float(df_sec.iloc[idx + 10]["close"])
            fwd_10.append((c10 - c0) / c0 * 100.0)

        if idx + 20 < len(df_sec):
            c20 = float(df_sec.iloc[idx + 20]["close"])
            fwd_20.append((c20 - c0) / c0 * 100.0)

    sample_size = len(fwd_5)
    win_5 = round(sum(1 for r in fwd_5 if r > 0) / max(1, len(fwd_5)) * 100.0, 1) if fwd_5 else 50.0
    win_10 = round(sum(1 for r in fwd_10 if r > 0) / max(1, len(fwd_10)) * 100.0, 1) if fwd_10 else 50.0
    win_20 = round(sum(1 for r in fwd_20 if r > 0) / max(1, len(fwd_20)) * 100.0, 1) if fwd_20 else 50.0

    avg_5 = round(float(np.mean(fwd_5)), 2) if fwd_5 else 0.0
    avg_10 = round(float(np.mean(fwd_10)), 2) if fwd_10 else 0.0
    avg_20 = round(float(np.mean(fwd_20)), 2) if fwd_20 else 0.0
    med_20 = round(float(np.median(fwd_20)), 2) if fwd_20 else 0.0

    notes = "v0.3 Backtest on active universe. Survivorship-bias not corrected. Results for educational and research transparency only."

    return {
        "pattern": pattern,
        "pattern_version": pattern_version,
        "market": market,
        "run_date": datetime.now().strftime("%Y-%m-%d"),
        "sample_size": sample_size,
        "win_rate_5d": win_5,
        "win_rate_10d": win_10,
        "win_rate_20d": win_20,
        "avg_return_5d": avg_5,
        "avg_return_10d": avg_10,
        "avg_return_20d": avg_20,
        "median_return_20d": med_20,
        "notes": notes
    }


def generate_historical_backtests(
    processed_prices: Dict[int, pd.DataFrame],
    pattern_configs: Dict[str, Dict[str, Any]],
    registered_patterns: List[str]
) -> List[Dict[str, Any]]:
    """
    Scans full 1-year history across universe for signals and computes +5d, +10d, +20d forward returns.
    """
    from backend.scanner.detectors.registry import run_all_detectors

    pattern_signals: Dict[str, List[Dict[str, Any]]] = {p: [] for p in registered_patterns}

    # Step through historical windows for each security
    for sec_id, df_sec in processed_prices.items():
        if df_sec.empty or len(df_sec) < 50:
            continue

        total_bars = len(df_sec)
        # Sample every 5 bars from index 35 to total_bars - 21
        for t in range(35, total_bars - 21, 5):
            window_slice = df_sec.iloc[:t + 1]
            events = run_all_detectors(window_slice, pattern_configs)
            sig_date = str(window_slice.iloc[-1]["date"])[:10]
            for ev in events:
                p = ev.get("pattern")
                if p in pattern_signals:
                    pattern_signals[p].append({
                        "security_id": sec_id,
                        "date": sig_date,
                        "idx": t
                    })

    backtests = []
    for p_name in registered_patterns:
        sigs = pattern_signals.get(p_name, [])
        p_ver = f"{p_name}_v1"
        bt = backtest_pattern_signals(p_name, p_ver, "ALL", sigs, processed_prices)
        backtests.append(bt)

    return backtests

