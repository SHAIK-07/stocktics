"""
Database Access Layer for Stocktics Dual-Market Pattern Scanner
Direct Supabase PostgREST implementation for both local development and production.
"""

import os
import json
import math
import logging
from collections import defaultdict
from pathlib import Path
from typing import List, Dict, Any, Optional
import requests
from dotenv import load_dotenv

# Load workspace .env
env_path = Path(__file__).resolve().parent.parent.parent / ".env"
load_dotenv(env_path)

logger = logging.getLogger("StockticsScannerDB")

SUPABASE_URL = os.getenv("SUPABASE_URL", "").rstrip("/")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY") or os.getenv("SUPABASE_KEY") or os.getenv("SUPABASE_PUBLISHABLE_KEY", "")


class ScannerDB:
    def __init__(self, supabase_url: str = SUPABASE_URL, supabase_key: str = SUPABASE_KEY):
        self.url = supabase_url
        self.key = supabase_key
        self.headers = {
            "apikey": self.key,
            "Authorization": f"Bearer {self.key}",
            "Content-Type": "application/json",
            "Prefer": "return=representation"
        }
        self.is_supabase_ready = False
        self._check_supabase()

    def _check_supabase(self):
        if not self.url or not self.key:
            logger.error("Supabase credentials missing in .env (SUPABASE_URL / SUPABASE_KEY).")
            return

        try:
            r = requests.get(f"{self.url}/rest/v1/securities?limit=1", headers=self.headers, timeout=5)
            if r.status_code == 200:
                self.is_supabase_ready = True
                logger.info("Successfully connected directly to Supabase pattern scanner.")
            else:
                logger.warning(f"Supabase responded with status {r.status_code}: {r.text[:120]}")
        except Exception as e:
            logger.error(f"Error connecting to Supabase: {e}")

    # -------------------------------------------------------------------------
    # SECURITIES
    # -------------------------------------------------------------------------
    def get_securities(self, market: Optional[str] = None, active_only: bool = True) -> List[Dict[str, Any]]:
        """Returns registered tickers directly from Supabase securities table."""
        if not self.is_supabase_ready:
            return []

        query = f"{self.url}/rest/v1/securities?select=*&limit=2500"
        if active_only:
            query += "&is_active=eq.true"
        if market and market.upper() != "ALL":
            query += f"&market=eq.{market.upper()}"
        query += "&order=ticker.asc"

        try:
            res = requests.get(query, headers=self.headers, timeout=12)
            if res.status_code == 200:
                return res.json()
            logger.error(f"Supabase get_securities error: {res.status_code} {res.text[:120]}")
        except Exception as e:
            logger.error(f"Supabase get_securities failed: {e}")
        return []

    def upsert_securities(self, securities: List[Dict[str, Any]]) -> None:
        """Upserts securities list directly into Supabase in safe chunks."""
        if not securities or not self.is_supabase_ready:
            return

        headers = dict(self.headers)
        headers["Prefer"] = "resolution=merge-duplicates"
        chunk_size = 150
        for i in range(0, len(securities), chunk_size):
            chunk = securities[i : i + chunk_size]
            try:
                res = requests.post(f"{self.url}/rest/v1/securities?on_conflict=ticker,market", headers=headers, json=chunk, timeout=20)
                if res.status_code not in (200, 201):
                    logger.error(f"Error upserting securities chunk: {res.status_code} {res.text[:120]}")
            except Exception as e:
                logger.error(f"Error upserting securities to Supabase: {e}")

    # -------------------------------------------------------------------------
    # PATTERN CONFIG
    # -------------------------------------------------------------------------
    def get_pattern_configs(self) -> List[Dict[str, Any]]:
        """Returns pattern configuration metadata directly from Supabase."""
        if not self.is_supabase_ready:
            return []

        try:
            res = requests.get(f"{self.url}/rest/v1/pattern_config?select=*&limit=100&order=pattern.asc", headers=self.headers, timeout=10)
            if res.status_code == 200:
                return res.json()
            logger.error(f"Supabase get_pattern_configs error: {res.status_code} {res.text[:120]}")
        except Exception as e:
            logger.error(f"Supabase get_pattern_configs failed: {e}")
        return []

    def upsert_pattern_configs(self, configs: List[Dict[str, Any]]) -> None:
        """Upserts pattern configs directly into Supabase."""
        if not configs or not self.is_supabase_ready:
            return

        try:
            headers = dict(self.headers)
            headers["Prefer"] = "resolution=merge-duplicates"
            res = requests.post(f"{self.url}/rest/v1/pattern_config?on_conflict=pattern", headers=headers, json=configs, timeout=15)
            if res.status_code not in (200, 201):
                logger.error(f"Error upserting pattern configs to Supabase: {res.status_code} {res.text[:120]}")
        except Exception as e:
            logger.error(f"Error upserting pattern configs to Supabase: {e}")

    # -------------------------------------------------------------------------
    # PRICES & INDICATORS
    # -------------------------------------------------------------------------
    def upsert_daily_prices(self, prices: List[Dict[str, Any]]) -> None:
        """Upserts price bars directly into Supabase daily_prices in safe batches."""
        if not prices or not self.is_supabase_ready:
            return

        headers = dict(self.headers)
        headers["Prefer"] = "resolution=merge-duplicates"
        chunk_size = 200
        for i in range(0, len(prices), chunk_size):
            chunk = prices[i : i + chunk_size]
            try:
                res = requests.post(f"{self.url}/rest/v1/daily_prices?on_conflict=security_id,date", headers=headers, json=chunk, timeout=20)
                if res.status_code not in (200, 201):
                    logger.error(f"Supabase prices upsert error: {res.status_code} {res.text[:120]}")
            except Exception as e:
                logger.error(f"Supabase prices upsert exception: {e}")

    def upsert_daily_indicators(self, indicators: List[Dict[str, Any]]) -> None:
        """Upserts technical indicators directly into Supabase daily_indicators in safe batches."""
        if not indicators or not self.is_supabase_ready:
            return

        headers = dict(self.headers)
        headers["Prefer"] = "resolution=merge-duplicates"
        chunk_size = 200
        for i in range(0, len(indicators), chunk_size):
            chunk = indicators[i : i + chunk_size]
            try:
                res = requests.post(f"{self.url}/rest/v1/daily_indicators?on_conflict=security_id,date", headers=headers, json=chunk, timeout=20)
                if res.status_code not in (200, 201):
                    logger.error(f"Supabase indicators upsert error: {res.status_code} {res.text[:120]}")
            except Exception as e:
                logger.error(f"Supabase indicators upsert exception: {e}")

    # -------------------------------------------------------------------------
    # PATTERN EVENTS
    # -------------------------------------------------------------------------
    def upsert_pattern_events(self, events: List[Dict[str, Any]]) -> None:
        """Upserts detected pattern events directly into Supabase pattern_events in safe batches."""
        if not events or not self.is_supabase_ready:
            return

        headers = dict(self.headers)
        headers["Prefer"] = "resolution=merge-duplicates"
        chunk_size = 150
        for i in range(0, len(events), chunk_size):
            chunk = events[i : i + chunk_size]
            try:
                res = requests.post(f"{self.url}/rest/v1/pattern_events?on_conflict=security_id,date,pattern,pattern_version", headers=headers, json=chunk, timeout=20)
                if res.status_code not in (200, 201):
                    logger.error(f"Supabase pattern events upsert error: {res.status_code} {res.text[:120]}")
            except Exception as e:
                logger.error(f"Supabase pattern events upsert exception: {e}")

    # -------------------------------------------------------------------------
    # RANKINGS
    # -------------------------------------------------------------------------
    def upsert_daily_rankings(self, rankings: List[Dict[str, Any]]) -> None:
        """Upserts daily composite rankings directly into Supabase daily_rankings in safe batches."""
        if not rankings or not self.is_supabase_ready:
            return

        headers = dict(self.headers)
        headers["Prefer"] = "resolution=merge-duplicates"
        chunk_size = 200
        for i in range(0, len(rankings), chunk_size):
            chunk = rankings[i : i + chunk_size]
            try:
                res = requests.post(f"{self.url}/rest/v1/daily_rankings?on_conflict=security_id,date", headers=headers, json=chunk, timeout=20)
                if res.status_code not in (200, 201):
                    logger.error(f"Supabase daily rankings upsert error: {res.status_code} {res.text[:120]}")
            except Exception as e:
                logger.error(f"Supabase daily rankings upsert exception: {e}")

    def upsert_weekly_rankings(self, rankings: List[Dict[str, Any]]) -> None:
        """Upserts rolling weekly composite rankings directly into Supabase weekly_rankings in safe batches."""
        if not rankings or not self.is_supabase_ready:
            return

        headers = dict(self.headers)
        headers["Prefer"] = "resolution=merge-duplicates"
        chunk_size = 200
        for i in range(0, len(rankings), chunk_size):
            chunk = rankings[i : i + chunk_size]
            try:
                res = requests.post(f"{self.url}/rest/v1/weekly_rankings?on_conflict=security_id,week_ending", headers=headers, json=chunk, timeout=20)
                if res.status_code not in (200, 201):
                    logger.error(f"Supabase weekly rankings upsert error: {res.status_code} {res.text[:120]}")
            except Exception as e:
                logger.error(f"Supabase weekly rankings upsert exception: {e}")

    # -------------------------------------------------------------------------
    # BACKTESTS
    # -------------------------------------------------------------------------
    def upsert_pattern_backtests(self, backtests: List[Dict[str, Any]]) -> None:
        """Upserts backtest performance metrics directly into Supabase pattern_backtests."""
        if not backtests or not self.is_supabase_ready:
            return

        try:
            headers = dict(self.headers)
            headers["Prefer"] = "resolution=merge-duplicates"
            res = requests.post(f"{self.url}/rest/v1/pattern_backtests?on_conflict=pattern,pattern_version,market,run_date", headers=headers, json=backtests, timeout=20)
            if res.status_code not in (200, 201):
                logger.error(f"Supabase backtests upsert error: {res.status_code} {res.text[:120]}")
        except Exception as e:
            logger.error(f"Supabase backtests upsert exception: {e}")

    # -------------------------------------------------------------------------
    # QUERY ENDPOINTS SUPPORT (Direct Supabase Views)
    # -------------------------------------------------------------------------
    def get_categories_overview(self, market: Optional[str] = None) -> List[Dict[str, Any]]:
        """
        Returns one entry per enabled pattern with top 5 stock matches and reasons.
        Queries Supabase directly using v_pattern_events_enriched in a single round-trip.
        """
        configs = self.get_pattern_configs()
        if not self.is_supabase_ready:
            return []

        try:
            # Single query fetching recent pattern events enriched with security details
            q_url = f"{self.url}/rest/v1/v_pattern_events_enriched?order=date.desc,pattern_score.desc&limit=2000"
            if market:
                q_url += f"&market=eq.{market.upper()}"

            res = requests.get(q_url, headers=self.headers, timeout=12)
            events = res.json() if res.status_code == 200 else []

            # Group events by pattern
            events_by_pattern = defaultdict(list)
            for ev in events:
                details = ev.get("details") or {}
                if isinstance(details, str):
                    try:
                        details = json.loads(details)
                    except Exception:
                        details = {}
                events_by_pattern[ev["pattern"]].append({
                    "ticker": ev.get("ticker", ""),
                    "name": ev.get("name") or ev.get("ticker", ""),
                    "market": ev.get("market", ""),
                    "exchange": ev.get("exchange", ""),
                    "pattern_score": round(float(ev.get("pattern_score") or 0), 1),
                    "pivot": round(float(ev.get("pivot") or 0), 2) if ev.get("pivot") else None,
                    "date": str(ev.get("date", "")),
                    "reasons": details.get("reasons", [])
                })

            categories = []
            for cfg in configs:
                pattern = cfg.get("pattern")
                matches = events_by_pattern.get(pattern, [])
                categories.append({
                    "pattern": pattern,
                    "display_name": cfg.get("display_name") or pattern,
                    "confidence_tier": cfg.get("confidence_tier", "STANDARD"),
                    "category": cfg.get("category", "STANDARD"),
                    "total_matches": len(matches),
                    "top5": matches[:5]
                })
            return categories

        except Exception as e:
            logger.error(f"Supabase categories overview query failed: {e}")
            return []

    def get_pattern_matches_paginated(
        self,
        pattern: str,
        market: Optional[str] = None,
        page: int = 1,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Full paginated list of current stock matches for a pattern directly from Supabase.
        """
        if not self.is_supabase_ready:
            return {"pattern": pattern, "market": market or "ALL", "page": page, "page_size": page_size, "total": 0, "total_pages": 1, "results": []}

        offset = (page - 1) * page_size
        q_url = f"{self.url}/rest/v1/v_pattern_events_enriched?pattern=eq.{pattern}"
        if market:
            q_url += f"&market=eq.{market.upper()}"
        q_url += f"&order=date.desc,pattern_score.desc&offset={offset}&limit={page_size}"

        try:
            req_headers = dict(self.headers)
            req_headers["Prefer"] = "count=exact"
            res = requests.get(q_url, headers=req_headers, timeout=10)

            rows = []
            total = 0
            if res.status_code in (200, 206):
                rows = res.json()
                content_range = res.headers.get("Content-Range", "")
                if "/" in content_range:
                    try:
                        total = int(content_range.split("/")[1])
                    except (ValueError, IndexError):
                        total = len(rows)
                else:
                    total = len(rows)

            results = []
            for r in rows:
                details = r.get("details") or {}
                if isinstance(details, str):
                    try:
                        details = json.loads(details)
                    except Exception:
                        details = {}
                results.append({
                    "ticker": r.get("ticker", ""),
                    "name": r.get("name") or r.get("ticker", ""),
                    "market": r.get("market", ""),
                    "exchange": r.get("exchange", ""),
                    "pattern_score": round(float(r.get("pattern_score") or 0), 1),
                    "pivot": round(float(r.get("pivot") or 0), 2) if r.get("pivot") else None,
                    "date": str(r.get("date", "")),
                    "reasons": details.get("reasons", [])
                })

            total_pages = math.ceil(total / page_size) if total > 0 else 1
            return {
                "pattern": pattern,
                "market": market or "ALL",
                "page": page,
                "page_size": page_size,
                "total": total,
                "total_pages": total_pages,
                "results": results
            }
        except Exception as e:
            logger.error(f"Supabase pattern matches paginated failed: {e}")
            return {"pattern": pattern, "market": market or "ALL", "page": page, "page_size": page_size, "total": 0, "total_pages": 1, "results": []}

    def get_top_week(self, market: Optional[str] = None, limit: int = 10) -> List[Dict[str, Any]]:
        """
        Reads weekly_rankings from Supabase directly via v_weekly_rankings_full.
        Deduplicates by ticker to ensure 'limit' distinct top performing stocks are returned.
        """
        if not self.is_supabase_ready:
            return []

        try:
            fetch_limit = max(limit * 5, 50)
            q_url = f"{self.url}/rest/v1/v_weekly_rankings_full?order=avg_composite_score.desc&limit={fetch_limit}"
            if market:
                q_url += f"&market=eq.{market.upper()}"

            res = requests.get(q_url, headers=self.headers, timeout=10)
            if res.status_code == 200:
                rows = res.json()
                seen_tickers = set()
                distinct_stocks = []
                for r in rows:
                    t = r.get("ticker", "").strip().upper()
                    if not t or t in seen_tickers:
                        continue
                    seen_tickers.add(t)
                    distinct_stocks.append({
                        "ticker": t,
                        "name": r.get("name") or t,
                        "market": r.get("market", ""),
                        "exchange": r.get("exchange", ""),
                        "week_ending": str(r.get("week_ending", "")),
                        "avg_composite_score": round(float(r.get("avg_composite_score") or 0), 1),
                        "best_composite_score": round(float(r.get("best_composite_score") or 0), 1),
                        "top_pattern": r.get("top_pattern"),
                        "top_pattern_score": round(float(r.get("top_pattern_score")), 1) if r.get("top_pattern_score") else None,
                        "trend_score": round(float(r.get("trend_score")), 1) if r.get("trend_score") else None,
                        "rel_strength_score": round(float(r.get("rel_strength_score")), 1) if r.get("rel_strength_score") else None,
                        "structure_score": round(float(r.get("structure_score")), 1) if r.get("structure_score") else None,
                        "volume_score": round(float(r.get("volume_score")), 1) if r.get("volume_score") else None
                    })
                    if len(distinct_stocks) >= limit:
                        break
                return distinct_stocks
            logger.error(f"Supabase get_top_week error: {res.status_code} {res.text[:120]}")
        except Exception as e:
            logger.error(f"Supabase get_top_week failed: {e}")
        return []

    def get_stock_patterns(self, ticker: str) -> List[Dict[str, Any]]:
        """
        Returns all pattern events for a single stock directly from Supabase via v_stock_patterns_full.
        """
        if not self.is_supabase_ready:
            return []

        clean_ticker = ticker.strip().upper()
        alt_ticker = f"{clean_ticker}.NS" if not clean_ticker.endswith(".NS") else clean_ticker[:-3]

        try:
            q_url = (f"{self.url}/rest/v1/v_stock_patterns_full"
                     f"?or=(ticker.eq.{clean_ticker},ticker.eq.{alt_ticker})"
                     f"&order=date.desc,pattern_score.desc")
            res = requests.get(q_url, headers=self.headers, timeout=10)
            if res.status_code == 200:
                rows = res.json()
                events = []
                for r in rows:
                    details = r.get("details") or {}
                    if isinstance(details, str):
                        try:
                            details = json.loads(details)
                        except Exception:
                            details = {}
                    events.append({
                        "pattern": r.get("pattern"),
                        "pattern_version": r.get("pattern_version"),
                        "display_name": r.get("display_name") or r.get("pattern"),
                        "confidence_tier": r.get("confidence_tier") or "STANDARD",
                        "category": r.get("category") or "STANDARD",
                        "pattern_score": round(float(r.get("pattern_score") or 0), 1),
                        "pivot": round(float(r.get("pivot") or 0), 2) if r.get("pivot") else None,
                        "date": str(r.get("date", "")),
                        "reasons": details.get("reasons", []),
                        "details": details
                    })
                return events
            logger.error(f"Supabase get_stock_patterns error: {res.status_code} {res.text[:120]}")
        except Exception as e:
            logger.error(f"Supabase get_stock_patterns failed: {e}")
        return []

    def get_pattern_backtest(self, pattern: str, market: Optional[str] = None) -> Optional[Dict[str, Any]]:
        """
        Returns historical backtest stats for a pattern directly from Supabase pattern_backtests.
        """
        if not self.is_supabase_ready:
            return None

        try:
            q_url = f"{self.url}/rest/v1/pattern_backtests?select=*&pattern=eq.{pattern}"
            if market:
                q_url += f"&market=eq.{market.upper()}"
            q_url += "&order=run_date.desc&limit=1"

            res = requests.get(q_url, headers=self.headers, timeout=10)
            if res.status_code == 200:
                rows = res.json()
                if rows:
                    return rows[0]
        except Exception as e:
            logger.error(f"Supabase get_pattern_backtest failed: {e}")
        return None


# Global singleton instance
db = ScannerDB()
