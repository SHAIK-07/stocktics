package com.stocktics.presentation.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.settings.QuantEngineWeights
import com.stocktics.data.settings.QuantWeightsPreferences
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.solidLiquidGlass
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import kotlin.math.roundToInt

@Composable
fun QuantWeightsConfigScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialWeights = remember { QuantWeightsPreferences.getWeights(context) }

    var techWeight by remember { mutableFloatStateOf(initialWeights.techWeight) }
    var mlWeight by remember { mutableFloatStateOf(initialWeights.mlWeight) }
    var smartMoneyWeight by remember { mutableFloatStateOf(initialWeights.smartMoneyWeight) }
    var regimeWeight by remember { mutableFloatStateOf(initialWeights.regimeWeight) }
    var forensicWeight by remember { mutableFloatStateOf(initialWeights.forensicWeight) }

    var defaultStopLoss by remember { mutableFloatStateOf(initialWeights.defaultStopLoss) }
    var defaultTarget by remember { mutableFloatStateOf(initialWeights.defaultProfitTarget) }

    val totalWeightPct = ((techWeight + mlWeight + smartMoneyWeight + regimeWeight + forensicWeight) * 100f).roundToInt()
    val isBalanced = totalWeightPct == 100

    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Sticky Header
            StickyConfigHeader(
                onBack = onBack,
                onReset = {
                    techWeight = 0.30f
                    mlWeight = 0.25f
                    smartMoneyWeight = 0.20f
                    regimeWeight = 0.15f
                    forensicWeight = 0.10f
                    defaultStopLoss = 0.05f
                    defaultTarget = 0.15f
                    Toast.makeText(context, "Reset to Audited Optimal (30/25/20/15/10)", Toast.LENGTH_SHORT).show()
                }
            )

            // 2. Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro / Status Banner Card
                item {
                    StockticsCard(
                        cornerRadius = 16.dp,
                        contentPadding = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(FintechSurfaceElevated)
                                        .border(1.dp, FintechBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = null,
                                        tint = FintechPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Custom Engine Weights",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Fine-tune factor sensitivity for scoring",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            StockticsBadge(
                                text = if (isBalanced) "100% BALANCED" else "TOTAL: $totalWeightPct%",
                                variant = if (isBalanced) BadgeVariant.GAIN else BadgeVariant.LOSS
                            )
                        }
                    }
                }

                // Core 5 Pillars Weight Sliders
                item {
                    StockticsCard(
                        cornerRadius = 16.dp,
                        contentPadding = 18.dp
                    ) {
                        Text(
                            text = "Buy Score Factor Weights",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Adjust the percentage contribution of each quantitative dimension.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Technical
                        WeightSliderRow(
                            title = "Technical Indicators (Tech)",
                            subtitle = "Momentum, RSI, Moving Averages & breakouts",
                            weight = techWeight,
                            onWeightChange = { techWeight = (it * 100).roundToInt() / 100f }
                        )

                        // 2. ML Consensus
                        WeightSliderRow(
                            title = "ML Model Consensus (ML)",
                            subtitle = "Ensemble tree predictions & directional classifiers",
                            weight = mlWeight,
                            onWeightChange = { mlWeight = (it * 100).roundToInt() / 100f }
                        )

                        // 3. Smart Money
                        WeightSliderRow(
                            title = "Smart Money Flow (Smart $)",
                            subtitle = "FII/DII institutional positioning & block activity",
                            weight = smartMoneyWeight,
                            onWeightChange = { smartMoneyWeight = (it * 100).roundToInt() / 100f }
                        )

                        // 4. Regime
                        WeightSliderRow(
                            title = "Market Regime & Internals (Regime)",
                            subtitle = "Volatility regime, PCR & breadth confirmation",
                            weight = regimeWeight,
                            onWeightChange = { regimeWeight = (it * 100).roundToInt() / 100f }
                        )

                        // 5. Forensic
                        WeightSliderRow(
                            title = "Forensic & Accounting Health (Forensic)",
                            subtitle = "Beneish M-Score, Altman Z & earnings veracity",
                            weight = forensicWeight,
                            onWeightChange = { forensicWeight = (it * 100).roundToInt() / 100f }
                        )
                    }
                }

                // Risk Sliders Card (Stop-Loss & Profit Target)
                item {
                    StockticsCard(
                        cornerRadius = 16.dp,
                        contentPadding = 18.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = FintechPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Default Risk Parameters",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stop Loss Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Stop-Loss %", fontSize = 12.sp, color = TextSecondary)
                            Text("${(defaultStopLoss * 100).roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LossRed)
                        }
                        Slider(
                            value = defaultStopLoss,
                            onValueChange = { defaultStopLoss = (it * 100).roundToInt() / 100f },
                            valueRange = 0.02f..0.15f,
                            colors = SliderDefaults.colors(
                                thumbColor = LossRed,
                                activeTrackColor = LossRed,
                                inactiveTrackColor = FintechSurfaceElevated
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target Profit Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Profit Target %", fontSize = 12.sp, color = TextSecondary)
                            Text("${(defaultTarget * 100).roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GainGreen)
                        }
                        Slider(
                            value = defaultTarget,
                            onValueChange = { defaultTarget = (it * 100).roundToInt() / 100f },
                            valueRange = 0.05f..0.30f,
                            colors = SliderDefaults.colors(
                                thumbColor = GainGreen,
                                activeTrackColor = GainGreen,
                                inactiveTrackColor = FintechSurfaceElevated
                            )
                        )
                    }
                }
            }
        }

        // Sticky Bottom Glass Action Bar with 2 Buttons: Reset & Save
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .solidLiquidGlass(cornerRadius = 24.dp, elevation = 16.dp)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Reset Button (Outlined)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            techWeight = 0.30f
                            mlWeight = 0.25f
                            smartMoneyWeight = 0.20f
                            regimeWeight = 0.15f
                            forensicWeight = 0.10f
                            defaultStopLoss = 0.05f
                            defaultTarget = 0.15f
                            QuantWeightsPreferences.resetToAudited(context)
                            Toast.makeText(context, "Reset to Audited Defaults", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }

                // 2. Save & Apply CTA Button
                Box(
                    modifier = Modifier
                        .weight(1.8f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechPrimary)
                        .clickable {
                            val updated = QuantEngineWeights(
                                techWeight = techWeight,
                                mlWeight = mlWeight,
                                smartMoneyWeight = smartMoneyWeight,
                                regimeWeight = regimeWeight,
                                forensicWeight = forensicWeight,
                                defaultStopLoss = defaultStopLoss,
                                defaultProfitTarget = defaultTarget
                            )
                            QuantWeightsPreferences.saveWeights(context, updated)
                            Toast.makeText(context, "Engine weights saved successfully", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save & Apply Weights",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StickyConfigHeader(
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FintechBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Quant Engine Weights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        IconButton(
            onClick = onReset,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(FintechSurfaceElevated)
                .border(1.dp, FintechBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = "Reset",
                tint = FintechPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun WeightSliderRow(
    title: String,
    subtitle: String,
    weight: Float,
    onWeightChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${(weight * 100).roundToInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechPrimary
                )
            }
        }

        Slider(
            value = weight,
            onValueChange = onWeightChange,
            valueRange = 0.05f..0.50f,
            colors = SliderDefaults.colors(
                thumbColor = FintechPrimary,
                activeTrackColor = FintechPrimary,
                inactiveTrackColor = FintechSurfaceElevated
            )
        )
    }
}
