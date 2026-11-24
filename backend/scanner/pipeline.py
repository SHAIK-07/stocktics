"""
Stocktics Dual-Market Pattern Scanner — Daily Batch Pipeline (Section 1 & 9 Build Spec)
Designed to run on schedule as a Render Cron Job.

Execution Steps:
1. Initialize & sync pattern configuration & universe seeds.
2. Fetch benchmark index OHLCV (^NSEI for IN, ^GSPC for US).
3. Fetch daily OHLCV for universe from yfinance, validate & clean.
4. Compute indicator layer (ATR, SMAs, EMAs, RSI, Stochastics, Rel Volume, Supertrend, etc.).
5. Run all 33 pattern detectors across Phase 1 to Phase 5.
6. Compute composite rankings and rolling 5-day weekly rankings.
7. Run backtest evaluations and update pattern_backtests.
8. Persist results idempotently to Supabase (and local sync cache).
"""

import sys
import os
import argparse
import logging
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional

import pandas as pd
import yfinance as yf

# Add root directory to sys.path
root_dir = Path(__file__).resolve().parent.parent.parent
if str(root_dir) not in sys.path:
    sys.path.insert(0, str(root_dir))

from backend.scanner.db import db
from backend.scanner.indicators import compute_indicators
from backend.scanner.detectors.registry import run_all_detectors, get_registered_patterns
from backend.scanner.ranking import compute_security_scores, compute_weekly_rolling_rankings
from backend.scanner.backtest import backtest_pattern_signals, generate_historical_backtests

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("ScannerPipeline")


# Default Universe Seeds (Expanded NIFTY 50 & US Large-Cap Leaders)
DEFAULT_INDIAN_SECURITIES = [
    {"ticker": "RELIANCE.NS", "market": "IN", "exchange": "NSE", "name": "Reliance Industries Ltd.", "sector": "Energy & Petrochemicals"},
    {"ticker": "TCS.NS", "market": "IN", "exchange": "NSE", "name": "Tata Consultancy Services", "sector": "Information Technology"},
    {"ticker": "HDFCBANK.NS", "market": "IN", "exchange": "NSE", "name": "HDFC Bank Ltd.", "sector": "Banking & Financials"},
    {"ticker": "INFY.NS", "market": "IN", "exchange": "NSE", "name": "Infosys Ltd.", "sector": "Information Technology"},
    {"ticker": "ICICIBANK.NS", "market": "IN", "exchange": "NSE", "name": "ICICI Bank Ltd.", "sector": "Banking & Financials"},
    {"ticker": "BHARTIARTL.NS", "market": "IN", "exchange": "NSE", "name": "Bharti Airtel Ltd.", "sector": "Telecommunications"},
    {"ticker": "SBIN.NS", "market": "IN", "exchange": "NSE", "name": "State Bank of India", "sector": "Banking & Financials"},
    {"ticker": "LICI.NS", "market": "IN", "exchange": "NSE", "name": "Life Insurance Corp. of India", "sector": "Insurance"},
    {"ticker": "ITC.NS", "market": "IN", "exchange": "NSE", "name": "ITC Ltd.", "sector": "Consumer Goods"},
    {"ticker": "HINDUNILVR.NS", "market": "IN", "exchange": "NSE", "name": "Hindustan Unilever Ltd.", "sector": "Consumer Goods"},
    {"ticker": "LT.NS", "market": "IN", "exchange": "NSE", "name": "Larsen & Toubro Ltd.", "sector": "Engineering & Infra"},
    {"ticker": "BAJFINANCE.NS", "market": "IN", "exchange": "NSE", "name": "Bajaj Finance Ltd.", "sector": "Financial Services"},
    {"ticker": "HCLTECH.NS", "market": "IN", "exchange": "NSE", "name": "HCL Technologies Ltd.", "sector": "Information Technology"},
    {"ticker": "MARUTI.NS", "market": "IN", "exchange": "NSE", "name": "Maruti Suzuki India Ltd.", "sector": "Automobile"},
    {"ticker": "SUNPHARMA.NS", "market": "IN", "exchange": "NSE", "name": "Sun Pharma Ltd.", "sector": "Pharmaceuticals"},
    {"ticker": "TMPV.NS", "market": "IN", "exchange": "NSE", "name": "Tata Motors Passenger Vehicles Ltd.", "sector": "Automobile"},
    {"ticker": "KOTAKBANK.NS", "market": "IN", "exchange": "NSE", "name": "Kotak Mahindra Bank Ltd.", "sector": "Banking & Financials"},
    {"ticker": "NTPC.NS", "market": "IN", "exchange": "NSE", "name": "NTPC Ltd.", "sector": "Power & Utilities"},
    {"ticker": "ONGC.NS", "market": "IN", "exchange": "NSE", "name": "Oil and Natural Gas Corp.", "sector": "Energy"},
    {"ticker": "TITAN.NS", "market": "IN", "exchange": "NSE", "name": "Titan Company Ltd.", "sector": "Consumer Durables"},
    {"ticker": "ADANIENT.NS", "market": "IN", "exchange": "NSE", "name": "Adani Enterprises Ltd.", "sector": "Metals & Energy"},
    {"ticker": "ADANIPORTS.NS", "market": "IN", "exchange": "NSE", "name": "Adani Ports and SEZ", "sector": "Infrastructure"},
    {"ticker": "AXISBANK.NS", "market": "IN", "exchange": "NSE", "name": "Axis Bank Ltd.", "sector": "Banking & Financials"},
    {"ticker": "ULTRACEMCO.NS", "market": "IN", "exchange": "NSE", "name": "UltraTech Cement Ltd.", "sector": "Materials & Cement"},
    {"ticker": "POWERGRID.NS", "market": "IN", "exchange": "NSE", "name": "Power Grid Corp. of India", "sector": "Power Transmission"},
    {"ticker": "TATASTEEL.NS", "market": "IN", "exchange": "NSE", "name": "Tata Steel Ltd.", "sector": "Metals & Mining"},
    {"ticker": "M&M.NS", "market": "IN", "exchange": "NSE", "name": "Mahindra & Mahindra Ltd.", "sector": "Automobile"},
    {"ticker": "BAJAJFINSV.NS", "market": "IN", "exchange": "NSE", "name": "Bajaj Finserv Ltd.", "sector": "Financial Services"},
    {"ticker": "WIPRO.NS", "market": "IN", "exchange": "NSE", "name": "Wipro Ltd.", "sector": "Information Technology"},
    {"ticker": "COALINDIA.NS", "market": "IN", "exchange": "NSE", "name": "Coal India Ltd.", "sector": "Metals & Mining"},
    {"ticker": "NESTLEIND.NS", "market": "IN", "exchange": "NSE", "name": "Nestle India Ltd.", "sector": "Consumer Goods"},
    {"ticker": "JSWSTEEL.NS", "market": "IN", "exchange": "NSE", "name": "JSW Steel Ltd.", "sector": "Metals & Mining"},
    {"ticker": "GRASIM.NS", "market": "IN", "exchange": "NSE", "name": "Grasim Industries Ltd.", "sector": "Materials"},
    {"ticker": "TECHM.NS", "market": "IN", "exchange": "NSE", "name": "Tech Mahindra Ltd.", "sector": "Information Technology"},
    {"ticker": "INDUSINDBK.NS", "market": "IN", "exchange": "NSE", "name": "IndusInd Bank Ltd.", "sector": "Banking"},
    {"ticker": "CIPLA.NS", "market": "IN", "exchange": "NSE", "name": "Cipla Ltd.", "sector": "Pharmaceuticals"},
    {"ticker": "ASIANPAINT.NS", "market": "IN", "exchange": "NSE", "name": "Asian Paints Ltd.", "sector": "Consumer Goods"},
    {"ticker": "HDFCLIFE.NS", "market": "IN", "exchange": "NSE", "name": "HDFC Life Insurance Co.", "sector": "Insurance"},
    {"ticker": "SBILIFE.NS", "market": "IN", "exchange": "NSE", "name": "SBI Life Insurance Co.", "sector": "Insurance"},
    {"ticker": "DRREDDY.NS", "market": "IN", "exchange": "NSE", "name": "Dr. Reddy's Laboratories", "sector": "Pharmaceuticals"},
    {"ticker": "DIVISLAB.NS", "market": "IN", "exchange": "NSE", "name": "Divi's Laboratories", "sector": "Pharmaceuticals"},
    {"ticker": "EICHERMOT.NS", "market": "IN", "exchange": "NSE", "name": "Eicher Motors Ltd.", "sector": "Automobile"},
    {"ticker": "HINDALCO.NS", "market": "IN", "exchange": "NSE", "name": "Hindalco Industries Ltd.", "sector": "Metals & Mining"},
    {"ticker": "BPCL.NS", "market": "IN", "exchange": "NSE", "name": "Bharat Petroleum Corp.", "sector": "Energy & Refining"},
    {"ticker": "APOLLOHOSP.NS", "market": "IN", "exchange": "NSE", "name": "Apollo Hospitals Enterprise", "sector": "Healthcare"},
    {"ticker": "BRITANNIA.NS", "market": "IN", "exchange": "NSE", "name": "Britannia Industries Ltd.", "sector": "Consumer Goods"},
    {"ticker": "HEROMOTOCO.NS", "market": "IN", "exchange": "NSE", "name": "Hero MotoCorp Ltd.", "sector": "Automobile"},
    {"ticker": "SHRIRAMFIN.NS", "market": "IN", "exchange": "NSE", "name": "Shriram Finance Ltd.", "sector": "Financial Services"},
    {"ticker": "BEL.NS", "market": "IN", "exchange": "NSE", "name": "Bharat Electronics Ltd.", "sector": "Defence & Aerospace"},
    {"ticker": "TRENT.NS", "market": "IN", "exchange": "NSE", "name": "Trent Ltd.", "sector": "Retail & Consumer"},
    {"ticker": "TATACONSUM.NS", "market": "IN", "exchange": "NSE", "name": "Tata Consumer Products", "sector": "Consumer Goods"}
]

DEFAULT_US_SECURITIES = [
    {"ticker": "AAPL", "market": "US", "exchange": "NASDAQ", "name": "Apple Inc.", "sector": "Technology"},
    {"ticker": "MSFT", "market": "US", "exchange": "NASDAQ", "name": "Microsoft Corporation", "sector": "Technology"},
    {"ticker": "NVDA", "market": "US", "exchange": "NASDAQ", "name": "NVIDIA Corporation", "sector": "Semiconductors"},
    {"ticker": "AMZN", "market": "US", "exchange": "NASDAQ", "name": "Amazon.com Inc.", "sector": "Consumer Discretionary"},
    {"ticker": "GOOGL", "market": "US", "exchange": "NASDAQ", "name": "Alphabet Inc.", "sector": "Communication Services"},
    {"ticker": "META", "market": "US", "exchange": "NASDAQ", "name": "Meta Platforms Inc.", "sector": "Communication Services"},
    {"ticker": "TSLA", "market": "US", "exchange": "NASDAQ", "name": "Tesla Inc.", "sector": "Automotive & Energy"},
    {"ticker": "BRK-B", "market": "US", "exchange": "NYSE", "name": "Berkshire Hathaway Inc.", "sector": "Financials"},
    {"ticker": "AVGO", "market": "US", "exchange": "NASDAQ", "name": "Broadcom Inc.", "sector": "Semiconductors"},
    {"ticker": "JPM", "market": "US", "exchange": "NYSE", "name": "JPMorgan Chase & Co.", "sector": "Financials"},
    {"ticker": "LLY", "market": "US", "exchange": "NYSE", "name": "Eli Lilly and Company", "sector": "Healthcare"},
    {"ticker": "V", "market": "US", "exchange": "NYSE", "name": "Visa Inc.", "sector": "Financials"},
    {"ticker": "UNH", "market": "US", "exchange": "NYSE", "name": "UnitedHealth Group Inc.", "sector": "Healthcare"},
    {"ticker": "MA", "market": "US", "exchange": "NYSE", "name": "Mastercard Incorporated", "sector": "Financials"},
    {"ticker": "XOM", "market": "US", "exchange": "NYSE", "name": "Exxon Mobil Corporation", "sector": "Energy"},
    {"ticker": "COST", "market": "US", "exchange": "NASDAQ", "name": "Costco Wholesale Corp.", "sector": "Consumer Staples"},
    {"ticker": "HD", "market": "US", "exchange": "NYSE", "name": "The Home Depot Inc.", "sector": "Consumer Discretionary"},
    {"ticker": "PG", "market": "US", "exchange": "NYSE", "name": "Procter & Gamble Company", "sector": "Consumer Staples"},
    {"ticker": "JNJ", "market": "US", "exchange": "NYSE", "name": "Johnson & Johnson", "sector": "Healthcare"},
    {"ticker": "WMT", "market": "US", "exchange": "NYSE", "name": "Walmart Inc.", "sector": "Consumer Staples"},
    {"ticker": "BAC", "market": "US", "exchange": "NYSE", "name": "Bank of America Corp.", "sector": "Financials"},
    {"ticker": "ORCL", "market": "US", "exchange": "NYSE", "name": "Oracle Corporation", "sector": "Technology"},
    {"ticker": "ABBV", "market": "US", "exchange": "NYSE", "name": "AbbVie Inc.", "sector": "Healthcare"},
    {"ticker": "CRM", "market": "US", "exchange": "NYSE", "name": "Salesforce Inc.", "sector": "Technology"},
    {"ticker": "NFLX", "market": "US", "exchange": "NASDAQ", "name": "Netflix Inc.", "sector": "Communication Services"},
    {"ticker": "AMD", "market": "US", "exchange": "NASDAQ", "name": "Advanced Micro Devices", "sector": "Semiconductors"},
    {"ticker": "QCOM", "market": "US", "exchange": "NASDAQ", "name": "QUALCOMM Incorporated", "sector": "Semiconductors"},
    {"ticker": "KO", "market": "US", "exchange": "NYSE", "name": "The Coca-Cola Company", "sector": "Consumer Staples"},
    {"ticker": "PEP", "market": "US", "exchange": "NASDAQ", "name": "PepsiCo Inc.", "sector": "Consumer Staples"},
    {"ticker": "LIN", "market": "US", "exchange": "NYSE", "name": "Linde plc", "sector": "Materials"},
    {"ticker": "ADBE", "market": "US", "exchange": "NASDAQ", "name": "Adobe Inc.", "sector": "Technology"},
    {"ticker": "DIS", "market": "US", "exchange": "NYSE", "name": "The Walt Disney Company", "sector": "Communication Services"},
    {"ticker": "INTC", "market": "US", "exchange": "NASDAQ", "name": "Intel Corporation", "sector": "Semiconductors"},
    {"ticker": "TXN", "market": "US", "exchange": "NASDAQ", "name": "Texas Instruments Inc.", "sector": "Semiconductors"},
    {"ticker": "PLTR", "market": "US", "exchange": "NYSE", "name": "Palantir Technologies Inc.", "sector": "Technology"},
    {"ticker": "CSCO", "market": "US", "exchange": "NASDAQ", "name": "Cisco Systems Inc.", "sector": "Technology"},
    {"ticker": "MCD", "market": "US", "exchange": "NYSE", "name": "McDonald's Corporation", "sector": "Consumer Discretionary"},
    {"ticker": "NKE", "market": "US", "exchange": "NYSE", "name": "NIKE Inc.", "sector": "Consumer Discretionary"},
    {"ticker": "PFE", "market": "US", "exchange": "NYSE", "name": "Pfizer Inc.", "sector": "Healthcare"},
    {"ticker": "CVX", "market": "US", "exchange": "NYSE", "name": "Chevron Corporation", "sector": "Energy"},
    {"ticker": "MRK", "market": "US", "exchange": "NYSE", "name": "Merck & Co. Inc.", "sector": "Healthcare"},
    {"ticker": "WFC", "market": "US", "exchange": "NYSE", "name": "Wells Fargo & Company", "sector": "Financials"},
    {"ticker": "IBM", "market": "US", "exchange": "NYSE", "name": "International Business Machines", "sector": "Technology"},
    {"ticker": "MS", "market": "US", "exchange": "NYSE", "name": "Morgan Stanley", "sector": "Financials"},
    {"ticker": "GS", "market": "US", "exchange": "NYSE", "name": "The Goldman Sachs Group Inc.", "sector": "Financials"},
    {"ticker": "UBER", "market": "US", "exchange": "NYSE", "name": "Uber Technologies Inc.", "sector": "Technology"},
    {"ticker": "GE", "market": "US", "exchange": "NYSE", "name": "GE Aerospace", "sector": "Industrials"},
    {"ticker": "BA", "market": "US", "exchange": "NYSE", "name": "The Boeing Company", "sector": "Aerospace & Defense"},
    {"ticker": "CAT", "market": "US", "exchange": "NYSE", "name": "Caterpillar Inc.", "sector": "Industrials"},
    {"ticker": "AMAT", "market": "US", "exchange": "NASDAQ", "name": "Applied Materials Inc.", "sector": "Semiconductors"},
    {"ticker": "NOW", "market": "US", "exchange": "NYSE", "name": "ServiceNow Inc.", "sector": "Technology"},
    {"ticker": "INTU", "market": "US", "exchange": "NASDAQ", "name": "Intuit Inc.", "sector": "Technology"},
    {"ticker": "BKNG", "market": "US", "exchange": "NASDAQ", "name": "Booking Holdings Inc.", "sector": "Consumer Discretionary"},
    {"ticker": "ISRG", "market": "US", "exchange": "NASDAQ", "name": "Intuitive Surgical Inc.", "sector": "Healthcare"},
    {"ticker": "VRTX", "market": "US", "exchange": "NASDAQ", "name": "Vertex Pharmaceuticals", "sector": "Healthcare"},
    {"ticker": "SPGI", "market": "US", "exchange": "NYSE", "name": "S&P Global Inc.", "sector": "Financials"},
    {"ticker": "ACN", "market": "US", "exchange": "NYSE", "name": "Accenture plc", "sector": "Information Technology"},
    {"ticker": "BLK", "market": "US", "exchange": "NYSE", "name": "BlackRock Inc.", "sector": "Financials"},
    {"ticker": "PANW", "market": "US", "exchange": "NASDAQ", "name": "Palo Alto Networks Inc.", "sector": "Cybersecurity"},
    {"ticker": "MU", "market": "US", "exchange": "NASDAQ", "name": "Micron Technology Inc.", "sector": "Semiconductors"}
]


def ensure_seed_data():
    """Ensures pattern configuration and universe securities exist in DB."""
    existing_sec = db.get_securities()
    if not existing_sec:
        logger.info("Seeding initial universe of Indian and US securities...")
        db.upsert_securities(DEFAULT_INDIAN_SECURITIES + DEFAULT_US_SECURITIES)

    existing_cfg = db.get_pattern_configs()
    if not existing_cfg or len(existing_cfg) < 33:
        logger.info("Seeding pattern configurations for all 33 detectors...")
        # Import default configs from SQL or structure
        configs = []
        for p in get_registered_patterns():
            is_exp = p in ["RENKO_TREND", "GANN_ANGLE_TOUCH", "MOON_PHASE_CORRELATION", "ELLIOTT_WAVE"]
            disp_name = p.replace("_", " ").title()
            configs.append({
                "pattern": p,
                "pattern_version": f"{p}_v1",
                "display_name": disp_name,
                "category": "EXPERIMENTAL" if is_exp else "STANDARD",
                "is_enabled": True,
                "confidence_tier": "EXPERIMENTAL" if is_exp else "STANDARD",
                "include_in_composite": not is_exp,
                "params": {}
            })
        db.upsert_pattern_configs(configs)


def fetch_benchmark_data() -> Dict[str, pd.DataFrame]:
    """Fetches benchmark OHLCV for relative strength computation."""
    benchmarks = {}
    for market, sym in [("IN", "^NSEI"), ("US", "^GSPC")]:
        try:
            logger.info(f"Fetching benchmark data for {market} ({sym})...")
            df = yf.download(sym, period="1y", interval="1d", progress=False)
            if not df.empty:
                if isinstance(df.columns, pd.MultiIndex):
                    df.columns = df.columns.get_level_values(0)
                df = df.reset_index()
                df.columns = [str(c).lower() for c in df.columns]
                benchmarks[market] = df
        except Exception as e:
            logger.warning(f"Error fetching benchmark {sym}: {e}")
    return benchmarks


def run_pipeline(market_filter: Optional[str] = None, limit_securities: Optional[int] = None, run_backtest: bool = True):
    """Executes the daily pattern scanner pipeline."""
    start_time = datetime.now()
    if market_filter and market_filter.upper() in ("ALL", "NONE", ""):
        market_filter = None
    logger.info("=== Starting Stocktics Dual-Market Pattern Scanner Daily Pipeline ===")

    ensure_seed_data()
    pattern_configs = {c["pattern"]: c for c in db.get_pattern_configs()}

    securities = db.get_securities(market=market_filter, active_only=True)
    if limit_securities:
        securities = securities[:limit_securities]

    logger.info(f"Loaded {len(securities)} active securities to scan.")
    benchmarks = fetch_benchmark_data()

    processed_prices: Dict[int, pd.DataFrame] = {}
    all_events: List[Dict[str, Any]] = []
    all_daily_rankings: List[Dict[str, Any]] = []
    rs_3m_values: List[float] = []

from concurrent.futures import ThreadPoolExecutor, as_completed

def _process_single_security(sec: Dict[str, Any], bm_df: Optional[pd.DataFrame], pattern_configs: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Worker task: ingests OHLCV, computes indicators, detects patterns for a single security."""
    sec_id = sec["id"]
    ticker = sec["ticker"]
    market = sec["market"]

    try:
        raw_df = yf.download(ticker, period="8mo", interval="1d", progress=False)
        if raw_df is None or raw_df.empty or len(raw_df) < 25:
            return None

        if isinstance(raw_df.columns, pd.MultiIndex):
            raw_df.columns = raw_df.columns.get_level_values(0)
        raw_df = raw_df.reset_index()
        raw_df.columns = [str(c).lower() for c in raw_df.columns]

        # Clean and validate
        raw_df = raw_df.dropna(subset=["close"]).sort_values(by="date").reset_index(drop=True)
        raw_df["date"] = pd.to_datetime(raw_df["date"]).dt.strftime("%Y-%m-%d")

        ind_df = compute_indicators(raw_df, benchmark_df=bm_df)
        if ind_df.empty:
            return None

        rs_3m = float(ind_df.iloc[-1]["rel_strength_3m"]) if "rel_strength_3m" in ind_df.columns and pd.notnull(ind_df.iloc[-1]["rel_strength_3m"]) else 0.0

        # Extract latest 5 bars to keep payload small and fast for daily updates
        price_records = []
        ind_records = []
        for _, row in ind_df.iloc[-5:].iterrows():
            price_records.append({
                "security_id": sec_id,
                "date": row["date"],
                "open": round(float(row["open"]), 2),
                "high": round(float(row["high"]), 2),
                "low": round(float(row["low"]), 2),
                "close": round(float(row["close"]), 2),
                "adj_close": round(float(row.get("adj close", row["close"])), 2),
                "volume": int(row["volume"]) if pd.notnull(row["volume"]) else 0,
                "data_quality": "VALID"
            })
            ind_records.append({
                "security_id": sec_id,
                "date": row["date"],
                "atr14": round(float(row["atr14"]), 2) if pd.notnull(row.get("atr14")) else None,
                "sma20": round(float(row["sma20"]), 2) if pd.notnull(row.get("sma20")) else None,
                "sma50": round(float(row["sma50"]), 2) if pd.notnull(row.get("sma50")) else None,
                "sma200": round(float(row["sma200"]), 2) if pd.notnull(row.get("sma200")) else None,
                "ema20": round(float(row["ema20"]), 2) if pd.notnull(row.get("ema20")) else None,
                "ema50": round(float(row["ema50"]), 2) if pd.notnull(row.get("ema50")) else None,
                "ema200": round(float(row["ema200"]), 2) if pd.notnull(row.get("ema200")) else None,
                "rsi14": round(float(row["rsi14"]), 1) if pd.notnull(row.get("rsi14")) else None,
                "stoch_k": round(float(row["stoch_k"]), 1) if pd.notnull(row.get("stoch_k")) else None,
                "stoch_d": round(float(row["stoch_d"]), 1) if pd.notnull(row.get("stoch_d")) else None,
                "rel_volume": round(float(row["rel_volume"]), 2) if pd.notnull(row.get("rel_volume")) else None,
                "rel_strength_1m": round(float(row["rel_strength_1m"]), 2) if pd.notnull(row.get("rel_strength_1m")) else None,
                "rel_strength_3m": round(float(row["rel_strength_3m"]), 2) if pd.notnull(row.get("rel_strength_3m")) else None,
                "rel_strength_6m": round(float(row["rel_strength_6m"]), 2) if pd.notnull(row.get("rel_strength_6m")) else None,
                "rel_strength_12m": round(float(row["rel_strength_12m"]), 2) if pd.notnull(row.get("rel_strength_12m")) else None,
                "supertrend_dir": row.get("supertrend_dir", "BULL"),
                "extra": row.get("extra", {})
            })

        # Run 33 pattern detectors
        detected = run_all_detectors(ind_df, pattern_configs)
        sec_events = []
        for ev in detected:
            sec_events.append({
                "security_id": sec_id,
                "date": ev["date"],
                "pattern": ev["pattern"],
                "pattern_version": ev["pattern_version"],
                "pattern_score": ev["pattern_score"],
                "pivot": ev.get("pivot"),
                "details": ev.get("details", {})
            })

        return {
            "sec_id": sec_id,
            "ticker": ticker,
            "market": market,
            "ind_df": ind_df,
            "price_records": price_records,
            "ind_records": ind_records,
            "events": sec_events,
            "rs_3m": rs_3m,
            "latest_date": str(ind_df.iloc[-1]["date"])[:10]
        }
    except Exception as e:
        logger.warning(f"Error processing {ticker}: {e}")
        return None


def run_pipeline(market_filter: Optional[str] = None, limit_securities: Optional[int] = None, run_backtest: bool = False, max_workers: int = 15):
    """
    Executes the daily pattern scanner pipeline in parallel across universe securities.
    Designed for fast, non-market hours daily scans across 1,000+ US securities.
    """
    start_time = datetime.now()
    if market_filter and market_filter.upper() in ("ALL", "NONE", ""):
        market_filter = None
    logger.info(f"=== Starting Stocktics Pattern Scanner Daily Pipeline (Market: {market_filter or 'ALL'}) ===")

    ensure_seed_data()
    pattern_configs = {c["pattern"]: c for c in db.get_pattern_configs()}

    securities = db.get_securities(market=market_filter, active_only=True)
    if limit_securities:
        securities = securities[:limit_securities]

    total_secs = len(securities)
    logger.info(f"Loaded {total_secs} active securities to scan with {max_workers} parallel workers.")
    benchmarks = fetch_benchmark_data()

    # Step A: Parallel Ingestion, Indicator Computation & Pattern Detection
    processed_results: List[Dict[str, Any]] = []
    processed_prices: Dict[int, pd.DataFrame] = {}

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        future_map = {}
        for sec in securities:
            m = sec["market"]
            bm_df = benchmarks.get(m)
            future = executor.submit(_process_single_security, sec, bm_df, pattern_configs)
            future_map[future] = sec["ticker"]

        completed_count = 0
        for future in as_completed(future_map):
            completed_count += 1
            ticker = future_map[future]
            try:
                res = future.result()
                if res:
                    processed_results.append(res)
                    processed_prices[res["sec_id"]] = res["ind_df"]
                if completed_count % 100 == 0 or completed_count == total_secs:
                    logger.info(f"Progress: {completed_count}/{total_secs} securities processed ({len(processed_results)} valid).")
            except Exception as e:
                logger.warning(f"Worker exception for {ticker}: {e}")

    logger.info(f"Parallel scanning completed: {len(processed_results)}/{total_secs} valid dataframes.")
    if not processed_results:
        logger.warning("No securities successfully processed.")
        return {"status": "empty", "securities_scanned": 0, "duration_seconds": 0}

    # Step B: Score Rankings and Prepare Batches
    rs_3m_values = [r["rs_3m"] for r in processed_results if r["rs_3m"] is not None]
    all_prices: List[Dict[str, Any]] = []
    all_indicators: List[Dict[str, Any]] = []
    all_events: List[Dict[str, Any]] = []
    all_daily_rankings: List[Dict[str, Any]] = []
    latest_date_str = processed_results[0]["latest_date"]

    for r in processed_results:
        all_prices.extend(r["price_records"])
        all_indicators.extend(r["ind_records"])
        all_events.extend(r["events"])

        ranking = compute_security_scores(r["ind_df"], r["events"], pattern_configs, all_rel_strengths=rs_3m_values)
        all_daily_rankings.append({
            "security_id": r["sec_id"],
            "date": r["latest_date"],
            "composite_score": ranking["composite_score"],
            "trend_score": ranking["trend_score"],
            "rel_strength_score": ranking["rel_strength_score"],
            "volume_score": ranking["volume_score"],
            "structure_score": ranking["structure_score"],
            "top_pattern": ranking["top_pattern"],
            "top_pattern_score": ranking["top_pattern_score"]
        })
        latest_date_str = r["latest_date"]

    # Step C: Bulk DB Upserts (Batched & Chunked)
    logger.info(f"Flushing {len(all_prices)} price bars and {len(all_indicators)} indicator bars to database...")
    db.upsert_daily_prices(all_prices)
    db.upsert_daily_indicators(all_indicators)

    if all_events:
        logger.info(f"Flushing {len(all_events)} detected pattern events to database...")
        db.upsert_pattern_events(all_events)

    if all_daily_rankings:
        logger.info(f"Flushing {len(all_daily_rankings)} daily composite rankings to database...")
        db.upsert_daily_rankings(all_daily_rankings)

        # Rolling 5-day weekly rankings
        weekly_rankings = compute_weekly_rolling_rankings(all_daily_rankings, week_ending=latest_date_str)
        if weekly_rankings:
            db.upsert_weekly_rankings(weekly_rankings)
            logger.info(f"Persisted {len(weekly_rankings)} rolling weekly rankings.")

    # Step D: Optional Backtest Engine
    if run_backtest and processed_prices:
        logger.info("Running deep historical backtest evaluations...")
        backtest_records = generate_historical_backtests(processed_prices, pattern_configs, get_registered_patterns())
        if backtest_records:
            db.upsert_pattern_backtests(backtest_records)
            logger.info(f"Persisted {len(backtest_records)} pattern backtests.")

    duration = (datetime.now() - start_time).total_seconds()
    logger.info(f"=== Pattern Scanner Pipeline Completed Successfully in {duration:.1f}s ===")
    return {
        "status": "success",
        "market": market_filter or "ALL",
        "securities_scanned": len(processed_results),
        "patterns_evaluated": 33,
        "total_events_detected": len(all_events),
        "duration_seconds": round(duration, 1)
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Stocktics Pattern Scanner Daily Batch Pipeline")
    parser.add_argument("--market", type=str, default=None, help="Market filter: IN or US")
    parser.add_argument("--limit", type=int, default=None, help="Limit number of securities scanned (for testing)")
    parser.add_argument("--no-backtest", action="store_true", help="Skip backtest calculation")
    args = parser.parse_args()

    run_pipeline(market_filter=args.market, limit_securities=args.limit, run_backtest=not args.no_backtest)
