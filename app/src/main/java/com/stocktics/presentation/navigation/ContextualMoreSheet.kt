package com.stocktics.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.components.fintechPressBounce
import com.stocktics.ui.theme.LocalFintechColors

/**
 * Contextual "More" Bottom Sheet modeled after INDmoney's quick-action drawer.
 */
@Composable
fun ContextualMoreSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenHoldings: () -> Unit,
    onOpenWatchlist: () -> Unit,
    onOpenHeatmap: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenSectors: () -> Unit = {},
    onOpenAcademy: () -> Unit,
    onOpenWeightsConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fintech = LocalFintechColors.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .navigationBarsPadding()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Intercept clicks inside card
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(fintech.surface)
                            .border(1.dp, fintech.border, RoundedCornerShape(24.dp))
                            .padding(vertical = 12.dp)
                    ) {
                        // Drag Pill Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 36.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(fintech.border)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Quick Actions & Tools",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = fintech.textTertiary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )

                        MoreSheetMenuItem(
                            icon = Icons.Rounded.Explore,
                            title = "Explore & Discover",
                            subtitle = "Top Gainers, Breakouts & Sectors",
                            onClick = {
                                onDismiss()
                                onOpenExplore()
                            }
                        )

                        MoreSheetMenuItem(
                            icon = Icons.Rounded.AccountBalanceWallet,
                            title = "Holdings & Portfolio",
                            subtitle = "View current active positions & P&L",
                            onClick = {
                                onDismiss()
                                onOpenHoldings()
                            }
                        )

                        MoreSheetMenuItem(
                            icon = Icons.Rounded.BookmarkBorder,
                            title = "Watchlist",
                            subtitle = "Your tracked radar stocks",
                            onClick = {
                                onDismiss()
                                onOpenWatchlist()
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            thickness = 1.dp,
                            color = fintech.border.copy(alpha = 0.6f)
                        )

                        MoreSheetMenuItem(
                            icon = Icons.Rounded.GridView,
                            title = "Market Treemap Heatmap",
                            subtitle = "Visual sector-by-sector heat overview",
                            onClick = {
                                onDismiss()
                                onOpenHeatmap()
                            }
                        )

                        MoreSheetMenuItem(
                            icon = Icons.AutoMirrored.Rounded.CompareArrows,
                            title = "Stock Comparison Radar",
                            subtitle = "Multi-stock quant comparison",
                            onClick = {
                                onDismiss()
                                onOpenCompare()
                            }
                        )

                        MoreSheetMenuItem(
                            icon = Icons.Rounded.PieChart,
                            title = "GICS Sector Explorer",
                            subtitle = "11 Sectors, breadth & top stocks",
                            onClick = {
                                onDismiss()
                                onOpenSectors()
                            }
                        )

                        MoreSheetMenuItem(
                            icon = Icons.Rounded.School,
                            title = "Stocktics Academy",
                            subtitle = "Learn quant signals & setups",
                            onClick = {
                                onDismiss()
                                onOpenAcademy()
                            }
                        )

                        MoreSheetMenuItem(
                            icon = Icons.Rounded.Tune,
                            title = "Quant Strategy & Weights",
                            subtitle = "Customize risk, momentum & valuation",
                            onClick = {
                                onDismiss()
                                onOpenWeightsConfig()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreSheetMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val fintech = LocalFintechColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .fintechPressBounce()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(fintech.surfaceElevated)
                    .border(1.dp, fintech.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = fintech.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = fintech.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = fintech.textTertiary
                )
            }
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = fintech.textTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}
