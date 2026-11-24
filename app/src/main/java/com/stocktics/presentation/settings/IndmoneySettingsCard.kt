package com.stocktics.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.api.ApiIndmoneyStatus
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.StockticsSecondaryButton
import com.stocktics.ui.components.fintechPressBounce
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

/**
 * Institutional INDmoney MCP Connection & Sync Card in Settings.
 * Provides real-time connection status, manual sync triggers, and account management.
 */
@Composable
fun IndmoneySettingsCard(
    status: ApiIndmoneyStatus,
    isSyncing: Boolean,
    lastFeedback: String?,
    onConnectClick: () -> Unit,
    onSyncNowClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDisconnectConfirmation by remember { mutableStateOf(false) }

    StockticsCard(
        cornerRadius = 16.dp,
        contentPadding = 18.dp,
        modifier = modifier
    ) {
        // Header Row: INDmoney Badge + Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FintechBlue.copy(alpha = 0.15f))
                        .border(1.dp, FintechBlue.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FintechBlue
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "INDmoney Portfolio Sync",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Official MCP 2.1 Protocol",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }

            if (status.connected) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GainGreenBg)
                        .border(1.dp, GainGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GainGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "SYNCED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GainGreen
                    )
                }
            } else {
                StockticsBadge(
                    text = "NOT CONNECTED",
                    variant = BadgeVariant.MUTED
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feedback Banner
        AnimatedVisibility(
            visible = !lastFeedback.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (lastFeedback?.contains("disconnect", ignoreCase = true) == true) LossRedBg else GainGreenBg)
                    .border(
                        1.dp,
                        if (lastFeedback?.contains("disconnect", ignoreCase = true) == true) LossRed.copy(alpha = 0.3f) else GainGreen.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = lastFeedback ?: "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (lastFeedback?.contains("disconnect", ignoreCase = true) == true) LossRed else GainGreen
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (status.connected) {
            // Synced Details Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FintechBackground)
                    .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Holdings", fontSize = 12.sp, color = TextSecondary)
                    val breakdown = if (status.indianStocksCount > 0 || status.usStocksCount > 0) {
                        "${status.holdingsCount} Stocks (${status.indianStocksCount} IN • ${status.usStocksCount} US)"
                    } else {
                        "${status.holdingsCount} Stocks"
                    }
                    Text(
                        text = breakdown,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (status.watchlistsCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Watchlist Items", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "${status.watchlistsCount} stocks",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Last Synced", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = status.lastSync?.take(16)?.replace("T", " ") ?: "Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (Sync Now + Disconnect)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sync Now Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FintechPrimary)
                        .clickable(enabled = !isSyncing) { onSyncNowClick() }
                        .fintechPressBounce(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Sync Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Disconnect Button
                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, LossRed.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable(enabled = !isSyncing) { showDisconnectConfirmation = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Disconnect",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LossRed
                    )
                }
            }
        } else {
            // Not connected view: Explanations + Connect Button
            Text(
                text = "Connect your INDmoney account via official MCP OAuth to auto-import your live Indian & US stock holdings, buy prices, and watchlists.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "Auto-sync for NSE, BSE & US stocks",
                    "Continuous real-time portfolio & P&L tracking",
                    "Bank-grade OAuth 2.1 protocol with PKCE"
                ).forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = FintechPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connect CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FintechPrimary)
                    .clickable(enabled = !isSyncing) { onConnectClick() }
                    .fintechPressBounce(),
                contentAlignment = Alignment.Center
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Connect INDmoney Account",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }

    // Disconnect Confirmation Dialog
    if (showDisconnectConfirmation) {
        AlertDialog(
            onDismissRequest = { showDisconnectConfirmation = false },
            title = {
                Text(
                    text = "Disconnect INDmoney?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Auto-sync will stop. Existing holdings imported into Stocktics will remain saved in your local portfolio.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDisconnectConfirmation = false
                        onDisconnectClick()
                    }
                ) {
                    Text(text = "Disconnect", color = LossRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectConfirmation = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            },
            containerColor = FintechSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
