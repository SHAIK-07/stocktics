package com.stocktics.presentation.heatmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.api.ApiHeatmapSector
import com.stocktics.data.api.ApiHeatmapStock
import com.stocktics.data.api.StockticsApiClient
import com.stocktics.data.portfolio.SupabasePortfolioClient
import com.stocktics.data.stocks.SupabaseWatchlistClient
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.StockticsSecondaryButton
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsPullToRefreshBox
import com.stocktics.ui.components.StockticsTextField
import com.stocktics.ui.components.fintechPressBounce
import com.stocktics.ui.components.fintechShimmer
import com.stocktics.ui.theme.LocalFintechColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Institutional Market Treemap Heatmap Screen.
 * Implements a recursive Squarified Treemap layout algorithm with 7-tier color gradient,
 * multi-market toggle (Nifty 50, S&P 500, Portfolio, Watchlist), sizing switcher,
 * sector grouping, multi-timeframe returns (1D, 1W, 1M, 1Y), and interactive tile inspection modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketHeatmapScreen(
    currentMarket: GlobalMarket,
    onStockSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    applyStatusBarsPadding: Boolean = true
) {
    val fintech = LocalFintechColors.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var activeMarketCode by remember(currentMarket) {
        mutableStateOf(if (currentMarket == GlobalMarket.INDIA) "IN" else "US")
    }
    var selectedPeriod by remember { mutableStateOf("1D") }
    val periods = listOf("1D", "1W", "1M", "1Y")

    // Sizing metric: "marketCap", "changePercent", "price"
    var sortBy by remember { mutableStateOf("marketCap") }
    var groupBySector by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val sectors = remember { mutableStateListOf<ApiHeatmapSector>() }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    // Bottom Sheet Inspection State
    var inspectedStock by remember { mutableStateOf<ApiHeatmapStock?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun loadData(fromRefresh: Boolean = false) {
        coroutineScope.launch {
            if (fromRefresh) {
                isRefreshing = true
            } else {
                isLoading = true
                sectors.clear()
            }
            hasError = false

            val rawSectors: List<ApiHeatmapSector> = when (activeMarketCode) {
                "PORTFOLIO" -> {
                    val holdings = try {
                        SupabasePortfolioClient.fetchHoldingsFromCloud(context)
                    } catch (_: Exception) {
                        emptyList()
                    }
                    if (holdings.isNotEmpty()) {
                        val stockList = holdings.map { h ->
                            val isIndian = h.market == "IN" || h.symbol.endsWith(".NS")
                            ApiHeatmapStock(
                                ticker = h.symbol,
                                name = h.name,
                                sector = if (isIndian) "Indian Equities" else "US Equities",
                                marketCap = max(h.currentPrice * h.qty * 1000.0, 10000.0),
                                price = h.currentPrice,
                                changePct = if (h.entryPrice > 0) ((h.currentPrice - h.entryPrice) / h.entryPrice) * 100.0 else 0.0,
                                peRatio = "--",
                                currencySymbol = h.currency
                            )
                        }
                        listOf(
                            ApiHeatmapSector(
                                sectorName = "Portfolio Holdings",
                                totalMarketCap = stockList.sumOf { it.marketCap },
                                avgChangePct = if (stockList.isNotEmpty()) stockList.map { it.changePct }.average() else 0.0,
                                stocks = stockList
                            )
                        )
                    } else {
                        // No holdings — empty list triggers empty state UI
                        emptyList()
                    }
                }
                "WATCHLIST" -> {
                    val isIndian = currentMarket == GlobalMarket.INDIA
                    val watch = try {
                        SupabaseWatchlistClient.fetchWatchlist(context, isIndian)
                    } catch (_: Exception) {
                        emptyList()
                    }
                    if (watch.isNotEmpty()) {
                        val catMap = mutableMapOf<String, MutableList<ApiHeatmapStock>>()
                        watch.forEach { w ->
                            val cat = w.category.ifBlank { "Watchlist" }
                            val list = catMap.getOrPut(cat) { mutableListOf() }
                            val priceD = w.price.replace(",", "").replace("₹", "").replace("$", "").trim().toDoubleOrNull() ?: 0.0
                            val chgD = w.change.replace("%", "").replace("+", "").trim().toDoubleOrNull() ?: 0.0
                            val isIndianSym = w.symbol.endsWith(".NS") || w.symbol.endsWith(".BO") || w.exchange.equals("NSE", ignoreCase = true)

                            list.add(
                                ApiHeatmapStock(
                                    ticker = w.symbol,
                                    name = w.name,
                                    sector = cat,
                                    marketCap = max(priceD * 500000.0, 100000.0),
                                    price = priceD,
                                    changePct = chgD,
                                    peRatio = "--",
                                    currencySymbol = if (isIndianSym) "₹" else "$"
                                )
                            )
                        }
                        catMap.map { (cat, stks) ->
                            ApiHeatmapSector(
                                sectorName = cat,
                                totalMarketCap = stks.sumOf { it.marketCap },
                                avgChangePct = if (stks.isNotEmpty()) stks.map { it.changePct }.average() else 0.0,
                                stocks = stks
                            )
                        }.sortedByDescending { it.totalMarketCap }
                    } else {
                        // No watchlist items — empty list triggers empty state UI
                        emptyList()
                    }
                }
                else -> {
                    val apiData = StockticsApiClient.fetchMarketHeatmap(activeMarketCode, selectedPeriod)
                    if (apiData.isEmpty()) {
                        // API returned no data — mark error for retry UI
                        hasError = true
                    }
                    apiData
                }
            }

            if (fromRefresh && rawSectors.isNotEmpty()) {
                sectors.clear()
            }
            sectors.addAll(rawSectors)
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(activeMarketCode, selectedPeriod) {
        loadData()
    }

    // Filter sectors & stocks by search query
    val cleanQuery = searchQuery.trim().lowercase()
    val filteredSectors = remember(sectors.toList(), cleanQuery) {
        if (cleanQuery.isEmpty()) {
            sectors.toList()
        } else {
            sectors.mapNotNull { sec ->
                val matching = sec.stocks.filter {
                    it.ticker.lowercase().contains(cleanQuery) || it.name.lowercase().contains(cleanQuery)
                }
                if (matching.isNotEmpty()) {
                    sec.copy(stocks = matching)
                } else null
            }
        }
    }

    val totalStocksCount = remember(filteredSectors) {
        filteredSectors.sumOf { it.stocks.size }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fintech.background)
            .then(if (applyStatusBarsPadding) Modifier.statusBarsPadding() else Modifier)
            .navigationBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(fintech.surfaceElevated)
                    .border(1.dp, fintech.border, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = fintech.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Market Treemap Heatmap",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = fintech.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StockticsBadge(
                        text = when (activeMarketCode) {
                            "IN" -> "NIFTY 50"
                            "US" -> "S&P 500"
                            "PORTFOLIO" -> "PORTFOLIO"
                            else -> "WATCHLIST"
                        },
                        variant = BadgeVariant.INFO
                    )
                }
                Text(
                    text = "Algorithmic squarified treemap capital concentration",
                    fontSize = 11.sp,
                    color = fintech.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Market Mode Selector Bar (IN, US, PORTFOLIO, WATCHLIST)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val marketModes = listOf(
                "IN" to "🇮🇳 Nifty 50",
                "US" to "🇺🇸 S&P 500",
                "PORTFOLIO" to "🛡️ Portfolio",
                "WATCHLIST" to "🔖 Watchlist"
            )
            items(marketModes) { (code, label) ->
                val isSelected = activeMarketCode == code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) fintech.primary else fintech.surfaceElevated)
                        .border(1.dp, if (isSelected) fintech.primary else fintech.border, RoundedCornerShape(10.dp))
                        .clickable { activeMarketCode = code }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0B0E14) else fintech.textSecondary
                    )
                }
            }
        }

        // Secondary Controls Bar: Sizing Metric & Sector Grouping Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sizing metric pill selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(fintech.surface)
                    .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeOptions = listOf(
                    "marketCap" to "Mkt Cap",
                    "changePercent" to "Daily %",
                    "price" to "Price"
                )
                sizeOptions.forEach { (key, lbl) ->
                    val isSel = sortBy == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) fintech.surfaceElevated else Color.Transparent)
                            .clickable { sortBy = key }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = lbl,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) fintech.textPrimary else fintech.textTertiary
                        )
                    }
                }
            }

            // Right Group: Sector toggle & Timeframe selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Group by Sector Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (groupBySector) fintech.surfaceElevated else fintech.surface)
                        .border(1.dp, if (groupBySector) fintech.primary.copy(alpha = 0.5f) else fintech.border, RoundedCornerShape(8.dp))
                        .clickable { groupBySector = !groupBySector }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = "Sectors",
                            tint = if (groupBySector) fintech.primary else fintech.textTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sectors",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (groupBySector) fintech.textPrimary else fintech.textTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Timeframe chips
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(fintech.surface)
                        .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    periods.forEach { p ->
                        val isSel = selectedPeriod == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) fintech.primary else Color.Transparent)
                                .clickable { selectedPeriod = p }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = p,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF0B0E14) else fintech.textTertiary
                            )
                        }
                    }
                }
            }
        }

        // Search Bar Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 3.dp)
        ) {
            StockticsTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "",
                placeholder = "Search ticker (e.g. RELIANCE, NVDA, TCS)...",
                leadingIcon = Icons.Rounded.Search,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Institutional 7-Tier Color Gradient Legend Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(fintech.surface)
                .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "≤ -3%",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = fintech.lossRed
            )

            // Gradient blocks
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Color(0xFFF04438),
                    Color(0xFFDC2626),
                    Color(0xFFB91C1C),
                    Color(0xFF1C2333),
                    Color(0xFF00875A),
                    Color(0xFF00B386),
                    Color(0xFF00D09C)
                ).forEach { col ->
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(col)
                    )
                }
            }

            Text(
                text = "≥ +3%",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = fintech.gainGreen
            )
        }

        // Main Content Area with Pull-To-Refresh
        StockticsPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { loadData(fromRefresh = true) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isLoading) {
            // Animated loading state — pulsing shimmer blocks
            val infiniteTransition = rememberInfiniteTransition(label = "heatmap_loading")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Loading indicator at top
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = fintech.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Fetching live market data...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = fintech.textSecondary.copy(alpha = alpha)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(18.dp)
                            .fintechShimmer(6.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .fintechShimmer(14.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (hasError && totalStocksCount == 0) {
            // Error state — API failed, show retry instead of dummy data
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                StockticsCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 24.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(fintech.lossRedBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WifiOff,
                                contentDescription = null,
                                tint = fintech.lossRed,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Unable to Load Market Data",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = fintech.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Live heatmap data could not be fetched. Check your connection and try again.",
                            fontSize = 12.sp,
                            color = fintech.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        StockticsPrimaryButton(
                            text = "Retry",
                            icon = Icons.Rounded.Refresh,
                            onClick = { loadData() }
                        )
                    }
                }
            }
        } else if (totalStocksCount == 0) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                StockticsCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 24.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(fintech.surfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (activeMarketCode == "PORTFOLIO") Icons.Rounded.Shield else Icons.Rounded.Bookmark,
                                contentDescription = null,
                                tint = fintech.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (cleanQuery.isNotEmpty()) "No Matching Securities" else "No Stocks in $activeMarketCode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = fintech.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (cleanQuery.isNotEmpty()) {
                                "No symbols match \"$searchQuery\". Clear the search filter to see full heatmap."
                            } else {
                                "Connect your broker account or add stocks to your watchlist to visualize your personal holdings."
                            },
                            fontSize = 12.sp,
                            color = fintech.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StockticsPrimaryButton(
                            text = if (cleanQuery.isNotEmpty()) "Clear Search Filter" else "Switch to 🇮🇳 Nifty 50",
                            onClick = {
                                if (cleanQuery.isNotEmpty()) {
                                    searchQuery = ""
                                } else {
                                    activeMarketCode = "IN"
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // Treemap Display
            if (groupBySector) {
                // Sector-Grouped Treemap List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredSectors, key = { it.sectorName }) { sector ->
                        SectorTreemapCard(
                            sector = sector,
                            sortBy = sortBy,
                            onTileClick = { inspectedStock = it }
                        )
                    }
                }
            } else {
                // Flat Treemap Display
                val allStocks = remember(filteredSectors) {
                    filteredSectors.flatMap { it.stocks }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp)
                ) {
                    item {
                        StockticsCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 12.dp
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "All Market Securities (${allStocks.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = fintech.textPrimary
                                    )
                                    StockticsBadge(
                                        text = "Sized by ${if (sortBy == "marketCap") "Market Cap" else if (sortBy == "price") "Price" else "Daily %"}",
                                        variant = BadgeVariant.INFO
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                FlatTreemapCanvas(
                                    stocks = allStocks,
                                    sortBy = sortBy,
                                    canvasHeightDp = 480,
                                    onTileClick = { inspectedStock = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Interactive Stock Inspection Bottom Sheet
    inspectedStock?.let { stock ->
        ModalBottomSheet(
            onDismissRequest = { inspectedStock = null },
            sheetState = sheetState,
            containerColor = fintech.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(fintech.border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Stock Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.ticker.replace(".NS", "").replace(".BO", ""),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = fintech.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StockticsBadge(
                                text = stock.sector,
                                variant = BadgeVariant.INFO
                            )
                        }
                        Text(
                            text = stock.name,
                            fontSize = 12.sp,
                            color = fintech.textSecondary
                        )
                    }

                    // % Change Pill
                    val isGain = stock.changePct >= 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isGain) fintech.gainGreenBg else fintech.lossRedBg)
                            .border(1.dp, if (isGain) fintech.gainGreen.copy(alpha = 0.3f) else fintech.lossRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isGain) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                contentDescription = null,
                                tint = if (isGain) fintech.gainGreen else fintech.lossRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isGain) "+" else ""}%.2f%%".format(stock.changePct),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isGain) fintech.gainGreen else fintech.lossRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price & Metric Cards Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        label = "CURRENT PRICE",
                        value = "${stock.currencySymbol}%.2f".format(stock.price),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "MARKET CAP",
                        value = TreemapLayoutEngine.formatLargeCurrency(stock.marketCap, stock.currencySymbol),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "P/E RATIO",
                        value = stock.peRatio,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StockticsSecondaryButton(
                        text = "Close",
                        onClick = { inspectedStock = null },
                        modifier = Modifier.weight(1f)
                    )
                    StockticsPrimaryButton(
                        text = "Deep Analysis",
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        onClick = {
                            val ticker = stock.ticker
                            inspectedStock = null
                            onStockSelected(ticker)
                        },
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(fintech.surfaceElevated)
            .border(1.dp, fintech.border, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fintech.textTertiary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = fintech.textPrimary,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectorTreemapCard(
    sector: ApiHeatmapSector,
    sortBy: String,
    onTileClick: (ApiHeatmapStock) -> Unit
) {
    val fintech = LocalFintechColors.current

    // Dynamically calculate canvas height based on stock count
    val canvasHeightDp = (140 + (sector.stocks.size / 3) * 28).coerceIn(140, 260)

    StockticsCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 12.dp
    ) {
        Column {
            // Sector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sector.sectorName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = fintech.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${sector.stocks.size})",
                        fontSize = 11.sp,
                        color = fintech.textTertiary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = TreemapLayoutEngine.formatLargeCurrency(sector.totalMarketCap, "$"),
                        fontSize = 10.sp,
                        color = fintech.textSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val isGain = sector.avgChangePct >= 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isGain) fintech.gainGreenBg else fintech.lossRedBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${if (isGain) "+" else ""}%.2f%%".format(sector.avgChangePct),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGain) fintech.gainGreen else fintech.lossRed,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proportional Squarified Treemap Area
            FlatTreemapCanvas(
                stocks = sector.stocks,
                sortBy = sortBy,
                canvasHeightDp = canvasHeightDp,
                onTileClick = onTileClick
            )
        }
    }
}

@Composable
private fun FlatTreemapCanvas(
    stocks: List<ApiHeatmapStock>,
    sortBy: String,
    canvasHeightDp: Int,
    onTileClick: (ApiHeatmapStock) -> Unit
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(canvasHeightDp.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val rects = remember(stocks, sortBy, widthPx, heightPx) {
            val nodes = stocks.map { st ->
                val nodeValue = when (sortBy) {
                    "price" -> max(st.price, 1.0)
                    "changePercent" -> max(abs(st.changePct), 0.1) * 1000.0
                    else -> max(st.marketCap, 1000.0)
                }
                TreemapNode(
                    id = st.ticker,
                    value = nodeValue,
                    data = st
                )
            }
            TreemapLayoutEngine.computeTreemap(nodes, widthPx, heightPx)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            rects.forEach { rect ->
                val stock = rect.data
                val rectWDp = with(density) { rect.w.toDp() }
                val rectHDp = with(density) { rect.h.toDp() }
                val tileBg = TreemapLayoutEngine.getTileColor(stock.changePct)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(rect.x.roundToInt(), rect.y.roundToInt()) }
                        .size(width = rectWDp, height = rectHDp)
                        .padding(1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(tileBg)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .clickable { onTileClick(stock) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        val sym = stock.ticker.replace(".NS", "").replace(".BO", "")

                        if (rectWDp >= 42.dp && rectHDp >= 26.dp) {
                            Text(
                                text = sym,
                                fontSize = if (rectWDp > 80.dp && rectHDp > 50.dp) 13.sp else 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${if (stock.changePct >= 0) "+" else ""}%.1f%%".format(stock.changePct),
                                fontSize = if (rectWDp > 80.dp && rectHDp > 50.dp) 11.sp else 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                fontFamily = FontFamily.Monospace
                            )
                        } else if (rectWDp >= 24.dp && rectHDp >= 18.dp) {
                            Text(
                                text = sym.take(4),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

