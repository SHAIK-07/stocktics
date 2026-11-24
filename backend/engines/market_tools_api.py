"""
Stocktics Universal Market Tools API:
- Market Heatmap Treemap (S&P 500 & Nifty 50)
- Multi-Stock Comparison Tool (Normalized Returns, Daily Price Series, Pearson Correlation Matrix)
"""

import time
import math
import concurrent.futures
from typing import Dict, Any, List, Optional
from fastapi import APIRouter, Query, HTTPException
import yfinance as yf
import pandas as pd
import numpy as np

router = APIRouter(tags=["Market Tools"])

# Cache with 15-minute TTL to reduce latency and eliminate timeouts
_HEATMAP_CACHE: Dict[str, Dict[str, Any]] = {}
_COMPARE_CACHE: Dict[str, Dict[str, Any]] = {}
CACHE_TTL = 900  # 15 minutes (900 seconds)

# Top 50 US S&P 500 Symbols
SP_500_SYMBOLS = [
    "MSFT", "AAPL", "NVDA", "AMZN", "META", "GOOGL", "GOOG", "BRK-B", "LLY", "AVGO",
    "TSLA", "JPM", "UNH", "XOM", "V", "PG", "MA", "COST", "JNJ", "HD",
    "MRK", "ABBV", "WMT", "BAC", "PEP", "AMD", "ADBE", "CRM", "CVX", "TMO",
    "QCOM", "CSCO", "ACN", "MCD", "ORCL", "LIN", "ABT", "DIS", "INTU", "TXN",
    "VZ", "AMAT", "DHR", "GE", "BKNG", "PFE", "CAT", "AMGN", "HON", "COP"
]

# Top 50 Indian Nifty 50 Symbols
NIFTY_50_SYMBOLS = [
    "RELIANCE.NS", "TCS.NS", "HDFCBANK.NS", "ICICIBANK.NS", "BHARTIARTL.NS", "SBIN.NS", "INFY.NS",
    "ITC.NS", "HINDUNILVR.NS", "LT.NS", "BAJFINANCE.NS", "MARUTI.NS", "TITAN.NS", "M&M.NS",
    "ADANIENT.NS", "ADANIPORTS.NS", "KOTAKBANK.NS", "AXISBANK.NS", "ULTRACEMCO.NS", "NTPC.NS",
    "BAJAJFINSV.NS", "BAJAJ-AUTO.NS", "JSWSTEEL.NS", "ONGC.NS", "HCLTECH.NS", "NESTLEIND.NS",
    "POWERGRID.NS", "ASIANPAINT.NS", "COALINDIA.NS", "SHRIRAMFIN.NS", "TATASTEEL.NS", "HINDALCO.NS",
    "EICHERMOT.NS", "SUNPHARMA.NS", "DIVISLAB.NS", "JIOFIN.NS", "WIPRO.NS", "TECHM.NS",
    "BPCL.NS", "GRASIM.NS", "SBILIFE.NS", "BEL.NS", "HAL.NS", "IOC.NS", "DLF.NS",
    "BRITANNIA.NS", "CIPLA.NS", "INDUSINDBK.NS", "DRREDDY.NS", "TATACONSUM.NS"
]

SECTOR_MAP: Dict[str, str] = {
    # US
    "MSFT": "Technology", "AAPL": "Technology", "NVDA": "Semiconductors", "AMZN": "Consumer Cyclical",
    "META": "Communication Services", "GOOGL": "Communication Services", "GOOG": "Communication Services",
    "BRK-B": "Financial Services", "LLY": "Healthcare", "AVGO": "Semiconductors", "TSLA": "Automobile",
    "JPM": "Financial Services", "UNH": "Healthcare", "XOM": "Energy", "V": "Financial Services",
    "PG": "Consumer Defensive", "MA": "Financial Services", "COST": "Consumer Defensive",
    "JNJ": "Healthcare", "HD": "Consumer Cyclical", "MRK": "Healthcare", "ABBV": "Healthcare",
    "WMT": "Consumer Defensive", "BAC": "Financial Services", "PEP": "Consumer Defensive",
    "AMD": "Semiconductors", "ADBE": "Technology", "CRM": "Technology", "CVX": "Energy",
    "TMO": "Healthcare", "QCOM": "Semiconductors", "CSCO": "Technology", "ACN": "Technology",
    "MCD": "Consumer Cyclical", "ORCL": "Technology", "LIN": "Materials", "ABT": "Healthcare",
    "DIS": "Communication Services", "INTU": "Technology", "TXN": "Semiconductors",
    "VZ": "Communication Services", "AMAT": "Semiconductors", "DHR": "Healthcare",
    "GE": "Industrials", "BKNG": "Consumer Cyclical", "PFE": "Healthcare", "CAT": "Industrials",
    "AMGN": "Healthcare", "HON": "Industrials", "COP": "Energy",

    # India
    "RELIANCE.NS": "Energy", "TCS.NS": "Technology", "HDFCBANK.NS": "Financial Services",
    "ICICIBANK.NS": "Financial Services", "BHARTIARTL.NS": "Communication Services",
    "SBIN.NS": "Financial Services", "INFY.NS": "Technology", "ITC.NS": "Consumer Defensive",
    "HINDUNILVR.NS": "Consumer Defensive", "LT.NS": "Industrials", "BAJFINANCE.NS": "Financial Services",
    "MARUTI.NS": "Automobile", "TITAN.NS": "Consumer Cyclical", "M&M.NS": "Automobile",
    "ADANIENT.NS": "Industrials", "ADANIPORTS.NS": "Industrials", "KOTAKBANK.NS": "Financial Services",
    "AXISBANK.NS": "Financial Services", "ULTRACEMCO.NS": "Materials", "NTPC.NS": "Utilities",
    "BAJAJFINSV.NS": "Financial Services", "BAJAJ-AUTO.NS": "Automobile", "JSWSTEEL.NS": "Materials",
    "ONGC.NS": "Energy", "HCLTECH.NS": "Technology", "NESTLEIND.NS": "Consumer Defensive",
    "POWERGRID.NS": "Utilities", "ASIANPAINT.NS": "Consumer Defensive", "COALINDIA.NS": "Energy",
    "SHRIRAMFIN.NS": "Financial Services", "TATASTEEL.NS": "Materials", "HINDALCO.NS": "Materials",
    "EICHERMOT.NS": "Automobile", "SUNPHARMA.NS": "Healthcare", "TATAMOTORS.NS": "Automobile",
    "JIOFIN.NS": "Financial Services", "WIPRO.NS": "Technology", "TECHM.NS": "Technology",
    "BPCL.NS": "Energy", "GRASIM.NS": "Materials", "SBILIFE.NS": "Financial Services",
    "BEL.NS": "Industrials", "HAL.NS": "Industrials", "IOC.NS": "Energy", "DLF.NS": "Real Estate",
    "BRITANNIA.NS": "Consumer Defensive", "CIPLA.NS": "Healthcare", "INDUSINDBK.NS": "Financial Services",
    "DIVISLAB.NS": "Healthcare", "DRREDDY.NS": "Healthcare", "TATACONSUM.NS": "Consumer Defensive"
}

NAME_MAP: Dict[str, str] = {
    "MSFT": "Microsoft Corp", "AAPL": "Apple Inc", "NVDA": "NVIDIA Corp", "AMZN": "Amazon.com Inc",
    "META": "Meta Platforms", "GOOGL": "Alphabet Cl A", "GOOG": "Alphabet Cl C", "BRK-B": "Berkshire Hathaway",
    "LLY": "Eli Lilly", "AVGO": "Broadcom Inc", "TSLA": "Tesla Inc", "JPM": "JPMorgan Chase",
    "UNH": "UnitedHealth Group", "XOM": "Exxon Mobil", "V": "Visa Inc", "PG": "Procter & Gamble",
    "MA": "Mastercard Inc", "COST": "Costco Wholesale", "JNJ": "Johnson & Johnson", "HD": "Home Depot",
    "MRK": "Merck & Co", "ABBV": "AbbVie Inc", "WMT": "Walmart Inc", "BAC": "Bank of America",
    "PEP": "PepsiCo Inc", "AMD": "Advanced Micro Devices", "ADBE": "Adobe Inc", "CRM": "Salesforce Inc",
    "CVX": "Chevron Corp", "TMO": "Thermo Fisher Scientific", "QCOM": "Qualcomm Inc", "CSCO": "Cisco Systems",
    "ACN": "Accenture Plc", "MCD": "McDonald's Corp", "ORCL": "Oracle Corp", "LIN": "Linde Plc",
    "ABT": "Abbott Labs", "DIS": "Walt Disney Co", "INTU": "Intuit Inc", "TXN": "Texas Instruments",
    "VZ": "Verizon Comm", "AMAT": "Applied Materials", "DHR": "Danaher Corp", "GE": "GE Aerospace",
    "BKNG": "Booking Holdings", "PFE": "Pfizer Inc", "CAT": "Caterpillar Inc", "AMGN": "Amgen Inc",
    "HON": "Honeywell", "COP": "ConocoPhillips",
    
    # India
    "RELIANCE.NS": "Reliance Industries", "TCS.NS": "Tata Consultancy", "HDFCBANK.NS": "HDFC Bank",
    "ICICIBANK.NS": "ICICI Bank", "BHARTIARTL.NS": "Bharti Airtel", "SBIN.NS": "State Bank of India",
    "INFY.NS": "Infosys Ltd", "ITC.NS": "ITC Ltd", "HINDUNILVR.NS": "Hindustan Unilever",
    "LT.NS": "Larsen & Toubro", "BAJFINANCE.NS": "Bajaj Finance", "MARUTI.NS": "Maruti Suzuki",
    "TITAN.NS": "Titan Company", "M&M.NS": "Mahindra & Mahindra", "ADANIENT.NS": "Adani Enterprises",
    "ADANIPORTS.NS": "Adani Ports", "KOTAKBANK.NS": "Kotak Mahindra Bank", "AXISBANK.NS": "Axis Bank",
    "ULTRACEMCO.NS": "UltraTech Cement", "NTPC.NS": "NTPC Ltd", "BAJAJFINSV.NS": "Bajaj Finserv",
    "BAJAJ-AUTO.NS": "Bajaj Auto", "JSWSTEEL.NS": "JSW Steel", "ONGC.NS": "ONGC", "HCLTECH.NS": "HCL Technologies",
    "NESTLEIND.NS": "Nestle India", "POWERGRID.NS": "Power Grid Corp", "ASIANPAINT.NS": "Asian Paints",
    "COALINDIA.NS": "Coal India", "SHRIRAMFIN.NS": "Shriram Finance", "TATASTEEL.NS": "Tata Steel",
    "HINDALCO.NS": "Hindalco Industries", "EICHERMOT.NS": "Eicher Motors", "SUNPHARMA.NS": "Sun Pharma",
    "TATAMOTORS.NS": "Tata Motors", "JIOFIN.NS": "Jio Financial", "WIPRO.NS": "Wipro Ltd",
    "TECHM.NS": "Tech Mahindra", "BPCL.NS": "BPCL", "GRASIM.NS": "Grasim Industries",
    "SBILIFE.NS": "SBI Life Insurance", "BEL.NS": "Bharat Electronics", "HAL.NS": "Hindustan Aeronautics",
    "IOC.NS": "Indian Oil Corp", "DLF.NS": "DLF Ltd", "BRITANNIA.NS": "Britannia Industries",
    "CIPLA.NS": "Cipla Ltd", "INDUSINDBK.NS": "IndusInd Bank", "LTIM.NS": "LTIMindtree", "TATACONSUM.NS": "Tata Consumer"
}


def _fetch_stock_quote(symbol: str) -> Optional[Dict[str, Any]]:
    try:
        t = yf.Ticker(symbol)
        fi = t.fast_info
        price = float(getattr(fi, "last_price", 0.0) or 0.0)
        prev_close = float(getattr(fi, "previous_close", price) or price)
        mcap = float(getattr(fi, "market_cap", 0.0) or 0.0)
        currency = getattr(fi, "currency", "USD") or "USD"
        
        if price <= 0:
            info = t.info
            price = float(info.get("currentPrice") or info.get("regularMarketPrice") or 0.0)
            prev_close = float(info.get("previousClose") or price)
            mcap = float(info.get("marketCap") or 0.0)
            currency = info.get("currency", "USD")
            pe = info.get("trailingPE") or info.get("forwardPE")
        else:
            pe = None

        if price <= 0:
            return None

        change = round(price - prev_close, 2)
        change_pct = round(((change / prev_close) * 100.0) if prev_close > 0 else 0.0, 2)
        
        is_in = symbol.endswith(".NS") or symbol.endswith(".BO")
        curr_sym = "₹" if is_in or currency.upper() in ["INR", "₹"] else "$"
        sector = SECTOR_MAP.get(symbol, "Other")
        name = NAME_MAP.get(symbol, symbol.replace(".NS", "").replace(".BO", ""))
        
        if mcap <= 0:
            mcap = price * 1000000000

        return {
            "symbol": symbol,
            "name": name,
            "price": round(price, 2),
            "change": change,
            "changePercent": change_pct,
            "changePct": change_pct,
            "marketCap": int(mcap),
            "pe": round(float(pe), 2) if (pe and isinstance(pe, (int, float)) and pe > 0) else "N/A",
            "sector": sector,
            "currency": currency,
            "currencySymbol": curr_sym,
            "logoUrl": f"https://assets.parqet.com/logos/symbol/{symbol}?format=png"
        }
    except Exception:
        return None


@router.get("/heatmap")
def get_market_heatmap(
    market: str = Query("US", description="Market code: US, IN, or ALL"),
    index: Optional[str] = Query(None, description="Index: SP500, NIFTY50"),
    period: str = Query("1D", description="Timeframe: 1D, 1W, 1M, 1Y")
):
    """
    Returns live market data and sector categorization for Treemap Squarified Heatmap.
    """
    market = market.upper()
    period = period.upper()
    cache_key = f"{market}_{index}_{period}"
    now = time.time()
    
    if cache_key in _HEATMAP_CACHE and (now - _HEATMAP_CACHE[cache_key]["time"] < CACHE_TTL):
        return _HEATMAP_CACHE[cache_key]["data"]

    if market == "IN" or index == "NIFTY50":
        symbols = NIFTY_50_SYMBOLS
        index_name = "Nifty 50"
    else:
        symbols = SP_500_SYMBOLS
        index_name = "S&P 500"

    stocks = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:
        results = executor.map(_fetch_stock_quote, symbols)
        for r in results:
            if r:
                stocks.append(r)

    # If live fetching returned too few stocks (rate limit or network blip), fallback to stale cache
    if len(stocks) < 10 and cache_key in _HEATMAP_CACHE:
        return _HEATMAP_CACHE[cache_key]["data"]

    # Group into sectors
    sectors_dict: Dict[str, List[Dict[str, Any]]] = {}
    for s in stocks:
        sec = s["sector"]
        if sec not in sectors_dict:
            sectors_dict[sec] = []
        sectors_dict[sec].append(s)

    sectors = []
    for sec_name, sec_stocks in sectors_dict.items():
        total_mcap = sum(st["marketCap"] for st in sec_stocks)
        avg_change = round(sum(st["changePercent"] for st in sec_stocks) / len(sec_stocks), 2) if sec_stocks else 0.0
        sectors.append({
            "name": sec_name,
            "totalMarketCap": total_mcap,
            "avgChangePercent": avg_change,
            "avgChangePct": avg_change,
            "stocks": sorted(sec_stocks, key=lambda x: x["marketCap"], reverse=True)
        })

    sectors.sort(key=lambda x: x["totalMarketCap"], reverse=True)

    response_data = {
        "market": market,
        "indexName": index_name,
        "count": len(stocks),
        "sectors": sectors,
        "stocks": stocks,
        "updatedAt": int(now * 1000)
    }

    if stocks:
        _HEATMAP_CACHE[cache_key] = {"time": now, "data": response_data}
    elif cache_key in _HEATMAP_CACHE:
        return _HEATMAP_CACHE[cache_key]["data"]

    return response_data


@router.get("/compare")
def compare_stocks(
    symbols: str = Query(..., description="Comma-separated ticker list e.g. AAPL,MSFT,NVDA"),
    period: str = Query("6mo", description="Timeframe: 1mo, 3mo, 6mo, 1y, 5y")
):
    """
    Multi-stock historical performance comparison with normalized percentage baseline
    and Pearson Correlation Matrix.
    """
    sym_list = [s.strip().upper() for s in symbols.split(",") if s.strip()][:15]
    if not sym_list:
        raise HTTPException(status_code=400, detail="At least one symbol is required")

    cache_key = f"{','.join(sorted(sym_list))}_{period}"
    now = time.time()
    if cache_key in _COMPARE_CACHE and (now - _COMPARE_CACHE[cache_key]["time"] < CACHE_TTL):
        return _COMPARE_CACHE[cache_key]["data"]

    period_map = {
        "1m": ("1mo", "1d"),
        "1mo": ("1mo", "1d"),
        "3m": ("3mo", "1d"),
        "3mo": ("3mo", "1d"),
        "6m": ("6mo", "1d"),
        "6mo": ("6mo", "1d"),
        "1y": ("1y", "1d"),
        "5y": ("5y", "1wk"),
    }
    yf_period, yf_interval = period_map.get(period.lower(), ("6mo", "1d"))

    def fetch_history(sym):
        try:
            t = yf.Ticker(sym)
            df = t.history(period=yf_period, interval=yf_interval)
            if df.empty and not sym.endswith(".NS") and not sym.endswith(".BO"):
                alt_sym = f"{sym}.NS"
                alt_df = yf.Ticker(alt_sym).history(period=yf_period, interval=yf_interval)
                if not alt_df.empty:
                    df = alt_df
                    sym = alt_sym

            if df.empty:
                return sym, None

            df = df.dropna(subset=["Close"])
            series = []
            for idx, row in df.iterrows():
                dt_str = str(idx)[:10]
                series.append({
                    "date": dt_str,
                    "timestamp": int(idx.timestamp() * 1000),
                    "close": round(float(row["Close"]), 2)
                })
            return sym, series
        except Exception:
            return sym, None

    histories: Dict[str, List[Dict[str, Any]]] = {}
    with concurrent.futures.ThreadPoolExecutor(max_workers=min(len(sym_list), 8)) as executor:
        results = executor.map(fetch_history, sym_list)
        for sym, s in results:
            if s and len(s) >= 2:
                histories[sym] = s

    active_symbols = list(histories.keys())
    if not active_symbols:
        raise HTTPException(status_code=404, detail="No historical chart data available for selected stocks")

    # Normalize series to 0% baseline (first available candle = 0.0%)
    normalized_series: Dict[str, List[Dict[str, Any]]] = {}
    for sym in active_symbols:
        pts = histories[sym]
        base_price = pts[0]["close"]
        norm_pts = []
        for p in pts:
            pct = round(((p["close"] - base_price) / base_price) * 100.0, 2) if base_price > 0 else 0.0
            norm_pts.append({
                "date": p["date"],
                "timestamp": p["timestamp"],
                "price": p["close"],
                "percent": pct
            })
        normalized_series[sym] = norm_pts

    # Align dates across symbols for Pearson correlation calculation
    dates_union = set()
    for sym, pts in normalized_series.items():
        for p in pts:
            dates_union.add(p["date"])
    sorted_dates = sorted(list(dates_union))

    # Build price lookup table by date
    price_by_date: Dict[str, Dict[str, float]] = {d: {} for d in sorted_dates}
    for sym, pts in normalized_series.items():
        for p in pts:
            price_by_date[p["date"]][sym] = p["price"]

    # Calculate daily percentage returns
    daily_returns: Dict[str, List[float]] = {s: [] for s in active_symbols}
    for i in range(1, len(sorted_dates)):
        prev_d = sorted_dates[i - 1]
        curr_d = sorted_dates[i]
        for sym in active_symbols:
            p_prev = price_by_date[prev_d].get(sym)
            p_curr = price_by_date[curr_d].get(sym)
            if p_prev and p_curr and p_prev > 0:
                ret = (p_curr - p_prev) / p_prev
                daily_returns[sym].append(ret)
            else:
                daily_returns[sym].append(0.0)

    # Compute pairwise Pearson correlation coefficients
    correlations = []
    n_pts = len(sorted_dates) - 1
    if n_pts >= 5:
        for i in range(len(active_symbols)):
            for j in range(i + 1, len(active_symbols)):
                s1 = active_symbols[i]
                s2 = active_symbols[j]
                r1 = daily_returns[s1]
                r2 = daily_returns[s2]

                mean1 = sum(r1) / len(r1)
                mean2 = sum(r2) / len(r2)

                num = sum((r1[k] - mean1) * (r2[k] - mean2) for k in range(len(r1)))
                den1 = sum((r1[k] - mean1) ** 2 for k in range(len(r1)))
                den2 = sum((r2[k] - mean2) ** 2 for k in range(len(r2)))

                den = math.sqrt(den1 * den2)
                coeff = round(num / den, 3) if den > 0 else 0.0
                coeff = max(-1.0, min(1.0, coeff))

                correlations.append({
                    "symbol1": s1,
                    "symbol2": s2,
                    "coefficient": coeff,
                    "strength": "Strong" if abs(coeff) >= 0.7 else "Moderate" if abs(coeff) >= 0.3 else "Weak",
                    "direction": "Positive" if coeff > 0.05 else "Negative" if coeff < -0.05 else "Neutral"
                })

    correlations.sort(key=lambda x: abs(x["coefficient"]), reverse=True)

    # Compute performance statistics
    stats = []
    for sym in active_symbols:
        pts = normalized_series[sym]
        first_p = pts[0]["price"]
        last_p = pts[-1]["price"]
        total_ret = round(((last_p - first_p) / first_p) * 100.0, 2) if first_p > 0 else 0.0
        
        prices = [p["price"] for p in pts]
        high_p = max(prices)
        low_p = min(prices)
        max_gain = round(((high_p - first_p) / first_p) * 100.0, 2) if first_p > 0 else 0.0
        max_drawdown = round(((low_p - high_p) / high_p) * 100.0, 2) if high_p > 0 else 0.0

        rets = daily_returns[sym]
        volatility = round(float(np.std(rets) * math.sqrt(252) * 100.0), 2) if len(rets) > 1 else 0.0

        stats.append({
            "symbol": sym,
            "name": NAME_MAP.get(sym, sym.replace(".NS", "").replace(".BO", "")),
            "startPrice": first_p,
            "currentPrice": last_p,
            "periodReturn": total_ret,
            "peakGain": max_gain,
            "maxDrawdown": max_drawdown,
            "annualizedVolatility": volatility,
            "currencySymbol": "₹" if sym.endswith(".NS") or sym.endswith(".BO") else "$"
        })

    stats.sort(key=lambda x: x["periodReturn"], reverse=True)

    result = {
        "symbols": active_symbols,
        "period": period,
        "dates": sorted_dates,
        "series": normalized_series,
        "correlations": correlations,
        "stats": stats,
        "updatedAt": int(now * 1000)
    }

    _COMPARE_CACHE[cache_key] = {"time": now, "data": result}
    return result
