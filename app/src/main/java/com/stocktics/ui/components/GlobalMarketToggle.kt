package com.stocktics.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.theme.LocalFintechColors

enum class GlobalMarket(val code: String, val flag: String, val displayName: String, val currencySymbol: String) {
    INDIA("IN", "🇮🇳", "India (NSE/BSE)", "₹"),
    US("US", "🇺🇸", "US (NYSE/NASDAQ)", "$")
}

/**
 * High-Precision Compact Global Market Switcher.
 * Positioned in the global top header, enabling 1-tap switching between Indian & US markets
 * across the entire application without cluttering individual screen spaces.
 */
@Composable
fun GlobalMarketToggle(
    currentMarket: GlobalMarket,
    onMarketSelected: (GlobalMarket) -> Unit,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(fintech.surfaceElevated)
            .border(width = 1.dp, color = fintech.border, shape = CircleShape)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlobalMarket.values().forEach { market ->
            val isSelected = market == currentMarket
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) fintech.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "marketBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF0B0E14) else fintech.textSecondary,
                animationSpec = tween(durationMillis = 200),
                label = "marketText"
            )

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { onMarketSelected(market) }
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = market.flag,
                    fontSize = 11.sp,
                    lineHeight = 11.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = market.code,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
