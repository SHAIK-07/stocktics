# 🛡️ Phase 6: Integration, Testing & Production Hardening

> **Goal**: Validate end-to-end data flow from market ingestion to Android presentation, verify Supabase Realtime replication & RLS policies, enforce strict security and zero token leakage, and run automated performance & integrity tests.

---

## 📋 Task Checklist

- [ ] **Task 6.1: Supabase Schema & Realtime Verification**
  - [ ] Execute [`supabase/migrations/01_initial_schema.sql`](file:///g:/Projects/stocktics/supabase/migrations/01_initial_schema.sql) in Supabase.
  - [ ] Verify Row-Level Security (RLS) policies for `user_profiles`, `user_watchlists`, `user_holdings`, `user_transactions`, `user_dcf_models`, `user_scenarios`, and `user_stock_notes`.
  - [ ] Confirm Realtime CDC publication triggers on mobile/web clients upon holding/watchlist modifications.
  - [ ] Test pgvector extension and cosine similarity search over `document_embeddings`.

- [ ] **Task 6.2: End-to-End Pipeline Integration Test**
  - [ ] Run complete automated pipeline on benchmark stocks (e.g. `ZAGGLE`, `RELIANCE`, `TCS`):
    1. Fetch instrument details $\to$ Save to `stock_master`.
    2. Download 5 years of daily OHLCV $\to$ Validate bar rules $\to$ Save to `ohlcv`.
    3. Compute 20+ technical indicators $\to$ Verify numerical stability.
    4. Ingest 5 years of P&L, Balance Sheet, Cash Flow $\to$ Run 8 Forensic Red Flag checks.
    5. Generate features $\to$ Run XGBoost & LSTM inference $\to$ Run Chronos forecast.
    6. Run Walk-Forward backtest $\to$ Compute win-rates & risk metrics.
    7. Compute 0–100 Buy Score and 0–100 Time-to-Leave Score.
    8. Generate AI Research Agent narrative summary.
  - [ ] Validate that all API endpoints return valid, consistent JSON within performance budgets.

- [ ] **Task 6.3: Offline-First Sync & Network Resilience**
  - [ ] Test Android client behavior under network dropouts and airplane mode.
  - [ ] Verify Room database handles local optimistic updates and reconciles with Supabase on reconnection.
  - [ ] Verify retry logic and exponential backoff on INDstocks API 429 rate limit responses.

- [ ] **Task 6.4: Security Hardening & Production Safeguards**
  - [ ] Audit APK and Git repository to ensure zero API keys or secrets (INDstocks token, Supabase Service Role, OpenAI/Claude API keys) are bundled in the client.
  - [ ] Add CORS restrictions and rate-limiting middleware to FastAPI backend.
  - [ ] Verify walk-forward validation has zero data leakage (time-series order strictly preserved).
  - [ ] Set up automated unit test suite with `pytest` for backend and JUnit/Compose tests for Android.

---

## 🎯 Acceptance Criteria
- [ ] Complete pipeline executes autonomously from market fetch to scoring without manual interventions.
- [ ] No secrets or sensitive keys present in Android assets or client code.
- [ ] Realtime updates push from Supabase to Android in $<250$ms.
- [ ] Codebase passes all linting and test suites.
