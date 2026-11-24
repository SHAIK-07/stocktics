package com.stocktics.presentation.compare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.api.ApiComparisonStock
import com.stocktics.data.api.ComparisonPoint
import com.stocktics.data.api.ComparisonStat
import com.stocktics.data.api.CorrelationPair
import com.stocktics.data.api.FullComparisonData
import com.stocktics.data.api.StockticsApiClient
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Institutional 12-Color Palette matching web application PALETTE
val COMPARISON_PALETTE = listOf(
    Color(0xFF00D09C), // Groww Mint Emerald
    Color(0xFF1455FE), // INDmoney Tech Blue
    Color(0xFFF59E0B), // Amber
    Color(0xFFEC4899), // Rose Pink
    Color(0xFF8B5CF6), // Violet
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFF10B981), // Emerald
    Color(0xFF6366F1), // Indigo
    Color(0xFFEAB308), // Yellow
    Color(0xFF14B8A6), // Teal
    Color(0xFFF43F5E)  // Red-Rose
)

private val DEFAULT_SYMBOLS_US = listOf("NVDA", "AAPL", "MSFT", "AMZN", "TSLA")
private val DEFAULT_SYMBOLS_IN = listOf("RELIANCE.NS", "TCS.NS", "HDFCBANK.NS", "ICICIBANK.NS", "INFY.NS")

/**
 * Multi-Stock Comparison Workstation Screen.
 * Features:
 * - Quick presets (Top US Tech, Nifty Leaders)
 * - Ticker management bar with color dots and live add search
 * - Interactive multi-asset Compose Canvas chart (0% baseline vs raw price)
 * - Drag/scrub crosshair tracker HUD
 * - Pairwise Pearson Correlation Matrix ($r$ values with co-movement badges)
 * - Performance & Volatility Leaderboard Table
 * - Side-by-side fundamental scorecard matrix
 */
@Composable
fun StockComparisonScreen(
    currentMarket: GlobalMarket,
    onStockSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    applyStatusBarsPadding: Boolean = true
) {
    val fintech = LocalFintechColors.current
    val coroutineScope = rememberCoroutineScope()

    val selectedTickers = remember(currentMarket) {
        mutableStateListOf<String>().apply {
            if (currentMarket == GlobalMarket.INDIA) {
                addAll(DEFAULT_SYMBOLS_IN)
            } else {
                addAll(DEFAULT_SYMBOLS_US)
            }
        }
    }

    var period by remember { mutableStateOf("6mo") } // 1mo, 3mo, 6mo, 1y, 5y
    var scaleMode by remember { mutableStateOf("percentage") } // "percentage" | "price"

    var comparisonData by remember { mutableStateOf<FullComparisonData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Search and add ticker state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSearchBox by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Highlighted correlation pair
    var highlightedCorrelationPair by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Touch scrub hover index
    var scrubIndex by remember { mutableStateOf<Int?>(null) }

    fun loadData(fromRefresh: Boolean = false) {
        if (selectedTickers.isEmpty()) {
            comparisonData = null
            isLoading = false
            isRefreshing = false
            return
        }
        coroutineScope.launch {
            if (fromRefresh) {
                isRefreshing = true
            } else {
                isLoading = true
            }
            try {
                val res = StockticsApiClient.fetchFullComparison(selectedTickers.toList(), period)
                comparisonData = res ?: generateComparisonFullFallback(selectedTickers.toList(), period)
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(selectedTickers.toList(), period) {
        loadData()
    }

    // Debounced search for adding new stocks
    fun onQueryChange(q: String) {
        searchQuery = q
        searchJob?.cancel()
        if (q.isBlank()) {
            searchResults = emptyList()
            return
        }
        searchJob = coroutineScope.launch {
            delay(250)
            val cleanQ = q.trim().uppercase()
            val list = if (currentMarket == GlobalMarket.INDIA) {
                listOf("RELIANCE.NS", "TCS.NS", "HDFCBANK.NS", "ICICIBANK.NS", "INFY.NS", "SBIN.NS", "BHARTIARTL.NS", "ITC.NS", "KOTAKBANK.NS", "LT.NS", "TATAMOTORS.NS", "WIPRO.NS", "MARUTI.NS", "SUNPHARMA.NS")
            } else {
                listOf("AAPL", "MSFT", "NVDA", "AMZN", "GOOGL", "META", "TSLA", "AVGO", "JPM", "LLY", "V", "MA", "UNH", "COST", "NFLX", "AMD")
            }
            searchResults = list.filter { it.contains(cleanQ) && !selectedTickers.contains(it) }.take(5)
        }
    }

    fun addSymbol(sym: String) {
        val clean = sym.trim().uppercase()
        if (!selectedTickers.contains(clean)) {
            if (selectedTickers.size >= 12) return
            selectedTickers.add(clean)
        }
        searchQuery = ""
        searchResults = emptyList()
        showSearchBox = false
    }

    // Dynamic color map for selected symbols
    val symbolColors = remember(selectedTickers.toList()) {
        selectedTickers.mapIndexed { idx, sym ->
            sym to COMPARISON_PALETTE[idx % COMPARISON_PALETTE.size]
        }.toMap()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fintech.background)
            .then(if (applyStatusBarsPadding) Modifier.statusBarsPadding() else Modifier)
            .navigationBarsPadding()
    ) {
        // Top Navigation & Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
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
                        text = "Stock Comparison",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = fintech.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StockticsBadge(
                        text = "WORKSTATION",
                        variant = BadgeVariant.INFO
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Normalized multi-asset returns & Pearson correlation",
                    fontSize = 11.sp,
                    color = fintech.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Ticker Chips Bar with Presets & Add button
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Presets
            item {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(fintech.surfaceElevated)
                        .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                        .clickable {
                            selectedTickers.clear()
                            selectedTickers.addAll(DEFAULT_SYMBOLS_US)
                        }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🇺🇸 Tech",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = fintech.textPrimary
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(fintech.surfaceElevated)
                        .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                        .clickable {
                            selectedTickers.clear()
                            selectedTickers.addAll(DEFAULT_SYMBOLS_IN)
                        }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🇮🇳 Nifty",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = fintech.textPrimary
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(fintech.border)
                )
            }

            items(selectedTickers) { sym ->
                val col = symbolColors[sym] ?: fintech.primary
                val isHighlighted = highlightedCorrelationPair?.let { it.first == sym || it.second == sym } ?: false

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isHighlighted) col.copy(alpha = 0.2f) else fintech.surfaceElevated)
                        .border(1.dp, if (isHighlighted) col else fintech.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(col)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = sym.replace(".NS", "").replace(".BO", ""),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = fintech.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onStockSelected(sym) }
                    )
                    if (selectedTickers.size > 2) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove",
                            tint = fintech.textTertiary,
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { selectedTickers.remove(sym) }
                        )
                    }
                }
            }

            // + Add Stock Button
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(fintech.surface)
                        .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                        .clickable { showSearchBox = !showSearchBox }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add",
                            tint = fintech.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = fintech.primary
                        )
                    }
                }
            }
        }

        // Add Stock Search Box Animated Visibility
        AnimatedVisibility(
            visible = showSearchBox,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                StockticsTextField(
                    value = searchQuery,
                    onValueChange = { onQueryChange(it) },
                    label = "",
                    placeholder = "Type symbol (e.g. MSFT, HDFCBANK.NS)...",
                    leadingIcon = Icons.Rounded.Search,
                    modifier = Modifier.fillMaxWidth()
                )

                if (searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        searchResults.forEach { match ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(fintech.surfaceElevated)
                                    .border(1.dp, fintech.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable { addSymbol(match) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+ ${match.replace(".NS", "")}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fintech.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        StockticsPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { loadData(fromRefresh = true) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isLoading && comparisonData == null) {
                Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .fintechShimmer(14.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .fintechShimmer(14.dp)
                )
            }
        } else {
            val data = comparisonData
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Main Interactive Multi-Line Chart Card
                item {
                    StockticsCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 12.dp
                    ) {
                        Column {
                            // Chart Toolbar: Timeframe & Scale Mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Timeframe selector (1MO..5Y)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(fintech.surfaceElevated)
                                        .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                                        .padding(2.dp)
                                ) {
                                    listOf("1mo", "3mo", "6mo", "1y", "5y").forEach { p ->
                                        val isSel = period == p
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) fintech.primary else Color.Transparent)
                                                .clickable { period = p }
                                                .padding(horizontal = 7.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = p.uppercase(Locale.US),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color(0xFF0B0E14) else fintech.textTertiary
                                            )
                                        }
                                    }
                                }

                                // Scale Mode (Normalized % vs Price)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(fintech.surfaceElevated)
                                        .border(1.dp, fintech.border, RoundedCornerShape(8.dp))
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (scaleMode == "percentage") fintech.surface else Color.Transparent)
                                            .clickable { scaleMode = "percentage" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Norm %",
                                            fontSize = 10.sp,
                                            fontWeight = if (scaleMode == "percentage") FontWeight.Bold else FontWeight.Medium,
                                            color = if (scaleMode == "percentage") fintech.textPrimary else fintech.textTertiary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (scaleMode == "price") fintech.surface else Color.Transparent)
                                            .clickable { scaleMode = "price" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Price",
                                            fontSize = 10.sp,
                                            fontWeight = if (scaleMode == "price") FontWeight.Bold else FontWeight.Medium,
                                            color = if (scaleMode == "price") fintech.textPrimary else fintech.textTertiary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Compose Canvas Multi-Line Chart
                            if (data != null && data.dates.isNotEmpty() && data.series.isNotEmpty()) {
                                MultiLineComparisonChart(
                                    data = data,
                                    symbolColors = symbolColors,
                                    scaleMode = scaleMode,
                                    highlightedPair = highlightedCorrelationPair,
                                    scrubIndex = scrubIndex,
                                    onScrubChange = { scrubIndex = it }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val activeIndex = (scrubIndex ?: (data.dates.size - 1)).coerceIn(0, data.dates.size - 1)
                                val dateStr = data.dates.getOrNull(activeIndex) ?: ""

                                ScrubTelemetryHUD(
                                    date = dateStr,
                                    series = data.series,
                                    dateIndex = activeIndex,
                                    symbolColors = symbolColors,
                                    scaleMode = scaleMode,
                                    isScrubbing = scrubIndex != null,
                                    onResetScrub = { scrubIndex = null }
                                )
                            }
                        }
                    }
                }

                // 2. Pearson Correlation Matrix Card
                item {
                    val correlations = data?.correlations ?: emptyList()
                    StockticsCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 14.dp
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timeline,
                                        contentDescription = null,
                                        tint = fintech.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pairwise Pearson Correlation",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = fintech.textPrimary
                                    )
                                }
                                StockticsBadge(
                                    text = "Daily Returns (r)",
                                    variant = BadgeVariant.INFO
                                )
                            }
                            Text(
                                text = "Co-movement between assets (-1.0 to +1.0). Tap any pair to illuminate on the chart.",
                                fontSize = 11.sp,
                                color = fintech.textSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (correlations.isEmpty()) {
                                Text(
                                    text = "Add at least 2 stocks to compute Pearson correlation.",
                                    fontSize = 11.sp,
                                    color = fintech.textTertiary
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    correlations.take(8).forEach { pair ->
                                        val isPairActive = highlightedCorrelationPair?.let {
                                            (it.first == pair.symbol1 && it.second == pair.symbol2) ||
                                            (it.first == pair.symbol2 && it.second == pair.symbol1)
                                        } ?: false

                                        CorrelationRowItem(
                                            pair = pair,
                                            c1 = symbolColors[pair.symbol1] ?: fintech.primary,
                                            c2 = symbolColors[pair.symbol2] ?: fintech.primary,
                                            isActive = isPairActive,
                                            onClick = {
                                                highlightedCorrelationPair = if (isPairActive) null else Pair(pair.symbol1, pair.symbol2)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Performance & Volatility Leaderboard Table
                item {
                    val stats = data?.stats ?: emptyList()
                    StockticsCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 14.dp
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.BarChart,
                                        contentDescription = null,
                                        tint = fintech.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Performance Leaderboard",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = fintech.textPrimary
                                    )
                                }
                                StockticsBadge(
                                    text = "${period.uppercase(Locale.US)} TIMEFRAME",
                                    variant = BadgeVariant.INFO
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val scrollState = rememberScrollState()
                            Column(modifier = Modifier.horizontalScroll(scrollState)) {
                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.width(110.dp)) {
                                        Text("SECURITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fintech.textTertiary)
                                    }
                                    Box(modifier = Modifier.width(85.dp)) {
                                        Text("RETURN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fintech.textTertiary)
                                    }
                                    Box(modifier = Modifier.width(80.dp)) {
                                        Text("PEAK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fintech.textTertiary)
                                    }
                                    Box(modifier = Modifier.width(80.dp)) {
                                        Text("MAX DD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fintech.textTertiary)
                                    }
                                    Box(modifier = Modifier.width(80.dp)) {
                                        Text("VOLATILITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fintech.textTertiary)
                                    }
                                }

                                Box(modifier = Modifier.width(435.dp).height(1.dp).background(fintech.border))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Table Rows
                                stats.forEachIndexed { idx, st ->
                                    val col = symbolColors[st.symbol] ?: fintech.primary
                                    val isGain = st.periodReturn >= 0
                                    Row(
                                        modifier = Modifier
                                            .padding(vertical = 5.dp)
                                            .clickable { onStockSelected(st.symbol) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.width(110.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "#${idx + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = fintech.textTertiary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(col))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = st.symbol.replace(".NS", ""),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = fintech.textPrimary,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1
                                            )
                                        }

                                        Box(modifier = Modifier.width(85.dp)) {
                                            Text(
                                                text = "${if (isGain) "+" else ""}%.2f%%".format(st.periodReturn),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isGain) fintech.gainGreen else fintech.lossRed,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Box(modifier = Modifier.width(80.dp)) {
                                            Text(
                                                text = "+%.2f%%".format(st.peakGain),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = fintech.gainGreen,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Box(modifier = Modifier.width(80.dp)) {
                                            Text(
                                                text = "%.2f%%".format(st.maxDrawdown),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = fintech.lossRed,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Box(modifier = Modifier.width(80.dp)) {
                                            Text(
                                                text = "%.1f%%".format(st.annualizedVolatility),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = fintech.textSecondary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Fundamental Metrics Scorecard Matrix Table
                item {
                    val stockItems = data?.stocks ?: emptyList()
                    if (stockItems.isNotEmpty()) {
                        StockticsCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 14.dp
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.CompareArrows,
                                            contentDescription = null,
                                            tint = fintech.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Fundamental Scorecard",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = fintech.textPrimary
                                        )
                                    }
                                    StockticsBadge(
                                        text = "QUANT MATRIX",
                                        variant = BadgeVariant.INFO
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val scoreScroll = rememberScrollState()
                                Column(modifier = Modifier.horizontalScroll(scoreScroll)) {
                                    // Ticker Header
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.width(120.dp)) {
                                            Text("METRIC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fintech.textTertiary)
                                        }
                                        stockItems.forEach { st ->
                                            val col = symbolColors[st.ticker] ?: fintech.primary
                                            Box(
                                                modifier = Modifier
                                                    .width(105.dp)
                                                    .clickable { onStockSelected(st.ticker) }
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(col))
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        text = st.ticker.replace(".NS", ""),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = fintech.primary,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.width((120 + stockItems.size * 105).dp).height(1.dp).background(fintech.border))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    ScorecardMetricRow("Current Price", stockItems.map { "$%.2f".format(it.price) })
                                    ScorecardMetricRow("Day Return", stockItems.map { "${if (it.changePct >= 0) "+" else ""}%.2f%%".format(it.changePct) })
                                    ScorecardMetricRow("Quant Score", stockItems.map { "${it.quantScore}/100" })
                                    ScorecardMetricRow("P/E Ratio", stockItems.map { it.peRatio })
                                    ScorecardMetricRow("P/B Ratio", stockItems.map { it.pbRatio })
                                    ScorecardMetricRow("ROE %", stockItems.map { it.roe })
                                    ScorecardMetricRow("Debt/Equity", stockItems.map { it.debtToEquity })
                                    ScorecardMetricRow("Sales Growth", stockItems.map { it.revenueGrowth })
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

@Composable
private fun ScorecardMetricRow(
    label: String,
    values: List<String>
) {
    val fintech = LocalFintechColors.current
    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(120.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fintech.textSecondary
            )
        }
        values.forEach { v ->
            Box(modifier = Modifier.width(105.dp)) {
                Text(
                    text = v,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = fintech.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun CorrelationRowItem(
    pair: CorrelationPair,
    c1: Color,
    c2: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current
    val coeff = pair.coefficient

    val badgeBg = when {
        coeff >= 0.7 -> fintech.gainGreenBg
        coeff >= 0.3 -> fintech.gainGreenBg.copy(alpha = 0.5f)
        coeff <= -0.3 -> fintech.lossRedBg
        else -> fintech.surfaceElevated
    }
    val badgeTextColor = when {
        coeff >= 0.7 -> fintech.gainGreen
        coeff >= 0.3 -> fintech.gainGreen
        coeff <= -0.3 -> fintech.lossRed
        else -> fintech.textSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) fintech.primary.copy(alpha = 0.12f) else fintech.surfaceElevated)
            .border(1.dp, if (isActive) fintech.primary else fintech.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Symbols with color dots
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(c1))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = pair.symbol1.replace(".NS", ""),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fintech.textPrimary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "↔",
                fontSize = 11.sp,
                color = fintech.textTertiary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(c2))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = pair.symbol2.replace(".NS", ""),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fintech.textPrimary,
                fontFamily = FontFamily.Monospace
            )
        }

        // Coefficient badge & strength
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${if (coeff > 0) "+" else ""}%.3f".format(coeff),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeTextColor,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${pair.strength} ${pair.direction}",
                fontSize = 9.sp,
                color = fintech.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScrubTelemetryHUD(
    date: String,
    series: Map<String, List<ComparisonPoint>>,
    dateIndex: Int,
    symbolColors: Map<String, Color>,
    scaleMode: String,
    isScrubbing: Boolean,
    onResetScrub: () -> Unit
) {
    val fintech = LocalFintechColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(fintech.surfaceElevated)
            .border(1.dp, fintech.border, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅 $date",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = fintech.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
                if (isScrubbing) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StockticsBadge(
                        text = "INSPECTING",
                        variant = BadgeVariant.INFO
                    )
                }
            }
            if (isScrubbing) {
                Text(
                    text = "Reset",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = fintech.primary,
                    modifier = Modifier
                        .clickable { onResetScrub() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            } else {
                Text(
                    text = "Touch chart to inspect",
                    fontSize = 10.sp,
                    color = fintech.textTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            series.keys.forEach { sym ->
                val pts = series[sym] ?: emptyList()
                val pt = pts.getOrNull(dateIndex) ?: pts.lastOrNull()
                if (pt != null) {
                    val col = symbolColors[sym] ?: fintech.primary
                    val isGain = pt.percent >= 0
                    val cleanSym = sym.replace(".NS", "").replace(".BO", "")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(fintech.surface)
                            .border(1.dp, fintech.border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(col)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = cleanSym,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = fintech.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (scaleMode == "price") {
                                val currSymbol = if (sym.endsWith(".NS") || sym.endsWith(".BO")) "₹" else "$"
                                "$currSymbol%.2f".format(pt.price)
                            } else {
                                "${if (isGain) "+" else ""}%.2f%%".format(pt.percent)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (scaleMode == "price") fintech.textPrimary else (if (isGain) fintech.gainGreen else fintech.lossRed),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiLineComparisonChart(
    data: FullComparisonData,
    symbolColors: Map<String, Color>,
    scaleMode: String,
    highlightedPair: Pair<String, String>?,
    scrubIndex: Int?,
    onScrubChange: (Int?) -> Unit
) {
    val dates = data.dates
    val series = data.series
    if (dates.isEmpty() || series.isEmpty()) return

    val density = LocalDensity.current
    val paddingLeft = with(density) { 54.dp.toPx() }
    val paddingRight = with(density) { 18.dp.toPx() }
    val paddingTop = with(density) { 16.dp.toPx() }
    val paddingBottom = with(density) { 28.dp.toPx() }

    // Calculate Min & Max Y
    var minY = Float.MAX_VALUE
    var maxY = -Float.MAX_VALUE

    series.values.forEach { pts ->
        pts.forEach { p ->
            val v = (if (scaleMode == "percentage") p.percent else p.price).toFloat()
            if (v < minY) minY = v
            if (v > maxY) maxY = v
        }
    }

    if (minY == Float.MAX_VALUE) minY = -10f
    if (maxY == -Float.MAX_VALUE) maxY = 10f

    val yRange = max(maxY - minY, 1f)
    minY -= yRange * 0.08f
    maxY += yRange * 0.08f

    val safeDivisor = max(1, dates.size - 1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .pointerInput(dates.size) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val chartW = size.width - paddingLeft - paddingRight
                    if (chartW > 0) {
                        val relX = (down.position.x - paddingLeft).coerceIn(0f, chartW)
                        val idx = ((relX / chartW) * safeDivisor).roundToInt()
                        onScrubChange(idx.coerceIn(0, safeDivisor))
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            break
                        }
                        val chartW = size.width - paddingLeft - paddingRight
                        if (chartW > 0) {
                            val dragX = (change.position.x - paddingLeft).coerceIn(0f, chartW)
                            val dragIdx = ((dragX / chartW) * safeDivisor).roundToInt()
                            onScrubChange(dragIdx.coerceIn(0, safeDivisor))
                        }
                        change.consume()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom
            if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            // Horizontal Y-Grid lines (4 intervals)
            val yTicks = 4
            for (i in 0..yTicks) {
                val tickVal = minY + (i.toFloat() / yTicks) * (maxY - minY)
                val yPos = paddingTop + chartHeight - ((tickVal - minY) / (maxY - minY)) * chartHeight

                drawLine(
                    color = Color(0xFF222A3B),
                    start = Offset(paddingLeft, yPos),
                    end = Offset(size.width - paddingRight, yPos),
                    strokeWidth = 1f,
                    pathEffect = dashedEffect
                )

                // Label
                val lbl = if (scaleMode == "percentage") {
                    "${if (tickVal >= 0) "+" else ""}%.1f%%".format(tickVal)
                } else {
                    "%.0f".format(tickVal)
                }

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 24f
                        typeface = android.graphics.Typeface.MONOSPACE
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    drawText(lbl, paddingLeft - 10f, yPos + 8f, paint)
                }
            }

            // Zero Baseline line (for percentage mode)
            if (scaleMode == "percentage" && minY <= 0f && maxY >= 0f) {
                val zeroY = paddingTop + chartHeight - ((0f - minY) / (maxY - minY)) * chartHeight
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(paddingLeft, zeroY),
                    end = Offset(size.width - paddingRight, zeroY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                )
            }

            // Vertical X-Grid lines & date labels
            val xStep = max(1, dates.size / 4)
            for (i in 0 until dates.size step xStep) {
                val xPos = paddingLeft + (i.toFloat() / safeDivisor) * chartWidth
                val dateLabel = dates[i].takeLast(5) // MM-DD

                drawLine(
                    color = Color(0xFF222A3B).copy(alpha = 0.5f),
                    start = Offset(xPos, paddingTop),
                    end = Offset(xPos, paddingTop + chartHeight),
                    strokeWidth = 1f
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 22f
                        typeface = android.graphics.Typeface.MONOSPACE
                        textAlign = when {
                            i == 0 -> android.graphics.Paint.Align.LEFT
                            i + xStep >= dates.size -> android.graphics.Paint.Align.RIGHT
                            else -> android.graphics.Paint.Align.CENTER
                        }
                    }
                    drawText(dateLabel, xPos, paddingTop + chartHeight + 24f, paint)
                }
            }

            // Multi-Stock Line Paths
            series.forEach { (sym, pts) ->
                if (pts.isEmpty()) return@forEach
                val col = symbolColors[sym] ?: Color(0xFF00D09C)

                val isHighlighted = highlightedPair?.let { it.first == sym || it.second == sym } ?: false
                val isDimmed = highlightedPair != null && !isHighlighted

                val path = Path()
                var started = false

                pts.forEachIndexed { idx, p ->
                    val v = (if (scaleMode == "percentage") p.percent else p.price).toFloat()
                    val x = paddingLeft + (idx.toFloat() / safeDivisor) * chartWidth
                    val y = paddingTop + chartHeight - ((v - minY) / (maxY - minY)) * chartHeight

                    if (!started) {
                        path.moveTo(x, y)
                        started = true
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = col.copy(alpha = if (isDimmed) 0.2f else 1f),
                    style = Stroke(
                        width = if (isHighlighted) 3.5f.dp.toPx() else 2f.dp.toPx()
                    )
                )

                // Scrub Circle Indicator for each line
                scrubIndex?.let { sIdx ->
                    val p = pts.getOrNull(sIdx)
                    if (p != null) {
                        val v = (if (scaleMode == "percentage") p.percent else p.price).toFloat()
                        val x = paddingLeft + (sIdx.toFloat() / safeDivisor) * chartWidth
                        val y = paddingTop + chartHeight - ((v - minY) / (maxY - minY)) * chartHeight

                        drawCircle(
                            color = col,
                            radius = 4.5f.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color(0xFF0B0E14),
                            radius = 2.5f.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            // Hover / Scrub Vertical Crosshair Line
            scrubIndex?.let { sIdx ->
                val x = paddingLeft + (sIdx.toFloat() / safeDivisor) * chartWidth
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(x, paddingTop),
                    end = Offset(x, paddingTop + chartHeight),
                    strokeWidth = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            }
        }
    }
}

// Full Fallback Data Generator when offline
private fun generateComparisonFullFallback(symbols: List<String>, period: String): FullComparisonData {
    val datesCount = when (period) {
        "1mo" -> 22
        "3mo" -> 65
        "6mo" -> 130
        "1y" -> 250
        else -> 300
    }

    val dates = (0 until datesCount).map { i ->
        val day = (i % 28) + 1
        val month = ((i / 28) % 12) + 1
        "2024-%02d-%02d".format(month, day)
    }

    val series = mutableMapOf<String, List<ComparisonPoint>>()
    val stats = mutableListOf<ComparisonStat>()

    symbols.forEachIndexed { sIdx, sym ->
        val basePrice = 120.0 + sIdx * 50.0
        val trend = 0.05 + (sIdx % 3) * 0.04
        val pts = mutableListOf<ComparisonPoint>()
        var curPrice = basePrice

        dates.forEachIndexed { dIdx, dt ->
            val noise = (Math.sin((dIdx + sIdx * 7).toDouble()) * 0.02)
            val pctReturn = (trend * (dIdx.toDouble() / datesCount)) + noise
            curPrice = basePrice * (1.0 + pctReturn)
            pts.add(
                ComparisonPoint(
                    date = dt,
                    timestamp = 1704067200000L + dIdx * 86400000L,
                    price = curPrice,
                    percent = pctReturn * 100.0
                )
            )
        }
        series[sym] = pts

        val finalReturn = pts.last().percent
        val peak = pts.maxOf { it.percent }
        val maxDD = pts.minOf { it.percent }
        stats.add(
            ComparisonStat(
                symbol = sym,
                periodReturn = finalReturn,
                peakGain = peak,
                maxDrawdown = maxDD,
                annualizedVolatility = 18.0 + (sIdx % 4) * 4.5
            )
        )
    }

    val correlations = mutableListOf<CorrelationPair>()
    for (i in symbols.indices) {
        for (j in i + 1 until symbols.size) {
            val r = 0.55 + ((i + j) % 5) * 0.08
            correlations.add(
                CorrelationPair(
                    symbol1 = symbols[i],
                    symbol2 = symbols[j],
                    coefficient = r,
                    strength = if (r >= 0.7) "Strong" else "Moderate",
                    direction = "Positive"
                )
            )
        }
    }

    val stocks = symbols.mapIndexed { idx, sym ->
        ApiComparisonStock(
            ticker = sym,
            name = sym.replace(".NS", ""),
            price = 150.0 + idx * 80.0,
            changePct = 1.2 - idx * 0.8,
            peRatio = "${22 + idx * 4}.5",
            pbRatio = "${4 + idx}.2",
            roe = "${18 + idx * 3}%",
            debtToEquity = "${0.1 + idx * 0.15}",
            revenueGrowth = "+${12 + idx * 2}%",
            quantScore = 84 + idx * 2
        )
    }

    return FullComparisonData(
        dates = dates,
        series = series,
        stats = stats.sortedByDescending { it.periodReturn },
        correlations = correlations.sortedByDescending { it.coefficient },
        stocks = stocks
    )
}
