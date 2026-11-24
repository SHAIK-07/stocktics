package com.stocktics.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.components.fintechPressBounce
import com.stocktics.ui.theme.LocalFintechColors

/**
 * Modern Dynamic Morphing Navigation Bar modeled after INDmoney's flagship UI.
 *
 * Supports:
 * - Expanding on scroll UP (with icon and name)
 * - Compressing on scroll DOWN (icon only, reduced height)
 * - Identical active tab shadow, border, and background across all navigation tabs
 *
 * Mode 1: Root / Home Mode
 * [ Home | IND | US | Charts ]   (Q Search)
 *
 * Mode 2: Contextual Market / Charts Mode
 * (Home)   [ Holdings | Buy Pipeline | Sell Pipeline | More ]   (Q Search)
 */
@Composable
fun DynamicFintechNavBar(
    navContext: AppNavContext,
    isExpanded: Boolean = true,
    onSelectRootTab: (RootNavTab) -> Unit,
    onSelectMarketSubTab: (MarketSubTab) -> Unit,
    onSelectChartsSubTab: (ChartsSubTab) -> Unit,
    onReturnHome: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val isCompactScreen = availableWidth < 380.dp

        // Dynamic responsive sizing (Generously proportioned for high touch comfort)
        val circleButtonSizeTarget = when {
            !isExpanded -> if (isCompactScreen) 48.dp else 50.dp
            isCompactScreen -> 54.dp
            else -> 58.dp
        }
        val circleButtonSize by animateDpAsState(
            targetValue = circleButtonSizeTarget,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "navCircleButtonSize"
        )

        val pillHeight by animateDpAsState(
            targetValue = if (isExpanded) (if (isCompactScreen) 54.dp else 58.dp) else (if (isCompactScreen) 48.dp else 50.dp),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "navPillHeight"
        )

        // Root pill width (scaled to fit available width alongside search button)
        val maxRootPill = (availableWidth - circleButtonSizeTarget - 18.dp).coerceAtLeast(190.dp)
        val rootPillWidthTarget = if (isExpanded) {
            minOf(if (isCompactScreen) 295.dp else 315.dp, maxRootPill)
        } else {
            minOf(if (isCompactScreen) 220.dp else 235.dp, maxRootPill)
        }
        val rootPillWidth by animateDpAsState(
            targetValue = rootPillWidthTarget,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "navRootPillWidth"
        )

        // Contextual pill width (scaled to fit between home and search buttons)
        val maxContextualPill = (availableWidth - (circleButtonSizeTarget * 2) - 20.dp).coerceAtLeast(170.dp)
        val contextualPillWidthTarget = if (isExpanded) {
            minOf(if (isCompactScreen) 250.dp else 270.dp, maxContextualPill)
        } else {
            minOf(if (isCompactScreen) 200.dp else 215.dp, maxContextualPill)
        }
        val contextualPillWidth by animateDpAsState(
            targetValue = contextualPillWidthTarget,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "navContextualPillWidth"
        )

        val itemSpacing = if (isCompactScreen) 6.dp else 8.dp

        AnimatedContent(
            targetState = navContext,
            transitionSpec = {
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            },
            label = "navMorphTransition"
        ) { currentContext ->
            when (currentContext) {
                is AppNavContext.HomeRoot -> {
                    // ==============================================================
                    // Root / Home State: [ Home | IND | US | Charts ] + (Search)
                    // ==============================================================
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main Pill Container
                        Box(
                            modifier = Modifier
                                .width(rootPillWidth)
                                .height(pillHeight)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    spotColor = Color.Black.copy(alpha = 0.25f)
                                )
                                .background(
                                    if (fintech.isDark) fintech.surface else Color(0xFFF8FAFC),
                                    CircleShape
                                )
                                .border(1.dp, fintech.border, CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RootNavTab.values().forEach { tab ->
                                    val isSelected = tab == RootNavTab.HOME
                                    RootPillItem(
                                        tab = tab,
                                        isSelected = isSelected,
                                        isExpanded = isExpanded,
                                        onClick = { onSelectRootTab(tab) }
                                    )
                                }
                            }
                        }

                        // Detached Search Circle Button
                        SearchCircleButton(
                            size = circleButtonSize,
                            onClick = onOpenSearch
                        )
                    }
                }

                is AppNavContext.Market -> {
                    // ==============================================================
                    // Market State: (Home) + [ Holdings | Buy | Sell | Watchlist ] + (Search)
                    // ==============================================================
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Compact Home Circle Button
                        HomeCircleButton(
                            size = circleButtonSize,
                            onClick = onReturnHome
                        )

                        // Center Contextual Market Sub-nav Pill
                        Box(
                            modifier = Modifier
                                .width(contextualPillWidth)
                                .height(pillHeight)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    spotColor = Color.Black.copy(alpha = 0.25f)
                                )
                                .background(
                                    if (fintech.isDark) fintech.surface else Color(0xFFF8FAFC),
                                    CircleShape
                                )
                                .border(1.dp, fintech.border, CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MarketSubTab.values().forEach { subTab ->
                                    val isSelected = subTab == currentContext.subTab
                                    ContextualPillItem(
                                        title = subTab.title,
                                        icon = subTab.icon,
                                        isSelected = isSelected,
                                        isExpanded = isExpanded,
                                        onClick = { onSelectMarketSubTab(subTab) }
                                    )
                                }
                            }
                        }

                        // Right Search Circle Button
                        SearchCircleButton(
                            size = circleButtonSize,
                            onClick = onOpenSearch
                        )
                    }
                }

                is AppNavContext.Charts -> {
                    // ==============================================================
                    // Charts State: (Home) + [ Patterns | Heatmap | Compare | Academy ] + (Search)
                    // ==============================================================
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeCircleButton(
                            size = circleButtonSize,
                            onClick = onReturnHome
                        )

                        Box(
                            modifier = Modifier
                                .width(contextualPillWidth)
                                .height(pillHeight)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    spotColor = Color.Black.copy(alpha = 0.25f)
                                )
                                .background(
                                    if (fintech.isDark) fintech.surface else Color(0xFFF8FAFC),
                                    CircleShape
                                )
                                .border(1.dp, fintech.border, CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    ChartsSubTab.PATTERNS,
                                    ChartsSubTab.HEATMAP,
                                    ChartsSubTab.COMPARE,
                                    ChartsSubTab.SECTORS
                                ).forEach { subTab ->
                                    val isSelected = subTab == currentContext.subTab
                                    ContextualPillItem(
                                        title = subTab.title,
                                        icon = subTab.icon,
                                        isSelected = isSelected,
                                        isExpanded = isExpanded,
                                        onClick = { onSelectChartsSubTab(subTab) }
                                    )
                                }
                            }
                        }

                        AcademyCircleButton(
                            size = circleButtonSize,
                            isSelected = currentContext.subTab == ChartsSubTab.ACADEMY,
                            onClick = { onSelectChartsSubTab(ChartsSubTab.ACADEMY) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Root Item in the Main Navigation Pill with consistent active shadow and background.
 */
@Composable
private fun RowScope.RootPillItem(
    tab: RootNavTab,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) fintech.textPrimary else fintech.textTertiary,
        label = "rootTabColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rootTabIconScale"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(
                            elevation = 2.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.15f),
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .background(
                            if (fintech.isDark) Color(0xFF1E2638) else Color(0xFFFFFFFF),
                            shape = CircleShape
                        )
                        .border(
                            1.dp,
                            if (fintech.isDark) Color(0xFF2A364F) else Color(0xFFE2E8F0),
                            shape = CircleShape
                        )
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = contentColor,
                modifier = Modifier
                    .size(if (isExpanded) 20.dp else 22.dp)
                    .scale(iconScale)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Sub-Item in Contextual Navigation Pill with identical active shadow and background.
 */
@Composable
private fun RowScope.ContextualPillItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) fintech.textPrimary else fintech.textTertiary,
        label = "contextualTabColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "contextualTabIconScale"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(
                            elevation = 2.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.15f),
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .background(
                            if (fintech.isDark) Color(0xFF1E2638) else Color(0xFFFFFFFF),
                            shape = CircleShape
                        )
                        .border(
                            1.dp,
                            if (fintech.isDark) Color(0xFF2A364F) else Color(0xFFE2E8F0),
                            shape = CircleShape
                        )
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier
                    .size(if (isExpanded) 20.dp else 22.dp)
                    .scale(iconScale)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Compact Circular Home Button (Appears on far left in contextual modes).
 */
@Composable
private fun HomeCircleButton(
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(CircleShape)
            .background(fintech.surface)
            .border(1.dp, fintech.border, CircleShape)
            .clickable(onClick = onClick)
            .fintechPressBounce(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Home,
            contentDescription = "Return to Home",
            tint = fintech.textPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Circular Search Button (Always floating on far right).
 */
@Composable
private fun SearchCircleButton(
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(CircleShape)
            .background(fintech.surface)
            .border(1.dp, fintech.border, CircleShape)
            .clickable(onClick = onClick)
            .fintechPressBounce(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search Stocks",
            tint = fintech.textPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Circular Academy Button (Replaces search button in Tools mode).
 */
@Composable
private fun AcademyCircleButton(
    size: androidx.compose.ui.unit.Dp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(CircleShape)
            .background(if (isSelected) fintech.primary else fintech.surface)
            .border(1.dp, if (isSelected) fintech.primary else fintech.border, CircleShape)
            .clickable(onClick = onClick)
            .fintechPressBounce(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.School,
            contentDescription = "Quantitative Academy",
            tint = if (isSelected) Color.Black else fintech.textPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}
