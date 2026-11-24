package com.stocktics.presentation.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.stocktics.ui.components.StockticsPullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.stocks.CategoryStockItem
import com.stocktics.data.stocks.MarketIndexItem
import com.stocktics.data.stocks.StockCategory
import com.stocktics.data.stocks.StockCategoryRepository
import com.stocktics.data.stocks.StockSearchItem
import com.stocktics.data.stocks.YahooFinanceClient
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBlueDark
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.IsDarkTheme
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import com.stocktics.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale



@Composable
fun ExploreScreen(
    currentMarket: GlobalMarket = GlobalMarket.INDIA,
    onStockSelected: (String) -> Unit = {},
    onBack: (() -> Unit)? = null,
    onViewAllCategory: ((StockCategory, CategoryMarketFilter) -> Unit)? = null,
    onOpenSectors: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val isIndian = currentMarket == GlobalMarket.INDIA
    val listState = rememberLazyListState()

    // 1. Live Market Indices for the Ticker Billboard
    var marketIndices by remember(currentMarket) {
        mutableStateOf(YahooFinanceClient.getMarketIndices(isIndian))
    }

    val tickerIndices = remember(marketIndices) {
        if (marketIndices.isNotEmpty()) {
            List(12) { marketIndices }.flatten()
        } else {
            emptyList()
        }
    }
    val tickerScrollState = rememberLazyListState()

    // Smooth continuous auto-scrolling ticker tape
    LaunchedEffect(tickerIndices) {
        if (tickerIndices.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(22)
            if (!tickerScrollState.isScrollInProgress) {
                tickerScrollState.scrollBy(1.2f)
                if (tickerScrollState.firstVisibleItemIndex >= marketIndices.size * 6) {
                    tickerScrollState.scrollToItem(
                        index = tickerScrollState.firstVisibleItemIndex % marketIndices.size,
                        scrollOffset = tickerScrollState.firstVisibleItemScrollOffset
                    )
                }
            }
        }
    }

    // 2. Curated Stocks per Category (5 Active Setups per category)
    var exploreCategoryStocksMap by remember(isIndian) {
        mutableStateOf(
            StockCategory.values().associateWith { cat ->
                StockCategoryRepository.getTopStocks(cat, limit = 5, isIndian = isIndian)
            }
        )
    }

    LaunchedEffect(isIndian) {
        try {
            val allItems = exploreCategoryStocksMap.values.flatten()
            val enriched = StockCategoryRepository.enrichWithLiveQuotes(allItems)
            val enrichedMap = StockCategory.values().associateWith { cat ->
                enriched.filter { it.category == cat && it.isIndian == isIndian }.take(5)
            }
            exploreCategoryStocksMap = enrichedMap
        } catch (_: Exception) {}
    }

    // 3. Periodic Live Price Refresh
    LaunchedEffect(currentMarket, isRefreshing) {
        try {
            val live = YahooFinanceClient.fetchLiveMarketIndices(isIndian)
            if (live.isNotEmpty()) {
                marketIndices = live
            }
        } catch (_: Exception) {}
    }

    // 4. Top Setups This Week (Netflix Style 10 Stocks)
    val topSetupsList = remember(isIndian) {
        if (isIndian) {
            listOf(
                TopWeeklySetupItem(1, "RELIANCE.NS", "Reliance Industries", "NSE", "94.5", "90", "98.0", "100", "BREAKOUT", "https://assets.parqet.com/logos/symbol/RELIANCE.NS", "RI", true),
                TopWeeklySetupItem(2, "TCS.NS", "Tata Consultancy Services", "NSE", "93.8", "100", "91.4", "95.0", "MULTI YEAR BASE", "https://assets.parqet.com/logos/symbol/TCS.NS", "TC", true),
                TopWeeklySetupItem(3, "HDFCBANK.NS", "HDFC Bank Ltd", "NSE", "92.9", "88", "94.2", "99.0", "VCP", "https://assets.parqet.com/logos/symbol/HDFCBANK.NS", "HD", true),
                TopWeeklySetupItem(4, "INFY.NS", "Infosys Limited", "NSE", "92.5", "100", "89.5", "92.0", "SUPPORT RETEST", "https://assets.parqet.com/logos/symbol/INFY.NS", "IN", true),
                TopWeeklySetupItem(5, "BHARTIARTL.NS", "Bharti Airtel Ltd", "NSE", "92.1", "100", "96.0", "97.0", "BREAKOUT", "https://assets.parqet.com/logos/symbol/BHARTIARTL.NS", "BA", true),
                TopWeeklySetupItem(6, "ITC.NS", "ITC Limited", "NSE", "91.8", "95", "92.0", "94.0", "MULTI YEAR BASE", "https://assets.parqet.com/logos/symbol/ITC.NS", "IT", true),
                TopWeeklySetupItem(7, "LT.NS", "Larsen & Toubro Ltd", "NSE", "91.4", "92", "93.5", "90.0", "FLAT BASE", "https://assets.parqet.com/logos/symbol/LT.NS", "LT", true),
                TopWeeklySetupItem(8, "ICICIBANK.NS", "ICICI Bank Ltd", "NSE", "91.0", "94", "95.1", "91.0", "VCP", "https://assets.parqet.com/logos/symbol/ICICIBANK.NS", "IC", true),
                TopWeeklySetupItem(9, "SUNPHARMA.NS", "Sun Pharmaceutical", "NSE", "90.7", "98", "92.8", "88.0", "MULTI YEAR BASE", "https://assets.parqet.com/logos/symbol/SUNPHARMA.NS", "SP", true),
                TopWeeklySetupItem(10, "TITAN.NS", "Titan Company Ltd", "NSE", "90.2", "89", "90.5", "87.0", "SUPPORT RETEST", "https://assets.parqet.com/logos/symbol/TITAN.NS", "TI", true)
            )
        } else {
            listOf(
                TopWeeklySetupItem(1, "ACN", "Accenture plc", "NYSE", "94.2", "85", "100", "100", "BREAKOUT", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=ACN", "AC", false),
                TopWeeklySetupItem(2, "JNJ", "Johnson & Johnson", "NYSE", "93.2", "100", "88.2", "100", "MULTI YEAR BASE", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=JNJ", "JJ", false),
                TopWeeklySetupItem(3, "PANW", "Palo Alto Networks Inc.", "NASDAQ", "92.9", "100", "92.7", "98.6", "VCP", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=PANW", "PA", false),
                TopWeeklySetupItem(4, "XOM", "Exxon Mobil Corporation", "NYSE", "92.9", "100", "89.1", "100", "SUPPORT RETEST", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=XOM", "XO", false),
                TopWeeklySetupItem(5, "ABBV", "AbbVie Inc.", "NYSE", "92.7", "100", "91.8", "96.1", "MULTI YEAR BASE", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=ABBV", "AB", false),
                TopWeeklySetupItem(6, "MSFT", "Microsoft Corporation", "NASDAQ", "92.2", "100", "95.5", "95.8", "SUPPORT RETEST", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=MSFT", "MS", false),
                TopWeeklySetupItem(7, "NVDA", "NVIDIA Corporation", "NASDAQ", "92.0", "98", "99.2", "94.5", "BREAKOUT", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=NVDA", "NV", false),
                TopWeeklySetupItem(8, "AMZN", "Amazon.com Inc.", "NASDAQ", "91.6", "95", "93.1", "92.0", "FLAT BASE", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=AMZN", "AM", false),
                TopWeeklySetupItem(9, "LLY", "Eli Lilly and Company", "NYSE", "91.2", "100", "94.0", "91.5", "MULTI YEAR BASE", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=LLY", "LL", false),
                TopWeeklySetupItem(10, "META", "Meta Platforms Inc.", "NASDAQ", "90.8", "96", "92.4", "90.0", "VCP", "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=META", "ME", false)
            )
        }
    }

    val handleStockSelect = { symbol: String ->
        StockticsDatabase.addToRecent(
            context,
            StockSearchItem(
                symbol = symbol,
                name = symbol,
                exchange = if (symbol.endsWith(".NS")) "NSE" else "US",
                currency = if (symbol.endsWith(".NS")) "₹" else "$",
                price = "--",
                change = "+0.00%",
                isPositive = true,
                logoUrl = ""
            )
        )
        onStockSelected(symbol)
    }

    // Determine if user has scrolled down past the hero module
    val isScrolledDown by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1 || (listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset > 40)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (onBack != null) {
                    Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                } else Modifier
            )
            .background(FintechBackground)
    ) {
        StockticsPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    try {
                        val live = YahooFinanceClient.fetchLiveMarketIndices(isIndian)
                        if (live.isNotEmpty()) marketIndices = live
                    } catch (_: Exception) {}
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 0.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // =========================================================================
                // 1. PAGE TOP HEADING: "Explore" + Subtitle (Pull down to refresh)
                // =========================================================================
                item(key = "explore_page_header") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (onBack != null) {
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
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Explore",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StockticsBadge(
                                            text = if (isIndian) "🇮🇳 INDIA" else "🇺🇸 US",
                                            variant = BadgeVariant.INFO
                                        )
                                    }
                                    Text(
                                        text = if (isIndian) "Indian equities, quant radar & live setups" else "US equities, indices & live analysis",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

            // =========================================================================
            // 2. LUXURY 3D NEOCLASSICAL EXCHANGE HERO BANNER WITH SCROLLING BILLBOARD
            // =========================================================================
            item(key = "explore_hero_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(600f / 530f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(18.dp))
                    ) {
                        val isDark = IsDarkTheme
                        val heroBannerRes = if (isDark) {
                            if (isIndian) com.stocktics.R.drawable.hero_bse_dalal_street else com.stocktics.R.drawable.hero_nyse_wall_street
                        } else {
                            if (isIndian) com.stocktics.R.drawable.hero_bse_dalal_street_light else com.stocktics.R.drawable.hero_nyse_wall_street_light
                        }
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = heroBannerRes),
                            contentDescription = "Exchange Facade",
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Live Scrolling Ticker Billboard inside the neoclassical building billboard
                        // Screen bounds: start = 44/600, width = 512/600, top = 432/530, height = 58/530
                        val bbStart = maxWidth * (44f / 600f)
                        val bbWidth = maxWidth * (512f / 600f)
                        val bbTop = maxHeight * (432f / 530f)
                        val bbHeight = maxHeight * (58f / 530f)
                        Box(
                            modifier = Modifier
                                .padding(start = bbStart, top = bbTop)
                                .size(bbWidth, bbHeight)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF070A0F) else Color(0xF2FFFFFF))
                                .border(1.dp, if (isDark) Color(0xFF222A3B) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            LazyRow(
                                state = tickerScrollState,
                                userScrollEnabled = false,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp)
                            ) {
                                items(tickerIndices) { index ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = if (index.isPositive) "▲" else "▼",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index.isPositive) GainGreen else LossRed
                                        )
                                        Text(
                                            text = index.name.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isDark) Color.White else TextPrimary
                                        )
                                        Text(
                                            text = index.price,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isDark) Color(0xFFCBD5E1) else TextSecondary
                                        )
                                        Text(
                                            text = index.change,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (index.isPositive) GainGreen else LossRed
                                        )
                                        Text(
                                            text = "•",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. TOP SETUPS THIS WEEK (NETFLIX-STYLE CAROUSEL WITH WATERMARK RANK)
            // =========================================================================
            item(key = "explore_top_setups_week") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = "Top Setups",
                                tint = FintechPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Top Setups This Week",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            StockticsBadge(
                                text = "Top 10 Composite Rank",
                                variant = BadgeVariant.GAIN
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(topSetupsList, key = { "top_week_${it.symbol}" }) { stock ->
                            NetflixTopSetupCard(
                                stock = stock,
                                onClick = { handleStockSelect(stock.symbol) }
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 3B. SECTOR INTELLIGENCE EXPLORER ENTRY CARD
            // =========================================================================
            item(key = "explore_sector_intelligence_entry") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(14.dp))
                        .clickable { onOpenSectors() }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FintechPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PieChart,
                                    contentDescription = "Sectors",
                                    tint = FintechPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Explore by GICS Sectors",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StockticsBadge(
                                        text = "11 SECTORS",
                                        variant = BadgeVariant.GAIN
                                    )
                                }
                                Text(
                                    text = "Analyze top gainers, losers & capital rotation across industries",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Open Sectors",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // =========================================================================
            // 4. THE 6 CATEGORIZED ACTIVE PATTERN SETUPS
            // (Near 52W Low, High, Support Retest, Breakout Alert, Zero Debt, Growth)
            // =========================================================================
            val categories = listOf(
                StockCategory.FIFTY_TWO_WEEK_LOW,
                StockCategory.FIFTY_TWO_WEEK_HIGH,
                StockCategory.NEAR_SUPPORT,
                StockCategory.NEAR_RESISTANCE,
                StockCategory.DEBT_FREE,
                StockCategory.GOOD_GROWTH
            )

            categories.forEach { cat ->
                val stocks = exploreCategoryStocksMap[cat] ?: StockCategoryRepository.getTopStocks(cat, limit = 5, isIndian = isIndian)
                val totalCount = StockCategoryRepository.getCategoryTotalCount(cat, isIndian = isIndian)
                val theoryMeta = getPatternTheoryForCategory(cat)

                item(key = "explore_pattern_category_${cat.name}") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Title Zone (Constrained to never push View All)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = cat.subtitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Text(
                                    text = "$totalCount setups active",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextTertiary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            if (onViewAllCategory != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onViewAllCategory(
                                                cat,
                                                if (isIndian) CategoryMarketFilter.INDIAN else CategoryMarketFilter.US
                                            )
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "View All ($totalCount)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FintechPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "View All",
                                        tint = FintechPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // CARD #1: Educational Pattern Theory / Institutional Rule Card
                            item(key = "theory_card_${cat.name}") {
                                PatternTheoryCard(
                                    meta = theoryMeta,
                                    onClick = {
                                        onViewAllCategory?.invoke(
                                            cat,
                                            if (isIndian) CategoryMarketFilter.INDIAN else CategoryMarketFilter.US
                                        )
                                    }
                                )
                            }

                            // CARDS #2 to #6: Top 5 Active Stock Cards
                            itemsIndexed(stocks, key = { _, s -> "setup_${cat.name}_${s.symbol}" }) { idx, stock ->
                                val score = when (idx) {
                                    0 -> 95
                                    1 -> 92
                                    2 -> 88
                                    3 -> 84
                                    else -> 78
                                }
                                ActiveSetupStockCard(
                                    stock = stock,
                                    category = cat,
                                    score = score,
                                    onClick = { handleStockSelect(stock.symbol) }
                                )
                            }

                            // CARD #7: Trailing View All Card
                            item(key = "view_all_card_${cat.name}") {
                                ViewAllSetupsCard(
                                    categoryTitle = cat.title,
                                    totalCount = totalCount,
                                    onClick = {
                                        onViewAllCategory?.invoke(
                                            cat,
                                            if (isIndian) CategoryMarketFilter.INDIAN else CategoryMarketFilter.US
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // STICKY HEADER WITH PINNED SCROLLING TICKER WHEN SCROLLED DOWN (ZERO GAP)
        // =========================================================================
        AnimatedVisibility(
            visible = isScrolledDown,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = FintechBackground.copy(alpha = 0.98f),
                border = BorderStroke(1.dp, FintechBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    // Compact Header Bar (Flush directly under top bar with NO statusBarsPadding)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(FintechSurfaceElevated)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Back",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Explore Setups",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            StockticsBadge(
                                text = if (isIndian) "🇮🇳 INDIA" else "🇺🇸 US",
                                variant = BadgeVariant.INFO
                            )
                        }

                        // Compact Refresh Button
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isRefreshing = true
                                    try {
                                        val live = YahooFinanceClient.fetchLiveMarketIndices(isIndian)
                                        if (live.isNotEmpty()) marketIndices = live
                                    } catch (_: Exception) {}
                                    isRefreshing = false
                                }
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(FintechSurfaceElevated)
                                .border(1.dp, FintechBorder, CircleShape)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 2.dp,
                                    color = FintechPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Sync",
                                    tint = FintechPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Pinned scrolling ticker stuck directly into the header!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070A0F))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        LazyRow(
                            state = tickerScrollState,
                            userScrollEnabled = false,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(tickerIndices) { index ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = if (index.isPositive) "▲" else "▼",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index.isPositive) GainGreen else LossRed
                                    )
                                    Text(
                                        text = index.name.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFF1F5F9)
                                    )
                                    Text(
                                        text = index.price,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = index.change,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (index.isPositive) GainGreen else LossRed
                                    )
                                    Text(
                                        text = "•",
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569)
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
