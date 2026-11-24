package com.stocktics.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@Composable
fun StockticsQuantLoadingView(
    symbol: String,
    modifier: Modifier = Modifier
) {
    val cleanSym = symbol.replace(".NS", "").replace(".BO", "")

    // Rotating Quant Stages
    val stages = listOf(
        "Connecting to Live Market Feeds...",
        "Computing Quant Valuation & P/E Metrics...",
        "Synthesizing Technical Momentum Matrix...",
        "Aggregating Smart Money & Institutional Activity...",
        "Compiling Tier-1 Institutional Scorecard..."
    )
    var currentStageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1600L)
            currentStageIndex = (currentStageIndex + 1) % stages.size
        }
    }

    // Infinite transitions for radar pulse and candlestick animation
    val infiniteTransition = rememberInfiniteTransition(label = "quantLoading")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    // Animated Candlestick Equalizer Heights
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 26f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val barHeight3 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val barHeight4 by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    // Sweeping shimmer progress bar
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // GPU Canvas-rendered animated candlestick equalizer and radar pulse
    // Eliminates per-frame CPU layout passes for silky-smooth 60/120fps on mobile
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            // Glowing Radar Beacon (GPU-accelerated graphicsLayer)
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Radar Ring 2 (Hardware layer)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = pulseScale2
                            scaleY = pulseScale2
                            alpha = pulseAlpha2
                        }
                        .clip(CircleShape)
                        .background(FintechBlue.copy(alpha = 0.15f))
                        .border(1.dp, FintechBlue, CircleShape)
                )

                // Expanding Radar Ring 1 (Hardware layer)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = pulseScale1
                            scaleY = pulseScale1
                            alpha = pulseAlpha1
                        }
                        .clip(CircleShape)
                        .background(FintechPrimary.copy(alpha = 0.15f))
                        .border(1.dp, FintechPrimary, CircleShape)
                )

                // Central Institutional Card
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(FintechSurface)
                        .border(1.5.dp, FintechPrimary, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoGraph,
                        contentDescription = null,
                        tint = FintechPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Candlestick Equalizer drawn on high-performance Canvas
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .width(42.dp)
                    .height(34.dp)
            ) {
                val barW = 5.dp.toPx()
                val gap = 6.dp.toPx()
                val cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5.dp.toPx())
                val centerY = size.height / 2f

                val bars = listOf(
                    Triple(barHeight1.dp.toPx(), Color(0xFF00D09C), 0),
                    Triple(barHeight2.dp.toPx(), Color(0xFF00B386), 1),
                    Triple(barHeight3.dp.toPx(), Color(0xFF1455FE), 2),
                    Triple(barHeight4.dp.toPx(), Color(0xFF00D09C), 3)
                )

                bars.forEach { (h, color, index) ->
                    val left = index * (barW + gap)
                    val top = centerY - (h / 2f)
                    drawRoundRect(
                        color = color,
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(barW, h),
                        cornerRadius = cornerRadius
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Target Stock Symbol Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechPrimary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "QUANT ENGINE • $cleanSym",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rotating Stage Text with smooth fade
            Box(
                modifier = Modifier.height(26.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = stages[currentStageIndex],
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "stageTransition"
                ) { stageText ->
                    Text(
                        text = stageText,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sweeping Shimmer Progress Bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(FintechBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(shimmerOffset)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(FintechBlue, FintechPrimary, GainGreen)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Live Market Ingestion Active",
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}
