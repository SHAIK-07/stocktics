package com.stocktics.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.stocktics.data.api.ApiPatternMatch
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.portfolio.BuyPipelineCandidate
import com.stocktics.data.portfolio.PortfolioHolding
import com.stocktics.data.portfolio.SupabasePortfolioClient
import com.stocktics.data.session.UserSessionManager
import com.stocktics.data.stocks.StockCategory
import com.stocktics.data.stocks.StockSearchItem
import com.stocktics.data.stocks.SupabaseWatchlistClient
import com.stocktics.presentation.buy.BuyScreen
import com.stocktics.presentation.buy.StockBuyDetailScreen
import com.stocktics.presentation.compare.StockComparisonScreen
import com.stocktics.presentation.explore.CategoryMarketFilter
import com.stocktics.presentation.explore.CategoryStocksScreen
import com.stocktics.presentation.explore.ExploreScreen
import com.stocktics.presentation.heatmap.MarketHeatmapScreen
import com.stocktics.presentation.home.HomeScreen
import com.stocktics.presentation.learning.LearningScreen
import com.stocktics.presentation.navigation.AppNavContext
import com.stocktics.presentation.navigation.BottomNavTab
import com.stocktics.presentation.navigation.BottomSearchOverlay
import com.stocktics.presentation.navigation.ChartsSubTab
import com.stocktics.presentation.navigation.ContextualMoreSheet
import com.stocktics.presentation.navigation.DynamicFintechNavBar
import com.stocktics.presentation.navigation.MarketSubTab
import com.stocktics.presentation.navigation.RootNavTab
import com.stocktics.presentation.scanner.PatternDetailScreen
import com.stocktics.presentation.scanner.PatternScannerScreen
import com.stocktics.presentation.scanner.PatternsCatalogScreen
import com.stocktics.presentation.scanner.StockPatternInspectionSheet
import com.stocktics.presentation.sectors.SectorExplorerScreen
import com.stocktics.presentation.sell.SellScreen
import com.stocktics.presentation.sell.StockSellDetailScreen
import com.stocktics.presentation.settings.SettingsScreen
import com.stocktics.presentation.stock_detail.StockDetailScreen
import com.stocktics.presentation.watchlist.MarketWatchlistScreen
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.ui.components.StockticsTopBar
import com.stocktics.data.indmoney.IndmoneySyncManager
import com.stocktics.ui.theme.FintechBackground
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(
    userEmail: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Automatic Market Open INDmoney Sync (9:15 AM IST for India, 7:00 PM IST for US)
    LaunchedEffect(Unit) {
        IndmoneySyncManager.checkAndTriggerMarketOpenAutoSync(context)
    }

    // Global Reactive Market State (India vs US)
    var currentMarket by remember {
        mutableStateOf(UserSessionManager.getGlobalMarket(context))
    }

    // Dynamic Navigation Context
    var navContext by remember {
        mutableStateOf<AppNavContext>(AppNavContext.HomeRoot)
    }
    var isMoreSheetOpen by remember { mutableStateOf(false) }
    var isSearchOverlayOpen by remember { mutableStateOf(false) }

    // Portfolio metrics & profile nickname
    var portfolioTotalFormatted by remember { mutableStateOf<String?>(null) }
    var userNickname by remember { mutableStateOf<String?>(null) }

    val portfolioTrigger by SupabasePortfolioClient.portfolioVersion.collectAsState()

    LaunchedEffect(currentMarket, portfolioTrigger) {
        val isIndian = currentMarket == GlobalMarket.INDIA
        val summary = StockticsDatabase.getPortfolioSummary(context, isIndian = isIndian)
        val sym = if (isIndian) "₹" else "$"
        portfolioTotalFormatted = String.format(java.util.Locale.US, "%s %,.2f", sym, summary.currentValue)
        userNickname = UserSessionManager.getLoggedInUserNickname(context)
    }

    // Modal & Sheet Navigation States
    var selectedStockSymbol by remember { mutableStateOf<String?>(null) }
    var selectedBuyCandidate by remember { mutableStateOf<BuyPipelineCandidate?>(null) }
    var selectedSellHolding by remember { mutableStateOf<PortfolioHolding?>(null) }
    var isConfiguringWeights by remember { mutableStateOf(false) }
    var isPatternsCatalogOpen by remember { mutableStateOf(false) }
    var selectedPatternId by remember { mutableStateOf<String?>(null) }
    var inspectedPatternStock by remember { mutableStateOf<ApiPatternMatch?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var selectedCategoryForViewAll by remember { mutableStateOf<StockCategory?>(null) }
    var selectedCategoryMarketFilter by remember { mutableStateOf(CategoryMarketFilter.ALL) }
    var isNavBarExpanded by remember { mutableStateOf(true) }

    // Universal nested scroll listener to shrink/expand bottom nav bar across all pages
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                if (deltaY < -10f) {
                    if (isNavBarExpanded) isNavBarExpanded = false
                } else if (deltaY > 10f) {
                    if (!isNavBarExpanded) isNavBarExpanded = true
                }
                return Offset.Zero
            }
        }
    }

    // Reset navbar to expanded state on tab change
    LaunchedEffect(navContext) {
        isNavBarExpanded = true
    }

    // Intercept back button if any modal/detail screen or contextual sub-mode is active
    val isAnyModalOpen = isPatternsCatalogOpen || selectedPatternId != null || inspectedPatternStock != null ||
            isSettingsOpen || selectedCategoryForViewAll != null || selectedStockSymbol != null ||
            selectedBuyCandidate != null || selectedSellHolding != null || isConfiguringWeights ||
            isSearchOverlayOpen

    val isAnySubScreenActive = isAnyModalOpen || isMoreSheetOpen || navContext !is AppNavContext.HomeRoot

    BackHandler(enabled = isAnySubScreenActive) {
        if (isSearchOverlayOpen) {
            isSearchOverlayOpen = false
        } else if (isMoreSheetOpen) {
            isMoreSheetOpen = false
        } else if (inspectedPatternStock != null) {
            inspectedPatternStock = null
        } else if (selectedPatternId != null) {
            selectedPatternId = null
        } else if (isPatternsCatalogOpen) {
            isPatternsCatalogOpen = false
        } else if (isSettingsOpen) {
            isSettingsOpen = false
        } else if (selectedStockSymbol != null) {
            selectedStockSymbol = null
        } else if (selectedCategoryForViewAll != null) {
            selectedCategoryForViewAll = null
        } else if (selectedSellHolding != null) {
            selectedSellHolding = null
        } else if (selectedBuyCandidate != null) {
            selectedBuyCandidate = null
        } else if (isConfiguringWeights) {
            isConfiguringWeights = false
        } else if (navContext !is AppNavContext.HomeRoot) {
            navContext = AppNavContext.HomeRoot
        }
        SupabaseWatchlistClient.notifyWatchlistChanged()
        SupabasePortfolioClient.notifyPortfolioChanged()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Persistent Global Top App Bar (INDmoney-grade luxury wealth header)
            StockticsTopBar(
                currentMarket = currentMarket,
                onMarketSelected = { newMarket ->
                    currentMarket = newMarket
                    UserSessionManager.saveGlobalMarket(context, newMarket)
                },
                onOpenSearch = { isSearchOverlayOpen = true },
                userEmail = userEmail,
                onOpenProfile = { isSettingsOpen = true },
                userNickname = userNickname,
                totalPortfolioValue = portfolioTotalFormatted,
                onRefresh = {
                    coroutineScope.launch {
                        SupabasePortfolioClient.fetchHoldingsFromCloud(context)
                        SupabasePortfolioClient.notifyPortfolioChanged()
                    }
                }
            )

            // Dynamic Navigation Content Area with cross-fade (Scroll collapses/expands navbar on all screens)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(nestedScrollConnection)
            ) {
                AnimatedContent(
                    targetState = navContext,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(220))
                    },
                    label = "mainNavContent"
                ) { state ->
                    when (state) {
                        is AppNavContext.HomeRoot -> {
                            HomeScreen(
                                userEmail = userEmail,
                                currentMarket = currentMarket,
                                onNavigateToTab = { tab ->
                                    when (tab) {
                                        BottomNavTab.HOME -> navContext = AppNavContext.HomeRoot
                                        BottomNavTab.SCANNER -> navContext = AppNavContext.Charts(ChartsSubTab.PATTERNS)
                                        BottomNavTab.BUY -> navContext = AppNavContext.Market(currentMarket, MarketSubTab.BUY_PIPELINE)
                                        BottomNavTab.SELL -> navContext = AppNavContext.Market(currentMarket, MarketSubTab.HOLDINGS)
                                        BottomNavTab.LEARNING -> navContext = AppNavContext.Charts(ChartsSubTab.ACADEMY)
                                    }
                                },
                                onOpenHeatmap = { navContext = AppNavContext.Charts(ChartsSubTab.HEATMAP) },
                                onOpenCompare = { navContext = AppNavContext.Charts(ChartsSubTab.COMPARE) },
                                onOpenSectors = { navContext = AppNavContext.Charts(ChartsSubTab.SECTORS) },
                                onNavigateToExplore = { navContext = AppNavContext.Market(currentMarket, MarketSubTab.EXPLORE) },
                                onStockSelected = { symbol -> selectedStockSymbol = symbol },
                                onViewAllCategory = { category, marketFilter ->
                                    selectedCategoryForViewAll = category
                                    selectedCategoryMarketFilter = marketFilter
                                },
                                onNavigateToMarket = { market, subTab ->
                                    currentMarket = market
                                    UserSessionManager.saveGlobalMarket(context, market)
                                    navContext = AppNavContext.Market(market, subTab)
                                },
                                onNavigateToCharts = { subTab ->
                                    navContext = AppNavContext.Charts(subTab)
                                },
                                onScrollDirectionChanged = { isExpanded ->
                                    isNavBarExpanded = isExpanded
                                }
                            )
                        }

                        is AppNavContext.Market -> {
                            when (state.subTab) {
                                MarketSubTab.EXPLORE -> {
                                    ExploreScreen(
                                        currentMarket = state.market,
                                        onStockSelected = { symbol -> selectedStockSymbol = symbol },
                                        onViewAllCategory = { category, marketFilter ->
                                            selectedCategoryForViewAll = category
                                            selectedCategoryMarketFilter = marketFilter
                                        },
                                        onOpenSectors = { navContext = AppNavContext.Charts(ChartsSubTab.SECTORS) }
                                    )
                                }
                                MarketSubTab.HOLDINGS -> {
                                    SellScreen(
                                        currentMarket = state.market,
                                        onNavigateToExplore = {
                                            navContext = AppNavContext.Market(state.market, MarketSubTab.EXPLORE)
                                        },
                                        onHoldingSelected = { holding -> selectedSellHolding = holding }
                                    )
                                }
                                MarketSubTab.BUY -> {
                                    BuyScreen(
                                        currentMarket = state.market,
                                        onNavigateToExplore = {
                                            navContext = AppNavContext.Market(state.market, MarketSubTab.EXPLORE)
                                        },
                                        onCandidateSelected = { candidate -> selectedBuyCandidate = candidate }
                                    )
                                }
                                MarketSubTab.WATCHLIST -> {
                                    MarketWatchlistScreen(
                                        currentMarket = state.market,
                                        onStockSelected = { symbol -> selectedStockSymbol = symbol },
                                        onNavigateToExplore = {
                                            navContext = AppNavContext.Market(state.market, MarketSubTab.EXPLORE)
                                        },
                                        onScrollDirectionChanged = { isExpanded ->
                                            isNavBarExpanded = isExpanded
                                        }
                                    )
                                }
                            }
                        }

                        is AppNavContext.Charts -> {
                            when (state.subTab) {
                                ChartsSubTab.PATTERNS -> {
                                    PatternScannerScreen(
                                        currentMarket = currentMarket,
                                        onMarketChanged = { market ->
                                            currentMarket = market
                                            UserSessionManager.saveGlobalMarket(context, market)
                                        },
                                        onStockSelected = { symbol -> selectedStockSymbol = symbol },
                                        onInspectStock = { stock -> inspectedPatternStock = stock },
                                        onOpenPatternsCatalog = { isPatternsCatalogOpen = true },
                                        onOpenPatternDetail = { patId -> selectedPatternId = patId },
                                        onOpenSectors = { navContext = AppNavContext.Charts(ChartsSubTab.SECTORS) },
                                        onViewAllCategory = { category, marketFilter ->
                                            selectedCategoryForViewAll = category
                                            selectedCategoryMarketFilter = marketFilter
                                        }
                                    )
                                }
                                ChartsSubTab.HEATMAP -> {
                                    MarketHeatmapScreen(
                                        currentMarket = currentMarket,
                                        onStockSelected = { symbol -> selectedStockSymbol = symbol },
                                        onBack = { navContext = AppNavContext.HomeRoot },
                                        applyStatusBarsPadding = false
                                    )
                                }
                                ChartsSubTab.COMPARE -> {
                                    StockComparisonScreen(
                                        currentMarket = currentMarket,
                                        onStockSelected = { symbol -> selectedStockSymbol = symbol },
                                        onBack = { navContext = AppNavContext.HomeRoot },
                                        applyStatusBarsPadding = false
                                    )
                                }
                                ChartsSubTab.SECTORS -> {
                                    SectorExplorerScreen(
                                        currentMarket = currentMarket,
                                        onMarketChange = { newMarket ->
                                            currentMarket = newMarket
                                            UserSessionManager.saveGlobalMarket(context, newMarket)
                                        },
                                        onStockSelected = { symbol -> selectedStockSymbol = symbol },
                                        onBack = { navContext = AppNavContext.HomeRoot },
                                        applyStatusBarsPadding = false
                                    )
                                }
                                ChartsSubTab.ACADEMY -> {
                                    LearningScreen(
                                        onStockSelected = { symbol -> selectedStockSymbol = symbol }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Dynamic Morphing Bottom Navigation Bar (Hidden when Sub-Screens or Modals are Open)
        if (!isAnyModalOpen) {
            DynamicFintechNavBar(
                navContext = navContext,
                isExpanded = isNavBarExpanded,
                onSelectRootTab = { tab ->
                    when (tab) {
                        RootNavTab.HOME -> {
                            navContext = AppNavContext.HomeRoot
                            SupabasePortfolioClient.notifyPortfolioChanged()
                            SupabaseWatchlistClient.notifyWatchlistChanged()
                        }
                        RootNavTab.IND -> {
                            currentMarket = GlobalMarket.INDIA
                            UserSessionManager.saveGlobalMarket(context, GlobalMarket.INDIA)
                            navContext = AppNavContext.Market(GlobalMarket.INDIA, MarketSubTab.EXPLORE)
                        }
                        RootNavTab.US -> {
                            currentMarket = GlobalMarket.US
                            UserSessionManager.saveGlobalMarket(context, GlobalMarket.US)
                            navContext = AppNavContext.Market(GlobalMarket.US, MarketSubTab.EXPLORE)
                        }
                        RootNavTab.TOOLS -> {
                            navContext = AppNavContext.Charts(ChartsSubTab.PATTERNS)
                        }
                    }
                },
                onSelectMarketSubTab = { subTab ->
                    val activeMarket = (navContext as? AppNavContext.Market)?.market ?: currentMarket
                    navContext = AppNavContext.Market(activeMarket, subTab)
                },
                onSelectChartsSubTab = { subTab ->
                    navContext = AppNavContext.Charts(subTab)
                },
                onReturnHome = {
                    navContext = AppNavContext.HomeRoot
                    SupabasePortfolioClient.notifyPortfolioChanged()
                    SupabaseWatchlistClient.notifyWatchlistChanged()
                },
                onOpenSearch = { isSearchOverlayOpen = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }

        // Contextual "More" Quick-Action Sheet
        ContextualMoreSheet(
            isVisible = isMoreSheetOpen,
            onDismiss = { isMoreSheetOpen = false },
            onOpenExplore = {
                navContext = AppNavContext.Market(currentMarket, MarketSubTab.EXPLORE)
            },
            onOpenHoldings = {
                val activeMarket = (navContext as? AppNavContext.Market)?.market ?: currentMarket
                navContext = AppNavContext.Market(activeMarket, MarketSubTab.HOLDINGS)
            },
            onOpenWatchlist = {
                navContext = AppNavContext.Market(currentMarket, MarketSubTab.WATCHLIST)
            },
            onOpenHeatmap = { navContext = AppNavContext.Charts(ChartsSubTab.HEATMAP) },
            onOpenCompare = { navContext = AppNavContext.Charts(ChartsSubTab.COMPARE) },
            onOpenSectors = { navContext = AppNavContext.Charts(ChartsSubTab.SECTORS) },
            onOpenAcademy = {
                navContext = AppNavContext.Charts(ChartsSubTab.ACADEMY)
            },
            onOpenWeightsConfig = { isConfiguringWeights = true }
        )

        // Modern Bottom Search Overlay (Screenshot 5)
        BottomSearchOverlay(
            isVisible = isSearchOverlayOpen,
            currentMarket = currentMarket,
            onDismiss = { isSearchOverlayOpen = false },
            onStockSelected = { symbol ->
                selectedStockSymbol = symbol
            }
        )

        // Fullscreen 33-Patterns Catalog Screen
        AnimatedVisibility(
            visible = isPatternsCatalogOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            PatternsCatalogScreen(
                onSelectPattern = { patId ->
                    selectedPatternId = patId
                },
                onBack = { isPatternsCatalogOpen = false }
            )
        }

        // Fullscreen Pattern Detail 10-Yr Backtrack Screen
        AnimatedVisibility(
            visible = selectedPatternId != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedPatternId?.let { patId ->
                PatternDetailScreen(
                    patternId = patId,
                    currentMarket = currentMarket,
                    onInspectStock = { stock -> inspectedPatternStock = stock },
                    onBack = { selectedPatternId = null }
                )
            }
        }

        // Candlestick Pattern Formation Diagnostic Sheet
        AnimatedVisibility(
            visible = inspectedPatternStock != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            StockPatternInspectionSheet(
                stock = inspectedPatternStock,
                onDismiss = { inspectedPatternStock = null },
                onGoToDeepAnalysis = { ticker ->
                    inspectedPatternStock = null
                    selectedStockSymbol = ticker
                },
                onAddToBuyPipeline = { stock ->
                    coroutineScope.launch {
                        val item = StockSearchItem(
                            symbol = stock.ticker,
                            name = stock.companyName,
                            exchange = stock.exchange,
                            currency = if (stock.market == "IN") "₹" else "$",
                            price = String.format(java.util.Locale.US, "%.2f", stock.price),
                            change = String.format(java.util.Locale.US, "%+.2f%%", stock.dayChangePct),
                            isPositive = stock.dayChangePct >= 0,
                            logoUrl = stock.logoUrl
                        )
                        SupabaseWatchlistClient.addToWatchlist(context, item)
                        SupabaseWatchlistClient.notifyWatchlistChanged()
                    }
                }
            )
        }

        // Fullscreen Dedicated Category Stocks View All Screen
        AnimatedVisibility(
            visible = selectedCategoryForViewAll != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedCategoryForViewAll?.let { category ->
                CategoryStocksScreen(
                    category = category,
                    currentMarket = currentMarket,
                    onBack = { selectedCategoryForViewAll = null },
                    onStockSelected = { symbol -> selectedStockSymbol = symbol },
                    initialMarketFilter = selectedCategoryMarketFilter
                )
            }
        }

        // Fullscreen Deep Stock Detail Research Screen
        AnimatedVisibility(
            visible = selectedStockSymbol != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedStockSymbol?.let { sym ->
                StockDetailScreen(
                    symbol = sym,
                    onBack = {
                        selectedStockSymbol = null
                        SupabaseWatchlistClient.notifyWatchlistChanged()
                    },
                    onNavigateToBuy = {
                        selectedStockSymbol = null
                        navContext = AppNavContext.Market(currentMarket, MarketSubTab.BUY_PIPELINE)
                    },
                    onNavigateToSell = {
                        selectedStockSymbol = null
                        navContext = AppNavContext.Market(currentMarket, MarketSubTab.HOLDINGS)
                    }
                )
            }
        }

        // Fullscreen Dedicated Stock Buy Detail & Quant Intelligence Screen (Buy Flow)
        AnimatedVisibility(
            visible = selectedBuyCandidate != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedBuyCandidate?.let { candidate ->
                StockBuyDetailScreen(
                    candidate = candidate,
                    onBack = {
                        selectedBuyCandidate = null
                    }
                )
            }
        }

        // Fullscreen Dedicated Stock Sell Detail Screen
        AnimatedVisibility(
            visible = selectedSellHolding != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedSellHolding?.let { holding ->
                StockSellDetailScreen(
                    holding = holding,
                    onBack = {
                        selectedSellHolding = null
                    }
                )
            }
        }

        // Fullscreen Settings & Profile Screen
        AnimatedVisibility(
            visible = isSettingsOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            SettingsScreen(
                userEmail = userEmail,
                onLogout = onLogout,
                onConfiguringWeightsChange = { isConfiguringWeights = it },
                onBack = { isSettingsOpen = false }
            )
        }
    }
}