-- ============================================================================
-- STOCKTICS MIGRATION 03: DUAL-MARKET PATTERN SCANNER SCHEMA
-- ============================================================================

-- 1. Securities Universe Table (India & US)
CREATE TABLE IF NOT EXISTS public.securities (
    id           BIGSERIAL PRIMARY KEY,
    ticker       TEXT NOT NULL,
    market       TEXT NOT NULL,        -- 'IN' | 'US'
    exchange     TEXT,                 -- 'NSE', 'BSE', 'NASDAQ', 'NYSE'
    name         TEXT,
    sector       TEXT,
    is_active    BOOLEAN DEFAULT TRUE,
    added_at     TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(ticker, market)
);
CREATE INDEX IF NOT EXISTS idx_securities_ticker_market ON public.securities(ticker, market);
CREATE INDEX IF NOT EXISTS idx_securities_is_active ON public.securities(is_active);

-- 2. Daily Prices Table
CREATE TABLE IF NOT EXISTS public.daily_prices (
    id            BIGSERIAL PRIMARY KEY,
    security_id   BIGINT REFERENCES public.securities(id) ON DELETE CASCADE,
    date          DATE NOT NULL,
    open          NUMERIC,
    high          NUMERIC,
    low           NUMERIC,
    close         NUMERIC,
    adj_close     NUMERIC,
    volume        BIGINT,
    data_quality  TEXT DEFAULT 'VALID',
    UNIQUE(security_id, date)
);
CREATE INDEX IF NOT EXISTS idx_daily_prices_sec_date ON public.daily_prices(security_id, date DESC);

-- 3. Daily Indicators Table
CREATE TABLE IF NOT EXISTS public.daily_indicators (
    id               BIGSERIAL PRIMARY KEY,
    security_id      BIGINT REFERENCES public.securities(id) ON DELETE CASCADE,
    date             DATE NOT NULL,
    atr14            NUMERIC,
    sma20            NUMERIC,
    sma50            NUMERIC,
    sma200           NUMERIC,
    ema20            NUMERIC,
    ema50            NUMERIC,
    ema200           NUMERIC,
    rsi14            NUMERIC,
    stoch_k          NUMERIC,
    stoch_d          NUMERIC,
    rel_volume       NUMERIC,
    rel_strength_1m  NUMERIC,
    rel_strength_3m  NUMERIC,
    rel_strength_6m  NUMERIC,
    rel_strength_12m NUMERIC,
    supertrend_dir   TEXT,    -- 'BULL' | 'BEAR'
    extra            JSONB,   -- volume profile POC/value area, Gann levels, moon phase angle, etc.
    UNIQUE(security_id, date)
);
CREATE INDEX IF NOT EXISTS idx_daily_indicators_sec_date ON public.daily_indicators(security_id, date DESC);

-- 4. Pattern Events Table (Every detected pattern signal)
CREATE TABLE IF NOT EXISTS public.pattern_events (
    id              BIGSERIAL PRIMARY KEY,
    security_id     BIGINT REFERENCES public.securities(id) ON DELETE CASCADE,
    date            DATE NOT NULL,       -- signal date = when signal became KNOWABLE
    pattern         TEXT NOT NULL,       -- pattern identifier
    pattern_version TEXT NOT NULL,       -- e.g. 'VCP_v1'
    pattern_score   NUMERIC,             -- 0-100
    pivot           NUMERIC,
    details         JSONB,               -- fields used to build the explainable "why" reasons list
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(security_id, date, pattern, pattern_version)
);
CREATE INDEX IF NOT EXISTS idx_pattern_events_date_pat ON public.pattern_events(date DESC, pattern);
CREATE INDEX IF NOT EXISTS idx_pattern_events_sec_date ON public.pattern_events(security_id, date DESC);

-- 5. Daily Rankings Table
CREATE TABLE IF NOT EXISTS public.daily_rankings (
    id                 BIGSERIAL PRIMARY KEY,
    security_id        BIGINT REFERENCES public.securities(id) ON DELETE CASCADE,
    date               DATE NOT NULL,
    composite_score    NUMERIC,
    trend_score        NUMERIC,
    rel_strength_score NUMERIC,
    volume_score       NUMERIC,
    structure_score    NUMERIC,
    top_pattern        TEXT,
    top_pattern_score  NUMERIC,
    UNIQUE(security_id, date)
);
CREATE INDEX IF NOT EXISTS idx_daily_rankings_date_composite ON public.daily_rankings(date DESC, composite_score DESC);

-- 6. Weekly Rankings Table (Rolling 5 trading days aggregation)
CREATE TABLE IF NOT EXISTS public.weekly_rankings (
    id                   BIGSERIAL PRIMARY KEY,
    security_id          BIGINT REFERENCES public.securities(id) ON DELETE CASCADE,
    week_ending          DATE NOT NULL,     -- most recent trading day included
    avg_composite_score  NUMERIC,
    best_composite_score NUMERIC,
    days_in_window       INT,
    UNIQUE(security_id, week_ending)
);
CREATE INDEX IF NOT EXISTS idx_weekly_rankings_week_ending ON public.weekly_rankings(week_ending DESC, avg_composite_score DESC);

-- 7. Pattern Backtests Table
CREATE TABLE IF NOT EXISTS public.pattern_backtests (
    id                BIGSERIAL PRIMARY KEY,
    pattern           TEXT NOT NULL,
    pattern_version   TEXT NOT NULL,
    market            TEXT NOT NULL,      -- 'IN' | 'US' | 'ALL'
    run_date          DATE NOT NULL,
    sample_size       INT,
    win_rate_5d       NUMERIC,
    win_rate_10d      NUMERIC,
    win_rate_20d      NUMERIC,
    avg_return_5d     NUMERIC,
    avg_return_10d    NUMERIC,
    avg_return_20d    NUMERIC,
    median_return_20d NUMERIC,
    notes             TEXT,
    UNIQUE(pattern, pattern_version, market, run_date)
);
CREATE INDEX IF NOT EXISTS idx_pattern_backtests_lookup ON public.pattern_backtests(pattern, market, run_date DESC);

-- 8. Pattern Configuration Table (33 Patterns, All Enabled)
CREATE TABLE IF NOT EXISTS public.pattern_config (
    pattern               TEXT PRIMARY KEY,
    pattern_version       TEXT NOT NULL,
    display_name          TEXT NOT NULL,       -- UI row title
    category              TEXT NOT NULL,       -- 'MARKET_STRUCTURE', 'QUANT_INDICATOR', 'PRICE_ACTION', 'ADVANCED', 'EXPERIMENTAL'
    is_enabled            BOOLEAN DEFAULT TRUE,
    confidence_tier       TEXT DEFAULT 'STANDARD',  -- 'STANDARD' | 'EXPERIMENTAL'
    include_in_composite  BOOLEAN DEFAULT TRUE,
    params                JSONB NOT NULL
);

-- Enable Read Access for Supabase Anon & Authenticated Roles
ALTER TABLE public.securities ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_prices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_indicators ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pattern_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_rankings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.weekly_rankings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pattern_backtests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pattern_config ENABLE ROW LEVEL SECURITY;

DO $$ 
BEGIN
    DROP POLICY IF EXISTS "Public Read Securities" ON public.securities;
    CREATE POLICY "Public Read Securities" ON public.securities FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public Read Daily Prices" ON public.daily_prices;
    CREATE POLICY "Public Read Daily Prices" ON public.daily_prices FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public Read Daily Indicators" ON public.daily_indicators;
    CREATE POLICY "Public Read Daily Indicators" ON public.daily_indicators FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public Read Pattern Events" ON public.pattern_events;
    CREATE POLICY "Public Read Pattern Events" ON public.pattern_events FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public Read Daily Rankings" ON public.daily_rankings;
    CREATE POLICY "Public Read Daily Rankings" ON public.daily_rankings FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public Read Weekly Rankings" ON public.weekly_rankings;
    CREATE POLICY "Public Read Weekly Rankings" ON public.weekly_rankings FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public Read Pattern Backtests" ON public.pattern_backtests;
    CREATE POLICY "Public Read Pattern Backtests" ON public.pattern_backtests FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public Read Pattern Config" ON public.pattern_config;
    CREATE POLICY "Public Read Pattern Config" ON public.pattern_config FOR SELECT USING (true);
END $$;

-- Service Role Full Access Policies
DO $$ 
BEGIN
    DROP POLICY IF EXISTS "Service Role Securities" ON public.securities;
    CREATE POLICY "Service Role Securities" ON public.securities USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');

    DROP POLICY IF EXISTS "Service Role Prices" ON public.daily_prices;
    CREATE POLICY "Service Role Prices" ON public.daily_prices USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');

    DROP POLICY IF EXISTS "Service Role Indicators" ON public.daily_indicators;
    CREATE POLICY "Service Role Indicators" ON public.daily_indicators USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');

    DROP POLICY IF EXISTS "Service Role Events" ON public.pattern_events;
    CREATE POLICY "Service Role Events" ON public.pattern_events USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');

    DROP POLICY IF EXISTS "Service Role Daily Rankings" ON public.daily_rankings;
    CREATE POLICY "Service Role Daily Rankings" ON public.daily_rankings USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');

    DROP POLICY IF EXISTS "Service Role Weekly Rankings" ON public.weekly_rankings;
    CREATE POLICY "Service Role Weekly Rankings" ON public.weekly_rankings USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');

    DROP POLICY IF EXISTS "Service Role Backtests" ON public.pattern_backtests;
    CREATE POLICY "Service Role Backtests" ON public.pattern_backtests USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');

    DROP POLICY IF EXISTS "Service Role Config" ON public.pattern_config;
    CREATE POLICY "Service Role Config" ON public.pattern_config USING (auth.jwt() ->> 'role' = 'service_role' OR auth.role() = 'service_role');
END $$;

-- ============================================================================
-- SEED DATA: PATTERN CONFIGURATION (ALL 33 PATTERNS)
-- ============================================================================
INSERT INTO public.pattern_config (pattern, pattern_version, display_name, category, is_enabled, confidence_tier, include_in_composite, params)
VALUES
-- Phase 1 — Market structure
('SUPPORT_RESISTANCE_TOUCH', 'SUPPORT_RESISTANCE_v1', 'Support / Resistance Reaction', 'MARKET_STRUCTURE', true, 'STANDARD', true, '{"tolerance_pct": 0.015, "min_touches": 2}'),
('TRENDLINE_BREAK', 'TRENDLINE_BREAK_v1', 'Trendline Breakout', 'MARKET_STRUCTURE', true, 'STANDARD', true, '{"lookback_bars": 60, "min_pivots": 3}'),
('BREAKOUT', 'BREAKOUT_v1', 'Pivot & Resistance Breakout', 'MARKET_STRUCTURE', true, 'STANDARD', true, '{"lookback_high": 20, "volume_multiplier": 1.5}'),
('FAILED_BREAKOUT', 'FAILED_BREAKOUT_v1', 'Failed Breakout / Trap', 'MARKET_STRUCTURE', true, 'STANDARD', true, '{"exhaustion_bars": 3}'),
('MARKET_STRUCTURE_BOS', 'BOS_v1', 'Break of Structure (BOS)', 'MARKET_STRUCTURE', true, 'STANDARD', true, '{"swing_window": 5}'),
('MARKET_STRUCTURE_CHOCH', 'CHOCH_v1', 'Change of Character (CHoCH)', 'MARKET_STRUCTURE', true, 'STANDARD', true, '{"swing_window": 5}'),

-- Phase 2 — Core quantitative indicators as signals
('MA_CROSSOVER', 'MA_CROSS_v1', 'Moving Average Golden Cross', 'QUANT_INDICATOR', true, 'STANDARD', true, '{"fast_ma": 50, "slow_ma": 200}'),
('RSI_EXTREME', 'RSI_EXTREME_v1', 'RSI Oversold / Extreme Rebound', 'QUANT_INDICATOR', true, 'STANDARD', true, '{"oversold": 32, "overbought": 70}'),
('STOCHASTIC_CROSS', 'STOCH_CROSS_v1', 'Stochastic Bullish Crossover', 'QUANT_INDICATOR', true, 'STANDARD', true, '{"lower_bound": 25}'),
('MOMENTUM_ROC', 'ROC_MOMENTUM_v1', 'Momentum Rate of Change (ROC)', 'QUANT_INDICATOR', true, 'STANDARD', true, '{"roc_period": 14, "roc_threshold": 4.0}'),
('RELATIVE_VOLUME_SPIKE', 'RVOL_SPIKE_v1', 'Relative Volume Spike (RVOL)', 'QUANT_INDICATOR', true, 'STANDARD', true, '{"rvol_threshold": 2.2}'),
('SUPERTREND_FLIP', 'SUPERTREND_FLIP_v1', 'Supertrend Direction Flip', 'QUANT_INDICATOR', true, 'STANDARD', true, '{"atr_period": 10, "multiplier": 3.0}'),

-- Phase 3 — Price-action / candlestick / base patterns
('BULLISH_ENGULFING', 'ENGULFING_v1', 'Bullish Engulfing Candle', 'PRICE_ACTION', true, 'STANDARD', true, '{"min_body_ratio": 1.1}'),
('BEARISH_ENGULFING', 'BEAR_ENGULFING_v1', 'Bearish Engulfing Candle', 'PRICE_ACTION', true, 'STANDARD', true, '{"min_body_ratio": 1.1}'),
('DOJI', 'DOJI_v1', 'Indecision Doji at Level', 'PRICE_ACTION', true, 'STANDARD', true, '{"body_max_pct": 0.1}'),
('HAMMER', 'HAMMER_v1', 'Hammer / Price Rejection Pin', 'PRICE_ACTION', true, 'STANDARD', true, '{"lower_shadow_mult": 2.0}'),
('SHOOTING_STAR', 'SHOOTING_STAR_v1', 'Shooting Star Reversal', 'PRICE_ACTION', true, 'STANDARD', true, '{"upper_shadow_mult": 2.0}'),
('MORNING_STAR', 'MORNING_STAR_v1', 'Morning Star 3-Bar Reversal', 'PRICE_ACTION', true, 'STANDARD', true, '{}'),
('EVENING_STAR', 'EVENING_STAR_v1', 'Evening Star 3-Bar Reversal', 'PRICE_ACTION', true, 'STANDARD', true, '{}'),
('DOUBLE_TOP', 'DOUBLE_TOP_v1', 'Double Top Pattern', 'PRICE_ACTION', true, 'STANDARD', true, '{"tolerance_pct": 0.02}'),
('DOUBLE_BOTTOM', 'DOUBLE_BOTTOM_v1', 'Double Bottom / W-Base', 'PRICE_ACTION', true, 'STANDARD', true, '{"tolerance_pct": 0.02}'),
('VCP', 'VCP_v1', 'Volatility Contraction Pattern (VCP)', 'PRICE_ACTION', true, 'STANDARD', true, '{"min_contractions": 2, "max_contractions": 4, "volume_decay": true}'),
('FLAT_BASE', 'FLAT_BASE_v1', 'Flat Base Consolidation', 'PRICE_ACTION', true, 'STANDARD', true, '{"max_depth_pct": 14.0, "min_weeks": 4}'),
('CUP_AND_HANDLE', 'CUP_HANDLE_v1', 'Cup with Handle', 'PRICE_ACTION', true, 'STANDARD', true, '{"max_handle_depth": 0.15, "min_cup_weeks": 6}'),
('IPO_BASE', 'IPO_BASE_v1', 'IPO Debut Consolidation Base', 'PRICE_ACTION', true, 'STANDARD', true, '{"max_age_days": 500}'),
('MULTI_YEAR_BASE', 'MULTI_YEAR_BASE_v1', 'Multi-Year Structural Base', 'PRICE_ACTION', true, 'STANDARD', true, '{"min_months": 18}'),
('SUPPLY_DEMAND_ZONE', 'SND_ZONE_v1', 'Institutional Demand Zone Retest', 'PRICE_ACTION', true, 'STANDARD', true, '{"zone_depth_pct": 0.03}'),
('FAIR_VALUE_GAP', 'FVG_v1', 'Fair Value Gap (FVG) Retest', 'PRICE_ACTION', true, 'STANDARD', true, '{"mitigation_check": true}'),

-- Phase 4 — Advanced
('DIVERGENCE', 'RSI_DIV_v1', 'RSI Momentum Divergence', 'ADVANCED', true, 'STANDARD', true, '{"lookback": 30}'),
('FIBONACCI_RETRACEMENT_REACTION', 'FIB_REACTION_v1', 'Fibonacci Golden Pocket (0.618)', 'ADVANCED', true, 'STANDARD', true, '{"ratios": [0.5, 0.618, 0.786]}'),
('FIBONACCI_EXTENSION_TARGET', 'FIB_EXT_v1', 'Fibonacci Extension Target', 'ADVANCED', true, 'STANDARD', true, '{"ratios": [1.272, 1.618]}'),
('HARMONIC_PATTERN', 'HARMONIC_v1', 'Harmonic Pattern (Gartley / Bat)', 'ADVANCED', true, 'STANDARD', true, '{"tolerance": 0.05, "require_confluence": true}'),
('HEIKIN_ASHI_TREND', 'HA_TREND_v1', 'Heikin-Ashi Trend Acceleration', 'ADVANCED', true, 'STANDARD', true, '{"consecutive_bars": 3}'),
('VOLUME_PROFILE_POC_REACTION', 'POC_REACTION_v1', 'Volume Profile POC Reaction', 'ADVANCED', true, 'STANDARD', true, '{"bins": 50, "tolerance_pct": 0.015}'),

-- Phase 5 — Full-library completion (EXPERIMENTAL: include_in_composite = false)
('RENKO_TREND', 'RENKO_v1', 'Renko ATR Brick Trend', 'EXPERIMENTAL', true, 'EXPERIMENTAL', false, '{"atr_mult": 0.5}'),
('GANN_ANGLE_TOUCH', 'GANN_v1', 'Gann Square / Angle Reaction', 'EXPERIMENTAL', true, 'EXPERIMENTAL', false, '{"scale_convention": "ATR_PROPORTIONAL", "angles": [1.0, 2.0, 0.5]}'),
('MOON_PHASE_CORRELATION', 'MOON_v1', 'Moon Phase Lunar Cycle', 'EXPERIMENTAL', true, 'EXPERIMENTAL', false, '{"synodic_period_days": 29.53058867}'),
('ELLIOTT_WAVE', 'ELLIOTT_v1', 'Elliott Wave 5-Wave Impulse Count', 'EXPERIMENTAL', true, 'EXPERIMENTAL', false, '{"rule1_strict": true, "rule2_strict": true, "rule3_strict": true}')
ON CONFLICT (pattern) DO UPDATE SET
    pattern_version = EXCLUDED.pattern_version,
    display_name = EXCLUDED.display_name,
    category = EXCLUDED.category,
    confidence_tier = EXCLUDED.confidence_tier,
    include_in_composite = EXCLUDED.include_in_composite,
    params = EXCLUDED.params;

-- ============================================================================
-- ============================================================================
-- SEED DATA: EXPANDED LARGE-CAP UNIVERSE (NIFTY 50 EQUITIES + S&P 100 / NASDAQ 100 GIANTS)
-- ============================================================================
INSERT INTO public.securities (ticker, market, exchange, name, sector, is_active)
VALUES
-- Indian Equities (NSE - NIFTY 50 Index Constituents)
('RELIANCE.NS', 'IN', 'NSE', 'Reliance Industries Ltd.', 'Energy & Petrochemicals', true),
('TCS.NS', 'IN', 'NSE', 'Tata Consultancy Services Ltd.', 'Information Technology', true),
('HDFCBANK.NS', 'IN', 'NSE', 'HDFC Bank Ltd.', 'Banking & Financials', true),
('INFY.NS', 'IN', 'NSE', 'Infosys Ltd.', 'Information Technology', true),
('ICICIBANK.NS', 'IN', 'NSE', 'ICICI Bank Ltd.', 'Banking & Financials', true),
('BHARTIARTL.NS', 'IN', 'NSE', 'Bharti Airtel Ltd.', 'Telecommunications', true),
('SBIN.NS', 'IN', 'NSE', 'State Bank of India', 'Banking & Financials', true),
('LICI.NS', 'IN', 'NSE', 'Life Insurance Corp. of India', 'Insurance', true),
('ITC.NS', 'IN', 'NSE', 'ITC Ltd.', 'Consumer Goods', true),
('HINDUNILVR.NS', 'IN', 'NSE', 'Hindustan Unilever Ltd.', 'Consumer Goods', true),
('LT.NS', 'IN', 'NSE', 'Larsen & Toubro Ltd.', 'Engineering & Infra', true),
('BAJFINANCE.NS', 'IN', 'NSE', 'Bajaj Finance Ltd.', 'Financial Services', true),
('HCLTECH.NS', 'IN', 'NSE', 'HCL Technologies Ltd.', 'Information Technology', true),
('MARUTI.NS', 'IN', 'NSE', 'Maruti Suzuki India Ltd.', 'Automobile', true),
('SUNPHARMA.NS', 'IN', 'NSE', 'Sun Pharmaceutical Industries', 'Pharmaceuticals', true),
('TMPV.NS', 'IN', 'NSE', 'Tata Motors Passenger Vehicles Ltd.', 'Automobile', true),
('KOTAKBANK.NS', 'IN', 'NSE', 'Kotak Mahindra Bank Ltd.', 'Banking & Financials', true),
('NTPC.NS', 'IN', 'NSE', 'NTPC Ltd.', 'Power & Utilities', true),
('ONGC.NS', 'IN', 'NSE', 'Oil and Natural Gas Corp.', 'Energy', true),
('TITAN.NS', 'IN', 'NSE', 'Titan Company Ltd.', 'Consumer Durables', true),
('ADANIENT.NS', 'IN', 'NSE', 'Adani Enterprises Ltd.', 'Metals & Energy', true),
('ADANIPORTS.NS', 'IN', 'NSE', 'Adani Ports and Special Economic Zone', 'Infrastructure', true),
('AXISBANK.NS', 'IN', 'NSE', 'Axis Bank Ltd.', 'Banking & Financials', true),
('ULTRACEMCO.NS', 'IN', 'NSE', 'UltraTech Cement Ltd.', 'Materials & Cement', true),
('POWERGRID.NS', 'IN', 'NSE', 'Power Grid Corp. of India', 'Power Transmission', true),
('TATASTEEL.NS', 'IN', 'NSE', 'Tata Steel Ltd.', 'Metals & Mining', true),
('M&M.NS', 'IN', 'NSE', 'Mahindra & Mahindra Ltd.', 'Automobile', true),
('BAJAJFINSV.NS', 'IN', 'NSE', 'Bajaj Finserv Ltd.', 'Financial Services', true),
('WIPRO.NS', 'IN', 'NSE', 'Wipro Ltd.', 'Information Technology', true),
('COALINDIA.NS', 'IN', 'NSE', 'Coal India Ltd.', 'Metals & Mining', true),
('NESTLEIND.NS', 'IN', 'NSE', 'Nestle India Ltd.', 'Consumer Goods', true),
('JSWSTEEL.NS', 'IN', 'NSE', 'JSW Steel Ltd.', 'Metals & Mining', true),
('GRASIM.NS', 'IN', 'NSE', 'Grasim Industries Ltd.', 'Materials', true),
('TECHM.NS', 'IN', 'NSE', 'Tech Mahindra Ltd.', 'Information Technology', true),
('INDUSINDBK.NS', 'IN', 'NSE', 'IndusInd Bank Ltd.', 'Banking', true),
('CIPLA.NS', 'IN', 'NSE', 'Cipla Ltd.', 'Pharmaceuticals', true),
('ASIANPAINT.NS', 'IN', 'NSE', 'Asian Paints Ltd.', 'Consumer Goods', true),
('HDFCLIFE.NS', 'IN', 'NSE', 'HDFC Life Insurance Co. Ltd.', 'Insurance', true),
('SBILIFE.NS', 'IN', 'NSE', 'SBI Life Insurance Co. Ltd.', 'Insurance', true),
('DRREDDY.NS', 'IN', 'NSE', 'Dr. Reddy''s Laboratories Ltd.', 'Pharmaceuticals', true),
('DIVISLAB.NS', 'IN', 'NSE', 'Divi''s Laboratories Ltd.', 'Pharmaceuticals', true),
('EICHERMOT.NS', 'IN', 'NSE', 'Eicher Motors Ltd.', 'Automobile', true),
('HINDALCO.NS', 'IN', 'NSE', 'Hindalco Industries Ltd.', 'Metals & Mining', true),
('BPCL.NS', 'IN', 'NSE', 'Bharat Petroleum Corp. Ltd.', 'Energy & Refining', true),
('APOLLOHOSP.NS', 'IN', 'NSE', 'Apollo Hospitals Enterprise Ltd.', 'Healthcare', true),
('BRITANNIA.NS', 'IN', 'NSE', 'Britannia Industries Ltd.', 'Consumer Goods', true),
('HEROMOTOCO.NS', 'IN', 'NSE', 'Hero MotoCorp Ltd.', 'Automobile', true),
('SHRIRAMFIN.NS', 'IN', 'NSE', 'Shriram Finance Ltd.', 'Financial Services', true),
('BEL.NS', 'IN', 'NSE', 'Bharat Electronics Ltd.', 'Defence & Aerospace', true),
('TRENT.NS', 'IN', 'NSE', 'Trent Ltd.', 'Retail & Consumer', true),
('TATACONSUM.NS', 'IN', 'NSE', 'Tata Consumer Products Ltd.', 'Consumer Goods', true),

-- US Equities (NASDAQ / NYSE - S&P 100 & Mega-Cap Leaders)
('AAPL', 'US', 'NASDAQ', 'Apple Inc.', 'Technology', true),
('MSFT', 'US', 'NASDAQ', 'Microsoft Corporation', 'Technology', true),
('NVDA', 'US', 'NASDAQ', 'NVIDIA Corporation', 'Semiconductors', true),
('AMZN', 'US', 'NASDAQ', 'Amazon.com Inc.', 'Consumer Discretionary', true),
('GOOGL', 'US', 'NASDAQ', 'Alphabet Inc.', 'Communication Services', true),
('META', 'US', 'NASDAQ', 'Meta Platforms Inc.', 'Communication Services', true),
('TSLA', 'US', 'NASDAQ', 'Tesla Inc.', 'Automotive & Energy', true),
('BRK-B', 'US', 'NYSE', 'Berkshire Hathaway Inc.', 'Financials', true),
('AVGO', 'US', 'NASDAQ', 'Broadcom Inc.', 'Semiconductors', true),
('JPM', 'US', 'NYSE', 'JPMorgan Chase & Co.', 'Financials', true),
('LLY', 'US', 'NYSE', 'Eli Lilly and Company', 'Healthcare', true),
('V', 'US', 'NYSE', 'Visa Inc.', 'Financials', true),
('UNH', 'US', 'NYSE', 'UnitedHealth Group Inc.', 'Healthcare', true),
('MA', 'US', 'NYSE', 'Mastercard Incorporated', 'Financials', true),
('XOM', 'US', 'NYSE', 'Exxon Mobil Corporation', 'Energy', true),
('COST', 'US', 'NASDAQ', 'Costco Wholesale Corp.', 'Consumer Staples', true),
('HD', 'US', 'NYSE', 'The Home Depot Inc.', 'Consumer Discretionary', true),
('PG', 'US', 'NYSE', 'Procter & Gamble Company', 'Consumer Staples', true),
('JNJ', 'US', 'NYSE', 'Johnson & Johnson', 'Healthcare', true),
('WMT', 'US', 'NYSE', 'Walmart Inc.', 'Consumer Staples', true),
('BAC', 'US', 'NYSE', 'Bank of America Corp.', 'Financials', true),
('ORCL', 'US', 'NYSE', 'Oracle Corporation', 'Technology', true),
('ABBV', 'US', 'NYSE', 'AbbVie Inc.', 'Healthcare', true),
('CRM', 'US', 'NYSE', 'Salesforce Inc.', 'Technology', true),
('NFLX', 'US', 'NASDAQ', 'Netflix Inc.', 'Communication Services', true),
('AMD', 'US', 'NASDAQ', 'Advanced Micro Devices', 'Semiconductors', true),
('QCOM', 'US', 'NASDAQ', 'QUALCOMM Incorporated', 'Semiconductors', true),
('KO', 'US', 'NYSE', 'The Coca-Cola Company', 'Consumer Staples', true),
('PEP', 'US', 'NASDAQ', 'PepsiCo Inc.', 'Consumer Staples', true),
('LIN', 'US', 'NYSE', 'Linde plc', 'Materials', true),
('ADBE', 'US', 'NASDAQ', 'Adobe Inc.', 'Technology', true),
('DIS', 'US', 'NYSE', 'The Walt Disney Company', 'Communication Services', true),
('INTC', 'US', 'NASDAQ', 'Intel Corporation', 'Semiconductors', true),
('TXN', 'US', 'NASDAQ', 'Texas Instruments Inc.', 'Semiconductors', true),
('PLTR', 'US', 'NYSE', 'Palantir Technologies Inc.', 'Technology', true),
('CSCO', 'US', 'NASDAQ', 'Cisco Systems Inc.', 'Technology', true),
('MCD', 'US', 'NYSE', 'McDonald''s Corporation', 'Consumer Discretionary', true),
('NKE', 'US', 'NYSE', 'NIKE Inc.', 'Consumer Discretionary', true),
('PFE', 'US', 'NYSE', 'Pfizer Inc.', 'Healthcare', true),
('CVX', 'US', 'NYSE', 'Chevron Corporation', 'Energy', true),
('MRK', 'US', 'NYSE', 'Merck & Co. Inc.', 'Healthcare', true),
('WFC', 'US', 'NYSE', 'Wells Fargo & Company', 'Financials', true),
('IBM', 'US', 'NYSE', 'International Business Machines', 'Technology', true),
('MS', 'US', 'NYSE', 'Morgan Stanley', 'Financials', true),
('GS', 'US', 'NYSE', 'The Goldman Sachs Group Inc.', 'Financials', true),
('UBER', 'US', 'NYSE', 'Uber Technologies Inc.', 'Technology', true),
('GE', 'US', 'NYSE', 'GE Aerospace', 'Industrials', true),
('BA', 'US', 'NYSE', 'The Boeing Company', 'Aerospace & Defense', true),
('CAT', 'US', 'NYSE', 'Caterpillar Inc.', 'Industrials', true),
('AMAT', 'US', 'NASDAQ', 'Applied Materials Inc.', 'Semiconductors', true),
('NOW', 'US', 'NYSE', 'ServiceNow Inc.', 'Technology', true),
('INTU', 'US', 'NASDAQ', 'Intuit Inc.', 'Technology', true),
('BKNG', 'US', 'NASDAQ', 'Booking Holdings Inc.', 'Consumer Discretionary', true),
('ISRG', 'US', 'NASDAQ', 'Intuitive Surgical Inc.', 'Healthcare', true),
('VRTX', 'US', 'NASDAQ', 'Vertex Pharmaceuticals', 'Healthcare', true),
('SPGI', 'US', 'NYSE', 'S&P Global Inc.', 'Financials', true),
('ACN', 'US', 'NYSE', 'Accenture plc', 'Information Technology', true),
('BLK', 'US', 'NYSE', 'BlackRock Inc.', 'Financials', true),
('PANW', 'US', 'NASDAQ', 'Palo Alto Networks Inc.', 'Cybersecurity', true),
('MU', 'US', 'NASDAQ', 'Micron Technology Inc.', 'Semiconductors', true)
ON CONFLICT (ticker, market) DO UPDATE SET
    name = EXCLUDED.name,
    sector = EXCLUDED.sector,
    exchange = EXCLUDED.exchange,
    is_active = EXCLUDED.is_active;
