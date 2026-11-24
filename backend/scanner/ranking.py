"""
Composite Ranking Engine (Section 6 Build Spec)
Computes daily composite scores and rolling 5-day weekly rankings.
composite_score =
    0.25 * trend_score          (EMA/Supertrend alignment)
  + 0.20 * rel_strength_score   (percentile rank vs universe)
  + 0.20 * structure_score      (best Phase 1 signal: BOS/CHoCH/breakout quality)
  + 0.20 * top_pattern_score    (best score among patterns with include_in_composite = true)
  + 0.15 * volume_score         (relative volume, normalized 0-100)
"""

import numpy as np
import pandas as pd
from typing import List, Dict, Any, Optional


def compute_security_scores(
    df: pd.DataFrame,
    events: List[Dict[str, Any]],
    pattern_configs: Dict[str, Dict[str, Any]],
    all_rel_strengths: Optional[List[float]] = None
) -> Dict[str, Any]:
    """Computes the 5 sub-scores and composite score for a single security."""
    if df.empty:
        return {
            "composite_score": 50.0,
            "trend_score": 50.0,
            "rel_strength_score": 50.0,
            "structure_score": 50.0,
            "volume_score": 50.0,
            "top_pattern": None,
            "top_pattern_score": 0.0
        }

    last_row = df.iloc[-1]
    c = float(last_row["close"])
    ema20 = float(last_row.get("ema20", c))
    ema50 = float(last_row.get("ema50", c))
    ema200 = float(last_row.get("ema200", c))
    supertrend = last_row.get("supertrend_dir", "BULL")

    # 1. Trend Score (0-100)
    # EMA alignment (c > 20 > 50 > 200) + Supertrend
    trend_pts = 50.0
    if c > ema20: trend_pts += 10.0
    if ema20 > ema50: trend_pts += 15.0
    if ema50 > ema200: trend_pts += 15.0
    if supertrend == "BULL": trend_pts += 10.0
    else: trend_pts -= 20.0
    trend_score = min(100.0, max(0.0, trend_pts))

    # 2. Relative Strength Score (0-100 percentile rank)
    rs_3m = float(last_row.get("rel_strength_3m", 0.0))
    if all_rel_strengths and len(all_rel_strengths) > 1:
        rank = sum(1 for x in all_rel_strengths if x <= rs_3m)
        rel_strength_score = round((rank / len(all_rel_strengths)) * 100.0, 1)
    else:
        rel_strength_score = min(100.0, max(0.0, 50.0 + (rs_3m * 1.5)))

    # 3. Structure Score (0-100 based on Phase 1 signals)
    phase1_patterns = {"SUPPORT_RESISTANCE_TOUCH", "TRENDLINE_BREAK", "BREAKOUT", "MARKET_STRUCTURE_BOS", "MARKET_STRUCTURE_CHOCH"}
    phase1_scores = [e["pattern_score"] for e in events if e.get("pattern") in phase1_patterns]
    structure_score = max(phase1_scores) if phase1_scores else (70.0 if supertrend == "BULL" else 40.0)

    # 4. Top Pattern Score (Only from patterns where include_in_composite == True)
    eligible_events = []
    for e in events:
        p_name = e.get("pattern")
        cfg = pattern_configs.get(p_name, {})
        if cfg.get("include_in_composite", True):
            eligible_events.append(e)

    if eligible_events:
        best_ev = max(eligible_events, key=lambda x: x.get("pattern_score", 0))
        top_pattern = best_ev.get("pattern")
        top_pattern_score = float(best_ev.get("pattern_score", 0))
    else:
        top_pattern = events[0].get("pattern") if events else None
        top_pattern_score = float(events[0].get("pattern_score", 50.0)) if events else 50.0

    # 5. Volume Score (0-100, normalized from rel_volume)
    rvol = float(last_row.get("rel_volume", 1.0))
    volume_score = min(100.0, max(0.0, 30.0 + (rvol * 35.0)))

    # Composite formula
    composite = (
        (0.25 * trend_score) +
        (0.20 * rel_strength_score) +
        (0.20 * structure_score) +
        (0.20 * top_pattern_score) +
        (0.15 * volume_score)
    )

    return {
        "composite_score": round(composite, 1),
        "trend_score": round(trend_score, 1),
        "rel_strength_score": round(rel_strength_score, 1),
        "structure_score": round(structure_score, 1),
        "volume_score": round(volume_score, 1),
        "top_pattern": top_pattern,
        "top_pattern_score": round(top_pattern_score, 1)
    }


def compute_weekly_rolling_rankings(daily_rankings: List[Dict[str, Any]], week_ending: str) -> List[Dict[str, Any]]:
    """
    Computes rolling 5-trading-day aggregation per security for weekly_rankings table.
    """
    df = pd.DataFrame(daily_rankings)
    if df.empty:
        return []

    weekly = []
    for sec_id, group in df.groupby("security_id"):
        scores = group["composite_score"].astype(float).tolist()
        if scores:
            weekly.append({
                "security_id": int(sec_id),
                "week_ending": week_ending,
                "avg_composite_score": round(float(np.mean(scores)), 1),
                "best_composite_score": round(float(np.max(scores)), 1),
                "days_in_window": len(scores)
            })

    return weekly
