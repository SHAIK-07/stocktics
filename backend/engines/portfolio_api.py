"""
Stocktics Portfolio, Watchlist & Authentication API
Connects to Supabase server-side with service-role security and exposes clean REST endpoints
for the web and mobile applications.
"""

import os
import time
import datetime
from typing import Optional, List, Dict, Any
from fastapi import APIRouter, Query, HTTPException, Header, Body
from pydantic import BaseModel
import requests
import concurrent.futures
import yfinance as yf

router = APIRouter(tags=["Portfolio & Auth"])

# Supabase Credentials from Environment
SUPABASE_URL = os.getenv("SUPABASE_URL", "").rstrip("/")
SUPABASE_ANON_KEY = os.getenv("SUPABASE_KEY") or os.getenv("SUPABASE_PUBLISHABLE_KEY") or ""
SUPABASE_SERVICE_ROLE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY") or os.getenv("SUPABASE_SECRET_KEY") or ""

def get_service_headers(prefer: Optional[str] = None) -> Dict[str, str]:
    headers = {
        "apikey": SUPABASE_SERVICE_ROLE_KEY,
        "Authorization": f"Bearer {SUPABASE_SERVICE_ROLE_KEY}",
        "Content-Type": "application/json; charset=UTF-8"
    }
    if prefer:
        headers["Prefer"] = prefer
    return headers

def get_anon_headers() -> Dict[str, str]:
    return {
        "apikey": SUPABASE_ANON_KEY,
        "Authorization": f"Bearer {SUPABASE_ANON_KEY}",
        "Content-Type": "application/json; charset=UTF-8"
    }

# -----------------------------------------------------------------------------
# Real-Time Price Enrichment (Delegated to Unified Quote Service)
# -----------------------------------------------------------------------------
from backend.engines.quote_service import get_batch_quotes as batch_enrich_symbols, fetch_single_quote


# -----------------------------------------------------------------------------
# Request & Response Models
# -----------------------------------------------------------------------------
class SignUpRequest(BaseModel):
    email: str
    password: str
    display_name: Optional[str] = None

class SignInRequest(BaseModel):
    email: str
    password: str

class WatchlistAddRequest(BaseModel):
    user_id: str
    symbol: str
    exchange: Optional[str] = "NSE"
    watchlist_name: Optional[str] = "Default"

class WatchlistDeleteRequest(BaseModel):
    user_id: str
    symbol: str

class BuyPositionRequest(BaseModel):
    user_id: str
    symbol: str
    exchange: Optional[str] = "NSE"
    entry_price: float
    qty: float
    stop_loss_pct: Optional[float] = 5.0
    target_pct: Optional[float] = 15.0
    trailing_stop_pct: Optional[float] = 4.0
    max_hold_days: Optional[int] = 60

class ExitPositionRequest(BaseModel):
    user_id: str
    symbol: str
    exit_price: float
    exit_qty: float
    exit_reason: Optional[str] = "manual"
    realized_pnl: Optional[float] = 0.0
    is_full_exit: Optional[bool] = True
    remaining_qty: Optional[float] = 0.0
    exchange: Optional[str] = "NSE"
    holding_id: Optional[str] = None

class UpdateHoldingRequest(BaseModel):
    stop_loss_pct: Optional[float] = None
    target_pct: Optional[float] = None
    trailing_stop_pct: Optional[float] = None
    max_hold_days: Optional[int] = None

# =============================================================================
# 1. AUTHENTICATION ENDPOINTS
# =============================================================================

@router.post("/auth/signup")
def signup(req: SignUpRequest):
    """
    Creates a new user via Supabase Auth and seeds user_profiles.
    """
    trimmed_email = req.email.strip().lower()
    try:
        url = f"{SUPABASE_URL}/auth/v1/signup"
        res = requests.post(url, headers=get_anon_headers(), json={"email": trimmed_email, "password": req.password}, timeout=10)
        data = res.json()
        if res.status_code not in (200, 201):
            err_msg = data.get("msg") or data.get("error_description") or data.get("message") or "Signup failed"
            return {"success": False, "error": err_msg}

        user_obj = data.get("user") or data
        user_id = user_obj.get("id") or f"user_{int(time.time())}"
        access_token = data.get("access_token") or ""

        # Create/upsert user_profiles row
        display_name = req.display_name or trimmed_email.split("@")[0].capitalize()
        profile_url = f"{SUPABASE_URL}/rest/v1/user_profiles"
        requests.post(profile_url, headers=get_service_headers("resolution=merge-duplicates"), json={
            "user_id": user_id,
            "display_name": display_name,
            "currency": "INR",
            "theme": "dark",
            "risk_profile": "moderate"
        }, timeout=5)

        return {
            "success": True,
            "user": {
                "userId": user_id,
                "email": trimmed_email,
                "displayName": display_name,
                "token": access_token
            }
        }
    except Exception as e:
        return {"success": False, "error": str(e)}

@router.post("/auth/login")
def login(req: SignInRequest):
    """
    Logs in existing user and returns session and profile data.
    """
    trimmed_email = req.email.strip().lower()
    try:
        url = f"{SUPABASE_URL}/auth/v1/token?grant_type=password"
        res = requests.post(url, headers=get_anon_headers(), json={"email": trimmed_email, "password": req.password}, timeout=10)
        data = res.json()
        if res.status_code != 200:
            err_msg = data.get("error_description") or data.get("msg") or data.get("message") or "Invalid email or password"
            return {"success": False, "error": err_msg}

        user_obj = data.get("user") or {}
        user_id = user_obj.get("id") or ""
        access_token = data.get("access_token") or ""

        # Fetch profile
        display_name = trimmed_email.split("@")[0].capitalize()
        currency = "INR"
        if user_id:
            try:
                prof_url = f"{SUPABASE_URL}/rest/v1/user_profiles?user_id=eq.{user_id}&select=*"
                prof_res = requests.get(prof_url, headers=get_service_headers(), timeout=5)
                if prof_res.status_code == 200:
                    prof_data = prof_res.json()
                    if prof_data and len(prof_data) > 0:
                        display_name = prof_data[0].get("display_name") or display_name
                        currency = prof_data[0].get("currency") or currency
            except Exception:
                pass

        return {
            "success": True,
            "user": {
                "userId": user_id,
                "email": trimmed_email,
                "displayName": display_name,
                "currency": currency,
                "token": access_token
            }
        }
    except Exception as e:
        return {"success": False, "error": str(e)}

@router.get("/auth/me")
def get_current_user(authorization: Optional[str] = Header(None)):
    """
    Validates token and returns current user info.
    """
    if not authorization:
        raise HTTPException(status_code=401, detail="Missing authorization header")
    try:
        url = f"{SUPABASE_URL}/auth/v1/user"
        res = requests.get(url, headers={"apikey": SUPABASE_ANON_KEY, "Authorization": authorization}, timeout=8)
        if res.status_code != 200:
            raise HTTPException(status_code=401, detail="Invalid session token")
        data = res.json()
        user_id = data.get("id")
        email = data.get("email")
        return {"userId": user_id, "email": email}
    except Exception as e:
        raise HTTPException(status_code=401, detail=str(e))

# =============================================================================
# 2. WATCHLIST ENDPOINTS (Enriched with Live Prices)
# =============================================================================

@router.get("/watchlist")
def get_watchlist(
    user_id: str = Query(..., description="User ID"),
    market: Optional[str] = Query("US", description="Market: IN or US")
):
    """
    Returns user watchlist from Supabase enriched with live batch quotes.
    """
    is_indian = (market or "US").strip().upper() == "IN"
    try:
        url = f"{SUPABASE_URL}/rest/v1/user_watchlists?user_id=eq.{user_id}&select=*&order=created_at.desc"
        res = requests.get(url, headers=get_service_headers(), timeout=8)
        if res.status_code != 200:
            return {"success": False, "items": []}
        raw_items = res.json()

        # Filter by market
        filtered = []
        symbols_to_quote = []
        for item in raw_items:
            sym = item.get("symbol", "").strip().upper()
            exch = item.get("exchange", "")
            is_sym_indian = sym.endswith(".NS") or sym.endswith(".BO") or exch in ["NSE", "BSE"]
            if is_indian == is_sym_indian:
                filtered.append(item)
                symbols_to_quote.append(sym)

        # Batch enrich with live prices
        quotes = batch_enrich_symbols(symbols_to_quote)

        enriched = []
        for item in filtered:
            sym = item.get("symbol", "").strip().upper()
            q = quotes.get(sym, {})
            price = q.get("price", 0.0)
            chg = q.get("change", 0.0)
            chg_pct = q.get("changePercent", 0.0)
            is_pos = q.get("isPositive", True)

            sign = "+" if chg >= 0 else ""
            formatted_chg = f"{sign}{chg:.2f} ({sign}{chg_pct:.2f}%)" if price > 0 else "+0.00 (0.00%)"

            clean_name = sym.replace(".NS", "").replace(".BO", "")
            cat_name = item.get("watchlist_name") or "Default"
            enriched.append({
                "id": item.get("id"),
                "symbol": sym,
                "exchange": item.get("exchange") or ("NSE" if sym.endswith(".NS") else "NASDAQ"),
                "name": clean_name,
                "price": price,
                "change": formatted_chg,
                "changePercent": chg_pct,
                "isPositive": is_pos,
                "fiftyTwoWeekHigh": q.get("fiftyTwoWeekHigh", 0.0),
                "fiftyTwoWeekLow": q.get("fiftyTwoWeekLow", 0.0),
                "logoUrl": f"https://assets.parqet.com/logos/symbol/{sym}?format=png",
                "addedDate": item.get("added_date") or item.get("created_at", "")[:10],
                "syncSource": item.get("sync_source") or "manual",
                "isSynced": item.get("sync_source") == "indmoney",
                "category": cat_name,
                "watchlistName": cat_name
            })

        category_counts = {}
        for it in enriched:
            cat = it.get("category") or "Default"
            category_counts[cat] = category_counts.get(cat, 0) + 1

        categories_summary = [{"name": "All", "count": len(enriched)}]
        for cat, cnt in sorted(category_counts.items(), key=lambda x: -x[1]):
            categories_summary.append({"name": cat, "count": cnt})

        return {"success": True, "count": len(enriched), "items": enriched, "categories": categories_summary}
    except Exception as e:
        print(f"[WatchlistAPI] Error: {e}")
        return {"success": False, "error": str(e), "items": [], "categories": []}

@router.post("/watchlist")
def add_watchlist_item(req: WatchlistAddRequest):
    """
    Adds a ticker to user_watchlists in Supabase.
    """
    sym = req.symbol.strip().upper()
    exch = req.exchange or ("NSE" if sym.endswith(".NS") else "NASDAQ")
    today = datetime.date.today().isoformat()
    try:
        url = f"{SUPABASE_URL}/rest/v1/user_watchlists"
        payload = {
            "user_id": req.user_id,
            "watchlist_name": req.watchlist_name or "Default",
            "symbol": sym,
            "exchange": exch,
            "added_date": today
        }
        res = requests.post(url, headers=get_service_headers("resolution=merge-duplicates"), json=payload, timeout=8)
        return {"success": res.status_code in (200, 201), "symbol": sym}
    except Exception as e:
        return {"success": False, "error": str(e)}

@router.delete("/watchlist")
def delete_watchlist_item(req: WatchlistDeleteRequest):
    """
    Deletes a ticker from user_watchlists.
    """
    sym = req.symbol.strip().upper()
    clean = sym.replace(".NS", "").replace(".BO", "")
    variants = list(set([sym, clean, f"{clean}.NS", f"{clean}.BO"]))
    in_clause = "(" + ",".join([f'"{v}"' for v in variants]) + ")"
    try:
        url = f"{SUPABASE_URL}/rest/v1/user_watchlists?user_id=eq.{req.user_id}&symbol=in.{in_clause}"
        res = requests.delete(url, headers=get_service_headers(), timeout=8)
        return {"success": res.status_code in (200, 204)}
    except Exception as e:
        return {"success": False, "error": str(e)}

# =============================================================================
# 3. HOLDINGS & EXIT RISK ENGINE ENDPOINTS
# =============================================================================

def calculate_ttl_risk(holding: Dict[str, Any], current_price: float) -> Dict[str, Any]:
    """
    Calculates Time-To-Leave (TTL) urgency score (0 to 100) and verdict based on:
    - Stop-loss proximity
    - Target profit proximity
    - Max hold days elapsed
    """
    entry_price = float(holding.get("entry_price") or 0.0)
    sl_pct = float(holding.get("stop_loss_pct") or 0.05)
    tgt_pct = float(holding.get("target_pct") or 0.15)
    max_days = int(holding.get("max_hold_days") or 60)
    entry_date_str = str(holding.get("entry_date") or datetime.date.today().isoformat())[:10]

    days_held = 0
    try:
        entry_d = datetime.date.fromisoformat(entry_date_str)
        days_held = max(0, (datetime.date.today() - entry_d).days)
    except Exception:
        days_held = 0

    if entry_price <= 0 or current_price <= 0:
        return {"score": 20, "verdict": "Safe Hold", "triggers": [], "daysHeld": days_held}

    sl_price = entry_price * (1.0 - sl_pct)
    tgt_price = entry_price * (1.0 + tgt_pct)

    score = 15
    triggers = []

    # 1. Stop loss breach or proximity
    if current_price <= sl_price:
        score = 95
        triggers.append("Stop-Loss Breached")
    elif current_price <= entry_price * (1.0 - sl_pct * 0.75):
        score = max(score, 75)
        triggers.append("Near Stop-Loss (Alert)")

    # 2. Target profit achieved
    if current_price >= tgt_price:
        score = max(score, 85)
        triggers.append("Target Reached (Book Gains)")
    elif current_price >= entry_price * (1.0 + tgt_pct * 0.85):
        score = max(score, 60)
        triggers.append("Near Target (Trailing Stop)")

    # 3. Max hold days exceeded
    if max_days > 0:
        pct_time = days_held / max_days
        if pct_time >= 1.0:
            score = max(score, 70)
            triggers.append(f"Holding Cap Exceeded ({days_held}d)")
        elif pct_time >= 0.8:
            score = max(score, 45)
            triggers.append("Time Window Closing")

    score = min(100, max(0, score))
    if score >= 80:
        verdict = "EXIT NOW"
    elif score >= 60:
        verdict = "CONSIDER EXITING"
    elif score >= 40:
        verdict = "HOLD POSITION"
    else:
        verdict = "SAFE HOLD"

    return {"score": score, "verdict": verdict, "triggers": triggers, "daysHeld": days_held}

@router.get("/holdings")
def get_holdings(
    user_id: str = Query(..., description="User ID"),
    market: Optional[str] = Query("US", description="Market: IN or US")
):
    """
    Returns active holdings with live calculated P&L and TTL risk engine scores.
    """
    is_indian = (market or "US").strip().upper() == "IN"
    try:
        url = f"{SUPABASE_URL}/rest/v1/user_holdings?user_id=eq.{user_id}&status=eq.open&select=*&order=created_at.desc"
        res = requests.get(url, headers=get_service_headers(), timeout=8)
        if res.status_code != 200:
            return {"success": False, "holdings": []}
        raw_holdings = res.json()

        # Filter by market and consolidate multiple lots/records for the same symbol (eliminates double stocks)
        by_symbol = {}
        for h in raw_holdings:
            sym = h.get("symbol", "").strip().upper()
            if not sym:
                continue
            exch = h.get("exchange", "")
            is_sym_indian = sym.endswith(".NS") or sym.endswith(".BO") or exch in ["NSE", "BSE"]
            if is_indian != is_sym_indian:
                continue

            if sym not in by_symbol:
                by_symbol[sym] = dict(h)
            else:
                # Merge duplicate positions: sum quantities and combine invested value
                prev = by_symbol[sym]
                q1 = float(prev.get("qty") or 0.0)
                q2 = float(h.get("qty") or 0.0)
                inv1 = float(prev.get("entry_price") or 0.0) * q1
                inv2 = float(h.get("entry_price") or 0.0) * q2
                tot_q = q1 + q2
                tot_inv = inv1 + inv2
                prev["qty"] = tot_q
                prev["entry_price"] = (tot_inv / tot_q) if tot_q > 0 else prev.get("entry_price")
                prev["current_value"] = float(prev.get("current_value") or 0.0) + float(h.get("current_value") or 0.0)

        filtered = list(by_symbol.values())
        symbols_to_quote = list(by_symbol.keys())

        quotes = batch_enrich_symbols(symbols_to_quote)

        enriched = []
        for h in filtered:
            sym = h.get("symbol", "").strip().upper()
            entry_p = float(h.get("entry_price") or 0.0)
            qty = float(h.get("qty") or 0.0)
            q = quotes.get(sym, {})
            
            synced_current = float(h.get("current_value") or 0.0)
            synced_price = (synced_current / qty) if (qty > 0 and synced_current > 0) else entry_p
            current_p = q.get("price") if (q.get("price") and q.get("price") > 0) else synced_price
            prev_close = q.get("previousClose") or current_p

            ttl_info = calculate_ttl_risk(h, current_p)
            unrealized_pnl = (current_p - entry_p) * qty
            roi_pct = ((current_p - entry_p) / entry_p * 100.0) if entry_p > 0 else 0.0

            enriched.append({
                "id": h.get("id"),
                "userId": h.get("user_id"),
                "symbol": sym,
                "name": sym.replace(".NS", "").replace(".BO", ""),
                "exchange": h.get("exchange") or ("NSE" if sym.endswith(".NS") else "NASDAQ"),
                "currency": "₹" if is_indian else "$",
                "isIndian": is_indian,
                "entryPrice": entry_p,
                "currentPrice": round(current_p, 2),
                "previousClose": round(prev_close, 2),
                "qty": qty,
                "investedValue": round(entry_p * qty, 2),
                "currentValue": round(current_p * qty, 2),
                "unrealizedPnl": round(unrealized_pnl, 2),
                "roiPct": round(roi_pct, 2),
                "isProfit": unrealized_pnl >= 0,
                "entryDate": h.get("entry_date") or "",
                "daysHeld": ttl_info["daysHeld"],
                "stopLossPct": float(h.get("stop_loss_pct") or 0.05) * 100.0,
                "targetPct": float(h.get("target_pct") or 0.15) * 100.0,
                "trailingStopPct": float(h.get("trailing_stop_pct") or 0.04) * 100.0,
                "maxHoldDays": int(h.get("max_hold_days") or 60),
                "timeToLeaveScore": ttl_info["score"],
                "exitVerdict": ttl_info["verdict"],
                "riskTriggers": ttl_info["triggers"],
                "logoUrl": f"https://assets.parqet.com/logos/symbol/{sym}?format=png",
                "syncSource": h.get("sync_source") or "manual",
                "externalBroker": h.get("external_broker") or ("INDmoney" if h.get("sync_source") == "indmoney" else None),
                "isSynced": h.get("sync_source") == "indmoney"
            })


        return {"success": True, "count": len(enriched), "holdings": enriched}
    except Exception as e:
        print(f"[HoldingsAPI] Error: {e}")
        return {"success": False, "error": str(e), "holdings": []}

@router.post("/holdings/buy")
def log_buy_position(req: BuyPositionRequest):
    """
    Logs a buy trade into user_holdings and user_transactions.
    """
    sym = req.symbol.strip().upper()
    exch = req.exchange or ("NSE" if sym.endswith(".NS") else "NASDAQ")
    today = datetime.date.today().isoformat()
    entry_p = float(req.entry_price)
    qty = float(req.qty)

    try:
        # 1. Insert holding
        holding_payload = {
            "user_id": req.user_id,
            "symbol": sym,
            "exchange": exch,
            "entry_price": entry_p,
            "entry_date": today,
            "qty": qty,
            "stop_loss_pct": (req.stop_loss_pct or 5.0) / 100.0,
            "target_pct": (req.target_pct or 15.0) / 100.0,
            "trailing_stop_pct": (req.trailing_stop_pct or 4.0) / 100.0,
            "max_hold_days": req.max_hold_days or 60,
            "status": "open",
            "time_to_leave_score": 20,
            "exit_verdict": "Safe Hold"
        }
        res_h = requests.post(f"{SUPABASE_URL}/rest/v1/user_holdings", headers=get_service_headers("return=representation"), json=holding_payload, timeout=8)
        holding_id = None
        if res_h.status_code in (200, 201):
            created = res_h.json()
            if isinstance(created, list) and len(created) > 0:
                holding_id = created[0].get("id")

        # 2. Record transaction
        tx_payload = {
            "user_id": req.user_id,
            "holding_id": holding_id,
            "symbol": sym,
            "exchange": exch,
            "tx_type": "BUY",
            "price": entry_p,
            "qty": qty,
            "total_amount": round(entry_p * qty, 2),
            "realized_pnl": 0.0,
            "tx_date": today
        }
        requests.post(f"{SUPABASE_URL}/rest/v1/user_transactions", headers=get_service_headers(), json=tx_payload, timeout=8)

        return {"success": True, "holdingId": holding_id, "symbol": sym}
    except Exception as e:
        return {"success": False, "error": str(e)}

@router.post("/holdings/exit")
def log_exit_position(req: ExitPositionRequest):
    """
    Executes a full or partial position exit.
    """
    sym = req.symbol.strip().upper()
    today = datetime.date.today().isoformat()
    try:
        # 1. Update user_holdings
        url = f"{SUPABASE_URL}/rest/v1/user_holdings?user_id=eq.{req.user_id}&symbol=eq.{sym}&status=eq.open"
        if req.is_full_exit:
            update_data = {
                "status": "closed",
                "exit_price": req.exit_price,
                "exit_date": today,
                "exit_reason": req.exit_reason or "manual",
                "updated_at": datetime.datetime.utcnow().isoformat()
            }
        else:
            update_data = {
                "qty": req.remaining_qty,
                "updated_at": datetime.datetime.utcnow().isoformat()
            }
        requests.patch(url, headers=get_service_headers(), json=update_data, timeout=8)

        # 2. Record SELL transaction in user_transactions
        tx_payload = {
            "user_id": req.user_id,
            "holding_id": req.holding_id,
            "symbol": sym,
            "exchange": req.exchange or ("NSE" if sym.endswith(".NS") else "NASDAQ"),
            "tx_type": "SELL",
            "price": req.exit_price,
            "qty": req.exit_qty,
            "total_amount": round(req.exit_price * req.exit_qty, 2),
            "realized_pnl": round(req.realized_pnl or 0.0, 2),
            "tx_date": today
        }
        requests.post(f"{SUPABASE_URL}/rest/v1/user_transactions", headers=get_service_headers(), json=tx_payload, timeout=8)

        return {"success": True, "symbol": sym, "realizedPnl": req.realized_pnl}
    except Exception as e:
        return {"success": False, "error": str(e)}

@router.put("/holdings/{holding_id}")
def update_holding(holding_id: str, req: UpdateHoldingRequest):
    """
    Updates holding parameters such as stop loss, target, or max hold days.
    """
    try:
        update_data = {}
        if req.stop_loss_pct is not None:
            update_data["stop_loss_pct"] = req.stop_loss_pct / 100.0
        if req.target_pct is not None:
            update_data["target_pct"] = req.target_pct / 100.0
        if req.trailing_stop_pct is not None:
            update_data["trailing_stop_pct"] = req.trailing_stop_pct / 100.0
        if req.max_hold_days is not None:
            update_data["max_hold_days"] = req.max_hold_days
        update_data["updated_at"] = datetime.datetime.utcnow().isoformat()

        url = f"{SUPABASE_URL}/rest/v1/user_holdings?id=eq.{holding_id}"
        res = requests.patch(url, headers=get_service_headers(), json=update_data, timeout=8)
        return {"success": res.status_code in (200, 204)}
    except Exception as e:
        return {"success": False, "error": str(e)}

@router.get("/portfolio/summary")
def get_portfolio_summary(
    user_id: str = Query(..., description="User ID"),
    market: Optional[str] = Query("US", description="Market: IN or US")
):
    """
    Returns high-level portfolio metrics (Net worth, 1D Return, Unrealized P&L, Realized P&L).
    """
    holdings_res = get_holdings(user_id=user_id, market=market)
    holdings = holdings_res.get("holdings", [])

    total_invested = sum([h.get("investedValue", 0.0) for h in holdings])
    total_current = sum([h.get("currentValue", 0.0) for h in holdings])
    total_unrealized_pnl = sum([h.get("unrealizedPnl", 0.0) for h in holdings])
    return_pct = ((total_current - total_invested) / total_invested * 100.0) if total_invested > 0 else 0.0

    # Realized P&L from transactions
    realized_pnl = 0.0
    try:
        tx_url = f"{SUPABASE_URL}/rest/v1/user_transactions?user_id=eq.{user_id}&tx_type=eq.SELL&select=realized_pnl"
        tx_res = requests.get(tx_url, headers=get_service_headers(), timeout=5)
        if tx_res.status_code == 200:
            tx_data = tx_res.json()
            realized_pnl = sum([float(tx.get("realized_pnl") or 0.0) for tx in tx_data])
    except Exception:
        pass

    return {
        "success": True,
        "market": market,
        "activePositions": len(holdings),
        "totalInvested": round(total_invested, 2),
        "netWorth": round(total_current, 2),
        "unrealizedPnl": round(total_unrealized_pnl, 2),
        "returnPct": round(return_pct, 2),
        "realizedPnl": round(realized_pnl, 2),
        "currency": "₹" if (market or "US").upper() == "IN" else "$"
    }

@router.post("/portfolio/clear")
def clear_user_portfolio(req: Dict[str, Any] = Body(...)):
    """
    Clears all manual and legacy holdings, transactions, and watchlists for a user to start fresh.
    """
    user_id = req.get("user_id")
    if not user_id:
        raise HTTPException(status_code=400, detail="Missing user_id")
    try:
        requests.delete(f"{SUPABASE_URL}/rest/v1/user_holdings?user_id=eq.{user_id}", headers=get_service_headers())
        requests.delete(f"{SUPABASE_URL}/rest/v1/user_transactions?user_id=eq.{user_id}", headers=get_service_headers())
        requests.delete(f"{SUPABASE_URL}/rest/v1/user_watchlists?user_id=eq.{user_id}", headers=get_service_headers())
        return {"success": True, "message": "Portfolio wiped clean. Ready for fresh INDmoney sync."}
    except Exception as e:
        return {"success": False, "error": str(e)}

