package com.stocktics.presentation.sell

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.stocktics.data.portfolio.PortfolioHolding
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.liquidGlassElevated
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun EditHoldingDialog(
    holding: PortfolioHolding,
    onDismiss: () -> Unit,
    onConfirmEdit: (price: Double, qty: Double, stopLossPct: Double, targetPct: Double, maxHoldDays: Int) -> Unit
) {
    var priceText by remember { mutableStateOf(String.format(Locale.US, "%.2f", holding.entryPrice)) }
    var qtyText by remember { mutableStateOf(if (holding.qty % 1.0 == 0.0) holding.qty.toLong().toString() else holding.qty.toString()) }
    var stopLossText by remember { mutableStateOf(String.format(Locale.US, "%.1f", holding.stopLossPct)) }
    var targetText by remember { mutableStateOf(String.format(Locale.US, "%.1f", holding.targetPct)) }
    var holdDaysText by remember { mutableStateOf(holding.maxHoldDays.toString()) }

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
    val parsedQty = qtyText.toDoubleOrNull() ?: 0.0
    val parsedSl = stopLossText.toDoubleOrNull() ?: 5.0
    val parsedTarget = targetText.toDoubleOrNull() ?: 15.0
    val parsedDays = holdDaysText.toIntOrNull() ?: 60

    val totalAmount = parsedPrice * parsedQty
    val isValid = parsedPrice > 0 && parsedQty > 0
    val cleanSym = holding.symbol.replace(".NS", "").replace(".BO", "")

    Dialog(onDismissRequest = onDismiss) {
        StockticsCard(
            cornerRadius = 20.dp,
            contentPadding = 20.dp
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockCompanyLogo(
                        symbol = holding.symbol,
                        initials = cleanSym.take(2).uppercase(),
                        logoUrl = holding.logoUrl,
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Edit Position",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$cleanSym • ${holding.exchange}",
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
                // Buy / Entry Price
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Entry Price (${holding.currency})",
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
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
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

                // Quantity
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quantity (Shares)",
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
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
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

            // Stop Loss & Target & Max Hold Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stop Loss %
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stop-Loss (%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = stopLossText,
                            onValueChange = { stopLossText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                            cursorBrush = SolidColor(FintechPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Target %
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Target (%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = targetText,
                            onValueChange = { targetText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                            cursorBrush = SolidColor(FintechPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Horizon Days
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Max Days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = holdDaysText,
                            onValueChange = { holdDaysText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                            cursorBrush = SolidColor(FintechPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Value Readout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassElevated(12.dp)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Investment",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${holding.currency} ${String.format(Locale.US, "%,.2f", totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Updates Cloud & Local",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = GainGreen
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions: Cancel & Save Changes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isValid) FintechPrimary else FintechSurfaceElevated)
                        .clickable(enabled = isValid) {
                            if (isValid) {
                                onConfirmEdit(parsedPrice, parsedQty, parsedSl, parsedTarget, parsedDays)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save Changes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isValid) Color.Black else TextTertiary
                    )
                }
            }
        }
    }
}
