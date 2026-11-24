package com.stocktics.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stocktics.ui.components.solidLiquidGlass
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.LocalFintechColors
import com.stocktics.ui.theme.TextTertiary

@Composable
fun FloatingPillNavBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .solidLiquidGlass(cornerRadius = 28.dp, elevation = 12.dp)
        ) {
            // Specular top highlight horizon line (Apple VisionOS / iOS glass light reflection)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                if (fintech.isDark) Color(0x4DFFFFFF) else Color(0xCCFFFFFF),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Order: HOME -> BUY -> EXPLORE -> SELL -> SETTINGS
                BottomNavTab.values().forEach { tab ->
                    val isSelected = tab == currentTab
                    PillNavItem(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.PillNavItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tabScale"
    )

    // Exact Royal Blue (FintechBlue / FintechPrimary) matching the Indian & US Market tabs
    val itemBgColor by animateColorAsState(
        targetValue = if (isSelected) FintechBlue else Color.Transparent,
        label = "tabBgColor"
    )

    // Crisp white icon on the blue button, muted slate for inactive
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else fintech.textTertiary,
        label = "tabColor"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(itemBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
