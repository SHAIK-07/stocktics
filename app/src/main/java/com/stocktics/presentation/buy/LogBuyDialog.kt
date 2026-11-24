package com.stocktics.presentation.buy

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
import androidx.compose.material.icons.rounded.ShoppingBag
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
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.liquidGlassElevated
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary

@Composable
fun LogBuyDialog(
    symbol: String,
    companyName: String,
    exchange: String,
    currency: String = "₹",
    currentPrice: Double,
    logoUrl: String = "",
    onDismiss: () -> Unit,
    onConfirmBuy: (price: Double, qty: Double, stopLossPct: Double, targetPct: Double, maxHoldDays: Int) -> Unit
) {
    var priceText by remember { mutableStateOf(if (currentPrice > 0) "%.2f".format(currentPrice) else "") }
    var qtyText by remember { mutableStateOf("1") }
    var stopLossText by remember { mutableStateOf("5.0") }
    var targetText by remember { mutableStateOf("15.0") }
    var holdDaysText by remember { mutableStateOf("60") }

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
    val parsedQty = qtyText.toDoubleOrNull() ?: 0.0
    val totalAmount = parsedPrice * parsedQty
    val isValid = parsedPrice > 0 && parsedQty > 0

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
                                text = "Log Purchase",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$symbol • $exchange",
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
                    // Buy Price
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Buy Price ($currency)",
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
                                value = priceText,
                                onValueChange = { priceText = it },
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

                    // Quantity
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quantity",
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
                                value = qtyText,
                                onValueChange = { qtyText = it },
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

                // Risk Parameters Row (Stop-Loss & Target)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stop-Loss (%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechBackground)
                                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = stopLossText,
                                onValueChange = { stopLossText = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                cursorBrush = SolidColor(FintechPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Target (%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechBackground)
                                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = targetText,
                                onValueChange = { targetText = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                cursorBrush = SolidColor(FintechPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Capital Outlay Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassElevated(12.dp)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Outlay",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "$currency %.2f".format(totalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val slVal = parsedPrice * (1.0 - (stopLossText.toDoubleOrNull() ?: 5.0) / 100.0)
                            val tgtVal = parsedPrice * (1.0 + (targetText.toDoubleOrNull() ?: 15.0) / 100.0)
                            Text(
                                text = "SL: $currency %.1f".format(slVal),
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                            Text(
                                text = "TGT: $currency %.1f".format(tgtVal),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GainGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Button
                StockticsPrimaryButton(
                    text = "Confirm & Log Purchase",
                    enabled = isValid,
                    onClick = {
                        val sl = stopLossText.toDoubleOrNull() ?: 5.0
                        val tgt = targetText.toDoubleOrNull() ?: 15.0
                        val days = holdDaysText.toIntOrNull() ?: 60
                        onConfirmBuy(parsedPrice, parsedQty, sl, tgt, days)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
        }
    }
}
