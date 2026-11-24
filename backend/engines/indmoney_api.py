"""
FastAPI Router for Stocktics INDmoney MCP Integration
Exposes endpoints for OAuth 2.1 initiation, callback handling,
portfolio & watchlist synchronization, and connection management.
"""

from typing import Optional, Dict, Any
from fastapi import APIRouter, Query, HTTPException, BackgroundTasks, Body
from fastapi.responses import HTMLResponse, RedirectResponse
from pydantic import BaseModel

from backend.engines.indmoney_engine import (
    build_authorization_url,
    exchange_code_for_token,
    perform_full_sync,
    get_sync_status,
    disconnect_user,
    get_valid_access_token,
    call_mcp_tool,
    get_stock_news_sentiment
)

router = APIRouter(prefix="/indmoney", tags=["INDmoney MCP Integration"])


class SyncRequest(BaseModel):
    user_id: str


class DisconnectRequest(BaseModel):
    user_id: str


@router.get("/auth-url")
def get_auth_url(
    user_id: str = Query(..., description="Stocktics User ID"),
    redirect_uri: Optional[str] = Query(None, description="Custom OAuth redirect URI")
):
    """
    Generates a secure PKCE authorization URL to connect user's INDmoney account.
    """
    if not user_id:
        raise HTTPException(status_code=400, detail="Missing user_id parameter")
    try:
        data = build_authorization_url(user_id=user_id, redirect_uri=redirect_uri)
        return {"success": True, **data}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/callback")
def oauth_callback(
    code: Optional[str] = Query(None),
    state: Optional[str] = Query(None),
    error: Optional[str] = Query(None),
    error_description: Optional[str] = Query(None),
    background_tasks: BackgroundTasks = None
):
    """
    Receives OAuth 2.1 authorization code from INDmoney login handoff,
    exchanges code for tokens, triggers initial portfolio sync, and returns status.
    """
    if error:
        err_msg = error_description or error
        html_error = f"""
        <!DOCTYPE html>
        <html>
        <head>
            <title>INDmoney Connection Failed</title>
            <style>
                body {{ background-color: #0B0E14; color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }}
                .card {{ background: #141923; border: 1px solid #F04438; border-radius: 16px; padding: 32px; max-width: 440px; text-align: center; }}
                h2 {{ color: #F04438; margin-bottom: 12px; }}
                p {{ color: #94A3B8; font-size: 14px; line-height: 1.5; }}
                a {{ display: inline-block; margin-top: 20px; background: #1C2333; color: #F8FAFC; text-decoration: none; padding: 12px 24px; border-radius: 10px; font-weight: 600; border: 1px solid #222A3B; }}
            </style>
        </head>
        <body>
            <div class="card">
                <h2>Connection Cancelled</h2>
                <p>{err_msg}</p>
                <a href="javascript:window.close();">Close Window</a>
            </div>
        </body>
        </html>
        """
        return HTMLResponse(content=html_error, status_code=400)

    if not code or not state:
        raise HTTPException(status_code=400, detail="Missing required code or state parameter")

    try:
        res = exchange_code_for_token(code=code, state=state)
        user_id = res["user_id"]

        # Run full portfolio sync in background
        if background_tasks:
            background_tasks.add_task(perform_full_sync, user_id)
        else:
            perform_full_sync(user_id)

        html_success = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>INDmoney Connected</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { background-color: #0B0E14; color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0; padding: 20px; box-sizing: border-box; }
                .card { background: #141923; border: 1px solid #00D09C; border-radius: 16px; padding: 36px 24px; max-width: 440px; width: 100%; text-align: center; box-shadow: 0 8px 30px rgba(0, 208, 156, 0.12); }
                .badge { background: #1A00D09C; color: #00D09C; display: inline-block; padding: 6px 14px; border-radius: 20px; font-weight: 700; font-size: 12px; margin-bottom: 16px; letter-spacing: 0.5px; }
                h2 { color: #F8FAFC; margin-bottom: 8px; font-size: 22px; font-weight: 700; }
                p { color: #94A3B8; font-size: 14px; line-height: 1.5; margin-bottom: 24px; }
                .btn-group { display: flex; flex-direction: column; gap: 10px; }
                .cta { background: #00D09C; color: #0B0E14; text-decoration: none; padding: 14px 24px; border-radius: 10px; font-weight: 700; font-size: 15px; display: block; transition: 0.2s; }
                .cta:hover { background: #00B386; }
                .secondary-btn { background: #1C2333; color: #94A3B8; border: 1px solid #222A3B; text-decoration: none; padding: 12px 20px; border-radius: 10px; font-weight: 600; font-size: 13px; display: block; }
            </style>
        </head>
        <body>
            <div class="card">
                <div class="badge">INDMONEY CONNECTED</div>
                <h2>Portfolio Synced Successfully</h2>
                <p>Your Indian and US stock holdings and watchlists have been imported into Stocktics.</p>
                <div class="btn-group">
                    <a id="appReturnBtn" class="cta" href="stocktics://indmoney-callback?success=true">Return to Stocktics App</a>
                    <a class="secondary-btn" href="/portfolio">Open Web Dashboard</a>
                </div>
            </div>
            <script>
                // 1. Send postMessage for web app popup
                try {
                    if (window.opener) {
                        window.opener.postMessage({ type: 'INDMONEY_AUTH_SUCCESS' }, '*');
                        setTimeout(function() { window.close(); }, 1200);
                    }
                } catch(e) {}

                // 2. Automatically launch deep link to Android application
                try {
                    setTimeout(function() {
                        window.location.href = "stocktics://indmoney-callback?success=true";
                    }, 400);
                } catch(e) {}
            </script>
        </body>
        </html>
        """
        return HTMLResponse(content=html_success, status_code=200)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/sync")
def trigger_sync(req: SyncRequest):
    """
    On-demand portfolio and watchlist synchronization for a user.
    """
    res = perform_full_sync(req.user_id)
    if not res.get("success"):
        return {"success": False, "error": res.get("error")}
    return res


@router.get("/status")
def status(user_id: str = Query(..., description="Stocktics User ID")):
    """
    Returns current connection and sync status for a user.
    """
    return get_sync_status(user_id)


@router.post("/disconnect")
def disconnect(req: DisconnectRequest):
    """
    Disconnects INDmoney account and revokes active tokens.
    """
    success = disconnect_user(req.user_id)
    return {"success": success}


@router.get("/snapshot")
def get_snapshot(user_id: str = Query(..., description="Stocktics User ID")):
    """
    Returns full net worth breakdown across equity, debt, gold, and liabilities.
    """
    token = get_valid_access_token(user_id)
    if not token:
        raise HTTPException(status_code=401, detail="User not connected to INDmoney")
    try:
        snapshot = call_mcp_tool(token, "networth_snapshot", {})
        return {"success": True, "snapshot": snapshot}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/news")
def get_news_sentiment(
    symbol: str = Query(..., description="Stock ticker symbol, e.g. AAPL or RELIANCE.NS"),
    user_id: Optional[str] = Query(None, description="Optional Stocktics User ID for OAuth token")
):
    """
    Fetches INDmoney MCP news and sentiment analysis for a stock symbol:
    - Calls get_us_stocks_details (US) or get_indian_stocks_details (India) with segments: ['news']
    - Parses title date and filters for current month only
    - Sorts news descending by date (latest first)
    - Returns sentiment summary gauge counts (positive, neutral, negative) and 'Today' breakdown
    - Upserts articles to Supabase stock_news table
    """
    try:
        token = get_valid_access_token(user_id) if user_id else None
        data = get_stock_news_sentiment(symbol=symbol, access_token=token)
        return {"success": True, **data}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

