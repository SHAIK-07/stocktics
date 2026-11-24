package com.stocktics.presentation.watchlist

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.stocktics.ui.components.StockticsPullToRefreshBox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.stocks.StockSearchItem
import com.stocktics.data.stocks.SupabaseWatchlistClient
import com.stocktics.data.stocks.YahooFinanceClient
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.fintechPressBounce
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.LossRedBg
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class WatchlistCategoryFilter {
    ALL,
    TOP,
    UNDER
}

data class SyncedCategoryModel(
    val name: String,
    val stocks: List<StockSearchItem>,
    val count: Int,
    val avgChange: Double,
    val isPositive: Boolean,
    val gainerCount: Int,
    val loserCount: Int
)

/**
 * Dedicated INDmoney Synced Watchlist Screen.
 * Strictly displays the user's INDmoney-synced categories and stocks.
 * Does not allow manual logging/entry — purely synced intelligence.
 */
@Composable
fun MarketWatchlistScreen(
    currentMarket: GlobalMarket,
    onStockSelected: (String) -> Unit,
    onNavigateToExplore: () -> Unit = {},
    onScrollDirectionChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isIndian = currentMarket == GlobalMarket.INDIA

    val watchlistTrigger by SupabaseWatchlistClient.watchlistVersion.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf(WatchlistCategoryFilter.ALL) }
    var expandedCategoryName by remember { mutableStateOf<String?>(null) }

    // Load actual synced watchlist stocks from SQLite (synced from INDmoney / Supabase)
    var syncedStocks by remember(isIndian, watchlistTrigger) {
        mutableStateOf(StockticsDatabase.getWatchlist(context, isIndian = isIndian))
    }

    // Function to refresh and pull latest synced data from cloud
    suspend fun refreshSyncedData() {
        withContext(Dispatchers.IO) {
            try {
                val refreshed = SupabaseWatchlistClient.fetchWatchlist(context, isIndian = isIndian)
                withContext(Dispatchers.Main) {
                    syncedStocks = refreshed
                }
            } catch (_: Exception) {
                val cached = StockticsDatabase.getWatchlist(context, isIndian = isIndian)
                withContext(Dispatchers.Main) {
                    syncedStocks = cached
                }
            }
        }
    }

    LaunchedEffect(isIndian, watchlistTrigger) {
        isRefreshing = true
        try {
            refreshSyncedData()
        } finally {
            isRefreshing = false
        }
    }

    // Scroll state detection to compress navbar on scroll down, expand on scroll up
    val scrollState = rememberLazyListState()
    var lastIndex by remember { mutableIntStateOf(0) }
    var lastOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState) {
        snapshotFlow { Pair(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) }
            .collect { (idx, offset) ->
                if (idx == 0 && offset < 25) {
                    onScrollDirectionChanged(true)
                } else if (idx > lastIndex || (idx == lastIndex && offset > lastOffset + 14)) {
                    onScrollDirectionChanged(false)
                } else if (idx < lastIndex || (idx == lastIndex && offset < lastOffset - 14)) {
                    onScrollDirectionChanged(true)
                }
                lastIndex = idx
                lastOffset = offset
            }
    }

    // Group actual synced stocks by INDmoney category (exact mirror of Web App & Home Screen)
    val categoryList = remember(syncedStocks) {
        val groups = mutableMapOf<String, MutableList<StockSearchItem>>()
        syncedStocks.forEach { stock ->
            val cat = stock.category.trim().ifBlank { "General" }
            groups.getOrPut(cat) { mutableListOf() }.add(stock)
        }

        groups.map { (catName, stocks) ->
            var totalChange = 0.0
            var gainers = 0
            var losers = 0

            stocks.forEach { s ->
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
                totalChange += pct
                if (s.isPositive || pct >= 0) gainers++ else losers++
            }

            val avg = if (stocks.isNotEmpty()) totalChange / stocks.size else 0.0
            SyncedCategoryModel(
                name = catName,
                stocks = stocks,
                count = stocks.size,
                avgChange = avg,
                isPositive = avg >= 0,
                gainerCount = gainers,
                loserCount = losers
            )
        }.sortedByDescending { it.avgChange }
    }

    // Filter categories based on active filter button
    val filteredCategories = remember(categoryList, activeFilter) {
        when (activeFilter) {
            WatchlistCategoryFilter.ALL -> categoryList
            WatchlistCategoryFilter.TOP -> categoryList.filter { it.avgChange > 0 }.sortedByDescending { it.avgChange }
            WatchlistCategoryFilter.UNDER -> categoryList.filter { it.avgChange < 0 }.sortedBy { it.avgChange }
        }
    }

    StockticsPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                try {
                    refreshSyncedData()
                } finally {
                    isRefreshing = false
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .background(FintechBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =====================================================================
            // HEADER: Market Watchlist Title & INDmoney Sync Info
            // =====================================================================
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isIndian) "India Watchlist" else "US Watchlist",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Categories synced with INDmoney • Tap to expand (Pull down to refresh)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

        // =====================================================================
        // FILTER BAR: Left: [All Categories N] | Right: [ ↗ | ↘ ] Icon Toggle
        // =====================================================================
        if (categoryList.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // All Categories Pill
                    val isAllActive = activeFilter == WatchlistCategoryFilter.ALL
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isAllActive) GainGreenBg else FintechSurface)
                            .border(1.dp, if (isAllActive) FintechPrimary else FintechBorder, RoundedCornerShape(20.dp))
                            .clickable { activeFilter = WatchlistCategoryFilter.ALL }
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
                                text = "${categoryList.size}",
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
                        val isTopActive = activeFilter == WatchlistCategoryFilter.TOP
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isTopActive) GainGreenBg else Color.Transparent)
                                .border(if (isTopActive) 1.dp else 0.dp, if (isTopActive) GainGreen else Color.Transparent, CircleShape)
                            .clickable {
                                activeFilter = if (isTopActive) WatchlistCategoryFilter.ALL else WatchlistCategoryFilter.TOP
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
                        val isUnderActive = activeFilter == WatchlistCategoryFilter.UNDER
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isUnderActive) LossRedBg else Color.Transparent)
                                .border(if (isUnderActive) 1.dp else 0.dp, if (isUnderActive) LossRed else Color.Transparent, CircleShape)
                            .clickable {
                                activeFilter = if (isUnderActive) WatchlistCategoryFilter.ALL else WatchlistCategoryFilter.UNDER
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
        // CATEGORY ACCORDION LIST: Real INDmoney Categories
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (syncedStocks.isEmpty()) "No INDmoney Watchlist Synced" else "No Categories Match Filter",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (syncedStocks.isEmpty())
                                "Your synced INDmoney categories and stocks will automatically appear here."
                            else
                                "No watchlist categories match the selected trend filter today.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredCategories, key = { it.name }) { cat ->
                val isExpanded = expandedCategoryName == cat.name

                StockticsCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    contentPadding = 14.dp
                ) {
                    // Category Header Row (Accordion Trigger)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCategoryName = if (isExpanded) null else cat.name
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Category Name & Count
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${cat.count} ${if (cat.count == 1) "stock" else "stocks"}",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                maxLines = 1
                            )
                        }

                        // Right: Average % Change Pill + Chevron
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                                        text = "$sign$avgFormatted%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cat.isPositive) GainGreen else LossRed
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = if (isExpanded) FintechPrimary else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Expandable Dropdown Content: Stocks under this synced category
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(
                                color = FintechBorder,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            cat.stocks.forEach { stock ->
                                SyncedWatchlistStockRow(
                                    stock = stock,
                                    onClick = { onStockSelected(stock.symbol) }
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

/**
 * Individual Stock Row inside an INDmoney Synced Category Dropdown.
 * Purely displays synced stock data (No manual add/remove/star button).
 */
@Composable
private fun SyncedWatchlistStockRow(
    stock: StockSearchItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground.copy(alpha = 0.5f))
            .border(0.8.dp, FintechBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Logo & Company Name / Symbol
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            StockCompanyLogo(
                symbol = stock.symbol,
                initials = stock.symbol.take(2),
                logoUrl = stock.logoUrl,
                size = 36.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stock.symbol.removeSuffix(".NS").removeSuffix(".BO"),
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

        // Right: Price + Change Badge
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
