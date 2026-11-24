package com.stocktics.presentation.sectors

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.api.ApiSectorDetailResponse
import com.stocktics.data.api.ApiSectorStockItem
import com.stocktics.data.api.ApiSectorSummary
import com.stocktics.data.api.ApiSectorsOverviewResponse
import com.stocktics.data.api.StockticsApiClient
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsPullToRefreshBox
import com.stocktics.ui.components.fintechPressBounce
import com.stocktics.ui.components.fintechShimmer
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.rounded.PieChart
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBlueDark
import com.stocktics.ui.theme.FintechBlueDim
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.IsDarkTheme
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.LossRedBg
import com.stocktics.ui.theme.LocalFintechColors
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Sleek Visual Metadata for GICS Sectors with Institutional Gradient Icons
 */
data class SectorVisualTheme(
    val icon: ImageVector,
    val gradientStart: Color,
    val gradientEnd: Color,
    val badgeLabel: String
)

@Composable
fun SectorGradientIcon(
    visual: SectorVisualTheme,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = visual.gradientStart.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(visual.gradientStart, visual.gradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = visual.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getSectorVisual(sector: String): SectorVisualTheme {
    return when {
        sector.contains("Technology", ignoreCase = true) || sector.contains("Tech", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.Memory,
            gradientStart = Color(0xFF2563EB),
            gradientEnd = Color(0xFF1D4ED8),
            badgeLabel = "TECH & AI"
        )
        sector.contains("Financial", ignoreCase = true) || sector.contains("Finance", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.AccountBalance,
            gradientStart = Color(0xFF3B82F6),
            gradientEnd = Color(0xFF1E40AF),
            badgeLabel = "FINANCE"
        )
        sector.contains("Health", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.MedicalServices,
            gradientStart = Color(0xFFEC4899),
            gradientEnd = Color(0xFFBE185D),
            badgeLabel = "HEALTHCARE"
        )
        sector.contains("Industrial", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.Factory,
            gradientStart = Color(0xFFF59E0B),
            gradientEnd = Color(0xFFB45309),
            badgeLabel = "INDUSTRIALS"
        )
        sector.contains("Discretionary", ignoreCase = true) || (sector.contains("Consumer", ignoreCase = true) && !sector.contains("Staple", ignoreCase = true)) -> SectorVisualTheme(
            icon = Icons.Rounded.ShoppingCart,
            gradientStart = Color(0xFF8B5CF6),
            gradientEnd = Color(0xFF6D28D9),
            badgeLabel = "CONSUMER"
        )
        sector.contains("Communication", ignoreCase = true) || sector.contains("Media", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.Wifi,
            gradientStart = Color(0xFF06B6D4),
            gradientEnd = Color(0xFF0369A1),
            badgeLabel = "MEDIA & COMMS"
        )
        sector.contains("Energy", ignoreCase = true) || sector.contains("Oil", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.LocalGasStation,
            gradientStart = Color(0xFFF97316),
            gradientEnd = Color(0xFFC2410C),
            badgeLabel = "ENERGY"
        )
        sector.contains("Staples", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.FitnessCenter,
            gradientStart = Color(0xFF10B981),
            gradientEnd = Color(0xFF047857),
            badgeLabel = "STAPLES"
        )
        sector.contains("Material", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.Build,
            gradientStart = Color(0xFF64748B),
            gradientEnd = Color(0xFF334155),
            badgeLabel = "MATERIALS"
        )
        sector.contains("Real Estate", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.Apartment,
            gradientStart = Color(0xFFEAB308),
            gradientEnd = Color(0xFFA16207),
            badgeLabel = "REITs"
        )
        sector.contains("Utility", ignoreCase = true) || sector.contains("Utilities", ignoreCase = true) -> SectorVisualTheme(
            icon = Icons.Rounded.Bolt,
            gradientStart = Color(0xFF14B8A6),
            gradientEnd = Color(0xFF0F766E),
            badgeLabel = "UTILITIES"
        )
        else -> SectorVisualTheme(
            icon = Icons.Rounded.Category,
            gradientStart = Color(0xFF6366F1),
            gradientEnd = Color(0xFF4338CA),
            badgeLabel = "SECTOR"
        )
    }
}

/**
 * Institutional Sector Explorer & GICS Intelligence Workstation
 * Features:
 * - Real-time 11 GICS sector performance rankings
 * - Top 10 sectors leaderboard carousel with daily % change
 * - 3D visual cards with breadth and outperformer indicators
 * - Granular sector constituent view with top 10 outperformers and watchlist-style stock rows
 * - IN vs US market toggle
 */
@Composable
fun SectorExplorerScreen(
    currentMarket: GlobalMarket,
    onMarketChange: (GlobalMarket) -> Unit = {},
    onStockSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    applyStatusBarsPadding: Boolean = true
) {
    val fintech = LocalFintechColors.current
    val coroutineScope = rememberCoroutineScope()

    var activeMarket by remember(currentMarket) { mutableStateOf(currentMarket) }
    var sectorsOverview by remember { mutableStateOf<ApiSectorsOverviewResponse?>(null) }
    var selectedSectorDetail by remember { mutableStateOf<ApiSectorDetailResponse?>(null) }
    var selectedSectorName by remember { mutableStateOf<String?>(null) }
    var isLoadingOverview by remember { mutableStateOf(true) }
    var isLoadingDetail by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    fun loadOverview(fromRefresh: Boolean = false) {
        coroutineScope.launch {
            if (fromRefresh) isRefreshing = true else isLoadingOverview = true
            val mktCode = if (activeMarket == GlobalMarket.INDIA) "IN" else "US"
            val res = StockticsApiClient.fetchSectorsOverview(mktCode)
            sectorsOverview = res
            isLoadingOverview = false
            isRefreshing = false
        }
    }

    fun loadSectorDetail(sectorName: String) {
        selectedSectorName = sectorName
        coroutineScope.launch {
            isLoadingDetail = true
            val mktCode = if (activeMarket == GlobalMarket.INDIA) "IN" else "US"
            val res = StockticsApiClient.fetchSectorStocks(sectorName, mktCode, limit = 60)
            selectedSectorDetail = res
            isLoadingDetail = false
        }
    }

    LaunchedEffect(activeMarket) {
        selectedSectorName = null
        selectedSectorDetail = null
        loadOverview()
    }

    // Intercept hardware/system back press when inside sector detail
    BackHandler(enabled = selectedSectorName != null) {
        selectedSectorName = null
        selectedSectorDetail = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fintech.background)
            .then(if (applyStatusBarsPadding) Modifier.statusBarsPadding() else Modifier)
            .navigationBarsPadding()
    ) {
        // =========================================================================
        // Top Navigation Header (Identical design to PatternScannerScreen)
        // =========================================================================
        if (selectedSectorName == null) {
            // Overview Mode: Tag + Title + Subtitle + 18dp rounded IN/US Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PieChart,
                            contentDescription = null,
                            tint = FintechBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "11 GICS SECTORS RADAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = FintechBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sector Intelligence",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = fintech.textPrimary
                    )
                    Text(
                        text = if (activeMarket == GlobalMarket.INDIA) "Capital rotation & performance across 11 Indian sectors" else "Capital rotation across US 1,000 & S&P 500 sectors",
                        fontSize = 12.sp,
                        color = fintech.textSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // MARKET TOGGLE (IN / US) - Identical to PatternScannerScreen with FintechBlue
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(fintech.surfaceElevated)
                        .border(1.dp, fintech.border, RoundedCornerShape(18.dp))
                        .padding(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Indian Market Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (activeMarket == GlobalMarket.INDIA) FintechBlue else Color.Transparent)
                                .clickable {
                                    activeMarket = GlobalMarket.INDIA
                                    onMarketChange(GlobalMarket.INDIA)
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🇮🇳 IN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (activeMarket == GlobalMarket.INDIA) Color.White else fintech.textSecondary
                            )
                        }

                        // US Market Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (activeMarket == GlobalMarket.US) FintechBlue else Color.Transparent)
                                .clickable {
                                    activeMarket = GlobalMarket.US
                                    onMarketChange(GlobalMarket.US)
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🇺🇸 US",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (activeMarket == GlobalMarket.US) Color.White else fintech.textSecondary
                            )
                        }
                    }
                }
            }
        } else {
            // Sector Detail Mode: Back Button + Sector Name + Badge + Market Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(fintech.surfaceElevated)
                            .border(1.dp, fintech.border, CircleShape)
                            .clickable {
                                selectedSectorName = null
                                selectedSectorDetail = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Sectors",
                            tint = fintech.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedSectorName ?: "Sector",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = fintech.textPrimary,
                                letterSpacing = (-0.3).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FintechBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CONSTITUENTS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechBlue,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = "Top outperformers & full constituent ranking",
                            fontSize = 11.sp,
                            color = fintech.textTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Market Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(fintech.surfaceElevated)
                        .border(1.dp, fintech.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (activeMarket == GlobalMarket.INDIA) "🇮🇳 NSE" else "🇺🇸 US",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = FintechBlue
                    )
                }
            }
        }

        // Main Content Area with Pull-To-Refresh
        StockticsPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                if (selectedSectorName != null) {
                    loadSectorDetail(selectedSectorName!!)
                } else {
                    loadOverview(fromRefresh = true)
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = selectedSectorName,
                transitionSpec = {
                    if (targetState != null) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "sectorScreenTransition"
            ) { targetSector ->
                if (targetSector == null) {
                    // OVERVIEW SCREEN (All 11 Sectors + Top 10 Leaderboard)
                    SectorOverviewContent(
                        overview = sectorsOverview,
                        isLoading = isLoadingOverview,
                        onSelectSector = { sectorName -> loadSectorDetail(sectorName) },
                        onStockSelected = onStockSelected
                    )
                } else {
                    // DETAIL SCREEN (Constituents of clicked Sector)
                    SectorDetailContent(
                        sectorName = targetSector,
                        detail = selectedSectorDetail,
                        isLoading = isLoadingDetail,
                        onStockSelected = onStockSelected,
                        onBackToOverview = {
                            selectedSectorName = null
                            selectedSectorDetail = null
                        }
                    )
                }
            }
        }
    }
}

/**
 * Main Overview Page: Top 10 Leaderboard Carousel + 11 GICS Sector Visual Cards
 */
@Composable
private fun SectorOverviewContent(
    overview: ApiSectorsOverviewResponse?,
    isLoading: Boolean,
    onSelectSector: (String) -> Unit,
    onStockSelected: (String) -> Unit
) {
    val fintech = LocalFintechColors.current

    if (isLoading && overview == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .fintechShimmer(14.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .fintechShimmer(14.dp)
            )
        }
        return
    }

    val sectors = overview?.sectors ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top 10 Performing Sectors Leaderboard Carousel (Netflix Style)
        item {
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
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = FintechBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sector Performance Leaderboard",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = fintech.textPrimary
                            )
                        }

                        Text(
                            text = "Top Performers 1D",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = fintech.textTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Leaderboard Strip with Netflix-Style Cards
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(sectors) { index, sec ->
                            NetflixSectorLeaderboardCard(
                                sector = sec,
                                rank = index + 1,
                                onClick = { onSelectSector(sec.sector) },
                                onStockSelected = onStockSelected
                            )
                        }
                    }
                }
            }
        }

        // 2. Section Header: 11 GICS Sectors Catalog
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GICS Industry Sectors (${sectors.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = fintech.textPrimary
                )
                Text(
                    text = "Tap sector to view stocks",
                    fontSize = 11.sp,
                    color = fintech.textTertiary
                )
            }
        }

        // 3. 2-Column Grid of Sector Cards (Vertical 2 rows)
        items(sectors.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { sec ->
                    Box(modifier = Modifier.weight(1f)) {
                        SectorCompactGridCard(
                            sector = sec,
                            onClick = { onSelectSector(sec.sector) },
                            onStockSelected = onStockSelected
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Netflix-Style Card for Top Performing Sectors Leaderboard
 */
@Composable
private fun NetflixSectorLeaderboardCard(
    sector: ApiSectorSummary,
    rank: Int,
    onClick: () -> Unit,
    onStockSelected: (String) -> Unit
) {
    val fintech = LocalFintechColors.current
    val isDark = IsDarkTheme
    val visual = getSectorVisual(sector.sector)
    val isPos = sector.changePercent >= 0

    StockticsCard(
        modifier = Modifier
            .width(185.dp)
            .height(240.dp),
        cornerRadius = 16.dp,
        contentPadding = 0.dp,
        fillMaxWidth = false,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Giant Netflix-style watermark outline numeral at bottom-left corner
            Text(
                text = "$rank",
                fontSize = 82.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) Color(0x15F8FAFC) else Color(0x0E0F172A),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Sector Gradient Icon + Change Pill
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectorGradientIcon(
                            visual = visual,
                            size = 38.dp,
                            iconSize = 20.dp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPos) GainGreenBg else LossRedBg)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${if (isPos) "+" else ""}${String.format("%.2f", sector.changePercent)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPos) GainGreen else LossRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = sector.sector,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = fintech.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${sector.totalStocks} STOCKS IN UNIVERSE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = fintech.textTertiary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Middle: Top Stock / Leader Spotlight
                if (sector.topGainerSymbol.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(fintech.surfaceElevated)
                            .border(1.dp, fintech.border, RoundedCornerShape(10.dp))
                            .clickable { onStockSelected(sector.topGainerSymbol) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StockCompanyLogo(
                                symbol = sector.topGainerSymbol,
                                size = 26.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LEADER",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fintech.textTertiary,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = sector.topGainerSymbol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechBlue,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "+${String.format("%.2f", sector.topGainerChangePct)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GainGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Bottom: Breadth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sector.advances} Adv • ${sector.declines} Dec",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = fintech.textSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Explore",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechBlue
                        )
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = FintechBlue,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact GICS Sector Card for 2-Column Grid Layout
 */
@Composable
private fun SectorCompactGridCard(
    sector: ApiSectorSummary,
    onClick: () -> Unit,
    onStockSelected: (String) -> Unit
) {
    val fintech = LocalFintechColors.current
    val visual = getSectorVisual(sector.sector)
    val isPos = sector.changePercent >= 0

    StockticsCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = 12.dp,
        cornerRadius = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Sector Icon + % Change
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectorGradientIcon(
                    visual = visual,
                    size = 34.dp,
                    iconSize = 18.dp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPos) GainGreenBg else LossRedBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${if (isPos) "+" else ""}${String.format("%.2f", sector.changePercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPos) GainGreen else LossRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sector Title + Stock count
            Text(
                text = sector.sector,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = fintech.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${sector.totalStocks} STOCKS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = fintech.textTertiary,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Leader pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(fintech.surfaceElevated)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.clickable {
                            if (sector.topGainerSymbol.isNotBlank()) onStockSelected(sector.topGainerSymbol)
                        }
                    ) {
                        Text(
                            text = "Top:",
                            fontSize = 9.sp,
                            color = fintech.textTertiary
                        )
                        Text(
                            text = sector.topGainerSymbol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechBlue,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "+${String.format("%.1f", sector.topGainerChangePct)}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GainGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${sector.advances} Adv • ${sector.declines} Dec",
                fontSize = 9.sp,
                color = fintech.textTertiary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Detailed Constituent View: Top 10 Outperformers + Complete Stock List (Watchlist Layout)
 */
@Composable
private fun SectorDetailContent(
    sectorName: String,
    detail: ApiSectorDetailResponse?,
    isLoading: Boolean,
    onStockSelected: (String) -> Unit,
    onBackToOverview: () -> Unit
) {
    val fintech = LocalFintechColors.current
    val visual = getSectorVisual(sectorName)

    if (isLoading && detail == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .fintechShimmer(14.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .fintechShimmer(14.dp)
            )
        }
        return
    }

    val stocks = detail?.stocks ?: emptyList()
    val top10 = detail?.top10Outperformers ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Sector Hero Card (With Vector Gradient Icon)
        item {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SectorGradientIcon(
                                visual = visual,
                                size = 48.dp,
                                iconSize = 24.dp
                            )

                            Column {
                                Text(
                                    text = sectorName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fintech.textPrimary
                                )
                                Text(
                                    text = "${detail?.totalStocks ?: stocks.size} Companies in Universe",
                                    fontSize = 11.sp,
                                    color = fintech.textTertiary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FintechBlue.copy(alpha = 0.12f))
                                .border(1.dp, FintechBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable(onClick = onBackToOverview)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "All Sectors",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = detail?.description ?: "Key economic drivers and leading companies.",
                        fontSize = 12.sp,
                        color = fintech.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Top 10 Outperformers Spotlight (Netflix Style)
        if (top10.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top 10 Outperformers Today",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = fintech.textPrimary
                        )
                        Text(
                            text = "Highest 24h Momentum",
                            fontSize = 10.sp,
                            color = fintech.textTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(top10) { idx, stock ->
                            NetflixStockConstituentCard(
                                stock = stock,
                                rank = idx + 1,
                                onClick = { onStockSelected(stock.ticker) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Section Header for Complete Constituents List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sector Stocks (${stocks.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = fintech.textPrimary
                )
                Text(
                    text = "Price / Change / Score",
                    fontSize = 10.sp,
                    color = fintech.textTertiary
                )
            }
        }

        // 4. Watchlist-Style Stock Rows with Authentic Stock Logos
        items(stocks) { stock ->
            SectorStockRowItem(
                stock = stock,
                onClick = { onStockSelected(stock.ticker) }
            )
        }
    }
}

/**
 * Netflix-Style Card for Constituent Stocks inside Sector Detail
 */
@Composable
private fun NetflixStockConstituentCard(
    stock: ApiSectorStockItem,
    rank: Int,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current
    val isDark = IsDarkTheme
    val isPos = stock.changePercent >= 0
    val currencySym = if (stock.market == "IN" || stock.ticker.endsWith(".NS")) "₹" else "$"

    StockticsCard(
        modifier = Modifier
            .width(190.dp)
            .height(260.dp),
        cornerRadius = 16.dp,
        contentPadding = 0.dp,
        fillMaxWidth = false,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Giant Netflix-style watermark outline numeral at bottom-left corner
            Text(
                text = "$rank",
                fontSize = 82.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) Color(0x15F8FAFC) else Color(0x0E0F172A),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Stock Logo + Exchange Badge + Symbol + Name
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StockCompanyLogo(
                            symbol = stock.ticker,
                            logoUrl = stock.logoUrl,
                            size = 38.dp
                        )
                        StockticsBadge(
                            text = stock.exchange,
                            variant = BadgeVariant.INFO
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stock.ticker.replace(".NS", "").replace(".BO", ""),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = fintech.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = stock.name,
                        fontSize = 11.sp,
                        color = fintech.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Middle: 24H Momentum Pill (Big Bold Style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPos) GainGreenBg else LossRedBg)
                        .border(1.dp, (if (isPos) GainGreen else LossRed).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${if (isPos) "+" else ""}${String.format("%.2f", stock.changePercent)}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isPos) GainGreen else LossRed
                        )
                        Text(
                            text = "24H MOMENTUM",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = if (isPos) GainGreen else LossRed
                        )
                    }
                }

                // Bottom: Price & Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currencySym${String.format("%.2f", stock.price)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = fintech.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(fintech.surfaceElevated)
                            .border(1.dp, fintech.border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${stock.quantScore} SCORE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechBlue,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * Watchlist-style constituent row item with Authentic Stock Logo
 */
@Composable
private fun SectorStockRowItem(
    stock: ApiSectorStockItem,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current
    val isPos = stock.changePercent >= 0
    val currencySym = if (stock.market == "IN" || stock.ticker.endsWith(".NS")) "₹" else "$"

    StockticsCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Logo, Ticker & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                StockCompanyLogo(
                    symbol = stock.ticker,
                    logoUrl = stock.logoUrl,
                    size = 38.dp
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.ticker.replace(".NS", ""),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = fintech.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(fintech.border)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stock.exchange,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = fintech.textTertiary
                            )
                        }
                    }
                    Text(
                        text = stock.name,
                        fontSize = 11.sp,
                        color = fintech.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Middle: Quant Score Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(fintech.surfaceElevated)
                    .border(1.dp, fintech.border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${stock.quantScore} SCORE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechBlue,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Price & 24h Change Pill
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$currencySym${String.format("%.2f", stock.price)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = fintech.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPos) GainGreenBg else LossRedBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${if (isPos) "+" else ""}${String.format("%.2f", stock.changePercent)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPos) GainGreen else LossRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

