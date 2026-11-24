-- ============================================================================
-- Stocktics Migration 02: Automatic User Profile & Defaults Trigger
-- ============================================================================

-- Function to handle new user registration from Supabase Auth
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. Create User Profile
    INSERT INTO public.user_profiles (
        user_id,
        display_name,
        currency,
        theme,
        risk_profile,
        default_stop_loss_pct,
        default_target_pct,
        default_trailing_stop_pct,
        notifications_enabled
    )
    VALUES (
        NEW.id,
        split_part(NEW.email, '@', 1),
        'INR',
        'dark',
        'moderate',
        0.05,
        0.15,
        0.04,
        TRUE
    )
    ON CONFLICT (user_id) DO UPDATE
    SET display_name = EXCLUDED.display_name;

    -- 2. Create Default Scoring Weights (Buy Engine & Sell Engine)
    INSERT INTO public.user_scoring_weights (
        user_id,
        engine_type,
        technicals_weight,
        models_weight,
        ownership_weight,
        internals_weight,
        sentiment_weight
    )
    VALUES
        (NEW.id, 'buy', 0.30, 0.25, 0.20, 0.15, 0.10),
        (NEW.id, 'sell', 0.25, 0.20, 0.10, 0.35, 0.10)
    ON CONFLICT (user_id, engine_type) DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger on auth.users table
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
