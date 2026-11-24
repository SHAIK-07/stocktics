# Stocktics — Final Consolidated Project Specification

**Purpose of this document:** the single hand-off reference for a coding
agent. Combines the application architecture (UI + backend), the
validated Phase 2/3 research findings, and the robustness/anti-overfitting
audit trail that justifies each component's inclusion.

**Type:** Personal-use project, single user, runs locally. Not for
commercial distribution. Not financial advice — a decision-support tool.

**Status:** Phase 2 (fundamental) and Phase 3 (quant ML) research
complete and empirically validated against realistic conditions
(sample-size audit, leakage checks, transaction costs, cross-sectional
correlation). Application layer (Explore/Watchlist/Buy/Sell) not yet
built. Phase 3.5 robustness items mostly closed; see section 11 for what
remains.

---

## 1. Project Overview & Philosophy

A system that takes a stock ticker (US or NSE/Indian) and produces a
risk-aware decision — not a naive "the stock will hit ₹1,540" point
prediction, but a probabilistic, explainable verdict combining:

- Fundamental analysis + forensic accounting checks
- Quantitative technical/microstructure features
- Multiple ML models, validated with proper time-series methodology
- Ownership/insider/smart-money signals
- An AI research agent for qualitative context
- A deterministic risk engine and exit engine

**Guiding principle carried through every layer:** every reported result
must survive being asked "what's the sample size, is there leakage, what
does it look like after realistic costs, and is it stable across time
periods and stocks?" Numbers that haven't cleared that bar are labeled
provisional, not production-grade — this discipline is what separates
this project from a backtest that merely looks good.

**Prediction horizons:** days to ~1 year. Daily-bar system, not
tick-level/HFT. Short horizons (10D-1M) get statistical confidence
intervals; long horizons (6M-1Y) get scenario ranges instead, because the
research below found long-horizon CIs are not statistically calibrated.

---

## 2. Validated Research Status (Phase 2 & 3)

This section summarizes what's been built and empirically tested in the
research notebooks, with the caveats that survived audit.

### 2.1 Fundamental & Forensic Engine — 🟢 solid foundation
- **Financial statement normalizer**: 5+ years of Income Statement,
  Balance Sheet, Cash Flow data
- **Ratios**: ROCE, ROE, FCF Conversion, 3/5-year CAGR — recommended
  additions: Debt/EBITDA, Net Debt/EBITDA, Interest Coverage, Debt/Equity,
  FCF/Debt, OCF/PAT, Accrual Ratio, Receivable/Inventory/Payable Days
- **8-point forensic detector**: PAT-vs-OCF divergence, receivables vs
  revenue growth, EBITDA-vs-FCF divergence, debt spikes, non-operating
  income reliance, promoter pledge, working-capital bloat, contingent
  liabilities — **recommendation: convert from pass/fail flags to a
  weighted 0-100 Forensic Risk Score** (see section 8 for weights)
- **DCF + Bull/Base/Bear scenarios**: keep as a *valuation* input (what
  might the business be worth), not a short-term price predictor. Add
  sensitivity analysis showing how fair value moves as assumptions change

### 2.2 Quant Feature Library — 🟢 solid, don't over-expand
27 indicators across momentum, microstructure volatility (Garman-Klass,
Parkinson — meaningfully more efficient than close-to-close vol),
relative strength vs Nifty, Hurst exponent (regime detection), volume
flow (MFI, CMF), and macro (USD/INR, crude). **Recommendation: add
higher-value features (fundamental-to-quant bridge: revenue/earnings
growth, ROCE, FCF yield, earnings surprise; market regime: India VIX,
market breadth; stock-specific: gap behavior, volume shock, earnings
proximity) rather than blindly adding more of the same category.**

### 2.3 ML Classification — 🟡 promising, validated with caveats
- **MLP (128→64→32, ReLU, Dropout 0.2, Adam)**: architecture was fixed
  *before* training, not tuned against test performance — verified clean.
  Single-seed result was 65.50%; **5-seed average is 62.27% ± 1.53%**,
  which is the number to actually trust and report.
- **Exact-tie mystery resolved**: several models landed on identical
  accuracy because the test set is only n=229 — each single correct
  prediction moves accuracy by ~0.44 percentage points. Always report raw
  counts (e.g., 132/229) alongside percentages so this is transparent.
- **Next required step**: purged walk-forward validation with an
  **embargo period ≥ the label horizon** (1 month), since overlapping
  label windows on daily samples can leak information across a naive
  chronological split.

### 2.4 ML Regression — 🟡 reframed, not discarded
Every regression model showed negative R² (-0.06 to -1.24) alongside a
55-58% directional hit rate. **This is not a bug** — hand-verified: total
variance in monthly equity returns is small enough that even correct-sign
predictions with magnitude error can push R² negative. Institutional
practice (per this research) is to **not use regression for exact return
prediction** — use it for **ranking** (Rank IC, Spearman correlation,
top/bottom-decile spread) instead.

### 2.5 Triple Barrier Labeling — 🟢 keep, real methodological upgrade
Replaces fixed-horizon forward-return labeling with a label tied to
ATR-based profit target (+2.0×ATR), stop-loss (-1.5×ATR), and a vertical
timeout (15 days) — whichever hits first. This is more realistic than
asking "is price higher after exactly N days" and **shares logic
directly with the application's exit engine** (section 7).

### 2.6 Meta-Labeling — 🟢 promising, needs full metric suite
A secondary classifier filters the primary model's candidate trades
(151 raw → 62 filtered, win rate 40.40% → 46.77%). **Still needed before
trusting this**: Net P&L, CAGR, Sharpe, Sortino, max drawdown, profit
factor, expectancy, turnover — compared against Primary-only and
Buy&Hold, not just win rate in isolation.

### 2.7 Mean-Reversion Strategy — 🟢 validated at scale, with real costs
- **Universe was fixed a priori** (20 stocks, 8 sectors, sectoral
  bellwethers) — confirmed no post-hoc cherry-picking.
- **399 trades, 66.67% win rate, 1.26x gross profit factor.**
- **JSW Steel's +98.97% audited and confirmed real** — no split/bonus
  adjustment bug; it reflects a genuine multi-year commodities-supercycle
  tailwind (22 wins averaging ~+2.76%/trade, compounded over 26 trades),
  not a data error.
- **Realistic transaction costs applied (35 bps round-trip: STT, NSE/SEBI
  charges, GST, brokerage, slippage)**: profit factor drops from 1.26x
  gross to **1.06x net** — and at a stress-tested 50bps, it's roughly
  breakeven (0.98x). **This is the single most important finding in the
  whole research phase**: naive, un-gated dip-buying barely survives
  realistic frictions, which is exactly why meta-labeling and the
  decision-scoring engine (section 8) are mandatory, not optional —
  pruning low-confidence trades is what's expected to lift net profit
  factor back toward 1.6x+.
- **Statistical significance, correlation-adjusted**: trades cluster in
  time (avg 2.22 concurrent trades per 2-week window, pairwise
  correlation 0.33), giving a Kish design effect of 1.40 and an effective
  N of ~285, not 399. Cluster-adjusted p-value is still ~1.29×10⁻⁸ — the
  edge survives the correlation correction, but always report the
  adjusted N alongside the naive one.

### 2.8 Multi-Horizon Forecast Calibration — 🟢 tested, hard UI rule set
50 rolling forecast origins tested against realized prices:

| Horizon | Mean Abs Error | 80% CI Empirical Hit Rate | Verdict |
|---|---|---|---|
| 10 Days | 3.07% | 80.0% | ✅ Calibrated |
| 1 Month | 4.13% | 82.0% | ✅ Calibrated |
| 6 Months | 19.80% | 52.0% | ⚠️ Miscalibrated |
| 1 Year | 30.24% | 48.0% | ⚠️ Miscalibrated |

**Hard product rule**: any horizon whose empirical 80% CI hit rate falls
outside 70-90% is **prohibited from displaying a statistical confidence
band**. Short horizons (≤1 month) show a trajectory line with a real
volatility envelope. Long horizons (6M/1Y) show **discrete Bull/Base/Bear
scenario ranges** driven by the DCF/fundamentals engine instead of a
(false) statistical interval.

---

## 3. Application Overview — Four Pages

```
EXPLORE  →  WATCHLIST  →  BUY PAGE  →  SELL PAGE
(browse,     (curated      (full pipeline    (your holdings,
 cheap)       subset)       + verdict)        exit signals)
```

1. **Explore** — lightweight browse (market cap, sector, P/E, price) across
   many stocks. No indicators/ML here — must stay cheap to load broadly.
2. **Watchlist** — the curated subset the expensive pipeline actually runs
   against. This scoping is what keeps daily/nightly runs computationally
   realistic on personal infrastructure.
3. **Buy page** — every watchlist stock gets the full validated pipeline:
   fundamental + forensic + quant ML + ownership + market internals +
   sentiment, combined into one explainable score and verdict. Confirming
   a buy writes to the portfolio store.
4. **Sell page** — shows only portfolio-store holdings. Each gets a "time
   to leave" score from exit-engine proximity, technical reversal, model
   signal flips, and insider/ownership selling.

---

## 4. Tech Stack

| Layer | Choice | Notes |
|---|---|---|
| Language | Python | Backend + ML/data pipeline |
| Data fetch | `yfinance` | US and NSE (`.NS`/`.BO`) |
| Indicators | `pandas-ta-classic` | Actively maintained fork |
| Classical ML | `xgboost`, `scikit-learn` | Tabular indicator-based models |
| Deep learning | `torch` | LSTM/MLP/BiLSTM/Transformer variants |
| Validation | Purged walk-forward + embargo | Embargo ≥ label horizon (1 month) |
| Database | **SQLite** | Single-file, zero setup |
| Scheduling | Cron job / simple script | No Airflow/Celery needed |
| Live data | Polling every N minutes | No WebSocket streaming needed |
| Agent | Claude/OpenAI API, tool-using | See section 9 |
| UI | Web app, mobile app, or local dashboard | Backend is UI-agnostic |
| Serving | One Python project, CLI entry point | No microservices |

---

## 5. Full System Architecture

```
                              YOU
                               |
                        EXPLORE PAGE
                    (market cap, sector,
                     P/E, price — cheap)
                               |
                       add to watchlist
                               v
                    +----------------------+
                    |   WATCHLIST STORE    |
                    +-----------+----------+
                                |
                    (full pipeline runs only
                     on watchlist symbols)
                                v
                    +----------------------+
                    |  TICKER RESOLVER +   |
                    |  DATA CONNECTORS     |
                    |  (US / NSE, OHLCV)   |
                    +-----------+----------+
                                |
          +---------------------+----------------------+
          |                     |                       |
          v                     v                       v
+------------------+  +--------------------+  +----------------------+
| FUNDAMENTAL      |  | QUANT FEATURE      |  | MACRO / REGIME       |
| ENGINE           |  | LIBRARY (27+)      |  | ENGINE (new)         |
| Financials, DCF, |  | momentum, vol,     |  | NIFTY trend, India   |
| ratios, Bull/    |  | rel. strength,     |  | VIX, market/sector   |
| Base/Bear        |  | Hurst, volume flow |  | breadth              |
+--------+---------+  +---------+----------+  +----------+-----------+
         |                      |                        |
         v                      |                        |
+------------------+            |                        |
| FORENSIC ENGINE  |            |                        |
| 8-point detector |            |                        |
| → 0-100 risk     |            |                        |
| score            |            |                        |
+--------+---------+            |                        |
         |                      v                        |
         |            +--------------------+              |
         |            | ML MODELS          |              |
         |            | Classification     |              |
         |            | (MLP, trees...)    |              |
         |            | Regression (rank)  |              |
         |            | Triple-Barrier     |              |
         |            | labeling           |              |
         |            +---------+----------+              |
         |                      |                          |
         |                      v                          |
         |            +--------------------+                |
         |            | META-LABEL FILTER  |                |
         |            | (secondary model:  |                |
         |            | take/reject trade) |                |
         |            +---------+----------+                |
         |                      |                            |
         +----------------------+----------------------------+
                                |
                                v
                    +----------------------+
                    |  OWNERSHIP / SMART   |
                    |  MONEY + MARKET      |
                    |  INTERNALS + NEWS    |
                    |  (context gathering) |
                    +-----------+----------+
                                |
                                v
                    +----------------------+
                    |  AI RESEARCH AGENT   |
                    |  (interprets, does   |
                    |  not invent numbers) |
                    +-----------+----------+
                                |
                                v
                    +----------------------+
                    |  RISK ENGINE         |
                    +-----------+----------+
                                |
                                v
                    +----------------------+
                    |  EXIT ENGINE         |
                    | stop-loss, target,   |
                    | trailing stop, max   |
                    | hold, model signal   |
                    +-----------+----------+
                                |
                                v
              +--------------------------------+
              |   DECISION / SCORING ENGINE     |
              |   independent 0-100 scores per  |
              |   category → weighted total →   |
              |   verdict + explainability      |
              +---------------+------------------+
                              |
              +----------------+-----------------+
              |                                  |
              v                                  v
      +---------------+                  +----------------+
      |   BUY PAGE    |                  |   SELL PAGE    |
      +-------+-------+                  +--------+-------+
              |                                   ^
       confirm a buy                              |
              v                                   |
      +---------------------------+
      |     PORTFOLIO STORE       |
      +---------------------------+
```

---

## 6. Data Models (SQLite schemas)

```sql
-- stock_data.sqlite: cached historical OHLCV
CREATE TABLE ohlcv (
    symbol TEXT NOT NULL, date TEXT NOT NULL,
    open REAL, high REAL, low REAL, close REAL, volume INTEGER,
    PRIMARY KEY (symbol, date)
);

-- fundamentals.sqlite: normalized financial statements + ratios
CREATE TABLE fundamentals (
    symbol TEXT NOT NULL, period TEXT NOT NULL,  -- e.g. 'FY2025Q4'
    revenue REAL, ebitda REAL, ebit REAL, pat REAL, eps REAL,
    total_debt REAL, cash REAL, ocf REAL, capex REAL, fcf REAL,
    roce REAL, roe REAL, fcf_conversion_pct REAL,
    debt_to_ebitda REAL, interest_coverage REAL, accrual_ratio REAL,
    PRIMARY KEY (symbol, period)
);

-- forensic_scores.sqlite: 8-point detector, weighted
CREATE TABLE forensic_scores (
    symbol TEXT NOT NULL, period TEXT NOT NULL,
    cashflow_deterioration REAL, receivable_anomaly REAL,
    debt_deterioration REAL, promoter_pledge REAL,
    working_capital REAL, contingent_liabilities REAL,
    non_operating_income REAL, fcf_deterioration REAL,
    forensic_risk_score REAL,   -- 0-100, weighted composite
    PRIMARY KEY (symbol, period)
);

-- watchlist.sqlite: from the Explore page
CREATE TABLE watchlist (
    symbol TEXT PRIMARY KEY, added_date TEXT NOT NULL
);

-- portfolio.sqlite: what you've actually bought (drives Sell page)
CREATE TABLE holdings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    symbol TEXT NOT NULL, entry_price REAL NOT NULL,
    entry_date TEXT NOT NULL, qty REAL NOT NULL,
    status TEXT DEFAULT 'open'
);

-- registry: tracks model versions and their validated backtest results
-- fields: model_version, training_date, training_data_range, features,
--         hyperparameters, embargo_days, purged_cv_results,
--         cost_adjusted_backtest_results, production_status
```

---

## 7. Backend Modules — responsibilities & status

| Module | Responsibility | Status |
|---|---|---|
| `screener/explore.py` | Lightweight market cap/sector/P&E fetch for Explore page | Not built |
| `data_fetch.py` | OHLCV fetch, US/NSE normalization | **Built** |
| `fundamentals/normalizer.py` | Ingests Income Statement/Balance Sheet/Cash Flow, computes ratios | Research complete, needs productionizing |
| `fundamentals/forensic_engine.py` | 8-point detector → weighted 0-100 forensic risk score | Research complete (as flags); needs scoring conversion |
| `fundamentals/dcf_engine.py` | 2-stage DCF + Bull/Base/Bear scenarios + sensitivity analysis | Research complete |
| `features.py` | Technical indicator engine (extend with the 27-indicator quant library) | **Built** (base set); quant library research complete, needs merging in |
| `indicators/ownership.py` | MF %, FII/DII %, superinvestor stakes, promoter pledge | Not built |
| `indicators/market_internals.py` | Daily FII/DII flow, insider trades, PCR/OI, VIX, market/sector breadth | Not built |
| `labeling/triple_barrier.py` | ATR-based profit target/stop/timeout labeling | Research complete, needs productionizing |
| `train_classical.py` | XGBoost + tree ensemble classifiers, purged walk-forward + embargo | **Built** (base); needs purged-CV upgrade |
| `train_dl.py` | LSTM/MLP/BiLSTM/Transformer classifiers | **Built** (LSTM); MLP/BiLSTM/Transformer research complete |
| `models/meta_label.py` | Secondary classifier filtering primary model's trades | Research complete, needs full metric suite (Sharpe/Sortino/drawdown/expectancy) |
| `ensemble/stacking.py` | Weighted voting → meta-model on all model outputs | Not built |
| `backtest.py` | Simulated equity curve, full exit engine (stop/target/trailing/max-hold/signal) | **Built**; needs realistic cost injection (35bps) as default |
| `regime/market_regime.py` | Bull/Sideways/Bear/High-Vol classifier gating the final signal | Not built (Phase 3.5 requirement) |
| `context/news.py`, `fundamentals.py` | News, earnings, sector/market context | Not built |
| `agent/research_agent.py` | Interprets quant signal + context; does not invent numbers | Not built |
| `risk/risk_engine.py` | Deterministic volatility/drawdown/position-size → risk tag | Not built |
| `scoring/verdict_engine.py` | Independent 0-100 scores per category → weighted total, tested not hardcoded | Not built |
| `main.py` | CLI entry point | **Built** |

---

## 8. Decision / Scoring Engine

**Do not hard-code final weights.** Per the robustness audit: first
compute independent 0-100 scores per category, then test different
weighting schemes on training data only, validated walk-forward.

**Independent category scores (0-100 each):**
Technical, Fundamental, Forensic (inverted — lower risk = higher score),
Quant ML, Valuation (DCF-based), Smart Money, Market Regime, Sentiment,
Risk.

**Forensic sub-weights (starting point, to be tested):**
Cash-flow deterioration 20%, receivable anomaly 15%, debt deterioration
15%, promoter pledge 15%, working capital 10%, contingent liabilities
10%, non-operating income 5%, FCF deterioration 10%.

**Buy-page starting weights (to be tested, not final):**
Technical 30%, Quant ML 25%, Smart Money 20%, Market Internals 15%,
Sentiment 10% — informed by, but not copied verbatim from, the research
document's proposal.

**Sell-page starting weights:**
Exit-rule proximity 35%, Technical reversal 25%, Model signal 20%,
Insider selling 10%, Sentiment shift 10%.

**Explainability is mandatory, not optional** — every verdict must show
its breakdown:
```
FINAL SCORE: 72/100        ACTION: BUY
Fundamental   +16   Technical    +13
Quant ML      +11   Valuation    +12
Smart Money    +8   Market Regime +5
Forensic       -2   Risk         -4
```
Plus an explicit "Why BUY?" bullet list and an "Invalidation" bullet list
(what would flip this thesis) — see section 9 for the full target format.

---

## 9. UI Output Format (target end-state)

This is the format every stock detail view (Buy or Sell page) should
render — probabilistic and explainable, never a fake point-price claim:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        STOCKTICS DECISION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Action:             BUY
Conviction:         78/100

Expected Return:              Probability of Gain:
10D    +1.8%                  10D   64%
1M     +3.7%                  1M    69%
6M     +8.9%                  6M    72%
1Y    +14.2%                  1Y    75%

Fundamental   82/100   Quant ML      78/100
Technical     74/100   Valuation     71/100
Smart Money   76/100   Market Regime 68/100
Forensic Risk 21/100   Overall Risk  34/100

Why BUY?
✓ Strong earnings growth        ✓ Healthy cash conversion
✓ Positive relative strength    ✓ Favorable market regime
✓ ML probability above threshold

Risks:
⚠ Valuation moderately expensive   ⚠ Earnings event approaching
⚠ Volatility elevated

Invalidation:
• Technical breakdown   • Forensic risk > threshold
• Earnings deterioration • Market regime turns bearish
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Hard rule (section 2.8):** 6M/1Y horizons never show a Gaussian/GBM
confidence band — only discrete Bull/Base/Bear scenario ranges from the
DCF engine. Only 10D/1M horizons get a statistical volatility envelope.

**Explore page**: symbol, name, price, market cap, sector, P/E, 52-week
range. No scores here.

**Sell page**: entry price, current price, unrealized P&L%, "time to
leave" score/verdict, same category-breakdown format oriented toward
exit reasoning.

---

## 10. AI Research Agent — expected tools

Does NOT invent quantitative numbers — those come from sections 7-8. Its
job is qualitative interpretation and risk narration. Tool access:
`get_forecast(symbol)`, `get_indicators(symbol)`, `get_fundamentals(symbol)`,
`get_forensic_score(symbol)`, `get_latest_news(symbol)`,
`get_sector_performance(symbol)`, `get_risk_metrics(symbol)`.

---

## 11. Phase 3.5 — Mandatory Robustness Checklist

Required before Phase 4 (decision engine) goes live:

- [x] Purged walk-forward validation methodology defined (embargo ≥ label horizon)
- [x] Realistic transaction costs applied (35bps base, 50bps stress test)
- [x] Benchmark comparison (buy & hold) included
- [x] Cross-sectional correlation adjustment (effective N, not raw trade count)
- [x] Universe pre-registration confirmed (no post-hoc stock selection)
- [x] Corporate-action/split audit on outlier results (JSW Steel confirmed clean)
- [x] Multi-horizon CI calibration tested and UI-gated accordingly
- [ ] **Full purged walk-forward + embargo actually implemented in code** (methodology defined, not yet run)
- [ ] **Survivorship bias check** — confirm delisted/merged stocks from 2018-2019 aren't silently excluded from the universe
- [ ] **Final untouched holdout for the assembled system** — reserve the most recent 6-12 months untouched by any tuning (weights, meta-label, regime filter) for one last check before Phase 4 ships
- [ ] Market regime engine (Bull/Sideways/Bear/High-Vol gate) — not yet built
- [ ] Sector-relative analysis layer — not yet built
- [ ] Earnings/event awareness layer (days-to-earnings, surprise history) — not yet built
- [ ] Meta-labeling full metric suite (Sharpe, Sortino, drawdown, expectancy, turnover vs Primary-only and Buy&Hold) — win rate alone isn't sufficient
- [ ] SHAP/permutation importance + feature stability across time periods (raw feature gain isn't enough)
- [ ] Position sizing rules (max % per stock, max concurrent positions, correlation-aware sizing) — required before this is used with real capital, not optional

---

## 12. Build Order

1. **Foundation** — indicators + XGBoost/LSTM + walk-forward backtest with exit engine. **Done.**
2. **Phase 3.5 robustness** — close the remaining checklist items in section 11. **Do this before anything below.**
3. **Fundamental + forensic engine productionization** — normalizer, ratios, 0-100 forensic score, DCF+scenarios.
4. **Quant feature merge + meta-labeling** — merge the 27-indicator library into `features.py`, productionize Triple Barrier + meta-label filter with full metrics.
5. **Market regime + sector-relative + earnings-event layers.**
6. **Context + agent** — news/fundamentals fetch, ownership/insider data (prioritize insider activity + daily FII/DII flow), research agent.
7. **Decision/scoring engine** — independent category scores, weight testing (not hardcoded), explainability output.
8. **Automation** — nightly retraining, model registry, scheduled runs.
9. **Explore, watchlist, buy/sell pages** — UI layer over everything above.

---

## 13. Setup

```bash
pip install yfinance pandas-ta-classic xgboost torch scikit-learn
python main.py AAPL
python main.py RELIANCE.NS
```

---

## 14. Constraints & Disclaimers for the Coding Agent

- Never present output as guaranteed financial advice — decision-support framing only
- Always validate against buy-and-hold and report realistic-cost-adjusted metrics, not gross
- Always use purged walk-forward validation with embargo ≥ label horizon — never a random or naive chronological split
- Never display a statistical confidence interval for horizons that failed calibration testing (6M/1Y) — scenario ranges only
- Ownership/insider filing data is lagged 45+ days — never treat as real-time
- The AI agent interprets and narrates; it does not generate its own confidence/return numbers
- Report effective/correlation-adjusted sample sizes alongside raw trade counts for any significance claim
- This is single-user personal software — no need for multi-tenant auth or production-grade scaling infrastructure
