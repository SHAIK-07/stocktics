# Stocktics — Dual-Market Pattern Scanner
## Build Spec (v0.3 — full pattern library + Netflix-style browse UI)

**Goal:** Add a new "Recommended Setups" feature to Stocktics that scans a small, fixed universe of Indian (NSE/BSE) and US (NYSE/NASDAQ) stocks daily, detects the full technical-pattern library below (every pattern enabled), backtests each pattern's historical performance, and presents results as browsable category rows — like a Netflix homepage, one row per pattern — instead of requiring users to search one ticker at a time.

**Existing stack (do not change):**
- Frontend: Cloudflare Workers, at https://stocktics.stocktics7.workers.dev/
- Backend: FastAPI, deployed on Render
- DB: Supabase (Postgres)
- Market data: free/unofficial source (yfinance-style)

**Principle:** Every screen result must be explainable. Never output "BUY" — output "this stock matches pattern X, here's why, here's the historical evidence." No live/real-time computation on page load — everything the frontend reads is precomputed by a daily batch job.

**Scope trade-off (explicit, per product decision):** universe stays small at first (~50–100 stocks). Pattern coverage is the full library below — **all patterns enabled, none disabled by default**, including the experimental ones (Gann, Moon-phase, Elliott Wave). See Section 6 for how experimental patterns are still kept out of the *ranking math* while remaining fully visible to users.

---

## 1. High-Level Architecture

```
Render Cron Job (daily, after both markets close)
        │
        ▼
  scanner/pipeline.py
   1. Fetch OHLCV for universe (yfinance)
   2. Clean & validate data
   3. Compute indicator layer (Section 4)
   4. Run EVERY pattern detector (Section 5, all enabled) → score each
   5. Compute composite rank score + weekly rolling aggregation
   6. Write results to Supabase
        │
        ▼
   Supabase (Postgres)
        │
        ▼
  FastAPI endpoints (existing Render service) — Section 8
        │
        ▼
  Cloudflare Workers frontend
   Netflix-style "Recommended" home: one row per pattern (Section 12)
```

Two separate Render pieces:
- **Existing web service** (FastAPI) — add the new read endpoints here.
- **New Render Cron Job** — runs `scanner/pipeline.py` once a day. Not always-on; runs, writes to Supabase, exits.

---

## 2. Universe (v0.3 — small on purpose)

- **India:** ~30–50 liquid Nifty 200 names to start
- **US:** ~30–50 liquid S&P 500 names to start

Store as a `universe` table (ticker, market, exchange, is_active) rather than hardcoded in the pipeline, so it's editable without a code change. Widen only after Section 13's build order is fully validated.

---

## 3. Database Schema (Supabase / Postgres)

```sql
create table securities (
    id           bigserial primary key,
    ticker       text not null,
    market       text not null,        -- 'IN' | 'US'
    exchange     text,
    name         text,
    sector       text,
    is_active    boolean default true,
    added_at     timestamptz default now(),
    unique(ticker, market)
);

create table daily_prices (
    id            bigserial primary key,
    security_id   bigint references securities(id),
    date          date not null,
    open          numeric,
    high          numeric,
    low           numeric,
    close         numeric,
    adj_close     numeric,
    volume        bigint,
    data_quality  text default 'VALID',
    unique(security_id, date)
);

create table daily_indicators (
    id            bigserial primary key,
    security_id   bigint references securities(id),
    date          date not null,
    atr14         numeric,
    sma20         numeric,
    sma50         numeric,
    sma200        numeric,
    ema20         numeric,
    ema50         numeric,
    ema200        numeric,
    rsi14         numeric,
    stoch_k       numeric,
    stoch_d       numeric,
    rel_volume    numeric,
    rel_strength_1m  numeric,
    rel_strength_3m  numeric,
    rel_strength_6m  numeric,
    rel_strength_12m numeric,
    supertrend_dir   text,    -- 'BULL' | 'BEAR'
    extra         jsonb,      -- volume profile POC/value area, Gann levels, moon phase angle, etc.
    unique(security_id, date)
);

-- One row per (security, date, pattern) detection. 'pattern' is free text so
-- new detectors never require a schema change.
create table pattern_events (
    id             bigserial primary key,
    security_id    bigint references securities(id),
    date           date not null,       -- signal date = when the signal became KNOWABLE (see look-ahead rule, Sec 7)
    pattern        text not null,       -- see Section 5 for the full enum of values used
    pattern_version text not null,      -- e.g. 'VCP_v1' — see Section 11, versioning
    pattern_score  numeric,             -- 0-100
    pivot          numeric,
    details        jsonb,               -- pattern-specific fields, used to build the "why" reasons list
    created_at     timestamptz default now()
);

create table daily_rankings (
    id             bigserial primary key,
    security_id    bigint references securities(id),
    date           date not null,
    composite_score numeric,
    trend_score     numeric,
    rel_strength_score numeric,
    volume_score    numeric,
    structure_score numeric,
    top_pattern     text,
    top_pattern_score numeric,
    unique(security_id, date)
);

-- Rolling weekly aggregation, recomputed daily over the trailing 5 trading days.
-- Powers the "Top 10 This Week" row.
create table weekly_rankings (
    id                  bigserial primary key,
    security_id         bigint references securities(id),
    week_ending         date not null,     -- most recent trading day included
    avg_composite_score numeric,
    best_composite_score numeric,
    days_in_window      int,
    unique(security_id, week_ending)
);

create table pattern_backtests (
    id              bigserial primary key,
    pattern         text not null,
    pattern_version text not null,
    market          text not null,      -- 'IN' | 'US' | 'ALL'
    run_date        date not null,
    sample_size     int,
    win_rate_5d     numeric,
    win_rate_10d    numeric,
    win_rate_20d    numeric,
    avg_return_5d   numeric,
    avg_return_10d  numeric,
    avg_return_20d  numeric,
    median_return_20d numeric,
    notes           text
);

-- Every pattern is enabled by default. confidence_tier is purely a UI/backtest
-- transparency label — it does NOT disable anything.
create table pattern_config (
    pattern          text primary key,
    pattern_version  text not null,
    display_name     text not null,       -- shown as the row title in the UI, e.g. "Volatility Contraction (VCP)"
    is_enabled       boolean default true,
    confidence_tier  text default 'STANDARD',  -- 'STANDARD' | 'EXPERIMENTAL'
    include_in_composite boolean default true, -- see Section 6
    params           jsonb not null        -- e.g. {"k": 5, "epsilon": 0.02, "atr_mult": 0.25}
);
```

Indexes: `(security_id, date)` on `daily_prices` and `daily_indicators`; `(date, pattern)` and `(security_id, date)` on `pattern_events`; `(week_ending)` on `weekly_rankings`.

---

## 4. Indicator Layer (compute for every security, every trading day)

| Indicator | Formula |
|---|---|
| True Range | `max(H-L, |H-C_prev|, |L-C_prev|)` |
| ATR(14) | Wilder smoothing: `((n-1)*ATR_prev + TR) / n` |
| SMA(n) | mean of last n closes |
| EMA(n) | `alpha*C + (1-alpha)*EMA_prev`, `alpha = 2/(n+1)` — compute 20/50/200 |
| RSI(14) | Wilder RSI: `100 - 100/(1+RS)`, `RS = AvgGain/AvgLoss` |
| Stochastic %K/%D | `%K = 100*(C - LowestLow_n)/(HighestHigh_n - LowestLow_n)`, `%D = SMA_m(%K)` |
| Relative Volume | `V_t / SMA20(V_t)` |
| Relative Strength | stock trailing return − benchmark trailing return, at 1m/3m/6m/12m (benchmark = Nifty 50 for IN, S&P 500 for US) |
| Supertrend | ATR-band state machine per Section 5.17; store direction only (BULL/BEAR) |
| Swing high/low (window k) | `H_t > max(H_{t-k..t-1,t+1..t+k})` — **signal only knowable at `t+k`**, store accordingly |
| Volume Profile (daily approximation) | bin the day's range `[L,H]`, assign volume proportionally, accumulate over the lookback window; `POC = argmax bin`; store in `extra` |
| Moon phase angle | pure astronomical calculation, `theta in [0,360)`, stored in `extra` — no market data needed for this one |

**VWAP note:** true VWAP needs intraday bars. v0.3 is daily-only, so implement an *anchored VWAP approximation* using daily typical price `(H+L+C)/3` anchored from a chosen swing point, clearly labeled as an approximation. Full intraday VWAP is a later-phase item if intraday data is ever added.

---

## 5. Pattern Library — full list, all enabled, phased by build difficulty

Every detector outputs a row in `pattern_events` with a `pattern_score` (0–100) and `details` jsonb explaining *why* it fired (feeds the "reasons" list and the category row). Build in the phase order below — validate each against 5-10 known historical examples before moving to the next phase — but by the time Phase 5 ships, **every one of these is live and visible to users.**

### Phase 1 — Market structure
1. **SUPPORT_RESISTANCE_TOUCH**
2. **TRENDLINE_BREAK**
3. **BREAKOUT** (+ **FAILED_BREAKOUT** as a related, separately-scored event)
4. **MARKET_STRUCTURE_BOS** (break of structure)
5. **MARKET_STRUCTURE_CHOCH** (change of character)

### Phase 2 — Core quantitative indicators as signals
6. **MA_CROSSOVER**
7. **RSI_EXTREME**
8. **STOCHASTIC_CROSS**
9. **MOMENTUM_ROC**
10. **RELATIVE_VOLUME_SPIKE**
11. **SUPERTREND_FLIP**

### Phase 3 — Price-action / candlestick / base patterns
12. **BULLISH_ENGULFING** / **BEARISH_ENGULFING**
13. **DOJI**
14. **HAMMER** / **SHOOTING_STAR**
15. **MORNING_STAR** / **EVENING_STAR**
16. **DOUBLE_TOP** / **DOUBLE_BOTTOM**
17. **VCP** (full Volatility Contraction Pattern — strictly decreasing contraction depth, prior uptrend, volume decline through the base)
18. **FLAT_BASE**
19. **CUP_AND_HANDLE**
20. **IPO_BASE**
21. **MULTI_YEAR_BASE**
22. **SUPPLY_DEMAND_ZONE**
23. **FAIR_VALUE_GAP**

### Phase 4 — Advanced
24. **DIVERGENCE** (price vs. RSI, bullish/bearish)
25. **FIBONACCI_RETRACEMENT_REACTION**
26. **FIBONACCI_EXTENSION_TARGET** (informational context field on other bullish patterns, not its own category row)
27. **HARMONIC_PATTERN** (Gartley/Bat/Butterfly-style XABCD, geometric ratio matching with tolerance) — most parameter-sensitive detector in the library; require confluence with at least one Phase 1–2 signal to keep the false-positive rate manageable
28. **HEIKIN_ASHI_TREND**
29. **VOLUME_PROFILE_POC_REACTION**

### Phase 5 — Full-library completion (all built, all enabled, `confidence_tier = 'EXPERIMENTAL'`)
30. **RENKO_TREND** — ATR-scaled brick trend (`B = k*ATR_n`)
31. **GANN_ANGLE_TOUCH** — you must fix and document your own price/time scaling convention (the toolbox doc is explicit that without one, Gann angles aren't objectively reproducible); store that convention in `pattern_config.params` so it's not a hidden assumption
32. **MOON_PHASE_CORRELATION** — astronomical phase angle vs. forward returns. Labeled `EXPERIMENTAL` in the UI (small badge on the row, see Section 12) since the source doc is explicit there's no established financial rationale — showing it is fine, just don't let it silently influence ranking (Section 6)
33. **ELLIOTT_WAVE** — 5-wave impulse + ABC correction per toolbox §4 rules (Wave 2 doesn't retrace past Wave 1 origin, Wave 3 not the shortest, Wave 4 doesn't overlap Wave 1). Labeling is inherently subjective — implement your clearest objective approximation of the three numbered rules, store the wave count/labels in `details`, and tag `EXPERIMENTAL` like Gann/Moon

---

## 6. Composite Scoring vs. "Enabled" — these are two different switches

Two independent flags per pattern in `pattern_config`, so "all enabled" doesn't quietly distort the main ranking:

- **`is_enabled`** — controls whether the detector runs at all and whether its row shows up in the UI. **All 33 patterns: `true`.**
- **`include_in_composite`** — controls whether that pattern's score feeds `composite_score` (the number used for the "Top 10 This Week" row and general sort order). Phase 5 patterns (Gann, Moon-phase, Elliott Wave) default to **`false`** here — they still run, still get their own category row, still get their own backtest — they just don't get to silently outvote a well-established Phase 1–3 signal in the one ranking that claims to be "the best stuff right now." This is a config value, not a code branch, so it's a one-line change if you decide later you want them counted.

```
composite_score =
    0.25 * trend_score          (EMA/Supertrend alignment)
  + 0.20 * rel_strength_score   (percentile rank vs universe)
  + 0.20 * structure_score      (best Phase 1 signal: BOS/CHoCH/breakout quality)
  + 0.20 * top_pattern_score    (best score among patterns with include_in_composite = true)
  + 0.15 * volume_score         (relative volume, normalized 0-100)
```
Starting weights only — configurable, revisit once backtest data (Section 7) exists per pattern.

---

## 7. Backtest Engine

Runs separately from the daily pipeline (weekly, or on-demand via an admin endpoint) — for **every** pattern, including Phase 5.

For each pattern:
1. Scan full available history for every historical `pattern_event`.
2. Forward returns at +5, +10, +20 trading days from the **signal date** (the date the signal was actually knowable).
3. Aggregate: sample size, win rate at each horizon, average/median return.
4. Write to `pattern_backtests`, one row per (pattern, pattern_version, market, run_date).
5. `notes` field must record: no survivorship-bias correction in v0.3 — surface this caveat in the UI wherever backtest stats are shown, and for Phase 5 patterns specifically also surface the `confidence_tier = EXPERIMENTAL` label next to the stats so users see the same honesty the source doc itself uses.

**Look-ahead bias rule (non-negotiable, applies to every swing-based pattern — most of Phase 1, 3, and 4):** the stored `date` in `pattern_events` is the date the signal became knowable, never the date it visually occurred.

---

## 8. FastAPI Endpoints (add to existing Render service)

```
# Existing single-stock endpoints unchanged.

GET /scanner/categories
    → one entry per enabled pattern: {pattern, display_name, confidence_tier, top5:[{ticker, name, pattern_score, pivot, reasons[]}, ...5 max]}
    → this single call powers the entire Netflix-style home screen (Section 12)

GET /scanner/pattern/{pattern}?market=&page=1&page_size=20
    → full paginated list of current matches for one pattern ("View All" destination)

GET /scanner/top-week?market=&limit=10
    → reads weekly_rankings, returns top N by avg_composite_score (or best_composite_score — pick one and document it)

GET /scanner/stock/{ticker}/patterns
    → all pattern_events for this ticker, most recent first

GET /scanner/backtest/{pattern}?market=US

GET /scanner/universe
```

Every stock object returned includes a `reasons` array built from `details` jsonb (e.g. `["Prior uptrend", "3 contractions: 18%→9%→4%", "Volume declining", "1.7% below pivot"]`) so results stay explainable, never just a bare score.

---

## 9. Render Cron Job Setup

- New Render service, type = **Cron Job**.
- Schedule: once daily, after both markets have closed.
- Command: `python scanner/pipeline.py`
- Pipeline also recomputes `weekly_rankings` each run (rolling 5-trading-day window), not just `daily_rankings`.
- Env vars: Supabase connection string, data source keys if any.
- Idempotent — safe to re-run for the same date (upsert, not insert-only).

---

## 10. Pattern Versioning

Every detector has a `pattern_version` (e.g. `VCP_v1`, `HARMONIC_GARTLEY_v1`, `MOON_PHASE_v1`). Never silently change a detector's logic under the same version string — bump the version instead, keep old rows, let `pattern_backtests` show both if comparing. `pattern_config` lets you retune parameters without a deploy; a *logic* change always gets a new version string.

---

## 11. Look-Ahead & Overfitting Discipline

Applies uniformly across all 33 patterns, including experimental ones — an EXPERIMENTAL label is about financial rationale, not about being exempt from correct backtesting methodology:
- Signal date = knowable date, always (Section 7).
- Don't tune parameters against the same data you evaluate on — reserve a holdout period per pattern once enough history exists.
- With 33 patterns × tunable parameters each, the combinatorial space is large — resist the urge to hand-tune every pattern until its backtest "looks good"; that's the overfitting trap the toolbox doc itself warns about.

---

## 12. UI Design — Netflix-style category browsing

**Home screen ("Recommended" tab):**
- A "Top 10 This Week" row at the very top — pulled from `/scanner/top-week`, always visible, not togglable.
- Below it, one horizontal-scroll row **per enabled pattern** (33 rows), each showing up to 5 stock cards: ticker, name, pattern score, tiny sparkline or key stat, and a "View All →" link at the end of the row.
- Row title = `pattern_config.display_name` (human-readable, e.g. "Volatility Contraction (VCP)" not the raw `VCP` enum).
- Rows for Phase 5 patterns (Gann, Moon-phase, Elliott Wave) carry a small "Experimental" badge next to the title — pulled from `confidence_tier`. They are not hidden, not collapsed, not de-prioritized in position — just labeled, matching how the spec treats them everywhere else (visible, backtested, just kept out of the composite ranking math per Section 6).
- Rows with zero matches today render as an empty/placeholder state ("No matches today") rather than being omitted — keeps the grid layout stable day to day.
- Market filter (India / US / Both) applies globally across all rows, not per-row.

**"View All" page (per pattern):**
- Full paginated grid/list for that one pattern, backed by `/scanner/pattern/{pattern}`.
- Same card format as the home row, plus the pattern's `pattern_backtests` summary shown once at the top of the page (win rate, avg return by horizon, sample size, and the survivorship-bias/experimental caveats from Section 7).

**Stock detail page (existing + additions):**
- New section listing every pattern currently matched by this stock, each with its score and reasons.
- No changes to the existing single-stock search flow — this is purely additive.

This is UI structure/behavior only — final visual styling is up to whoever builds the Cloudflare Workers frontend piece.

---

## 13. Build Order

1. Supabase schema (Section 3).
2. Small universe seed (Section 2, ~30-50 tickers each market).
3. Data pipeline: fetch + clean OHLCV → `daily_prices`. Verify manually.
4. Indicator layer (Section 4) → `daily_indicators`. Spot-check against known tickers.
5. **Phase 1 patterns** (1–5) → `pattern_events`. Validate each against known historical examples.
6. Composite score (Phase-1-only) → `daily_rankings` + first pass at `weekly_rankings`.
7. `/scanner/categories`, `/scanner/pattern/{pattern}`, `/scanner/top-week` endpoints.
8. Frontend Netflix-style home (Section 12) wired to step 7 — even with only 5 rows, this proves the UI pattern end-to-end before scaling to 33.
9. **Phase 2 patterns** (6–11) → validate → fold into composite score.
10. **Phase 3 patterns** (12–23) → validate each individually — biggest phase, most time here.
11. Backtest engine (Section 7) + `/scanner/backtest/{pattern}` endpoint — run as soon as Phase 1–3 have enough history.
12. **Phase 4 patterns** (24–29) → validate, note the harmonic-pattern confluence requirement.
13. **Phase 5 patterns** (30–33) → build, enable, set `confidence_tier = EXPERIMENTAL` and `include_in_composite = false`, wire the "Experimental" badge into the UI.
14. Expand universe only after all 33 patterns are validated end-to-end on the small universe.

---

## 14. Explicit Non-Goals for v0.3

- No options, futures, mutual funds.
- No true intraday VWAP/Volume-Profile (daily-approximated versions only, clearly labeled).
- No survivorship-bias-corrected historical universe (flagged as a known limitation, not solved).
- No live/real-time scanning — daily batch only.
- No personalized financial advice language anywhere in the UI — "matches pattern X" not "buy this stock."
- Experimental patterns (Gann, Moon-phase, Elliott Wave) never feed `composite_score` by default — this is a config flag, reversible, but the default stays off given the source doc's own caveats about them.
