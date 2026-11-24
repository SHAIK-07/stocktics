"""
Large-Cap Universe Synchronizer & Market-Cap Filter
Stocktics Institutional Universe Management Script

Fetches current market capitalization and company profile data via yfinance,
filters against large-cap thresholds, and automatically syncs/upserts them
into the Stocktics Database (Supabase or local fallback SQLite).

Thresholds:
- Indian Equities (IN): Market Cap >= ₹20,000 Crore (~$2.4B USD)
- US Equities (US):     Market Cap >= $10 Billion USD
"""

import os
import sys
import logging
from pathlib import Path
from typing import List, Dict, Any
import yfinance as yf

# Add root directory to sys.path
root_dir = Path(__file__).resolve().parent.parent.parent
if str(root_dir) not in sys.path:
    sys.path.insert(0, str(root_dir))

from backend.scanner.db import db

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("UniverseManager")

# Curated candidates for continuous tracking
CURATED_IN_CANDIDATES = [
    "RELIANCE.NS", "TCS.NS", "HDFCBANK.NS", "INFY.NS", "ICICIBANK.NS",
    "BHARTIARTL.NS", "SBIN.NS", "LICI.NS", "ITC.NS", "HINDUNILVR.NS",
    "LT.NS", "BAJFINANCE.NS", "HCLTECH.NS", "MARUTI.NS", "SUNPHARMA.NS",
    "TMPV.NS", "KOTAKBANK.NS", "NTPC.NS", "ONGC.NS", "TITAN.NS",
    "ADANIENT.NS", "ADANIPORTS.NS", "AXISBANK.NS", "ULTRACEMCO.NS", "POWERGRID.NS",
    "TATASTEEL.NS", "M&M.NS", "BAJAJFINSV.NS", "WIPRO.NS", "COALINDIA.NS",
    "NESTLEIND.NS", "JSWSTEEL.NS", "GRASIM.NS", "TECHM.NS", "INDUSINDBK.NS",
    "CIPLA.NS", "ASIANPAINT.NS", "HDFCLIFE.NS", "SBILIFE.NS", "DRREDDY.NS",
    "DIVISLAB.NS", "EICHERMOT.NS", "HINDALCO.NS", "BPCL.NS", "APOLLOHOSP.NS",
    "BRITANNIA.NS", "HEROMOTOCO.NS", "SHRIRAMFIN.NS", "BEL.NS", "TRENT.NS", "TATACONSUM.NS"
]

CURATED_US_CANDIDATES = [
    "AAPL", "MSFT", "NVDA", "AMZN", "GOOGL", "META", "TSLA", "BRK-B", "AVGO", "JPM",
    "LLY", "V", "UNH", "MA", "XOM", "COST", "HD", "PG", "JNJ", "WMT",
    "BAC", "ORCL", "ABBV", "CRM", "NFLX", "AMD", "QCOM", "KO", "PEP", "LIN",
    "ADBE", "DIS", "INTC", "TXN", "PLTR", "CSCO", "MCD", "NKE", "PFE", "CVX",
    "MRK", "WFC", "IBM", "MS", "GS", "UBER", "GE", "BA", "CAT", "AMAT",
    "NOW", "INTU", "BKNG", "ISRG", "VRTX", "SPGI", "ACN", "BLK", "PANW", "MU"
]


def audit_and_sync_securities(min_in_mcap_cr: float = 20000.0, min_us_mcap_b: float = 10.0):
    """
    Validates candidates via yfinance, applies market cap thresholds, and upserts into database.
    """
    logger.info("=== Starting Large-Cap Universe Audit & Database Sync ===")
    verified_securities: List[Dict[str, Any]] = []

    # 1. Audit Indian Equities
    logger.info(f"Auditing {len(CURATED_IN_CANDIDATES)} Indian large-cap candidates...")
    for ticker in CURATED_IN_CANDIDATES:
        try:
            t = yf.Ticker(ticker)
            info = t.info or {}
            mcap = info.get("marketCap") or 0
            mcap_cr = mcap / 10000000.0  # Convert to Crores INR

            name = info.get("shortName") or info.get("longName") or ticker
            sector = info.get("sector") or "Diversified"
            exchange = "NSE"

            if mcap_cr >= min_in_mcap_cr or mcap == 0:
                verified_securities.append({
                    "ticker": ticker,
                    "market": "IN",
                    "exchange": exchange,
                    "name": name,
                    "sector": sector,
                    "is_active": True
                })
                logger.info(f"  [PASS] {ticker}: ₹{mcap_cr:,.0f} Cr ({sector})")
            else:
                logger.warning(f"  [SKIP] {ticker}: ₹{mcap_cr:,.0f} Cr below ₹{min_in_mcap_cr:,.0f} Cr cutoff.")
        except Exception as e:
            logger.error(f"  [ERROR] {ticker}: {e}")

    # 2. Audit US Equities
    logger.info(f"Auditing {len(CURATED_US_CANDIDATES)} US large-cap candidates...")
    for ticker in CURATED_US_CANDIDATES:
        try:
            t = yf.Ticker(ticker)
            info = t.info or {}
            mcap = info.get("marketCap") or 0
            mcap_b = mcap / 1000000000.0  # Convert to Billions USD

            name = info.get("shortName") or info.get("longName") or ticker
            sector = info.get("sector") or "Diversified"
            exchange = "NASDAQ" if info.get("exchange") == "NMS" else "NYSE"

            if mcap_b >= min_us_mcap_b or mcap == 0:
                verified_securities.append({
                    "ticker": ticker,
                    "market": "US",
                    "exchange": exchange,
                    "name": name,
                    "sector": sector,
                    "is_active": True
                })
                logger.info(f"  [PASS] {ticker}: ${mcap_b:,.1f}B ({sector})")
            else:
                logger.warning(f"  [SKIP] {ticker}: ${mcap_b:,.1f}B below ${min_us_mcap_b:,.1f}B cutoff.")
        except Exception as e:
            logger.error(f"  [ERROR] {ticker}: {e}")

    # 3. Upsert into Database (Supabase / Local SQLite)
    if verified_securities:
        logger.info(f"Upserting {len(verified_securities)} verified large-cap securities into database...")
        db.upsert_securities(verified_securities)
        logger.info("Database universe synchronization complete!")

    return len(verified_securities)


if __name__ == "__main__":
    audit_and_sync_securities()
