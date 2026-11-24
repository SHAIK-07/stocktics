"""
Stocktics Real-Time Stock Analysis API
Exposes FastAPI endpoints for live stock search and audited financial metrics calculation
for both US (NASDAQ, NYSE) and Indian (NSE, BSE) markets.
"""

from fastapi import FastAPI, HTTPException, Query, Request
from fastapi.responses import JSONResponse, Response
from fastapi.middleware.cors import CORSMiddleware
import yfinance as yf
import requests
import pandas as pd
import time
import datetime
import os
import threading
from typing import Dict, Any, List, Optional
from dotenv import load_dotenv

# Load credentials from the project-root .env (gitignored) before engines read os.environ
load_dotenv(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), ".env"))

from backend.engines.metric_engine import compute_all_metrics, format_currency
from backend.engines.quote_service import get_browser_session, get_yf_ticker, get_batch_quotes

app = FastAPI(
    title="Stocktics Data-Driven Financial Learning API",
    description="Live audited metrics engine for US and Indian stock market curriculum",
    version="1.0.0"
)

# Background Keep-Alive Worker for Render Free-Tier
def _start_render_keepalive():
    if os.getenv("RENDER") or os.getenv("ENABLE_KEEPALIVE") == "true":
        def _ping_loop():
            time.sleep(60)  # Wait 1 minute after boot
            while True:
                try:
                    time.sleep(600)  # Ping every 10 minutes (Render timeout is 15 mins)
                    requests.get("https://stocktics-backend.onrender.com/health", timeout=15)
                except Exception:
                    pass
        t = threading.Thread(target=_ping_loop, daemon=True)
        t.start()

def _start_indmoney_token_auto_refresher():
    """
    Proactively checks all connected user INDmoney tokens in Supabase every 10 minutes.
    Refreshes any token expiring within the next 20 minutes so tokens NEVER expire!
    """
    def _refresh_loop():
        time.sleep(30)
        while True:
            try:
                from backend.engines.indmoney_engine import auto_refresh_all_active_tokens
                auto_refresh_all_active_tokens()
            except Exception as e:
                print(f"[TokenRefresher] Error: {e}")
            time.sleep(600)  # Check every 10 minutes
    t = threading.Thread(target=_refresh_loop, daemon=True)
    t.start()

_start_render_keepalive()
_start_indmoney_token_auto_refresher()

# Start Non-Market Hours Daily Pattern Scanner Scheduler (US 1,000 Universe)
try:
    from backend.scanner.scheduler import scanner_scheduler
    scanner_scheduler.start_scheduler()
except Exception as _sched_err:
    print(f"[ScannerScheduler] Initialization error: {_sched_err}")

# Enable CORS for React frontend (Cloudflare Workers, Vite, and local development)
ALLOWED_ORIGINS = [
    "https://stocktics.stocktics7.workers.dev",
    "https://stocktics.pages.dev",
    "http://localhost:5173",
    "http://localhost:3000",
    "http://127.0.0.1:5173",
    "http://127.0.0.1:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_origin_regex=r"https?://.*",
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["*"],
)

@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """
    Ensures that uncaught server errors return valid CORS headers so the browser
    receives the detailed JSON response instead of a raw CORS failure.
    """
    origin = request.headers.get("origin")
    headers = {}
    if origin:
        headers["Access-Control-Allow-Origin"] = origin
        headers["Access-Control-Allow-Credentials"] = "true"
        headers["Access-Control-Allow-Methods"] = "GET, POST, PUT, DELETE, OPTIONS, PATCH"
        headers["Access-Control-Allow-Headers"] = "*"
    return JSONResponse(
        status_code=500,
        content={"success": False, "detail": "Internal Server Error", "error": str(exc)},
        headers=headers,
    )


# Simple in-memory cache with 5-minute TTL to ensure fast responses
_CACHE: Dict[str, Dict[str, Any]] = {}
CACHE_TTL_SECONDS = 300

# Mount Dual-Market Pattern Scanner Routes (Accessible at /scanner and /api/scanner)
from backend.engines.scanner_api import router as scanner_router
app.include_router(scanner_router, prefix="/scanner")
app.include_router(scanner_router, prefix="/api/scanner")

# Mount Portfolio, Watchlist & Auth Routes (Accessible at / and /api)
from backend.engines.portfolio_api import router as portfolio_router
app.include_router(portfolio_router)
app.include_router(portfolio_router, prefix="/api")

# Mount Universal Market Tools (Heatmap Treemap & Multi-Stock Compare)
from backend.engines.market_tools_api import router as market_tools_router
app.include_router(market_tools_router)
app.include_router(market_tools_router, prefix="/api")

# Mount INDmoney MCP OAuth & Portfolio Sync Routes (Accessible at /indmoney and /api/indmoney)
from backend.engines.indmoney_api import router as indmoney_router
app.include_router(indmoney_router)
app.include_router(indmoney_router, prefix="/api")

# Mount Sector Intelligence & GICS Explorer Routes (Accessible at /api/sectors and /sectors)
from backend.engines.sector_api import router as sector_router
app.include_router(sector_router)
app.include_router(sector_router, prefix="")



@app.get("/")
@app.get("/health")
@app.get("/api/health")
def health_check():
    return {
        "status": "healthy",
        "service": "Stocktics Financial Learning API",
        "supportedMarkets": ["US (NASDAQ, NYSE)", "India (NSE, BSE)"]
    }


@app.get("/api/quotes")
@app.get("/quotes")
def get_batch_quotes_endpoint(symbols: str = Query(..., description="Comma-separated list of symbols e.g. AAPL,MSFT,RELIANCE.NS")):
    """
    High-speed parallel batch quotes endpoint for Watchlist and Portfolio live price streaming.
    """
    sym_list = [s.strip().upper() for s in symbols.split(",") if s.strip()][:50]
    if not sym_list:
        return {"quotes": {}}
    return {"quotes": get_batch_quotes(sym_list)}



@app.get("/api/search")
@app.get("/search")
def search_tickers(q: str = Query(..., min_length=1, description="Company name or ticker symbol")):
    """
    Real-time cross-market search across US (NYSE, NASDAQ) and Indian (NSE, BSE) exchanges.
    """
    clean_q = q.strip()
    headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}
    
    candidates = []
    
    # Check if query is an exact Indian ticker without suffix (e.g. RELIANCE, TCS, INFY, HDFCBANK)
    upper_q = clean_q.upper()
    common_indian_tickers = {
        "RELIANCE": ("RELIANCE.NS", "Reliance Industries Ltd.", "NSE"),
        "TCS": ("TCS.NS", "Tata Consultancy Services Ltd.", "NSE"),
        "INFY": ("INFY.NS", "Infosys Limited", "NSE"),
        "HDFCBANK": ("HDFCBANK.NS", "HDFC Bank Limited", "NSE"),
        "ICICIBANK": ("ICICIBANK.NS", "ICICI Bank Limited", "NSE"),
        "TATAMOTORS": ("TMPV.NS", "Tata Motors Limited", "NSE"),
        "SBIN": ("SBIN.NS", "State Bank of India", "NSE"),
        "BHARTIARTL": ("BHARTIARTL.NS", "Bharti Airtel Limited", "NSE"),
        "ITC": ("ITC.NS", "ITC Limited", "NSE"),
        "WIPRO": ("WIPRO.NS", "Wipro Limited", "NSE"),
        "HINDUNILVR": ("HINDUNILVR.NS", "Hindustan Unilever Ltd.", "NSE")
    }
    
    if upper_q in common_indian_tickers:
        sym, name, exch = common_indian_tickers[upper_q]
        candidates.append({
            "symbol": sym,
            "name": name,
            "exchange": exch,
            "quoteType": "EQUITY",
            "country": "India"
        })

    # Query Yahoo Finance real-time search API using browser session to avoid 401/403 blocks
    try:
        url = f"https://query2.finance.yahoo.com/v1/finance/search?q={clean_q}&quotesCount=8&newsCount=0"
        session = get_browser_session()
        resp = session.get(url, timeout=5)
        if resp.status_code == 200:
            data = resp.json()
            quotes = data.get("quotes", [])
            for item in quotes:
                sym = item.get("symbol")
                if not sym:
                    continue
                name = item.get("shortname") or item.get("longname") or sym
                exch = item.get("exchange", "")
                qtype = item.get("quoteType", "EQUITY")
                
                # Determine country
                country = "USA"
                if sym.endswith(".NS") or exch in ["NSI", "NSE"]:
                    country = "India (NSE)"
                elif sym.endswith(".BO") or exch in ["BSE", "BOM"]:
                    country = "India (BSE)"
                
                # Avoid duplicate symbols
                if not any(c["symbol"] == sym for c in candidates):
                    candidates.append({
                        "symbol": sym,
                        "name": name,
                        "exchange": exch,
                        "quoteType": qtype,
                        "country": country
                    })
    except Exception as e:
        print(f"Error querying search API: {e}")

    return {"query": clean_q, "results": candidates[:10]}


@app.get("/api/stock/{ticker}/history")
@app.get("/stock/{ticker}/history")
def get_stock_history(ticker: str, period: str = Query("1y")):
    """
    Returns real historical Open, High, Low, Close, Volume data points for line and bar charts.
    """
    clean_ticker = ticker.strip().upper()
    target_ticker = clean_ticker
    
    # Deep buffer configuration so TradingView pan/drag reveals continuous previous days and weeks
    deep_buffer_map = {
        "1d": {"fetch_period": "1mo", "interval": "5m", "default_bars": 78},   # 1 month of 5m candles (~1600 bars)
        "5d": {"fetch_period": "60d", "interval": "15m", "default_bars": 130}, # 60 days of 15m candles (~1500 bars, max intraday range allowed by Yahoo)
        "1mo": {"fetch_period": "1y", "interval": "1d", "default_bars": 22},   # 1 year of daily candles (~252 bars)
        "6mo": {"fetch_period": "2y", "interval": "1d", "default_bars": 126},  # 2 years of daily candles (~504 bars)
        "1y": {"fetch_period": "5y", "interval": "1d", "default_bars": 252},   # 5 years of daily candles (~1260 bars)
        "5y": {"fetch_period": "max", "interval": "1wk", "default_bars": 260}  # max history weekly (~1000+ bars)
    }

    cfg = deep_buffer_map.get(period, {"fetch_period": period, "interval": "1d", "default_bars": 60})
    fetch_period = cfg["fetch_period"]
    interval = cfg["interval"]
    default_bars = cfg["default_bars"]

    t = get_yf_ticker(target_ticker)
    hist = t.history(period=fetch_period, interval=interval)
    if hist.empty and not clean_ticker.endswith(".NS") and not clean_ticker.endswith(".BO"):
        alt_ticker = f"{clean_ticker}.NS"
        alt_t = get_yf_ticker(alt_ticker)
        alt_hist = alt_t.history(period=fetch_period, interval=interval)
        if not alt_hist.empty:
            t = alt_t
            hist = alt_hist
            target_ticker = alt_ticker

    # Fallback to direct period if deep period query is empty
    if hist.empty:
        hist = t.history(period=period, interval=interval)

    if hist.empty:
        raise HTTPException(status_code=404, detail=f"No historical chart data found for {ticker}")

    clean_hist = hist.dropna(subset=["Close"])
    pts = []
    for idx, row in clean_hist.iterrows():
        time_str = str(idx)[:16] if interval in ["1m", "2m", "5m", "15m", "30m", "60m", "1h"] else str(idx)[:10]
        pts.append({
            "time": time_str,
            "open": round(float(row["Open"]), 2),
            "high": round(float(row["High"]), 2),
            "low": round(float(row["Low"]), 2),
            "close": round(float(row["Close"]), 2),
            "volume": int(row["Volume"]) if pd.notnull(row["Volume"]) else 0
        })

    # Window change represents the selected period window change
    last_close = pts[-1]["close"] if pts else 0.0

    if period == "1d":
        # Group by date to find today's active session
        last_date = pts[-1]["time"][:10] if pts else ""
        today_indices = [i for i, p in enumerate(pts) if p["time"][:10] == last_date]
        today_bars = len(today_indices)
        today_start_idx = today_indices[0] if today_indices else (len(pts) - 1)

        # Official previous close from fast_info or candle immediately before today
        prev_close = None
        try:
            prev_close = t.fast_info.previous_close
        except Exception:
            pass

        if prev_close is None or prev_close <= 0:
            if today_start_idx > 0:
                prev_close = pts[today_start_idx - 1]["close"]
            else:
                prev_close = pts[today_start_idx]["open"] if today_indices else last_close

        window_first_close = round(float(prev_close), 2)
        window_bars = today_bars
        default_bars = max(today_bars + 6, 24)
    elif period == "5d":
        # Find unique dates in pts, take up to last 5 distinct dates
        seen_dates = []
        for p in reversed(pts):
            d = p["time"][:10]
            if d not in seen_dates:
                seen_dates.append(d)
            if len(seen_dates) >= 5:
                break
        farthest_date = seen_dates[-1] if seen_dates else pts[0]["time"][:10]
        farthest_idx = next((i for i, p in enumerate(pts) if p["time"][:10] == farthest_date), 0)
        window_bars = len(pts) - farthest_idx
        window_first_close = pts[farthest_idx]["open"]
        default_bars = window_bars
    else:
        window_bars = min(len(pts), default_bars)
        window_first_close = pts[-window_bars]["open"] if window_bars > 0 else (pts[0]["close"] if pts else 0.0)

    period_change = round(last_close - window_first_close, 2)
    period_change_pct = round(((last_close - window_first_close) / window_first_close * 100.0), 2) if window_first_close > 0 else 0.0

    return {
        "ticker": target_ticker,
        "period": period,
        "defaultVisibleBars": default_bars,
        "points": pts,
        "firstClose": window_first_close,
        "lastClose": last_close,
        "periodChange": period_change,
        "periodChangePercent": period_change_pct,
        "isUp": period_change >= 0,
        "highest": max((p["high"] for p in pts[-window_bars:]), default=0.0) if pts else 0.0,
        "lowest": min((p["low"] for p in pts[-window_bars:]), default=0.0) if pts else 0.0
    }


@app.get("/api/stock/{ticker}")
@app.get("/stock/{ticker}")
def get_stock_analysis(ticker: str):
    """
    Fetches real audited financial statements and returns 100% real metrics,
    positive/negative status flags, educational explanations, and charts.
    """
    clean_ticker = ticker.strip().upper()
    now = time.time()
    
    # Check cache (ensure entry has performance data)
    if clean_ticker in _CACHE and "performance" in _CACHE[clean_ticker].get("data", {}):
        entry = _CACHE[clean_ticker]
        if now - entry["timestamp"] < CACHE_TTL_SECONDS:
            return entry["data"]

    # Resolution helper: If user entered an Indian ticker without suffix (e.g. RELIANCE, TCS)
    # and it isn't found, try .NS then .BO
    target_ticker = clean_ticker
    t = get_yf_ticker(target_ticker)
    info = t.info or {}

    # If info is empty or invalid, try appending .NS for common Indian stocks
    if not (info.get("currentPrice") or info.get("regularMarketPrice")):
        if not clean_ticker.endswith(".NS") and not clean_ticker.endswith(".BO"):
            alt_ticker = f"{clean_ticker}.NS"
            alt_t = get_yf_ticker(alt_ticker)
            alt_info = alt_t.info or {}
            if alt_info.get("currentPrice") or alt_info.get("regularMarketPrice"):
                target_ticker = alt_ticker
                t = alt_t
                info = alt_info

    # Guarantee live price from fast_info if Yahoo quoteSummary is transitioning or partial
    if not (info.get("currentPrice") or info.get("regularMarketPrice")):
        try:
            fi = getattr(t, "fast_info", None)
            if fi:
                last_p = getattr(fi, "last_price", 0.0) or getattr(fi, "regular_market_previous_close", 0.0) or 0.0
                if last_p > 0:
                    info["currentPrice"] = float(last_p)
                    info["regularMarketPrice"] = float(last_p)
                    if not info.get("previousClose") and getattr(fi, "previous_close", None):
                        info["previousClose"] = float(fi.previous_close)
                    if not info.get("currency") and getattr(fi, "currency", None):
                        info["currency"] = str(fi.currency)
                    if not info.get("marketCap") and getattr(fi, "market_cap", None):
                        info["marketCap"] = float(fi.market_cap)
                    if not info.get("sharesOutstanding") and getattr(fi, "shares", None):
                        info["sharesOutstanding"] = float(fi.shares)
                    if not info.get("fiftyTwoWeekHigh") and getattr(fi, "year_high", None):
                        info["fiftyTwoWeekHigh"] = float(fi.year_high)
                    if not info.get("fiftyTwoWeekLow") and getattr(fi, "year_low", None):
                        info["fiftyTwoWeekLow"] = float(fi.year_low)
        except Exception:
            pass

    # If still not found, check history(period="5d")
    if not (info.get("currentPrice") or info.get("regularMarketPrice")):
        try:
            h = t.history(period="5d")
            if not h.empty and "Close" in h:
                last_p = float(h["Close"].iloc[-1])
                prev_p = float(h["Close"].iloc[-2]) if len(h) > 1 else last_p
                info["currentPrice"] = last_p
                info["regularMarketPrice"] = last_p
                if not info.get("previousClose"):
                    info["previousClose"] = prev_p
        except Exception:
            pass

    if not info or not (info.get("currentPrice") or info.get("regularMarketPrice")):
        raise HTTPException(
            status_code=404,
            detail=f"Unable to find live stock data for symbol '{ticker}'. Please verify the ticker symbol."
        )

    # Fetch audited financial statements safely
    try:
        financials = t.financials
        if financials is None:
            financials = pd.DataFrame()
    except Exception:
        financials = pd.DataFrame()

    try:
        balance_sheet = t.balance_sheet
        if balance_sheet is None:
            balance_sheet = pd.DataFrame()
    except Exception:
        balance_sheet = pd.DataFrame()

    try:
        cashflow = t.cashflow
        if cashflow is None:
            cashflow = pd.DataFrame()
    except Exception:
        cashflow = pd.DataFrame()

    # Basic overview data
    company_name = info.get("shortName") or info.get("longName") or target_ticker.replace(".NS", "").replace(".BO", "")
    currency = info.get("currency", "USD")
    current_price = float(info.get("currentPrice") or info.get("regularMarketPrice") or 0.0)
    prev_close = float(info.get("previousClose") or info.get("regularMarketPreviousClose") or current_price)
    change = current_price - prev_close
    change_pct = (change / prev_close * 100.0) if prev_close > 0 else 0.0

    # Calculate Trailing Multi-Period Performance Returns (1W, 1M, 3M, 6M, YTD, 1Y)
    performance = {}
    try:
        hist_2y = t.history(period="2y")
        if not hist_2y.empty and "Close" in hist_2y:
            now_dt = datetime.datetime.now()
            last_p = current_price if current_price > 0 else float(hist_2y["Close"].iloc[-1])

            def get_trailing_ret(days):
                cutoff = (now_dt - datetime.timedelta(days=days)).strftime("%Y-%m-%d")
                sub = hist_2y[hist_2y.index <= cutoff]
                if not sub.empty:
                    base_p = float(sub["Close"].iloc[-1])
                    if base_p > 0:
                        return round((last_p - base_p) / base_p * 100.0, 2)
                return 0.0

            def get_ytd_trailing_ret():
                sub = hist_2y[hist_2y.index <= f"{now_dt.year}-01-01"]
                if not sub.empty:
                    base_p = float(sub["Close"].iloc[-1])
                    if base_p > 0:
                        return round((last_p - base_p) / base_p * 100.0, 2)
                return 0.0

            performance = {
                "1W": get_trailing_ret(7),
                "1M": get_trailing_ret(30),
                "3M": get_trailing_ret(90),
                "6M": get_trailing_ret(180),
                "YTD": get_ytd_trailing_ret(),
                "1Y": get_trailing_ret(365)
            }
    except Exception as e:
        print(f"Error computing performance returns for {target_ticker}: {e}")

    # Compute all 6 Dimensions
    analysis = compute_all_metrics(info, financials, balance_sheet, cashflow)

    payload = {
        "ticker": target_ticker,
        "companyName": company_name,
        "exchange": info.get("exchange", "US"),
        "currency": currency,
        "currencySymbol": "₹" if currency.upper() in ["INR", "₹"] else "$",
        "currentPrice": current_price,
        "formattedPrice": format_currency(current_price, currency),
        "previousClose": prev_close,
        "change": round(change, 2),
        "changePercent": round(change_pct, 2),
        "isUp": change >= 0,
        "marketCap": float(info.get("marketCap") or 0.0),
        "formattedMarketCap": format_currency(float(info.get("marketCap") or 0.0), currency),
        "fiftyTwoWeekHigh": float(info.get("fiftyTwoWeekHigh") or current_price),
        "fiftyTwoWeekLow": float(info.get("fiftyTwoWeekLow") or current_price),
        "formattedFiftyTwoWeekHigh": format_currency(float(info.get("fiftyTwoWeekHigh") or current_price), currency),
        "formattedFiftyTwoWeekLow": format_currency(float(info.get("fiftyTwoWeekLow") or current_price), currency),
        "website": info.get("website", ""),
        "logoUrl": f"https://assets.parqet.com/logos/symbol/{target_ticker}?format=png",
        "sector": info.get("sector", "N/A"),
        "industry": info.get("industry", "N/A"),
        "businessSummary": info.get("longBusinessSummary", "No company summary available."),
        "performance": performance,
        "dimensions": analysis["dimensions"],
        "charts": analysis["charts"],
        "scorecard": analysis["scorecard"]
    }

    # Store in cache
    _CACHE[clean_ticker] = {"timestamp": now, "data": payload}
    if target_ticker != clean_ticker:
        _CACHE[target_ticker] = {"timestamp": now, "data": payload}

    return payload


@app.get("/stock/{ticker}/news")
@app.get("/api/stock/{ticker}/news")
def get_stock_news(ticker: str, user_id: Optional[str] = None):
    """
    Returns INDmoney MCP parsed news and sentiment analysis for a stock symbol:
    - Current month only
    - Latest items on top
    - Sentiment counts (positive, neutral, negative)
    - Today's items identified
    """
    from backend.engines.indmoney_engine import get_stock_news_sentiment
    return get_stock_news_sentiment(symbol=ticker, user_id=user_id)



if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.api:app", host="0.0.0.0", port=8000, reload=True)


