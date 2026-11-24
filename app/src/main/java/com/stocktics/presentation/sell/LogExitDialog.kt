package com.stocktics.presentation.sell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsDangerButton
import com.stocktics.ui.theme.FintechBackground
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogExitDialog(
    symbol: String,
    exchange: String,
    currency: String = "₹",
    entryPrice: Double,
    currentPrice: Double,
    totalQty: Double,
    logoUrl: String = "",
    onDismiss: () -> Unit,
    onConfirmExit: (exitPrice: Double, exitQty: Double, reason: String) -> Unit
) {
    var exitPriceText by remember { mutableStateOf(if (currentPrice > 0) "%.2f".format(currentPrice) else "%.2f".format(entryPrice)) }
    var exitQtyText by remember { mutableStateOf("%.0f".format(totalQty)) }
    var selectedReason by remember { mutableStateOf("Target Achieved") }

    val exitReasons = remember {
        listOf(
            "Target Achieved",
            "Stop-Loss Hit",
            "Bearish Reversal",
            "Time Limit Reached",
            "Manual Exit"
        )
    }

    val parsedExitPrice = exitPriceText.toDoubleOrNull() ?: 0.0
    val parsedExitQty = exitQtyText.toDoubleOrNull() ?: 0.0
    val isValid = parsedExitPrice > 0 && parsedExitQty > 0 && parsedExitQty <= totalQty

    val realizedPnl = (parsedExitPrice - entryPrice) * parsedExitQty
    val realizedPnlPct = if (entryPrice > 0) ((parsedExitPrice - entryPrice) / entryPrice) * 100.0 else 0.0
    val isProfit = realizedPnl >= 0

    Dialog(onDismissRequest = onDismiss) {
        StockticsCard(
            cornerRadius = 20.dp,
            contentPadding = 20.dp
        ) {
            // Top Row: Stock Info & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StockCompanyLogo(
                            symbol = symbol,
                            initials = symbol.take(2).uppercase(),
                            logoUrl = logoUrl,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Log Exit / Sell",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$symbol • Entry: $currency %.2f".format(entryPrice),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FintechSurfaceElevated)
                            .border(1.dp, FintechBorder, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price & Quantity Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Exit Price
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exit Price ($currency)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechBackground)
                                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = exitPriceText,
                                onValueChange = { exitPriceText = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(FintechPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Exit Quantity
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sell Qty",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                text = "Max: %.0f".format(totalQty),
                                fontSize = 11.sp,
                                color = FintechPrimary,
                                modifier = Modifier.clickable {
                                    exitQtyText = "%.0f".format(totalQty)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechBackground)
                                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = exitQtyText,
                                onValueChange = { exitQtyText = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(FintechPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Exit Reason Choice Chips
                Text(
                    text = "Exit Trigger Reason",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exitReasons.forEach { reason ->
                        val isSelected = reason == selectedReason
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FintechPrimary else FintechSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) FintechPrimary else FintechBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedReason = reason }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = reason,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Realized P&L Preview Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isProfit) GainGreenBg else LossRedBg)
                        .border(1.dp, if (isProfit) GainGreen.copy(alpha = 0.3f) else LossRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Realized P&L",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            val pnlSign = if (realizedPnl >= 0) "+" else ""
                            Text(
                                text = "$pnlSign$currency %.2f".format(realizedPnl),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isProfit) GainGreen else LossRed
                            )
                        }

                        Text(
                            text = "${if (realizedPnlPct >= 0) "+" else ""}%.2f%%".format(realizedPnlPct),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProfit) GainGreen else LossRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Danger Button to finalize exit
                StockticsDangerButton(
                    text = "Confirm & Execute Exit",
                    enabled = isValid,
                    onClick = {
                        onConfirmExit(parsedExitPrice, parsedExitQty, selectedReason)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
        }
    }
}
