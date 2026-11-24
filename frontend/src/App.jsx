import React, { useState, useEffect } from 'react';
import SearchHeader from './components/SearchHeader';
import StockBanner from './components/StockBanner';
import PriceHistoryChart from './components/charts/PriceHistoryChart';
import StockPerformanceCard from './components/StockPerformanceCard';
import HorizontalFilterBar from './components/HorizontalFilterBar';
import StoryCenterColumn from './components/StoryCenterColumn';
import RecommendedScannerView from './components/scanner/RecommendedScannerView';
import PatternDetailPage from './components/scanner/PatternDetailPage';
import PatternsCatalogPage from './components/scanner/PatternsCatalogPage';
import StockPatternInspectionModal from './components/scanner/StockPatternInspectionModal';
import StockPatternMatchesSection from './components/scanner/StockPatternMatchesSection';
import FintechLoader from './components/common/FintechLoader';

import DashboardView from './components/dashboard/DashboardView';
import BuyPipelineView from './components/buy/BuyPipelineView';
import HoldingsExitView from './components/sell/HoldingsExitView';
import LandingPageView from './components/landing/LandingPageView';
import AuthModal from './components/auth/AuthModal';
import MarketHeatmapView from './components/heatmap/MarketHeatmapView';
import StockComparisonView from './components/compare/StockComparisonView';
import LearningView from './components/learning/LearningView';
import IndmoneySyncPromptModal from './components/common/IndmoneySyncPromptModal';
import NewsSentimentModule from './components/common/NewsSentimentModule';

import { fetchStockAnalysis, getIndmoneyStatus } from './services/api';
import { authService } from './services/supabase';

import { parseCurrentUrl, navigateTo } from './utils/router';
import { AlertCircle, RefreshCw } from 'lucide-react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }
  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught error:', error, errorInfo);
  }
  render() {
    if (this.state.hasError) {
      return (
        <div className="error-card" style={{ margin: '24px 0' }}>
          <AlertCircle size={40} className="error-icon" />
          <div className="error-title">Rendering Notice for this Stock</div>
          <div className="error-msg">A metric could not be parsed for this company. You can still inspect all other chapters.</div>
          <button className="stocktics-button" onClick={() => this.setState({ hasError: false })}>
            <RefreshCw size={14} />
            <span>Retry Story</span>
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

export default function App() {
  const [user, setUser] = useState(() => authService.getCurrentUser());
  const initialRoute = parseCurrentUrl();
  const [activeTab, setActiveTab] = useState(initialRoute.tab || 'dashboard'); // 'dashboard' | 'buy' | 'sell' | 'scanner' | 'analysis' | 'pattern' | 'learning'
  const [activeLesson, setActiveLesson] = useState(initialRoute.lesson || null);
  const [ticker, setTicker] = useState(initialRoute.ticker);
  const [market, setMarket] = useState(() => {
    return initialRoute.market || localStorage.getItem('stocktics_global_market') || 'IN';
  });
  const [activePattern, setActivePattern] = useState(initialRoute.pattern || 'BREAKOUT');
  const [inspectingStock, setInspectingStock] = useState(null);
  const [stockData, setStockData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filterType, setFilterType] = useState('all');
  const [activeChapterId, setActiveChapterId] = useState('market_liquidity');

  // Modals & Navigation state
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [authModalMode, setAuthModalMode] = useState('signin');
  const [refreshKey, setRefreshKey] = useState(0);
  const [isIndmoneyPromptOpen, setIsIndmoneyPromptOpen] = useState(false);

  // One-time startup purge of legacy manual cache to ensure a 100% fresh start
  useEffect(() => {
    try {
      localStorage.removeItem('stocktics_local_holdings');
      localStorage.removeItem('stocktics_local_watchlist');
      localStorage.removeItem('stocktics_local_transactions');
    } catch {}
  }, []);

  // Sync user state with Supabase auth listener
  useEffect(() => {
    const unsub = authService.onAuthStateChange((newUser) => {
      setUser(newUser);
      if (newUser && !user) {
        setActiveTab('dashboard');
      }
    });
    return () => unsub();
  }, [user]);


  // Prompt user to sync with INDmoney if not yet connected
  useEffect(() => {
    if (!user?.userId) return;

    let isMounted = true;
    const timer = setTimeout(async () => {
      try {
        const status = await getIndmoneyStatus(user.userId);
        if (isMounted && !status.connected) {
          const lastDismissed = localStorage.getItem('stocktics_indmoney_prompt_dismissed');
          const now = Date.now();
          if (!lastDismissed || (now - parseInt(lastDismissed, 10)) > 86400000) {
            setIsIndmoneyPromptOpen(true);
          }
        }
      } catch (err) {
        console.error('Error checking INDmoney status for prompt:', err);
      }
    }, 1200);

    return () => {
      isMounted = false;
      clearTimeout(timer);
    };
  }, [user?.userId]);


  const loadStock = async (symbol) => {
    if (!symbol) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchStockAnalysis(symbol);
      setStockData(data);
      setTicker(data.ticker);
      if (data.dimensions && data.dimensions.length > 0) {
        setActiveChapterId(data.dimensions[0].id);
      }
    } catch (err) {
      console.error('Failed to load stock analysis:', err);
      setError(err.message || 'Failed to load stock data');
    } finally {
      setIsLoading(false);
    }
  };

  // Initial mount: synchronize address bar and load stock only if deep analysis was requested
  useEffect(() => {
    navigateTo(
      {
        tab: initialRoute.tab,
        ticker: initialRoute.ticker,
        market: initialRoute.market,
        pattern: initialRoute.pattern,
        lesson: initialRoute.lesson
      },
      { replace: true }
    );

    if (initialRoute.tab === 'analysis' && initialRoute.ticker) {
      loadStock(initialRoute.ticker);
    } else if (initialRoute.tab === 'chart' && initialRoute.ticker) {
      setInspectingStock({
        ticker: initialRoute.ticker,
        market: initialRoute.market,
        pattern: initialRoute.pattern
      });
    } else if (initialRoute.tab === 'learning') {
      setActiveLesson(initialRoute.lesson || null);
    }
  }, []);

  // Browser Back / Forward History Listener
  useEffect(() => {
    const handlePopState = () => {
      const route = parseCurrentUrl();
      setActiveTab(route.tab === 'chart' ? 'scanner' : route.tab);
      setMarket(route.market);

      if (route.tab === 'chart') {
        setInspectingStock({
          ticker: route.ticker,
          market: route.market,
          pattern: route.pattern
        });
      } else {
        setInspectingStock(null);
      }

      if (route.tab === 'learning') {
        setActiveLesson(route.lesson || null);
      } else if (route.tab === 'pattern') {
        setActivePattern(route.pattern || 'BREAKOUT');
      } else if (route.tab === 'analysis') {
        setTicker(route.ticker);
        if (!stockData || stockData.ticker !== route.ticker) {
          loadStock(route.ticker);
        }
      }
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, [stockData]);

  // Scroll sync: detect which chapter is currently in view for the jump bar highlight
  useEffect(() => {
    if (activeTab !== 'analysis' || !stockData?.dimensions || stockData.dimensions.length === 0) return;

    const chapterIds = stockData.dimensions.map(d => `chapter-${d.id}`);

    const handleScroll = () => {
      const scrollPos = window.scrollY + 260;
      for (let i = chapterIds.length - 1; i >= 0; i--) {
        const el = document.getElementById(chapterIds[i]);
        if (el && el.offsetTop <= scrollPos) {
          const rawId = chapterIds[i].replace('chapter-', '');
          setActiveChapterId(rawId);
          break;
        }
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll();

    return () => window.removeEventListener('scroll', handleScroll);
  }, [stockData, filterType, activeTab]);

  const handleTabChange = (newTab) => {
    if (!user) {
      setAuthModalMode('signin');
      setIsAuthModalOpen(true);
      return;
    }
    setActiveTab(newTab);
    if (newTab === 'analysis') {
      navigateTo({ tab: 'analysis', ticker });
      if (!stockData || stockData.ticker !== ticker) {
        loadStock(ticker);
      }
    } else if (newTab === 'patterns') {
      navigateTo({ tab: 'patterns' });
    } else if (newTab === 'buy') {
      navigateTo({ tab: 'buy' });
    } else if (newTab === 'sell') {
      navigateTo({ tab: 'sell' });
    } else if (newTab === 'dashboard') {
      navigateTo({ tab: 'dashboard' });
    } else if (newTab === 'heatmap') {
      navigateTo({ tab: 'heatmap', market });
    } else if (newTab === 'compare') {
      navigateTo({ tab: 'compare', market });
    } else if (newTab === 'learning') {
      navigateTo({ tab: 'learning', lesson: activeLesson });
    } else {
      navigateTo({ tab: 'scanner', market });
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSelectTicker = (newTicker) => {
    if (!user) {
      setAuthModalMode('signin');
      setIsAuthModalOpen(true);
      return;
    }
    setTicker(newTicker);
    setActiveTab('analysis');
    navigateTo({ tab: 'analysis', ticker: newTicker });
    loadStock(newTicker);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleMarketChange = (newMarket) => {
    const cleanMarket = newMarket === 'US' ? 'US' : 'IN';
    setMarket(cleanMarket);
    try {
      localStorage.setItem('stocktics_global_market', cleanMarket);
    } catch {}
    const currentRoute = parseCurrentUrl();
    navigateTo({ ...currentRoute, market: cleanMarket }, { replace: true });
    setRefreshKey((k) => k + 1);
  };

  const handleOpenPatternPage = (patternName) => {
    if (!user) {
      setAuthModalMode('signin');
      setIsAuthModalOpen(true);
      return;
    }
    const clean = (patternName || 'BREAKOUT').toLowerCase();
    setActivePattern(clean);
    setActiveTab('pattern');
    navigateTo({ tab: 'pattern', pattern: clean, market });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleBackToScanner = () => {
    if (window.history.length > 1) {
      window.history.back();
    } else {
      setActiveTab('patterns');
      navigateTo({ tab: 'patterns' });
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleInspectStock = (stock) => {
    if (!user) {
      setAuthModalMode('signin');
      setIsAuthModalOpen(true);
      return;
    }
    setInspectingStock(stock);
    navigateTo({
      tab: 'chart',
      ticker: stock.ticker,
      market: stock.market || market,
      pattern: stock.pattern
    });
  };

  const handleCloseInspection = () => {
    setInspectingStock(null);
    const currentRoute = parseCurrentUrl();
    if (currentRoute.tab === 'chart') {
      if (window.history.length > 1) {
        window.history.back();
      } else {
        navigateTo({ tab: 'scanner', market });
      }
    }
  };

  const handleGoToDeepAnalysis = (symbol) => {
    setInspectingStock(null);
    handleSelectTicker(symbol);
  };

  // Metric counts across all chapters
  let totalMetrics = 0;
  let positiveCount = 0;
  let negativeCount = 0;

  if (stockData?.dimensions) {
    stockData.dimensions.forEach((dim) => {
      dim.metrics.forEach((m) => {
        totalMetrics++;
        if (m.isPositive) positiveCount++;
        else negativeCount++;
      });
    });
  }

  return (
    <div>
      <SearchHeader
        currentTicker={ticker}
        onSelectTicker={handleSelectTicker}
        stockData={stockData}
        activeTab={user ? activeTab : 'landing'}
        onTabChange={handleTabChange}
        market={market}
        onMarketChange={handleMarketChange}
        onOpenAuth={(mode = 'signin') => {
          setAuthModalMode(mode);
          setIsAuthModalOpen(true);
        }}
      />

      <main className="app-container">
        {!user ? (
          <LandingPageView
            onOpenAuth={(mode = 'signin') => {
              setAuthModalMode(mode);
              setIsAuthModalOpen(true);
            }}
          />
        ) : (
          <>
            {/* Institutional FinTech Dashboard */}
            {activeTab === 'dashboard' && (
              <ErrorBoundary>
                <DashboardView
                  key={`dash-${market}-${refreshKey}`}
                  market={market}
                  onSelectStock={handleSelectTicker}
                  onNavigateTab={handleTabChange}
                  onOpenAuth={(mode = 'signin') => {
                    setAuthModalMode(mode);
                    setIsAuthModalOpen(true);
                  }}
                />
              </ErrorBoundary>
            )}

        {/* Quant Trading Pipeline (Buy Candidates & Holdings/Sell Signals) */}
        {(activeTab === 'buy' || activeTab === 'sell') && (
          <ErrorBoundary>
            <BuyPipelineView
              key={`pipeline-${activeTab}-${market}-${refreshKey}`}
              market={market}
              initialSubTab={activeTab === 'sell' ? 'sell' : 'buy'}
              onSelectStock={handleSelectTicker}
              onNavigateTab={handleTabChange}
            />
          </ErrorBoundary>
        )}

        {/* Dedicated Pattern Page (Deep Dive, 10-Yr Backtrack & Separated Indian / US Stocks) */}
        {activeTab === 'pattern' && (
          <ErrorBoundary>
            <PatternDetailPage
              patternName={activePattern}
              market={market}
              onBackToScanner={handleBackToScanner}
              onInspectStock={handleInspectStock}
            />
          </ErrorBoundary>
        )}

        {/* Institutional Patterns Catalog Library */}
        {activeTab === 'patterns' && (
          <ErrorBoundary>
            <PatternsCatalogPage
              onSelectPattern={(pat) => handleOpenPatternPage(pat)}
            />
          </ErrorBoundary>
        )}

        {/* Recommended Setups (Netflix-Style Pattern Scanner View) */}
        {activeTab === 'scanner' && (
          <ErrorBoundary>
            <RecommendedScannerView
              market={market}
              onMarketChange={handleMarketChange}
              onOpenPatternPage={handleOpenPatternPage}
              onOpenPatternsCatalog={() => handleTabChange('patterns')}
              onInspectStock={handleInspectStock}
            />
          </ErrorBoundary>
        )}

        {/* Market Heatmap Treemap View */}
        {activeTab === 'heatmap' && (
          <ErrorBoundary>
            <MarketHeatmapView
              market={market}
              onInspectStock={handleInspectStock}
              user={user}
            />
          </ErrorBoundary>
        )}

        {/* Multi-Stock Comparison Tool View */}
        {activeTab === 'compare' && (
          <ErrorBoundary>
            <StockComparisonView
              market={market}
              onInspectStock={handleInspectStock}
            />
          </ErrorBoundary>
        )}

        {/* Quantitative Fundamental & Trading Academy (40 Lessons) */}
        {activeTab === 'learning' && (
          <ErrorBoundary>
            <LearningView
              initialLessonNumber={activeLesson}
              user={user}
              onSelectStock={handleSelectTicker}
              onNavigateTab={handleTabChange}
            />
          </ErrorBoundary>
        )}


        {/* Deep Single-Stock Analysis View */}
        {activeTab === 'analysis' && (
          <>
            {isLoading && (
              <FintechLoader
                title={`Auditing Corporate Financial Statements for ${ticker}...`}
                subtitle="Calculating solvency, valuation scorecards, institutional technical patterns & narratives"
                steps={[
                  `Pulling audited 10-K & quarterly filings for ${ticker}`,
                  'Evaluating financial health, leverage & margins',
                  'Aggregating 33 institutional quantitative setups',
                  'Rendering dynamic chapter visuals & interactive valuation models'
                ]}
              />
            )}

            {error && !isLoading && (
              <div className="error-card">
                <AlertCircle size={44} className="error-icon" />
                <div className="error-title">Unable to Analyze Stock</div>
                <div className="error-msg">{error}</div>
                <button className="stocktics-button" onClick={() => loadStock(ticker)}>
                  <RefreshCw size={16} />
                  <span>Retry Analysis</span>
                </button>
              </div>
            )}

            {!isLoading && !error && stockData && (
              <ErrorBoundary>
                {/* Top Grid: Stock Profile Banner, Historical Price Chart & Performance Returns */}
                <div className="top-banner-price-grid">
                  <StockBanner
                    stockData={stockData}
                    onOpenAuth={() => setIsAuthModalOpen(true)}
                  />
                  <div className="price-chart-header-card">
                    <PriceHistoryChart
                      ticker={ticker}
                      currencySymbol={stockData.currencySymbol}
                      fiftyTwoWeekHigh={stockData.fiftyTwoWeekHigh}
                      fiftyTwoWeekLow={stockData.fiftyTwoWeekLow}
                      companyName={stockData.companyName}
                      logoUrl={stockData.logoUrl}
                    />
                  </div>
                  <StockPerformanceCard performance={stockData.performance} />
                </div>

                {/* Technical Pattern Signals Detected for This Stock */}
                <StockPatternMatchesSection
                  ticker={ticker}
                  onOpenPatternDetail={(pat) => handleInspectStock({
                    ticker: ticker,
                    market: stockData?.market || (ticker.endsWith('.NS') || ticker.endsWith('.BO') ? 'IN' : 'US'),
                    pattern: pat.pattern,
                    display_name: pat.display_name,
                    pattern_score: pat.pattern_score,
                    pivot: pat.pivot,
                    score: pat.pattern_score,
                    reasons: pat.reasons,
                    name: stockData?.companyName || ticker,
                    companyName: stockData?.companyName || ticker
                  })}
                />

                {/* INDmoney MCP News & Sentiment Analysis */}
                <div style={{ marginTop: '20px', marginBottom: '20px' }}>
                  <NewsSentimentModule
                    symbol={ticker}
                    companyName={stockData?.companyName || ticker}
                  />
                </div>

                {/* Horizontal Filter Bar with Quick Chapter Jumps */}
                <HorizontalFilterBar
                  filterType={filterType}
                  setFilterType={setFilterType}
                  totalMetrics={totalMetrics}
                  positiveCount={positiveCount}
                  negativeCount={negativeCount}
                  activeChapterId={activeChapterId}
                />

                {/* Full-Width Continuous Financial Story with Embedded Plots in Each Chapter */}
                <div className="full-story-container">
                  <StoryCenterColumn
                    dimensions={stockData.dimensions}
                    charts={stockData.charts}
                    currencySymbol={stockData.currencySymbol}
                    stockData={stockData}
                    filterType={filterType}
                  />
                </div>
              </ErrorBoundary>
            )}
          </>
        )}
        </>
      )}
      </main>

      {/* Maximized Interactive Candlestick Chart & Pattern Formation Diagnostic Modal */}
      {inspectingStock && (
        <StockPatternInspectionModal
          isOpen={!!inspectingStock}
          onClose={handleCloseInspection}
          stock={inspectingStock}
          onGoToDeepAnalysis={handleGoToDeepAnalysis}
        />
      )}

      {/* Supabase Authentication Modal */}
      <AuthModal
        isOpen={isAuthModalOpen}
        initialMode={authModalMode}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={(loggedInUser) => {
          setUser(loggedInUser);
          setActiveTab('dashboard');
          setRefreshKey(k => k + 1);
        }}
      />





      {/* INDmoney Sync Prompt Modal for Unsynced Users */}
      <IndmoneySyncPromptModal
        userId={user?.userId}
        isOpen={isIndmoneyPromptOpen}
        onClose={() => setIsIndmoneyPromptOpen(false)}
        onSyncSuccess={() => {
          setIsIndmoneyPromptOpen(false);
          setRefreshKey(k => k + 1);
        }}
      />
    </div>
  );
}

