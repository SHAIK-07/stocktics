package com.stocktics.presentation.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.stocktics.ui.components.StockticsPullToRefreshBox
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.stocktics.R
import kotlinx.coroutines.isActive
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.portfolio.PortfolioHolding
import com.stocktics.data.portfolio.PortfolioSummary
import com.stocktics.data.portfolio.SupabasePortfolioClient
import com.stocktics.data.profile.SupabaseProfileClient
import com.stocktics.data.session.UserSessionManager
import com.stocktics.data.stocks.MarketIndexItem
import com.stocktics.data.stocks.StockCategory
import com.stocktics.data.stocks.StockSearchItem
import com.stocktics.data.stocks.SupabaseStockClient
import com.stocktics.data.stocks.SupabaseWatchlistClient
import com.stocktics.data.stocks.YahooFinanceClient
import com.stocktics.presentation.explore.CategoryMarketFilter
import com.stocktics.presentation.navigation.BottomNavTab
import com.stocktics.presentation.navigation.ChartsSubTab
import com.stocktics.presentation.navigation.MarketSubTab
import com.stocktics.presentation.sectors.SectorGradientIcon
import com.stocktics.presentation.sectors.getSectorVisual
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.data.indmoney.IndmoneySyncManager
import com.stocktics.presentation.indmoney.IndmoneyAuthDialog
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.StockticsSegmentedControl
import com.stocktics.ui.components.fintechPressBounce
import com.stocktics.ui.components.liquidGlassElevated
import androidx.compose.ui.draw.shadow
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBlueDark
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.IsDarkTheme
import com.stocktics.ui.theme.LocalFintechColors
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.LossRedBg
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import com.stocktics.ui.theme.WarningAmber
import com.stocktics.ui.theme.WarningAmberBg
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Watchlist Category Movement Analytics data model (mirrored from web application).
 */
data class WatchlistCategoryAnalytics(
    val name: String,
    val stocks: List<StockSearchItem>,
    val count: Int,
    val avgChange: Double,
    val isPositive: Boolean,
    val gainerCount: Int,
    val loserCount: Int,
    val topStock: StockSearchItem? = null,
    val bottomStock: StockSearchItem? = null
)

enum class WatchlistPerformanceFilter {
    ALL,
    TOP,
    UNDER
}

/**
 * Modern Streamlined HomeScreen.
 * Layout Hierarchy (100% Scrollable):
 * 1. Top Bar: Greetings ("Hello, Shaik") + Live Market Status + Refresh & Notification Action buttons.
 * 2. Benchmark Indices: Auto-scrolling horizontal live pulse.
 * 3. Market Toggle: Segmented [🇮🇳 India | 🇺🇸 US] switcher.
 * 4. Net Portfolio Value Card: Contextual balance, 1-Day Return, Total Returns, Realized P&L.
 * 5. Quick Buttons: Setups (33 Patterns), Heatmap (Treemap), Compare (vs Peers).
 * 6. Market Spotlight & Showcase: 3D Stock Exchange Hero + The Trillion Dollar Club cards.
 * 7. My Watchlist: INDmoney-synced Category Movement & Momentum analysis with icon toggle & expandable stocks.
 */
@Composable
fun HomeScreen(
    @Suppress("UNUSED_PARAMETER") userEmail: String = "",
    currentMarket: GlobalMarket = GlobalMarket.INDIA,
    modifier: Modifier = Modifier,
    onNavigateToTab: (BottomNavTab) -> Unit = {},
    onOpenHeatmap: () -> Unit = {},
    onOpenCompare: () -> Unit = {},
    onOpenSectors: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onStockSelected: (String) -> Unit = {},
    onViewAllCategory: (StockCategory, CategoryMarketFilter) -> Unit = { _, _ -> },
    onNavigateToMarket: (GlobalMarket, MarketSubTab) -> Unit = { _, _ -> },
    onNavigateToCharts: (ChartsSubTab) -> Unit = {},
    onScrollDirectionChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nickname by remember { mutableStateOf(UserSessionManager.getLoggedInUserNickname(context)) }
    var isRefreshing by remember { mutableStateOf(false) }

    // INDmoney MCP Sync State
    val indmoneyStatus by IndmoneySyncManager.status.collectAsState()
    val isIndmoneySyncing by IndmoneySyncManager.isSyncing.collectAsState()
    var indmoneyAuthUrl by remember { mutableStateOf<String?>(null) }
    var isOpeningAuth by remember { mutableStateOf(false) }

    // Active Market Toggle: defaults to currentMarket, togglable between India and US
    var selectedMarket by remember(currentMarket) { mutableStateOf(currentMarket) }
    val isIndian = selectedMarket == GlobalMarket.INDIA
    val currencySymbol = if (isIndian) "₹" else "$"

    // Live Market Hours & Public Holiday Detection
    val isIndianOpen = remember { SupabaseStockClient.isIndianMarketOpen() }
    val isUsOpen = remember { SupabaseStockClient.isUsMarketOpen() }
    val indianHolidayStatus = remember { SupabaseStockClient.getIndianMarketStatusDetail() }

    val liveMarketStatus = when {
        isIndianOpen && isUsOpen -> "🇮🇳 NSE/BSE & 🇺🇸 NYSE/NASDAQ Live"
        isIndianOpen -> "🇮🇳 Indian Markets Live (NSE / BSE)"
        isUsOpen -> "🇺🇸 US Markets Live (NYSE / NASDAQ)"
        else -> indianHolidayStatus
    }
    val isAnyMarketLive = isIndianOpen || isUsOpen

    // Benchmark Indices State: partitioned strictly by selectedMarket (India vs US)
    var marketIndices by remember(selectedMarket) {
        val initial = if (isIndian) {
            listOf(
                MarketIndexItem("NIFTY 50", "^NSEI", "22,421.95", "-198.50 (-0.88%)", false),
                MarketIndexItem("SENSEX", "^BSESN", "71,909.70", "-570.59 (-0.79%)", false),
                MarketIndexItem("BANK NIFTY", "^NSEBANK", "48,150.20", "+142.10 (+0.30%)", true),
                MarketIndexItem("NIFTY IT", "^CNXIT", "38,240.50", "+210.80 (+0.55%)", true)
            )
        } else {
            listOf(
                MarketIndexItem("S&P 500", "^GSPC", "5,117.09", "+28.40 (+0.56%)", true),
                MarketIndexItem("NASDAQ 100", "^IXIC", "16,107.33", "+130.65 (+0.82%)", true),
                MarketIndexItem("DOW JONES", "^DJI", "38,989.84", "-42.30 (-0.11%)", false),
                MarketIndexItem("RUSSELL 2000", "^RUT", "2,054.12", "+18.30 (+0.90%)", true)
            )
        }
        mutableStateOf(initial)
    }

    // Auto-scrolling state for Benchmark Indices
    val indicesListState = rememberLazyListState()
    val repeatedIndices = remember(marketIndices) {
        if (marketIndices.isNotEmpty()) {
            List(12) { marketIndices }.flatten()
        } else {
            emptyList()
        }
    }

    LaunchedEffect(repeatedIndices) {
        if (repeatedIndices.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(20)
            if (!indicesListState.isScrollInProgress) {
                indicesListState.scrollBy(1.2f)
                if (indicesListState.firstVisibleItemIndex >= marketIndices.size * 6) {
                    indicesListState.scrollToItem(
                        index = indicesListState.firstVisibleItemIndex % marketIndices.size,
                        scrollOffset = indicesListState.firstVisibleItemScrollOffset
                    )
                }
            }
        }
    }

    // Reactive Triggers for Portfolio & Watchlist
    val portfolioTrigger by SupabasePortfolioClient.portfolioVersion.collectAsState()
    val watchlistTrigger by SupabaseWatchlistClient.watchlistVersion.collectAsState()

    var portfolioSummary by remember(selectedMarket) {
        mutableStateOf(StockticsDatabase.getPortfolioSummary(context, isIndian = isIndian))
    }
    var activeHoldings by remember(selectedMarket) {
        mutableStateOf<List<PortfolioHolding>>(StockticsDatabase.getActiveHoldings(context, isIndian = isIndian))
    }
    var watchlistStocks by remember(selectedMarket) {
        mutableStateOf<List<StockSearchItem>>(StockticsDatabase.getWatchlist(context, isIndian = isIndian))
    }

    var dynamicLeaderStocks by remember(selectedMarket) {
        mutableStateOf<List<StockSearchItem>>(emptyList())
    }

    // Market leaders list for India vs US (Item 5B)
    val leaderStocks = remember(isIndian, watchlistStocks, dynamicLeaderStocks) {
        if (dynamicLeaderStocks.isNotEmpty()) {
            dynamicLeaderStocks
        } else {
            val baseList = if (isIndian) {
                listOf(
                    StockSearchItem("RELIANCE.NS", "Reliance Industries", "NSE", "₹", "₹ 2,980.40", "+1.42%", true, "https://assets.parqet.com/logos/symbol/RELIANCE.NS", "Energy"),
                    StockSearchItem("TCS.NS", "Tata Consultancy Services", "NSE", "₹", "₹ 4,120.00", "+0.85%", true, "https://assets.parqet.com/logos/symbol/TCS.NS", "IT Services"),
                    StockSearchItem("HDFCBANK.NS", "HDFC Bank Ltd", "NSE", "₹", "₹ 1,652.80", "+1.15%", true, "https://assets.parqet.com/logos/symbol/HDFCBANK.NS", "Banking"),
                    StockSearchItem("INFY.NS", "Infosys Limited", "NSE", "₹", "₹ 1,894.50", "+1.65%", true, "https://assets.parqet.com/logos/symbol/INFY.NS", "IT Services"),
                    StockSearchItem("BHARTIARTL.NS", "Bharti Airtel Ltd", "NSE", "₹", "₹ 1,562.30", "+1.80%", true, "https://assets.parqet.com/logos/symbol/BHARTIARTL.NS", "Telecom"),
                    StockSearchItem("ICICIBANK.NS", "ICICI Bank Ltd", "NSE", "₹", "₹ 1,242.00", "+0.92%", true, "https://assets.parqet.com/logos/symbol/ICICIBANK.NS", "Banking"),
                    StockSearchItem("ITC.NS", "ITC Limited", "NSE", "₹", "₹ 502.40", "+0.64%", true, "https://assets.parqet.com/logos/symbol/ITC.NS", "FMCG")
                )
            } else {
                listOf(
                    StockSearchItem("NVDA", "NVIDIA Corporation", "NASDAQ", "$", "$ 128.40", "+2.84%", true, "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=NVDA", "Semiconductors"),
                    StockSearchItem("AAPL", "Apple Inc.", "NASDAQ", "$", "$ 226.50", "+1.22%", true, "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=AAPL", "Consumer Tech"),
                    StockSearchItem("MSFT", "Microsoft Corporation", "NASDAQ", "$", "$ 449.10", "+0.95%", true, "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=MSFT", "Cloud & AI"),
                    StockSearchItem("AMZN", "Amazon.com Inc.", "NASDAQ", "$", "$ 188.20", "+1.45%", true, "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=AMZN", "E-Commerce"),
                    StockSearchItem("GOOGL", "Alphabet Inc.", "NASDAQ", "$", "$ 183.60", "+1.52%", true, "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=GOOGL", "Search & AI"),
                    StockSearchItem("META", "Meta Platforms Inc.", "NASDAQ", "$", "$ 512.30", "+2.14%", true, "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=META", "Social Media"),
                    StockSearchItem("TSLA", "Tesla Inc.", "NASDAQ", "$", "$ 248.80", "-0.75%", false, "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=TSLA", "Automotive")
                )
            }
            baseList.map { base ->
                watchlistStocks.firstOrNull { it.symbol == base.symbol || it.symbol == base.symbol.replace(".NS", "") }?.let { live ->
                    base.copy(price = live.price, change = live.change, isPositive = live.isPositive)
                } ?: base
            }
        }
    }

    val leaderScrollState = rememberLazyListState()
    val repeatedLeaderStocks = remember(leaderStocks) {
        if (leaderStocks.isNotEmpty()) List(20) { leaderStocks }.flatten() else emptyList()
    }

    LaunchedEffect(repeatedLeaderStocks) {
        if (repeatedLeaderStocks.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(20)
            if (!leaderScrollState.isScrollInProgress) {
                leaderScrollState.scrollBy(1.2f)
                if (leaderStocks.isNotEmpty() && leaderScrollState.firstVisibleItemIndex >= leaderStocks.size * 6) {
                    leaderScrollState.scrollToItem(
                        index = leaderScrollState.firstVisibleItemIndex % leaderStocks.size,
                        scrollOffset = leaderScrollState.firstVisibleItemScrollOffset
                    )
                }
            }
        }
    }

    // Category Watchlist Filter & Expansion State
    var performanceFilter by remember { mutableStateOf(WatchlistPerformanceFilter.ALL) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // Scroll State Detection to compress navbar on scroll down, expand on scroll up
    val mainScrollState = rememberLazyListState()
    var lastScrollIndex by remember { mutableIntStateOf(0) }
    var lastScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(mainScrollState) {
        snapshotFlow { Pair(mainScrollState.firstVisibleItemIndex, mainScrollState.firstVisibleItemScrollOffset) }
            .collect { (idx, offset) ->
                if (idx == 0 && offset < 25) {
                    onScrollDirectionChanged(true)
                } else if (idx > lastScrollIndex || (idx == lastScrollIndex && offset > lastScrollOffset + 14)) {
                    onScrollDirectionChanged(false)
                } else if (idx < lastScrollIndex || (idx == lastScrollIndex && offset < lastScrollOffset - 14)) {
                    onScrollDirectionChanged(true)
                }
                lastScrollIndex = idx
                lastScrollOffset = offset
            }
    }

    // Fetch and refresh all data
    suspend fun loadAllData() {
        coroutineScope {
            launch {
                try {
                    val remote = SupabaseProfileClient.fetchUserProfile(context)
                    if (!remote.isNullOrBlank()) {
                        nickname = remote
                    }
                } catch (_: Exception) {}
            }

            launch {
                try {
                    val live = YahooFinanceClient.fetchLiveMarketIndices(isIndian)
                    val valid = live.filter { it.price.isNotBlank() && it.price != "--" }
                    if (valid.isNotEmpty()) {
                        marketIndices = valid
                    }
                } catch (_: Exception) {}
            }

            launch {
                try {
                    SupabasePortfolioClient.fetchHoldingsFromCloud(context)
                    SupabasePortfolioClient.fetchTransactionsFromCloud(context)
                    SupabasePortfolioClient.refreshLiveHoldingPrices(context)
                    activeHoldings = StockticsDatabase.getActiveHoldings(context, isIndian = isIndian)
                    portfolioSummary = StockticsDatabase.getPortfolioSummary(context, isIndian = isIndian)
                } catch (_: Exception) {}
            }

            launch {
                try {
                    val cloudWl = SupabaseWatchlistClient.fetchWatchlist(context, isIndian = isIndian)
                    if (cloudWl.isNotEmpty()) {
                        watchlistStocks = cloudWl
                    } else {
                        watchlistStocks = StockticsDatabase.getWatchlist(context, isIndian = isIndian)
                    }
                } catch (_: Exception) {
                    watchlistStocks = StockticsDatabase.getWatchlist(context, isIndian = isIndian)
                }
            }

            launch {
                try {
                    val realStocks = SupabaseStockClient.getRealStocks(if (isIndian) "IN" else "US")
                    if (realStocks.isNotEmpty()) {
                        dynamicLeaderStocks = realStocks.take(12).map { real ->
                            StockSearchItem(
                                symbol = real.symbol,
                                name = real.name,
                                exchange = real.exchange,
                                currency = if (isIndian) "₹" else "$",
                                price = real.price,
                                change = real.change,
                                isPositive = real.isPositive,
                                logoUrl = real.logoUrl,
                                category = real.sector.ifBlank { "Equities" }
                            )
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Initial Load & On Reactive Change
    LaunchedEffect(selectedMarket, portfolioTrigger, watchlistTrigger) {
        IndmoneySyncManager.checkStatus(context)
        IndmoneySyncManager.checkAndTriggerMarketOpenAutoSync(context)
        portfolioSummary = StockticsDatabase.getPortfolioSummary(context, isIndian = isIndian)
        activeHoldings = StockticsDatabase.getActiveHoldings(context, isIndian = isIndian)
        val localWl = StockticsDatabase.getWatchlist(context, isIndian = isIndian)
        if (localWl.isNotEmpty()) {
            watchlistStocks = localWl
        }
        loadAllData()
    }

    // Periodic Auto-Sync Check (Market open at 9:15 AM IST & 7:00 PM IST)
    LaunchedEffect(Unit) {
        while (isActive) {
            IndmoneySyncManager.checkAndTriggerMarketOpenAutoSync(context)
            delay(60_000L)
        }
    }

    // Calculate Category Movement Analytics (same algorithm as web app DashboardView.jsx)
    val categoryAnalytics = remember(watchlistStocks) {
        val groups = mutableMapOf<String, MutableList<StockSearchItem>>()
        watchlistStocks.forEach { stock ->
            val cat = stock.category.ifBlank { "General" }
            groups.getOrPut(cat) { mutableListOf() }.add(stock)
        }

        groups.map { (catName, stocks) ->
            var totalChange = 0.0
            var gainers = 0
            var losers = 0

            val parsedStocks = stocks.map { s ->
                val clean = s.change
                    .replace("%", "")
                    .replace("+", "")
                    .replace("₹", "")
                    .replace("$", "")
                    .trim()
                val pct = try {
                    if (clean.contains("(")) {
                        clean.substringAfter("(").substringBefore(")").replace("%", "").trim().toDouble()
                    } else {
                        clean.toDouble()
                    }
                } catch (_: Exception) {
                    0.0
                }
                Pair(s, pct)
            }

            parsedStocks.forEach { (s, pct) ->
                totalChange += pct
                if (s.isPositive || pct >= 0) gainers++ else losers++
            }

            val sortedStocks = parsedStocks.sortedByDescending { it.second }.map { it.first }
            val avg = if (stocks.isNotEmpty()) totalChange / stocks.size else 0.0
            val topStock = sortedStocks.firstOrNull()
            val bottomStock = if (sortedStocks.size > 1) sortedStocks.lastOrNull() else null

            WatchlistCategoryAnalytics(
                name = catName,
                stocks = sortedStocks,
                count = stocks.size,
                avgChange = avg,
                isPositive = avg >= 0,
                gainerCount = gainers,
                loserCount = losers,
                topStock = topStock,
                bottomStock = bottomStock
            )
        }.sortedByDescending { it.avgChange }
    }

    // Filter categories based on active filter button
    val filteredCategories = remember(categoryAnalytics, performanceFilter) {
        when (performanceFilter) {
            WatchlistPerformanceFilter.ALL -> categoryAnalytics
            WatchlistPerformanceFilter.TOP -> categoryAnalytics.filter { it.avgChange > 0 }.sortedByDescending { it.avgChange }
            WatchlistPerformanceFilter.UNDER -> categoryAnalytics.filter { it.avgChange < 0 }.sortedBy { it.avgChange }
        }
    }

    StockticsPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                try {
                    loadAllData()
                    delay(400)
                } finally {
                    isRefreshing = false
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        // Single unified scrollable LazyColumn
        LazyColumn(
            state = mainScrollState,
            modifier = Modifier
                .fillMaxSize()
                .background(FintechBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =====================================================================
            // ITEM 1: GREETINGS & LIVE STATUS & NOTIFICATION ACTION
            // =====================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val displayName = nickname.takeIf { !it.isNullOrBlank() } ?: "Investor"
                        Text(
                            text = "Hello, $displayName",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isAnyMarketLive) GainGreen else TextTertiary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = liveMarketStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isAnyMarketLive) GainGreen else TextTertiary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // INDmoney Sync Action Button (Blue pill matching the positions tab)
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isIndmoneySyncing) FintechBlue.copy(alpha = 0.85f) else FintechBlue)
                                .clickable(enabled = !isIndmoneySyncing) {
                                    scope.launch {
                                        IndmoneySyncManager.performSync(context)
                                        loadAllData()
                                    }
                                }
                                .fintechPressBounce()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isIndmoneySyncing) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Syncing",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Sync",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Notification Action Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FintechSurface)
                                .border(1.dp, FintechBorder, CircleShape)
                                .clickable { }
                                .fintechPressBounce(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Notifications",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // =====================================================================
            // ITEM 1B: SEGMENTED MARKET TOGGLE (INDIA 🇮🇳 vs US 🇺🇸 WITH FLAGS)
            // =====================================================================
            item {
                StockticsSegmentedControl(
                    items = listOf(GlobalMarket.INDIA, GlobalMarket.US),
                    selectedItem = selectedMarket,
                    onItemSelected = { market ->
                        selectedMarket = market
                        UserSessionManager.saveGlobalMarket(context, market)
                    },
                    itemLabel = { market ->
                        if (market == GlobalMarket.INDIA) "India (NSE / BSE)" else "US (NYSE / NASDAQ)"
                    },
                    itemPrefix = { market -> market.flag },
                    height = 46.dp,
                    outerCornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        // =====================================================================
        // ITEM 2: BENCHMARK INDICES EXCHANGE HERO BANNER WITH SCROLLING PULSE
        // =====================================================================
        item {
            val isDark = IsDarkTheme
            val bannerBorderColor = if (isDark) FintechBorder else Color(0xFFCBD5E1)
            val indicesHeroImage = if (isDark) {
                if (isIndian) R.drawable.hero_bse_dalal_street else R.drawable.hero_nyse_wall_street
            } else {
                if (isIndian) R.drawable.hero_bse_dalal_street_light else R.drawable.hero_nyse_wall_street_light
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(272.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDark) Color(0xFF0B0E14) else FintechSurface)
                    .border(1.dp, bannerBorderColor, RoundedCornerShape(18.dp))
            ) {
                // Background Exchange Facade (BSE Dalal Street for India / NYSE Wall Street for US)
                Image(
                    painter = painterResource(id = indicesHeroImage),
                    contentDescription = "Exchange Indices",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // High-contrast gradient scrim (theme adaptive)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) listOf(
                                    Color(0xE6070A0F),
                                    Color(0x550B0E14),
                                    Color(0xF5070A0F)
                                ) else listOf(
                                    Color(0xF0FFFFFF),
                                    Color(0x66FFFFFF),
                                    Color(0xF5F8FAFC)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row: Exchange Pill & Live Pulse Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xCC141923) else Color(0xEEFFFFFF))
                                .border(1.dp, if (isDark) Color(0x33475569) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isIndian) FintechPrimary else (if (isDark) Color(0xFF38BDF8) else Color(0xFF0A58CA)))
                                )
                                Text(
                                    text = if (isIndian) "BOMBAY STOCK EXCHANGE" else "NEW YORK STOCK EXCHANGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Pulse indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0x2E00D09C) else Color(0xEEFFFFFF))
                                .border(1.dp, if (isDark) GainGreen.copy(alpha = 0.5f) else Color(0xFF00D09C), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) GainGreen else Color(0xFF00B386))
                                )
                                Text(
                                    text = "REAL-TIME PULSE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = if (isDark) GainGreen else Color(0xFF00875A),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Middle Info: Title encapsulated in frosted high-contrast pill for razor-sharp legibility
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0x800B0E14)
                                else Color(0xE6FFFFFF)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0x33475569) else Color(0xFFCBD5E1),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isIndian) "DALAL STREET BENCHMARKS" else "WALL STREET BENCHMARKS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = if (isIndian) {
                                    if (isDark) FintechPrimary else Color(0xFF00875A)
                                } else {
                                    if (isDark) Color(0xFF38BDF8) else Color(0xFF0A58CA)
                                },
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isIndian) "NIFTY 50 • SENSEX • BANK NIFTY" else "S&P 500 • NASDAQ • DOW JONES",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Bottom Row: Auto-Scrolling Benchmark Indices Cards
                    LazyRow(
                        state = indicesListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(repeatedIndices) { idx, index ->
                            Box(
                                modifier = Modifier
                                    .height(74.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            if (isDark) listOf(
                                                Color(0xD9161C28),
                                                Color(0xF00F141E)
                                            ) else listOf(
                                                Color(0xF5FFFFFF),
                                                Color(0xEEF1F5F9)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            if (isDark) listOf(
                                                Color(0x55FFFFFF),
                                                Color(0x2094A3B8),
                                                Color(0x0A222A3B)
                                            ) else listOf(
                                                Color(0xFFE2E8F0),
                                                Color(0xFFCBD5E1)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = index.name,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFF94A3B8) else TextSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = index.price,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isDark) Color.White else TextPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        val isPlaceholder = index.price == "--" || index.price.isBlank()
                                        StockticsBadge(
                                            text = if (isPlaceholder) "Syncing..." else index.change,
                                            variant = if (isPlaceholder) BadgeVariant.MUTED else if (index.isPositive) BadgeVariant.GAIN else BadgeVariant.LOSS,
                                            showTrendIcon = !isPlaceholder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // ITEM 3: NET PORTFOLIO VALUE CARD (BASED ON ACTIVE TOGGLE)
        // =====================================================================
        item {
            StockticsCard(
                cornerRadius = 16.dp,
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Net Portfolio Value",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "$currencySymbol ${String.format(java.util.Locale.US, "%,.2f", portfolioSummary.currentValue)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StockticsBadge(
                            text = "${portfolioSummary.activePositionsCount} Positions",
                            variant = if (portfolioSummary.activePositionsCount > 0) BadgeVariant.GAIN else BadgeVariant.MUTED
                        )
                        if (portfolioSummary.totalInvested > 0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Invested $currencySymbol ${String.format(java.util.Locale.US, "%,.2f", portfolioSummary.totalInvested)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassElevated(12.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 1-Day Return
                    val isOneDayPos = portfolioSummary.oneDayReturn >= 0
                    val oneDaySign = if (isOneDayPos) "+" else "-"
                    val oneDayValStr = String.format(java.util.Locale.US, "%,.2f", kotlin.math.abs(portfolioSummary.oneDayReturn))
                    val oneDayPctStr = String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(portfolioSummary.oneDayReturnPct))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "1-Day Return",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$oneDaySign$currencySymbol $oneDayValStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOneDayPos) GainGreen else LossRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "($oneDaySign$oneDayPctStr%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOneDayPos) GainGreen else LossRed,
                            maxLines = 1
                        )
                    }

                    // Vertical Hairline Divider
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(FintechBorder)
                    )

                    // 2. Total Returns
                    val totalReturns = portfolioSummary.unrealizedPnl + portfolioSummary.realizedPnl
                    val totalReturnsPct = if (portfolioSummary.totalInvested > 0) (totalReturns / portfolioSummary.totalInvested) * 100.0 else 0.0
                    val isTotalPos = totalReturns >= 0
                    val totalSign = if (isTotalPos) "+" else "-"
                    val totalValStr = String.format(java.util.Locale.US, "%,.2f", kotlin.math.abs(totalReturns))
                    val totalPctStr = String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(totalReturnsPct))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Returns",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalSign$currencySymbol $totalValStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTotalPos) GainGreen else LossRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "($totalSign$totalPctStr%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isTotalPos) GainGreen else LossRed,
                            maxLines = 1
                        )
                    }

                    // Vertical Hairline Divider
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(FintechBorder)
                    )

                    // 3. Unrealized P&L
                    val isUnrealizedPos = portfolioSummary.unrealizedPnl >= 0
                    val unrealizedSign = if (isUnrealizedPos) "+" else "-"
                    val unrealizedValStr = String.format(java.util.Locale.US, "%,.2f", kotlin.math.abs(portfolioSummary.unrealizedPnl))
                    val unrealizedPctStr = String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(portfolioSummary.unrealizedPnlPct))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Unrealized P&L",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$unrealizedSign$currencySymbol $unrealizedValStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnrealizedPos) GainGreen else LossRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "($unrealizedSign$unrealizedPctStr%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnrealizedPos) GainGreen else LossRed,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // =====================================================================
        // ITEM 4B: INDMONEY AUTO-SYNC PROMPT CARD (Visible when not synced)
        // =====================================================================
        if (!indmoneyStatus.connected) {
            item {
                HomeIndmoneySyncCard(
                    isVisible = !indmoneyStatus.connected,
                    isSyncing = isIndmoneySyncing || isOpeningAuth,
                    onConnectClick = {
                        scope.launch {
                            isOpeningAuth = true
                            try {
                                val url = IndmoneySyncManager.getAuthUrl(context)
                                if (!url.isNullOrBlank()) {
                                    indmoneyAuthUrl = url
                                }
                            } finally {
                                isOpeningAuth = false
                            }
                        }
                    }
                )
            }
        }

        // =====================================================================
        // ITEM 5: QUICK BUTTONS (SETUPS, HEATMAP, COMPARE, SECTORS)
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionPill(
                    icon = Icons.Rounded.AutoGraph,
                    title = "Setups",
                    subtitle = "33 Patterns",
                    onClick = { onNavigateToCharts(ChartsSubTab.PATTERNS) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.BarChart,
                    title = "Heatmap",
                    subtitle = "Treemap",
                    onClick = onOpenHeatmap,
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.AutoMirrored.Rounded.CompareArrows,
                    title = "Compare",
                    subtitle = "vs Peers",
                    onClick = onOpenCompare,
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.PieChart,
                    title = "Sectors",
                    subtitle = "11 GICS",
                    onClick = onOpenSectors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // =====================================================================
        // ITEM 5B: MARKET SPOTLIGHT COMMAND CENTER WITH AUTO-SCROLLING STOCK CARDS
        // =====================================================================
        item {
            val isDark = IsDarkTheme
            val bannerBorderColor = if (isDark) FintechBorder else Color(0xFFCBD5E1)



            val portfolioHeroImage = if (isDark) R.drawable.hero_home_portfolio else R.drawable.hero_home_portfolio_light

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(262.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDark) Color(0xFF0B0E14) else FintechSurface)
                    .border(1.dp, bannerBorderColor, RoundedCornerShape(18.dp))
            ) {
                // Background 3D Trading Desk Illustration (Dark / Light dynamic)
                Image(
                    painter = painterResource(id = portfolioHeroImage),
                    contentDescription = "Market Spotlight",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // High-contrast gradient scrim (theme adaptive)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) listOf(
                                    Color(0xE6070A0F), // Top scrim for badges
                                    Color(0x550B0E14), // Subtle mid scrim so 3D monitors glow through
                                    Color(0xF5070A0F)  // Deep bottom scrim for auto-scrolling stock cards
                                ) else listOf(
                                    Color(0xF0FFFFFF),
                                    Color(0x66FFFFFF),
                                    Color(0xF5F8FAFC)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row: Desk Badge & Live Breadth Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Desk Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xCC141923) else Color(0xEEFFFFFF))
                                .border(1.dp, if (isDark) Color(0x33475569) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isIndian) FintechPrimary else (if (isDark) Color(0xFF38BDF8) else Color(0xFF0A58CA)))
                                )
                                Text(
                                    text = if (isIndian) "DALAL STREET DESK" else "WALL STREET DESK",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            }
                        }

                        // Live Market Breadth indicator pill
                        val breadthText = if (isIndian) "34 ADV / 16 DEC (+1.2%)" else "312 ADV / 188 DEC (+0.8%)"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0x2E00D09C) else Color(0xEEFFFFFF))
                                .border(1.dp, if (isDark) GainGreen.copy(alpha = 0.5f) else Color(0xFF00D09C), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = if (isDark) GainGreen else Color(0xFF00875A),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = breadthText,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isDark) GainGreen else Color(0xFF00875A),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Middle Info: Category & Subtitle encapsulated in frosted high-contrast pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0x800B0E14)
                                else Color(0xE6FFFFFF)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0x33475569) else Color(0xFFCBD5E1),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isIndian) "DALAL STREET TITANS" else "TRILLION-DOLLAR CLUB",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = if (isIndian) {
                                    if (isDark) FintechPrimary else Color(0xFF00875A)
                                } else {
                                    if (isDark) Color(0xFF38BDF8) else Color(0xFF0A58CA)
                                }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isIndian) "NIFTY 50 • Mega-Cap Alpha Momentum" else "Magnificent 7 • Mega-Tech Leaders",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Bottom Row: Auto-Scrolling Live Stock Cards Carousel
                    LazyRow(
                        state = leaderScrollState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(repeatedLeaderStocks) { _, stock ->
                            val cleanSym = stock.symbol.replace(".NS", "").replace(".BO", "")
                            Box(
                                modifier = Modifier
                                    .width(172.dp)
                                    .height(62.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            if (isDark) listOf(
                                                Color(0xD9161C28),
                                                Color(0xF00F141E)
                                            ) else listOf(
                                                Color(0xF5FFFFFF),
                                                Color(0xEEF1F5F9)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            if (isDark) listOf(
                                                Color(0x55FFFFFF),
                                                Color(0x2094A3B8),
                                                Color(0x0A222A3B)
                                            ) else listOf(
                                                Color(0xFFE2E8F0),
                                                Color(0xFFCBD5E1)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onStockSelected(stock.symbol) }
                                    .fintechPressBounce()
                                    .padding(horizontal = 9.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StockCompanyLogo(
                                        symbol = cleanSym,
                                        logoUrl = stock.logoUrl,
                                        size = 30.dp
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cleanSym,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = stock.change,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (stock.isPositive) GainGreen else LossRed,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stock.price,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isDark) Color(0xFFE2E8F0) else TextSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // ITEM 6: MY WATCHLIST HEADER & ICON TOGGLE FILTERS
        // =====================================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Watchlist",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Categories synced with INDmoney",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                    StockticsBadge(
                        text = "${watchlistStocks.size}",
                        variant = BadgeVariant.MUTED
                    )
                }

                // Filter Bar: Left: [All Categories N] | Right: [ ↗ | ↘ ] Icon Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: All Categories Pill
                    val isAllActive = performanceFilter == WatchlistPerformanceFilter.ALL
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isAllActive) GainGreenBg else FintechSurface)
                            .border(1.dp, if (isAllActive) FintechPrimary else FintechBorder, RoundedCornerShape(20.dp))
                            .clickable { performanceFilter = WatchlistPerformanceFilter.ALL }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = null,
                                tint = if (isAllActive) FintechPrimary else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All Categories",
                                fontSize = 12.sp,
                                fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllActive) FintechPrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${categoryAnalytics.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllActive) FintechPrimary else TextTertiary
                            )
                        }
                    }

                    // Right: Icon-only Toggle Capsule for Top / Underperforming
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(20.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Performing Icon Toggle (TrendingUp)
                        val isTopActive = performanceFilter == WatchlistPerformanceFilter.TOP
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isTopActive) GainGreenBg else Color.Transparent)
                                .border(if (isTopActive) 1.dp else 0.dp, if (isTopActive) GainGreen else Color.Transparent, CircleShape)
                                .clickable {
                                    performanceFilter = if (isTopActive) WatchlistPerformanceFilter.ALL else WatchlistPerformanceFilter.TOP
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = "Top Performing",
                                tint = if (isTopActive) GainGreen else TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Underperforming Icon Toggle (TrendingDown)
                        val isUnderActive = performanceFilter == WatchlistPerformanceFilter.UNDER
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isUnderActive) LossRedBg else Color.Transparent)
                                .border(if (isUnderActive) 1.dp else 0.dp, if (isUnderActive) LossRed else Color.Transparent, CircleShape)
                                .clickable {
                                    performanceFilter = if (isUnderActive) WatchlistPerformanceFilter.ALL else WatchlistPerformanceFilter.UNDER
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                                contentDescription = "Underperforming",
                                tint = if (isUnderActive) LossRed else TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // ITEM 7: CATEGORY CARDS LIST (CLEAN, NO BOOKMARK ICON)
        // =====================================================================
        if (filteredCategories.isEmpty()) {
            item {
                StockticsCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (watchlistStocks.isEmpty()) "Watchlist is Empty" else "No Categories Match Filter",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (watchlistStocks.isEmpty())
                                "Your INDmoney synced watchlist will render here automatically."
                            else
                                "No watchlist categories match the selected trend filter today.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (watchlistStocks.isEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            StockticsPrimaryButton(
                                text = "Explore & Add Stocks",
                                onClick = onNavigateToExplore,
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredCategories, key = { it.name }) { cat ->
                val isExpanded = expandedCategory == cat.name

                StockticsCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = 14.dp
                ) {
                    // Category Header Row (Clean, bookmark icon removed)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCategory = if (isExpanded) null else cat.name
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Category Name & Stock Count directly
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${cat.count} ${if (cat.count == 1) "stock" else "stocks"}",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                maxLines = 1
                            )
                        }

                        // Right: Average Change Pill + Gain/Loss Badges + Chevron
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Avg % Change Pill
                            val sign = if (cat.isPositive) "+" else ""
                            val avgFormatted = String.format(java.util.Locale.US, "%.2f", cat.avgChange)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (cat.isPositive) GainGreenBg else LossRedBg)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (cat.isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                        contentDescription = null,
                                        tint = if (cat.isPositive) GainGreen else LossRed,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "~$sign$avgFormatted%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cat.isPositive) GainGreen else LossRed
                                    )
                                }
                            }

                            // Gainer / Loser Mini Indicators (7▲ 6▼)
                            if (cat.gainerCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GainGreenBg)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${cat.gainerCount}▲",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GainGreen
                                    )
                                }
                            }
                            if (cat.loserCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LossRedBg)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${cat.loserCount}▼",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LossRed
                                    )
                                }
                            }

                            // Expand/Collapse Chevron
                            Icon(
                                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quick-Glance Top & Bottom Performer Horizontal Cards (Visible without clicking dropdown)
                    if (cat.topStock != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WatchlistPerformerCard(
                                stock = cat.topStock,
                                badgeLabel = if (cat.stocks.size > 1) "TOP" else null,
                                badgeIsTop = true,
                                modifier = Modifier.weight(1f),
                                onClick = { onStockSelected(cat.topStock.symbol) }
                            )
                            if (cat.bottomStock != null && cat.bottomStock != cat.topStock) {
                                WatchlistPerformerCard(
                                    stock = cat.bottomStock,
                                    badgeLabel = "LOW",
                                    badgeIsTop = false,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onStockSelected(cat.bottomStock.symbol) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Expandable Stock Rows inside this Category
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(
                                color = FintechBorder,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "All Stocks",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextTertiary
                                )
                                Text(
                                    text = "${cat.count} total",
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                            }

                            cat.stocks.forEach { stock ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onStockSelected(stock.symbol) }
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        StockCompanyLogo(
                                            symbol = stock.symbol,
                                            initials = stock.symbol.take(2).uppercase(),
                                            logoUrl = stock.logoUrl,
                                            size = 32.dp
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = stock.symbol.replace(".NS", "").replace(".BO", ""),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = stock.name,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stock.price,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        StockticsBadge(
                                            text = stock.change,
                                            variant = if (stock.isPositive) BadgeVariant.GAIN else BadgeVariant.LOSS,
                                            showTrendIcon = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // In-App INDmoney OAuth Dialog
    indmoneyAuthUrl?.let { url ->
        IndmoneyAuthDialog(
            authUrl = url,
            onDismiss = { indmoneyAuthUrl = null },
            onAuthSuccess = {
                indmoneyAuthUrl = null
                scope.launch {
                    IndmoneySyncManager.performSync(context)
                    loadAllData()
                }
            }
        )
    }
}

/**
 * Modern Quick Action Pill Component.
 */
@Composable
private fun QuickActionPill(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current

    Box(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .liquidGlassElevated(cornerRadius = 16.dp)
            .clickable(onClick = onClick)
            .fintechPressBounce()
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (fintech.isDark) Color(0x662A3448) else Color(0x3300D09C))
                    .border(
                        1.dp,
                        if (fintech.isDark) Color(0x40FFFFFF) else Color(0x2000D09C),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = fintech.primary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = fintech.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = fintech.textSecondary
            )
        }
    }
}

/**
 * Quick-glance Top / Bottom performer horizontal preview card.
 * Styled after Tier-1 fintech horizontal asset cards (matching leaders carousel).
 */
@Composable
private fun WatchlistPerformerCard(
    stock: StockSearchItem,
    badgeLabel: String?,
    badgeIsTop: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current
    val cleanSym = stock.symbol.replace(".NS", "").replace(".BO", "")
    val isPos = stock.isPositive

    val badgeColor = if (badgeIsTop) GainGreen else if (isPos) WarningAmber else LossRed
    val badgeBg = if (badgeIsTop) GainGreenBg else if (isPos) WarningAmberBg else LossRedBg

    val cardBgBrush = if (fintech.isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xD9161C28),
                Color(0xF00F141E)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF1F5F9)
            )
        )
    }

    val cardBorderBrush = if (fintech.isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0x40FFFFFF),
                Color(0x1894A3B8),
                Color(0x0A222A3B)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0x3094A3B8),
                Color(0x15CBD5E1),
                Color(0x08E2E8F0)
            )
        )
    }

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBgBrush)
            .border(
                width = 1.dp,
                brush = cardBorderBrush,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .fintechPressBounce()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            StockCompanyLogo(
                symbol = cleanSym,
                logoUrl = stock.logoUrl,
                size = 28.dp
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // Row 1: Symbol & Change%
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cleanSym,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = fintech.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = stock.change,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isPos) GainGreen else LossRed,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Row 2: Price & TOP/LOW Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stock.price,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (fintech.isDark) Color(0xFFE2E8F0) else fintech.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (badgeLabel != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(badgeBg)
                                .padding(horizontal = 3.5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeColor
                            )
                        }
                    }
                }
            }
        }
    }
}
