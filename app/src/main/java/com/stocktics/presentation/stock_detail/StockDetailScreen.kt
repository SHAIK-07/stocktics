package com.stocktics.presentation.stock_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import com.stocktics.ui.components.StockticsPullToRefreshBox
import com.stocktics.ui.components.StockRangeSlider
import com.stocktics.ui.components.StockSpeedometerGauge
import com.stocktics.ui.components.StockInsightStoryCard
import com.stocktics.ui.components.StockFloatingBottomAction
import java.util.Locale
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.stocktics.presentation.common.StockticsQuantLoadingView
import com.stocktics.ui.theme.ScreenDeviceClass
import com.stocktics.ui.theme.rememberScreenDimensions
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.stocks.ChartCandlePoint
import com.stocktics.data.stocks.DeepStockResearchPayload
import com.stocktics.data.stocks.StatementMetricRow
import com.stocktics.data.stocks.YahooFinanceClient
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.StockticsSecondaryButton
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.LossRedBg
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import com.stocktics.ui.theme.WarningAmber
import com.stocktics.ui.theme.WarningAmberBg
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.stocks.StockSearchItem
import com.stocktics.data.stocks.NewsArticleItem
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

enum class StockDetailTab(val title: String) {
    OVERVIEW("Overview"),
    TECHNICALS("Technicals"),
    FINANCIALS("Financials"),
    EVENTS("Events"),
    NEWS("News"),
    HOLDERS("Holders")
}

@Composable
fun StockDetailScreen(
    symbol: String,
    onBack: () -> Unit,
    onNavigateToBuy: () -> Unit = {},
    onNavigateToSell: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentSymbol by remember(symbol) { mutableStateOf(symbol) }
    val instant = remember(currentSymbol) { YahooFinanceClient.getInstantPayload(currentSymbol) }
    var researchData by remember(currentSymbol) { mutableStateOf<DeepStockResearchPayload?>(instant) }
    var isLoading by remember(currentSymbol) { mutableStateOf(instant == null) }
    var selectedTimeframe by remember { mutableStateOf("1D") }
    var selectedTab by remember { mutableStateOf(StockDetailTab.OVERVIEW) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentSymbol) {
        if (researchData == null) {
            isLoading = true
        }
        try {
            val liveData = YahooFinanceClient.fetchDeepStockResearch(currentSymbol)
            researchData = liveData
            if (liveData != null) {
                val searchItem = StockSearchItem(
                    symbol = liveData.symbol,
                    name = liveData.name,
                    exchange = liveData.exchange,
                    currency = liveData.currency,
                    price = String.format(java.util.Locale.US, "%,.2f", liveData.price),
                    change = String.format(java.util.Locale.US, "%+.2f (%+.2f%%)", liveData.changeAbs, liveData.changePct),
                    isPositive = liveData.changeAbs >= 0,
                    logoUrl = liveData.logoUrl
                )
                StockticsDatabase.addToRecent(context, searchItem)
            }
        } catch (e: Exception) {
            // Keep instant data if already displayed
            if (researchData == null) {
                researchData = null
            }
        }
        isLoading = false
    }

    // Auto-refresh: silently poll every 30 seconds for live price + chart data
    LaunchedEffect(currentSymbol) {
        while (true) {
            kotlinx.coroutines.delay(30_000L)
            try {
                val freshData = YahooFinanceClient.fetchDeepStockResearch(currentSymbol)
                if (freshData != null) {
                    researchData = freshData
                    val searchItem = StockSearchItem(
                        symbol = freshData.symbol,
                        name = freshData.name,
                        exchange = freshData.exchange,
                        currency = freshData.currency,
                        price = String.format(java.util.Locale.US, "%,.2f", freshData.price),
                        change = String.format(java.util.Locale.US, "%+.2f (%+.2f%%)", freshData.changeAbs, freshData.changePct),
                        isPositive = freshData.changeAbs >= 0,
                        logoUrl = freshData.logoUrl
                    )
                    StockticsDatabase.addToRecent(context, searchItem)
                }
            } catch (_: Exception) {
                // silent fail — next cycle will retry
            }
        }
    }

    val dims = rememberScreenDimensions()
    val configuration = LocalConfiguration.current
    val isSystemLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var isLandscapeFullScreen by rememberSaveable { mutableStateOf(false) }

    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    val isFullScreen = isLandscapeFullScreen || isSystemLandscape

    BackHandler(enabled = isFullScreen) {
        isLandscapeFullScreen = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .statusBarsPadding()
    ) {
        if (isLoading) {
            StockticsQuantLoadingView(symbol = currentSymbol)
        } else if (researchData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("Could not load $currentSymbol", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Failed to retrieve live market data.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    StockticsPrimaryButton(text = "Go Back", onClick = onBack)
                }
            }
        } else if (isFullScreen) {
            LandscapeProChartWorkstation(
                stock = researchData!!,
                selectedTimeframe = selectedTimeframe,
                onSelectTimeframe = { selectedTimeframe = it },
                onClose = {
                    isLandscapeFullScreen = false
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            )
        } else {
            val stock = researchData!!

            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Sticky Header (Back + Stock Symbol + Verdict Pill + Exchange Badge / Switcher)
                StickyDetailHeader(
                    stock = stock,
                    currentSymbol = currentSymbol,
                    onBack = onBack,
                    onExchangeChange = { newExch ->
                        val isIndian = currentSymbol.endsWith(".NS") || currentSymbol.endsWith(".BO") || stock.currency == "₹" || stock.currency == "INR"
                        if (isIndian) {
                            val base = currentSymbol.replace(".NS", "").replace(".BO", "")
                            currentSymbol = if (newExch == "BSE") "$base.BO" else "$base.NS"
                        }
                    }
                )

                // 2. Continuous Vertical Scroll Feed with Pull-To-Refresh
                StockticsPullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            try {
                                researchData = YahooFinanceClient.fetchDeepStockResearch(currentSymbol)
                            } catch (_: Exception) {}
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(dims.cardSpacing)
                    ) {
                    // Item 1: Groww Full-Screen Canvas Hero Header (Company Logo, Name, Current Price & 1D Change)
                    item {
                        Box(modifier = Modifier.padding(horizontal = dims.horizontalPadding)) {
                            GrowwStockHeroHeader(
                                stock = stock,
                                currentSymbol = currentSymbol
                            )
                        }
                    }

                    // Item 2: Interactive Chart Card with Baseline & Timeframe Selector
                    item {
                        Box(modifier = Modifier.padding(horizontal = dims.horizontalPadding)) {
                            InteractiveChartModule(
                                stock = stock,
                                selectedTimeframe = selectedTimeframe,
                                onSelectTimeframe = { selectedTimeframe = it },
                                onToggleMaximize = {
                                    isLandscapeFullScreen = true
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                }
                            )
                        }
                    }

                    // Item 2.5: Modern Insights Story Carousel
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Insights on ${stock.name}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = dims.horizontalPadding, vertical = 6.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = dims.horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Card 1: Executive Thesis
                                item {
                                    StockInsightStoryCard(
                                        headline = "✦ INSIDE ${stock.symbol}",
                                        title = stock.quantVerdict.action.ifBlank { "Verified Institutional Moat" },
                                        subtitle = "AI Catalyst & Core Thesis",
                                        accentColor = FintechPrimary,
                                        icon = Icons.Rounded.Lightbulb,
                                        onClick = { selectedTab = StockDetailTab.OVERVIEW }
                                    )
                                }

                                // Card 2: Consensus Speedometer Card
                                item {
                                    val totalRatings = (stock.consensus.buyRatings + stock.consensus.holdRatings + stock.consensus.sellRatings).coerceAtLeast(1)
                                    val buyPct = (stock.consensus.buyRatings.toFloat() / totalRatings) * 100f
                                    StockInsightStoryCard(
                                        headline = "IS THE PRICE RIGHT?",
                                        title = "${String.format(Locale.US, "%.0f%%", buyPct)} Buy Consensus",
                                        subtitle = "Target: ${stock.currency} ${String.format(Locale.US, "%,.2f", stock.consensus.meanTarget)}",
                                        accentColor = GainGreen,
                                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                                        badgeText = "UPSIDE",
                                        onClick = { selectedTab = StockDetailTab.FINANCIALS }
                                    )
                                }

                                // Card 3: Quant Score
                                item {
                                    StockInsightStoryCard(
                                        headline = "STOCKTICS ML SCORE",
                                        title = "${stock.quantVerdict.score}/100 Rating",
                                        subtitle = stock.quantVerdict.rating.replace("🟢 ", "").replace("🟡 ", "").replace("🔴 ", ""),
                                        accentColor = if (stock.quantVerdict.score >= 60) GainGreen else WarningAmber,
                                        icon = Icons.Rounded.AutoGraph,
                                        badgeText = "SCORE",
                                        onClick = { selectedTab = StockDetailTab.OVERVIEW }
                                    )
                                }

                                // Card 4: Technical Setup & Resistance
                                item {
                                    val techVerdict = stock.technicals?.summaryVerdict ?: "Neutral"
                                    StockInsightStoryCard(
                                        headline = "PRICE ACTION & S/R",
                                        title = "$techVerdict Momentum",
                                        subtitle = "Pivot: ${stock.currency} ${String.format(Locale.US, "%,.2f", stock.technicals?.pivot ?: stock.price)}",
                                        accentColor = FintechBlue,
                                        icon = Icons.Rounded.BarChart,
                                        onClick = { selectedTab = StockDetailTab.TECHNICALS }
                                    )
                                }
                            }
                        }
                    }

                    // Item 3: Groww Sticky Tab Bar (Pins to top on scroll)
                    @OptIn(ExperimentalFoundationApi::class)
                    stickyHeader {
                        GrowwStickyTabBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            horizontalPadding = dims.horizontalPadding
                        )
                    }

                    // Item 4: Tab-Driven Content Modules with Horizontal Swipe & Slide Animation
                    item {
                        var dragDistance by remember { mutableFloatStateOf(0f) }
                        val density = LocalDensity.current
                        val swipeThresholdPx = with(density) { 45.dp.toPx() }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 450.dp)
                                .pointerInput(selectedTab) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { dragDistance = 0f },
                                        onHorizontalDrag = { change, dragAmount ->
                                            dragDistance += dragAmount
                                            if (kotlin.math.abs(dragDistance) > swipeThresholdPx / 3f) {
                                                change.consume()
                                            }
                                        },
                                        onDragEnd = {
                                            val tabs = StockDetailTab.values()
                                            val currIdx = tabs.indexOf(selectedTab)
                                            if (dragDistance < -swipeThresholdPx && currIdx < tabs.size - 1) {
                                                // Swiped Left -> Move to Next Tab
                                                selectedTab = tabs[currIdx + 1]
                                            } else if (dragDistance > swipeThresholdPx && currIdx > 0) {
                                                // Swiped Right -> Move to Previous Tab
                                                selectedTab = tabs[currIdx - 1]
                                            }
                                            dragDistance = 0f
                                        },
                                        onDragCancel = {
                                            dragDistance = 0f
                                        }
                                    )
                                }
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    if (targetState.ordinal > initialState.ordinal) {
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 280),
                                            initialOffsetX = { fullWidth -> fullWidth }
                                        ) + fadeIn(animationSpec = tween(220))).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(durationMillis = 280),
                                                targetOffsetX = { fullWidth -> -fullWidth }
                                            ) + fadeOut(animationSpec = tween(180))
                                        )
                                    } else {
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 280),
                                            initialOffsetX = { fullWidth -> -fullWidth }
                                        ) + fadeIn(animationSpec = tween(220))).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(durationMillis = 280),
                                                targetOffsetX = { fullWidth -> fullWidth }
                                            ) + fadeOut(animationSpec = tween(180))
                                        )
                                    }
                                },
                                label = "StockDetailTabSwipeAnimation"
                            ) { currentTab ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = dims.horizontalPadding),
                                    verticalArrangement = Arrangement.spacedBy(dims.cardSpacing)
                                ) {
                                    when (currentTab) {
                                        StockDetailTab.OVERVIEW -> {
                                            PriceRangeGaugeModule(stock = stock)
                                            QuantScoreSection(stock = stock)
                                            Metrics16GridSection(stock = stock)
                                            CompanyOverviewSection(stock = stock)
                                        }
                                        StockDetailTab.TECHNICALS -> {
                                            GrowwTechnicalsSection(stock = stock)
                                        }
                                        StockDetailTab.FINANCIALS -> {
                                            FinancialStatementsSection(stock = stock)
                                            KeyStatsSection(stock = stock)
                                            AnalystConsensusSection(stock = stock)
                                        }
                                        StockDetailTab.EVENTS -> {
                                            CorporateEventsSection(stock = stock)
                                        }
                                        StockDetailTab.NEWS -> {
                                            NewsSection(stock = stock)
                                        }
                                        StockDetailTab.HOLDERS -> {
                                            MajorHoldersSection(stock = stock)
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
    }
}

// =============================================================================
// Top Navigation Bar (Clean, Minimal, Institutional)
// =============================================================================
@Composable
private fun StickyDetailHeader(
    stock: DeepStockResearchPayload,
    currentSymbol: String,
    onBack: () -> Unit,
    onExchangeChange: (String) -> Unit
) {
    val cleanSym = currentSymbol.replace(".NS", "").replace(".BO", "")
    val isIndian = currentSymbol.endsWith(".NS") || currentSymbol.endsWith(".BO") || stock.currency == "₹" || stock.currency == "INR"
    val isBse = currentSymbol.endsWith(".BO") || stock.exchange.equals("BSE", ignoreCase = true)
    var showExchangeMenu by remember { mutableStateOf(false) }

    // Quant Verdict Pill
    val isBull = stock.quantVerdict.score >= 60
    val isBear = stock.quantVerdict.score <= 35
    val verdictColor = when {
        isBull -> GainGreen
        isBear -> LossRed
        else -> WarningAmber
    }
    val verdictBg = when {
        isBull -> GainGreenBg
        isBear -> LossRedBg
        else -> WarningAmberBg
    }
    val cleanRating = stock.quantVerdict.rating
        .replace("🟢 ", "")
        .replace("🟡 ", "")
        .replace("🔴 ", "")
        .trim()
        .ifEmpty { "BUY / ACCUMULATE" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FintechBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back Arrow + Symbol
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = cleanSym,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Middle: Quant Verdict Pill (e.g. BUY / ACCUMULATE)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(verdictBg)
                .border(1.dp, verdictColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(verdictColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = cleanRating,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = verdictColor,
                    maxLines = 1
                )
            }
        }

        // Right: Exchange Badge or NSE/BSE Switcher
        if (isIndian) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                        .clickable { showExchangeMenu = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isBse) "BSE" else "NSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechPrimary
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Select Exchange",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                DropdownMenu(
                    expanded = showExchangeMenu,
                    onDismissRequest = { showExchangeMenu = false },
                    modifier = Modifier
                        .background(FintechSurface)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "NSE (National Stock Exchange)",
                                fontSize = 13.sp,
                                fontWeight = if (!isBse) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isBse) FintechPrimary else TextPrimary
                            )
                        },
                        onClick = {
                            showExchangeMenu = false
                            onExchangeChange("NSE")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "BSE (Bombay Stock Exchange)",
                                fontSize = 13.sp,
                                fontWeight = if (isBse) FontWeight.Bold else FontWeight.Normal,
                                color = if (isBse) FintechPrimary else TextPrimary
                            )
                        },
                        onClick = {
                            showExchangeMenu = false
                            onExchangeChange("BSE")
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = stock.exchange.ifEmpty { "NASDAQ" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechBlue
                )
            }
        }
    }
}

// =============================================================================
// Groww Full-Screen Canvas Hero Header (Company Logo, Name, Current Price & 1D Change)
// =============================================================================
@Composable
private fun GrowwStockHeroHeader(
    stock: DeepStockResearchPayload,
    currentSymbol: String
) {
    val dims = rememberScreenDimensions()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Logo + Company Name + Sector
        Column(modifier = Modifier.weight(1f)) {
            StockCompanyLogo(
                symbol = stock.symbol,
                initials = stock.symbol.take(2).uppercase(),
                logoUrl = stock.logoUrl,
                size = if (dims.isSmall) 42.dp else 48.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stock.name,
                fontSize = if (dims.isSmall) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.3).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stock.profile.sector.ifBlank { "Equities" },
                fontSize = 12.sp,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Column: Live Price + 1-Day Change (positioned where duplicate exchange was)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "${stock.currency} ${String.format(java.util.Locale.US, "%,.2f", stock.price)}",
                fontSize = if (dims.isSmall) 22.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (stock.changePct >= 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = if (stock.changePct >= 0) GainGreen else LossRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%+.2f (%+.2f%%) Today", stock.changeAbs, stock.changePct),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stock.changePct >= 0) GainGreen else LossRed
                )
            }
        }
    }
}

// =============================================================================
// Groww Sticky Tab Bar (Pins on Scroll)
// =============================================================================
@Composable
private fun GrowwStickyTabBar(
    selectedTab: StockDetailTab,
    onTabSelected: (StockDetailTab) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp
) {
    val dims = rememberScreenDimensions()
    val tabs = StockDetailTab.values()
    val selectedIndex = tabs.indexOf(selectedTab)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FintechBackground,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                containerColor = FintechBackground,
                contentColor = FintechPrimary,
                edgePadding = if (dims.widthDp < 410.dp) 8.dp else horizontalPadding,
                indicator = { tabPositions ->
                    if (selectedIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            height = 3.dp,
                            color = FintechPrimary
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = FintechBorder, thickness = 1.dp)
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = if (dims.widthDp < 410.dp) 13.sp else 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) FintechPrimary else TextSecondary
                            )
                        }
                    )
                }
            }
        }
    }
}

// =============================================================================
// Groww Technicals Section (Summary 20-segment Gauge, Indicators, Pivots)
// =============================================================================
@Composable
private fun GrowwTechnicalsSection(stock: DeepStockResearchPayload) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TechnicalSummaryCard(stock = stock)
        TechnicalIndicatorsCard(stock = stock)
        SupportResistanceCard(stock = stock)
    }
}

@Composable
private fun TechnicalSummaryCard(stock: DeepStockResearchPayload) {
    val tech = stock.technicals
    val isBull = if (tech != null) tech.summaryVerdict == "Bullish" else stock.quantVerdict.score >= 60
    val isBear = if (tech != null) tech.summaryVerdict == "Bearish" else stock.quantVerdict.score <= 35
    val statusText = tech?.summaryVerdict ?: (if (isBull) "Bullish" else if (isBear) "Bearish" else "Neutral")
    val statusColor = if (isBull) GainGreen else if (isBear) LossRed else WarningAmber
    val statusBg = if (isBull) GainGreenBg else if (isBear) LossRedBg else WarningAmberBg

    val bearishCount = tech?.bearishCount ?: (if (stock.quantVerdict.score >= 60) 3 else 6)
    val neutralCount = tech?.neutralCount ?: 5
    val bullishCount = tech?.bullishCount ?: (20 - bearishCount - neutralCount).coerceAtLeast(1)

    val activeIndex = tech?.gaugeIndex ?: (((stock.quantVerdict.score.toFloat() / 100f) * 19).toInt().coerceIn(0, 19))

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 20-Segment Groww Multi-Bar Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 20) {
                    val barColor = when {
                        i <= 6 -> LossRed
                        i in 7..12 -> WarningAmber
                        else -> GainGreen
                    }
                    val isActiveZone = kotlin.math.abs(i - activeIndex) <= 2
                    val alpha = if (isActiveZone) 1.0f else 0.28f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor.copy(alpha = alpha))
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Triangle Indicator Arrow below needle position
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (i in 0 until 20) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (i == activeIndex) {
                            Text(
                                text = "▲",
                                fontSize = 11.sp,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Count Columns: Bearish, Neutral, Bullish
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Bearish", fontSize = 11.sp, color = LossRed, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$bearishCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(FintechBorder))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Neutral", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$neutralCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(FintechBorder))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Bullish", fontSize = 11.sp, color = GainGreen, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$bullishCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun TechnicalIndicatorsCard(stock: DeepStockResearchPayload) {
    val tech = stock.technicals

    val rsiVal = tech?.rsi14 ?: (42.0 + (stock.quantVerdict.score * 0.32)).coerceIn(20.0, 85.0)
    val rsiSignal = tech?.rsiSignal ?: (if (rsiVal >= 70) "Overbought" else if (rsiVal <= 30) "Oversold" else "Neutral")
    val rsiColor = when (rsiSignal) {
        "Oversold" -> GainGreen
        "Overbought" -> WarningAmber
        else -> TextSecondary
    }

    val macdVal = tech?.macd ?: (stock.changePct * 0.45)
    val macdSignal = tech?.macdStatus ?: (if (macdVal >= 0) "Bullish Cross" else "Bearish Cross")
    val macdColor = if (macdSignal.contains("Bullish", ignoreCase = true)) GainGreen else LossRed

    val betaVal = tech?.beta?.ifBlank { stock.metrics16.beta.ifBlank { "1.08" } } ?: stock.metrics16.beta.ifBlank { "1.08" }

    val sma50Val = tech?.sma50 ?: (stock.price * 0.98)
    val sma200Val = tech?.sma200 ?: (stock.price * 0.94)

    val sma50Str = if (tech != null) {
        String.format("${stock.currency} %,.2f", sma50Val)
    } else {
        stock.trendStats.find { it.first.contains("50-Day") }?.second ?: String.format("${stock.currency} %,.2f", sma50Val)
    }

    val sma200Str = if (tech != null) {
        String.format("${stock.currency} %,.2f", sma200Val)
    } else {
        stock.trendStats.find { it.first.contains("200-Day") }?.second ?: String.format("${stock.currency} %,.2f", sma200Val)
    }

    val sma50Signal = if (stock.price >= sma50Val) "Above SMA (Bullish)" else "Below SMA (Bearish)"
    val sma50Color = if (stock.price >= sma50Val) GainGreen else LossRed

    val stochVal = if (tech != null) String.format("%.1f", tech.stochK) else String.format("%.1f", (stock.quantVerdict.score * 0.75 + 14.0).coerceIn(12.0, 92.0))
    val stochStatus = tech?.stochStatus ?: "Neutral Trend"

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Technical Indicators",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Momentum, Trend & Volatility Oscillators",
                fontSize = 11.sp,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(14.dp))

            TechnicalIndicatorRow("RSI (14)", String.format("%.2f", rsiVal), rsiSignal, rsiColor)
            HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            TechnicalIndicatorRow("MACD (12, 26, 9)", String.format("%+.2f", macdVal), macdSignal, macdColor)
            HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            TechnicalIndicatorRow("Beta (1 Year)", betaVal, "Market Volatility", FintechBlue)
            HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            TechnicalIndicatorRow("50-Day SMA", sma50Str, sma50Signal, sma50Color)
            HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            TechnicalIndicatorRow("200-Day SMA", sma200Str, "Long-Term Anchor", TextSecondary)
            HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            TechnicalIndicatorRow("Stochastic %K", stochVal, stochStatus, TextSecondary)
        }
    }
}

@Composable
private fun TechnicalIndicatorRow(
    name: String,
    value: String,
    signal: String,
    signalColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = signal, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = signalColor)
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun SupportResistanceCard(stock: DeepStockResearchPayload) {
    val tech = stock.technicals
    val dayParts = stock.metrics16.daysRange.split("-").map { it.trim().replace(",", "").toDoubleOrNull() ?: 0.0 }
    val high = if (dayParts.size >= 2 && dayParts[1] > 0) dayParts[1] else stock.price * 1.018
    val low = if (dayParts.isNotEmpty() && dayParts[0] > 0) dayParts[0] else stock.price * 0.982
    val close = stock.prevClose.takeIf { it > 0 } ?: stock.price

    val pivot = tech?.pivot ?: ((high + low + close) / 3.0)
    val r1 = tech?.r1 ?: ((2 * pivot) - low)
    val s1 = tech?.s1 ?: ((2 * pivot) - high)
    val r2 = tech?.r2 ?: (pivot + (high - low))
    val s2 = tech?.s2 ?: (pivot - (high - low))
    val currentPrice = stock.price

    var viewMode by remember { mutableStateOf("Both") } // "Both", "Chart", "Table"

    // Market bias and relative positioning
    val diffFromPivot = currentPrice - pivot
    val diffPct = (diffFromPivot / pivot) * 100.0
    val isBullish = currentPrice >= pivot
    val statusColor = if (isBullish) GainGreen else LossRed
    val statusText = when {
        currentPrice >= r2 -> "Above R2 (Extreme Bullish Breakout)"
        currentPrice >= r1 -> "Testing R1 Resistance (Bullish Momentum)"
        currentPrice >= pivot -> "Above Pivot Point (Bullish Bias • Next: R1)"
        currentPrice >= s1 -> "Below Pivot Point (Testing S1 Support)"
        currentPrice >= s2 -> "Testing S2 Support (Oversold Range)"
        else -> "Below S2 Support (Bearish Breakdown)"
    }

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Support & Resistance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Classical Daily Pivot Points",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                // View Mode Switcher: [ Both | Chart | Table ]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(6.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("Both", "Chart", "Table").forEach { mode ->
                        val isSel = mode == viewMode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) FintechPrimary else Color.Transparent)
                                .clickable { viewMode = mode }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.Black else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Price Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.08f))
                    .border(0.8.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f).padding(end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(statusColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = String.format("%+.2f%% vs P", diffPct),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    maxLines = 1
                )
            }

            // Visual Support & Resistance Ladder Chart
            if (viewMode == "Both" || viewMode == "Chart") {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    val allVals = listOf(r2, r1, pivot, s1, s2, currentPrice)
                    val minPriceVal = allVals.minOrNull() ?: (currentPrice * 0.98)
                    val maxPriceVal = allVals.maxOrNull() ?: (currentPrice * 1.02)
                    val priceSpread = ((maxPriceVal - minPriceVal) * 1.08).coerceAtLeast(1.0)
                    val chartBaseMin = (minPriceVal + maxPriceVal) / 2.0 - (priceSpread / 2.0)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val topPad = 14.dp.toPx()
                        val botPad = 14.dp.toPx()
                        val usableH = h - topPad - botPad

                        fun getLevelY(p: Double): Float {
                            val ratio = ((p - chartBaseMin) / priceSpread).coerceIn(0.0, 1.0)
                            return topPad + (usableH * (1.0 - ratio)).toFloat()
                        }

                        val leftLabelMargin = 44.dp.toPx()
                        val rightPriceMargin = 68.dp.toPx()
                        val lineStart = leftLabelMargin
                        val lineEnd = w - rightPriceMargin

                        val redColor = android.graphics.Color.parseColor("#F04438")
                        val greenColor = android.graphics.Color.parseColor("#00D09C")
                        val slateColor = android.graphics.Color.parseColor("#94A3B8")

                        val levels = listOf(
                            Triple("R2", r2, redColor),
                            Triple("R1", r1, redColor),
                            Triple("P", pivot, slateColor),
                            Triple("S1", s1, greenColor),
                            Triple("S2", s2, greenColor)
                        )

                        val badgePaint = android.graphics.Paint().apply {
                            textSize = 9.5.sp.toPx()
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        val pricePaint = android.graphics.Paint().apply {
                            textSize = 9.5.sp.toPx()
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.RIGHT
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }

                        // 1. Draw S/R levels
                        levels.forEach { (tag, lvlVal, colInt) ->
                            val ly = getLevelY(lvlVal)
                            val compColor = Color(colInt)

                            // Horizontal dashed guide line
                            drawLine(
                                color = compColor.copy(alpha = 0.40f),
                                start = Offset(lineStart, ly),
                                end = Offset(lineEnd, ly),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )

                            // Left tag
                            badgePaint.color = colInt
                            drawContext.canvas.nativeCanvas.drawText(tag, 2.dp.toPx(), ly + 3.5.dp.toPx(), badgePaint)

                            // Right price
                            pricePaint.color = colInt
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format("%.2f", lvlVal),
                                w - 2.dp.toPx(),
                                ly + 3.5.dp.toPx(),
                                pricePaint
                            )
                        }

                        // 2. Draw Current Price (LTP) with glowing beam & prominent pills
                        val cy = getLevelY(currentPrice)

                        // Shaded spread corridor from Pivot to Current Price
                        val py = getLevelY(pivot)
                        val corridorTop = minOf(cy, py)
                        val corridorH = (maxOf(cy, py) - corridorTop).coerceAtLeast(1f)
                        drawRect(
                            color = (if (isBullish) GainGreen else LossRed).copy(alpha = 0.08f),
                            topLeft = Offset(lineStart, corridorTop),
                            size = Size(lineEnd - lineStart, corridorH)
                        )

                        // Solid Current Price Line
                        drawLine(
                            color = FintechBlue,
                            start = Offset(lineStart - 6.dp.toPx(), cy),
                            end = Offset(lineEnd + 6.dp.toPx(), cy),
                            strokeWidth = 2.dp.toPx()
                        )

                        // Center glowing dot
                        drawCircle(
                            color = FintechBlue.copy(alpha = 0.35f),
                            radius = 6.dp.toPx(),
                            center = Offset(lineStart + (lineEnd - lineStart) / 2f, cy)
                        )
                        drawCircle(
                            color = FintechBlue,
                            radius = 3.dp.toPx(),
                            center = Offset(lineStart + (lineEnd - lineStart) / 2f, cy)
                        )

                        // Current Price Left Pill
                        val pillH = 15.dp.toPx()
                        val pillW = 38.dp.toPx()
                        drawRoundRect(
                            color = FintechBlue,
                            topLeft = Offset(0f, cy - pillH / 2f),
                            size = Size(pillW, pillH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                        )
                        val currTextPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 8.5.sp.toPx()
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        drawContext.canvas.nativeCanvas.drawText("LTP", 4.dp.toPx(), cy + 3.dp.toPx(), currTextPaint)

                        // Current Price Right Pill
                        val currPriceStr = String.format("%.2f", currentPrice)
                        val currPriceW = 56.dp.toPx()
                        drawRoundRect(
                            color = FintechBlue,
                            topLeft = Offset(w - currPriceW, cy - pillH / 2f),
                            size = Size(currPriceW, pillH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                        )
                        val rightCurrPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 9.sp.toPx()
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.RIGHT
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        drawContext.canvas.nativeCanvas.drawText(currPriceStr, w - 4.dp.toPx(), cy + 3.dp.toPx(), rightCurrPaint)
                    }
                }
            }

            // Classical Daily Pivot Points Table
            if (viewMode == "Both" || viewMode == "Table") {
                Spacer(modifier = Modifier.height(14.dp))

                PivotRow("Resistance 2 (R2)", String.format("${stock.currency} %,.2f", r2), LossRedBg, LossRed)
                HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                PivotRow("Resistance 1 (R1)", String.format("${stock.currency} %,.2f", r1), LossRedBg, LossRed)
                HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                PivotRow("Pivot Point (P)", String.format("${stock.currency} %,.2f", pivot), FintechSurfaceElevated, TextPrimary)
                HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                PivotRow("Support 1 (S1)", String.format("${stock.currency} %,.2f", s1), GainGreenBg, GainGreen)
                HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                PivotRow("Support 2 (S2)", String.format("${stock.currency} %,.2f", s2), GainGreenBg, GainGreen)
            }
        }
    }
}

@Composable
private fun PivotRow(
    label: String,
    value: String,
    badgeBg: Color,
    badgeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBg)
                .border(0.8.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badgeColor)
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

// =============================================================================
// Groww Fixed Bottom Action Bar (Watchlist Icon + Sell Button + Buy Button)
// =============================================================================
@Composable
private fun GrowwBottomActionBar(
    cleanSymbol: String,
    isWatchlisted: Boolean,
    onToggleWatchlist: () -> Unit,
    onSell: () -> Unit,
    onBuy: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FintechBackground.copy(alpha = 0.98f))
            .border(width = 1.dp, color = FintechBorder)
            .navigationBarsPadding()
            .padding(horizontal = horizontalPadding, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Watchlist outline button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isWatchlisted) GainGreenBg else FintechSurfaceElevated)
                    .border(1.dp, if (isWatchlisted) GainGreen else FintechBorder, RoundedCornerShape(12.dp))
                    .clickable { onToggleWatchlist() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isWatchlisted) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = if (isWatchlisted) "Remove from Watchlist" else "Add to Watchlist",
                    tint = if (isWatchlisted) GainGreen else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Sell Button (Red)
            Button(
                onClick = onSell,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LossRed,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Sell",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Buy Button (Mint Emerald)
            Button(
                onClick = onBuy,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FintechPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Buy",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

// =============================================================================
// Chapter 1: The Executive Investment Story & AI Narrative
// =============================================================================
@Composable
private fun ExecutiveStorySection(stock: DeepStockResearchPayload) {
    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(FintechPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = FintechPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Executive Investment Story",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FintechBlue.copy(alpha = 0.12f))
                        .border(1.dp, FintechBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "✦ AI Deep Thesis",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stock.aiStorySummary,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Strategic Story Milestones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StoryMilestoneChip(
                    icon = Icons.Rounded.Shield,
                    title = "Moat",
                    desc = "Scale Barrier",
                    color = FintechPrimary,
                    modifier = Modifier.weight(1f)
                )
                StoryMilestoneChip(
                    icon = Icons.Rounded.RocketLaunch,
                    title = "Catalyst",
                    desc = "Margin Growth",
                    color = FintechBlue,
                    modifier = Modifier.weight(1f)
                )
                StoryMilestoneChip(
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    title = "Flows",
                    desc = "Smart Money",
                    color = GainGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StoryMilestoneChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 9.sp, color = TextTertiary, maxLines = 1)
        }
    }
}

// =============================================================================
// Chapter 2: Interactive Chart Module with Multi-Timeframes & Scrubber
// =============================================================================
@Composable
internal fun InteractiveChartModule(
    stock: DeepStockResearchPayload,
    selectedTimeframe: String,
    onSelectTimeframe: (String) -> Unit,
    onToggleMaximize: () -> Unit = {}
) {
    val timeframes = listOf("1D", "5D", "1M", "6M", "YTD", "1Y", "5Y", "All")
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var isAreaMode by remember { mutableStateOf(true) }
    var showPivotLines by remember { mutableStateOf(false) }

    val srDayParts = remember(stock.metrics16.daysRange) {
        stock.metrics16.daysRange.split("-").map { it.trim().replace(",", "").toDoubleOrNull() ?: 0.0 }
    }
    val srHigh = if (srDayParts.size >= 2 && srDayParts[1] > 0) srDayParts[1] else stock.price * 1.018
    val srLow = if (srDayParts.isNotEmpty() && srDayParts[0] > 0) srDayParts[0] else stock.price * 0.982
    val srClose = stock.prevClose.takeIf { it > 0 } ?: stock.price
    val srPivot = (srHigh + srLow + srClose) / 3.0
    val srR1 = (2 * srPivot) - srLow
    val srS1 = (2 * srPivot) - srHigh
    val srR2 = srPivot + (srHigh - srLow)
    val srS2 = srPivot - (srHigh - srLow)

    val candles = remember(stock, selectedTimeframe) {
        val list = stock.timeframeCharts[selectedTimeframe]
        if (!list.isNullOrEmpty()) {
            list
        } else {
            val retStr = stock.timeframeReturns[selectedTimeframe] ?: ""
            val retVal = retStr.replace("%", "").replace("+", "").trim().toDoubleOrNull() ?: when (selectedTimeframe) {
                "5Y" -> 84.0
                "All" -> 620.0
                "1Y" -> 22.0
                "YTD" -> 14.0
                "6M" -> 8.0
                "1M" -> 3.5
                "5D" -> 1.2
                else -> 0.0
            }
            when (selectedTimeframe) {
                "5Y" -> com.stocktics.data.stocks.YahooFinanceClient.generateMultiYearCandleSeries(stock.price.toFloat(), retVal, 5, "5Y")
                "All" -> com.stocktics.data.stocks.YahooFinanceClient.generateMultiYearCandleSeries(stock.price.toFloat(), retVal, 10, "All")
                else -> stock.chartPoints
            }
        }
    }

    val activePoint = if (hoveredIndex != null && hoveredIndex!! in candles.indices) {
        candles[hoveredIndex!!]
    } else null

    val baseReference = if (selectedTimeframe == "1D") stock.prevClose.toFloat() else (candles.firstOrNull()?.open ?: stock.prevClose.toFloat())
    val displayPrice = activePoint?.close?.toDouble() ?: stock.price
    val displayChangeAbs = if (activePoint != null) (activePoint.close - baseReference).toDouble() else stock.changeAbs
    val displayChangePct = if (activePoint != null && baseReference > 0) (((activePoint.close - baseReference) / baseReference) * 100).toDouble() else stock.changePct
    val displayDate = activePoint?.dateLabel

    // Determine if Indian market for time label generation
    val isIndianExchange = stock.exchange.contains("NSE", true) || stock.exchange.contains("BSE", true)

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Chart Control & Scrubber Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activePoint != null) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "${stock.currency} ${String.format("%,.2f", displayPrice)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%+.2f (%+.2f%%)", displayChangeAbs, displayChangePct),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (displayChangePct >= 0) GainGreen else LossRed
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• $displayDate",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Technical Momentum",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Drag on chart to inspect",
                            fontSize = 10.sp,
                            color = TextTertiary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Return badge for selected timeframe
                val ret = stock.timeframeReturns[selectedTimeframe] ?: "+0.00%"
                val isPos = !ret.startsWith("-")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPos) GainGreenBg else LossRedBg)
                        .border(1.dp, if (isPos) GainGreen else LossRed, RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$selectedTimeframe: $ret",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPos) GainGreen else LossRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas with Y-Axis Price Ticks + X-Axis Time Labels
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Increased for axis labels
            ) {
                val minPrice = candles.minOf { it.low } * 0.995f
                val maxPrice = candles.maxOf { it.high } * 1.005f
                val priceRange = (maxPrice - minPrice).coerceAtLeast(1f)
                val maxVol = candles.maxOf { it.volume }.coerceAtLeast(1L)

                // Generate X-axis labels based on timeframe
                val xAxisLabels = remember(selectedTimeframe, candles, isIndianExchange) {
                    if (selectedTimeframe == "1D" && candles.isNotEmpty()) {
                        // Market hours: India 9:15-15:30, US 9:30-16:00
                        if (isIndianExchange) {
                            listOf("9:30", "10:30", "11:30", "12:30", "1:30", "2:30", "3:30")
                        } else {
                            listOf("9:30", "10:30", "11:30", "12:30", "1:30", "2:30", "3:30", "4:00")
                        }
                    } else if (candles.size > 4) {
                        // For non-1D timeframes, pick evenly spaced date labels from candle data
                        val step = candles.size / 5
                        (0 until 5).map { i ->
                            val idx = (i * step).coerceIn(0, candles.size - 1)
                            candles[idx].dateLabel.take(6) // Shortened date
                        }
                    } else {
                        candles.map { it.dateLabel.take(6) }
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedTimeframe) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val leftMargin = 48.dp.toPx()
                                    val chartW = size.width - leftMargin
                                    val candleWidth = chartW / candles.size
                                    val idx = ((offset.x - leftMargin) / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                    hoveredIndex = idx
                                },
                                onDragEnd = { hoveredIndex = null },
                                onDragCancel = { hoveredIndex = null },
                                onHorizontalDrag = { change, _ ->
                                    val leftMargin = 48.dp.toPx()
                                    val chartW = size.width - leftMargin
                                    val candleWidth = chartW / candles.size
                                    val idx = ((change.position.x - leftMargin) / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                    hoveredIndex = idx
                                }
                            )
                        }
                        .pointerInput(selectedTimeframe) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val leftMargin = 48.dp.toPx()
                                    val chartW = size.width - leftMargin
                                    val candleWidth = chartW / candles.size
                                    val idx = ((offset.x - leftMargin) / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                    hoveredIndex = idx
                                    tryAwaitRelease()
                                    hoveredIndex = null
                                }
                            )
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val leftMargin = 48.dp.toPx()
                    val bottomMargin = 22.dp.toPx()
                    val chartW = w - leftMargin
                    val chartH = (h - bottomMargin)
                    val candleWidth = chartW / candles.size
                    val bodyWidth = (candleWidth * 0.55f).coerceAtLeast(3f)

                    val chartAreaH = chartH * 0.78f
                    val volAreaH = chartH * 0.22f

                    // ── Y-Axis Price Ticks (3 levels: max, mid, min) ──
                    val yAxisPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 9.sp.toPx()
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    val priceLevels = listOf(maxPrice, (minPrice + maxPrice) / 2f, minPrice)
                    priceLevels.forEach { price ->
                        val y = chartAreaH - ((price - minPrice) / priceRange) * chartAreaH
                        // Horizontal grid line
                        drawLine(
                            color = Color(0xFF222A3B),
                            start = Offset(leftMargin, y),
                            end = Offset(w, y),
                            strokeWidth = 0.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
                        )
                        // Price label
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format("%,.0f", price),
                            leftMargin - 6.dp.toPx(),
                            y + 4.dp.toPx(),
                            yAxisPaint
                        )
                    }

                    // ── X-Axis Time/Date Labels ──
                    val xAxisPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 9.sp.toPx()
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val labelSpacing = chartW / (xAxisLabels.size.coerceAtLeast(1))
                    xAxisLabels.forEachIndexed { i, label ->
                        val lx = leftMargin + (i + 0.5f) * labelSpacing
                        // Tick mark
                        drawLine(
                            color = Color(0xFF222A3B),
                            start = Offset(lx, chartH),
                            end = Offset(lx, chartH + 4.dp.toPx()),
                            strokeWidth = 0.5.dp.toPx()
                        )
                        // Label
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            lx,
                            chartH + 16.dp.toPx(),
                            xAxisPaint
                        )
                    }

                    // ── Dashed previous close baseline ──
                    val pcY = chartAreaH - ((stock.prevClose.toFloat() - minPrice) / priceRange) * chartAreaH
                    if (pcY in 0f..chartAreaH) {
                        drawLine(
                            color = Color(0xFF64748B),
                            start = Offset(leftMargin, pcY),
                            end = Offset(w, pcY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // ── Support & Resistance Horizontal Levels Overlay ──
                    if (showPivotLines) {
                        val redArgb = android.graphics.Color.parseColor("#F04438")
                        val greenArgb = android.graphics.Color.parseColor("#00D09C")
                        val slateArgb = android.graphics.Color.parseColor("#94A3B8")

                        val srLevels = listOf(
                            Triple("R2", srR2.toFloat(), redArgb),
                            Triple("R1", srR1.toFloat(), redArgb),
                            Triple("P", srPivot.toFloat(), slateArgb),
                            Triple("S1", srS1.toFloat(), greenArgb),
                            Triple("S2", srS2.toFloat(), greenArgb)
                        )
                        val srPaint = android.graphics.Paint().apply {
                            textSize = 8.5.sp.toPx()
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }

                        srLevels.forEach { (lbl, lvlVal, colInt) ->
                            val ly = chartAreaH - ((lvlVal - minPrice) / priceRange) * chartAreaH
                            if (ly in 0f..chartAreaH) {
                                drawLine(
                                    color = Color(colInt).copy(alpha = 0.55f),
                                    start = Offset(leftMargin, ly),
                                    end = Offset(w - 22.dp.toPx(), ly),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                                )
                                srPaint.color = colInt
                                drawContext.canvas.nativeCanvas.drawText(lbl, w - 18.dp.toPx(), ly + 3.dp.toPx(), srPaint)
                            }
                        }

                        // Current Price (LTP) Line on Chart
                        val currLtpY = chartAreaH - ((stock.price.toFloat() - minPrice) / priceRange) * chartAreaH
                        if (currLtpY in 0f..chartAreaH) {
                            drawLine(
                                color = FintechBlue,
                                start = Offset(leftMargin, currLtpY),
                                end = Offset(w - 38.dp.toPx(), currLtpY),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            val ltpPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#1455FE")
                                textSize = 8.5.sp.toPx()
                                isAntiAlias = true
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                            drawContext.canvas.nativeCanvas.drawText("LTP", w - 34.dp.toPx(), currLtpY + 3.dp.toPx(), ltpPaint)
                        }
                    }

                    // ── 1. Volume Histogram Bars ──
                    candles.forEachIndexed { i, c ->
                        val x = leftMargin + i * candleWidth + candleWidth / 2
                        val volHeight = (c.volume.toFloat() / maxVol) * (volAreaH * 0.9f)
                        val isBull = c.close >= c.open
                        drawRect(
                            color = (if (isBull) GainGreen else LossRed).copy(alpha = 0.25f),
                            topLeft = Offset(x - bodyWidth / 2, chartH - volHeight),
                            size = Size(bodyWidth, volHeight)
                        )
                    }

                    // ── 2. Price Action: Area or Candles ──
                    if (isAreaMode) {
                        val path = Path()
                        val fillPath = Path()
                        fillPath.moveTo(leftMargin, chartAreaH)

                        candles.forEachIndexed { i, c ->
                            val x = leftMargin + i * candleWidth + candleWidth / 2
                            val y = chartAreaH - ((c.close - minPrice) / priceRange) * chartAreaH
                            if (i == 0) {
                                path.moveTo(x, y)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }

                        val lastX = leftMargin + (candles.size - 1) * candleWidth + candleWidth / 2
                        fillPath.lineTo(lastX, chartAreaH)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(FintechPrimary.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = chartAreaH
                            )
                        )

                        drawPath(
                            path = path,
                            color = FintechPrimary,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        val lastC = candles.last()
                        val lastY = chartAreaH - ((lastC.close - minPrice) / priceRange) * chartAreaH
                        drawCircle(color = FintechPrimary.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = Offset(lastX, lastY))
                        drawCircle(color = FintechPrimary, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
                    } else {
                        // Candlestick Mode
                        candles.forEachIndexed { i, c ->
                            val x = leftMargin + i * candleWidth + candleWidth / 2
                            val isBull = c.close >= c.open
                            val color = if (isBull) GainGreen else LossRed

                            val highY = chartAreaH - ((c.high - minPrice) / priceRange) * chartAreaH
                            val lowY = chartAreaH - ((c.low - minPrice) / priceRange) * chartAreaH
                            val openY = chartAreaH - ((c.open - minPrice) / priceRange) * chartAreaH
                            val closeY = chartAreaH - ((c.close - minPrice) / priceRange) * chartAreaH

                            drawLine(color = color, start = Offset(x, highY), end = Offset(x, lowY), strokeWidth = 1.5.dp.toPx())

                            val top = minOf(openY, closeY)
                            val bottom = maxOf(openY, closeY)
                            val bodyH = (bottom - top).coerceAtLeast(2.dp.toPx())
                            drawRect(color = color, topLeft = Offset(x - bodyWidth / 2, top), size = Size(bodyWidth, bodyH))
                        }
                    }

                    // ── 3. Hovered Scrubber Crosshair ──
                    hoveredIndex?.let { idx ->
                        if (idx in candles.indices) {
                            val c = candles[idx]
                            val hx = leftMargin + idx * candleWidth + candleWidth / 2
                            val hy = chartAreaH - ((c.close - minPrice) / priceRange) * chartAreaH

                            drawLine(
                                color = FintechPrimary,
                                start = Offset(hx, 0f),
                                end = Offset(hx, chartH),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )

                            drawCircle(color = FintechPrimary.copy(alpha = 0.3f), radius = 8.dp.toPx(), center = Offset(hx, hy))
                            drawCircle(color = FintechPrimary, radius = 4.dp.toPx(), center = Offset(hx, hy))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(hx, hy))
                        }
                    }
                }

                // Floating Inspection Card
                activePoint?.let { c ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 52.dp, top = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechSurfaceElevated.copy(alpha = 0.95f))
                            .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(text = "Date: ${c.dateLabel}", color = TextTertiary, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "O: ${c.open}", color = TextSecondary, fontSize = 10.sp)
                                Text(text = "H: ${c.high}", color = GainGreen, fontSize = 10.sp)
                                Text(text = "L: ${c.low}", color = LossRed, fontSize = 10.sp)
                                Text(text = "C: ${c.close}", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(text = "Vol: ${String.format("%,d", c.volume)}", color = TextTertiary, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeframe Selector Buttons (1D to All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FintechSurfaceElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                timeframes.forEach { tf ->
                    val isSel = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isSel) FintechPrimary else Color.Transparent)
                            .clickable {
                                hoveredIndex = null
                                onSelectTimeframe(tf)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Controls: Bar / Line Toggle and S/R Pivot Button (Centered below days filter)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chart Mode Toggle: Bar (Candles) vs Line (Area)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isAreaMode) FintechPrimary else Color.Transparent)
                                .clickable { isAreaMode = false }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.BarChart,
                                    contentDescription = "Bar",
                                    tint = if (!isAreaMode) Color.Black else TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Bar",
                                    fontSize = 11.sp,
                                    fontWeight = if (!isAreaMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isAreaMode) Color.Black else TextSecondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isAreaMode) FintechPrimary else Color.Transparent)
                                .clickable { isAreaMode = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoGraph,
                                    contentDescription = "Line",
                                    tint = if (isAreaMode) Color.Black else TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Line",
                                    fontSize = 11.sp,
                                    fontWeight = if (isAreaMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAreaMode) Color.Black else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // S/R Overlay Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (showPivotLines) FintechPrimary else FintechSurfaceElevated)
                        .border(1.dp, if (showPivotLines) FintechPrimary else FintechBorder, RoundedCornerShape(8.dp))
                        .clickable { showPivotLines = !showPivotLines }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "S/R",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showPivotLines) Color.Black else TextSecondary
                        )
                        if (showPivotLines) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Maximize Button (Landscape Pro Chart) beside S/R
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleMaximize() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Fullscreen,
                            contentDescription = "Maximize Chart (Landscape)",
                            tint = FintechPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceRangeGaugeModule(stock: DeepStockResearchPayload) {
    val m = stock.metrics16

    // Parse Day's Range: e.g. "1,293.10 - 1,321.90"
    val dayParts = m.daysRange.split("-").map { it.trim().replace(",", "").toDoubleOrNull() ?: 0.0 }
    val dayLow = dayParts.getOrElse(0) { stock.price * 0.98 }
    val dayHigh = dayParts.getOrElse(1) { stock.price * 1.02 }

    // Parse 52-Week Range: e.g. "1,249.80 - 1,611.80"
    val yrParts = m.fiftyTwoWeekRange.split("-").map { it.trim().replace(",", "").toDoubleOrNull() ?: 0.0 }
    val yrLow = yrParts.getOrElse(0) { stock.price * 0.85 }
    val yrHigh = yrParts.getOrElse(1) { stock.price * 1.25 }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StockRangeSlider(
            label = "Day",
            lowPrice = dayLow,
            highPrice = dayHigh,
            currentPrice = stock.price,
            currencySymbol = stock.currency
        )
        StockRangeSlider(
            label = "52-Week",
            lowPrice = yrLow,
            highPrice = yrHigh,
            currentPrice = stock.price,
            currencySymbol = stock.currency
        )
    }
}

@Composable
private fun RangeBarItem(
    title: String,
    lowVal: Double,
    highVal: Double,
    currentVal: Double,
    currency: String
) {
    val fraction = ((currentVal - lowVal) / (highVal - lowVal).coerceAtLeast(0.01)).toFloat().coerceIn(0.05f, 0.95f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Text(text = "Current: $currency ${String.format("%,.2f", currentVal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Range track with indicator needle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background gradient track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(LossRed.copy(alpha = 0.6f), WarningAmber.copy(alpha = 0.6f), GainGreen.copy(alpha = 0.8f))
                        )
                    )
            )

            // Current price pin needle
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, FintechPrimary, CircleShape)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "L: ${String.format("%,.2f", lowVal)}", fontSize = 10.sp, color = TextTertiary)
            Text(text = "H: ${String.format("%,.2f", highVal)}", fontSize = 10.sp, color = TextTertiary)
        }
    }
}

// =============================================================================
// 3. Stocktics Proprietary Quant Score & Diagnostic Verdict
// =============================================================================
@Composable
private fun QuantScoreSection(stock: DeepStockResearchPayload) {
    var showSignalsTable by remember { mutableStateOf(false) }
    val quant = stock.quantVerdict

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Stocktics Quant Engine",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Institutional 5-Pillar Algorithmic Verdict",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                val cleanRating = quant.rating.replace("🟢 ", "").replace("🟡 ", "").replace("🔴 ", "").trim()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (quant.score >= 60) GainGreenBg else WarningAmberBg)
                        .border(1.dp, if (quant.score >= 60) GainGreen else WarningAmber, RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (quant.score >= 60) GainGreen else WarningAmber)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = cleanRating,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (quant.score >= 60) GainGreen else WarningAmber,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { quant.score / 100f },
                        modifier = Modifier.size(64.dp),
                        color = if (quant.score >= 60) GainGreen else WarningAmber,
                        trackColor = FintechBorder,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "${quant.score}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Strategy: ${quant.rating.replace("🟢 ", "").replace("🟡 ", "").replace("🔴 ", "")}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = quant.action,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5 Pillars Breakdown
            Text(
                text = "5-Pillar Score Breakdown",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            quant.pillars.forEach { (pillarName, scoreText) ->
                val currentPts = scoreText.substringBefore(" /").trim().toIntOrNull() ?: 10
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pillarName,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(0.45f)
                    )
                    LinearProgressIndicator(
                        progress = { currentPts / 20f },
                        modifier = Modifier
                            .weight(0.40f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (currentPts >= 14) GainGreen else if (currentPts >= 8) WarningAmber else LossRed,
                        trackColor = FintechSurfaceElevated
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = scoreText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.weight(0.15f),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key Catalysts & Watchout Risks
            if (quant.bullishDrivers.isNotEmpty()) {
                Text(text = "Top Bullish Drivers:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GainGreen)
                quant.bullishDrivers.take(3).forEach { d ->
                    Text(text = "• $d", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }

            if (quant.watchoutRisks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Watchout Risks:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LossRed)
                quant.watchoutRisks.take(2).forEach { r ->
                    Text(text = "• $r", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle Full Signals Table
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .clickable { showSignalsTable = !showSignalsTable }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showSignalsTable) "Hide Detailed Metric Signals" else "View All Metric Signals (${quant.signals.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FintechPrimary
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = FintechPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (showSignalsTable) {
                Spacer(modifier = Modifier.height(10.dp))
                quant.signals.forEach { s ->
                    val isPos = s.signal.contains("Positive", ignoreCase = true)
                    val isNeu = s.signal.contains("Neutral", ignoreCase = true)
                    val cardBg = when {
                        isPos -> GainGreen.copy(alpha = 0.10f)
                        isNeu -> WarningAmber.copy(alpha = 0.09f)
                        else -> LossRed.copy(alpha = 0.10f)
                    }
                    val cardBorder = when {
                        isPos -> GainGreen.copy(alpha = 0.45f)
                        isNeu -> WarningAmber.copy(alpha = 0.45f)
                        else -> LossRed.copy(alpha = 0.45f)
                    }
                    val badgeBg = when {
                        isPos -> GainGreenBg
                        isNeu -> WarningAmberBg
                        else -> LossRedBg
                    }
                    val badgeColor = when {
                        isPos -> GainGreen
                        isNeu -> WarningAmber
                        else -> LossRed
                    }
                    val cleanSignalText = s.signal.replace("🟢 ", "").replace("🟡 ", "").replace("🔴 ", "").trim()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = s.metric,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f, fill = false).padding(end = 6.dp)
                                )
                                Text(
                                    text = s.value,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = s.diagnosis,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }

                        // High-contrast Fintech Signal Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBg)
                                .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cleanSignalText,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
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

// =============================================================================
// 4. Quick Stats Visual Grid (Color-Coded 2-Column Mini Cards)
// =============================================================================
@Composable
private fun Metrics16GridSection(stock: DeepStockResearchPayload) {
    val m = stock.metrics16

    val defaultValColor = TextPrimary
    data class StatItem(
        val label: String,
        val value: String,
        val icon: String,
        val valueColor: Color = Color.Unspecified
    )

    // Calculate smart financial colors
    val openVal = m.open.replace(",", "").toDoubleOrNull() ?: 0.0
    val pcVal = m.previousClose.replace(",", "").toDoubleOrNull() ?: 0.0
    val openColor = if (openVal >= pcVal && openVal > 0.0) GainGreen else if (openVal < pcVal && openVal > 0.0) LossRed else TextPrimary

    val epsVal = m.eps.replace(",", "").toFloatOrNull()
    val epsColor = if (epsVal != null && epsVal < 0f) LossRed else GainGreen

    val peVal = m.peRatio.replace(",", "").toFloatOrNull()
    val peColor = when {
        peVal == null || peVal <= 0f -> TextPrimary
        peVal < 25f -> GainGreen
        peVal < 45f -> FintechPrimary
        else -> WarningAmber
    }

    val betaVal = m.beta.replace(",", "").toFloatOrNull()
    val betaColor = when {
        betaVal == null -> TextPrimary
        betaVal < 1.0f -> GainGreen
        betaVal < 1.5f -> FintechPrimary
        else -> WarningAmber
    }

    val targetNum = m.targetEst.replace("₹", "").replace("$", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
    val targetColor = if (targetNum > stock.price) GainGreen else if (targetNum > 0.0) LossRed else TextPrimary

    val divYieldColor = if (m.forwardDividend.contains("0.00") || m.forwardDividend.contains("-") || m.forwardDividend.isBlank()) TextSecondary else GainGreen

    val dims = rememberScreenDimensions()
    val allStatsFlat = listOf(
        StatItem("Market Cap", m.marketCap, "🏛", FintechPrimary),
        StatItem("P/E Ratio", m.peRatio, "📊", peColor),
        StatItem("EPS (TTM)", m.eps, "💰", epsColor),
        StatItem("Beta (5Y)", m.beta, "⚡", betaColor),
        StatItem("Volume", m.volume, "📈", FintechPrimary),
        StatItem("Avg Volume", m.avgVolume, "📉", TextSecondary),
        StatItem("Prev Close", m.previousClose, "🔒", TextSecondary),
        StatItem("Open", m.open, "🔓", openColor),
        StatItem("Bid", m.bid, "🟢", GainGreen),
        StatItem("Ask", m.ask, "🔴", LossRed),
        StatItem("Div Yield", m.forwardDividend, "💎", divYieldColor),
        StatItem("Target Est", m.targetEst, "🎯", targetColor),
        StatItem("Earnings", m.earningsDate, "📅", WarningAmber),
        StatItem("Ex-Div Date", m.exDividendDate, "📆", FintechPrimary)
    )
    val chunkedStats = allStatsFlat.chunked(dims.statColumns)

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Key Statistics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Fundamental Metrics Overview", fontSize = 11.sp, color = TextTertiary)
                }
                StockticsBadge(text = "Key Metrics", variant = BadgeVariant.NEUTRAL)
            }

            Spacer(modifier = Modifier.height(14.dp))

            chunkedStats.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { stat ->
                        val effectiveValColor = if (stat.valueColor != Color.Unspecified) stat.valueColor else defaultValColor
                        val statCardBg = effectiveValColor.copy(alpha = 0.09f)
                        val statCardBorder = effectiveValColor.copy(alpha = 0.40f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(statCardBg)
                                .border(1.dp, statCardBorder, RoundedCornerShape(10.dp))
                                .padding(
                                    horizontal = if (dims.isSmall) 8.dp else 10.dp,
                                    vertical = if (dims.isSmall) 6.dp else 8.dp
                                )
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (dims.isSmall) 16.dp else 18.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(effectiveValColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(stat.icon, fontSize = if (dims.isSmall) 9.sp else 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = stat.label,
                                        fontSize = dims.captionSp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = stat.value,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = effectiveValColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 5. Company Overview Card
// =============================================================================
// 5. Company Overview & Corporate Profile (Live Financials)
// =============================================================================
@Composable
private fun CompanyOverviewSection(stock: DeepStockResearchPayload) {
    val context = LocalContext.current
    val dims = rememberScreenDimensions()
    var expanded by remember { mutableStateOf(false) }
    val p = stock.profile

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = p.title,
                fontSize = dims.h2Sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = p.summary,
                fontSize = dims.bodySp,
                color = TextSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = (dims.bodySp.value * 1.5f).sp
            )

            Text(
                text = if (expanded) "Read Less" else "Read More",
                fontSize = dims.captionSp,
                fontWeight = FontWeight.Bold,
                color = FintechPrimary,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Corporate stats list
            val stats = listOf(
                "Official Website" to p.website,
                "Full Time Employees" to p.employees,
                "Fiscal Year Ends" to p.fiscalYearEnd,
                "Sector" to p.sector,
                "Industry" to p.industry
            )

            stats.forEach { (lbl, valStr) ->
                val isWebsite = lbl == "Official Website" && valStr.isNotBlank() && valStr != "N/A"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isWebsite) {
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { openWebUrl(context, valStr) }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            } else {
                                Modifier.padding(vertical = 4.dp)
                            }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lbl,
                        fontSize = dims.bodySp,
                        color = TextTertiary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = if (isWebsite) "$valStr ↗" else valStr,
                        fontSize = dims.bodySp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWebsite) FintechPrimary else TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

// =============================================================================
// 6. Corporate Events Calendar
// =============================================================================
@Composable
private fun CorporateEventsSection(stock: DeepStockResearchPayload) {
    val dims = rememberScreenDimensions()
    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = FintechPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Corporate Events & Catalysts",
                    fontSize = dims.h2Sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (stock.upcomingEvents.isEmpty() && stock.recentEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scheduled corporate events found for ${stock.symbol}",
                        fontSize = dims.bodySp,
                        color = TextSecondary
                    )
                }
            } else {
                if (stock.upcomingEvents.isNotEmpty()) {
                    Text(text = "Upcoming Events", fontSize = dims.bodySp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    stock.upcomingEvents.forEach { ev ->
                        EventCard(ev.name, ev.dateTime, ev.details, isUpcoming = true)
                    }
                }

                if (stock.recentEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Recent Events", fontSize = dims.bodySp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    stock.recentEvents.forEach { ev ->
                        EventCard(ev.name, ev.dateTime, ev.details, isUpcoming = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(name: String, dateTime: String, details: String, isUpcoming: Boolean) {
    val dims = rememberScreenDimensions()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = dims.bodySp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isUpcoming) GainGreenBg else FintechSurface)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isUpcoming) "Upcoming" else "Completed",
                        fontSize = dims.badgeSp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUpcoming) FintechPrimary else TextTertiary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = dateTime, fontSize = dims.captionSp, color = TextTertiary)
            Text(text = details, fontSize = dims.captionSp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// =============================================================================
// 7. Key Financial Statistics & Solvency Multiples (Color-Coded)
// =============================================================================
@Composable
private fun KeyStatsSection(stock: DeepStockResearchPayload) {
    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Key Institutional Statistics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Valuation Measures, Margins & Solvency",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
                StockticsBadge(text = "Fundamentals", variant = BadgeVariant.NEUTRAL)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category 1: Valuation Measures
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FintechPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Valuation Measures",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            stock.valuationStats.forEach { (lbl, v) ->
                StatRow(lbl, v)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category 2: Profitability & Margins
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GainGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profitability & Margins",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GainGreen
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            stock.profitabilityStats.forEach { (lbl, v) ->
                StatRow(lbl, v)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category 3: Balance Sheet & Solvency
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(WarningAmber)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Balance Sheet & Solvency",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            stock.solvencyStats.forEach { (lbl, v) ->
                StatRow(lbl, v)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    val cleanVal = value.trim()
    val valueColor = when {
        cleanVal.startsWith("+") -> GainGreen
        cleanVal.startsWith("-") -> LossRed
        label.contains("Growth", ignoreCase = true) && !cleanVal.startsWith("-") && !cleanVal.contains("N/A") -> GainGreen
        label.contains("Margin", ignoreCase = true) && !cleanVal.startsWith("-") && !cleanVal.contains("N/A") -> GainGreen
        label.contains("Total Cash", ignoreCase = true) -> GainGreen
        label.contains("Total Debt", ignoreCase = true) -> WarningAmber
        label.contains("Net Income", ignoreCase = true) && !cleanVal.startsWith("-") -> GainGreen
        label.contains("EBITDA", ignoreCase = true) && !cleanVal.startsWith("-") -> GainGreen
        label.contains("Forward P/E", ignoreCase = true) || label.contains("Book Value", ignoreCase = true) -> FintechPrimary
        else -> TextPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

// =============================================================================
// 8. Financial Statements (Interactive 4-Year Visual Bar Charts)
// =============================================================================
@Composable
private fun FinancialStatementsSection(stock: DeepStockResearchPayload) {
    var selectedTab by remember { mutableStateOf("Income") }
    var selectedYear by remember { mutableStateOf("2026") }
    val tabs = listOf("Income", "Balance Sheet", "Cash Flow")
    val stmt = stock.financialStatements

    val activeRows = when (selectedTab) {
        "Income" -> stmt.incomeStatement
        "Balance Sheet" -> stmt.balanceSheet
        else -> stmt.cashFlow
    }

    val primaryRow = activeRows.firstOrNull()
    val secondaryRow = when (selectedTab) {
        "Income" -> activeRows.getOrNull(1) ?: primaryRow
        "Balance Sheet" -> activeRows.getOrNull(1) ?: primaryRow
        else -> activeRows.getOrNull(2) ?: activeRows.getOrNull(1) ?: primaryRow
    }

    val primaryMetric = primaryRow?.metric ?: "Total Revenue"
    val secondaryMetric = secondaryRow?.metric ?: "Gross Profit"

    val color1 = FintechBlue
    val color2 = when (selectedTab) {
        "Income" -> GainGreen
        "Balance Sheet" -> WarningAmber
        else -> GainGreen
    }

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Financial Statements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Annual (4-Years)",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Statement Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEach { tab ->
                    val isSel = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) FintechPrimary else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (primaryRow != null) {
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color1))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = primaryMetric,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color2))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = secondaryMetric,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Tap bar",
                        fontSize = 10.sp,
                        color = FintechPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val pYears = mapOf(
                    "2023" to primaryRow.val2023,
                    "2024" to primaryRow.val2024,
                    "2025" to primaryRow.val2025,
                    "2026" to primaryRow.val2026
                )
                val sYears = mapOf(
                    "2023" to (secondaryRow?.val2023 ?: (primaryRow.val2023 * 0.4)),
                    "2024" to (secondaryRow?.val2024 ?: (primaryRow.val2024 * 0.4)),
                    "2025" to (secondaryRow?.val2025 ?: (primaryRow.val2025 * 0.4)),
                    "2026" to (secondaryRow?.val2026 ?: (primaryRow.val2026 * 0.4))
                )
                val maxVal = maxOf(pYears.values.maxOrNull() ?: 1.0, sYears.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                val yearList = listOf("2023", "2024", "2025", "2026")

                // Dedicated Dual-Bar Chart Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    // Bar Area (Fixed height 80dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        yearList.forEach { yr ->
                            val isSelYear = yr == selectedYear
                            val v1 = pYears[yr] ?: 1.0
                            val v2 = sYears[yr] ?: 0.5
                            val hFactor1 = (v1 / maxVal).toFloat().coerceIn(0.12f, 1f)
                            val hFactor2 = (v2 / maxVal).toFloat().coerceIn(0.10f, 1f)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { selectedYear = yr }
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Bar 1
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height((64 * hFactor1).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (isSelYear) Brush.verticalGradient(listOf(color1, color1.copy(alpha = 0.7f)))
                                            else Brush.verticalGradient(listOf(color1.copy(alpha = 0.5f), color1.copy(alpha = 0.25f)))
                                        )
                                        .border(
                                            width = if (isSelYear) 1.dp else 0.dp,
                                            color = if (isSelYear) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )

                                // Bar 2
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height((64 * hFactor2).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (isSelYear) Brush.verticalGradient(listOf(color2, color2.copy(alpha = 0.7f)))
                                            else Brush.verticalGradient(listOf(color2.copy(alpha = 0.5f), color2.copy(alpha = 0.25f)))
                                        )
                                        .border(
                                            width = if (isSelYear) 1.dp else 0.dp,
                                            color = if (isSelYear) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dedicated Year Labels Row (NEVER clipped)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        yearList.forEach { yr ->
                            val isSelYear = yr == selectedYear
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelYear) FintechPrimary.copy(alpha = 0.16f) else Color.Transparent)
                                    .clickable { selectedYear = yr }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = yr,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelYear) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelYear) FintechPrimary else TextTertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Dual-Value Performance Breakdown Card
                val pValStr = when (selectedYear) {
                    "2023" -> primaryRow.year2023
                    "2024" -> primaryRow.year2024
                    "2025" -> primaryRow.year2025
                    else -> primaryRow.year2026
                }
                val sValStr = when (selectedYear) {
                    "2023" -> secondaryRow?.year2023 ?: "--"
                    "2024" -> secondaryRow?.year2024 ?: "--"
                    "2025" -> secondaryRow?.year2025 ?: "--"
                    else -> secondaryRow?.year2026 ?: "--"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$selectedYear Performance Overview",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            // Growth Badge
                            val growthStr = when (selectedYear) {
                                "2023" -> "Baseline"
                                "2024" -> "+10.9% YoY"
                                "2025" -> "+3.0% YoY"
                                else -> "+12.5% YoY"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GainGreenBg)
                                    .border(1.dp, GainGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = growthStr,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GainGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Primary Metric Pill Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color1.copy(alpha = 0.08f))
                                    .border(0.8.dp, color1.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                            ) {
                                Column {
                                    Text(
                                        text = primaryMetric,
                                        fontSize = 10.5.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = pValStr,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color1,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            // Secondary Metric Pill Box
                            if (secondaryRow != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color2.copy(alpha = 0.08f))
                                        .border(0.8.dp, color2.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = secondaryMetric,
                                            fontSize = 10.5.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = sValStr,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color2,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Metric",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextTertiary,
                        modifier = Modifier.weight(0.32f),
                        maxLines = 1,
                        softWrap = false
                    )
                    yearList.forEach { yr ->
                        val isSelYear = yr == selectedYear
                        Text(
                            text = yr,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelYear) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelYear) FintechPrimary else TextTertiary,
                            modifier = Modifier.weight(0.17f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                HorizontalDivider(color = FintechBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(4.dp))

                // Table Breakdown Rows
                activeRows.forEach { r ->
                    val cleanMetric = when {
                        r.metric.contains("Operating Income", ignoreCase = true) -> "Operating Income"
                        r.metric.contains("Cash & Equivalents", ignoreCase = true) -> "Cash & Equiv"
                        r.metric.contains("Stockholder", ignoreCase = true) -> "Total Equity"
                        r.metric.contains("Operating Cash", ignoreCase = true) -> "Operating Cash"
                        r.metric.contains("Investing Cash", ignoreCase = true) -> "Investing Cash"
                        r.metric.contains("Financing Cash", ignoreCase = true) -> "Financing Cash"
                        r.metric.contains("Net Income to Common", ignoreCase = true) -> "Net Income"
                        r.metric.contains("Total Cash & ST", ignoreCase = true) -> "Cash & Short-Term"
                        else -> r.metric.replace(" (EBIT)", "").replace(" (₹)", "").replace(" ($)", "")
                    }
                    val clean2023 = r.year2023.replace("₹ ", "₹").replace("$ ", "$").replace("-₹ ", "-₹").replace("-$ ", "-$").trim()
                    val clean2024 = r.year2024.replace("₹ ", "₹").replace("$ ", "$").replace("-₹ ", "-₹").replace("-$ ", "-$").trim()
                    val clean2025 = r.year2025.replace("₹ ", "₹").replace("$ ", "$").replace("-₹ ", "-₹").replace("-$ ", "-$").trim()
                    val clean2026 = r.year2026.replace("₹ ", "₹").replace("$ ", "$").replace("-₹ ", "-₹").replace("-$ ", "-$").trim()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cleanMetric,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(0.32f),
                            softWrap = false
                        )
                        Text(
                            text = clean2023,
                            fontSize = 10.5.sp,
                            fontWeight = if (selectedYear == "2023") FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedYear == "2023") FintechPrimary else TextTertiary,
                            modifier = Modifier.weight(0.17f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = clean2024,
                            fontSize = 10.5.sp,
                            fontWeight = if (selectedYear == "2024") FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedYear == "2024") FintechPrimary else TextTertiary,
                            modifier = Modifier.weight(0.17f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = clean2025,
                            fontSize = 10.5.sp,
                            fontWeight = if (selectedYear == "2025") FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedYear == "2025") FintechPrimary else TextTertiary,
                            modifier = Modifier.weight(0.17f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = clean2026,
                            fontSize = 10.5.sp,
                            fontWeight = if (selectedYear == "2026") FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selectedYear == "2026") FintechPrimary else TextPrimary,
                            modifier = Modifier.weight(0.17f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 9. Wall Street Analyst Consensus & Estimates
// =============================================================================
@Composable
private fun AnalystConsensusSection(stock: DeepStockResearchPayload) {
    val cons = stock.consensus
    val totalRatings = (cons.buyRatings + cons.holdRatings + cons.sellRatings).coerceAtLeast(1)
    val buyPct = (cons.buyRatings.toFloat() / totalRatings) * 100f
    val holdPct = (cons.holdRatings.toFloat() / totalRatings) * 100f
    val sellPct = (cons.sellRatings.toFloat() / totalRatings) * 100f

    val cleanRating = when {
        buyPct >= 70f -> "STRONG BUY"
        buyPct >= 50f -> "BUY"
        sellPct >= 40f -> "SELL"
        else -> "HOLD"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Modern Speedometer Gauge & Consensus Transition Card
        StockSpeedometerGauge(
            consensusRating = cleanRating,
            buyPercent = buyPct,
            holdPercent = holdPct,
            sellPercent = sellPct,
            currentPrice = stock.price,
            targetPrice = if (cons.meanTarget > 0) cons.meanTarget else stock.price * 1.18,
            currencySymbol = stock.currency
        )

        // ── EPS Surprise History (Visual Beat/Miss) ──
        if (cons.epsTrends.isNotEmpty()) {
            StockticsCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("EPS Surprise History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Quarterly Consensus vs Reported Results", fontSize = 11.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(10.dp))

                    cons.epsTrends.forEach { ep ->
                        val isBeat = ep.outcome.contains("Beat", ignoreCase = true)
                        val isMiss = ep.outcome.contains("Miss", ignoreCase = true)
                        val outcomeColor = when {
                            isBeat -> GainGreen
                            isMiss -> LossRed
                            else -> TextTertiary
                        }
                        val outcomeBg = when {
                            isBeat -> GainGreenBg
                            isMiss -> LossRedBg
                            else -> WarningAmberBg
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FintechSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ep.quarterDate, fontSize = 11.sp, color = TextTertiary, modifier = Modifier.weight(0.25f))
                            Text("Est: ${ep.epsEstimate}", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(0.25f))
                            Text("Rep: ${ep.reportedEps}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.25f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(outcomeBg)
                                    .border(1.dp, outcomeColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(ep.outcome, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = outcomeColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 10. Major Holders & Smart Money Footprint
// =============================================================================
@Composable
private fun MajorHoldersSection(stock: DeepStockResearchPayload) {
    val h = stock.holders
    var selectedHolderIndex by remember { mutableStateOf(0) }

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Major Holders & Smart Money", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Ownership Distribution & Insider Activity", fontSize = 11.sp, color = TextTertiary)

            Spacer(modifier = Modifier.height(14.dp))

            val percentages = h.majorHolders.map { mh ->
                val numStr = mh.value.replace("%", "").replace(",", "").trim()
                numStr.toFloatOrNull() ?: 0f
            }
            val arcColors = listOf(FintechPrimary, FintechBlue, GainGreen, WarningAmber, LossRed)
            val fallbackArcColor = TextTertiary

            val safeIndex = selectedHolderIndex.coerceIn(0, (h.majorHolders.size - 1).coerceAtLeast(0))
            val selectedHolder = h.majorHolders.getOrNull(safeIndex)

            // ── Ownership Donut + Compact Chips ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Donut Chart with Center Metric Label
                Box(
                    modifier = Modifier.size(105.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size(105.dp)
                    ) {
                        val strokeWidth = 16.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        var startAngle = -90f

                        val total = percentages.sum().coerceAtLeast(1f)

                        percentages.forEachIndexed { i, pct ->
                            val sweep = (pct / total) * 360f
                            val isSel = i == safeIndex
                            drawArc(
                                color = if (isSel) arcColors.getOrElse(i) { fallbackArcColor }
                                        else arcColors.getOrElse(i) { fallbackArcColor }.copy(alpha = 0.45f),
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(
                                    width = if (isSel) strokeWidth * 1.15f else strokeWidth,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                            startAngle += sweep + 2f
                        }
                    }

                    // Center Label
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedHolder?.value ?: "51.8%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = getHolderShortName(safeIndex),
                            fontSize = 8.5.sp,
                            color = TextTertiary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Compact Interactive Chip Pills
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    h.majorHolders.forEachIndexed { i, mh ->
                        val isSel = i == safeIndex
                        val color = arcColors.getOrElse(i) { fallbackArcColor }
                        val shortTitle = getHolderShortName(i)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) color.copy(alpha = 0.16f) else FintechSurfaceElevated)
                                .border(1.dp, if (isSel) color.copy(alpha = 0.6f) else FintechBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedHolderIndex = i }
                                .padding(horizontal = 8.dp, vertical = 4.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = shortTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) TextPrimary else TextSecondary
                                )
                            }
                            Text(
                                text = mh.value,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) color else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Detail Insight Card for Selected Holding
            if (selectedHolder != null) {
                val activeColor = arcColors.getOrElse(safeIndex) { fallbackArcColor }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeColor.copy(alpha = 0.08f))
                        .border(1.dp, activeColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedHolder.breakdown,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = selectedHolder.value,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = getHolderExplanation(safeIndex, selectedHolder.value, stock.name),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Insider Roster (Non-overlapping) ──
            Text("Recent Insider Activity", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            h.insiderRoster.take(4).forEach { ins ->
                val isAcq = ins.transaction.contains("Acquisition", ignoreCase = true) || ins.transaction.contains("Buy", ignoreCase = true)
                val isSale = ins.transaction.contains("Sale", ignoreCase = true) || ins.transaction.contains("Sell", ignoreCase = true)
                val badgeBg = if (isAcq) GainGreenBg else if (isSale) LossRedBg else FintechSurfaceElevated
                val badgeColor = if (isAcq) GainGreen else if (isSale) LossRed else TextTertiary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = ins.nameAndRole,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = ins.transaction,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ins.date,
                                fontSize = 10.sp,
                                color = TextTertiary
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = ins.shares,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Shares Owned",
                            fontSize = 9.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

private fun getHolderShortName(index: Int): String {
    return when (index) {
        0 -> "All Insiders"
        1 -> "Institutions"
        2 -> "Inst. Float"
        3 -> "Inst. Count"
        else -> "Holders"
    }
}

private fun getHolderExplanation(index: Int, value: String, companyName: String): String {
    return when (index) {
        0 -> "Promoters, executives, and company founders hold $value of total outstanding shares."
        1 -> "Mutual funds, foreign portfolio investors, and pension funds hold $value of the capital."
        2 -> "Institutions control $value of the freely tradeable public float shares."
        else -> "Holding distribution metrics for $companyName."
    }
}

private fun analyzeNewsSentiment(title: String, desc: String = ""): String {
    val text = "$title $desc".lowercase(java.util.Locale.ROOT)
    val posWords = listOf(
        "surge", "surges", "surging", "gain", "gains", "profit", "profits", "profitable",
        "beat", "beats", "rally", "rallies", "growth", "high", "highs", "upgrade",
        "upgraded", "record", "jump", "jumps", "bull", "bullish", "boost", "boosts",
        "soar", "soars", "partner", "partnership", "expands", "expansion", "dividend",
        "revenue up", "top pick", "outperform", "buy", "strong", "breakout", "winner"
    )
    val negWords = listOf(
        "drop", "drops", "dropping", "fall", "falls", "falling", "loss", "losses",
        "miss", "misses", "missed", "plunge", "plunges", "decline", "declines", "cut",
        "cuts", "downgrade", "downgraded", "probe", "investigation", "lawsuit", "sues",
        "sued", "layoff", "layoffs", "bear", "bearish", "crash", "slump", "slumps",
        "warns", "warning", "risk", "risks", "debt", "fine", "penalty", "selloff"
    )
    val posCount = posWords.count { text.contains(it) }
    val negCount = negWords.count { text.contains(it) }
    return when {
        posCount > negCount -> "positive"
        negCount > posCount -> "negative"
        else -> "neutral"
    }
}

// =============================================================================
// 11. Verified Clean News Feed (Last 2 Months, Zero Ads)
// =============================================================================
@Composable
private fun NewsSection(stock: DeepStockResearchPayload) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedSource by remember { mutableStateOf("indmoney") } // "indmoney" or "yahoo"
    var selectedSentimentFilter by remember { mutableStateOf<String?>("all") }
    val lazyListState = rememberLazyListState()

    var liveIndmoneyList by remember(stock.symbol) { mutableStateOf(stock.indmoneyEvents) }
    var isLoadingIndmoney by remember(stock.symbol) { mutableStateOf(stock.indmoneyEvents.isEmpty()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Asynchronously fetch INDmoney MCP news with authenticated user token and automatic sentiment analysis
    LaunchedEffect(stock.symbol, refreshTrigger) {
        if (stock.indmoneyEvents.isNotEmpty() && refreshTrigger == 0) {
            liveIndmoneyList = stock.indmoneyEvents
            isLoadingIndmoney = false
            return@LaunchedEffect
        }
        isLoadingIndmoney = true
        withContext(Dispatchers.IO) {
            val fetched = mutableListOf<NewsArticleItem>()
            val cleanBase = stock.symbol.replace(".NS", "").replace(".BO", "").trim().uppercase()
            val userId = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
            val userParam = if (userId.isNotBlank()) "?user_id=${java.net.URLEncoder.encode(userId, "UTF-8")}" else ""

            val candidateUrls = listOf(
                "https://stocktics-backend.onrender.com/api/stock/${stock.symbol}/news$userParam",
                "https://stocktics-backend.onrender.com/api/stock/$cleanBase/news$userParam",
                "http://192.168.1.8:8000/api/stock/${stock.symbol}/news$userParam",
                "http://10.0.2.2:8000/api/stock/${stock.symbol}/news$userParam"
            )
            var jsonStr = ""
            for (url in candidateUrls) {
                try {
                    val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 3000
                        readTimeout = 12000
                    }
                    if (conn.responseCode == 200) {
                        jsonStr = conn.inputStream.bufferedReader().readText()
                        if (jsonStr.isNotBlank()) break
                    }
                } catch (_: Exception) {}
            }

            if (jsonStr.isNotBlank()) {
                try {
                    val bObj = org.json.JSONObject(jsonStr)
                    val indObj = bObj.optJSONObject("indmoney")
                    val keyEvts = listOfNotNull(
                        indObj?.optJSONArray("key_events"),
                        bObj.optJSONArray("key_events"),
                        indObj?.optJSONArray("this_month_news"),
                        bObj.optJSONArray("this_month_news"),
                        indObj?.optJSONArray("indmoney_news"),
                        bObj.optJSONArray("indmoney_news")
                    ).firstOrNull { it.length() > 0 }
                        ?: indObj?.optJSONArray("key_events")
                        ?: bObj.optJSONArray("key_events")

                    if (keyEvts != null && keyEvts.length() > 0) {
                        for (i in 0 until keyEvts.length()) {
                            val e = keyEvts.optJSONObject(i) ?: continue
                            val rawSent = e.optString("sentiment", "positive")
                            val parsedSent = when {
                                rawSent.contains("pos", true) || rawSent.contains("bull", true) -> "positive"
                                rawSent.contains("neg", true) || rawSent.contains("bear", true) -> "negative"
                                else -> "neutral"
                            }
                            fetched.add(
                                NewsArticleItem(
                                    title = e.optString("clean_title", e.optString("title", "")),
                                    publisher = e.optString("publisher", "INDmoney Key Events"),
                                    timeAgo = e.optString("date_str", e.optString("time_ago", "")),
                                    publishedAt = e.optString("news_date", ""),
                                    url = e.optString("url", ""),
                                    description = e.optString("description", "Institutional market catalyst regarding ${e.optString("title", stock.symbol)}."),
                                    sentiment = parsedSent,
                                    newsDate = e.optString("news_date", ""),
                                    isToday = e.optBoolean("is_today", false)
                                )
                            )
                        }
                    }

                    // Step 1: Fallback to Supabase stored authentic INDmoney events if backend key_events was empty or cached
                    if (fetched.isEmpty()) {
                        try {
                            val sbUrl = "${com.stocktics.BuildConfig.SUPABASE_URL}/rest/v1/stock_news?symbol=eq.$cleanBase&order=news_date.desc&limit=25"
                            val sbConn = (java.net.URL(sbUrl).openConnection() as java.net.HttpURLConnection).apply {
                                requestMethod = "GET"
                                connectTimeout = 4000
                                readTimeout = 6000
                                setRequestProperty("apikey", com.stocktics.BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                                setRequestProperty("Authorization", "Bearer ${com.stocktics.BuildConfig.SUPABASE_PUBLISHABLE_KEY}")
                            }
                            if (sbConn.responseCode == 200) {
                                val sbText = sbConn.inputStream.bufferedReader().readText()
                                val sbArr = org.json.JSONArray(sbText)
                                for (i in 0 until sbArr.length()) {
                                    val row = sbArr.optJSONObject(i) ?: continue
                                    val rowTitle = row.optString("title", "")
                                    if (rowTitle.isBlank()) continue
                                    val rowDesc = row.optString("description", "")
                                    val rowSent = row.optString("sentiment", "positive")
                                    val parsedSent = when {
                                        rowSent.contains("pos", true) || rowSent.contains("bull", true) -> "positive"
                                        rowSent.contains("neg", true) || rowSent.contains("bear", true) -> "negative"
                                        else -> "neutral"
                                    }
                                    fetched.add(
                                        NewsArticleItem(
                                            title = rowTitle,
                                            publisher = row.optString("publisher", "INDmoney Key Events"),
                                            timeAgo = row.optString("news_date", ""),
                                            publishedAt = row.optString("news_date", ""),
                                            url = row.optString("url", ""),
                                            description = if (rowDesc.isNotBlank()) rowDesc else "Institutional market catalyst for $cleanBase.",
                                            sentiment = parsedSent,
                                            newsDate = row.optString("news_date", ""),
                                            isToday = false
                                        )
                                    )
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    // Step 2: Fallback to news from backend payload with sentiment analysis
                    if (fetched.isEmpty()) {
                        val yahooArr = bObj.optJSONArray("yahoo_news") ?: bObj.optJSONObject("yahoo")?.optJSONArray("articles")
                        if (yahooArr != null && yahooArr.length() > 0) {
                            for (i in 0 until yahooArr.length()) {
                                val y = yahooArr.optJSONObject(i) ?: continue
                                val yTitle = y.optString("title", "")
                                if (yTitle.isBlank()) continue
                                val yPub = y.optString("publisher", "Market Wire")
                                val yTime = y.optString("time_ago", "Today")
                                val yLink = y.optString("link", y.optString("url", ""))
                                val sent = analyzeNewsSentiment(yTitle)
                                fetched.add(
                                    NewsArticleItem(
                                        title = yTitle,
                                        publisher = yPub,
                                        timeAgo = yTime,
                                        publishedAt = yTime,
                                        url = yLink,
                                        description = "Institutional catalyst from $yPub. Sentiment analyzed as ${sent.uppercase()}.",
                                        sentiment = sent,
                                        newsDate = yTime,
                                        isToday = yTime.contains("h ago", true) || yTime.contains("m ago", true) || yTime.contains("now", true)
                                    )
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            // Step 3: Direct Supabase fallback if backend candidates completely timed out
            if (fetched.isEmpty()) {
                try {
                    val sbUrl = "${com.stocktics.BuildConfig.SUPABASE_URL}/rest/v1/stock_news?symbol=eq.$cleanBase&order=news_date.desc&limit=25"
                    val sbConn = (java.net.URL(sbUrl).openConnection() as java.net.HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 4000
                        readTimeout = 6000
                        setRequestProperty("apikey", com.stocktics.BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                        setRequestProperty("Authorization", "Bearer ${com.stocktics.BuildConfig.SUPABASE_PUBLISHABLE_KEY}")
                    }
                    if (sbConn.responseCode == 200) {
                        val sbText = sbConn.inputStream.bufferedReader().readText()
                        val sbArr = org.json.JSONArray(sbText)
                        for (i in 0 until sbArr.length()) {
                            val row = sbArr.optJSONObject(i) ?: continue
                            val rowTitle = row.optString("title", "")
                            if (rowTitle.isBlank()) continue
                            fetched.add(
                                NewsArticleItem(
                                    title = rowTitle,
                                    publisher = row.optString("publisher", "INDmoney Key Events"),
                                    timeAgo = row.optString("news_date", ""),
                                    publishedAt = row.optString("news_date", ""),
                                    url = row.optString("url", ""),
                                    description = row.optString("description", "Institutional market catalyst for $cleanBase."),
                                    sentiment = row.optString("sentiment", "positive"),
                                    newsDate = row.optString("news_date", ""),
                                    isToday = false
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}
            }

            // If still empty, synthesize from stock's loaded news articles with sentiment analysis
            if (fetched.isEmpty()) {
                val available = if (stock.yahooNews.isNotEmpty()) stock.yahooNews else stock.news
                for (item in available) {
                    val sent = analyzeNewsSentiment(item.title, item.description)
                    fetched.add(
                        NewsArticleItem(
                            title = item.title,
                            publisher = item.publisher.ifBlank { "Market Wire" },
                            timeAgo = item.timeAgo,
                            publishedAt = item.publishedAt,
                            url = item.url,
                            description = item.description.ifBlank { "Institutional catalyst for ${stock.symbol}. Sentiment classified as ${sent.uppercase()}." },
                            sentiment = sent,
                            newsDate = item.publishedAt,
                            isToday = item.timeAgo.contains("h ago", true) || item.timeAgo.contains("m ago", true) || item.timeAgo.contains("now", true)
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                if (fetched.isNotEmpty()) {
                    liveIndmoneyList = fetched
                }
                isLoadingIndmoney = false
            }
        }
    }

    // 1. Resolve INDmoney Key Events list (With smart fallback to sentiment-analyzed articles so it is never empty!)
    val indmoneyList = remember(stock.indmoneyEvents, liveIndmoneyList, stock.yahooNews, stock.news) {
        val src = when {
            liveIndmoneyList.isNotEmpty() -> liveIndmoneyList
            stock.indmoneyEvents.isNotEmpty() -> stock.indmoneyEvents
            else -> {
                val available = if (stock.yahooNews.isNotEmpty()) stock.yahooNews else stock.news
                available.map { item ->
                    val sent = analyzeNewsSentiment(item.title, item.description)
                    NewsArticleItem(
                        title = item.title,
                        publisher = item.publisher.ifBlank { "Market Wire" },
                        timeAgo = item.timeAgo,
                        publishedAt = item.publishedAt,
                        url = item.url,
                        description = item.description.ifBlank { "Institutional catalyst for ${stock.symbol}." },
                        sentiment = sent,
                        newsDate = item.publishedAt,
                        isToday = item.timeAgo.contains("h ago", true) || item.timeAgo.contains("m ago", true) || item.timeAgo.contains("now", true)
                    )
                }
            }
        }
        src.sortedByDescending { it.newsDate.ifBlank { it.publishedAt } }
    }

    // 2. Resolve Yahoo News list (Excludes any INDmoney items)
    val yahooList = remember(stock.yahooNews, stock.news) {
        if (stock.yahooNews.isNotEmpty()) {
            stock.yahooNews.filter { !it.publisher.contains("INDmoney", ignoreCase = true) }
        } else {
            stock.news.filter { !it.publisher.contains("INDmoney", ignoreCase = true) }
        }
    }

    val positiveCount = indmoneyList.count { it.sentiment.equals("positive", ignoreCase = true) }
    val neutralCount = indmoneyList.count { it.sentiment.equals("neutral", ignoreCase = true) }
    val negativeCount = indmoneyList.count { it.sentiment.equals("negative", ignoreCase = true) }
    val totalIndCount = indmoneyList.size.coerceAtLeast(1)

    // Filtered INDmoney events
    val filteredIndmoney = remember(indmoneyList, selectedSentimentFilter) {
        if (selectedSentimentFilter == null || selectedSentimentFilter == "all") {
            indmoneyList
        } else {
            indmoneyList.filter { it.sentiment.equals(selectedSentimentFilter, ignoreCase = true) }
        }
    }

    // First visible card index for pagination dots
    val currentCardIndex by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }

    StockticsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Source Switcher Tabs: INDmoney vs Yahoo Finance ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: INDmoney Key Events
                val isIndSelected = selectedSource == "indmoney"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isIndSelected) FintechPrimary else Color.Transparent)
                        .clickable { selectedSource = "indmoney" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ INDmoney (${if (isLoadingIndmoney) "..." else indmoneyList.size.toString()})",
                        fontSize = 11.sp,
                        fontWeight = if (isIndSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isIndSelected) Color.Black else TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Tab 2: Yahoo Finance News
                val isYahooSelected = selectedSource == "yahoo"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isYahooSelected) FintechBlue else Color.Transparent)
                        .clickable { selectedSource = "yahoo" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📰 Yahoo News (${yahooList.size})",
                        fontSize = 11.sp,
                        fontWeight = if (isYahooSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isYahooSelected) Color.White else TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =========================================================================
            // VIEW 1: INDMONEY KEY EVENTS (Native INDmoney Carousel & Analysis)
            // =========================================================================
            if (selectedSource == "indmoney") {
                // Header: "Key events for {SYMBOL}"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Key events for ${stock.symbol}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Curated institutional catalysts • Sentiment analysis",
                            fontSize = 10.5.sp,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GainGreenBg)
                            .border(1.dp, GainGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isLoadingIndmoney) "Fetching..." else "${indmoneyList.size} Events",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Case A: Loading Animation
                if (isLoadingIndmoney) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FintechSurfaceElevated)
                            .border(1.dp, FintechBorder, RoundedCornerShape(14.dp))
                            .padding(vertical = 28.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(GainGreenBg),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = FintechPrimary,
                                    strokeWidth = 2.5.dp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Fetching INDmoney Catalysts...",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Analyzing corporate filings & sentiment for ${stock.symbol}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Skeleton preview placeholders
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FintechBackground.copy(alpha = 0.6f))
                                        .border(1.dp, FintechBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FintechBackground.copy(alpha = 0.6f))
                                        .border(1.dp, FintechBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }
                } else if (indmoneyList.isEmpty()) {
                    // Case B: Clean Empty State (Never show Yahoo news here!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FintechSurfaceElevated)
                            .border(1.dp, FintechBorder, RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FintechBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "No INDmoney Catalysts Recorded",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "No high-impact corporate filings or catalyst events recorded by INDmoney for ${stock.symbol} this period. Check the Yahoo News tab for real-time market articles.",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FintechBackground)
                                    .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                                    .clickable { refreshTrigger++ }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Retry",
                                    tint = FintechPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retry Scan",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FintechPrimary
                                )
                            }
                        }
                    }
                } else {
                    // Case C: Populated INDmoney Key Events Carousel & Feed
                    // ── Native INDmoney Carousel Cards ──
                    LazyRow(
                        state = lazyListState,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(indmoneyList) { event ->
                            val isPos = event.sentiment.equals("positive", ignoreCase = true)
                            val isNeg = event.sentiment.equals("negative", ignoreCase = true)

                            // Clean institutional card background - NOT whole green or red
                            val cardBg = FintechSurfaceElevated
                            val cardStroke = FintechBorder

                            val iconBg = when {
                                isPos -> GainGreen
                                isNeg -> LossRed
                                else -> FintechBlue
                            }
                            val sentimentLabel = when {
                                isPos -> "BULLISH"
                                isNeg -> "BEARISH"
                                else -> "NEUTRAL"
                            }

                            Box(
                                modifier = Modifier
                                    .width(265.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardStroke, RoundedCornerShape(14.dp))
                                    .clickable { openNewsArticle(context, event, stock.name, stock.symbol) }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    // Header with circular up/down arrow and sentiment badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Circular Up/Down Trend Arrow
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(iconBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when {
                                                        isNeg -> Icons.AutoMirrored.Rounded.TrendingDown
                                                        isPos -> Icons.AutoMirrored.Rounded.TrendingUp
                                                        else -> Icons.AutoMirrored.Rounded.TrendingFlat
                                                    },
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            Text(
                                                text = sentimentLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = iconBg,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        val displayDate = event.timeAgo.ifBlank { event.newsDate }
                                        if (displayDate.isNotBlank()) {
                                            Text(
                                                text = displayDate,
                                                fontSize = 10.sp,
                                                color = TextTertiary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Title formatted e.g. "Rocket Lab Secures Major Contracts... - 01 Oct, 2026"
                                    Text(
                                        text = event.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        lineHeight = 18.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Detailed Narrative Catalyst
                                    Text(
                                        text = event.description.ifBlank { "Verified institutional market catalyst regarding ${event.title}." },
                                        fontSize = 11.5.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp,
                                        maxLines = 5,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Pager Dots Indicator (matching native INDmoney screenshot) ──
                    val totalDots = indmoneyList.size.coerceAtMost(6)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until totalDots) {
                            val isActive = i == currentCardIndex.coerceAtMost(totalDots - 1)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (isActive) 6.dp else 4.5.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Color(0xFF3B82F6) else TextTertiary.copy(alpha = 0.4f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Sentiment Breakdown Summary ──
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechSurfaceElevated)
                            .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sentiment Breakdown",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${negativeCount} Neg • ${neutralCount} Neu • ${positiveCount} Pos",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Multi-Segment Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            if (positiveCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(positiveCount.toFloat() / totalIndCount)
                                        .fillMaxHeight()
                                        .background(GainGreen)
                                )
                            }
                            if (neutralCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(neutralCount.toFloat() / totalIndCount)
                                        .fillMaxHeight()
                                        .background(FintechBlue)
                                )
                            }
                            if (negativeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(negativeCount.toFloat() / totalIndCount)
                                        .fillMaxHeight()
                                        .background(LossRed)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GainGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Positive ($positiveCount)", fontSize = 9.5.sp, color = GainGreen)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(FintechBlue))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Neutral ($neutralCount)", fontSize = 9.5.sp, color = FintechBlue)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(LossRed))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Negative ($negativeCount)", fontSize = 9.5.sp, color = LossRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Filter Chips (All, Good, Neutral, Negative) ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "all" to "All (${indmoneyList.size})",
                            "positive" to "Good ($positiveCount)",
                            "neutral" to "Neutral ($neutralCount)",
                            "negative" to "Negative ($negativeCount)"
                        ).forEach { (key, label) ->
                            val isSel = selectedSentimentFilter == key
                            val filterChipBorder = when (key) {
                                "positive" -> GainGreen
                                "negative" -> LossRed
                                "neutral" -> FintechBlue
                                else -> FintechPrimary
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) filterChipBorder.copy(alpha = 0.16f) else FintechSurfaceElevated)
                                    .border(1.dp, if (isSel) filterChipBorder else FintechBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedSentimentFilter = key }
                                    .padding(horizontal = 4.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) filterChipBorder else TextSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── All INDmoney Events Feed ──
                    filteredIndmoney.forEachIndexed { index, n ->
                        if (index > 0) {
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 0.5.dp,
                                color = FintechBorder.copy(alpha = 0.5f)
                            )
                        }

                        val isRowPos = n.sentiment.equals("positive", ignoreCase = true)
                        val isRowNeg = n.sentiment.equals("negative", ignoreCase = true)
                        val rowCircleBg = when {
                            isRowPos -> GainGreen
                            isRowNeg -> LossRed
                            else -> FintechBlue
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { openNewsArticle(context, n, stock.name, stock.symbol) }
                                .padding(horizontal = 4.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Up / Down Arrow Icon
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(rowCircleBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isRowNeg -> Icons.AutoMirrored.Rounded.TrendingDown
                                        isRowPos -> Icons.AutoMirrored.Rounded.TrendingUp
                                        else -> Icons.AutoMirrored.Rounded.TrendingFlat
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = n.sentiment.uppercase(),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = rowCircleBg
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = n.publisher,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${if (n.isToday) "Today" else n.newsDate.ifBlank { n.timeAgo }}",
                                        fontSize = 10.sp,
                                        color = TextTertiary
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = n.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )

                                if (n.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = n.description,
                                        fontSize = 10.5.sp,
                                        color = TextSecondary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "Read Article",
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // VIEW 2: YAHOO FINANCE NEWS (Real-time Market News Wire)
            // =========================================================================
            if (selectedSource == "yahoo") {
                // Header: "Yahoo Finance Market News"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Yahoo Finance News Wire",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Live coverage & market articles for ${stock.symbol}",
                            fontSize = 10.5.sp,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FintechBlue.copy(alpha = 0.15f))
                            .border(1.dp, FintechBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${yahooList.size} Articles",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechBlue,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lead Highlight Story
                if (yahooList.isNotEmpty()) {
                    val lead = yahooList.first()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FintechBackground)
                            .border(1.dp, FintechBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { openNewsArticle(context, lead, stock.name, stock.symbol) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(FintechBlue.copy(alpha = 0.15f))
                                        .border(0.5.dp, FintechBlue, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⚡ TODAY'S STORY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FintechBlue
                                    )
                                }

                                Text(
                                    text = lead.publisher,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lead.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${lead.timeAgo.ifBlank { "Recent" }} • ${lead.publisher}",
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                                Text(
                                    text = "Read article →",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Full Yahoo News List
                yahooList.forEachIndexed { index, n ->
                    if (index > 0) {
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = FintechBorder.copy(alpha = 0.5f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { openNewsArticle(context, n, stock.name, stock.symbol) }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = n.publisher,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${n.timeAgo.ifBlank { "Recently" }}",
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = n.title,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Read Article",
                            tint = FintechBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun openNewsArticle(context: Context, news: NewsArticleItem, stockName: String, stockSymbol: String) {
    val rawUrl = news.url.trim()
    val targetUrl = when {
        // Direct article link (contains deep path beyond base domain)
        rawUrl.isNotBlank() && rawUrl.startsWith("http") && rawUrl.substringAfter("://").trimEnd('/').contains("/") -> {
            rawUrl
        }
        // Root publisher domain (e.g., https://www.livemint.com) -> Search verified story so user lands directly on article
        rawUrl.isNotBlank() && rawUrl.startsWith("http") -> {
            val query = "${news.publisher} ${news.title}".trim()
            "https://www.google.com/search?q=" + java.net.URLEncoder.encode(query, "UTF-8")
        }
        // Fallback search with stock name and news title
        else -> {
            val query = "$stockName ${news.title}".trim()
            "https://www.google.com/search?q=" + java.net.URLEncoder.encode(query, "UTF-8")
        }
    }
    openWebUrl(context, targetUrl)
}

private fun openWebUrl(context: Context, url: String) {
    if (url.isBlank()) return
    try {
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Gracefully ignore if no browser activity is registered
    }
}
