package com.stocktics.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stocktics.ui.theme.LocalFintechColors

/**
 * Institutional Shimmer Loading Skeleton Placeholder Modifier.
 * Displays a fluid diagonal metallic shimmer wave over dark slate containers.
 */
@Composable
fun Modifier.fintechShimmer(
    cornerRadius: Dp = 8.dp
): Modifier {
    val fintech = LocalFintechColors.current
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = if (fintech.isDark) {
        listOf(
            Color(0xFF141923),
            Color(0xFF1F2738),
            Color(0xFF141923)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )
    }

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim.value - 400f, y = translateAnim.value - 400f),
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(brush)
}
