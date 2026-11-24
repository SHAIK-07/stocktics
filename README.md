# Stocktics

**A full-stack stock analysis platform for the US (NYSE / NASDAQ) and Indian (NSE / BSE) markets.** It combines live market data, audited fundamental metrics, a daily 30+ pattern technical scanner with backtesting, sector intelligence, and portfolio tracking with INDmoney broker sync. All of this is delivered through a native Android app and a React web dashboard, backed by a FastAPI service.

![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Python](https://img.shields.io/badge/Python-FastAPI-009688?logo=fastapi&logoColor=white)
![React](https://img.shields.io/badge/React-19%20%2B%20Vite-61DAFB?logo=react&logoColor=black)
![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?logo=supabase&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

---

## Highlights

- **Dual-market coverage**: one codebase serves both US and Indian equities, with market-aware currency formatting, benchmarks (`^GSPC`, `^NSEI`) and trading calendars.
- **30+ pattern technical scanner**: a daily batch pipeline computes an indicator layer (ATR, SMA/EMA, RSI, Stochastics, Supertrend, relative volume). It then runs 30+ chart-pattern detectors (breakouts, harmonics, Heikin-Ashi trends, volume-profile POC reactions and more) in five phases, ranks the results and backtests every pattern.
- **Audited fundamentals engine**: computes valuation, profitability, growth and balance-sheet metrics from raw financial statements, with safe fallbacks for inconsistent line-item labels.
- **Portfolio and broker sync**: buy/exit pipeline, holdings P&L, watchlists, and INDmoney integration over OAuth 2.0 + PKCE through its MCP endpoint.
- **Market tools**: market heatmaps, side-by-side stock comparison, sector overviews and spotlights, weekly top-pattern rankings and a news feed.
- **Learning module**: an interactive curriculum that teaches financial metrics using live data.
- **Resilient data layer**: a `curl_cffi` browser-impersonating session gets around Yahoo Finance 401 blocks on cloud IPs, plus an SWR (stale-while-revalidate) cache on the client.

## Architecture

```
┌──────────────────────┐     ┌──────────────────────┐
│  Android app         │     │  Web dashboard       │
│  Kotlin · Compose    │     │  React 19 · Vite     │
│  Material 3          │     │  (Vercel)            │
└──────────┬───────────┘     └──────────┬───────────┘
           │        REST / JSON         │
           └─────────────┬──────────────┘
                         ▼
             ┌───────────────────────┐        ┌────────────────────┐
             │  FastAPI backend      │◀──────▶│  Yahoo Finance     │
             │  (Render)             │        │  INDmoney MCP      │
             │  metrics · portfolio  │        └────────────────────┘
             │  scanner · sectors    │
             └───────────┬───────────┘
                         ▼
             ┌───────────────────────┐        ┌────────────────────┐
             │  Supabase (Postgres)  │◀───────│  Daily scanner     │
             │  auth · RLS · views   │        │  (GitHub Actions)  │
             └───────────────────────┘        └────────────────────┘
```

## Tech stack

| Layer | Technologies |
|---|---|
| Android | Kotlin, Jetpack Compose, Material 3, Coil, coroutines |
| Web | React 19, Vite, Lucide icons, deployed on Vercel |
| Backend | Python, FastAPI, Uvicorn, pandas, NumPy, yfinance, curl_cffi, pandas-ta |
| Data / ML | scikit-learn, XGBoost, PyTorch (experimentation) |
| Database & auth | Supabase (PostgreSQL, row-level security, auth triggers, SQL views) |
| Automation | GitHub Actions (scheduled scanner after the IN and US market closes, keep-alive) |

## Repository layout

```
app/                    Android app (Kotlin + Jetpack Compose)
  └─ src/main/java/com/stocktics/
       data/            API clients: auth, stocks, portfolio, watchlist, INDmoney, cache
       presentation/    Screens: home, explore, stock detail, scanner, heatmap,
                        compare, sectors, buy/sell, watchlist, learning, settings
backend/
  api.py                FastAPI entry point
  engines/              metric_engine, quote_service, portfolio, sector,
                        market tools, scanner API, INDmoney integration
  scanner/              pipeline, indicators, detectors (phase1–5), ranking, backtest
  scripts/              universe sync jobs (US 1000, large caps)
  tests/
frontend/               React web dashboard
supabase/migrations/    Schema, auth trigger, scanner tables/views, INDmoney sync, news
.github/workflows/      Daily scanner and Render keep-alive
docs/                   Product specs, design system and build plans
```

## Pattern scanner pipeline

Runs after the market close on each trading day (16:00 IST for India, 17:30 ET for the US):

1. Sync the pattern configuration and the stock universe.
2. Fetch benchmark and constituent OHLCV data, then validate and clean it.
3. Compute the indicator layer.
4. Run every registered detector across five phases (registered in `backend/scanner/detectors/registry.py`).
5. Compute composite and rolling 5-day rankings.
6. Backtest each pattern and update `pattern_backtests`.
7. Write the results to Supabase idempotently, so re-running a day overwrites rather than duplicates.

## API overview

Most routes are also served under an `/api` prefix. Interactive docs are at `/docs` when the server is running.

| Area | Example endpoints |
|---|---|
| Stocks | `GET /api/search`, `GET /api/stock/{ticker}`, `GET /api/stock/{ticker}/history`, `GET /api/quotes` |
| Scanner | `GET /scanner/categories`, `GET /scanner/pattern/{pattern}`, `GET /scanner/top-week`, `GET /scanner/backtest/{pattern}`, `POST /scanner/run` |
| Portfolio | `POST /auth/login`, `GET /holdings`, `POST /holdings/buy`, `POST /holdings/exit`, `GET /portfolio/summary` |
| Market tools | `GET /heatmap`, `GET /compare` |
| Sectors | `GET /api/sectors/overview`, `GET /api/sectors/spotlight`, `GET /api/sectors/{sector_name}/stocks` |
| INDmoney | `GET /indmoney/auth-url`, `GET /indmoney/callback`, `POST /indmoney/sync`, `GET /indmoney/snapshot` |

## Getting started

### Prerequisites
Python 3.11+, Node 20+, Android Studio (with its bundled JDK), and a Supabase project.

### 1. Configure the environment
```bash
cp .env.example .env      # then fill in your own values
```
Apply the SQL files in `supabase/migrations/` to your Supabase project, in numeric order.

### 2. Backend
```bash
python -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn backend.api:app --reload --port 8000
```

### 3. Web dashboard
```bash
cd frontend
npm install
npm run dev               # http://localhost:5173
```
Or start the backend and the frontend together with `python run_app.py`.

### 4. Android app
Open the project in Android Studio and run the `app` configuration. Gradle reads `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY` and `SUPABASE_SECRET_KEY` from the root `.env`. You can override any of them in `local.properties`. Both files are gitignored, so credentials never enter source control.

```bash
./gradlew assembleDebug   # APK → app/build/outputs/apk/debug/
```

## Download

Prebuilt Android APKs are attached to [GitHub Releases](../../releases).

## Security

- No credentials are committed. Every key is loaded from environment variables, GitHub Actions secrets, or gitignored local files.
- Database access is protected by Supabase row-level security policies, defined in `supabase/migrations/`.
- Broker linking uses OAuth 2.0 with PKCE. Tokens are never shipped in client code.

## License

All rights reserved. This repository is published as a portfolio project.
