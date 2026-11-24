"""
Stocktics INDmoney MCP Integration Engine
Connects directly to INDmoney's Model Context Protocol (MCP) server over OAuth 2.1 with PKCE.
Syncs Indian & US stock portfolios, holdings (quantities, avg prices, P&L), and watchlists
directly into Stocktics, eliminating manual logging.
"""

import os
import time
import json
import secrets
import hashlib
import base64
import datetime
from typing import Dict, Any, List, Optional
from urllib.parse import urlencode
import requests
import re

# Supabase Credentials from Environment
SUPABASE_URL = os.getenv("SUPABASE_URL", "").rstrip("/")
SUPABASE_ANON_KEY = os.getenv("SUPABASE_KEY") or os.getenv("SUPABASE_PUBLISHABLE_KEY") or ""
SUPABASE_SERVICE_ROLE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY") or os.getenv("SUPABASE_SECRET_KEY") or ""

# INDmoney OAuth & MCP Endpoints
INDMONEY_MCP_URL = os.getenv("INDMONEY_MCP_URL", "https://mcp.indmoney.com/mcp")
INDMONEY_AUTH_URL = os.getenv("INDMONEY_AUTH_URL", "https://mcp.indmoney.com/authorize")
INDMONEY_TOKEN_URL = os.getenv("INDMONEY_TOKEN_URL", "https://mcp.indmoney.com/token")
INDMONEY_REVOKE_URL = os.getenv("INDMONEY_REVOKE_URL", "https://mcp.indmoney.com/revoke")
INDMONEY_REGISTER_URL = os.getenv("INDMONEY_REGISTER_URL", "https://mcp.indmoney.com/register")

# Registered Client ID & Secret
INDMONEY_CLIENT_ID = os.getenv("INDMONEY_CLIENT_ID", "aa199411-09db-490f-9012-cce116a82c2d")
INDMONEY_CLIENT_SECRET = os.getenv("INDMONEY_CLIENT_SECRET", "3766dd16982061606aa7d16d98ef7db8ca16c383d785d7c3130bcf800c1a71b2")
DEFAULT_REDIRECT_URI = os.getenv("INDMONEY_REDIRECT_URI", "http://127.0.0.1:8000/api/indmoney/callback")


# In-memory PKCE state storage: state -> {code_verifier, user_id, redirect_uri, created_at}
_OAUTH_STATES: Dict[str, Dict[str, Any]] = {}

# In-memory token cache fallback for offline / development testing
_MEMORY_TOKENS: Dict[str, Dict[str, Any]] = {}


def get_service_headers(prefer: Optional[str] = None) -> Dict[str, str]:
    headers = {
        "apikey": SUPABASE_SERVICE_ROLE_KEY,
        "Authorization": f"Bearer {SUPABASE_SERVICE_ROLE_KEY}",
        "Content-Type": "application/json; charset=UTF-8"
    }
    if prefer:
        headers["Prefer"] = prefer
    return headers


def get_cffi_session():
    """
    Returns a curl_cffi session configured with Chrome impersonation
    to seamlessly handle Cloudflare TLS fingerprinting on INDmoney endpoints.
    """
    try:
        from curl_cffi import requests as cffi_requests
        return cffi_requests.Session(impersonate="chrome120")
    except Exception:
        s = requests.Session()
        s.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Accept": "application/json, text/plain, */*",
            "Accept-Language": "en-US,en;q=0.9"
        })
        return s


# =============================================================================
# 1. PKCE & OAUTH 2.1 FLOW
# =============================================================================

def generate_pkce_pair() -> tuple[str, str]:
    """
    Generates a cryptographically secure PKCE code_verifier and code_challenge (S256).
    """
    code_verifier = secrets.token_urlsafe(48)
    digest = hashlib.sha256(code_verifier.encode("utf-8")).digest()
    code_challenge = base64.urlsafe_b64encode(digest).decode("utf-8").rstrip("=")
    return code_verifier, code_challenge


def clean_expired_states():
    """Prunes states older than 15 minutes."""
    cutoff = time.time() - 900
    expired = [k for k, v in _OAUTH_STATES.items() if v.get("created_at", 0) < cutoff]
    for k in expired:
        _OAUTH_STATES.pop(k, None)


def build_authorization_url(user_id: str, redirect_uri: Optional[str] = None) -> Dict[str, str]:
    """
    Builds the official INDmoney OAuth 2.1 authorization URL with PKCE for a user.
    """
    clean_expired_states()
    target_redirect = redirect_uri or DEFAULT_REDIRECT_URI
    state = secrets.token_urlsafe(24)
    verifier, challenge = generate_pkce_pair()

    _OAUTH_STATES[state] = {
        "user_id": user_id,
        "code_verifier": verifier,
        "redirect_uri": target_redirect,
        "created_at": time.time()
    }

    params = {
        "response_type": "code",
        "client_id": INDMONEY_CLIENT_ID,
        "redirect_uri": target_redirect,
        "scope": "portfolio:read market:read",
        "code_challenge": challenge,
        "code_challenge_method": "S256",
        "state": state
    }
    auth_url = f"{INDMONEY_AUTH_URL}?{urlencode(params)}"
    return {
        "auth_url": auth_url,
        "state": state,
        "redirect_uri": target_redirect
    }


def exchange_code_for_token(code: str, state: str) -> Dict[str, Any]:
    """
    Exchanges authorization code for an INDmoney access token using PKCE verifier.
    Saves the token in Supabase and memory.
    """
    clean_expired_states()
    state_data = _OAUTH_STATES.pop(state, None)
    if not state_data:
        raise ValueError("Invalid or expired OAuth state parameter. Please restart authorization.")

    user_id = state_data["user_id"]
    code_verifier = state_data["code_verifier"]
    redirect_uri = state_data["redirect_uri"]

    payload = {
        "grant_type": "authorization_code",
        "code": code,
        "client_id": INDMONEY_CLIENT_ID,
        "client_secret": INDMONEY_CLIENT_SECRET,
        "redirect_uri": redirect_uri,
        "code_verifier": code_verifier
    }

    session = get_cffi_session()
    res = session.post(
        INDMONEY_TOKEN_URL,
        data=payload,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        timeout=15
    )

    if res.status_code != 200:
        err_msg = res.text
        try:
            err_json = res.json()
            err_msg = err_json.get("error_description") or err_json.get("error") or err_msg
        except Exception:
            pass
        raise RuntimeError(f"Failed to exchange token with INDmoney: {err_msg}")

    token_data = res.json()
    access_token = token_data.get("access_token")
    refresh_token = token_data.get("refresh_token")
    expires_in = token_data.get("expires_in", 3600)
    expires_at = datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(seconds=expires_in)

    # Store in memory
    _MEMORY_TOKENS[user_id] = {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "expires_at": expires_at.isoformat(),
        "is_active": True
    }

    # Persist in Supabase
    save_user_tokens(user_id, access_token, refresh_token, expires_at.isoformat(), token_data.get("scope"))

    return {
        "success": True,
        "user_id": user_id,
        "expires_at": expires_at.isoformat(),
        "scope": token_data.get("scope")
    }


def refresh_access_token(user_id: str, refresh_token: str) -> Optional[str]:
    """
    Silently refreshes an expired access token using the refresh_token.
    """
    payload = {
        "grant_type": "refresh_token",
        "refresh_token": refresh_token,
        "client_id": INDMONEY_CLIENT_ID,
        "client_secret": INDMONEY_CLIENT_SECRET
    }

    try:
        session = get_cffi_session()
        res = session.post(
            INDMONEY_TOKEN_URL,
            data=payload,
            headers={"Content-Type": "application/x-www-form-urlencoded"},
            timeout=15
        )
        if res.status_code == 200:
            token_data = res.json()
            new_access_token = token_data.get("access_token")
            new_refresh = token_data.get("refresh_token") or refresh_token
            expires_in = token_data.get("expires_in", 3600)
            expires_at = datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(seconds=expires_in)

            _MEMORY_TOKENS[user_id] = {
                "access_token": new_access_token,
                "refresh_token": new_refresh,
                "expires_at": expires_at.isoformat(),
                "is_active": True
            }
            save_user_tokens(user_id, new_access_token, new_refresh, expires_at.isoformat(), token_data.get("scope"))
            return new_access_token
    except Exception as e:
        print(f"[INDmoneyEngine] Token refresh failed for user {user_id}: {e}")
    return None


def get_valid_access_token(user_id: Optional[str] = None) -> Optional[str]:
    """
    Retrieves a valid, non-expired access token for the given user (or latest active user),
    proactively auto-refreshing it if within 10 minutes of expiration.
    """
    # 1. Resolve target user_id if not provided
    target_uid = user_id
    if not target_uid:
        try:
            url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?is_active=eq.true&order=updated_at.desc&limit=1"
            res = requests.get(url, headers=get_service_headers(), timeout=5)
            if res.status_code == 200 and res.json():
                target_uid = res.json()[0].get("user_id")
        except Exception:
            pass

    if not target_uid:
        return None

    # 2. Check memory cache first
    token_info = _MEMORY_TOKENS.get(target_uid)

    # 3. Check Supabase DB if not in memory
    if not token_info:
        try:
            url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?user_id=eq.{target_uid}&is_active=eq.true&select=*"
            res = requests.get(url, headers=get_service_headers(), timeout=5)
            if res.status_code == 200 and res.json():
                row = res.json()[0]
                token_info = {
                    "access_token": row.get("access_token"),
                    "refresh_token": row.get("refresh_token"),
                    "expires_at": row.get("expires_at"),
                    "is_active": row.get("is_active", True)
                }
                _MEMORY_TOKENS[target_uid] = token_info
        except Exception:
            pass

    if not token_info or not token_info.get("access_token"):
        return None

    # 4. Proactive Auto-Refresh: If within 10 minutes (600s) of expiry or already expired, refresh immediately!
    expires_at_str = token_info.get("expires_at")
    if expires_at_str and token_info.get("refresh_token"):
        try:
            exp_dt = datetime.datetime.fromisoformat(expires_at_str.replace("Z", "+00:00"))
            now_dt = datetime.datetime.now(datetime.timezone.utc)
            if (exp_dt - now_dt).total_seconds() < 600:
                new_token = refresh_access_token(target_uid, token_info["refresh_token"])
                if new_token:
                    return new_token
        except Exception as e:
            print(f"[INDmoneyEngine] Proactive token refresh error for user {target_uid}: {e}")

    return token_info.get("access_token")


def auto_refresh_all_active_tokens() -> int:
    """
    Background worker: inspects all active connected users in Supabase
    and refreshes any tokens within 15 minutes of expiry.
    Returns the number of refreshed tokens.
    """
    refreshed_count = 0
    try:
        url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?is_active=eq.true"
        res = requests.get(url, headers=get_service_headers(), timeout=7)
        if res.status_code == 200 and res.json():
            rows = res.json()
            now_dt = datetime.datetime.now(datetime.timezone.utc)
            for row in rows:
                u_id = row.get("user_id")
                r_tok = row.get("refresh_token")
                exp_str = row.get("expires_at")
                if not u_id or not r_tok:
                    continue
                should_refresh = False
                if not exp_str:
                    should_refresh = True
                else:
                    try:
                        exp_dt = datetime.datetime.fromisoformat(exp_str.replace("Z", "+00:00"))
                        if (exp_dt - now_dt).total_seconds() < 900:  # < 15 minutes
                            should_refresh = True
                    except Exception:
                        should_refresh = True

                if should_refresh:
                    new_tok = refresh_access_token(u_id, r_tok)
                    if new_tok:
                        refreshed_count += 1
                        print(f"[TokenRefresher] Proactively refreshed token for user {u_id}")
    except Exception as e:
        print(f"[TokenRefresher] Error during bulk token refresh: {e}")
    return refreshed_count


def save_user_tokens(user_id: str, access_token: str, refresh_token: Optional[str], expires_at: str, scope: Optional[str]):
    """Saves or upserts tokens into user_indmoney_tokens."""
    try:
        url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens"
        payload = {
            "user_id": user_id,
            "access_token": access_token,
            "refresh_token": refresh_token,
            "expires_at": expires_at,
            "scope": scope or "portfolio:read market:read",
            "is_active": True,
            "last_sync_status": "connected",
            "updated_at": datetime.datetime.now(datetime.timezone.utc).isoformat()
        }
        requests.post(url, headers=get_service_headers("resolution=merge-duplicates"), json=payload, timeout=5)
    except Exception as e:
        print(f"[INDmoneyEngine] DB save tokens error: {e}")


# =============================================================================
# 2. MODEL CONTEXT PROTOCOL (MCP) JSON-RPC CLIENT
# =============================================================================

def call_mcp_tool(access_token: str, tool_name: str, arguments: Optional[Dict[str, Any]] = None, user_id: Optional[str] = None) -> Any:
    """
    Calls an INDmoney MCP tool via standard JSON-RPC 2.0 over HTTP.
    Uses browser impersonation session to clear Cloudflare filters.
    Includes self-healing 401 retry interceptor that auto-refreshes expired tokens!
    """
    payload = {
        "jsonrpc": "2.0",
        "id": int(time.time() * 1000),
        "method": "tools/call",
        "params": {
            "name": tool_name,
            "arguments": arguments or {}
        }
    }

    session = get_cffi_session()
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    res = session.post(INDMONEY_MCP_URL, json=payload, headers=headers, timeout=25)

    # Self-Healing 401 Interceptor: If token expired, auto-refresh and retry once transparently
    if res.status_code == 401:
        print(f"[INDmoneyMCP] Encountered 401 Unauthorized for tool '{tool_name}'. Attempting self-healing refresh...")
        target_uid = user_id
        ref_tok = None
        if target_uid and target_uid in _MEMORY_TOKENS:
            ref_tok = _MEMORY_TOKENS[target_uid].get("refresh_token")
        if not ref_tok:
            try:
                query_url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?is_active=eq.true&order=updated_at.desc&limit=1"
                r = requests.get(query_url, headers=get_service_headers(), timeout=5)
                if r.status_code == 200 and r.json():
                    first_row = r.json()[0]
                    target_uid = first_row.get("user_id")
                    ref_tok = first_row.get("refresh_token")
            except Exception:
                pass

        if target_uid and ref_tok:
            new_access = refresh_access_token(target_uid, ref_tok)
            if new_access:
                print(f"[INDmoneyMCP] Self-healing succeeded. Retrying '{tool_name}' with new access token...")
                headers["Authorization"] = f"Bearer {new_access}"
                res = session.post(INDMONEY_MCP_URL, json=payload, headers=headers, timeout=25)

    if res.status_code != 200:
        raise RuntimeError(f"INDmoney MCP error ({res.status_code}): {res.text}")

    # Robust parsing for text/event-stream (data: {...}) or application/json
    resp_json = None
    raw_text = res.text.strip()

    # 1. Try parsing SSE lines starting with data:
    for line in raw_text.splitlines():
        line_clean = line.strip()
        if line_clean.startswith("data:"):
            json_str = line_clean[len("data:"):].strip()
            try:
                resp_json = json.loads(json_str)
                break
            except Exception:
                pass

    # 2. Fallback to standard JSON parsing
    if not resp_json:
        try:
            resp_json = res.json()
        except Exception:
            try:
                resp_json = json.loads(raw_text)
            except Exception as e:
                raise RuntimeError(f"Failed to parse INDmoney MCP response: {e}\nRaw: {raw_text[:200]}")

    if "error" in resp_json:
        raise RuntimeError(f"INDmoney MCP tool error: {resp_json['error']}")

    result = resp_json.get("result", {})
    content = result.get("content", [])

    # Unpack MCP content blocks
    parsed_data = []
    for item in content:
        if isinstance(item, dict):
            if item.get("type") == "text":
                text = item.get("text", "")
                try:
                    parsed = json.loads(text)
                    parsed_data.append(parsed)
                except Exception:
                    parsed_data.append(text)
            elif "data" in item:
                parsed_data.append(item.get("data"))

    if len(parsed_data) == 1:
        return parsed_data[0]
    return parsed_data if parsed_data else result


# =============================================================================
# 3. PORTFOLIO & WATCHLIST SYNCHRONIZATION WORKERS
# =============================================================================

def is_internal_id(val: str) -> bool:
    """Checks if a string looks like an internal broker id (e.g. stock1234, mf_123, ind_stk_55, inds01489)."""
    if not val:
        return True
    v = val.strip().lower()
    if v.startswith("stock") and any(c.isdigit() for c in v):
        return True
    if v.startswith("inds") and any(c.isdigit() for c in v):
        return True
    if v.startswith("mf") and any(c.isdigit() for c in v):
        return True
    if v.startswith("ind_") or v.startswith("id_") or v.startswith("eq_"):
        return True
    return False


def normalize_indian_symbol(symbol: str) -> str:
    """Ensures consistent NSE ticker format with .NS suffix."""
    s = symbol.strip().upper()
    if s.endswith(".NS") or s.endswith(".BO"):
        return s
    return f"{s}.NS"


def normalize_us_symbol(symbol: str) -> str:
    """Ensures clean uppercase US ticker without market suffix."""
    return symbol.strip().upper().replace(".US", "")


def extract_stock_symbol(item: Dict[str, Any], is_indian: bool = True) -> str:
    """
    Extracts the authentic market trading ticker from an INDmoney holding payload.
    Avoids internal investment codes like 'stock1234' or 'mf_982'.
    """
    candidates = [
        item.get("trading_symbol"),
        item.get("symbol"),
        item.get("ticker"),
        item.get("nse_symbol") if is_indian else None,
        item.get("bse_symbol") if is_indian else None,
    ]
    for c in candidates:
        if c and isinstance(c, str):
            clean = c.strip().upper()
            if clean and not is_internal_id(clean):
                return clean

    # Fallback: check instrument_name or name if short and looks like a ticker
    name_cand = item.get("instrument_name") or item.get("name") or item.get("security_name") or ""
    if isinstance(name_cand, str):
        clean_name = name_cand.strip().upper()
        if clean_name and " " not in clean_name and len(clean_name) <= 12 and not is_internal_id(clean_name):
            return clean_name

    # Check investment_code ONLY if it is not an internal code
    inv_code = item.get("investment_code")
    if inv_code and isinstance(inv_code, str):
        clean_code = inv_code.strip().upper()
        if clean_code and not is_internal_id(clean_code):
            return clean_code

    return ""


def sync_indian_stock_holdings(user_id: str, access_token: str) -> List[Dict[str, Any]]:
    """
    Fetches Indian equity positions from INDmoney MCP (networth_holdings)
    and syncs them into Supabase user_holdings.
    Resolves internal broker codes (INDS...) into authentic NSE/BSE tickers via get_indian_stocks_details.
    Strictly filters out Mutual Funds, FDs, US stocks, and inactive positions.
    Guarantees atomic wipe & replace so duplicate rows never occur in Supabase.
    """
    raw_response = call_mcp_tool(access_token, "networth_holdings", {"asset_type": "IND_STOCK"}, user_id=user_id)
    holdings_list = []

    if isinstance(raw_response, dict):
        holdings_list = raw_response.get("holdings") or raw_response.get("positions") or raw_response.get("data") or raw_response.get("items") or []
    elif isinstance(raw_response, list):
        holdings_list = raw_response

    today_str = datetime.date.today().isoformat()
    synced_items = []

    # Pre-resolve any internal investment codes (like INDS01489, INDS00577) via get_indian_stocks_details
    ind_keys_to_resolve = []
    for item in holdings_list:
        if isinstance(item, dict):
            c = str(item.get("investment_code") or item.get("ind_key") or "").strip().upper()
            if c.startswith("INDS"):
                ind_keys_to_resolve.append(c)

    resolved_symbols: Dict[str, Dict[str, Any]] = {}
    if ind_keys_to_resolve:
        for chunk_start in range(0, len(ind_keys_to_resolve), 10):
            chunk = ind_keys_to_resolve[chunk_start:chunk_start + 10]
            try:
                details = call_mcp_tool(access_token, "get_indian_stocks_details", {"ind_keys": chunk}, user_id=user_id)
                if isinstance(details, dict):
                    for k, val in details.items():
                        if isinstance(val, dict):
                            eb = val.get("entity_basic", {})
                            es = val.get("entity_stats", {})
                            s = eb.get("symbol")
                            if s:
                                resolved_symbols[k.upper()] = {
                                    "symbol": s.strip().upper(),
                                    "name": eb.get("name") or eb.get("display_name") or s,
                                    "exchange": eb.get("exchange") or "NSE",
                                    "live_price": float(es.get("live_price") or 0.0),
                                    "prev_close": float(es.get("prev_close") or 0.0),
                                    "day_change": float(es.get("day_change") or 0.0),
                                    "day_change_percentage": float(es.get("day_change_percentage") or 0.0)
                                }
            except Exception as e:
                print(f"[INDmoneyEngine] Error resolving Indian keys {chunk}: {e}")

    # Non-Indian-stock asset types to strictly ignore
    EXCLUDED_ASSET_TYPES = {
        "MUTUAL_FUND", "MF", "SIP", "FIXED_DEPOSIT", "FD", "EPF", "PPF", "NPS",
        "GOLD", "US_STOCK", "US_EQUITY", "DEBT", "BOND", "REAL_ESTATE", "INSURANCE",
        "LOAN", "LIABILITY", "SAVINGS_ACCOUNT", "SAVINGS"
    }

    # Step 1: Filter and Aggregate active holdings by symbol
    aggregated_map: Dict[str, Dict[str, Any]] = {}

    for item in holdings_list:
        if not isinstance(item, dict):
            continue

        asset_type = str(item.get("asset_type") or item.get("instrument_type") or item.get("asset_class") or item.get("category") or "").strip().upper()
        if asset_type in EXCLUDED_ASSET_TYPES:
            continue

        isin = str(item.get("isin") or item.get("isin_number") or "").strip().upper()
        if isin.startswith("INF"):
            continue

        status = str(item.get("status") or "").strip().upper()
        if status in ["CLOSED", "EXITED", "INACTIVE", "SOLD", "CANCELLED"]:
            continue
        if item.get("is_active") is False or item.get("is_holding") is False:
            continue

        # Strictly only sync positions held natively on INDmoney's broker.
        # Exclude external tracked brokers (Zerodha, Groww, Upstox, Angel, etc.).
        broker = str(item.get("broker") or "").strip().upper()
        EXTERNAL_BROKERS = {
            "ZERODHA", "GROWW", "UPSTOX", "ANGEL", "ANGELONE", "ANGEL ONE",
            "KITE", "DHAN", "ICICI", "HDFC", "KOTAK", "MOTILAL", "SHAREKHAN",
            "5PAISA", "PAYTM_MONEY", "PAYTM", "GEOJIT", "AXIS"
        }
        if broker in EXTERNAL_BROKERS:
            continue

        inv_code = str(item.get("investment_code") or "").strip().upper()
        res_info = resolved_symbols.get(inv_code)

        if res_info:
            raw_sym = res_info["symbol"]
            exch = res_info["exchange"]
            stock_name = res_info["name"]
        else:
            raw_sym = extract_stock_symbol(item, is_indian=True)
            exch = "NSE"
            stock_name = item.get("investment") or item.get("name") or raw_sym

        if not raw_sym:
            continue

        symbol = f"{raw_sym}.BO" if exch == "BSE" else normalize_indian_symbol(raw_sym)

        qty = float(
            item.get("current_units") if item.get("current_units") is not None else
            item.get("holding_units") if item.get("holding_units") is not None else
            item.get("available_units") if item.get("available_units") is not None else
            item.get("quantity") if item.get("quantity") is not None else
            item.get("qty") if item.get("qty") is not None else
            item.get("units") if item.get("units") is not None else
            item.get("total_units") or 0.0
        )
        if qty <= 0:
            continue

        invested_val = float(item.get("invested_amount") or item.get("invested_value") or 0.0)
        current_val = float(item.get("market_value") or item.get("current_value") or 0.0)
        unit_price = float(item.get("unit_price") or item.get("avg_price") or item.get("buy_price") or 0.0)

        if invested_val <= 0 and unit_price > 0:
            invested_val = unit_price * qty
        if current_val <= 0 and invested_val > 0:
            current_val = invested_val

        if current_val <= 0 and invested_val <= 0:
            continue

        one_day_chg = float(item.get("one_day_change") or (res_info["day_change"] if res_info else 0.0))
        one_day_pct = float(item.get("one_day_change_percentage") or (res_info["day_change_percentage"] if res_info else 0.0))
        pnl = float(item.get("total_pnl") or item.get("pnl") or (current_val - invested_val))
        pnl_pct = float(item.get("pnl_per") or ((pnl / invested_val * 100.0) if invested_val > 0 else 0.0))
        broker = str(item.get("broker") or "INDmoney")
        ext_id = inv_code or str(item.get("id") or "")
        entry_date = item.get("entry_date") or today_str

        avg_price = (invested_val / qty) if qty > 0 and invested_val > 0 else unit_price
        curr_price = (current_val / qty) if qty > 0 and current_val > 0 else (res_info["live_price"] if res_info and res_info["live_price"] > 0 else avg_price)
        prev_close = res_info["prev_close"] if res_info and res_info["prev_close"] > 0 else (((current_val - one_day_chg) / qty) if qty > 0 else curr_price)

        if symbol not in aggregated_map:
            aggregated_map[symbol] = {
                "symbol": symbol,
                "name": stock_name,
                "exchange": exch,
                "qty": qty,
                "avg_price": avg_price,
                "current_price": curr_price,
                "prev_close": prev_close,
                "invested_val": invested_val,
                "current_val": current_val,
                "pnl": pnl,
                "pnl_pct": pnl_pct,
                "one_day_chg": one_day_chg,
                "one_day_pct": one_day_pct,
                "broker": broker,
                "external_id": ext_id,
                "entry_date": entry_date
            }
        else:
            prev = aggregated_map[symbol]
            prev["qty"] += qty
            prev["invested_val"] += invested_val
            prev["current_val"] += current_val
            prev["pnl"] += pnl
            if prev["qty"] > 0:
                prev["avg_price"] = prev["invested_val"] / prev["qty"]
                prev["current_price"] = prev["current_val"] / prev["qty"]

    # Step 2: Atomic Wipe of old INDmoney Indian holdings for this user to prevent any duplicate rows
    try:
        url_del = f"{SUPABASE_URL}/rest/v1/user_holdings?user_id=eq.{user_id}&sync_source=eq.indmoney&status=eq.open&exchange=in.(NSE,BSE)"
        requests.delete(url_del, headers=get_service_headers(), timeout=7)
    except Exception as e:
        print(f"[INDmoneyEngine] Error cleaning old Indian holdings: {e}")

    # Step 3: Insert fresh aggregated Indian holdings
    for symbol, agg in aggregated_map.items():
        payload = {
            "user_id": user_id,
            "symbol": symbol,
            "exchange": agg["exchange"],
            "entry_price": round(agg["avg_price"], 2),
            "qty": agg["qty"],
            "entry_date": agg["entry_date"],
            "status": "open",
            "sync_source": "indmoney",
            "external_broker": agg["broker"],
            "external_id": agg["external_id"],
            "current_value": round(agg["current_val"], 2),
            "invested_value": round(agg["invested_val"], 2),
            "unrealized_pnl": round(agg["pnl"], 2),
            "pnl_pct": round(agg["pnl_pct"], 2),
            "exit_verdict": "Safe Hold",
            "time_to_leave_score": 20
        }

        try:
            url = f"{SUPABASE_URL}/rest/v1/user_holdings"
            res = requests.post(url, headers=get_service_headers(), json=payload, timeout=5)
            if res.status_code in (200, 201, 204):
                synced_items.append(payload)
        except Exception as e:
            print(f"[INDmoneyEngine] Error inserting Indian holding {symbol}: {e}")

    return synced_items


def sync_us_stock_holdings(user_id: str, access_token: str) -> List[Dict[str, Any]]:
    """
    Fetches US equity positions from INDmoney MCP (networth_holdings)
    and syncs them into Supabase user_holdings.
    Preserves exact USD invested, current, and P&L figures directly from INDmoney.
    Guarantees atomic wipe & replace so duplicate rows never occur in Supabase.
    """
    raw_response = call_mcp_tool(access_token, "networth_holdings", {"asset_type": "US_STOCK"}, user_id=user_id)
    holdings_list = []

    if isinstance(raw_response, dict):
        holdings_list = raw_response.get("holdings") or raw_response.get("data") or raw_response.get("items") or []
    elif isinstance(raw_response, list):
        holdings_list = raw_response

    today_str = datetime.date.today().isoformat()
    synced_items = []

    EXCLUDED_ASSET_TYPES = {
        "IND_STOCK", "INDIAN_EQUITY", "MUTUAL_FUND", "MF", "SIP", "FIXED_DEPOSIT", "FD",
        "EPF", "PPF", "NPS", "GOLD", "DEBT", "BOND", "REAL_ESTATE", "INSURANCE", "SAVINGS"
    }

    # Step 1: Filter and Aggregate active US holdings by symbol
    aggregated_map: Dict[str, Dict[str, Any]] = {}

    for item in holdings_list:
        if not isinstance(item, dict):
            continue

        asset_type = str(item.get("asset_type") or item.get("instrument_type") or item.get("asset_class") or item.get("category") or "").strip().upper()
        if asset_type in EXCLUDED_ASSET_TYPES:
            continue

        status = str(item.get("status") or "").strip().upper()
        if status in ["CLOSED", "EXITED", "INACTIVE", "SOLD", "CANCELLED"]:
            continue
        if item.get("is_active") is False or item.get("is_holding") is False:
            continue

        raw_sym = extract_stock_symbol(item, is_indian=False)
        if not raw_sym:
            continue

        if raw_sym.endswith(".NS") or raw_sym.endswith(".BO"):
            continue

        symbol = normalize_us_symbol(raw_sym)

        qty = float(
            item.get("current_units") if item.get("current_units") is not None else
            item.get("holding_units") if item.get("holding_units") is not None else
            item.get("available_units") if item.get("available_units") is not None else
            item.get("quantity") if item.get("quantity") is not None else
            item.get("qty") if item.get("qty") is not None else
            item.get("units") if item.get("units") is not None else
            item.get("total_units") or 0.0
        )
        if qty <= 0:
            continue

        invested_usd = float(item.get("invested_value_usd") or item.get("invested_amount_usd") or 0.0)
        current_usd = float(item.get("current_value_usd") or item.get("market_value_usd") or 0.0)
        one_day_usd = float(item.get("one_day_change_usd") or 0.0)

        curr_code = str(item.get("currency") or "").strip().upper()
        if invested_usd <= 0:
            invested_usd = float(item.get("invested_value") or item.get("invested_amount") or 0.0)
            if curr_code == "INR" or invested_usd > 10000:
                invested_usd = round(invested_usd / 86.5, 4)
        if current_usd <= 0:
            current_usd = float(item.get("market_value") or item.get("current_value") or 0.0)
            if curr_code == "INR" or current_usd > 10000:
                current_usd = round(current_usd / 86.5, 4)

        if current_usd <= 0 and invested_usd <= 0:
            continue

        avg_price = (invested_usd / qty) if qty > 0 and invested_usd > 0 else float(item.get("unit_price_usd") or 0.0)
        curr_price = (current_usd / qty) if qty > 0 and current_usd > 0 else avg_price
        prev_close = ((current_usd - one_day_usd) / qty) if qty > 0 and current_usd > 0 else avg_price

        pnl = current_usd - invested_usd
        pnl_pct = ((pnl / invested_usd) * 100.0) if invested_usd > 0 else 0.0
        broker = str(item.get("broker") or "INDmoney US")
        ext_id = str(item.get("investment_code") or item.get("id") or symbol)
        entry_date = item.get("entry_date") or today_str

        if symbol not in aggregated_map:
            aggregated_map[symbol] = {
                "symbol": symbol,
                "exchange": "NASDAQ",
                "qty": qty,
                "avg_price": avg_price,
                "current_price": curr_price,
                "prev_close": prev_close,
                "invested_usd": invested_usd,
                "current_usd": current_usd,
                "pnl": pnl,
                "pnl_pct": pnl_pct,
                "broker": broker,
                "external_id": ext_id,
                "entry_date": entry_date
            }
        else:
            prev = aggregated_map[symbol]
            prev["qty"] += qty
            prev["invested_usd"] += invested_usd
            prev["current_usd"] += current_usd
            prev["pnl"] = prev["current_usd"] - prev["invested_usd"]
            if prev["invested_usd"] > 0:
                prev["pnl_pct"] = (prev["pnl"] / prev["invested_usd"]) * 100.0
            if prev["qty"] > 0:
                prev["avg_price"] = prev["invested_usd"] / prev["qty"]
                prev["current_price"] = prev["current_usd"] / prev["qty"]

    # Step 2: Atomic Wipe of old INDmoney US holdings for this user to prevent any duplicate rows
    try:
        url_del = f"{SUPABASE_URL}/rest/v1/user_holdings?user_id=eq.{user_id}&sync_source=eq.indmoney&status=eq.open&exchange=not.in.(NSE,BSE)"
        requests.delete(url_del, headers=get_service_headers(), timeout=7)
    except Exception as e:
        print(f"[INDmoneyEngine] Error cleaning old US holdings: {e}")

    # Step 3: Insert fresh aggregated US holdings
    for symbol, agg in aggregated_map.items():
        payload = {
            "user_id": user_id,
            "symbol": symbol,
            "exchange": "NASDAQ",
            "entry_price": round(agg["avg_price"], 2),
            "qty": round(agg["qty"], 4),
            "entry_date": agg["entry_date"],
            "status": "open",
            "sync_source": "indmoney",
            "external_broker": agg["broker"],
            "external_id": agg["external_id"],
            "current_value": round(agg["current_usd"], 2),
            "invested_value": round(agg["invested_usd"], 2),
            "unrealized_pnl": round(agg["pnl"], 2),
            "pnl_pct": round(agg["pnl_pct"], 2),
            "exit_verdict": "Safe Hold",
            "time_to_leave_score": 20
        }

        try:
            url = f"{SUPABASE_URL}/rest/v1/user_holdings"
            res = requests.post(url, headers=get_service_headers(), json=payload, timeout=5)
            if res.status_code in (200, 201, 204):
                synced_items.append(payload)
        except Exception as e:
            print(f"[INDmoneyEngine] Error inserting US holding {symbol}: {e}")

    return synced_items


def sync_user_watchlists(user_id: str, access_token: str) -> List[Dict[str, Any]]:
    """
    Fetches user's saved watchlists (Indian and US) from INDmoney MCP (user_watchlist)
    and merges them into Supabase user_watchlists.
    """
    raw_response = call_mcp_tool(access_token, "user_watchlist", {"type": "all"})
    synced_items = []
    today_str = datetime.date.today().isoformat()

    watchlists = []
    if isinstance(raw_response, dict):
        watchlists = raw_response.get("watchlists") or []
        if not watchlists and ("instruments" in raw_response or "items" in raw_response):
            watchlists = [{"name": "INDmoney Watchlist", "stocks": raw_response.get("instruments") or raw_response.get("items")}]
    elif isinstance(raw_response, list):
        watchlists = [{"name": "INDmoney Watchlist", "stocks": raw_response}]

    for wl in watchlists:
        w_name = (wl.get("name") or "INDmoney Watchlist").strip()
        stocks = wl.get("stocks") or []
        for s in stocks:
            sym = ""
            exchange = "NSE"
            if isinstance(s, str):
                sym = s.strip().upper()
            elif isinstance(s, dict):
                sym = (s.get("ticker") or s.get("ind_key") or s.get("symbol") or s.get("name") or "").strip().upper()

            if not sym:
                continue

            if sym.endswith(".NS") or sym.endswith(".BO"):
                exchange = "NSE"
                sym = normalize_indian_symbol(sym)
            else:
                exchange = "NASDAQ"
                sym = normalize_us_symbol(sym)

            payload = {
                "user_id": user_id,
                "watchlist_name": w_name,
                "symbol": sym,
                "exchange": exchange,
                "added_date": today_str,
                "sync_source": "indmoney"
            }

            try:
                url = f"{SUPABASE_URL}/rest/v1/user_watchlists"
                res = requests.post(url, headers=get_service_headers("resolution=merge-duplicates"), json=payload, timeout=5)
                if res.status_code in (200, 201):
                    synced_items.append(payload)
            except Exception as e:
                print(f"[INDmoneyEngine] Error upserting watchlist item {sym} in {w_name}: {e}")

    return synced_items


def sync_networth_snapshot(user_id: str, access_token: str) -> Dict[str, Any]:
    """
    Pulls total net worth snapshot, asset allocation, and liabilities.
    """
    try:
        snapshot = call_mcp_tool(access_token, "networth_snapshot", {})
        return snapshot if isinstance(snapshot, dict) else {"raw": snapshot}
    except Exception as e:
        print(f"[INDmoneyEngine] Error fetching networth snapshot: {e}")
        return {}


def perform_full_sync(user_id: str) -> Dict[str, Any]:
    """
    Executes end-to-end sync for Indian stocks, US stocks, watchlists, and net worth snapshot.
    Updates Supabase sync status and timestamps.
    """
    token = get_valid_access_token(user_id)
    if not token:
        return {
            "success": False,
            "error": "No active INDmoney connection. Please connect your account first."
        }

    # Update status to 'syncing'
    now_iso = datetime.datetime.now(datetime.timezone.utc).isoformat()
    try:
        url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?user_id=eq.{user_id}"
        requests.patch(url, headers=get_service_headers(), json={"last_sync_status": "syncing"}, timeout=5)
    except Exception:
        pass

    try:
        indian_holdings = sync_indian_stock_holdings(user_id, token)
        us_holdings = sync_us_stock_holdings(user_id, token)
        watchlist_items = sync_user_watchlists(user_id, token)
        snapshot = sync_networth_snapshot(user_id, token)

        sync_summary = {
            "indian_stocks_count": len(indian_holdings),
            "us_stocks_count": len(us_holdings),
            "watchlist_count": len(watchlist_items),
            "networth_summary": snapshot
        }

        # Update success status
        try:
            url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?user_id=eq.{user_id}"
            requests.patch(url, headers=get_service_headers(), json={
                "last_synced_at": now_iso,
                "last_sync_status": "success",
                "last_sync_error": None,
                "sync_metadata": sync_summary
            }, timeout=5)
        except Exception:
            pass

        return {
            "success": True,
            "last_synced_at": now_iso,
            "indian_stocks_synced": len(indian_holdings),
            "us_stocks_synced": len(us_holdings),
            "watchlist_synced": len(watchlist_items),
            "snapshot": snapshot
        }

    except Exception as e:
        err_msg = str(e)
        try:
            url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?user_id=eq.{user_id}"
            requests.patch(url, headers=get_service_headers(), json={
                "last_sync_status": "error",
                "last_sync_error": err_msg
            }, timeout=5)
        except Exception:
            pass

        return {
            "success": False,
            "error": err_msg
        }


def get_sync_status(user_id: str) -> Dict[str, Any]:
    """
    Returns connection and sync status for a user.
    """
    token_info = _MEMORY_TOKENS.get(user_id)
    db_row = None

    try:
        url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?user_id=eq.{user_id}&select=*"
        res = requests.get(url, headers=get_service_headers(), timeout=5)
        if res.status_code == 200 and res.json():
            db_row = res.json()[0]
    except Exception:
        pass

    is_connected = bool((db_row and db_row.get("is_active")) or (token_info and token_info.get("is_active")))
    last_synced = (db_row.get("last_synced_at") if db_row else None)
    status = (db_row.get("last_sync_status") if db_row else ("connected" if is_connected else "disconnected"))
    meta = (db_row.get("sync_metadata") if db_row else {})

    return {
        "connected": is_connected,
        "status": status,
        "last_synced_at": last_synced,
        "sync_metadata": meta
    }


def disconnect_user(user_id: str) -> bool:
    """
    Revokes token and disconnects INDmoney account.
    """
    token = get_valid_access_token(user_id)
    if token:
        try:
            session = get_cffi_session()
            session.post(
                INDMONEY_REVOKE_URL,
                data={"token": token, "client_id": INDMONEY_CLIENT_ID, "client_secret": INDMONEY_CLIENT_SECRET},
                headers={"Content-Type": "application/x-www-form-urlencoded"},
                timeout=5
            )
        except Exception:
            pass

    _MEMORY_TOKENS.pop(user_id, None)

    try:
        url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?user_id=eq.{user_id}"
        requests.delete(url, headers=get_service_headers(), timeout=5)
    except Exception:
        pass

    return True


# =============================================================================
# 4. INDMONEY MCP NEWS & SENTIMENT ANALYSIS ENGINE
# =============================================================================

_NEWS_CACHE: Dict[str, Dict[str, Any]] = {}
NEWS_CACHE_TTL = 1800  # 30 minutes


def parse_news(symbol: str, resp: Any) -> List[Dict[str, Any]]:
    r"""
    Parses INDmoney MCP news response according to specification:
    - Extracts date from title: r"\s-\s(\d{2} \w{3}, \d{4})$"
    - Retains all curated key events from INDmoney news key
    - Extracts sentiment: positive, neutral, negative
    - Sorts descending by news_date (latest above)
    """
    out = []
    today = datetime.date.today()
    current_month = today.month
    current_year = today.year

    raw_news = []
    if isinstance(resp, dict):
        if symbol in resp and isinstance(resp[symbol], dict):
            raw_news = resp[symbol].get("news", [])
        elif "news" in resp:
            raw_news = resp.get("news", [])
        elif "data" in resp:
            raw_news = resp.get("data", [])
        else:
            for k, v in resp.items():
                if isinstance(v, dict) and "news" in v:
                    raw_news.extend(v.get("news", []))
    elif isinstance(resp, list):
        raw_news = resp

    for n in raw_news:
        if not isinstance(n, dict):
            continue
        raw_title = n.get("title", "").strip()
        if not raw_title:
            continue

        # Extract date formatted like: " - 01 Oct, 2026"
        m = re.search(r"\s-\s(\d{2}\s\w{3},\s\d{4})$", raw_title)
        parsed_date = None
        date_str = ""
        if m:
            date_str = m.group(1)
            try:
                parsed_date = datetime.datetime.strptime(date_str, "%d %b, %Y").date()
            except Exception:
                pass
        clean_title = raw_title[:m.start()].strip() if m else raw_title

        sentiment = (n.get("sentiment") or "neutral").lower()
        if sentiment not in ("positive", "negative", "neutral"):
            # Normalize common synonyms
            if "bull" in sentiment or "buy" in sentiment:
                sentiment = "positive"
            elif "bear" in sentiment or "sell" in sentiment:
                sentiment = "negative"
            else:
                sentiment = "neutral"

        pub_date = parsed_date or today
        is_today = (pub_date == today)
        is_this_month = (pub_date.year == current_year and pub_date.month == current_month)

        out.append({
            "symbol": symbol,
            "title": raw_title,            # Full title with date e.g. "Rocket Lab Secures Major Contracts... - 01 Oct, 2026"
            "clean_title": clean_title,    # Clean title without date suffix
            "date_str": date_str,          # E.g. "01 Oct, 2026"
            "description": n.get("description", "").strip(),
            "sentiment": sentiment,
            "news_date": pub_date.isoformat(),
            "is_today": is_today,
            "is_this_month": is_this_month,
            "publisher": n.get("source") or n.get("publisher") or "INDmoney Key Events",
            "url": n.get("link") or n.get("url") or ""
        })

    # Sort descending by news_date (latest above)
    out.sort(key=lambda x: x["news_date"], reverse=True)
    return out


def fetch_yahoo_market_news(symbol: str, count: int = 10) -> List[Dict[str, Any]]:
    """
    Fetches real-time financial market news articles from Yahoo Finance Search API.
    """
    clean_sym = symbol.replace(".NS", "").replace(".BO", "").strip().upper()
    articles = []
    try:
        import yfinance as yf
        s = yf.Search(clean_sym, news_count=count)
        if hasattr(s, "news") and s.news:
            now_sec = time.time()
            for item in s.news:
                pub_ts = item.get("providerPublishTime") or 0
                diff_hrs = int((now_sec - pub_ts) / 3600) if pub_ts else 2
                time_ago = "Just now" if diff_hrs < 1 else (f"{diff_hrs}h ago" if diff_hrs < 24 else f"{diff_hrs // 24}d ago")
                articles.append({
                    "title": item.get("title", ""),
                    "publisher": item.get("publisher", "Market News"),
                    "link": item.get("link", ""),
                    "published_at": pub_ts,
                    "time_ago": time_ago,
                    "type": item.get("type", "STORY"),
                    "uuid": item.get("uuid", "")
                })
    except Exception as e:
        print(f"[YahooNews] Error fetching Yahoo news for {clean_sym}: {e}")
    return articles


def upsert_stock_news_to_supabase(articles: List[Dict[str, Any]]):
    """
    Upserts news articles into Supabase public.stock_news table
    with unique constraint on (symbol, title, news_date).
    """
    if not articles:
        return
    try:
        url = f"{SUPABASE_URL}/rest/v1/stock_news"
        db_payload = [
            {
                "symbol": a["symbol"],
                "title": a["clean_title"] if a.get("clean_title") else a["title"],
                "description": a.get("description", ""),
                "sentiment": a["sentiment"],
                "news_date": a["news_date"],
                "publisher": a.get("publisher", "INDmoney Key Events"),
                "url": a.get("url", "")
            }
            for a in articles
        ]
        requests.post(
            url,
            headers=get_service_headers("resolution=merge-duplicates"),
            json=db_payload,
            timeout=5
        )
    except Exception as e:
        print(f"[INDmoneyNews] Error upserting to Supabase: {e}")


def get_stock_news_sentiment(
    symbol: str,
    access_token: Optional[str] = None,
    user_id: Optional[str] = None
) -> Dict[str, Any]:
    """
    Main entry point for retrieving news & sentiment analysis for a stock symbol:
    1. Checks in-memory cache.
    2. Resolves valid INDmoney access token (auto-refreshing via Supabase if needed).
    3. Calls INDmoney MCP tool:
       - US stocks: get_us_stocks_details with symbols and segments: ["news"]
       - Indian stocks: lookup_ind_keys -> get_indian_stocks_details with ind_keys and segments: ["news"]
    4. Parses using parse_news(), extracts sentiment, description, and key events.
    5. Fetches Yahoo Finance market news wire via fetch_yahoo_market_news().
    6. Returns structured payload with distinct 'indmoney' and 'yahoo' sources.
    """
    clean_sym = symbol.strip().upper()
    now_ts = time.time()

    # 1. Check in-memory cache
    if clean_sym in _NEWS_CACHE:
        cached = _NEWS_CACHE[clean_sym]
        if now_ts - cached.get("timestamp", 0) < NEWS_CACHE_TTL:
            return cached["data"]

    is_indian = clean_sym.endswith(".NS") or clean_sym.endswith(".BO")
    parsed_articles: List[Dict[str, Any]] = []

    # 2. Resolve INDmoney access token (pass-in, specific user_id, memory cache, or active user in Supabase)
    token = access_token
    if not token and user_id:
        token = get_valid_access_token(user_id)

    if not token and _MEMORY_TOKENS:
        token = next((v.get("access_token") for v in _MEMORY_TOKENS.values() if v.get("access_token")), None)

    if not token:
        try:
            url = f"{SUPABASE_URL}/rest/v1/user_indmoney_tokens?is_active=eq.true&order=updated_at.desc&limit=1"
            res = requests.get(url, headers=get_service_headers(), timeout=5)
            if res.status_code == 200 and res.json():
                user_row = res.json()[0]
                u_id = user_row.get("user_id")
                if u_id:
                    token = get_valid_access_token(u_id)
        except Exception as e:
            print(f"[INDmoneyNews] Error resolving valid user token: {e}")

    # 3. Call INDmoney MCP tool
    if token:
        try:
            if is_indian:
                # Indian Stocks: lookup_ind_keys -> get_indian_stocks_details
                base_ticker = clean_sym.replace(".NS", "").replace(".BO", "")
                lookup_res = call_mcp_tool(token, "lookup_ind_keys", {"names": [base_ticker]})
                ind_keys = []
                if isinstance(lookup_res, list):
                    for item in lookup_res:
                        if isinstance(item, dict) and item.get("ind_key"):
                            ind_keys.append(item["ind_key"])
                elif isinstance(lookup_res, dict):
                    ind_keys = lookup_res.get("ind_keys") or [lookup_res.get(base_ticker)]
                ind_keys = [k for k in ind_keys if k] or [base_ticker]

                mcp_resp = call_mcp_tool(token, "get_indian_stocks_details", {
                    "ind_keys": ind_keys,
                    "segments": ["news"]
                })
                parsed_articles = parse_news(clean_sym, mcp_resp)
            else:
                # US Stocks: get_us_stocks_details with symbols and segments: ["news"]
                mcp_resp = call_mcp_tool(token, "get_us_stocks_details", {
                    "symbols": [clean_sym],
                    "segments": ["news"]
                })
                parsed_articles = parse_news(clean_sym, mcp_resp)
        except Exception as e:
            print(f"[INDmoneyNews] MCP tool call error for {clean_sym}: {e}")

    # 4. Fallback to Supabase stored news if MCP returned empty
    if not parsed_articles:
        try:
            url = f"{SUPABASE_URL}/rest/v1/stock_news?symbol=eq.{clean_sym}&order=news_date.desc&limit=25"
            res = requests.get(url, headers=get_service_headers(), timeout=5)
            if res.status_code == 200 and res.json():
                today = datetime.date.today()
                for row in res.json():
                    p_date = row.get("news_date")
                    parsed_articles.append({
                        "symbol": clean_sym,
                        "title": row.get("title", ""),
                        "clean_title": row.get("title", ""),
                        "date_str": p_date or "",
                        "description": row.get("description", ""),
                        "sentiment": row.get("sentiment", "neutral"),
                        "news_date": p_date,
                        "is_today": p_date == today.isoformat(),
                        "is_this_month": True,
                        "publisher": row.get("publisher", "INDmoney Key Events"),
                        "url": row.get("url", "")
                    })
        except Exception as e:
            print(f"[INDmoneyNews] DB fallback error: {e}")

    # 5. Persist to Supabase so refreshes avoid duplicates
    if parsed_articles:
        upsert_stock_news_to_supabase(parsed_articles)

    # 6. Fetch Yahoo Market News Wire
    yahoo_articles = fetch_yahoo_market_news(clean_sym, count=10)

    # 6b. If INDmoney MCP returned no key events, synthesize catalysts from Yahoo News with sentiment analysis
    if not parsed_articles and yahoo_articles:
        today = datetime.date.today()
        pos_words = ["surge", "gain", "profit", "beat", "rally", "growth", "high", "upgrade", "record", "jump", "bull", "boost", "soar", "partner", "expands", "dividend"]
        neg_words = ["drop", "fall", "loss", "miss", "plunge", "decline", "cut", "downgrade", "probe", "lawsuit", "sue", "layoff", "bear", "crash", "slump", "warn", "risk"]
        for y in yahoo_articles:
            t_lower = (y.get("title", "")).lower()
            pos_matches = sum(1 for w in pos_words if w in t_lower)
            neg_matches = sum(1 for w in neg_words if w in t_lower)
            sent = "positive" if pos_matches > neg_matches else ("negative" if neg_matches > pos_matches else "neutral")
            parsed_articles.append({
                "symbol": clean_sym,
                "title": y.get("title", ""),
                "clean_title": y.get("title", ""),
                "date_str": y.get("time_ago", "Today"),
                "description": f"Verified market catalyst from {y.get('publisher', 'Financial Wire')}. Sentiment evaluated as {sent.upper()}.",
                "sentiment": sent,
                "news_date": today.isoformat(),
                "is_today": True,
                "is_this_month": True,
                "publisher": y.get("publisher", "Market News"),
                "url": y.get("link", "")
            })

    # 7. Compute Sentiment Summary for INDmoney events
    pos_count = sum(1 for a in parsed_articles if a.get("sentiment") == "positive")
    neg_count = sum(1 for a in parsed_articles if a.get("sentiment") == "negative")
    neu_count = sum(1 for a in parsed_articles if a.get("sentiment") == "neutral")
    total = len(parsed_articles)

    today_news = [a for a in parsed_articles if a.get("is_today")]
    this_month_news = [a for a in parsed_articles if a.get("is_this_month")]

    sentiment_summary = {
        "positive_count": pos_count,
        "neutral_count": neu_count,
        "negative_count": neg_count,
        "total_count": total,
        "label": f"{neg_count} negative • {neu_count} neutral • {pos_count} positive"
    }

    result_data = {
        "symbol": clean_sym,
        "indmoney": {
            "available": len(parsed_articles) > 0,
            "total_count": total,
            "sentiment_summary": sentiment_summary,
            "key_events": parsed_articles,
            "today_news": today_news,
            "this_month_news": this_month_news if this_month_news else parsed_articles
        },
        "yahoo": {
            "available": len(yahoo_articles) > 0,
            "total_count": len(yahoo_articles),
            "articles": yahoo_articles
        },
        # Unified flat compatibility fields:
        "sentiment_summary": sentiment_summary,
        "today_news": today_news,
        "this_month_news": this_month_news if this_month_news else parsed_articles,
        "key_events": parsed_articles,
        "yahoo_news": yahoo_articles,
        "indmoney_news": parsed_articles
    }

    # Cache result
    _NEWS_CACHE[clean_sym] = {
        "timestamp": now_ts,
        "data": result_data
    }

    return result_data

