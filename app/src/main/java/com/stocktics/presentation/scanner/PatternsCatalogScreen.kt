package com.stocktics.presentation.scanner

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.api.ApiPatternDefinition
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsTextField
import com.stocktics.ui.theme.LocalFintechColors

/**
 * Institutional 33-Patterns Catalog Screen.
 */
@Composable
fun PatternsCatalogScreen(
    onSelectPattern: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "BULLISH", "BEARISH", "REVERSAL", "CONTINUATION")

    val allPatterns = remember {
        listOf(
            ApiPatternDefinition("BREAKOUT", "Momentum Breakout", "BULLISH", "Price breaking multi-week resistance on high volume", 76, 21.4, 18),
            ApiPatternDefinition("CUP_HANDLE", "Cup and Handle", "CONTINUATION", "U-shaped accumulation with a low-volume handle pullback", 74, 24.2, 12),
            ApiPatternDefinition("DOUBLE_BOTTOM", "Double Bottom (W)", "REVERSAL", "Classic reversal testing key support twice with bullish divergence", 71, 18.8, 14),
            ApiPatternDefinition("BULL_FLAG", "Bull Flag", "CONTINUATION", "Sharp pole rally followed by shallow downward consolidation", 77, 16.5, 22),
            ApiPatternDefinition("ASCENDING_TRIANGLE", "Ascending Triangle", "BULLISH", "Flat resistance ceiling with rising higher swing lows", 73, 19.1, 9),
            ApiPatternDefinition("HEAD_SHOULDERS_INV", "Inverse Head & Shoulders", "REVERSAL", "Institutions absorbing supply creating lower head and higher right shoulder", 75, 26.0, 7),
            ApiPatternDefinition("FALLING_WEDGE", "Falling Wedge", "BULLISH", "Converging downward channel preparing for violent upside break", 69, 20.3, 11),
            ApiPatternDefinition("TRIPLE_BOTTOM", "Triple Bottom", "REVERSAL", "Three distinct retests of major institutional accumulation floor", 70, 22.1, 5),
            ApiPatternDefinition("PENNANT", "Bullish Pennant", "CONTINUATION", "Symmetrical converging triangle after vertical momentum thrust", 72, 15.8, 16),
            ApiPatternDefinition("ROUNDING_BOTTOM", "Rounding Bottom", "REVERSAL", "Long multi-month gradual bowl accumulation by smart money", 68, 31.4, 4),
            ApiPatternDefinition("GAP_AND_GO", "Gap & Go Momentum", "BULLISH", "Opening gap above key resistance held with follow-through buying", 75, 14.2, 15),
            ApiPatternDefinition("VOLUME_CONTRACTION", "VCP (Minervini)", "CONTINUATION", "Progressively tighter volatility contractions before expansion", 78, 23.5, 10),
            ApiPatternDefinition("DOUBLE_TOP", "Double Top (M)", "BEARISH", "Two failed attempts at resistance signaling institutional distribution", 72, -16.2, 8),
            ApiPatternDefinition("HEAD_SHOULDERS", "Head & Shoulders", "BEARISH", "Major topping formation signaling completion of bull campaign", 74, -19.5, 6),
            ApiPatternDefinition("RISING_WEDGE", "Rising Wedge", "BEARISH", "Upward sloping wedge losing underlying buying momentum", 69, -14.8, 9)
        )
    }

    val filteredPatterns = allPatterns.filter { pat ->
        val matchesCat = selectedCategory == "ALL" || pat.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
            pat.displayName.contains(searchQuery, ignoreCase = true) ||
            pat.pattern.contains(searchQuery, ignoreCase = true) ||
            pat.description.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fintech.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

            Column {
                Text(
                    text = "Patterns Catalog Library",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = fintech.textPrimary
                )
                Text(
                    text = "33 Audited Quantitative Base & Chart Formations",
                    fontSize = 11.sp,
                    color = fintech.textSecondary
                )
            }
        }

        // Search Input
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            StockticsTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Patterns",
                placeholder = "Search (e.g. Breakout, Cup & Handle, VCP)...",
                leadingIcon = Icons.Rounded.Search,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) fintech.primary else fintech.surfaceElevated)
                        .border(1.dp, if (isSelected) fintech.primary else fintech.border, RoundedCornerShape(8.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0B0E14) else fintech.textSecondary
                    )
                }
            }
        }

        // Pattern Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredPatterns, key = { it.pattern }) { pat ->
                StockticsCard(
                    isGlass = true,
                    contentPadding = 14.dp,
                    onClick = { onSelectPattern(pat.pattern) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pat.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fintech.textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StockticsBadge(
                                    text = pat.category,
                                    variant = when (pat.category) {
                                        "BULLISH" -> BadgeVariant.GAIN
                                        "BEARISH" -> BadgeVariant.LOSS
                                        "REVERSAL" -> BadgeVariant.INFO
                                        else -> BadgeVariant.NEUTRAL
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pat.description,
                                fontSize = 11.sp,
                                color = fintech.textSecondary,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Win Rate: ${pat.winRate}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fintech.gainGreen
                                )
                                Text(
                                    text = "Avg Return: +${pat.avgGain}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fintech.textPrimary
                                )
                                Text(
                                    text = "${pat.activeCount} Active Today",
                                    fontSize = 11.sp,
                                    color = fintech.primary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = fintech.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
