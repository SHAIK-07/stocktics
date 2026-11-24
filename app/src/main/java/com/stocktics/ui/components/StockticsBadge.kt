package com.stocktics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBlueDim
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.LossRedBg
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.WarningAmber
import com.stocktics.ui.theme.WarningAmberBg

enum class BadgeVariant {
    GAIN,
    LOSS,
    NEUTRAL,
    INFO,
    MUTED
}

/**
 * Groww/INDmoney-style Financial Badge Pill.
 * Refined 12% opacity translucent background with high-contrast text.
 */
@Composable
fun StockticsBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.NEUTRAL,
    showTrendIcon: Boolean = false
) {
    val (textColor, bgColor) = when (variant) {
        BadgeVariant.GAIN -> GainGreen to GainGreenBg
        BadgeVariant.LOSS -> LossRed to LossRedBg
        BadgeVariant.NEUTRAL -> WarningAmber to WarningAmberBg
        BadgeVariant.INFO -> FintechBlue to FintechBlueDim
        BadgeVariant.MUTED -> TextSecondary to FintechSurfaceElevated
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showTrendIcon) {
                val icon = if (variant == BadgeVariant.GAIN) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = text,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
