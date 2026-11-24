package com.stocktics.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.theme.LocalFintechColors
import com.stocktics.ui.theme.ThemeManager

/**
 * Modern Clean Global Top App Bar (Modeled after INDmoney & Groww flagship experience).
 * Features:
 * - Left: Stocktics Brand Wordmark & Icon
 * - Right: Theme Toggle Switch (Light/Dark) + User Profile Avatar ("S" Settings Action)
 */
@Composable
fun StockticsTopBar(
    currentMarket: GlobalMarket = GlobalMarket.INDIA,
    onMarketSelected: (GlobalMarket) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    userEmail: String? = null,
    onOpenProfile: () -> Unit = {},
    userNickname: String? = null,
    totalPortfolioValue: String? = null,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(fintech.background.copy(alpha = 0.96f))
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Stocktics Brand Identity
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockticsLogoIcon(size = 30.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "STOCK",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = fintech.textPrimary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "TICS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = fintech.primary,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Right: Theme Toggle Switch + User Profile / Settings Action ("S" Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Simple Light / Dark Mode Toggle Button
                ThemeToggleSwitch(
                    isDark = fintech.isDark,
                    onToggle = {
                        val nextMode = if (fintech.isDark) ThemeManager.ThemeMode.LIGHT else ThemeManager.ThemeMode.DARK
                        ThemeManager.setTheme(context, nextMode)
                    }
                )

                // User Profile / Settings Action ("S" Avatar)
                val initial = (userNickname?.firstOrNull() ?: userEmail?.firstOrNull())?.uppercaseChar()?.toString() ?: "U"
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(fintech.surfaceElevated)
                        .border(1.dp, fintech.border, CircleShape)
                        .clickable(onClick = onOpenProfile)
                        .fintechPressBounce(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = fintech.primary
                    )
                }
            }
        }
    }
}

/**
 * Simple, elegant pill toggle switch for toggling between Light and Dark mode.
 * Styled with smooth spring physics and institutional hairline borders.
 */
@Composable
fun ThemeToggleSwitch(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current

    val thumbOffsetX by animateDpAsState(
        targetValue = if (isDark) 20.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "themeThumbOffset"
    )

    val trackBgColor by animateColorAsState(
        targetValue = if (isDark) fintech.surfaceElevated else Color(0xFFE2E8F0),
        label = "themeTrackBg"
    )

    val thumbBgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0B0E14) else Color.White,
        label = "themeThumbBg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isDark) fintech.primary else Color(0xFFF59E0B),
        label = "themeIconTint"
    )

    Box(
        modifier = modifier
            .width(46.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackBgColor)
            .border(1.dp, fintech.border, RoundedCornerShape(13.dp))
            .clickable(onClick = onToggle)
            .fintechPressBounce(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffsetX)
                .size(22.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(thumbBgColor)
                .border(0.5.dp, fintech.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = iconTint,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
