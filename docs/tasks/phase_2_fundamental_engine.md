# 📊 Phase 2: Fundamental Analysis, Forensics & Financial Engines

> **Goal**: Build comprehensive fundamental research engines, multi-year financial statements processing (5–10+ years), forensic accounting contradiction detection, ownership analytics, interactive DCF & scenario modeling, and multi-factor stock screening.

---

## 📋 Task Checklist

- [ ] **Task 2.1: Financial Statements Ingestion & Normalization**
  - [ ] Implement [`backend/app/data/fundamentals_fetch.py`](file:///g:/Projects/stocktics/backend/app/data/fundamentals_fetch.py).
  - [ ] Ingest annual & quarterly statements (P&L, Balance Sheet, Cash Flow) for 5–10+ fiscal years.
  - [ ] Standardize metrics: Revenue, EBITDA, EBIT, PAT, EPS, Operating Cash Flow (OCF), Free Cash Flow (FCF), Capex, Total Debt, Cash & Equivalents, Net Working Capital, Receivables, Inventory.
  - [ ] Persist statements in `public.company_financials` with deduplication by `(symbol, period_type, period_date)`.

- [ ] **Task 2.2: Ratios & CAGR Growth Engine**
  - [ ] Implement [`backend/app/engines/ratios.py`](file:///g:/Projects/stocktics/backend/app/engines/ratios.py).
  - [ ] Compute return metrics:
    - Return on Capital Employed (ROCE) = $\frac{\text{EBIT}}{\text{Total Assets} - \text{Current Liabilities}}$
    - Return on Equity (ROE) = $\frac{\text{PAT}}{\text{Shareholder Equity}}$
    - Return on Assets (ROA) = $\frac{\text{PAT}}{\text{Total Assets}}$
  - [ ] Compute operational & liquidity ratios:
    - Gross Margin %, EBITDA Margin %, EBIT Margin %, PAT Margin %
    - Asset Turnover, Inventory Turnover Days, Days Sales Outstanding (DSO), Cash Conversion Cycle (CCC)
    - Debt-to-Equity, Net Debt-to-EBITDA, Interest Coverage Ratio, Current Ratio
  - [ ] Compute 3-Year, 5-Year, and 10-Year Compounded Annual Growth Rates (CAGR) for Revenue, EBITDA, PAT, and EPS.
  - [ ] Classify growth trajectory: Organic, Acquisition-driven, Margin-driven, or Unsustainable.

- [ ] **Task 2.3: Forensic Accounting & Contradiction Alert Engine**
  - [ ] Implement [`backend/app/engines/forensic.py`](file:///g:/Projects/stocktics/backend/app/engines/forensic.py).
  - [ ] Implement 8 deterministic accounting red flag detectors:
    1. ⚠️ **PAT ↑ but OCF ↓**: Net Profit increasing while Operating Cash Flow decreases (divergence $> 15\%$).
    2. ⚠️ **Revenue ↑ but Receivables ↑↑**: Trade Receivables growing $> 2\times$ faster than sales.
    3. ⚠️ **EBITDA ↑ but FCF ↓**: Heavy capex or working capital drain despite operating profitability.
    4. ⚠️ **Debt ↑ while Profitability ↓**: Increasing leverage to mask falling operational margins.
    5. ⚠️ **Large Other Income**: Non-operating income exceeding $20\%$ of Pre-Tax Income.
    6. ⚠️ **Large One-Time Gains**: Artificially inflated quarterly/annual earnings.
    7. ⚠️ **Related-Party Transactions**: High capital transfers to promoter-controlled entities.
    8. ⚠️ **Contingent Liabilities**: Off-balance-sheet claims exceeding $15\%$ of Net Worth.
  - [ ] Output structured alerts with severity levels (`CRITICAL`, `WARNING`, `NEUTRAL`).

- [ ] **Task 2.4: Ownership, Smart Money & Institutional Activity**
  - [ ] Implement [`backend/app/data/ownership_fetch.py`](file:///g:/Projects/stocktics/backend/app/data/ownership_fetch.py).
  - [ ] Track 8-quarter historical ownership trends across Promoters, FIIs, DIIs, Mutual Funds, and Retail.
  - [ ] Monitor Promoter Pledge % changes (Zero, Low $<10\%$, High $>25\%$).
  - [ ] Implement Institutional Move Tracker (New entrants, Stake additions, Stake reductions, Exits).
  - [ ] Persist ownership in `public.shareholding`.

- [ ] **Task 2.5: Interactive Valuation Models & DCF Calculator**
  - [ ] Implement [`backend/app/engines/valuation.py`](file:///g:/Projects/stocktics/backend/app/engines/valuation.py).
  - [ ] Compute Relative Valuation Multiples: Trailing P/E, Forward P/E, P/B, EV/EBITDA, PEG, Dividend Yield vs 5-year average & industry peers.
  - [ ] Implement Historical P/E Valuation Bands ($Low \leftrightarrow Fair \leftrightarrow High$).
  - [ ] Multi-Stage Discounted Cash Flow (DCF) Model:
    - User inputs: 5Y Revenue Growth %, EBITDA Margin %, Tax Rate %, Capex % of Rev, Working Cap % of Rev, WACC (Discount Rate %), Terminal Growth Rate %.
    - Computes: Projected FCFs, Enterprise Value, Net Debt deduction, Equity Value, Fair Value per Share, and Implied Upside/Downside %.
  - [ ] Save user-tuned models to `public.user_dcf_models`.

- [ ] **Task 2.6: Future Outlook & Scenario Builder Engine**
  - [ ] Implement [`backend/app/engines/scenario.py`](file:///g:/Projects/stocktics/backend/app/engines/scenario.py).
  - [ ] Bull / Base / Bear scenario matrix generator:
    - **🟢 Bull**: High growth, margin expansion, re-rated P/E multiple.
    - **🟡 Base**: Consensus growth, steady margins, fair historical P/E.
    - **🔴 Bear**: Downside growth, margin compression, trough multiple.
  - [ ] Provide clear assumption provenance tagging: `FACT`, `MANAGEMENT GUIDANCE`, `USER ASSUMPTION`, `MODEL CALCULATION`.
  - [ ] Persist scenarios to `public.user_scenarios`.

- [ ] **Task 2.7: Peer Comparison & Screener Filter Engine**
  - [ ] Side-by-side comparative matrix against 3–5 industry peers across valuation, returns, leverage, and growth.
  - [ ] Implement Pre-built Screeners:
    - 🚀 **High Growth**: `Rev Growth > 15% AND PAT Growth > 15% AND ROCE > 15%`
    - 💎 **Quality Moat**: `ROCE > 20% AND ROE > 15% AND Debt/Equity < 0.5`
    - 💰 **Deep Value**: `P/E < 5Y Avg AND Debt/Equity < 0.3 AND OCF/PAT > 0.8`
    - 💵 **Cash Flow Champion**: `OCF/PAT > 1.0 AND FCF > 0 AND Net Debt <= 0`
    - 📈 **Momentum**: `Price > 50 DMA AND Price > 200 DMA AND RSI 50..70`
  - [ ] Custom Screener Query Engine with JSON filter AST execution.

---

## 🎯 Acceptance Criteria
- [ ] Financial statements parse cleanly for 5+ historical years with zero NaN crashes.
- [ ] Forensic red flags accurately detect divergence on known distressed balance sheets.
- [ ] DCF calculator produces mathematically sound fair value outputs matching manual Excel models.
- [ ] Screener engine filters 1,000+ stocks in under 200ms.
