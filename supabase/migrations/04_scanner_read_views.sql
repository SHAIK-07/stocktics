-- ============================================================================
-- STOCKTICS MIGRATION 04: READ VIEWS FOR PATTERN SCANNER API
-- ============================================================================
-- These views mirror the JOIN queries used by the FastAPI read endpoints,
-- enabling PostgREST to serve them as flat API responses.
-- ============================================================================

-- View 1: Pattern events enriched with security info
-- Used by: get_categories_overview(), get_pattern_matches_paginated()
CREATE OR REPLACE VIEW public.v_pattern_events_enriched AS
SELECT
    pe.id AS event_id,
    pe.security_id,
    pe.date,
    pe.pattern,
    pe.pattern_version,
    pe.pattern_score,
    pe.pivot,
    pe.details,
    s.ticker,
    s.name,
    s.market,
    s.exchange
FROM public.pattern_events pe
JOIN public.securities s ON pe.security_id = s.id;

-- View 2: Weekly rankings with security info and daily ranking details
-- Used by: get_top_week()
CREATE OR REPLACE VIEW public.v_weekly_rankings_full AS
SELECT
    wr.id AS ranking_id,
    wr.security_id,
    wr.week_ending,
    wr.avg_composite_score,
    wr.best_composite_score,
    wr.days_in_window,
    s.ticker,
    s.name,
    s.market,
    s.exchange,
    dr.top_pattern,
    dr.top_pattern_score,
    dr.trend_score,
    dr.rel_strength_score,
    dr.structure_score,
    dr.volume_score
FROM public.weekly_rankings wr
JOIN public.securities s ON wr.security_id = s.id
LEFT JOIN public.daily_rankings dr ON dr.security_id = s.id AND dr.date = wr.week_ending;

-- View 3: Stock patterns with config metadata
-- Used by: get_stock_patterns()
CREATE OR REPLACE VIEW public.v_stock_patterns_full AS
SELECT
    pe.id AS event_id,
    pe.security_id,
    pe.date,
    pe.pattern,
    pe.pattern_version,
    pe.pattern_score,
    pe.pivot,
    pe.details,
    s.ticker,
    s.name,
    s.market,
    s.exchange,
    pc.display_name,
    pc.confidence_tier,
    pc.category
FROM public.pattern_events pe
JOIN public.securities s ON pe.security_id = s.id
LEFT JOIN public.pattern_config pc ON pe.pattern = pc.pattern;

-- Grant read access to views for all roles
GRANT SELECT ON public.v_pattern_events_enriched TO anon, authenticated, service_role;
GRANT SELECT ON public.v_weekly_rankings_full TO anon, authenticated, service_role;
GRANT SELECT ON public.v_stock_patterns_full TO anon, authenticated, service_role;
