package com.stocktics.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Locale

// ==============================================================================
// 1. MODERN PRICE RANGE SLIDER (Day Range & 52-Week Range)
// ==============================================================================
@Composable
fun StockRangeSlider(
    label: String,
    lowPrice: Double,
    highPrice: Double,
    currentPrice: Double,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val progress = if (highPrice > lowPrice) {
        ((currentPrice - lowPrice) / (highPrice - lowPrice)).toFloat().coerceIn(0f, 1f)
    } else {
        0.5f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "rangeProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FintechSurfaceElevated.copy(alpha = 0.5f))
            .border(1.dp, FintechBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Top Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "$label Low",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.US, "%s%.2f", currencySymbol, lowPrice),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }

            // Current Position Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechSurface)
                    .border(1.dp, FintechBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "Cur: %s%.2f", currencySymbol, currentPrice),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (animatedProgress > 0.5f) GainGreen else WarningAmber
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$label High",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.US, "%s%.2f", currencySymbol, highPrice),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Slider Track Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackHeight = 6.dp.toPx()
                val centerY = size.height / 2f
                val trackWidth = size.width

                // 1. Background Track
                drawRoundRect(
                    color = Color(0xFF1E2638),
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(trackWidth, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )

                // 2. Gradient Active Track
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(LossRed, WarningAmber, GainGreen)
                    ),
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(trackWidth * animatedProgress, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )

                // 3. Thumb Marker Indicator
                val thumbX = trackWidth * animatedProgress
                val thumbRadius = 6.dp.toPx()
                val haloRadius = 9.dp.toPx()

                // Glow ring
                drawCircle(
                    color = Color(0x3300D09C),
                    radius = haloRadius,
                    center = Offset(thumbX, centerY)
                )
                // White outer stroke
                drawCircle(
                    color = Color.White,
                    radius = thumbRadius,
                    center = Offset(thumbX, centerY)
                )
                // Center colored core
                drawCircle(
                    color = if (animatedProgress > 0.5f) GainGreen else LossRed,
                    radius = thumbRadius - 2.dp.toPx(),
                    center = Offset(thumbX, centerY)
                )
            }
        }
    }
}

// ==============================================================================
// 2. MODERN SPEEDOMETER CONSENSUS GAUGE & TARGET PRICE
// ==============================================================================
@Composable
fun StockSpeedometerGauge(
    consensusRating: String, // e.g. "BUY", "STRONG BUY", "HOLD", "SELL"
    buyPercent: Float = 88f,
    holdPercent: Float = 12f,
    sellPercent: Float = 0f,
    currentPrice: Double,
    targetPrice: Double,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val total = (buyPercent + holdPercent + sellPercent).coerceAtLeast(1f)
    val normBuy = (buyPercent / total)
    val normHold = (holdPercent / total)
    val normSell = (sellPercent / total)

    val upsidePercent = if (currentPrice > 0) {
        ((targetPrice - currentPrice) / currentPrice) * 100.0
    } else {
        0.0
    }

    val ratingColor = when {
        consensusRating.contains("BUY", ignoreCase = true) -> GainGreen
        consensusRating.contains("SELL", ignoreCase = true) -> LossRed
        else -> WarningAmber
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Editorial Headline
        Text(
            text = "ANALYST CONSENSUS",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Analysts project ",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (upsidePercent >= 0) GainGreenBg else LossRedBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%+.1f%% %s", upsidePercent, if (upsidePercent >= 0) "upside" else "downside"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (upsidePercent >= 0) GainGreen else LossRed
                )
            }
            Text(
                text = " 1-yr target",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Semi-Circle Speedometer Arc
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 110.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                // Background 180-deg arc
                drawArc(
                    color = Color(0xFF1E2638),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Segment 1: Buy (Green)
                val buySweep = 180f * normBuy
                if (buySweep > 0) {
                    drawArc(
                        color = GainGreen,
                        startAngle = 180f,
                        sweepAngle = buySweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = if (normHold == 0f && normSell == 0f) StrokeCap.Round else StrokeCap.Butt)
                    )
                }

                // Segment 2: Hold (Amber)
                val holdSweep = 180f * normHold
                if (holdSweep > 0) {
                    drawArc(
                        color = WarningAmber,
                        startAngle = 180f + buySweep,
                        sweepAngle = holdSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                }

                // Segment 3: Sell (Red)
                val sellSweep = 180f * normSell
                if (sellSweep > 0) {
                    drawArc(
                        color = LossRed,
                        startAngle = 180f + buySweep + holdSweep,
                        sweepAngle = sellSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Big Bold Centered Rating inside the arc
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = consensusRating.uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = ratingColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Breakdown Indicators (Buy %, Hold %, Sell %)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GainGreen))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "BUY ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                Text(text = String.format(Locale.US, "%.1f%%", buyPercent), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WarningAmber))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "HOLD ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                Text(text = String.format(Locale.US, "%.1f%%", holdPercent), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LossRed))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "SELL ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                Text(text = String.format(Locale.US, "%.1f%%", sellPercent), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = LossRed)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Current Price vs Target Price Transition Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(FintechBackground)
                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "CURRENT PRICE", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.US, "%s%.2f", currencySymbol, currentPrice),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "1-YR TARGET PRICE", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%s%.2f", currencySymbol, targetPrice),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = GainGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "(%+.1f%%)", upsidePercent),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (upsidePercent >= 0) GainGreen else LossRed
                    )
                }
            }
        }
    }
}

// ==============================================================================
// 3. MODERN HORIZONTAL INSIGHT STORY CARD (For Story Carousels)
// ==============================================================================
@Composable
fun StockInsightStoryCard(
    headline: String,
    title: String,
    subtitle: String,
    accentColor: Color = FintechPrimary,
    icon: ImageVector = Icons.AutoMirrored.Rounded.TrendingUp,
    badgeText: String? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(180.dp)
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FintechSurfaceElevated,
                        FintechSurface
                    )
                )
            )
            .border(1.dp, FintechBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(13.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon circle and Headline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            // Headline
            Text(
                text = headline.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Main Title
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            // Subtitle
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==============================================================================
// 4. PERSISTENT FLOATING BOTTOM ACTION CAPSULE
// ==============================================================================
@Composable
fun StockFloatingBottomAction(
    actionText: String,
    subText: String? = null,
    backgroundColor: Color = FintechPrimary,
    textColor: Color = Color.Black,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(25.dp),
                    ambientColor = backgroundColor.copy(alpha = 0.4f),
                    spotColor = backgroundColor.copy(alpha = 0.6f)
                )
                .clip(RoundedCornerShape(25.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = actionText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = textColor
                )

                if (subText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $subText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
