# 📋 Stocktics — Master Task Breakdown & Roadmap

Welcome to the **Stocktics Implementation Task Suite**. This directory contains the exhaustive, modular, and phased breakdown of all tasks required to build the Stocktics Personal Stock Research, Forecasting & Decision System as specified in [`STOCKTICS_UNIFIED_MASTER_SPEC.md`](../STOCKTICS_UNIFIED_MASTER_SPEC.md).

---

## 📊 Phase Progress Summary

| Phase | Description | Status | Total Tasks | Completed |
|---|---|:---:|:---:|:---:|
| **[Phase 1](./phase_1_data_foundation.md)** | Data Foundation, Ingestion & Market Infrastructure | 🟡 In Progress | 6 | 2 |
| **[Phase 2](./phase_2_fundamental_engine.md)** | Fundamental Analysis, Forensics & Financial Engines | ⚪ Pending | 7 | 0 |
| **[Phase 3](./phase_3_quant_ml_pipeline.md)** | Quantitative ML Models & Walk-Forward Backtest | ⚪ Pending | 6 | 0 |
| **[Phase 4](./phase_4_decision_scoring_agent.md)** | Decision, Risk, Exit Engines & AI Research Agent | ⚪ Pending | 6 | 0 |
| **[Phase 5](./phase_5_android_presentation.md)** | Android Native Presentation Client (Jetpack Compose) | ⚪ Pending | 9 | 1 |
| **[Phase 6](./phase_6_integration_and_hardening.md)** | End-to-End Integration, Testing & Hardening | ⚪ Pending | 4 | 0 |

---

## 🗂️ Task Directory Structure

```text
tasks/
├── README.md                             # Master index & progress dashboard (this file)
├── android_auth_and_navigation.md        # 📱 Feature Task: Auth (Login/Signup) & Floating Pill Navigation
├── phase_1_data_foundation.md            # Phase 1: Ingestion, INDstocks, OHLCV, Indicators & Base API
├── phase_2_fundamental_engine.md         # Phase 2: Statements, Ratios, Forensics, DCF & Screener
├── phase_3_quant_ml_pipeline.md          # Phase 3: XGBoost, LSTM, Chronos & Walk-Forward Backtester
├── phase_4_decision_scoring_agent.md     # Phase 4: Buy/Sell Verdicts, Risk Matrix & AI Analyst Agent
├── phase_5_android_presentation.md       # Phase 5: Android Compose UI, 23 Research Tabs & Realtime
└── phase_6_integration_and_hardening.md # Phase 6: E2E Pipelines, DB Migrations & Production Hardening
```

---

## 🧭 Architecture Reference

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
| * pgvector (Semantic search across Earnings Transcripts & Filings)                  |
+------------------------------------------+------------------------------------------+
                                           | Database Connection (asyncpg / SQLModel)
                                           v
+-------------------------------------------------------------------------------------+
|                        PYTHON ANALYTICS & ML SERVICE ENGINE                         |
|                   FastAPI + Background Workers (Heavy Compute)                      |
+------------------------------------------+------------------------------------------+
          +--------------------------------+---------------------------------+
          |                                |                                 |
          v                                v                                 v
+------------------+             +--------------------+            +--------------------+
| 1. DATA PIPELINE |             | 2. RESEARCH ENGINES|            | 3. QUANT ML ENGINE |
+------------------+             +--------------------+            +--------------------+
| * INDstocks API  |             | * Indicator Engine |            | * XGBoost & LSTM   |
| * yfinance       |             | * Forensic Checks  |            | * Chronos Pretrain |
| * Scrapers & News|             | * DCF & Multiples  |            | * Walk-Forward     |
| * Filings / PDFs |             | * Risk & Exit Rules|            |   Backtester       |
+------------------+             +--------------------+            +--------------------+
```

---

## 🎯 Core Operating Rules
1. **Manual Research First**: Always provide raw financial facts, statements, forensic checks, and valuation models.
2. **Transparent Quant Rigor**: Walk-forward backtested ML models compute numbers with zero lookahead bias.
3. **No LLM Hallucinations**: AI acts as a structured interpreter/synthesizer of computed data; never fabricates financial metrics or forecast prices.
4. **Security**: Keep all API keys (INDstocks, OpenAI, Supabase Service Role) strictly on the backend. Never expose to client APK or Git.
