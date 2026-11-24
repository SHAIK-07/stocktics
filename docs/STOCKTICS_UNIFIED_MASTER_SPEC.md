# Stocktics — Personal Stock Research, Forecasting & Decision System
## Unified Master Architecture & Product Specification

---

## 1. Executive Summary & Vision

**Stocktics** is an end-to-end stock research, quantitative forecasting, and decision-support platform designed for Indian (NSE/BSE) and US equities. It bridges the gap between **deep fundamental/manual stock research** and **institutional-grade quantitative ML forecasting, risk modeling, and AI-driven synthesis**.

### Core Philosophy
1. **Manual Research First, AI & Quant Augmented**: The system provides complete manual exploration, transparent financial statements, forensic accounting checks, interactive valuation models (DCF, multiples), and technical charting without requiring an AI black-box.
2. **Transparent Quantitative Rigor**: Quantitative signals, walk-forward backtested ML models (XGBoost, LSTM, Chronos), and deterministic risk engines compute objective numbers. The AI layer *interprets and flags risks*—it never invents quantitative figures.
3. **Actionable Decision Funnel**: A clean 4-stage pipeline that keeps compute efficient and decisions disciplined:
   $$\text{Explore (Cheap Filter)} \longrightarrow \text{Watchlist (Curated)} \longrightarrow \text{Buy Verdict (Pipeline Run)} \longrightarrow \text{Sell/Exit Engine (Holdings)}$$

---

## 2. System Architecture & Data Flow

```
                      +-------------------------------------------------------+
                      |                 PRESENTATION LAYER                    |
                      |    Android (Kotlin/Compose) + Web (Next.js/React)     |
                      +-------------------+-------------------+---------------+
                                          |                   |
                        Direct / Realtime |                   | REST / RPC
                        (Supabase Kotlin) |                   | (FastAPI)
                                          v                   v
+-------------------------------------------------------------------------------------+
|                                SUPABASE BACKEND SUITE                               |
+-------------------------------------------------------------------------------------+
| * PostgreSQL Database (Primary relational & time-series storage)                    |
| * Row-Level Security (RLS) & Auth (Multi-device Watchlist & Portfolio sync)         |
| * Realtime CDC (Live Push: Watchlist Score updates, Alert notifications)            |
| * pgvector (Semantic search across Earnings Transcripts, Filings & Annual Reports)  |
| * Supabase Storage (PDF reports, ML model artifacts, cached datasets)              |
+------------------------------------------+------------------------------------------+
                                           | Database Connection (SQLAlchemy / asyncpg)
                                           | + Supabase Python SDK
                                           v
+-------------------------------------------------------------------------------------+
|                        PYTHON ANALYTICS & ML SERVICE ENGINE                         |
|                   FastAPI + Background Workers (Heavy Compute)                      |
+------------------------------------------+------------------------------------------+
                                           |
         +---------------------------------+---------------------------------+
         |                                 |                                 |
         v                                 v                                 v
+------------------+             +--------------------+            +--------------------+
| 1. DATA PIPELINE |             | 2. RESEARCH ENGINES|            | 3. QUANT ML ENGINE |
+------------------+             +--------------------+            +--------------------+
| * INDstocks API  |             | * Indicator Engine |            | * XGBoost & LSTM   |
|   (Master, OHLCV)|             | * Forensic Checks  |            | * Chronos Pretrain |
| * yfinance       |             | * DCF & Multiples  |            | * Walk-Forward     |
| * Scrapers & News|             | * Risk & Exit Rules|            |   Backtester       |
| * Filings / PDFs |             | * Verdict Engine   |            | * AI Agent Tools   |
+------------------+             +--------------------+            +--------------------+
```

---

## 3. Application Structure & Directory Layout

The codebase follows a modular clean-architecture pattern separating the Python backend/data engine from the presentation client.

```text
stocktics/
├── backend/                             # Python Core & Backend API
│   ├── app/
│   │   ├── api/                         # FastAPI Endpoints
│   │   │   ├── v1/
│   │   │   │   ├── explore.py           # Search, filters, screening
│   │   │   │   ├── stocks.py            # Deep stock detail & tabs
│   │   │   │   ├── watchlist.py         # Watchlist CRUD
│   │   │   │   ├── portfolio.py         # Holdings & transactions
│   │   │   │   ├── forecast.py          # ML pipeline runs & verdicts
│   │   │   │   ├── calculators.py       # DCF, CAGR, Margin tools
│   │   │   │   └── agent.py             # AI research agent endpoint
│   │   │   └── deps.py                  # Database & auth dependencies
│   │   │
│   │   ├── core/                        # Global configurations & constants
│   │   │   ├── config.py                # Environment variables & secrets
│   │   │   ├── security.py              # Token storage & validation
│   │   │   └── logging.py               # Structured logger
│   │   │
│   │   ├── data/                        # Data Ingestion & Provider Layer
│   │   │   ├── base.py                  # Abstract BaseDataProvider
│   │   │   ├── indstocks/               # INDstocks / INDmoney integration
│   │   │   │   ├── client.py            # HTTP client (auth, rate limits)
│   │   │   │   ├── instrument_master.py # Master sync & symbol mapping
│   │   │   │   ├── historical.py        # Chunked OHLCV downloader
│   │   │   │   └── validator.py         # Bar integrity & sanity checks
│   │   │   ├── yfinance_provider.py     # US equities & international fallback
│   │   │   ├── fundamentals_fetch.py    # P&L, Balance Sheet, Cash Flow
│   │   │   ├── ownership_fetch.py       # Promoters, FII, DII, Mutual Funds
│   │   │   ├── filings_fetch.py         # Exchange announcements & reports
│   │   │   └── news_fetch.py            # News & corporate feeds
│   │   │
│   │   ├── engines/                     # Analytical & Mathematical Engines
│   │   │   ├── indicators.py            # pandas-ta & custom technicals
│   │   │   ├── ratios.py                # ROCE, ROE, margins, turnover
│   │   │   ├── forensic.py              # Rule-based accounting red flags
│   │   │   ├── valuation.py             # DCF calculator, historical P/E bands
│   │   │   ├── scenario.py              # Bull / Base / Bear model engine
│   │   │   ├── risk_engine.py           # Deterministic risk classification
│   │   │   ├── exit_engine.py           # Stop-loss, targets, trailing stops
│   │   │   └── scoring_engine.py        # 0-100 Weighted Buy & Sell verdict
│   │   │
│   │   ├── ml/                          # Quantitative Forecasting Models
│   │   │   ├── features.py              # Feature engineering & labels
│   │   │   ├── train_classical.py       # XGBoost classifier
│   │   │   ├── train_dl.py              # PyTorch LSTM rolling window
│   │   │   ├── pretrained/
│   │   │   │   └── chronos_wrapper.py   # Amazon Chronos zero-shot wrapper
│   │   │   ├── backtest.py              # Walk-forward backtester & scorecard
│   │   │   ├── ensemble.py              # Weighted voting & stacking
│   │   │   └── registry.py              # Model versioning & artifact store
│   │   │
│   │   ├── agent/                       # LLM Synthesis & Research Agent
│   │   │   ├── research_agent.py        # Tool-calling agent (OpenAI/Claude)
│   │   │   ├── tools.py                 # Structured tool definitions
│   │   │   └── prompts.py               # System prompts & output schemas
│   │   │
│   │   └── storage/                     # SQLite ORM & Migrations
│   │       ├── database.py              # Connection pool & sessions
│   │       ├── models.py                # SQLAlchemy / SQLModel tables
│   │       └── repositories/            # Data access objects (DAOs)
│   │           ├── stock_repo.py
│   │           ├── price_repo.py
│   │           ├── fundamental_repo.py
│   │           ├── watchlist_repo.py
│   │           └── portfolio_repo.py
│   │
│   ├── main.py                          # CLI entry point & API launcher
│   ├── pyproject.toml                   # Python dependencies & metadata
│   └── .env.example                     # Environment template
│
└── client/                              # Presentation Layer (Android Native)
    ├── app/
    │   ├── src/main/java/com/stocktics/
    │   │   ├── data/                    # Retrofit API clients & Room cache
    │   │   │   ├── api/
    │   │   │   ├── model/
    │   │   │   └── repository/
    │   │   ├── di/                      # Dependency Injection (Hilt)
    │   │   ├── domain/                  # Use Cases & UI Models
    │   │   ├── presentation/            # Jetpack Compose UI
    │   │   │   ├── navigation/          # Bottom Bar & NavHost
    │   │   │   ├── home/                # Home dashboard
    ├── client/                              # Presentation Layer (Android Native)
    │   ├── app/
    │   │   ├── src/main/java/com/stocktics/
    │   │   │   ├── data/                    # Supabase Kotlin SDK & Local Cache
    │   │   │   ├── di/                      # Dependency Injection (Hilt)
    │   │   │   ├── domain/                  # Use Cases & UI Models
    │   │   │   ├── presentation/            # Jetpack Compose UI
    │   │   │   │   ├── navigation/          # Bottom Bar & NavHost
    │   │   │   │   ├── home/                # 🏠 Home Dashboard (Portfolio P&L, Alerts, Market Snapshot)
    │   │   │   │   ├── buy/                 # 🟢 Buy Page (Watchlist Pipeline, Buy Verdicts & 1-Tap Purchase)
    │   │   │   │   ├── sell/                # 🔴 Sell Page (Active Holdings, Exit Engine, Time-to-Leave Score)
    │   │   │   │   ├── explore/             # 🔎 Explore (Search, Screeners, 15-Tab Stock Research)
    │   │   │   │   ├── stock_detail/        # 15+ Comprehensive Stock Research Tabs
    │   │   │   │   ├── calculators/         # Standalone Calculators (DCF, Margin, CAGR)
    │   │   │   │   └── settings/            # ⚙️ Settings (Profile, Scoring Weights Tuner, Token Config)
    │   │   │   └── ui/theme/                # Color palettes, typography
    │   │   └── build.gradle.kts
    │   └── settings.gradle.kts
```

---

## 4. End-to-End User Flow & Navigation

### 4.1 Primary App Navigation (Persistent 5-Item Navigation)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                               ACTIVE SCREEN                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│  🏠 Home   │   🟢 Buy Page   │   🔴 Sell Page   │   🔎 Explore   │  ⚙️ Settings  │
└─────────────────────────────────────────────────────────────────────────────┘
```

1. **🏠 Home (Personalized User Dashboard)**:
   - **Portfolio Financial Summary**: Total Portfolio Value, Net Invested, Overall Unrealized P&L (% & ₹), Today's P&L.
   - **Urgent Exit & Risk Alerts**: Immediate banner if any holding is nearing Stop-Loss or triggers an Exit signal.
   - **Top Buy Pipeline Picks**: Quick cards of highest-scoring stocks currently in your watchlist (`Strong Buy`).
   - **Market Indices & Movers**: NIFTY, SENSEX, S&P 500, Sector trends, Top Gainers & Losers.
   - **Recent Activity**: Quick access to recently researched stocks and transaction logs.

2. **🟢 Buy Page (Target Pipeline & Entry Verdict Hub)**:
   - Displays curated stocks from your watchlist, ranked by the **0–100 Weighted Buy Score**.
   - **Verdict Badges**: `Strong Buy` (≥75), `Buy` (55–74), `Hold` (35–54), `Avoid` (<35).
   - **5-Factor Category Breakdown** per stock:
     - 📈 Technical Indicators (30%)
     - 🤖 ML Model Consensus (25%)
     - 👥 Ownership & Smart Money (20%)
     - 📊 Market Internals (15%)
     - 📰 News & Filings Sentiment (10%)
   - **1-Tap Buy Action**: Enter purchase quantity/price $\to$ instantly writes to `user_holdings` and `user_transactions`.

3. **🔴 Sell Page (Active Holdings & Exit Engine Monitor)**:
   - Displays all active open positions from `user_holdings`.
   - Displays Entry Price, Current Price, Unrealized P&L %, Holding Period (Days).
   - **0–100 "Time to Leave" Score & Verdict**: `Exit Now` (≥75), `Consider Exiting` (55–74), `Hold Position` (35–54), `Safe` (<35).
   - **Exit Engine Real-Time Proximity**:
     - Stop-Loss Proximity Indicator (e.g. `2.1% from Stop-Loss`).
     - Trailing Stop & Profit Target Proximity.
     - Technical Reversal Signals (RSI Bearish Divergence, 50 DMA break).
     - ML Model Sell Signal flips.
     - Insider / Promoter selling flags.
   - **1-Tap Sell Action**: Closes the holding, marks status `closed`, calculates realized P&L, and logs to `user_transactions`.

4. **🔎 Explore (Global Stock Discovery & Deep Research)**:
   - Instant search across NSE, BSE, and US tickers by Symbol, Company Name, or ISIN.
   - Pre-built Strategy Screeners (*High Growth*, *Quality Moat*, *Deep Value*, *Momentum*).
   - Multi-parameter custom financial screening (ROCE, P/E, Debt/Equity, Moving Averages).
   - **Action**: "Add to Watchlist / Buy Pipeline" button per stock.
   - Selecting any stock opens the full **15-Tab Deep Stock Research Screen**.

5. **⚙️ Settings (Profile, Configuration & Customization Hub)**:
   - **User Profile & Sync**: Supabase authentication status, multi-device cloud sync state.
   - **Scoring Engine Weight Tuner**: Sliders to customize the Buy and Sell engine category weights to match your personal strategy.
   - **Risk Management Defaults**: Configure default Stop-Loss % (e.g. 5%), Profit Target % (e.g. 15%), and Trailing Stop %.
   - **Data Provider & Tokens**: INDstocks API token management, health checks, and scheduled refresh settings.
   - **App Preferences**: Currency toggle (INR ₹ / USD $), Theme (Dark / Light / AMOLED), Notification preferences.

---

### 4.2 Deep Stock Detail Screen (Complete 23 Research & Analysis Modules)

When a stock is selected from Explore or Buy Page, a unified sticky header displays:
`← Back | [Symbol] [Company Name] | [Exchange] | ₹[Price] ([Change %]) | [⭐ Watchlist] | [Badge: e.g. 🟢 82/100 Strong Buy]`

The user seamlessly navigates between comprehensive tabs:

---

#### Tab 1: 📌 Overview
- **Real-Time Price Header**: Latest Available Price, timestamp of last quote, Open, High, Low, Previous Close, Day Change, Day %, Volume, 52-Week High, 52-Week Low, Market Capitalization.
- **Quick Health Cards (0–10 Score with Expandable Breakdown)**:
  - 🏢 Business: Score / 10 (Moat, segment diversity)
  - 💰 Financials: Score / 10 (Margin stability, returns)
  - 📈 Growth: Score / 10 (Revenue & PAT CAGR)
  - 💵 Cash Flow: Score / 10 (OCF / PAT conversion)
  - 🏦 Balance Sheet: Score / 10 (Debt/Equity, current ratio)
  - 👔 Management: Score / 10 (Governance, capital allocation)
  - 💎 Valuation: Score / 10 (Historical P/E vs peers)
  - 📊 Technical: Score / 10 (Trend, moving averages)
  - ⚠️ Risk: Low / Moderate / High Rating

---

#### Tab 2: 🏢 Business & Products
- Company description & core business model.
- Products & services breakdown.
- **Revenue Segments**: Percentage contribution per business unit.
- **Geographic Segments**: Domestic vs Export breakdown.
- **Customer Concentration**: Top 5 / Top 10 customer share of revenue.
- Recurring vs Non-recurring revenue percentage.
- Key revenue & profitability drivers, business cyclicality, market position.
- **Structured Cards**:
  - 🏢 *What the company does*
  - 💰 *How it makes money*
  - 📦 *Products & Services*
  - 🌍 *Geography*
  - 🎯 *Revenue Drivers*
  - 🛡️ *Competitive Advantage*
  - ⚠️ *Business Dependencies*

---

#### Tab 3: 🏭 Industry & Moat Analysis
- Industry size, industry growth rate, and key structural growth drivers.
- Industry challenges, regulatory environment, and competitive intensity.
- Cyclicality, commodity sensitivity, currency sensitivity, technology disruption.
- **Competitive Advantage Moat Checklist (Status: 🟢 Strong / 🟡 Moderate / 🔴 Weak / ⚪ Unavailable)**:
  - 💰 Cost Advantage
  - 🏷️ Brand Advantage
  - 🚚 Distribution Advantage
  - 💻 Technology Advantage
  - 🔗 Network Effects
  - 🔒 Switching Costs
  - 📈 Scale Advantage
  - ⚖️ Regulatory Advantage

---

#### Tab 4: 💰 Financial Statements (P&L History)
- **5–10+ Years Interactive Financial Table (Horizontal Scroll)**:
  - FY Year
  - Revenue & Revenue Growth %
  - EBITDA, EBITDA Margin %, EBITDA Growth %
  - EBIT, EBIT Margin %
  - PAT (Net Profit), PAT Margin %, PAT Growth %
  - EPS, EPS Growth %
  - Operating Cash Flow (OCF), Free Cash Flow (FCF)
- **Interactive Multi-Year Trend Charts (5Y / 10Y / All Available)**:
  - 📈 Revenue Trend
  - 📈 EBITDA Trend
  - 📈 PAT & EPS Trend
  - 📊 Margin Progression (Gross, EBITDA, PAT)
  - 📊 Cash Flow vs Profit Trend

---

#### Tab 5: 📊 Growth Analysis
- **CAGR Metrics**: 3-Year, 5-Year, and 10-Year CAGR for Revenue, EBITDA, PAT, and EPS.
- Market-share trajectory, capacity expansion track record, new product pipeline.
- Geographic expansion progress, order-book size & book-to-bill ratio.
- Capex plans & historical acquisition integration track record.
- **Growth Classification**:
  - 🟢 Organic
  - 🔵 Acquisition-driven
  - 🟠 Price-driven
  - 🟣 Volume-driven
  - 🟡 Temporary
  - 🟢 Sustainable
  - ⚪ Data unavailable

---

#### Tab 6: 🏦 Balance Sheet & Liquidity
- Total Debt, Cash & Cash Equivalents, Net Debt.
- Debt-to-Equity, Net Debt-to-EBITDA, Interest Coverage Ratio.
- Current Assets, Current Liabilities, Net Working Capital.
- Trade Receivables, Inventory, Trade Payables, Goodwill, Intangible Assets.
- **Balance Sheet Charts**:
  - 📊 Debt Trend
  - 📊 Cash Trend
  - 📊 Receivables vs Revenue Trend
  - 📊 Inventory Days & Working Capital Trend
- **Automated Balance Sheet Red Flags**:
  - 🔴 Debt increasing rapidly while profits stagnate
  - 🔴 Receivables growing faster than revenue
  - 🔴 Inventory building up (Days Sales of Inventory spike)
  - 🔴 Liquidity deterioration (Current Ratio < 1.0)
  - 🔴 Unusual contingent liabilities

---

#### Tab 7: 💵 Cash Flow & Forensic Accounting
- Operating Cash Flow (OCF), Investing Cash Flow, Financing Cash Flow, Free Cash Flow (FCF).
- Capex breakdown, acquisitions, dividends paid, share buybacks, debt raised/repaid.
- **Core Comparison Cards**:
  - `PAT vs OCF` (Profit to Cash Conversion)
  - `EBITDA vs FCF` (Operating Cash Conversion after Capex)
  - `Revenue vs Receivables` (Quality of Reported Sales)
  - `Debt vs Cash Flow` (Debt Repayment Ability)
- **8 Rule-Based Accounting Contradiction Alerts**:
  - ⚠️ **PAT ↑ but OCF ↓**: Net profit rising while operational cash drops.
  - ⚠️ **Revenue ↑ but Receivables ↑↑**: Sales recorded on credit without cash realization.
  - ⚠️ **EBITDA ↑ but FCF ↓**: Heavy capex draining cash despite operating profit.
  - ⚠️ **Debt ↑ while Profitability ↓**: Increasing leverage to support falling returns.
  - ⚠️ **Large Other Income**: Core profit weak, supported by non-operating income.
  - ⚠️ **Large One-time Gains**: Artificially inflated quarterly/annual earnings.
  - ⚠️ **Unusual Related-Party Transactions**: High capital transfers to promoter entities.
  - ⚠️ **Contingent Liabilities**: Significant off-balance-sheet litigation risks.

---

#### Tab 8: 📐 Profitability, Returns & Operating Ratios
- **Key Metrics & 5-Year Trends**:
  - Return on Capital Employed (ROCE)
  - Return on Equity (ROE)
  - Return on Assets (ROA)
  - Gross Margin %, EBITDA Margin %, EBIT Margin %, PAT Margin %
  - Asset Turnover Ratio
  - Cash Conversion Cycle (Days)
- **Visual Status per Metric**: `🟢 Improving`, `🟡 Stable`, `🔴 Deteriorating` with 5-year trend line.

---

#### Tab 9: 👥 Shareholding & Ownership Patterns
- **Latest Ownership Distribution Pie Chart**:
  - Promoters %
  - Foreign Institutional Investors (FII) %
  - Domestic Institutional Investors (DII) %
  - Mutual Funds %
  - Retail & Public %
  - Others %
- **Historical Ownership Trend Charts**: 8-Quarter promoter, FII, DII, and MF holding trends.
- **Promoter Pledge Tracker**: Current pledged shares % and historical pledge changes.
- **Ownership Signal Flags**:
  - 🟢 Promoter accumulation
  - 🟡 Stable ownership
  - 🔴 Promoter selling
  - 🔴 Promoter pledge increase

---

#### Tab 10: 🏛️ Mutual Funds & Institutional Activity
- Top Mutual Fund holders and institutional investment firms.
- Percentage holding and position market value.
- **Quarterly Institutional Moves**:
  - 🟢 New Entrants (Funds initiating fresh positions)
  - 🟢 Additions (Funds increasing stake)
  - 🔴 Reductions (Funds trimming stake)
  - 🔴 Exits (Funds completely exiting)
- Institutional concentration ratio (Top 5 funds holding share).

---

#### Tab 11: 👔 Management, Promoters & Governance
- Promoter background, education, and industry experience.
- Key managerial personnel (CEO, CFO, COO, MD) and tenure track records.
- Promoter remuneration as % of PAT, historical insider buying & selling disclosures.
- **Management Credibility Evidence Dashboard**:
  - 🎯 Guidance Achievement: 🟢 Strong / 🟡 Moderate / 🔴 Missed
  - 💰 Capital Allocation: 🟢 Disciplined / 🟡 Aggressive / 🔴 Value-destroying
  - 📈 Execution Track Record: 🟢 Consistent / 🟡 Mixed / 🔴 Poor
  - 🏛️ Governance & Board Independence: 🟢 High / 🟡 Watch / 🔴 Concern
  - 🔒 Promoter Pledge: 🟢 Zero / 🟡 Low (<10%) / 🔴 High (>25%)

---

#### Tab 12: 📑 Annual Reports & Filings Archive
- Chronological document archive (Annual Reports for FY26, FY25, FY24...).
- Direct access to extracted core sections:
  - Chairman & CEO Letter to Shareholders
  - Management Discussion & Analysis (MD&A)
  - Business Segment Breakdown & Geographic Revenue
  - Risk Factors & Mitigation Strategies
  - Capital Allocation & Future Capex Plans
  - Auditor Report & Key Audit Matters / Observations
  - Related-Party Disclosures & Contingent Liabilities

---

#### Tab 13: 🎙️ Quarterly Earnings & Results
- Quarterly financial history table (Q1, Q2, Q3, Q4):
  - Revenue (₹ Cr), EBITDA (₹ Cr), PAT (₹ Cr), EPS (₹).
- **Comparison Columns**: Year-over-Year (YoY %), Quarter-over-Quarter (QoQ %), vs Previous Quarter.
- Quarterly trend charts for Revenue, EBITDA, PAT, and Margins.

---

#### Tab 14: 📞 Earnings Call & Transcript Analysis
- **Categorized Management Commentary**:
  - 📈 Revenue Outlook & Guidance
  - 💰 Margin Trajectory & Cost Pressures
  - 📦 Demand Environment & Order Book
  - 🏗️ Capex Timeline & Commissioning
  - 🏭 Industry Outlook & Competitor Behavior
  - ⚠️ Management Concerns & Headwinds
  - ❓ Key Analyst Questions & Management Responses
- **Management Promise Tracker**:
  - Table: `[Period] | [Statement / Guidance] | [Expected Metric] | [Actual Result] | [Status: 🟢 Achieved / 🟡 Partial / 🔴 Missed]`

---

#### Tab 15: 📰 News & Corporate Announcements
- Company-specific live news & announcement stream.
- **Timeframe Filters**: Today, 7 Days, 30 Days, 3 Months, 1 Year.
- **Sentiment Categorization**: 🟢 Positive, 🟡 Neutral, 🔴 Negative.
- **Section Separation**: `📌 Material Announcements` vs `📰 General Media News`.
- Event tagging: Contracts, acquisitions, partnerships, product launches, senior management changes, legal disputes, regulatory actions, capex announcements, client wins/losses.

---

#### Tab 16: 📜 Regulatory Filings & Disclosures
- Feed of regulatory filings (BSE/NSE corporate disclosures & SEC EDGAR):
  - Board Meeting Outcomes
  - Financial Results Disclosures
  - Shareholding Pattern Submissions
  - Insider Trading Disclosures (Form 4 / SAST)
  - Bulk & Block Deals
  - Investor Presentations & Press Releases

---

#### Tab 17: 🏆 Competitor & Peer Comparison
- Side-by-side comparative table against 3–5 industry peers.
- **Comparison Metrics**:
  - Revenue Growth %
  - Profit Growth %
  - EBITDA Margin %
  - Return on Equity (ROE)
  - Return on Capital Employed (ROCE)
  - Debt-to-Equity
  - Price-to-Earnings (P/E)
  - Price-to-Book (P/B)
  - EV/EBITDA
  - Market Capitalization
- Visual cell indicators: `🟢 Better`, `🟡 Similar`, `🔴 Worse`. Sortable by any metric column.

---

#### Tab 18: 📈 Technical Analysis & Interactive Candlestick
- **Key Technical Metrics**:
  - Current Price, 52-Week High/Low, Trend Direction, Daily Volume.
  - Moving Averages: 20 DMA, 50 DMA, 100 DMA, 200 DMA (Price above/below status).
  - Relative Strength Index (RSI 14), MACD (12, 26, 9) line & signal.
  - Calculated Support & Resistance levels, Breakout & Breakdown alerts.
  - Relative Strength vs NIFTY/S&P 500 benchmark, Volume confirmation.
- **Interactive Candlestick Chart**:
  - Time intervals: `1D`, `1W`, `1M`, `6M`, `1Y`, `5Y`, `MAX`.
  - Toggable Indicator Overlays: SMA, EMA, RSI, MACD, Bollinger Bands, VWAP, ATR, ADX, Volume.
- **Trend Summary**:
  - Short-Term: 🟢 Bullish / 🔴 Bearish / 🟡 Neutral
  - Medium-Term: 🟢 Bullish / 🔴 Bearish / 🟡 Neutral
  - Long-Term: 🟢 Bullish / 🔴 Bearish / 🟡 Neutral

---

#### Tab 19: 💎 Valuation & Interactive DCF Calculator
- **Relative Valuation Multiples**:
  - Trailing P/E, Forward P/E, Price-to-Book (P/B), EV/EBITDA, PEG Ratio, Dividend Yield %.
  - Comparison vs Historical 5-Year Average and Industry Median.
- **Historical P/E Valuation Band**:
  - Low (e.g. 12x) ———— Fair Value (e.g. 22x) ———— High (e.g. 38x) with pointer to Current P/E.
- **User-Editable Discounted Cash Flow (DCF) Calculator**:
  - Editable Inputs: 5-Year Revenue Growth %, EBITDA Margin %, Tax Rate %, Capex % of Revenue, Working Capital % of Revenue, WACC (Discount Rate %), Terminal Growth Rate %.
  - Computed Outputs: Enterprise Value, Net Debt, Equity Value, Fair Value per Share, Current Market Price, Implied Upside / Downside %.
  - Clearly tagged as *Model Calculation*, not a guaranteed price.

---

#### Tab 20: 🔮 Future Outlook & Scenario Builder
- User-adjustable Bull / Base / Bear scenario planner:
  - **🟢 Bull Scenario**: User inputs Revenue Growth, EBITDA Margin, Target P/E $\to$ Implied Stock Price & Return %.
  - **🟡 Base Scenario**: User inputs Expected Growth, Expected Margin, Fair P/E $\to$ Implied Target.
  - **🔴 Bear Scenario**: User inputs Downside Growth, Compressed Margin, Trough P/E $\to$ Implied Downside Risk.
- Every assumption clearly labeled: `FACT`, `MANAGEMENT GUIDANCE`, `USER ASSUMPTION`, `MODEL CALCULATION`.

---

#### Tab 21: ⚠️ Comprehensive Risk Matrix Dashboard
- **14-Category Risk Assessment**:
  1. 🏢 Business Model Risk
  2. 💰 Financial Health Risk
  3. 🏦 Debt & Solvency Risk
  4. 🏆 Competitive Intensity Risk
  5. ⚖️ Regulatory & Policy Risk
  6. 🏛️ Governance & Management Risk
  7. 👔 Promoter Stake & Pledge Risk
  8. 👥 Customer Concentration Risk
  9. 🛢️ Commodity Price Sensitivity Risk
  10. 💱 Foreign Exchange / Currency Risk
  11. 💻 Technology Disruption Risk
  12. 📉 Macroeconomic Cyclicality Risk
  13. 💎 Valuation Multiple Risk
  14. 🎯 Execution & Capex Delay Risk
- **Risk Matrix**: Probability (Low/Medium/High) $\times$ Impact (Low/Medium/High).
- **"What to Monitor"**: Specific early-warning triggers defined for every material risk.

---

#### Tab 22: 🤖 Quantitative ML Forecast & AI Synthesis
- **Quantitative Model Consensus**:
  - XGBoost Indicator Classifier: Signal (`BUY` / `HOLD` / `SELL`) + Probability %.
  - PyTorch LSTM Sequence Model (20-Day Window): Signal + Probability %.
  - Amazon Chronos Zero-Shot Model: Projected 10-day price distribution.
  - Ensemble Consensus & Walk-Forward Backtest Win-Rate.
- **AI Research Agent Synthesis**: Structured qualitative synthesis written by LLM reading model outputs, financials, filings, and news (never hallucinating numbers).

---

#### Tab 23: 🎯 Master Investor Scorecard & Checklist
- **14-Category Scorecard (0–10 Score per dimension)**:
  - Business Quality, Industry Position, Management, Financial Health, Growth, Cash Flow, Balance Sheet, Capital Allocation, Ownership, Competitive Advantage, Valuation, Technical Trend, Risk, Future Outlook.
  - **Overall Score**: `X / 10`.
- **Top 5 Positive Signals** (Automatically surfaced strongest metrics).
- **Top 5 Warning Signals** (Automatically surfaced critical red flags).
- **16-Item Persistent Monitoring Checklist** (Status: `🟢 Good`, `🟡 Watch`, `🔴 Concern`):
  1. Next quarterly result
  2. Revenue growth trajectory
  3. EBITDA margin defense
  4. PAT growth
  5. Cash-flow conversion (OCF/PAT)
  6. Net debt levels
  7. Trade receivables growth
  8. Inventory days
  9. Order-book execution
  10. Capex commissioning
  11. Management guidance achievement
  12. Promoter buying/selling
  13. Institutional ownership changes
  14. Valuation multiple expansion/compression
  15. Technical moving average trend
  16. Industry & regulatory headwinds

---

### 4.3 Standalone Financial Calculators (10 Specialized Tools)

Available directly from the app for manual analysis:
1. **CAGR Calculator**: Calculate compounded annual growth rates across any custom time horizon.
2. **Margin Calculator**: Instant gross, operating, EBITDA, and net profit margin calculations.
3. **ROE Calculator**: DuPont decomposition analysis (Net Margin $\times$ Asset Turnover $\times$ Financial Leverage).
4. **ROCE Calculator**: EBIT $\div$ Capital Employed (Total Assets $-$ Current Liabilities).
5. **Full DCF Valuation Tool**: Detailed multi-stage discounted cash flow model.
6. **EPS Growth Calculator**: Normalized diluted EPS progression and projections.
7. **P/E Valuation Calculator**: Target stock price from EPS and target historical P/E multiple.
8. **PEG Calculator**: Price-to-Earnings divided by Earnings Growth Rate (identifying Growth at a Reasonable Price).
9. **Position Size Calculator**: Computes exact share quantity to buy based on portfolio risk tolerance % and stop-loss distance.
10. **Risk / Reward Ratio Calculator**: Quantifies risk-to-reward ratio before trade entry ($\frac{\text{Target Price} - \text{Entry}}{\text{Entry} - \text{Stop Loss}}$).

---

### 4.4 Stock Screener Presets & Custom Query Builder

- **🚀 High Growth Preset**: `Revenue Growth > 15% AND Profit Growth > 15% AND ROCE > 15%`.
- **💎 Quality Moat Preset**: `ROCE > 20% AND ROE > 15% AND Debt/Equity < 0.5`.
- **💰 Deep Value Preset**: `P/E < 5Y Historical Average AND Debt/Equity < 0.3 AND OCF/PAT > 0.8`.
- **💵 Cash Flow Champion Preset**: `OCF/PAT > 1.0 AND Free Cash Flow > 0 AND Net Debt <= 0`.
- **📈 Technical Momentum Preset**: `Price > 50 DMA AND Price > 200 DMA AND RSI between 50 and 70`.
- **Custom Screener Builder**: Combine multiple fundamental and technical parameters and save custom screens to your Supabase profile.

---

## 5. Scoring, Risk & Decision Engines

### 5.1 Multi-Factor Scoring & Verdict Formula

The system uses a unified scoring framework ($0-100$) with distinct weights for entry (Buy) and exit (Sell) evaluations:

$$\text{Total Score} = \sum_{i} \left( \text{Category Score}_i \times \text{Weight}_i \right)$$

#### Buy Page Weights & Verdict Thresholds
| Category | Weight | Sub-Score Calculation Drivers |
|---|---|---|
| **Technical Indicators** | **30%** | RSI, MACD trend, Moving Average alignment (Price > 50 > 200 DMA), ADX strength. |
| **ML Model Consensus** | **25%** | Ensemble probability of Bullish return across XGBoost, LSTM, and Chronos models. |
| **Ownership / Smart Money** | **20%** | Mutual fund accumulation, FII net flow, zero promoter pledge, insider buying. |
| **Market Internals** | **15%** | Delivery volume %, Put-Call Ratio (PCR), relative sector strength. |
| **News & Sentiment** | **10%** | Scored sentiment from recent company filings and material announcements. |

- $\ge 75$: **Strong Buy**
- $55 - 74$: **Buy**
- $35 - 54$: **Hold / Neutral**
- $< 35$: **Avoid**

#### Sell Page (Time to Leave) Weights & Verdict Thresholds
| Category | Weight | Sub-Score Calculation Drivers |
|---|---|---|
| **Exit-Engine Proximity** | **35%** | Closeness to Stop-loss, Trailing stop trigger, Target reached, Max holding period. |
| **Technical Reversal** | **25%** | Bearish divergence (RSI), MACD death cross, break below 50 DMA, high volume sell-off. |
| **Model Reversal Signal** | **20%** | ML model consensus flipping to Bearish or Sharp confidence drop. |
| **Ownership / Insider Selling** | **10%** | Promoter stake reduction, institutional outflow, promoter pledge spike. |
| **News / Sentiment Deterioration**| **10%** | Negative material filings, litigation, earnings downgrade. |

- $\ge 75$: **Exit Now**
- $55 - 74$: **Consider Exiting**
- $35 - 54$: **Hold Position**
- $< 35$: **Safe / Long Hold**

---

### 5.2 Deterministic Risk & Exit Engines

#### Risk Engine
Computes objective risk ratings (**Low / Moderate / High**) based on:
1. **Historical Volatility (Annualized ATR & Beta)**.
2. **Maximum Historical Drawdown (MDD)**.
3. **Debt-to-Equity & Interest Coverage Health**.
4. **Liquidity & Delivery Ratio**.

#### Exit Engine Rules
Monitors open holdings in `portfolio.sqlite` and triggers whenever any rule fires:
- **Stop-Loss Trigger**: $\text{Current Price} \le \text{Entry Price} \times (1 - \text{StopLoss}_\%)$.
- **Profit Target Trigger**: $\text{Current Price} \ge \text{Entry Price} \times (1 + \text{Target}_\%)$.
- **Trailing Stop Trigger**: $\text{Current Price} \le \text{Peak Price Since Entry} \times (1 - \text{Trailing}_\%)$.
- **Max Holding Period**: $\text{Current Date} - \text{Entry Date} > \text{MaxDays}$.
- **Quantitative Reversal**: Multi-model consensus turns Bearish.

---

### 5.3 Forensic Accounting & Contradiction Alerts

The engine applies deterministic rules to detect hidden corporate stress:
- ⚠️ **Cash Flow Divergence**: Reported PAT $\uparrow$ by $>15\%$, but Operating Cash Flow (OCF) $\downarrow$.
- ⚠️ **Aggressive Revenue Recognition**: Revenue $\uparrow$ by $10\%$, but Trade Receivables $\uparrow > 30\%$.
- ⚠️ **Margin Deterioration**: Revenue $\uparrow$, but Operating & Gross Margin $\downarrow > 300\text{ bps}$.
- ⚠️ **Quality vs Valuation Paradox**: ROCE $>20\%$, but trading at historical peak P/E ($>3\sigma$).
- ⚠️ **Promoter Disconnection**: Company claims record growth, but Promoters/Insiders are aggressively selling or pledging shares.

---

## 6. Quantitative ML & AI Research Architecture

### 6.1 Machine Learning Pipeline

```
+-----------------------------------------------------------------------------+
| 1. DATA PREPARATION: Daily OHLCV -> Indicator Features -> 3-Class Labels     |
|    (BUY: Return > +3%, HOLD: -3% to +3%, SELL: Return < -3% over 10-day bar)|
+-----------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------+
| 2. TIME-SERIES SPLITTING: Strict Walk-Forward Validation (Zero Data Leakage)|
+-----------------------------------------------------------------------------+
                                      |
         +----------------------------+----------------------------+
         |                                                         |
         v                                                         v
+-----------------------------+                           +-----------------------------+
| XGBoost Classifier          |                           | PyTorch LSTM Sequence Model |
| Tabular Indicator Features  |                           | Rolling 20-Day Tensor Window|
+--------------+--------------+                           +--------------+--------------+
               |                                                         |
               +----------------------------+----------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------+
| 3. PRETRAINED FOUNDATION MODEL: Amazon Chronos Zero-Shot Forecast           |
+-----------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------+
| 4. ENSEMBLE / META-MODEL: Backtest-Weighted Stacking & Consensus            |
+-----------------------------------------------------------------------------+
```

### 6.2 AI Research Agent (Structured Interpreter)

The AI Agent never generates quantitative predictions on its own. It acts as an **expert financial analyst** that reads structured JSON payloads and produces insightful narratives.

#### Available Agent Tools
- `get_forecast(symbol)`: Returns ensemble forecast, per-model win rates, and expected return.
- `get_indicators(symbol)`: Returns RSI, MACD, Moving Averages, and Volatility.
- `get_financials(symbol)`: Returns 5-year revenue, margins, ROCE, and debt metrics.
- `get_forensic_alerts(symbol)`: Returns rule-based accounting contradiction flags.
- `get_ownership(symbol)`: Returns promoter, FII, DII, and mutual fund trends.
- `get_latest_news(symbol)`: Returns summarized recent announcements and sentiment.

---

## 7. Data Ingestion & Provider Specification

### 7.1 INDstocks API Integration (NSE/BSE Primary)

Used as the primary market data provider for Indian securities (`/market/historical/{interval}`).

```text
INDstocks API
   │
   ├── 1. Instrument Master ──► Synced to stock_master (ISIN, Security ID, Symbol)
   │
   └── 2. Historical OHLCV  ──► Downloaded in Paginated Windows
                                 │
                                 ▼
                         Data Validation Rules
                         (Open, High, Low, Close > 0; High >= Low; Volume >= 0)
                                 │
                                 ▼
                         Normalized SQLite Cache
```

#### Pagination & Security Requirements
- Download multi-year history in API-compliant chunks (e.g., 1-year windows), merge, deduplicate, and sort.
- **Security Rule**: API Token is stored strictly on the backend (`.env` / Secret Manager). **Never expose the token to the Android client or git repositories.**
- **Rate Limit Resilience**: Throttling with exponential backoff on HTTP 429.

### 7.2 Secondary Providers & Connectors
- **yfinance**: US Equities (AAPL, NVDA, etc.) and global fallback data.
- **NSE/BSE & SEC EDGAR / Screener**: Shareholding patterns, quarterly financial statements, annual reports, and insider disclosures.

---

## 8. Database Schemas (Supabase PostgreSQL)

The database is divided into two distinct domains:
1. **Global Market & Quantitative Data** (Shared across all users, synchronized by backend pipeline).
2. **Per-User Synchronized Data** (Tied to `auth.users(id)` via Row-Level Security, synced in realtime across mobile & web).

```sql
-- Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- ============================================================================
-- PART A: GLOBAL MARKET & RESEARCH DATA (Managed by Backend Ingestion Pipeline)
-- ============================================================================

-- 1. Security & Master Data
CREATE TABLE public.stock_master (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol TEXT NOT NULL,
    company_name TEXT NOT NULL,
    exchange TEXT NOT NULL,          -- 'NSE', 'BSE', 'NASDAQ', 'NYSE'
    security_id TEXT,               -- INDstocks security ID
    isin TEXT,
    sector TEXT,
    industry TEXT,
    active BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(symbol, exchange)
);
CREATE INDEX idx_stock_master_symbol ON public.stock_master(symbol);
CREATE INDEX idx_stock_master_sec_id ON public.stock_master(security_id);

-- 2. Normalized Historical Market Data (Partition-ready daily & weekly OHLCV)
CREATE TABLE public.ohlcv (
    symbol TEXT NOT NULL,
    exchange TEXT NOT NULL,
    date DATE NOT NULL,
    open NUMERIC NOT NULL,
    high NUMERIC NOT NULL,
    low NUMERIC NOT NULL,
    close NUMERIC NOT NULL,
    volume BIGINT NOT NULL,
    interval TEXT DEFAULT '1d',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (symbol, exchange, date, interval)
);
CREATE INDEX idx_ohlcv_lookup ON public.ohlcv(symbol, exchange, date DESC);

-- 3. Fundamental & Financial Data (5-10 Years of Statements)
CREATE TABLE public.company_financials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol TEXT NOT NULL,
    period_type TEXT NOT NULL,      -- 'annual' or 'quarterly'
    period_date DATE NOT NULL,      -- '2026-03-31'
    revenue NUMERIC,
    ebitda NUMERIC,
    ebit NUMERIC,
    pat NUMERIC,
    eps NUMERIC,
    ocf NUMERIC,
    fcf NUMERIC,
    total_debt NUMERIC,
    cash_equivalents NUMERIC,
    roce NUMERIC,
    roe NUMERIC,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(symbol, period_type, period_date)
);

-- 4. Shareholding & Ownership Patterns
CREATE TABLE public.shareholding (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol TEXT NOT NULL,
    quarter_date DATE NOT NULL,
    promoter_pct NUMERIC,
    fii_pct NUMERIC,
    dii_pct NUMERIC,
    mutual_fund_pct NUMERIC,
    retail_pct NUMERIC,
    promoter_pledge_pct NUMERIC,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(symbol, quarter_date)
);

-- 5. Document Embeddings for AI Research Agent (pgvector)
CREATE TABLE public.document_embeddings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol TEXT NOT NULL,
    doc_type TEXT NOT NULL,          -- 'annual_report', 'transcript', 'filing', 'news'
    period TEXT,
    chunk_content TEXT NOT NULL,
    embedding vector(1536),          -- OpenAI text-embedding-3-small
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_doc_embeddings_vector ON public.document_embeddings USING ivfflat (embedding vector_cosine_ops);

-- 6. Model Registry & Quantitative Signal Store
CREATE TABLE public.model_registry (
    model_id TEXT PRIMARY KEY,
    symbol TEXT NOT NULL,
    model_type TEXT NOT NULL,       -- 'xgboost', 'lstm', 'chronos'
    training_date TIMESTAMPTZ NOT NULL,
    features_used JSONB,
    val_accuracy NUMERIC,
    backtest_win_rate NUMERIC,
    backtest_sharpe NUMERIC,
    artifact_storage_path TEXT
);

-- ============================================================================
-- PART B: PER-USER SYNCHRONIZED DATA (Tied to auth.users with RLS & Realtime)
-- ============================================================================

-- 7. User Profile & Preferences (Multi-device sync)
CREATE TABLE public.user_profiles (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT,
    currency TEXT DEFAULT 'INR',     -- 'INR', 'USD'
    theme TEXT DEFAULT 'dark',       -- 'dark', 'light', 'system'
    risk_profile TEXT DEFAULT 'moderate', -- 'conservative', 'moderate', 'aggressive'
    default_stop_loss_pct NUMERIC DEFAULT 0.05,
    default_target_pct NUMERIC DEFAULT 0.15,
    default_trailing_stop_pct NUMERIC DEFAULT 0.04,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own profile" ON public.user_profiles FOR ALL USING (auth.uid() = user_id);

-- 8. User Custom Engine Scoring Weights (Tunable Buy/Sell Weights)
CREATE TABLE public.user_scoring_weights (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    engine_type TEXT NOT NULL,       -- 'buy' or 'sell'
    technicals_weight NUMERIC NOT NULL DEFAULT 0.30,
    models_weight NUMERIC NOT NULL DEFAULT 0.25,
    ownership_weight NUMERIC NOT NULL DEFAULT 0.20,
    internals_weight NUMERIC NOT NULL DEFAULT 0.15,
    sentiment_weight NUMERIC NOT NULL DEFAULT 0.10,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, engine_type)
);
ALTER TABLE public.user_scoring_weights ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own weights" ON public.user_scoring_weights FOR ALL USING (auth.uid() = user_id);

-- 9. User Watchlists (Curated Target Pipeline & Notes)
CREATE TABLE public.user_watchlists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    watchlist_name TEXT DEFAULT 'Default',
    symbol TEXT NOT NULL,
    exchange TEXT NOT NULL,
    latest_score NUMERIC,            -- 0-100 Multi-factor Buy score
    verdict TEXT,                    -- 'Strong Buy', 'Buy', 'Hold', 'Avoid'
    personal_thesis TEXT,
    added_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, watchlist_name, symbol, exchange)
);
ALTER TABLE public.user_watchlists ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own watchlist" ON public.user_watchlists FOR ALL USING (auth.uid() = user_id);

-- 10. User Portfolio Holdings & Exit Rules (Active Positions)
CREATE TABLE public.user_holdings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    exchange TEXT NOT NULL,
    entry_price NUMERIC NOT NULL,
    entry_date DATE NOT NULL,
    qty NUMERIC NOT NULL,
    stop_loss_pct NUMERIC DEFAULT 0.05,
    target_pct NUMERIC DEFAULT 0.15,
    trailing_stop_pct NUMERIC DEFAULT 0.04,
    max_hold_days INTEGER DEFAULT 180,
    status TEXT DEFAULT 'open',     -- 'open' or 'closed'
    time_to_leave_score NUMERIC,     -- 0-100 Exit engine score
    exit_verdict TEXT,              -- 'Hold', 'Consider Exiting', 'Exit Now'
    exit_price NUMERIC,
    exit_date DATE,
    exit_reason TEXT,               -- 'stop_loss', 'target', 'trailing_stop', 'model_sell', 'manual'
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.user_holdings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own holdings" ON public.user_holdings FOR ALL USING (auth.uid() = user_id);

-- 11. User Portfolio Transaction History (Audit log & realized P&L)
CREATE TABLE public.user_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    holding_id UUID REFERENCES public.user_holdings(id) ON DELETE SET NULL,
    symbol TEXT NOT NULL,
    exchange TEXT NOT NULL,
    tx_type TEXT NOT NULL,           -- 'BUY' or 'SELL'
    price NUMERIC NOT NULL,
    qty NUMERIC NOT NULL,
    total_amount NUMERIC NOT NULL,
    realized_pnl NUMERIC,
    tx_date TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.user_transactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view own transactions" ON public.user_transactions FOR ALL USING (auth.uid() = user_id);

-- 12. User Custom DCF Models (User-tuned assumptions)
CREATE TABLE public.user_dcf_models (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    revenue_growth_y1_5 NUMERIC NOT NULL,
    terminal_growth_rate NUMERIC NOT NULL DEFAULT 0.04,
    ebitda_margin NUMERIC NOT NULL,
    tax_rate NUMERIC NOT NULL DEFAULT 0.25,
    wacc NUMERIC NOT NULL DEFAULT 0.11,
    fair_value_per_share NUMERIC,
    upside_downside_pct NUMERIC,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, symbol)
);
ALTER TABLE public.user_dcf_models ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own DCF models" ON public.user_dcf_models FOR ALL USING (auth.uid() = user_id);

-- 13. User Scenario Analyses (Bull / Base / Bear custom parameters)
CREATE TABLE public.user_scenarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    scenario_type TEXT NOT NULL,     -- 'bull', 'base', 'bear'
    rev_growth NUMERIC NOT NULL,
    target_margin NUMERIC NOT NULL,
    target_pe_multiple NUMERIC NOT NULL,
    target_price NUMERIC NOT NULL,
    notes TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, symbol, scenario_type)
);
ALTER TABLE public.user_scenarios ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own scenarios" ON public.user_scenarios FOR ALL USING (auth.uid() = user_id);

-- 14. User Personal Research Notes & Monitoring Checklists
CREATE TABLE public.user_stock_notes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    note_title TEXT,
    content TEXT NOT NULL,
    tags TEXT[],
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.user_stock_notes ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own notes" ON public.user_stock_notes FOR ALL USING (auth.uid() = user_id);

CREATE TABLE public.user_monitoring_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    item_key TEXT NOT NULL,          -- 'quarterly_result', 'promoter_activity', 'capex', etc.
    status TEXT NOT NULL DEFAULT 'watch', -- 'good', 'watch', 'concern'
    notes TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, symbol, item_key)
);
ALTER TABLE public.user_monitoring_items ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own monitoring checklist" ON public.user_monitoring_items FOR ALL USING (auth.uid() = user_id);

-- 15. User Saved Screeners & Price/Trigger Alerts
CREATE TABLE public.user_saved_screeners (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    screener_name TEXT NOT NULL,
    filter_criteria JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.user_saved_screeners ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own screeners" ON public.user_saved_screeners FOR ALL USING (auth.uid() = user_id);

CREATE TABLE public.user_alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    alert_type TEXT NOT NULL,        -- 'price_above', 'price_below', 'rsi_oversold', 'stop_loss_proximity'
    threshold_value NUMERIC NOT NULL,
    is_triggered BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.user_alerts ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own alerts" ON public.user_alerts FOR ALL USING (auth.uid() = user_id);

-- ============================================================================
-- SUPABASE REALTIME REPLICATION (Instant Multi-Device Sync)
-- ============================================================================
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_profiles;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_scoring_weights;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_watchlists;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_holdings;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_transactions;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_dcf_models;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_scenarios;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_stock_notes;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_monitoring_items;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_alerts;

---

## 9. Backend REST API Specification

All endpoints return JSON responses with standardized error handling and metadata.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/explore/search?q={query}` | Search stocks by symbol, name, or ISIN across exchanges. |
| `POST` | `/api/v1/explore/screen` | Screen stocks with multi-variable filters (ROCE, P/E, 50 DMA, etc.). |
| `GET` | `/api/v1/stocks/{symbol}` | Overview, live/latest quote, metadata, and quick health scores. |
| `GET` | `/api/v1/stocks/{symbol}/prices?range=5y&interval=1d` | Historical OHLCV candle array. |
| `GET` | `/api/v1/stocks/{symbol}/technicals` | Computed indicators (RSI, MACD, EMAs, Support/Resistance). |
| `GET` | `/api/v1/stocks/{symbol}/financials` | 5-10 Year P&L, Balance Sheet, Cash Flow, and margin trends. |
| `GET` | `/api/v1/stocks/{symbol}/forensics` | Computed accounting red flags & contradiction alerts. |
| `GET` | `/api/v1/stocks/{symbol}/ownership` | Shareholding breakdown, MF holdings, and promoter trends. |
| `GET` | `/api/v1/stocks/{symbol}/peers` | Comparison metrics against 3-5 industry peers. |
| `POST` | `/api/v1/stocks/{symbol}/valuation/dcf` | Dynamic DCF calculation based on user assumptions. |
| `GET` | `/api/v1/stocks/{symbol}/forecast` | Runs XGBoost/LSTM/Chronos models & returns Buy/Sell verdict. |
| `GET` | `/api/v1/stocks/{symbol}/agent/summary` | AI Research Agent narrative analysis and risk breakdown. |
| `GET` | `/api/v1/watchlist` | List all curated watchlist stocks. |
| `POST` | `/api/v1/watchlist` | Add stock to watchlist. |
| `DELETE`| `/api/v1/watchlist/{symbol}` | Remove stock from watchlist. |
| `GET` | `/api/v1/portfolio` | List open holdings with live P&L and Exit Engine sub-scores. |
| `POST` | `/api/v1/portfolio/buy` | Record a new stock purchase. |
| `POST` | `/api/v1/portfolio/sell/{id}` | Close an active holding position. |

---

## 10. Phased Implementation Roadmap

```
+-----------------------------------------------------------------------------------+
| PHASE 1: DATA FOUNDATION & MARKET DATA                                            |
| * INDstocks API client (Instrument Master, Historical OHLCV pagination)           |
| * SQLite database schema setup & data normalization                               |
| * Technical indicator engine (pandas-ta / numpy)                                  |
| * Basic FastAPI endpoints & end-to-end milestone test (ZAGGLE data fetch & chart) |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
| PHASE 2: MANUAL RESEARCH & FUNDAMENTAL ENGINE                                     |
| * Financials repository (P&L, Balance sheet, Cash flow)                           |
| * Forensic accounting contradiction alert engine                                  |
| * Ownership, mutual funds, and governance data parsers                            |
| * Peer comparison & Screener filter engine                                        |
| * Interactive Valuation calculators (DCF, Multiples, Scenarios)                   |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
| PHASE 3: QUANTITATIVE ML & BACKTESTING PIPELINE                                   |
| * 3-Class label builder & walk-forward time-series split                          |
| * XGBoost tabular indicator model                                                 |
| * PyTorch LSTM 20-day rolling sequence model                                      |
| * Amazon Chronos zero-shot forecasting integration                                |
| * Walk-forward backtester with stop-loss/target exit simulation                   |
| * Stacking ensemble & model registry                                              |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
| PHASE 4: DECISION, RISK & SCORING VERDICT ENGINES                                 |
| * Deterministic Risk Engine (volatility, drawdown, liquidity)                     |
| * 0-100 Weighted Buy Page Scoring Engine (Technicals, ML, Smart Money, Internals) |
| * 0-100 Weighted Sell Page "Time to Leave" Exit Engine                            |
| * AI Research Agent tool integration & prompt synthesis                           |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
| PHASE 5: ANDROID PRESENTATION CLIENT                                              |
| * Clean architecture Android app (Hilt, Supabase Kotlin, Room, Jetpack Compose)   |
| * 5-Item Navigation (🏠 Home Dashboard, 🟢 Buy Page, 🔴 Sell Page, 🔎 Explore, ⚙️ Settings)|
| * Interactive Candlestick Charts & Technical Overlays                             |
| * 15+ Comprehensive Stock Detail Research Tabs                                    |
| * Realtime sync for Watchlists, DCF models, Scenarios, and Active Holdings        |
| * Standalone Calculators & Multi-Factor Screener UI                               |
+-----------------------------------------------------------------------------------+
```

---

## 11. Core Constraints & Safety Rules

1. **Decision Support Only**: All outputs are framed strictly as decision support tools, never as guaranteed financial advice.
2. **Strict Time-Series Integrity**: Walk-forward validation must always be used for backtesting—random shuffling is strictly prohibited to eliminate lookahead bias.
3. **Institutional Data Lag**: Filings and shareholding updates (13F, SHP) must be treated as lagged ($45+$ days), never as real-time trades.
4. **No Black-Box Hallucinations**: The AI Research Agent must never invent quantitative scores, target prices, or win-rates—it only synthesizes data produced by the math and ML layers.
5. **Security & Secrets**: Never store API tokens (INDstocks, OpenAI, Claude) in client code, APK assets, or git commits. All credentials reside exclusively on the backend.
6. **Data Authenticity**: Never fabricate missing financial data; clearly flag missing points as `Data unavailable`.
