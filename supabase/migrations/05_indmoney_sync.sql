-- ==============================================================================
-- 05_indmoney_sync.sql: INDmoney OAuth Tokens & Synced Portfolios Schema
-- ==============================================================================

-- 1. Table to store user INDmoney OAuth connection tokens & sync status
CREATE TABLE IF NOT EXISTS public.user_indmoney_tokens (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    token_type TEXT DEFAULT 'Bearer',
    expires_at TIMESTAMPTZ,
    scope TEXT DEFAULT 'portfolio:read market:read',
    is_active BOOLEAN DEFAULT TRUE,
    last_synced_at TIMESTAMPTZ,
    last_sync_status TEXT DEFAULT 'pending', -- 'success', 'error', 'pending', 'syncing'
    last_sync_error TEXT,
    sync_metadata JSONB DEFAULT '{}'::jsonb, -- e.g. net worth, asset split, stock count
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.user_indmoney_tokens ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own indmoney token" ON public.user_indmoney_tokens 
    FOR ALL USING (auth.uid() = user_id);

-- 2. Add sync-tracking columns to user_holdings
ALTER TABLE public.user_holdings 
    ADD COLUMN IF NOT EXISTS sync_source TEXT DEFAULT 'manual',       -- 'indmoney' or 'manual'
    ADD COLUMN IF NOT EXISTS external_broker TEXT,                    -- e.g. 'INDmoney', 'Zerodha', 'Groww'
    ADD COLUMN IF NOT EXISTS external_id TEXT,                        -- broker or INDmoney instrument identifier
    ADD COLUMN IF NOT EXISTS current_value NUMERIC,
    ADD COLUMN IF NOT EXISTS invested_value NUMERIC,
    ADD COLUMN IF NOT EXISTS unrealized_pnl NUMERIC,
    ADD COLUMN IF NOT EXISTS pnl_pct NUMERIC,
    ADD COLUMN IF NOT EXISTS xirr NUMERIC;

-- 3. Add sync-tracking column to user_watchlists
ALTER TABLE public.user_watchlists
    ADD COLUMN IF NOT EXISTS sync_source TEXT DEFAULT 'manual';       -- 'indmoney' or 'manual'
