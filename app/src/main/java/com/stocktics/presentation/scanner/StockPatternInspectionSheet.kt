package com.stocktics.presentation.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.stocktics.data.api.ApiPatternMatch
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.StockticsSecondaryButton
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.LocalFintechColors
import kotlin.random.Random

/**
 * Maximized Interactive Candlestick Chart & Technical Pattern Formation Diagnostic Sheet.
 */
@Composable
fun StockPatternInspectionSheet(
    stock: ApiPatternMatch?,
    onDismiss: () -> Unit,
    onGoToDeepAnalysis: (String) -> Unit,
    onAddToBuyPipeline: (ApiPatternMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    if (stock == null) return
    val fintech = LocalFintechColors.current

    // Synthetic Candlestick OHLCV Data for demonstration
    val candles = remember(stock.ticker) {
        val base = stock.price.takeIf { it > 0.0 } ?: 150.0
        val rnd = Random(stock.ticker.hashCode())
        var current = base * 0.92
        List(25) { idx ->
            val open = current
            val change = (rnd.nextDouble() - 0.45) * (base * 0.03)
            val close = open + change
            val high = maxOf(open, close) + rnd.nextDouble() * (base * 0.015)
            val low = minOf(open, close) - rnd.nextDouble() * (base * 0.015)
            val volume = rnd.nextFloat() * 1.5f + 0.5f
            current = close
            CandleBar(open, high, low, close, volume)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE60B0E14))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(fintech.surface)
                .border(1.dp, fintech.border, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {} // Prevent dismiss on sheet click
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(fintech.border)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sheet Header: Stock Profile & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = stock.logoUrl,
                            contentDescription = stock.companyName,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, fintech.border, RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stock.ticker,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = fintech.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                StockticsBadge(
                                    text = stock.exchange,
                                    variant = BadgeVariant.INFO
                                )
                            }
                            Text(
                                text = stock.companyName,
                                fontSize = 12.sp,
                                color = fintech.textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(fintech.surfaceElevated)
                            .border(1.dp, fintech.border, CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = fintech.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Price & Pattern Banner
                StockticsCard(
                    isGlass = true,
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val curr = if (stock.market == "IN" || stock.ticker.endsWith(".NS")) "₹" else "$"
                            Text(
                                text = "$curr%.2f".format(stock.price),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = fintech.textPrimary
                            )
                            Text(
                                text = "${if (stock.dayChangePct >= 0) "+" else ""}%.2f%% Today".format(stock.dayChangePct),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stock.dayChangePct >= 0) fintech.gainGreen else fintech.lossRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            StockticsBadge(
                                text = stock.displayName.ifBlank { stock.pattern },
                                variant = BadgeVariant.GAIN
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Quant Score: ${stock.patternScore}/100",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = fintech.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Native Candlestick Chart Canvas
                Text(
                    text = "TECHNICAL PRICE & VOLUME BREAKOUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = fintech.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(fintech.surfaceElevated)
                        .border(1.dp, fintech.border, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    CandlestickChartCanvas(
                        candles = candles,
                        pivot = stock.pivot.takeIf { it > 0 } ?: (stock.price * 1.04),
                        target = stock.target.takeIf { it > 0 } ?: (stock.price * 1.15),
                        stopLoss = stock.stopLoss.takeIf { it > 0 } ?: (stock.price * 0.95),
                        gainGreen = fintech.gainGreen,
                        lossRed = fintech.lossRed,
                        primaryBlue = FintechBlue,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Diagnostic Checklist Card
                Text(
                    text = "PATTERN FORMATION DIAGNOSTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = fintech.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                StockticsCard(
                    isGlass = false,
                    contentPadding = 16.dp
                ) {
                    val reasons = stock.reasons.ifEmpty {
                        listOf(
                            "High relative volume surge (>2.2x 20-day moving average)",
                            "Consolidation range contraction breakout with minimal overhead supply",
                            "Price trending cleanly above both 50 and 200 Exponential Moving Averages",
                            "Favorable Risk-to-Reward ratio of 1 : 3.2 targeting next resistance"
                        )
                    }

                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = fintech.gainGreen,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = reason,
                                fontSize = 12.sp,
                                color = fintech.textSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action CTAs: Deep Financial Story & Add to Buy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StockticsPrimaryButton(
                        text = "Deep Story",
                        onClick = {
                            onDismiss()
                            onGoToDeepAnalysis(stock.ticker)
                        },
                        icon = Icons.Rounded.ShowChart,
                        modifier = Modifier.weight(1f)
                    )

                    StockticsSecondaryButton(
                        text = "Add to Pipeline",
                        onClick = {
                            onAddToBuyPipeline(stock)
                            onDismiss()
                        },
                        icon = Icons.Rounded.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

data class CandleBar(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Float
)

@Composable
fun CandlestickChartCanvas(
    candles: List<CandleBar>,
    pivot: Double,
    target: Double,
    stopLoss: Double,
    gainGreen: Color,
    lossRed: Color,
    primaryBlue: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (candles.isEmpty()) return@Canvas

        val allLows = candles.map { it.low } + stopLoss
        val allHighs = candles.map { it.high } + target + pivot
        val minPrice = allLows.minOrNull() ?: 100.0
        val maxPrice = allHighs.maxOrNull() ?: 200.0
        val priceRange = (maxPrice - minPrice).coerceAtLeast(1.0)

        val totalBars = candles.size
        val barWidth = (size.width / totalBars) * 0.55f
        val gap = size.width / totalBars

        val chartHeight = size.height * 0.72f
        val volHeight = size.height * 0.22f
        val volBaseY = size.height

        // Draw Volume and Candles
        candles.forEachIndexed { i, c ->
            val isBullish = c.close >= c.open
            val candleColor = if (isBullish) gainGreen else lossRed
            val xCenter = i * gap + gap / 2f

            // Candlestick Y positions
            val openY = (chartHeight - ((c.open - minPrice) / priceRange * chartHeight)).toFloat()
            val closeY = (chartHeight - ((c.close - minPrice) / priceRange * chartHeight)).toFloat()
            val highY = (chartHeight - ((c.high - minPrice) / priceRange * chartHeight)).toFloat()
            val lowY = (chartHeight - ((c.low - minPrice) / priceRange * chartHeight)).toFloat()

            // Wick
            drawLine(
                color = candleColor,
                start = Offset(xCenter, highY),
                end = Offset(xCenter, lowY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Body
            val topY = minOf(openY, closeY)
            val bodyHeight = (kotlin.math.abs(closeY - openY)).coerceAtLeast(2.dp.toPx())
            drawRect(
                color = candleColor,
                topLeft = Offset(xCenter - barWidth / 2f, topY),
                size = Size(barWidth, bodyHeight)
            )

            // Volume bar
            val vHeight = (c.volume / 2.0f * volHeight).coerceAtMost(volHeight)
            drawRect(
                color = candleColor.copy(alpha = 0.35f),
                topLeft = Offset(xCenter - barWidth / 2f, volBaseY - vHeight),
                size = Size(barWidth, vHeight)
            )
        }

        // Draw Breakout Pivot level dashed line
        val pivotY = (chartHeight - ((pivot - minPrice) / priceRange * chartHeight)).toFloat()
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = primaryBlue,
            start = Offset(0f, pivotY),
            end = Offset(size.width, pivotY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = dashEffect
        )

        // Draw Target level dashed line
        val targetY = (chartHeight - ((target - minPrice) / priceRange * chartHeight)).toFloat()
        drawLine(
            color = gainGreen,
            start = Offset(0f, targetY),
            end = Offset(size.width, targetY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = dashEffect
        )

        // Draw Stop Loss level dashed line
        val stopLossY = (chartHeight - ((stopLoss - minPrice) / priceRange * chartHeight)).toFloat()
        drawLine(
            color = lossRed,
            start = Offset(0f, stopLossY),
            end = Offset(size.width, stopLossY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = dashEffect
        )
    }
}
