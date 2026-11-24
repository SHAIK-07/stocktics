package com.stocktics.presentation.sell

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import com.stocktics.ui.components.StockticsPullToRefreshBox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.portfolio.PortfolioHolding
import com.stocktics.data.portfolio.PortfolioSummary
import com.stocktics.data.portfolio.SupabasePortfolioClient
import com.stocktics.ui.components.liquidGlass
import com.stocktics.ui.components.liquidGlassElevated
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsDangerButton
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBlueDark
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.IsDarkTheme
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.LossRedBg
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import com.stocktics.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun SellScreen(
    currentMarket: com.stocktics.ui.components.GlobalMarket = com.stocktics.ui.components.GlobalMarket.INDIA,
    onNavigateToExplore: () -> Unit = {},
    onHoldingSelected: (PortfolioHolding) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("All Positions") }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var expandedHoldingId by remember { mutableStateOf<Long?>(null) }

    val isIndian = currentMarket == com.stocktics.ui.components.GlobalMarket.INDIA
    val currencySymbol = if (isIndian) "₹" else "$"

    val portfolioTrigger by SupabasePortfolioClient.portfolioVersion.collectAsState()
    var activeHoldings by remember { mutableStateOf<List<PortfolioHolding>>(emptyList()) }
    var portfolioSummary by remember { mutableStateOf(PortfolioSummary()) }

    // Reactive load of active holdings and summary with cloud synchronization partitioned by active market
    LaunchedEffect(currentMarket, portfolioTrigger, isRefreshing) {
        activeHoldings = StockticsDatabase.getActiveHoldings(context, isIndian = isIndian)
        portfolioSummary = StockticsDatabase.getPortfolioSummary(context, isIndian = isIndian)
        try {
            val cloud = SupabasePortfolioClient.fetchHoldingsFromCloud(context)
            if (cloud.isNotEmpty()) {
                activeHoldings = StockticsDatabase.getActiveHoldings(context, isIndian = isIndian)
                portfolioSummary = StockticsDatabase.getPortfolioSummary(context, isIndian = isIndian)
            }
            SupabasePortfolioClient.fetchTransactionsFromCloud(context)
        } catch (_: Exception) {}
    }

    val filteredHoldings = activeHoldings.filter { holding ->
        val matchesSearch = searchQuery.isBlank() ||
                holding.symbol.contains(searchQuery.trim(), ignoreCase = true) ||
                holding.name.contains(searchQuery.trim(), ignoreCase = true)
        val matchesTab = when (selectedTab) {
            "Exit Signals" -> holding.timeToLeaveScore >= 60
            "Safe Holds" -> holding.timeToLeaveScore < 60
            else -> true
        }
        matchesSearch && matchesTab
    }

    StockticsPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                SupabasePortfolioClient.notifyPortfolioChanged()
                kotlinx.coroutines.delay(600)
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FintechBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with Title
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Holdings & Exit",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Exit signals & portfolio risk monitor",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 1B. Hero Image Banner with Interactive Overlays
            item {
                val exitTriggerCount = remember(activeHoldings) {
                    activeHoldings.count { it.timeToLeaveScore >= 60 }
                }
                val safeHoldingsCount = remember(activeHoldings) {
                    activeHoldings.count { it.timeToLeaveScore < 60 }
                }
                val healthPct = remember(activeHoldings) {
                    if (activeHoldings.isEmpty()) 100
                    else ((safeHoldingsCount.toFloat() / activeHoldings.size) * 100).toInt()
                }

                val isDark = IsDarkTheme
                val bannerBorderColor = if (isDark) FintechBorder else Color(0xFFCBD5E1)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF0B0E14) else FintechSurface)
                        .border(1.dp, bannerBorderColor, RoundedCornerShape(16.dp))
                ) {
                    val sellHeroRes = if (isDark) com.stocktics.R.drawable.hero_sell_exit else com.stocktics.R.drawable.hero_sell_exit_light
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = sellHeroRes),
                        contentDescription = "Holdings & Exit",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Theme-adaptive vertical scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = if (isDark) listOf(
                                        Color(0x33070A0F),
                                        Color(0x660B0E14),
                                        Color(0xF2070A0F)
                                    ) else listOf(
                                        Color(0x40FFFFFF),
                                        Color(0x80FFFFFF),
                                        Color(0xF5F8FAFC)
                                    )
                                )
                            )
                    )

                    // Top-right: Portfolio Health Gauge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xCC141923) else Color(0xEEFFFFFF))
                            .border(
                                1.dp,
                                if (exitTriggerCount == 0) GainGreen.copy(alpha = if (isDark) 0.6f else 0.4f)
                                else LossRed.copy(alpha = if (isDark) 0.6f else 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (exitTriggerCount == 0) GainGreen else LossRed)
                            )
                            Text(
                                text = "PORTFOLIO HEALTH: $healthPct% SAFE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                                color = if (exitTriggerCount == 0) GainGreen else LossRed
                            )
                        }
                    }

                    // Bottom overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "PORTFOLIO SENTINEL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    color = if (isDark) Color(0xFFF04438) else Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Exit Signals & Profit Shield",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    letterSpacing = (-0.3).sp
                                )
                            }

                            // Exit trigger count pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xCC141923) else Color(0xEEFFFFFF))
                                    .border(
                                        1.dp,
                                        if (exitTriggerCount == 0) GainGreen.copy(alpha = if (isDark) 0.4f else 0.3f)
                                        else LossRed.copy(alpha = if (isDark) 0.4f else 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (exitTriggerCount > 0) "⚠ $exitTriggerCount Exit Triggers"
                                           else "✓ 0 Exit Triggers",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (exitTriggerCount > 0) LossRed else GainGreen
                                )
                            }
                        }
                    }
                }
            }

        // 2. Metric Summary Strip (Institutional Groww/INDmoney styled matching HomeScreen)
        if (activeHoldings.isNotEmpty()) {
            item {
                StockticsCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Net Portfolio Value",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currencySymbol ${String.format(java.util.Locale.US, "%,.2f", portfolioSummary.currentValue)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            StockticsBadge(
                                text = "${portfolioSummary.activePositionsCount} Positions",
                                variant = if (portfolioSummary.activePositionsCount > 0) BadgeVariant.GAIN else BadgeVariant.MUTED
                            )
                            if (portfolioSummary.totalInvested > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Invested $currencySymbol ${String.format(java.util.Locale.US, "%,.2f", portfolioSummary.totalInvested)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassElevated(12.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$oneDaySign$currencySymbol $oneDayValStr",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOneDayPos) GainGreen else LossRed
                            )
                            Text(
                                text = "($oneDaySign$oneDayPctStr%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOneDayPos) GainGreen else LossRed
                            )
                        }

                        // Vertical Hairline Divider
                        Box(
                            modifier = Modifier
                                .height(34.dp)
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
                                .padding(horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Returns",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalSign$currencySymbol $totalValStr",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTotalPos) GainGreen else LossRed
                            )
                            Text(
                                text = "($totalSign$totalPctStr%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isTotalPos) GainGreen else LossRed
                            )
                        }

                        // Vertical Hairline Divider
                        Box(
                            modifier = Modifier
                                .height(34.dp)
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
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$unrealizedSign$currencySymbol $unrealizedValStr",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnrealizedPos) GainGreen else LossRed
                            )
                            Text(
                                text = "($unrealizedSign$unrealizedPctStr%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isUnrealizedPos) GainGreen else LossRed
                            )
                        }
                    }
                }
            }
        }

        // 3. Segmented Filter Tabs (Matching ExploreScreen)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FintechSurface)
                    .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All Positions", "Exit Signals", "Safe Holds").forEach { tab ->
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) FintechPrimary else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 4. Search Bar
        if (activeHoldings.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechSurface)
                        .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search active positions...",
                                    color = TextTertiary,
                                    fontSize = 13.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                cursorBrush = SolidColor(FintechPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }
        }

        // 5. Positions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monitored Positions (${filteredHoldings.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (portfolioSummary.highRiskCount > 0) {
                    StockticsBadge(
                        text = "${portfolioSummary.highRiskCount} AT RISK",
                        variant = BadgeVariant.LOSS
                    )
                }
            }
        }

        // 6. Active Holdings List or Empty State
        if (filteredHoldings.isEmpty()) {
            item {
                StockticsCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = "Empty",
                            tint = TextTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (activeHoldings.isEmpty()) {
                                "No active portfolio holdings"
                            } else {
                                "No holdings match the selected filter"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log purchases from the Buy Pipeline or detail pages to monitor exit alerts in real-time",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        StockticsPrimaryButton(
                            text = "View Buy Pipeline",
                            onClick = onNavigateToExplore,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
            }
        } else {
            items(filteredHoldings, key = { it.id }) { holding ->
                WatchlistHoldingCard(
                    holding = holding,
                    onClick = { onHoldingSelected(holding) }
                )
            }
        }
    }
}
}

@Composable
private fun WatchlistHoldingCard(
    holding: PortfolioHolding,
    onClick: () -> Unit
) {
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

    val isPos = holding.isPositive
    val sign = if (isPos) "+" else ""

    StockticsCard(
        cornerRadius = 16.dp,
        contentPadding = 16.dp,
        onClick = onClick
    ) {
        // Top Row: Logo + Symbol/Name and Current Price + P&L Badge
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
                    initials = holding.symbol.take(2).uppercase(),
                    logoUrl = holding.logoUrl,
                    size = 42.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val cleanSym = holding.symbol.replace(".NS", "").replace(".BO", "")
                    Text(
                        text = cleanSym,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = holding.name,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${holding.currency} ${String.format(java.util.Locale.US, "%,.2f", holding.currentPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                StockticsBadge(
                    text = "${sign}${String.format(java.util.Locale.US, "%.1f", holding.unrealizedPnlPct)}%",
                    variant = if (isPos) BadgeVariant.GAIN else BadgeVariant.LOSS,
                    showTrendIcon = true
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Middle Sub-Row: Shares Owned + Entry Price & Exit Urgency Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%.0f Shares • Entry: ${holding.currency} ${String.format(java.util.Locale.US, "%,.2f", holding.entryPrice)}".format(holding.qty),
                fontSize = 11.sp,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Urgency Pill + subtle forward indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ttlColor.copy(alpha = 0.12f))
                        .border(0.5.dp, ttlColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$ttlVerdict • $ttlScore",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ttlColor
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
