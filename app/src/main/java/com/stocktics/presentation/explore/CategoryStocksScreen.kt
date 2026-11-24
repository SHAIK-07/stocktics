package com.stocktics.presentation.explore

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import com.stocktics.ui.components.StockticsPullToRefreshBox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.data.stocks.CategoryStockItem
import com.stocktics.data.stocks.StockCategory
import com.stocktics.data.stocks.StockCategoryRepository
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import kotlinx.coroutines.launch

enum class CategoryMarketFilter(val label: String) {
    ALL("All Markets"),
    INDIAN("🇮🇳 Indian"),
    US("🇺🇸 US")
}

@Composable
fun CategoryStocksScreen(
    category: StockCategory,
    onBack: () -> Unit,
    onStockSelected: (String) -> Unit,
    currentMarket: GlobalMarket = GlobalMarket.INDIA,
    initialMarketFilter: CategoryMarketFilter = CategoryMarketFilter.ALL,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    val isIndianFilter: Boolean? = currentMarket == GlobalMarket.INDIA

    var stocks by remember(category, currentMarket, searchQuery) {
        mutableStateOf(
            StockCategoryRepository.getAllStocks(
                category = category,
                isIndian = isIndianFilter,
                query = searchQuery
            )
        )
    }

    // Enrich with live quotes on mount
    LaunchedEffect(category, currentMarket) {
        val initial = StockCategoryRepository.getAllStocks(
            category = category,
            isIndian = isIndianFilter,
            query = searchQuery
        )
        stocks = initial
        try {
            val enriched = StockCategoryRepository.enrichWithLiveQuotes(initial)
            stocks = enriched
        } catch (_: Exception) {}
    }

    StockticsPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                try {
                    val refreshed = StockCategoryRepository.enrichWithLiveQuotes(stocks)
                    stocks = refreshed
                } catch (_: Exception) {}
                isRefreshing = false
            }
        },
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FintechBackground),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. App Bar Header
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

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.subtitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StockticsBadge(
                                    text = category.badgeText,
                                    variant = BadgeVariant.INFO
                                )
                            }
                            Text(
                                text = "${stocks.size} Equities identified • Live screening (Pull down to refresh)",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

        // 2. Institutional Strategy Thesis Card
        item {
            StockticsCard(
                cornerRadius = 14.dp,
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FintechPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = null,
                            tint = FintechPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quantitative Screener Thesis",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = category.explanation,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }



        // 4. In-Category Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search in ${category.title}...",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(FintechPrimary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Stock List
        if (stocks.isEmpty()) {
            item {
                StockticsCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matching stocks found",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try switching the market tab or clearing the search query",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(stocks, key = { it.symbol }) { stock ->
                CategoryStockCard(
                    stock = stock,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onStockSelected(stock.symbol)
                    }
                )
            }
        }
    }
}
}

@Composable
fun CategoryStockCard(
    stock: CategoryStockItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StockticsCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 14.dp,
        contentPadding = 14.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Logo & Company Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    StockCompanyLogo(
                        symbol = stock.symbol,
                        initials = stock.initials,
                        logoUrl = stock.logoUrl,
                        size = 38.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.symbol.replace(".NS", "").replace(".BO", ""),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StockticsBadge(
                                text = stock.exchange,
                                variant = BadgeVariant.INFO
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stock.name,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Price & Change
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stock.price,
                        fontSize = 15.sp,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Ribbon: Category Specific Metric Highlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${stock.metricLabel}: ",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stock.metricValue,
                        fontSize = 11.sp,
                        color = if (stock.category == StockCategory.NEAR_RESISTANCE) LossRed else GainGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stock.metricHighlight,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Modern compact horizontal card designed for the 5-item horizontal swipe strip
 * on Home and Explore screens.
 */
@Composable
fun CategoryStockCardHorizontal(
    stock: CategoryStockItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StockticsCard(
        modifier = modifier
            .width(180.dp)
            .clickable { onClick() },
        cornerRadius = 14.dp,
        contentPadding = 12.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockCompanyLogo(
                    symbol = stock.symbol,
                    initials = stock.initials,
                    logoUrl = stock.logoUrl,
                    size = 32.dp
                )

                StockticsBadge(
                    text = stock.exchange,
                    variant = BadgeVariant.INFO
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stock.symbol.replace(".NS", "").replace(".BO", ""),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )

            Text(
                text = stock.name,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.price,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                StockticsBadge(
                    text = stock.change,
                    variant = if (stock.isPositive) BadgeVariant.GAIN else BadgeVariant.LOSS,
                    showTrendIcon = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom metric badge ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.metricValue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stock.category == StockCategory.NEAR_RESISTANCE) LossRed else GainGreen
                )

                Text(
                    text = stock.metricHighlight.replace("Support: ", "S: ").replace("Resistance: ", "R: "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
