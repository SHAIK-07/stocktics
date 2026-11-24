"""
Pattern Detector Registry
Orchestrates all 33 detectors across Phase 1 through Phase 5.
"""

from typing import List, Dict, Any
import pandas as pd

from .phase1 import (
    detect_support_resistance_touch,
    detect_trendline_break,
    detect_breakout,
    detect_market_structure_bos,
    detect_market_structure_choch
)
from .phase2 import (
    detect_ma_crossover,
    detect_rsi_extreme,
    detect_stochastic_cross,
    detect_momentum_roc,
    detect_relative_volume_spike,
    detect_supertrend_flip
)
from .phase3 import (
    detect_candlestick_patterns,
    detect_double_top_bottom,
    detect_vcp,
    detect_flat_base,
    detect_cup_and_handle,
    detect_ipo_base,
    detect_multi_year_base,
    detect_supply_demand_zone,
    detect_fair_value_gap
)
from .phase4 import (
    detect_divergence,
    detect_fibonacci_reaction,
    detect_fibonacci_extension,
    detect_harmonic_pattern,
    detect_heikin_ashi_trend,
    detect_volume_profile_poc_reaction
)
from .phase5 import (
    detect_renko_trend,
    detect_gann_angle_touch,
    detect_moon_phase_correlation,
    detect_elliott_wave
)

ALL_33_PATTERNS = [
    # Phase 1
    "SUPPORT_RESISTANCE_TOUCH", "TRENDLINE_BREAK", "BREAKOUT", "FAILED_BREAKOUT",
    "MARKET_STRUCTURE_BOS", "MARKET_STRUCTURE_CHOCH",
    # Phase 2
    "MA_CROSSOVER", "RSI_EXTREME", "STOCHASTIC_CROSS", "MOMENTUM_ROC",
    "RELATIVE_VOLUME_SPIKE", "SUPERTREND_FLIP",
    # Phase 3
    "BULLISH_ENGULFING", "BEARISH_ENGULFING", "DOJI", "HAMMER", "SHOOTING_STAR",
    "MORNING_STAR", "EVENING_STAR", "DOUBLE_TOP", "DOUBLE_BOTTOM",
    "VCP", "FLAT_BASE", "CUP_AND_HANDLE", "IPO_BASE", "MULTI_YEAR_BASE",
    "SUPPLY_DEMAND_ZONE", "FAIR_VALUE_GAP",
    # Phase 4
    "DIVERGENCE", "FIBONACCI_RETRACEMENT_REACTION", "FIBONACCI_EXTENSION_TARGET",
    "HARMONIC_PATTERN", "HEIKIN_ASHI_TREND", "VOLUME_PROFILE_POC_REACTION",
    # Phase 5
    "RENKO_TREND", "GANN_ANGLE_TOUCH", "MOON_PHASE_CORRELATION", "ELLIOTT_WAVE"
]


def get_registered_patterns() -> List[str]:
    return list(ALL_33_PATTERNS)


def run_all_detectors(df: pd.DataFrame, pattern_configs: Dict[str, Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Runs all 33 detectors against the given dataframe.
    Returns a list of event dictionaries ready for pattern_events table.
    """
    if df.empty or len(df) < 20:
        return []

    events = []

    def get_params(p_name: str) -> Dict[str, Any]:
        cfg = pattern_configs.get(p_name, {})
        params = cfg.get("params", {})
        return params if isinstance(params, dict) else {}

    # --- Phase 1 ---
    ev = detect_support_resistance_touch(df, get_params("SUPPORT_RESISTANCE_TOUCH"))
    if ev: events.append(ev)

    ev = detect_trendline_break(df, get_params("TRENDLINE_BREAK"))
    if ev: events.append(ev)

    ev = detect_breakout(df, get_params("BREAKOUT"))
    if ev: events.append(ev)

    ev = detect_market_structure_bos(df, get_params("MARKET_STRUCTURE_BOS"))
    if ev: events.append(ev)

    ev = detect_market_structure_choch(df, get_params("MARKET_STRUCTURE_CHOCH"))
    if ev: events.append(ev)

    # --- Phase 2 ---
    ev = detect_ma_crossover(df, get_params("MA_CROSSOVER"))
    if ev: events.append(ev)

    ev = detect_rsi_extreme(df, get_params("RSI_EXTREME"))
    if ev: events.append(ev)

    ev = detect_stochastic_cross(df, get_params("STOCHASTIC_CROSS"))
    if ev: events.append(ev)

    ev = detect_momentum_roc(df, get_params("MOMENTUM_ROC"))
    if ev: events.append(ev)

    ev = detect_relative_volume_spike(df, get_params("RELATIVE_VOLUME_SPIKE"))
    if ev: events.append(ev)

    ev = detect_supertrend_flip(df, get_params("SUPERTREND_FLIP"))
    if ev: events.append(ev)

    # --- Phase 3 ---
    candle_res = detect_candlestick_patterns(df, {})
    for _, ev in candle_res.items():
        if ev: events.append(ev)

    dtb_res = detect_double_top_bottom(df, get_params("DOUBLE_BOTTOM"))
    for _, ev in dtb_res.items():
        if ev: events.append(ev)

    ev = detect_vcp(df, get_params("VCP"))
    if ev: events.append(ev)

    ev = detect_flat_base(df, get_params("FLAT_BASE"))
    if ev: events.append(ev)

    ev = detect_cup_and_handle(df, get_params("CUP_AND_HANDLE"))
    if ev: events.append(ev)

    ev = detect_ipo_base(df, get_params("IPO_BASE"))
    if ev: events.append(ev)

    ev = detect_multi_year_base(df, get_params("MULTI_YEAR_BASE"))
    if ev: events.append(ev)

    ev = detect_supply_demand_zone(df, get_params("SUPPLY_DEMAND_ZONE"))
    if ev: events.append(ev)

    ev = detect_fair_value_gap(df, get_params("FAIR_VALUE_GAP"))
    if ev: events.append(ev)

    # --- Phase 4 ---
    ev = detect_divergence(df, get_params("DIVERGENCE"))
    if ev: events.append(ev)

    ev = detect_fibonacci_reaction(df, get_params("FIBONACCI_RETRACEMENT_REACTION"))
    if ev: events.append(ev)

    ev = detect_fibonacci_extension(df, get_params("FIBONACCI_EXTENSION_TARGET"))
    if ev: events.append(ev)

    ev = detect_harmonic_pattern(df, get_params("HARMONIC_PATTERN"))
    if ev: events.append(ev)

    ev = detect_heikin_ashi_trend(df, get_params("HEIKIN_ASHI_TREND"))
    if ev: events.append(ev)

    ev = detect_volume_profile_poc_reaction(df, get_params("VOLUME_PROFILE_POC_REACTION"))
    if ev: events.append(ev)

    # --- Phase 5 (Experimental) ---
    ev = detect_renko_trend(df, get_params("RENKO_TREND"))
    if ev: events.append(ev)

    ev = detect_gann_angle_touch(df, get_params("GANN_ANGLE_TOUCH"))
    if ev: events.append(ev)

    ev = detect_moon_phase_correlation(df, get_params("MOON_PHASE_CORRELATION"))
    if ev: events.append(ev)

    ev = detect_elliott_wave(df, get_params("ELLIOTT_WAVE"))
    if ev: events.append(ev)

    # Attach signal date
    signal_date = str(df.iloc[-1]["date"])[:10]
    for e in events:
        e["date"] = signal_date

    return events
