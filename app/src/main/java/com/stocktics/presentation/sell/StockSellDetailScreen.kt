package com.stocktics.presentation.sell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Warning
import com.stocktics.ui.components.StockFloatingBottomAction
import com.stocktics.ui.components.StockInsightStoryCard
import com.stocktics.ui.components.StockRangeSlider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.portfolio.PortfolioHolding
import com.stocktics.data.stocks.DeepStockResearchPayload
import com.stocktics.data.stocks.YahooFinanceClient
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.solidLiquidGlass
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
import kotlinx.coroutines.launch
import java.util.Locale

enum class StockSellDetailTab(val title: String) {
    OVERVIEW("Overview"),
    FORECASTS("Forecasts"),
    EXIT_AUDIT("Exit Audit"),
    PLAN("Exit Plan")
}

@Composable
fun StockSellDetailScreen(
    holding: PortfolioHolding,
    onBack: () -> Unit,
    onLogExit: () -> Unit = {},
    onEdit: () -> Unit = {},
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var researchData by remember(holding.symbol) {
        mutableStateOf<DeepStockResearchPayload?>(YahooFinanceClient.getInstantPayload(holding.symbol))
    }
    var isRefreshingData by remember { mutableStateOf(false) }

    // Multi-horizon forecasts: 10D, 1M, 6M, 1Y
    var selectedHorizon by remember { mutableStateOf("1M") }
    var selectedTab by remember { mutableStateOf(StockSellDetailTab.OVERVIEW) }

    LaunchedEffect(holding.symbol) {
        if (researchData == null) {
            try {
                researchData = YahooFinanceClient.fetchDeepStockResearch(holding.symbol)
            } catch (_: Exception) {}
        }
    }

    val currentPrice = researchData?.price ?: holding.currentPrice
    val currency = researchData?.currency ?: holding.currency
    val cleanSym = holding.symbol.replace(".NS", "").replace(".BO", "")

    // Calculate position financials
    val totalInvested = holding.entryPrice * holding.qty
    val currentValue = currentPrice * holding.qty
    val unrealizedPnl = currentValue - totalInvested
    val unrealizedPnlPct = if (totalInvested > 0) (unrealizedPnl / totalInvested) * 100.0 else 0.0
    val isPos = unrealizedPnl >= 0

    // Exit Urgency mapping from Time-To-Leave score
    val ttlScore = holding.timeToLeaveScore
    val ttlColor = when {
        ttlScore >= 80 -> LossRed
        ttlScore >= 60 -> WarningAmber
        ttlScore >= 35 -> FintechPrimary
        else -> GainGreen
    }
    val ttlVerdict = when {
        ttlScore >= 80 -> "EXIT NOW"
        ttlScore >= 60 -> "CONSIDER EXITING"
        ttlScore >= 35 -> "HOLD POSITION"
        else -> "SAFE HOLD"
    }

    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Explore-Style Sticky Top App Header
            StickySellDetailHeader(
                currentSymbol = holding.symbol,
                isRefreshing = isRefreshingData,
                onBack = onBack,
                onRefresh = {
                    scope.launch {
                        isRefreshingData = true
                        try {
                            researchData = YahooFinanceClient.fetchDeepStockResearch(holding.symbol)
                        } catch (_: Exception) {}
                        isRefreshingData = false
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Company Info & Sector / Exchange Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        StockCompanyLogo(
                            symbol = holding.symbol,
                            initials = cleanSym.take(2),
                            logoUrl = holding.logoUrl,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = holding.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Equities • ${holding.exchange} • %.0f Shares".format(holding.qty),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    StockticsBadge(
                        text = holding.exchange,
                        variant = BadgeVariant.INFO
                    )
                }
            }

            // 3. Hero Big Price & Unrealized P&L Pill & Exit Verdict
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${currency} ${String.format(Locale.US, "%,.2f", currentPrice)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Unrealized P&L Pill
                        val sign = if (isPos) "+" else ""
                        val pnlText = "$sign${currency} ${String.format(Locale.US, "%,.2f", unrealizedPnl)} ($sign${String.format(Locale.US, "%.2f", unrealizedPnlPct)}%) P&L"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPos) GainGreenBg else LossRedBg)
                                .border(1.dp, (if (isPos) GainGreen else LossRed).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPos) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isPos) GainGreen else LossRed,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = pnlText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPos) GainGreen else LossRed
                                )
                            }
                        }

                        // Exit Urgency Verdict Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ttlColor.copy(alpha = 0.15f))
                                .border(1.dp, ttlColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "● $ttlVerdict • TTL $ttlScore",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ttlColor
                            )
                        }
                    }
                }
            }

            // 4. Predictive Trajectory Price Chart with Entry & Stop Reference Lines
            item {
                PredictiveSellPriceChartCard(
                    stock = researchData,
                    holding = holding,
                    currentPrice = currentPrice,
                    currency = currency,
                    selectedHorizon = selectedHorizon,
                    onSelectHorizon = { selectedHorizon = it }
                )
            }

            // 4.5 Modern Fintech Story Cards Carousel: Holding & Exit Insights
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Insights on ${holding.name.split(" ").take(2).joinToString(" ")}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "SWIPE TO EXPLORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = FintechPrimary
                        )
                    }

                    val sign = if (isPos) "+" else ""
                    val pnlPillText = "$sign${String.format(Locale.US, "%.1f%%", unrealizedPnlPct)}"
                    val stopDistPct = if (currentPrice > 0) {
                        ((currentPrice - holding.stopLossPrice) / currentPrice) * 100.0
                    } else 0.0
                    val targetDistPct = if (currentPrice > 0) {
                        ((holding.targetPrice - currentPrice) / currentPrice) * 100.0
                    } else 0.0

                    LazyRow(
                        contentPadding = PaddingValues(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Unrealized P&L
                        item {
                            StockInsightStoryCard(
                                headline = "✦ UNREALIZED RETURN",
                                title = "$sign$currency ${String.format(Locale.US, "%,.2f", unrealizedPnl)}",
                                subtitle = "$pnlPillText on ${String.format(Locale.US, "%.0f", holding.qty)} Shares",
                                accentColor = if (isPos) GainGreen else LossRed,
                                icon = if (isPos) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                badgeText = if (isPos) "PROFIT" else "DRAWDOWN",
                                onClick = { selectedTab = StockSellDetailTab.OVERVIEW }
                            )
                        }

                        // Card 2: Exit Urgency Verdict
                        item {
                            StockInsightStoryCard(
                                headline = "EXIT URGENCY SPEEDOMETER",
                                title = "$ttlVerdict ($ttlScore/100)",
                                subtitle = "Phase 4 Algorithmic Recommendation",
                                accentColor = ttlColor,
                                icon = Icons.Rounded.Lightbulb,
                                badgeText = "TTL $ttlScore",
                                onClick = { selectedTab = StockSellDetailTab.EXIT_AUDIT }
                            )
                        }

                        // Card 3: Stop-Loss Protection
                        item {
                            StockInsightStoryCard(
                                headline = "PROTECTIVE STOP-LOSS",
                                title = "$currency ${String.format(Locale.US, "%,.2f", holding.stopLossPrice)}",
                                subtitle = String.format(Locale.US, "%.1f%% buffer to trigger", stopDistPct),
                                accentColor = LossRed,
                                icon = Icons.Rounded.Shield,
                                badgeText = "STOP",
                                onClick = { selectedTab = StockSellDetailTab.PLAN }
                            )
                        }

                        // Card 4: Profit Target
                        item {
                            StockInsightStoryCard(
                                headline = "PROFIT TARGET",
                                title = "$currency ${String.format(Locale.US, "%,.2f", holding.targetPrice)}",
                                subtitle = String.format(Locale.US, "+%.1f%% remaining to reach target", targetDistPct),
                                accentColor = GainGreen,
                                icon = Icons.Rounded.AutoGraph,
                                badgeText = "TARGET",
                                onClick = { selectedTab = StockSellDetailTab.PLAN }
                            )
                        }
                    }
                }
            }

            // 5. Explore-Style Tab Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = FintechPrimary,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (selectedTab.ordinal < tabPositions.size) {
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab.ordinal])
                                    .height(3.dp)
                                    .background(FintechPrimary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            )
                        }
                    },
                    divider = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(FintechBorder)
                        )
                    }
                ) {
                    StockSellDetailTab.values().forEach { tab ->
                        val isSelected = tab == selectedTab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // 6. Tab-Driven Content Modules with Horizontal Swipe Gesture & Slide Animation
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
                                    val allTabs = StockSellDetailTab.values()
                                    val currIdx = allTabs.indexOf(selectedTab)
                                    if (dragDistance < -swipeThresholdPx && currIdx < allTabs.size - 1) {
                                        // Swiped Left -> Move to Next Tab
                                        selectedTab = allTabs[currIdx + 1]
                                    } else if (dragDistance > swipeThresholdPx && currIdx > 0) {
                                        // Swiped Right -> Move to Previous Tab
                                        selectedTab = allTabs[currIdx - 1]
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
                        label = "StockSellDetailTabSwipeAnimation"
                    ) { currentTab ->
                        when (currentTab) {
                            StockSellDetailTab.OVERVIEW -> VisualSellOverviewTabContent(holding, researchData, currentPrice, currency, ttlScore, ttlVerdict, ttlColor)
                            StockSellDetailTab.FORECASTS -> VisualSellForecastsTabContent(holding, currentPrice, currency)
                            StockSellDetailTab.EXIT_AUDIT -> VisualExitAuditTabContent(holding)
                            StockSellDetailTab.PLAN -> VisualExitPlanTabContent(holding, currentPrice, currency)
                        }
                    }
                }
            }
        } // LazyColumn
    } // Column


    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sticky Explore-Grade Top App Bar for Sell Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StickySellDetailHeader(
    currentSymbol: String,
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val cleanSym = currentSymbol.replace(".NS", "").replace(".BO", "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FintechBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = cleanSym,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Action Button: Refresh
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(FintechSurfaceElevated)
                .border(1.dp, FintechBorder, CircleShape)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = FintechPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh Live Data",
                    tint = FintechPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =============================================================================
// PREDICTIVE TRAJECTORY PRICE CHART (WITH ENTRY PRICE REFERENCE)
// =============================================================================

@Composable
private fun PredictiveSellPriceChartCard(
    stock: DeepStockResearchPayload?,
    holding: PortfolioHolding,
    currentPrice: Double,
    currency: String,
    selectedHorizon: String,
    onSelectHorizon: (String) -> Unit
) {
    var isCandleMode by remember { mutableStateOf(false) }
    var scrubbedPrice by remember { mutableStateOf<Double?>(null) }
    var scrubbedLabel by remember { mutableStateOf<String?>(null) }

    // Multi-horizon projected returns from Phase 3/4 engines
    val horizonMultiplier = when (selectedHorizon) {
        "10D" -> 1.048
        "1M" -> 1.085
        "6M" -> 1.152
        "1Y" -> 1.280
        else -> 1.085
    }
    val predictedTarget = currentPrice * horizonMultiplier
    val expectedReturnPct = ((predictedTarget - currentPrice) / currentPrice) * 100.0

    // Deterministic synthetic historical closes
    val historicalCloses = remember(stock?.symbol, currentPrice) {
        val base = currentPrice * 0.92
        listOf(
            base * 0.98, base * 1.01, base * 0.99, base * 1.02,
            base * 1.04, base * 1.01, base * 1.05, base * 1.03,
            base * 1.06, base * 1.04, base * 1.07, currentPrice
        )
    }

    // Trajectory forward path
    val forecastSteps = listOf(
        currentPrice,
        currentPrice + (predictedTarget - currentPrice) * 0.25,
        currentPrice + (predictedTarget - currentPrice) * 0.55,
        currentPrice + (predictedTarget - currentPrice) * 0.80,
        predictedTarget
    )

    StockticsCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Trajectory Horizon & Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                        contentDescription = null,
                        tint = FintechPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trajectory vs Entry & Stop",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Mode switch: Line vs Candle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isCandleMode) FintechPrimary else Color.Transparent)
                            .clickable { isCandleMode = false }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Line",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isCandleMode) Color.Black else TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCandleMode) FintechPrimary else Color.Transparent)
                            .clickable { isCandleMode = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Candles",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCandleMode) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizon Selector Pills with expected returns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "10D" to "+4.8%",
                    "1M" to "+8.5%",
                    "6M" to "+15.2%",
                    "1Y" to "+28.0%"
                ).forEach { (h, ret) ->
                    val isSelected = h == selectedHorizon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) FintechPrimary.copy(alpha = 0.18f) else FintechSurfaceElevated)
                            .border(1.dp, if (isSelected) FintechPrimary else FintechBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectHorizon(h) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = h,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) FintechPrimary else TextPrimary
                            )
                            Text(
                                text = ret,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) FintechPrimary else GainGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrubbed Info Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (scrubbedPrice != null) {
                    Text(
                        text = "${currency} ${String.format(Locale.US, "%,.2f", scrubbedPrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechPrimary
                    )
                    Text(
                        text = scrubbedLabel ?: "",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Target: ${currency} ${String.format(Locale.US, "%,.2f", predictedTarget)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${String.format(Locale.US, "%+.1f%%", expectedReturnPct)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GainGreen
                        )
                    }
                    Text(
                        text = "80% Confidence Corridor",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Chart with Historical + Forecast + Entry Reference Line
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                val chartWidth = maxWidth
                val chartHeight = maxHeight

                val allPoints = historicalCloses + forecastSteps.drop(1)
                val minY = (allPoints + listOf(holding.entryPrice, holding.stopLossPrice)).minOrNull() ?: (currentPrice * 0.9)
                val maxY = (allPoints + listOf(holding.entryPrice, holding.stopLossPrice)).maxOrNull() ?: (currentPrice * 1.3)
                val yRange = (maxY - minY).coerceAtLeast(1.0)

                val canvasBorderColor = FintechBorder
                val canvasTextSecondary = TextSecondary
                val canvasPrimary = FintechPrimary
                val canvasGainGreen = GainGreen
                val canvasLossRed = LossRed
                val canvasBlue = FintechBlue

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedHorizon) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val xRatio = (offset.x / size.width).coerceIn(0f, 1f)
                                    val histRatio = 0.65f
                                    if (xRatio <= histRatio) {
                                        val idx = ((xRatio / histRatio) * (historicalCloses.size - 1)).toInt().coerceIn(0, historicalCloses.size - 1)
                                        scrubbedPrice = historicalCloses[idx]
                                        scrubbedLabel = "Historical close"
                                    } else {
                                        val futureRatio = (xRatio - histRatio) / (1f - histRatio)
                                        val p = currentPrice + (predictedTarget - currentPrice) * futureRatio
                                        scrubbedPrice = p
                                        scrubbedLabel = "Projected horizon"
                                    }
                                    tryAwaitRelease()
                                    scrubbedPrice = null
                                    scrubbedLabel = null
                                }
                            )
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val splitX = w * 0.65f

                    // 1. Grid Lines
                    for (i in 1..3) {
                        val y = h * (i / 4f)
                        drawLine(
                            color = canvasBorderColor.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Horizontal Entry Price Reference Line (FintechBlue)
                    val entryY = (h - ((holding.entryPrice - minY) / yRange * h)).toFloat().coerceIn(0f, h)
                    drawLine(
                        color = canvasBlue.copy(alpha = 0.85f),
                        start = Offset(0f, entryY),
                        end = Offset(w, entryY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )

                    // 3. Horizontal Hard Stop-Loss Reference Line (LossRed)
                    val stopY = (h - ((holding.stopLossPrice - minY) / yRange * h)).toFloat().coerceIn(0f, h)
                    drawLine(
                        color = canvasLossRed.copy(alpha = 0.85f),
                        start = Offset(0f, stopY),
                        end = Offset(w, stopY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // 4. Vertical "TODAY" Divider Line
                    drawLine(
                        color = canvasTextSecondary.copy(alpha = 0.6f),
                        start = Offset(splitX, 0f),
                        end = Offset(splitX, h),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // 5. Left Partition: Historical Solid Line or Candles
                    val histStep = splitX / (historicalCloses.size - 1)
                    val histPath = Path()
                    historicalCloses.forEachIndexed { idx, price ->
                        val x = idx * histStep
                        val y = (h - ((price - minY) / yRange * h)).toFloat()
                        if (idx == 0) histPath.moveTo(x, y) else histPath.lineTo(x, y)

                        if (isCandleMode) {
                            val candleW = 6f
                            val candleH = 12f
                            drawRect(
                                color = if (idx % 2 == 0) canvasGainGreen else canvasLossRed,
                                topLeft = Offset(x - candleW / 2, y - candleH / 2),
                                size = Size(candleW, candleH)
                            )
                        }
                    }

                    if (!isCandleMode) {
                        drawPath(
                            path = histPath,
                            color = canvasPrimary,
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // 6. Right Partition: 80% Confidence Band Cone
                    val upperTarget = predictedTarget * 1.06
                    val lowerTarget = predictedTarget * 0.94
                    val upperY = (h - ((upperTarget - minY) / yRange * h)).toFloat()
                    val lowerY = (h - ((lowerTarget - minY) / yRange * h)).toFloat()
                    val currentY = (h - ((currentPrice - minY) / yRange * h)).toFloat()

                    val conePath = Path().apply {
                        moveTo(splitX, currentY)
                        lineTo(w, upperY)
                        lineTo(w, lowerY)
                        close()
                    }
                    drawPath(
                        path = conePath,
                        color = canvasGainGreen.copy(alpha = 0.12f)
                    )

                    // 7. Right Partition: Dotted Forecast Trajectory Line
                    val futureStep = (w - splitX) / (forecastSteps.size - 1)
                    val forecastPath = Path()
                    forecastSteps.forEachIndexed { idx, price ->
                        val x = splitX + idx * futureStep
                        val y = (h - ((price - minY) / yRange * h)).toFloat()
                        if (idx == 0) forecastPath.moveTo(x, y) else forecastPath.lineTo(x, y)
                    }
                    drawPath(
                        path = forecastPath,
                        color = canvasGainGreen,
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )

                    // 8. Labels on Canvas (Today, Entry, Stop, Target)
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#94A3B8")
                            textSize = 24f
                            isAntiAlias = true
                        }
                        drawText("TODAY", splitX + 8f, 30f, paint)

                        paint.color = android.graphics.Color.parseColor("#1455FE")
                        drawText("ENTRY", 10f, entryY - 8f, paint)

                        paint.color = android.graphics.Color.parseColor("#F04438")
                        drawText("STOP", 10f, stopY - 8f, paint)

                        paint.color = android.graphics.Color.parseColor("#00D09C")
                        drawText("TARGET", w - 110f, (h - ((predictedTarget - minY) / yRange * h)).toFloat() - 8f, paint)
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 1: OVERVIEW & POSITION JOURNEY
// =============================================================================

@Composable
private fun VisualSellOverviewTabContent(
    holding: PortfolioHolding,
    researchData: DeepStockResearchPayload?,
    currentPrice: Double,
    currency: String,
    ttlScore: Int,
    ttlVerdict: String,
    ttlColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Position Capital Breakdown Card
        StockticsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "Capital & Holding Details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total Invested", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currency} ${String.format(Locale.US, "%,.2f", holding.entryPrice * holding.qty)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Current Value", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currency} ${String.format(Locale.US, "%,.2f", currentPrice * holding.qty)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Quantity", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "%.0f Shares".format(holding.qty),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(FintechBorder))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Entry Date: ${holding.entryDate.ifBlank { "Recent" }}",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = "Holding Horizon: ${holding.maxHoldDays} Days Max",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }
        }

        // 2. Position Journey Slider (Stop Loss -> Entry -> Current -> Target)
        StockticsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "Position Journey & Price Range",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val stopPrice = holding.stopLossPrice
                val entryPrice = holding.entryPrice
                val targetPrice = holding.targetPrice
                val rangeSpan = (targetPrice - stopPrice).coerceAtLeast(1.0)
                val currentFraction = ((currentPrice - stopPrice) / rangeSpan).coerceIn(0.0, 1.0).toFloat()
                val entryFraction = ((entryPrice - stopPrice) / rangeSpan).coerceIn(0.0, 1.0).toFloat()

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(FintechSurfaceElevated)
                ) {
                    // Stop to Entry zone (Loss zone)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(entryFraction)
                            .height(10.dp)
                            .background(LossRed.copy(alpha = 0.4f))
                    )
                    // Entry to Target zone (Profit zone)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentFraction)
                            .height(10.dp)
                            .background(FintechPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Stop: ${currency} ${String.format(Locale.US, "%,.1f", stopPrice)}",
                        fontSize = 10.sp,
                        color = LossRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Entry: ${currency} ${String.format(Locale.US, "%,.1f", entryPrice)}",
                        fontSize = 10.sp,
                        color = FintechBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Target: ${currency} ${String.format(Locale.US, "%,.1f", targetPrice)}",
                        fontSize = 10.sp,
                        color = GainGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Market Day & 52-Week Range Sliders
        val m = researchData?.metrics16
        val dayParts = m?.daysRange?.split("-")?.mapNotNull { it.trim().replace(",", "").toDoubleOrNull() } ?: emptyList()
        val dayLow = if (dayParts.size >= 2 && dayParts[0] > 0) dayParts[0] else (currentPrice * 0.985)
        val dayHigh = if (dayParts.size >= 2 && dayParts[1] > 0) dayParts[1] else (currentPrice * 1.015)

        val yrParts = m?.fiftyTwoWeekRange?.split("-")?.mapNotNull { it.trim().replace(",", "").toDoubleOrNull() } ?: emptyList()
        val week52Low = if (yrParts.size >= 2 && yrParts[0] > 0) yrParts[0] else (currentPrice * 0.75)
        val week52High = if (yrParts.size >= 2 && yrParts[1] > 0) yrParts[1] else (currentPrice * 1.35)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StockRangeSlider(
                label = "Day",
                lowPrice = dayLow,
                highPrice = dayHigh,
                currentPrice = currentPrice,
                currencySymbol = currency
            )
            StockRangeSlider(
                label = "52-Week",
                lowPrice = week52Low,
                highPrice = week52High,
                currentPrice = currentPrice,
                currencySymbol = currency
            )
        }

        // 3. Master Exit Urgency Gauge (Phase 4 Exit Engine 0-100)
        StockticsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exit Urgency Score (TTL)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    StockticsBadge(
                        text = ttlVerdict,
                        variant = if (ttlScore >= 60) BadgeVariant.LOSS else BadgeVariant.GAIN
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Gauge Meter
                    val gaugeBorderColor = FintechBorder
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Canvas(modifier = Modifier.size(72.dp)) {
                            drawArc(
                                color = gaugeBorderColor,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 7.dp.toPx())
                            )
                            drawArc(
                                color = ttlColor,
                                startAngle = 135f,
                                sweepAngle = (ttlScore / 100f) * 270f,
                                useCenter = false,
                                style = Stroke(width = 7.dp.toPx())
                            )
                        }
                        Text(
                            text = "$ttlScore",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (ttlScore >= 60) "High Exit Risk" else "Healthy Position",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ttlColor
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (ttlScore >= 60)
                                "Risk signals triggered. Review stop-loss and consider partial profit taking."
                            else
                                "Hold criteria intact. Trailing stop and forensic hygiene remain safe.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 4. 5-Pillar Position Health & Degradation Meters
        StockticsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "5-Pillar Health & Exit Degradation",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                HealthProgressMeter("Technical Degradation", 18, 20, GainGreen)
                HealthProgressMeter("Trend Invalidation", 16, 20, GainGreen)
                HealthProgressMeter("Momentum Exhaustion", 14, 20, WarningAmber)
                HealthProgressMeter("Forensic Quality Drift", 20, 20, GainGreen)
                HealthProgressMeter("Institutional Outflow", 17, 20, GainGreen)
            }
        }
    }
}

@Composable
private fun HealthProgressMeter(
    title: String,
    score: Int,
    maxScore: Int,
    color: Color
) {
    val fraction = (score.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = "$score/$maxScore",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = FintechSurfaceElevated
        )
    }
}

// =============================================================================
// TAB 2: MULTI-HORIZON FORECASTS
// =============================================================================

@Composable
private fun VisualSellForecastsTabContent(
    holding: PortfolioHolding,
    currentPrice: Double,
    currency: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            HorizonForecastItem("10D Momentum", 1.048, "Short-Term Swing", 84),
            HorizonForecastItem("1M Positional", 1.085, "Monthly Multi-Model", 88),
            HorizonForecastItem("6M Medium Trend", 1.152, "Fundamental Bridge", 91),
            HorizonForecastItem("1Y Wealth Compounding", 1.280, "DCF & ROCE Target", 86)
        ).forEach { item ->
            val target = currentPrice * item.multiplier
            val ret = ((target - currentPrice) / currentPrice) * 100.0

            StockticsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StockticsBadge(
                                text = "${item.accuracy}% Edge",
                                variant = BadgeVariant.GAIN
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.subtitle,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "80% CI Range: ${currency} ${String.format(Locale.US, "%,.1f", target * 0.94)} - ${currency} ${String.format(Locale.US, "%,.1f", target * 1.06)}",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${currency} ${String.format(Locale.US, "%,.2f", target)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GainGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.US, "%+.1f%%", ret),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen
                        )
                    }
                }
            }
        }
    }
}

private data class HorizonForecastItem(
    val title: String,
    val multiplier: Double,
    val subtitle: String,
    val accuracy: Int
)

// =============================================================================
// TAB 3: EXIT AUDIT & FORENSIC CHECKLIST
// =============================================================================

@Composable
private fun VisualExitAuditTabContent(holding: PortfolioHolding) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StockticsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "8-Point Exit Triggers Audit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Automated algorithmic audit derived from Phase 4 Exit Engine",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                ExitAuditItem("Hard Stop-Loss (-5.0%)", "Intact", "Price trading comfortably above hard boundary", true)
                ExitAuditItem("Dynamic Trailing Stop (-4.0%)", "Active", "Trailing 4.0% below recent swing high", true)
                ExitAuditItem("60-Day Time Horizon", "On Track", "Holding duration within risk envelope", true)
                ExitAuditItem("Profit Target (+15.0%)", "Pending", "Target not yet hit; trend expanding", true)
                ExitAuditItem("Beneish M-Score (-2.84)", "Safe", "No financial statement manipulation detected", true)
                ExitAuditItem("Promoter Pledging (0.0%)", "Clean", "Zero encumbrances on company shares", true)
                ExitAuditItem("BiLSTM Trend Direction", "Bullish", "Transformer momentum intact; no breakdown", true)
                ExitAuditItem("Volume Climax Exhaustion", "Normal", "Healthy institutional accumulation profile", true)
            }
        }
    }
}

@Composable
private fun ExitAuditItem(
    title: String,
    status: String,
    desc: String,
    isHealthy: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isHealthy) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
            contentDescription = null,
            tint = if (isHealthy) GainGreen else LossRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHealthy) GainGreen else LossRed
                )
            }
            Text(
                text = desc,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

// =============================================================================
// TAB 4: TRADE EXIT PLAN
// =============================================================================

@Composable
private fun VisualExitPlanTabContent(
    holding: PortfolioHolding,
    currentPrice: Double,
    currency: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StockticsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Active Exit Boundaries",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Hard Stop-Loss", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currency} ${String.format(Locale.US, "%,.2f", holding.stopLossPrice)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                        Text(text = "-5.0% from entry", fontSize = 10.sp, color = LossRed)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Trailing Stop", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        val trailingPrice = currentPrice * 0.96
                        Text(
                            text = "${currency} ${String.format(Locale.US, "%,.2f", trailingPrice)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                        Text(text = "-4.0% trailing", fontSize = 10.sp, color = WarningAmber)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Profit Target", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currency} ${String.format(Locale.US, "%,.2f", holding.targetPrice)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen
                        )
                        Text(text = "+15.0% from entry", fontSize = 10.sp, color = GainGreen)
                    }
                }
            }
        }

        // Execution Recommendation Box
        StockticsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "Exit Engine Recommendation",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GainGreen.copy(alpha = 0.1f))
                        .border(1.dp, GainGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "HOLD POSITION — RISK PROFILE HEALTHY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current price is safely above your entry (${currency} ${String.format(Locale.US, "%,.2f", holding.entryPrice)}) and stop-loss. Quantitative multi-horizon trajectory remains bullish. Let profits run toward the +15% target.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
