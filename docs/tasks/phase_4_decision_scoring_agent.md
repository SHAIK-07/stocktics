# 🎯 Phase 4: Scoring, Risk, Exit Engines & AI Research Agent

> **Goal**: Build the multi-factor scoring verdict engine (0–100 Buy Score and 0–100 Sell "Time to Leave" Score), deterministic risk classifier, 14-category investor scorecard, 16-item monitoring checklist, and the zero-hallucination AI research agent.

---

## 📋 Task Checklist

- [ ] **Task 4.1: Deterministic Risk Engine**
  - [ ] Implement [`backend/app/engines/risk_engine.py`](file:///g:/Projects/stocktics/backend/app/engines/risk_engine.py).
  - [ ] Calculate quantitative risk metrics:
    - Annualized Volatility & Beta vs NIFTY / S&P 500
    - Historical Maximum Drawdown (MDD) over 1Y, 3Y, 5Y
    - Solvency Health (Debt/Equity, Interest Coverage, Current Ratio)
    - Liquidity & Delivery Ratio %
  - [ ] Evaluate 14-Category Risk Rating Matrix (Probability $\times$ Impact $\to$ `Low`, `Moderate`, `High`):
    1. Business Model, 2. Financial Health, 3. Debt & Solvency, 4. Competitive Intensity, 5. Regulatory, 6. Governance, 7. Promoter Pledge, 8. Customer Concentration, 9. Commodity Sensitivity, 10. FX / Currency, 11. Technology Disruption, 12. Macro Cyclicality, 13. Valuation Multiple, 14. Capex Execution.

- [ ] **Task 4.2: 0–100 Weighted Buy Page Scoring Engine**
  - [ ] Implement [`backend/app/engines/scoring_engine.py`](file:///g:/Projects/stocktics/backend/app/engines/scoring_engine.py).
  - [ ] Compute 5 Category Sub-scores ($0-100$ each):
    - 📈 **Technical Indicators (30%)**: RSI, MACD, MA Alignment (Price > 50 > 200 DMA), ADX trend strength.
    - 🤖 **ML Model Consensus (25%)**: Weighted ensemble probability of bullish return.
    - 👥 **Ownership / Smart Money (20%)**: MF/FII net flow, zero promoter pledge, insider buying.
    - 📊 **Market Internals (15%)**: Delivery volume %, PCR, relative sector strength.
    - 📰 **News & Sentiment (10%)**: Scored sentiment from filings and material corporate news.
  - [ ] Compute Total Buy Score and classify Verdict:
    - $\ge 75$: `Strong Buy` 🟢
    - $55 - 74$: `Buy` 🟢
    - $35 - 54$: `Hold / Neutral` 🟡
    - $< 35$: `Avoid` 🔴
  - [ ] Support custom user scoring weights from `public.user_scoring_weights`.

- [ ] **Task 4.3: 0–100 Weighted Sell Page "Time to Leave" Exit Engine**
  - [ ] Implement [`backend/app/engines/exit_engine.py`](file:///g:/Projects/stocktics/backend/app/engines/exit_engine.py).
  - [ ] Compute 5 Exit Sub-scores ($0-100$ each):
    - 🎯 **Exit-Engine Proximity (35%)**: Proximity to Stop-Loss %, Trailing Stop trigger, Target reached %, Holding duration vs max days.
    - 📉 **Technical Reversal (25%)**: Bearish RSI divergence, MACD death cross, 50 DMA breakdown, high-volume sell bar.
    - 🤖 **Model Reversal Signal (20%)**: ML model consensus flipping to Bearish or confidence collapse.
    - 👔 **Ownership / Insider Selling (10%)**: Promoter stake reduction, institutional exit, pledge spike.
    - 📰 **News / Sentiment Deterioration (10%)**: Regulatory action, auditor resignation, earnings miss.
  - [ ] Classify Exit Verdict:
    - $\ge 75$: `Exit Now` 🔴
    - $55 - 74$: `Consider Exiting` 🟠
    - $35 - 54$: `Hold Position` 🟡
    - $< 35$: `Safe / Long Hold` 🟢

- [ ] **Task 4.4: Master Investor Scorecard & 16-Item Checklist**
  - [ ] Compute 14-Category Scorecard (0–10 per dimension): Business, Industry, Management, Financials, Growth, Cash Flow, Balance Sheet, Capital Allocation, Ownership, Moat, Valuation, Technicals, Risk, Outlook.
  - [ ] Automatically extract **Top 5 Positive Signals** and **Top 5 Warning Signals**.
  - [ ] Generate 16-Item Persistent Monitoring Checklist (`🟢 Good`, `🟡 Watch`, `🔴 Concern`).

- [ ] **Task 4.5: AI Research Agent & Tool Calling Suite**
  - [ ] Implement [`backend/app/agent/research_agent.py`](file:///g:/Projects/stocktics/backend/app/agent/research_agent.py), [`tools.py`](file:///g:/Projects/stocktics/backend/app/agent/tools.py), and [`prompts.py`](file:///g:/Projects/stocktics/backend/app/agent/prompts.py).
  - [ ] Register structured agent tools:
    - `get_forecast(symbol)`
    - `get_indicators(symbol)`
    - `get_financials(symbol)`
    - `get_forensic_alerts(symbol)`
    - `get_ownership(symbol)`
    - `get_latest_news(symbol)`
  - [ ] Enforce zero-hallucination prompt architecture: AI only interprets returned tool JSON.
  - [ ] Integrate `pgvector` semantic search over `public.document_embeddings` for annual reports and earnings call transcripts.

- [ ] **Task 4.6: Decision & Portfolio REST API Endpoints**
  - [ ] Implement [`backend/app/api/v1/watchlist.py`](file:///g:/Projects/stocktics/backend/app/api/v1/watchlist.py) (`GET`, `POST`, `DELETE /api/v1/watchlist`).
  - [ ] Implement [`backend/app/api/v1/portfolio.py`](file:///g:/Projects/stocktics/backend/app/api/v1/portfolio.py):
    - `GET /api/v1/portfolio` (Holdings with live P&L and Time-to-Leave scores)
    - `POST /api/v1/portfolio/buy` (Record buy transaction & open holding)
    - `POST /api/v1/portfolio/sell/{id}` (Close holding, compute realized P&L, log transaction)
  - [ ] Implement [`backend/app/api/v1/agent.py`](file:///g:/Projects/stocktics/backend/app/api/v1/agent.py) (`GET /api/v1/stocks/{symbol}/agent/summary`).

---

## 🎯 Acceptance Criteria
- [ ] Buy Score and Time-to-Leave calculations strictly follow the mathematical weighting formulas.
- [ ] Risk engine flags high volatility or high leverage without false positives on healthy firms.
- [ ] AI agent produces crisp, structured narrative reports without generating non-existent figures.
- [ ] Buying and selling via API updates Supabase tables and triggers realtime replication events.
