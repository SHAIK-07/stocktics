-- ============================================================================
-- STOCKTICS INITIAL SUPABASE DATABASE MIGRATION
-- ============================================================================

-- Enable Necessary Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- ============================================================================
-- PART A: GLOBAL MARKET & RESEARCH DATA (Managed by Backend Ingestion Pipeline)
-- ============================================================================

-- 1. Security & Master Data
CREATE TABLE IF NOT EXISTS public.stock_master (
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
CREATE INDEX IF NOT EXISTS idx_stock_master_symbol ON public.stock_master(symbol);
CREATE INDEX IF NOT EXISTS idx_stock_master_sec_id ON public.stock_master(security_id);

-- 2. Normalized Historical Market Data (Partition-ready daily & weekly OHLCV)
CREATE TABLE IF NOT EXISTS public.ohlcv (
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
CREATE INDEX IF NOT EXISTS idx_ohlcv_lookup ON public.ohlcv(symbol, exchange, date DESC);

-- 3. Fundamental & Financial Data (5-10 Years of Statements)
CREATE TABLE IF NOT EXISTS public.company_financials (
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
CREATE TABLE IF NOT EXISTS public.shareholding (
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
CREATE TABLE IF NOT EXISTS public.document_embeddings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol TEXT NOT NULL,
    doc_type TEXT NOT NULL,          -- 'annual_report', 'transcript', 'filing', 'news'
    period TEXT,
    chunk_content TEXT NOT NULL,
    embedding vector(1536),          -- OpenAI text-embedding-3-small
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_doc_embeddings_vector ON public.document_embeddings USING ivfflat (embedding vector_cosine_ops);

-- 6. Model Registry & Quantitative Signal Store
CREATE TABLE IF NOT EXISTS public.model_registry (
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
CREATE TABLE IF NOT EXISTS public.user_profiles (
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
CREATE TABLE IF NOT EXISTS public.user_scoring_weights (
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
CREATE TABLE IF NOT EXISTS public.user_watchlists (
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
CREATE TABLE IF NOT EXISTS public.user_holdings (
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
CREATE TABLE IF NOT EXISTS public.user_transactions (
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
CREATE TABLE IF NOT EXISTS public.user_dcf_models (
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
CREATE TABLE IF NOT EXISTS public.user_scenarios (
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
CREATE TABLE IF NOT EXISTS public.user_stock_notes (
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

CREATE TABLE IF NOT EXISTS public.user_monitoring_items (
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
CREATE TABLE IF NOT EXISTS public.user_saved_screeners (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    screener_name TEXT NOT NULL,
    filter_criteria JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.user_saved_screeners ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own screeners" ON public.user_saved_screeners FOR ALL USING (auth.uid() = user_id);

CREATE TABLE IF NOT EXISTS public.user_alerts (
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
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_profiles') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_profiles;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_scoring_weights') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_scoring_weights;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_watchlists') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_watchlists;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_holdings') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_holdings;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_transactions') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_transactions;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_dcf_models') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_dcf_models;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_scenarios') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_scenarios;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_stock_notes') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_stock_notes;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_monitoring_items') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_monitoring_items;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'user_alerts') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.user_alerts;
    END IF;
END $$;
