package com.stocktics.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.stocks.StockSearchItem
import com.stocktics.data.stocks.SupabaseStockClient
import com.stocktics.data.stocks.YahooFinanceClient
import com.stocktics.ui.components.GlobalMarket
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.fintechPressBounce
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.LocalFintechColors
import com.stocktics.ui.theme.LossRed
import kotlinx.coroutines.delay

/**
 * Modern Bottom Search Overlay modeled after INDmoney's signature search experience.
 * Features:
 * - Floating results sheet right above the keyboard
 * - Bottom search capsule [Q Search stocks, ETFs, themes]
 * - Circular [X] close button
 * - Instant live search across US & Indian equities
 */
@Composable
fun BottomSearchOverlay(
    isVisible: Boolean,
    currentMarket: GlobalMarket,
    onDismiss: () -> Unit,
    onStockSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fintech = LocalFintechColors.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<StockSearchItem>>(emptyList()) }
    var defaultStocks by remember { mutableStateOf<List<StockSearchItem>>(emptyList()) }

    // Load trending/popular stocks when opened
    LaunchedEffect(isVisible) {
        if (isVisible) {
            searchQuery = ""
            try {
                val realStocks = SupabaseStockClient.getRealStocks()
                if (realStocks.isNotEmpty()) {
                    defaultStocks = realStocks.take(8).map { s ->
                        val isIndian = s.exchange.equals("NSE", ignoreCase = true) ||
                                s.exchange.equals("BSE", ignoreCase = true) ||
                                s.symbol.endsWith(".NS") || s.symbol.endsWith(".BO")
                        StockSearchItem(
                            symbol = s.symbol,
                            name = s.name,
                            exchange = s.exchange,
                            currency = if (isIndian) "₹" else "$",
                            price = s.price,
                            change = s.change,
                            isPositive = s.isPositive,
                            logoUrl = s.logoUrl
                        )
                    }
                }
            } catch (_: Exception) {}

            delay(150)
            focusRequester.requestFocus()
        }
    }

    // Live instant search
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            val local = StockticsDatabase.searchLocalStocks(context, q, isIndian = null)
            if (local.isNotEmpty()) {
                searchResults = local
            }
            try {
                val online = YahooFinanceClient.searchStocksOnline(q, isIndian = null)
                if (online.isNotEmpty()) {
                    searchResults = online
                }
            } catch (_: Exception) {}
        } else {
            searchResults = emptyList()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        keyboardController?.hide()
                        onDismiss()
                    }
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Intercept clicks inside card
                        ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val displayedStocks = if (searchQuery.isNotEmpty()) searchResults else defaultStocks

                    // 1. Floating Results Sheet above Search Bar
                    if (displayedStocks.isNotEmpty() || searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    spotColor = Color.Black.copy(alpha = 0.35f)
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(fintech.surface)
                                .border(1.dp, fintech.border, RoundedCornerShape(24.dp))
                                .padding(vertical = 8.dp)
                        ) {
                            if (displayedStocks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No stocks matching \"$searchQuery\"",
                                        fontSize = 13.sp,
                                        color = fintech.textTertiary
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(displayedStocks, key = { it.symbol }) { stock ->
                                        SearchStockResultRow(
                                            stock = stock,
                                            onClick = {
                                                keyboardController?.hide()
                                                onStockSelected(stock.symbol)
                                                onDismiss()
                                            }
                                        )
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = fintech.border.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(horizontal = 18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Bottom Search Bar + Close Circle Button (Screenshot 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Capsule Search Input Field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .shadow(
                                    elevation = 14.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    spotColor = Color.Black.copy(alpha = 0.25f)
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(fintech.surface)
                                .border(1.dp, fintech.border, RoundedCornerShape(28.dp))
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    tint = fintech.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search stocks, ETFs, themes",
                                            color = fintech.textTertiary,
                                            fontSize = 14.sp
                                        )
                                    }

                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        textStyle = TextStyle(
                                            color = fintech.textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(fintech.primary),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Search
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                keyboardController?.hide()
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                    )
                                }

                                if (searchQuery.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(fintech.surfaceElevated)
                                            .clickable { searchQuery = "" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear",
                                            tint = fintech.textSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Close Circle Button (Screenshot 5)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(
                                    elevation = 14.dp,
                                    shape = CircleShape,
                                    spotColor = Color.Black.copy(alpha = 0.25f)
                                )
                                .clip(CircleShape)
                                .background(fintech.surface)
                                .border(1.dp, fintech.border, CircleShape)
                                .clickable {
                                    keyboardController?.hide()
                                    onDismiss()
                                }
                                .fintechPressBounce(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Search",
                                tint = fintech.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Stock Result Row matching Screenshot 5
 */
@Composable
private fun SearchStockResultRow(
    stock: StockSearchItem,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .fintechPressBounce()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Company Logo
            StockCompanyLogo(
                symbol = stock.symbol,
                initials = stock.symbol.take(2),
                logoUrl = stock.logoUrl,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = stock.symbol.substringBefore("."),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fintech.textTertiary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stock.name.ifBlank { stock.symbol },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = fintech.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right side: Price & Day Change %
        Column(
            horizontalAlignment = Alignment.End
        ) {
            val priceStr = if (stock.price.isNotBlank() && stock.price != "--") {
                val num = stock.price.replace(",", "").replace("$", "").replace("₹", "").trim().toDoubleOrNull()
                if (num != null) {
                    val curr = if (stock.currency.isNotBlank()) stock.currency else if (stock.symbol.endsWith(".NS") || stock.symbol.endsWith(".BO")) "₹" else "$"
                    "$curr${String.format(java.util.Locale.US, "%,.2f", num)}"
                } else {
                    stock.price
                }
            } else {
                "--"
            }

            Text(
                text = priceStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = fintech.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            val isPos = stock.isPositive
            val arrow = if (isPos) "↑" else "↓"
            val changeStr = if (stock.change.isNotBlank() && stock.change != "--") {
                "$arrow ${stock.change.replace("+", "").replace("-", "")}"
            } else {
                "--"
            }

            Text(
                text = changeStr,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPos) GainGreen else LossRed
            )
        }
    }
}
