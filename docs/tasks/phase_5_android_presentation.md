# 📱 Phase 5: Android Native Presentation Client (Jetpack Compose)

> **Goal**: Build a modern, state-of-the-art Android Native application using Jetpack Compose, Material 3, Clean Architecture, Hilt DI, Supabase Kotlin SDK, and interactive financial charts, implementing the full 5-Item Navigation and all 23 Deep Stock Research Tabs.

---

## 📋 Task Checklist

- [ ] **Task 5.1: Clean Architecture Foundation & Data Layer**
  - [ ] Set up Hilt Dependency Injection (`@HiltAndroidApp`, modules for Network, Database, Repository).
  - [ ] Configure Supabase Kotlin SDK for Auth, Database queries, and Realtime subscriptions.
  - [ ] Implement Retrofit / Ktor client for backend FastAPI endpoints.
  - [ ] Set up Room local database for offline-first caching of stock masters and quotes.
  - [ ] Implement Repository pattern with Kotlin `Flow` and `StateFlow`.

- [ ] **Task 5.2: Design System, Themes & Persistent 5-Item Navigation**
  - [ ] Build Material 3 theme with Dark Mode, Light Mode, and AMOLED Black support in [`ui/theme`](file:///g:/Projects/stocktics/app/src/main/java/com/stocktics/ui/theme).
  - [ ] Implement vibrant financial color tokens:
    - Bullish Green (`#00E676`), Bearish Red (`#FF5252`), Warning Amber (`#FFD600`), Accent Purple (`#7C4DFF`).
  - [ ] Implement Bottom Navigation Bar with 5 persistent tabs:
    1. 🏠 **Home** (`com.stocktics.presentation.home`)
    2. 🟢 **Buy Page** (`com.stocktics.presentation.buy`)
    3. 🔴 **Sell Page** (`com.stocktics.presentation.sell`)
    4. 🔎 **Explore** (`com.stocktics.presentation.explore`)
    5. ⚙️ **Settings** (`com.stocktics.presentation.settings`)

- [ ] **Task 5.3: 🏠 Home Dashboard Screen**
  - [ ] **Portfolio Financial Summary Card**: Total Portfolio Value, Net Invested, Total P&L (% & ₹), Today's P&L.
  - [ ] **Urgent Exit & Risk Alert Banners**: Highlight holdings nearing Stop-Loss or with `Exit Now` scores.
  - [ ] **Top Buy Pipeline Picks**: Horizontal carousel of highest-scoring watchlist stocks (`Strong Buy`).
  - [ ] **Market Indices Snapshot**: NIFTY 50, SENSEX, S&P 500, Sector trends, Top Gainers & Losers.
  - [ ] **Recent Activity & Fast Actions**: Quick jump to recently viewed stocks and transaction log.

- [ ] **Task 5.4: 🟢 Buy Page Screen (Target Pipeline & Entry Verdict)**
  - [ ] List curated watchlist stocks sorted by **0–100 Weighted Buy Score**.
  - [ ] Verdict Badges: `🟢 Strong Buy (≥75)`, `🟢 Buy (55-74)`, `🟡 Hold (35-54)`, `🔴 Avoid (<35)`.
  - [ ] Expandable 5-Factor Category Breakdown Card per stock:
    - 📈 Technicals (30%), 🤖 ML Consensus (25%), 👥 Ownership (20%), 📊 Internals (15%), 📰 Sentiment (10%).
  - [ ] **1-Tap Purchase Modal**: Bottom sheet to enter quantity and purchase price, instantly writing to `user_holdings`.

- [ ] **Task 5.5: 🔴 Sell Page Screen (Active Holdings & Exit Engine)**
  - [ ] List active open positions from `user_holdings` with Entry Price, Current Price, P&L %, and Days Held.
  - [ ] Display **0–100 "Time to Leave" Score** & Verdict Badge:
    - `🔴 Exit Now (≥75)`, `🟠 Consider Exiting (55-74)`, `🟡 Hold Position (35-54)`, `🟢 Safe (<35)`.
  - [ ] Visual Proximity Meters:
    - Distance to Stop-Loss (e.g. `⚠️ 1.8% to Stop-Loss`)
    - Trailing Stop & Profit Target progress bars
    - Technical reversal warning pills (RSI Bearish Divergence, 50 DMA break)
  - [ ] **1-Tap Sell Action**: Confirmation dialog to close position, record realized P&L, and update transaction log.

- [ ] **Task 5.6: 🔎 Explore Screen & Strategy Screeners**
  - [ ] Search bar with instant debounced search across NSE, BSE, and US symbols with fuzzy match.
  - [ ] Pre-built Strategy Screener Chips:
    - 🚀 *High Growth*, 💎 *Quality Moat*, 💰 *Deep Value*, 💵 *Cash Flow Champion*, 📈 *Momentum*.
  - [ ] Custom Screener Filter Bottom Sheet (Sliders for ROCE, P/E, Debt/Equity, Volume).
  - [ ] Direct "Add to Watchlist / Buy Pipeline" button on search result cards.

- [ ] **Task 5.7: 📊 Deep Stock Detail Screen (All 23 Research Tabs)**
  - [ ] **Sticky Header**: `← Back | [Symbol] [Name] | ₹[Price] ([Change%]) | ⭐ Watchlist | [Verdict Badge]`.
  - [ ] Implement all 23 modular tabs with smooth horizontal tab row:
    1. 📌 **Overview**: Price header, 8 Quick Health Cards (Business, Financials, Growth, Cash Flow, Balance Sheet, Management, Valuation, Technical).
    2. 🏢 **Business & Products**: Revenue by segment, geography, customer concentration cards.
    3. 🏭 **Industry & Moat**: 8-point moat checklist (Cost, Brand, Distribution, Tech, Network, Switching, Scale, Regulatory).
    4. 💰 **Financial Statements**: 5–10Y interactive horizontal financial table & multi-year trend charts.
    5. 📊 **Growth Analysis**: 3Y/5Y/10Y CAGR cards & Organic vs Acquisition growth tags.
    6. 🏦 **Balance Sheet & Liquidity**: Debt vs Cash charts, DSO/Inventory trends, balance sheet red flags.
    7. 💵 **Cash Flow & Forensics**: PAT vs OCF conversion, EBITDA vs FCF, 8 Accounting Contradiction alerts.
    8. 📐 **Operating Ratios**: ROCE, ROE, ROA, Margins, Asset Turnover with 5-year visual trendlines.
    9. 👥 **Shareholding**: Ownership distribution pie chart, 8-quarter trends, promoter pledge tracker.
    10. 🏛️ **Mutual Funds & FIIs**: Top institutional holders, quarterly additions/reductions/exits.
    11. 👔 **Management & Governance**: Tenure, remuneration % of PAT, capital allocation score.
    12. 📑 **Annual Reports**: Chronological archive with extracted MD&A, CEO letter, and auditor notes.
    13. 🎙️ **Quarterly Results**: Q-o-Q and Y-o-Y financial history table with margin progression.
    14. 📞 **Earnings Transcripts**: Categorized commentary (Outlook, Margins, Capex) and Management Promise Tracker.
    15. 📰 **News & Announcements**: Filterable newsfeed (Material vs General) with sentiment badges.
    16. 📜 **Regulatory Filings**: Corporate disclosures, insider trades, and bulk deals.
    17. 🏆 **Peer Comparison**: Side-by-side benchmarking table with color-coded comparison cells.
    18. 📈 **Technical Analysis**: Interactive Candlestick chart (1D to MAX) with toggleable overlays (SMA, EMA, RSI, MACD, Bollinger, VWAP).
    19. 💎 **Valuation & DCF**: Relative multiples, Historical P/E band, and Interactive Editable DCF model.
    20. 🔮 **Future Outlook**: Interactive Bull / Base / Bear scenario slider tool.
    21. ⚠️ **Risk Matrix**: 14-category risk evaluation with early-warning triggers.
    22. 🤖 **ML Forecast & AI Synthesis**: Model consensus, Chronos projections, walk-forward win-rate, and AI report.
    23. 🎯 **Master Scorecard**: 14-dimension scores, Top 5 positives/warnings, and 16-item monitoring checklist.

- [ ] **Task 5.8: 🧮 Standalone Financial Calculators Suite (10 Tools)**
  - [ ] Dedicated calculators screen accessible from drawer/explore:
    1. CAGR Calculator
    2. Margin Calculator
    3. DuPont ROE Decomposition Tool
    4. ROCE Calculator
    5. Full Multi-Stage DCF Valuation Tool
    6. Normalized EPS Growth Calculator
    7. Historical P/E Target Calculator
    8. PEG (Growth at a Reasonable Price) Calculator
    9. Position Sizing & Capital Risk Calculator
    10. Risk / Reward Ratio Calculator

- [ ] **Task 5.9: ⚙️ Settings & Strategy Customization**
  - [ ] Supabase Authentication screen (Email / Magic Link / Google Sign-in) & multi-device sync status.
  - [ ] Scoring Engine Weight Tuner (Interactive sliders for Technicals, ML, Ownership, Internals, Sentiment).
  - [ ] Default Risk Settings (Stop-Loss %, Profit Target %, Trailing Stop %).
  - [ ] INDstocks API Token Manager & Connection Health Tester.
  - [ ] Currency toggle (INR ₹ / USD $) and App Theme selector.

---

## 🎯 Acceptance Criteria
- [ ] Compose UI renders smoothly with 60fps animations and zero frame drops on complex tabs.
- [ ] Realtime push updates immediately reflect price changes and watchlist score updates.
- [ ] Candlestick chart supports pinch-to-zoom, pan, and smooth overlay toggles.
- [ ] 1-Tap Buy and Sell actions execute transactions and update Room + Supabase state atomically.
