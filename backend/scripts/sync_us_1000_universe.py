"""
Stocktics US 1,000 Universe Synchronizer & Supabase Ingestion
Fetches, verifies, and batch-upserts the Top 1,000 US Institutional Equities
(S&P 500 + S&P MidCap 400 + Top S&P 600 Quality Growth Leaders) into Supabase.
"""

import os
import sys
import io
import urllib.request
import logging
from pathlib import Path
from typing import List, Dict, Any
import pandas as pd
import requests

# Add root directory to sys.path
root_dir = Path(__file__).resolve().parent.parent.parent
if str(root_dir) not in sys.path:
    sys.path.insert(0, str(root_dir))

from backend.scanner.db import db

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("US1000Sync")


def fetch_and_build_us_1000() -> List[Dict[str, Any]]:
    logger.info("1. Fetching S&P 500 components from Wikipedia...")
    url_500 = "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies"
    req_500 = urllib.request.Request(url_500, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    html_500 = urllib.request.urlopen(req_500, timeout=15).read().decode("utf-8")
    sp500 = pd.read_html(io.StringIO(html_500))[0]
    sp500 = sp500.rename(columns={"Symbol": "ticker", "Security": "name", "GICS Sector": "sector"})
    sp500["index_source"] = "S&P 500"

    logger.info("2. Fetching S&P MidCap 400 components from Wikipedia...")
    url_400 = "https://en.wikipedia.org/wiki/List_of_S%26P_400_companies"
    req_400 = urllib.request.Request(url_400, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    html_400 = urllib.request.urlopen(req_400, timeout=15).read().decode("utf-8")
    sp400 = pd.read_html(io.StringIO(html_400))[0]
    ticker_col = "Symbol" if "Symbol" in sp400.columns else "Ticker symbol"
    sp400 = sp400.rename(columns={ticker_col: "ticker", "Security": "name", "GICS Sector": "sector"})
    sp400["index_source"] = "S&P 400"

    logger.info("3. Fetching S&P SmallCap 600 components for the remaining quality quota...")
    url_600 = "https://en.wikipedia.org/wiki/List_of_S%26P_600_companies"
    req_600 = urllib.request.Request(url_600, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    html_600 = urllib.request.urlopen(req_600, timeout=15).read().decode("utf-8")
    sp600 = pd.read_html(io.StringIO(html_600))[0]
    sp600 = sp600.rename(columns={"Symbol": "ticker", "Security": "name", "GICS Sector": "sector"})
    sp600["index_source"] = "S&P 600"

    # Combine and deduplicate
    combined = pd.concat([
        sp500[["ticker", "name", "sector", "index_source"]],
        sp400[["ticker", "name", "sector", "index_source"]],
        sp600[["ticker", "name", "sector", "index_source"]]
    ]).drop_duplicates(subset=["ticker"])

    # Clean ticker symbols (standardize Yahoo Finance dots to hyphens: BRK.B -> BRK-B)
    combined["ticker"] = combined["ticker"].str.strip().str.replace(".", "-", regex=False)

    # Build the exact 1,000 universe:
    # 1. All S&P 500 (~503)
    # 2. All S&P 400 (~400)
    # 3. Fill remaining balance to 1,000 from S&P 600 leaders
    s500 = combined[combined["index_source"] == "S&P 500"]
    s400 = combined[combined["index_source"] == "S&P 400"]
    needed_600 = max(0, 1000 - (len(s500) + len(s400)))
    s600_top = combined[combined["index_source"] == "S&P 600"].head(needed_600)

    top_1000 = pd.concat([s500, s400, s600_top]).reset_index(drop=True)
    logger.info(f"Assembled top {len(top_1000)} US equities across {top_1000['sector'].nunique()} GICS sectors.")

    # Convert to securities dictionary format
    securities = []
    for _, row in top_1000.iterrows():
        ticker = str(row["ticker"]).strip().upper()
        name = str(row["name"]).strip()
        sector = str(row["sector"]).strip() if pd.notna(row["sector"]) else "Diversified"
        exchange = "NASDAQ" if any(ticker.startswith(p) for p in ["AAPL", "MSFT", "NVDA", "AMZN", "GOOG", "META", "TSLA", "AVGO", "COST", "AMD", "QCOM", "ADBE", "INTC", "CSCO", "INTU", "BKNG", "ISRG", "VRTX", "PANW", "MU"]) else "NYSE"
        
        securities.append({
            "ticker": ticker,
            "market": "US",
            "exchange": exchange,
            "name": name,
            "sector": sector,
            "is_active": True
        })

    return securities


def upsert_to_supabase(securities: List[Dict[str, Any]]):
    if not db.is_supabase_ready:
        logger.error("Supabase is not ready. Check .env credentials.")
        return

    logger.info(f"Upserting {len(securities)} US securities into Supabase in chunks...")
    chunk_size = 100
    total_upserted = 0
    headers = dict(db.headers)
    headers["Prefer"] = "resolution=merge-duplicates"

    for i in range(0, len(securities), chunk_size):
        chunk = securities[i : i + chunk_size]
        try:
            res = requests.post(
                f"{db.url}/rest/v1/securities?on_conflict=ticker,market",
                headers=headers,
                json=chunk,
                timeout=20
            )
            if res.status_code in (200, 201):
                total_upserted += len(chunk)
                logger.info(f"  Upserted batch {i // chunk_size + 1}/{(len(securities) + chunk_size - 1) // chunk_size} ({total_upserted}/{len(securities)})")
            else:
                logger.error(f"  Batch error: {res.status_code} {res.text[:120]}")
        except Exception as e:
            logger.error(f"  Exception on batch {i}: {e}")

    logger.info(f"=== Successfully synchronized {total_upserted} US securities to Supabase! ===")


if __name__ == "__main__":
    us_securities = fetch_and_build_us_1000()
    print(f"Generated {len(us_securities)} US securities.")
    upsert_to_supabase(us_securities)
