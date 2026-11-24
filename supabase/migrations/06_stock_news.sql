-- ==============================================================================
-- 06_stock_news.sql: INDmoney MCP Synced Stock News & Sentiment Analysis
-- ==============================================================================

CREATE TABLE IF NOT EXISTS public.stock_news (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    symbol TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    sentiment TEXT NOT NULL CHECK (sentiment IN ('positive', 'neutral', 'negative')),
    news_date DATE NOT NULL,
    publisher TEXT DEFAULT 'Market News',
    url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT stock_news_unique_article UNIQUE (symbol, title, news_date)
);

CREATE INDEX IF NOT EXISTS idx_stock_news_sym_date ON public.stock_news (symbol, news_date DESC);
CREATE INDEX IF NOT EXISTS idx_stock_news_sentiment ON public.stock_news (sentiment);

ALTER TABLE public.stock_news ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read access for stock news" ON public.stock_news
    FOR SELECT USING (true);
