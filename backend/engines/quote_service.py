"""
Stocktics Unified Quote Service
Provides high-speed parallel price quote retrieval and caching for watchlists,
holdings, and live market streaming across US and Indian exchanges.
"""

import time
import requests
import concurrent.futures
from typing import Dict, Any, List, Optional
import yfinance as yf

# In-memory TTL quote cache (60 seconds)
_QUOTE_CACHE: Dict[str, Dict[str, Any]] = {}
CACHE_TTL_SECONDS = 60


def get_browser_session():
    """
    Returns a curl_cffi session impersonating Google Chrome to pass Yahoo Finance TLS
    fingerprinting on cloud hosts (Render/AWS). Falls back gracefully to requests.Session.
    """
    try:
        from curl_cffi import requests as cffi_requests
        return cffi_requests.Session(impersonate="chrome")
    except Exception:
        s = requests.Session()
        s.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.9"
        })
        return s


def get_yf_ticker(ticker_symbol: str) -> yf.Ticker:
    """
    Instantiates a yfinance Ticker backed by a browser-impersonating session.
    """
    session = get_browser_session()
    try:
        return yf.Ticker(ticker_symbol, session=session)
    except TypeError:
        return yf.Ticker(ticker_symbol)


def fetch_single_quote(symbol: str) -> Dict[str, Any]:
    """
    Fetches real-time price, previous close, percentage change, and 52-week levels for a symbol.
    Cached for 60 seconds.
    """
    sym = symbol.strip().upper()
    now = time.time()

    if sym in _QUOTE_CACHE and (now - _QUOTE_CACHE[sym]["timestamp"] < CACHE_TTL_SECONDS):
        return _QUOTE_CACHE[sym]["data"]

    price = 0.0
    prev_close = 0.0
    name = sym.replace(".NS", "").replace(".BO", "")
    currency = "USD"
    high_52w = 0.0
    low_52w = 0.0

    try:
        t = get_yf_ticker(sym)
        info = getattr(t, "fast_info", None)
        if info:
            price = float(getattr(info, "last_price", 0.0) or getattr(info, "regular_market_previous_close", 0.0) or 0.0)
            prev_close = float(getattr(info, "previous_close", 0.0) or getattr(info, "regular_market_previous_close", 0.0) or price)
            high_52w = float(getattr(info, "year_high", 0.0) or price)
            low_52w = float(getattr(info, "year_low", 0.0) or price)
            currency = getattr(info, "currency", "USD") or "USD"

        if price <= 0:
            full_info = t.info or {}
            price = float(full_info.get("currentPrice") or full_info.get("regularMarketPrice") or 0.0)
            prev_close = float(full_info.get("previousClose") or price)
            high_52w = float(full_info.get("fiftyTwoWeekHigh") or price)
            low_52w = float(full_info.get("fiftyTwoWeekLow") or price)
            name = full_info.get("shortName") or full_info.get("longName") or name
            currency = full_info.get("currency", "USD")

        if price <= 0:
            df = t.history(period="2d", interval="1d")
            if not df.empty and len(df) >= 1:
                price = float(df["Close"].iloc[-1])
                prev_close = float(df["Close"].iloc[-2]) if len(df) >= 2 else price
                high_52w = float(df["High"].max()) if "High" in df else price
                low_52w = float(df["Low"].min()) if "Low" in df else price
    except Exception as e:
        pass

    change = round(price - prev_close, 2)
    change_pct = round((change / prev_close * 100.0) if prev_close > 0 else 0.0, 2)
    is_indian = sym.endswith(".NS") or sym.endswith(".BO")
    curr_sym = "₹" if (currency.upper() in ["INR", "₹"] or is_indian) else "$"

    quote_data = {
        "symbol": sym,
        "name": name,
        "price": round(price, 2),
        "previousClose": round(prev_close, 2),
        "change": change,
        "changePercent": change_pct,
        "isPositive": change >= 0,
        "currency": currency,
        "currencySymbol": curr_sym,
        "fiftyTwoWeekHigh": round(high_52w, 2),
        "fiftyTwoWeekLow": round(low_52w, 2),
        "logoUrl": f"https://assets.parqet.com/logos/symbol/{sym}?format=png"
    }

    _QUOTE_CACHE[sym] = {"timestamp": now, "data": quote_data}
    return quote_data


def get_batch_quotes(symbols: List[str], max_workers: int = 8) -> Dict[str, Dict[str, Any]]:
    """
    High-speed parallel batch quotes lookup for multiple symbols.
    """
    unique_syms = list(set([s.strip().upper() for s in symbols if s.strip()]))[:50]
    if not unique_syms:
        return {}

    quotes = {}
    now = time.time()
    to_fetch = []

    for s in unique_syms:
        cached = _QUOTE_CACHE.get(s)
        if cached and now - cached["timestamp"] < CACHE_TTL_SECONDS:
            quotes[s] = cached["data"]
        else:
            to_fetch.append(s)

    if to_fetch:
        with concurrent.futures.ThreadPoolExecutor(max_workers=min(len(to_fetch), max_workers)) as executor:
            future_map = {executor.submit(fetch_single_quote, s): s for s in to_fetch}
            try:
                for future in concurrent.futures.as_completed(future_map, timeout=6.0):
                    sym = future_map[future]
                    try:
                        q = future.result()
                        if q:
                            quotes[sym] = q
                    except Exception:
                        pass
            except concurrent.futures.TimeoutError:
                for future, sym in future_map.items():
                    if sym not in quotes:
                        quotes[sym] = {
                            "symbol": sym, "name": sym, "price": 0.0, "previousClose": 0.0,
                            "change": 0.0, "changePercent": 0.0, "isPositive": True,
                            "currency": "USD", "currencySymbol": "$", "fiftyTwoWeekHigh": 0.0, "fiftyTwoWeekLow": 0.0,
                            "logoUrl": f"https://assets.parqet.com/logos/symbol/{sym}?format=png"
                        }

    return quotes
