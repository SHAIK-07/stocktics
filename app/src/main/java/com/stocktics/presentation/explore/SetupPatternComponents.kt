package com.stocktics.presentation.explore

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.data.stocks.CategoryStockItem
import com.stocktics.data.stocks.StockCategory
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.IsDarkTheme
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import com.stocktics.ui.theme.WarningAmber
import java.util.Locale

data class TopWeeklySetupItem(
    val rank: Int,
    val symbol: String,
    val name: String,
    val exchange: String,
    val compositeRank: String,
    val trendScore: String,
    val relStrScore: String,
    val volumeScore: String,
    val patternName: String,
    val logoUrl: String,
    val initials: String,
    val isIndian: Boolean
)

data class PatternTheoryMeta(
    val code: String,
    val typeSubtitle: String,
    val patternTitle: String,
    val institutionalRule: String,
    val diagramType: String,
    val patternId: String = "BREAKOUT",
    @DrawableRes val imageResId: Int? = null,
    val overlayBadge: String? = null,
    val overlaySubtitle: String? = null
)

/**
 * Netflix-Style Card for Top Setups This Week (Screenshot 2 / Web Setups)
 */
@Composable
fun NetflixTopSetupCard(
    stock: TopWeeklySetupItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = IsDarkTheme
    StockticsCard(
        modifier = modifier
            .width(200.dp)
            .height(280.dp),
        cornerRadius = 16.dp,
        contentPadding = 0.dp,
        fillMaxWidth = false,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Giant Netflix-style watermark outline numeral at bottom-left corner
            Text(
                text = "${stock.rank}",
                fontSize = 82.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) Color(0x15F8FAFC) else Color(0x0E0F172A),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            // 1. Top Section: Logo + Market Badge + Symbol + Name
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StockCompanyLogo(
                        symbol = stock.symbol,
                        initials = stock.initials,
                        logoUrl = stock.logoUrl,
                        size = 38.dp
                    )
                    StockticsBadge(
                        text = stock.exchange,
                        variant = BadgeVariant.INFO
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stock.symbol.replace(".NS", "").replace(".BO", ""),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = stock.name,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 2. Middle Section: Composite Score Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GainGreenBg)
                    .border(1.dp, GainGreen.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stock.compositeRank,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = GainGreen
                    )
                    Text(
                        text = "COMPOSITE RANK",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = GainGreen
                    )
                }
            }

            // 3. Mini Metrics Grid: TREND | REL STR | VOLUME
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TREND", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                    Text(stock.trendScore, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                }
                Text("|", fontSize = 10.sp, color = FintechBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REL STR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                    Text(stock.relStrScore, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                }
                Text("|", fontSize = 10.sp, color = FintechBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VOLUME", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                    Text(stock.volumeScore, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                }
            }

            // 4. Bottom Pattern Pill with Flame
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Pattern",
                    tint = WarningAmber,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stock.patternName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        }
    }
}

/**
 * Card #1 in Each Category: Institutional Pattern Theory & Rule Card (Screenshot 3 / Web Setups)
 */
@Composable
fun PatternTheoryCard(
    meta: PatternTheoryMeta,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = IsDarkTheme
    StockticsCard(
        modifier = modifier
            .width(250.dp)
            .height(340.dp),
        cornerRadius = 16.dp,
        contentPadding = 14.dp,
        fillMaxWidth = false,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FintechBlue.copy(alpha = 0.15f))
                            .border(1.dp, FintechBlue.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = meta.code,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = FintechBlue
                        )
                    }
                    Text(
                        text = meta.typeSubtitle.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = meta.patternTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
            }

            // Visual Technical Diagram or 3D Setup Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            if (isDark) listOf(Color(0xFF0F172A), Color(0xFF070A0F))
                            else listOf(Color(0xFFF1F5F9), Color(0xFFFFFFFF))
                        )
                    )
                    .border(
                        1.dp,
                        if (isDark) FintechBorder else Color(0xFFCBD5E1),
                        RoundedCornerShape(10.dp)
                    )
            ) {
                if (meta.imageResId != null) {
                    Image(
                        painter = painterResource(id = meta.imageResId),
                        contentDescription = meta.patternTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        PatternSchematicCanvas(diagramType = meta.diagramType)
                    }
                }
            }

            // Institutional Rule Box
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ElectricBolt,
                        contentDescription = "Rule",
                        tint = FintechPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "INSTITUTIONAL SETUP RULE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meta.institutionalRule,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Footer Link: Audited Backtest & Math
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audited Backtest & Math",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FintechPrimary
                )
                Icon(
                    imageVector = Icons.Rounded.NorthEast,
                    contentDescription = "Backtest",
                    tint = FintechPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Cards #2 to #6: Active Stock Setup Card (Screenshot 3 / Web Setups)
 */
@Composable
fun ActiveSetupStockCard(
    stock: CategoryStockItem,
    category: StockCategory? = null,
    score: Int = 90,
    patternName: String? = null,
    pivotPrice: Double? = null,
    customReasons: List<String>? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanPrice = stock.rawPrice
    val currPrefix = if (stock.isIndian) "₹" else "$"
    val pivotLevel = pivotPrice ?: when (category) {
        StockCategory.FIFTY_TWO_WEEK_LOW -> cleanPrice * 0.985
        StockCategory.FIFTY_TWO_WEEK_HIGH -> cleanPrice * 1.015
        StockCategory.NEAR_SUPPORT -> cleanPrice * 0.982
        StockCategory.NEAR_RESISTANCE -> cleanPrice * 1.022
        StockCategory.DEBT_FREE -> cleanPrice * 0.975
        StockCategory.GOOD_GROWTH -> cleanPrice * 0.990
        null -> cleanPrice * 1.025
    }
    val formattedPivot = "$currPrefix ${String.format(Locale.US, "%,.2f", pivotLevel)}"
    val isDark = IsDarkTheme

    StockticsCard(
        modifier = modifier
            .width(235.dp)
            .height(340.dp),
        cornerRadius = 16.dp,
        contentPadding = 14.dp,
        fillMaxWidth = false,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Section: Logo + Ticker + Score Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    StockCompanyLogo(
                        symbol = stock.symbol,
                        initials = stock.initials,
                        logoUrl = stock.logoUrl,
                        size = 34.dp
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.symbol.replace(".NS", "").replace(".BO", ""),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            StockticsBadge(
                                text = stock.exchange,
                                variant = BadgeVariant.INFO
                            )
                        }
                        Text(
                            text = stock.name,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Score Badge (e.g. 95 SCORE)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GainGreenBg)
                        .border(1.dp, GainGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = GainGreen
                        )
                        Text(
                            text = "SCORE",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen
                        )
                    }
                }
            }

            // 2. Pattern Badge Pill
            val patternPillText = patternName?.uppercase() ?: when (category) {
                StockCategory.FIFTY_TWO_WEEK_LOW -> "NEAR 52-WEEK LOW"
                StockCategory.FIFTY_TWO_WEEK_HIGH -> "52-WEEK HIGH BREAKOUT"
                StockCategory.NEAR_SUPPORT -> "KEY SUPPORT RETEST"
                StockCategory.NEAR_RESISTANCE -> "PIVOT BREAKOUT"
                StockCategory.DEBT_FREE -> "ZERO NET DEBT"
                StockCategory.GOOD_GROWTH -> "HIGH GROWTH TITAN"
                null -> "CONFIRMED PATTERN"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, FintechBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = patternPillText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. Mini Candlestick Preview with Pivot Level Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF070A0F))
                        )
                    )
                    .border(
                        1.dp,
                        if (isDark) FintechBorder else Color(0x33475569),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                MiniCandlePreviewCanvas(isBullish = stock.isPositive)
            }

            // 4. Pivot Level Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PIVOT LEVEL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextTertiary
                )
                Text(
                    text = formattedPivot,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }

            // 5. Checklist Reasons (3 Explainable Bullets)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val reasons = if (!customReasons.isNullOrEmpty()) {
                    customReasons
                } else if (category != null) {
                    getReasonsForCategory(category, stock)
                } else {
                    listOf(
                        "Institutional volume expansion confirmed",
                        "Key moving average confluence support",
                        "High reward-to-risk ratio setup"
                    )
                }
                reasons.take(3).forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Check",
                            tint = GainGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = r,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 6. Footer Link: Inspect Chart ↗
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Inspect",
                        tint = FintechPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Inspect Chart",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FintechPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.NorthEast,
                    contentDescription = "Open",
                    tint = FintechPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Card #7: Trailing View All Card
 */
@Composable
fun ViewAllSetupsCard(
    categoryTitle: String,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StockticsCard(
        modifier = modifier
            .width(140.dp)
            .height(340.dp),
        cornerRadius = 16.dp,
        contentPadding = 14.dp,
        fillMaxWidth = false,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(FintechPrimary.copy(alpha = 0.12f))
                    .border(1.dp, FintechPrimary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Layers,
                    contentDescription = "View All",
                    tint = FintechPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "View All ($totalCount)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Historical Evidence & Grid",
                fontSize = 10.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Forward",
                tint = FintechPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Custom Canvas Drawing for Pattern Theory Diagrams
 */
@Composable
fun PatternSchematicCanvas(diagramType: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val gridColor = Color(0xFF1E293B)
        drawLine(gridColor, Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
        drawLine(gridColor, Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
        drawLine(gridColor, Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), strokeWidth = 1f)
        drawLine(gridColor, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 1f)
        drawLine(gridColor, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 1f)

        when (diagramType) {
            "52W_LOW" -> {
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(10f, h * 0.8f),
                    end = Offset(w - 10f, h * 0.8f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                val path = Path().apply {
                    moveTo(15f, h * 0.25f)
                    lineTo(w * 0.35f, h * 0.75f)
                    lineTo(w * 0.55f, h * 0.5f)
                    lineTo(w * 0.75f, h * 0.78f)
                    lineTo(w - 15f, h * 0.2f)
                }
                drawPath(path, color = GainGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            "52W_HIGH" -> {
                drawLine(
                    color = Color(0xFF1455FE),
                    start = Offset(10f, h * 0.35f),
                    end = Offset(w - 10f, h * 0.35f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                val path = Path().apply {
                    moveTo(15f, h * 0.85f)
                    lineTo(w * 0.35f, h * 0.55f)
                    lineTo(w * 0.5f, h * 0.65f)
                    lineTo(w * 0.75f, h * 0.35f)
                    lineTo(w - 15f, h * 0.12f)
                }
                drawPath(path, color = GainGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            "SUPPORT" -> {
                drawLine(
                    color = GainGreen,
                    start = Offset(10f, h * 0.75f),
                    end = Offset(w - 10f, h * 0.75f),
                    strokeWidth = 3f
                )
                val path = Path().apply {
                    moveTo(15f, h * 0.25f)
                    lineTo(w * 0.3f, h * 0.73f)
                    lineTo(w * 0.45f, h * 0.45f)
                    lineTo(w * 0.65f, h * 0.73f)
                    lineTo(w * 0.8f, h * 0.5f)
                    lineTo(w - 15f, h * 0.15f)
                }
                drawPath(path, color = Color(0xFF38BDF8), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            }
            "BREAKOUT" -> {
                drawLine(
                    color = LossRed,
                    start = Offset(10f, h * 0.45f),
                    end = Offset(w - 10f, h * 0.45f),
                    strokeWidth = 2.5f
                )
                val path = Path().apply {
                    moveTo(15f, h * 0.75f)
                    lineTo(w * 0.3f, h * 0.48f)
                    lineTo(w * 0.45f, h * 0.6f)
                    lineTo(w * 0.6f, h * 0.46f)
                    lineTo(w * 0.75f, h * 0.25f)
                    lineTo(w - 15f, h * 0.1f)
                }
                drawPath(path, color = GainGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            "BEARISH_ENGULFING" -> {
                // Bull candle (small green)
                val bullX = w * 0.35f
                drawLine(color = GainGreen, start = Offset(bullX, h * 0.35f), end = Offset(bullX, h * 0.75f), strokeWidth = 1.5f)
                drawRoundRect(color = GainGreen, topLeft = Offset(bullX - 5.dp.toPx(), h * 0.42f), size = Size(10.dp.toPx(), h * 0.25f), cornerRadius = CornerRadius(2f, 2f))

                // Engulfing bear candle (large red covering the bull)
                val bearX = w * 0.55f
                drawLine(color = LossRed, start = Offset(bearX, h * 0.2f), end = Offset(bearX, h * 0.85f), strokeWidth = 1.5f)
                drawRoundRect(color = LossRed, topLeft = Offset(bearX - 7.dp.toPx(), h * 0.28f), size = Size(14.dp.toPx(), h * 0.52f), cornerRadius = CornerRadius(2f, 2f))

                // Downward arrow
                val arrowPath = Path().apply {
                    moveTo(w * 0.72f, h * 0.45f)
                    lineTo(w * 0.88f, h * 0.75f)
                    lineTo(w * 0.75f, h * 0.75f)
                    lineTo(w * 0.88f, h * 0.75f)
                    lineTo(w * 0.88f, h * 0.62f)
                }
                drawPath(arrowPath, color = LossRed, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
            "CUP_HANDLE" -> {
                // U shaped cup
                val cupPath = Path().apply {
                    moveTo(15f, h * 0.3f)
                    cubicTo(w * 0.15f, h * 0.85f, w * 0.55f, h * 0.85f, w * 0.65f, h * 0.32f)
                    // handle pullback
                    cubicTo(w * 0.72f, h * 0.45f, w * 0.78f, h * 0.48f, w * 0.82f, h * 0.38f)
                    // breakout
                    lineTo(w - 15f, h * 0.15f)
                }
                drawPath(cupPath, color = GainGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            "DOUBLE_BOTTOM" -> {
                drawLine(color = Color(0xFF64748B), start = Offset(10f, h * 0.8f), end = Offset(w - 10f, h * 0.8f), strokeWidth = 1.5f)
                val wPath = Path().apply {
                    moveTo(15f, h * 0.25f)
                    lineTo(w * 0.3f, h * 0.78f)
                    lineTo(w * 0.5f, h * 0.45f)
                    lineTo(w * 0.7f, h * 0.78f)
                    lineTo(w - 15f, h * 0.2f)
                }
                drawPath(wPath, color = GainGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            "DEBT_FREE" -> {
                drawRoundRect(
                    color = GainGreen,
                    topLeft = Offset(w * 0.25f, h * 0.25f),
                    size = Size(w * 0.2f, h * 0.65f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = LossRed,
                    topLeft = Offset(w * 0.55f, h * 0.75f),
                    size = Size(w * 0.2f, h * 0.15f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            else -> {
                val path = Path().apply {
                    moveTo(15f, h * 0.85f)
                    cubicTo(w * 0.4f, h * 0.8f, w * 0.65f, h * 0.5f, w - 15f, h * 0.15f)
                }
                drawPath(path, color = GainGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

/**
 * Mini Candlestick Preview for Stock Setup Cards
 */
@Composable
fun MiniCandlePreviewCanvas(isBullish: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val candleCount = 8
        val spacing = w / (candleCount + 1)
        val candleWidth = 6.dp.toPx()

        for (i in 0 until candleCount) {
            val cx = spacing * (i + 1)
            val isGreen = if (isBullish) (i >= 5 || i % 2 == 0) else (i < 4 || i % 3 == 0)
            val candleColor = if (isGreen) GainGreen else LossRed

            val baseY = if (isBullish) {
                h * 0.7f - (i * (h * 0.05f))
            } else {
                h * 0.3f + (i * (h * 0.05f))
            }
            val bodyHeight = (h * 0.25f) + ((i % 3) * 4f)

            drawLine(
                color = candleColor,
                start = Offset(cx, baseY - 8f),
                end = Offset(cx, baseY + bodyHeight + 8f),
                strokeWidth = 1.5f
            )
            drawRoundRect(
                color = candleColor,
                topLeft = Offset(cx - candleWidth / 2, baseY),
                size = Size(candleWidth, bodyHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        val pivotY = if (isBullish) h * 0.45f else h * 0.65f
        drawLine(
            color = Color(0xFF64748B),
            start = Offset(0f, pivotY),
            end = Offset(w, pivotY),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    }
}

fun getPatternTheoryForCategory(cat: StockCategory): PatternTheoryMeta {
    return when (cat) {
        StockCategory.FIFTY_TWO_WEEK_LOW -> PatternTheoryMeta(
            code = "01",
            typeSubtitle = "Value Accumulation Setup",
            patternTitle = "52-Week Low Accumulation",
            institutionalRule = "Price trades within 0-6% of 52-week trough with institutional accumulation volume divergence.",
            diagramType = "52W_LOW",
            patternId = "52W_LOW",
            imageResId = com.stocktics.R.drawable.setup_3d_52w_low
        )
        StockCategory.FIFTY_TWO_WEEK_HIGH -> PatternTheoryMeta(
            code = "02",
            typeSubtitle = "Momentum Continuation",
            patternTitle = "52-Week High Breakout",
            institutionalRule = "Leader within 5% of 52-week highs with volume expansion and blue-sky overhead resistance.",
            diagramType = "52W_HIGH",
            patternId = "52W_HIGH",
            imageResId = com.stocktics.R.drawable.setup_3d_52w_high
        )
        StockCategory.NEAR_SUPPORT -> PatternTheoryMeta(
            code = "03",
            typeSubtitle = "Divergence Accumulation",
            patternTitle = "Key Support Retest",
            institutionalRule = "Tested major horizontal floor or 200 DMA with volume contraction and tight risk invalidation.",
            diagramType = "SUPPORT",
            patternId = "SUPPORT",
            imageResId = com.stocktics.R.drawable.setup_3d_support
        )
        StockCategory.NEAR_RESISTANCE -> PatternTheoryMeta(
            code = "04",
            typeSubtitle = "Price Action & Volume",
            patternTitle = "Pivot & Resistance Breakout",
            institutionalRule = "Compression beneath multi-week overhead barrier with drying supply before explosive thrust.",
            diagramType = "BREAKOUT",
            patternId = "BREAKOUT",
            imageResId = com.stocktics.R.drawable.setup_3d_breakout
        )
        StockCategory.DEBT_FREE -> PatternTheoryMeta(
            code = "05",
            typeSubtitle = "Solvency Fortress Filter",
            patternTitle = "Zero Net Debt Balance",
            institutionalRule = "Cash balances exceed total debt obligations with fortress solvency and high return on capital.",
            diagramType = "DEBT_FREE",
            patternId = "DEBT_FREE",
            imageResId = com.stocktics.R.drawable.setup_3d_debt_free
        )
        StockCategory.GOOD_GROWTH -> PatternTheoryMeta(
            code = "06",
            typeSubtitle = "Quant Growth Engine",
            patternTitle = "High Growth Compounders",
            institutionalRule = "Consistent double-digit revenue expansion, accelerating EPS, and top-tier quant growth ranking.",
            diagramType = "GROWTH",
            patternId = "GOOD_GROWTH",
            imageResId = com.stocktics.R.drawable.setup_3d_growth
        )
    }
}

fun getReasonsForCategory(cat: StockCategory, stock: CategoryStockItem): List<String> {
    return when (cat) {
        StockCategory.FIFTY_TWO_WEEK_LOW -> listOf(
            "Price within ${stock.metricValue} of 52-week trough",
            "Seller exhaustion with accumulation volume",
            "Asymmetric upside risk-reward entry zone"
        )
        StockCategory.FIFTY_TWO_WEEK_HIGH -> listOf(
            "Trading ${stock.metricValue} near 52-week high",
            "Volume expansion indicates institutional buying",
            "Blue-sky momentum with low overhead resistance"
        )
        StockCategory.NEAR_SUPPORT -> listOf(
            "Tested major technical support boundary",
            "Low selling volume with buyer wick rejection",
            "Tight invalidation stop-loss with 3:1 reward"
        )
        StockCategory.NEAR_RESISTANCE -> listOf(
            "Pressing against multi-week ceiling resistance",
            "Tight price coil with drying volume",
            "Explosive momentum breakout watch active"
        )
        StockCategory.DEBT_FREE -> listOf(
            "Zero net debt with pristine solvency balance",
            "High ROCE and insulated from rate hikes",
            "Robust free cash flow yield and dividend safety"
        )
        StockCategory.GOOD_GROWTH -> listOf(
            "Accelerating double-digit quarterly revenue",
            "Expanding operating margins over 4 quarters",
            "Top quintile quantitative composite score"
        )
    }
}

fun getPatternTheoryForCode(patternCode: String, displayName: String? = null, category: String? = null): PatternTheoryMeta {
    val p = patternCode.trim().uppercase()
    return when {
        p == "PIVOT_BREAKOUT" || p == "BREAKOUT" -> PatternTheoryMeta(
            code = "02",
            typeSubtitle = "Price Action & Volume Expansion",
            patternTitle = displayName ?: "Pivot Breakout",
            institutionalRule = "Price decisively pierces multi-week overhead pivot resistance accompanied by surge volume (>1.5x 50-day SMA).",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_breakout
        )
        p == "FAILED_BREAKOUT" -> PatternTheoryMeta(
            code = "02B",
            typeSubtitle = "Liquidity Grab & Reversal",
            patternTitle = displayName ?: "Failed Breakout (Trap)",
            institutionalRule = "Price initially pierces resistance to trigger breakout buy stops, but swiftly collapses back into the prior range.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_failed_breakout
        )
        p == "CUP_AND_HANDLE" || p == "CUP_WITH_HANDLE" || p == "CUP_HANDLE" -> PatternTheoryMeta(
            code = "03",
            typeSubtitle = "William O'Neil Institutional Base",
            patternTitle = displayName ?: "Cup with Handle",
            institutionalRule = "Rounded U-shaped accumulation base (12-33% depth) followed by a tight downward handle on dry volume.",
            diagramType = "CUP_HANDLE",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_cup_handle
        )
        p == "DOUBLE_BOTTOM" -> PatternTheoryMeta(
            code = "03B",
            typeSubtitle = "Bullish Reversal Structure",
            patternTitle = displayName ?: "Double Bottom (W-Base)",
            institutionalRule = "Classic 'W' shape where 2nd trough undercuts or matches 1st trough before neckline breakout.",
            diagramType = "DOUBLE_BOTTOM",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_52w_low
        )
        p == "DOUBLE_TOP" -> PatternTheoryMeta(
            code = "03C",
            typeSubtitle = "Bearish Reversal Structure",
            patternTitle = displayName ?: "Double Top Pattern",
            institutionalRule = "Classic 'M' formation where price twice tests overhead resistance and fails to make a higher high.",
            diagramType = "DOUBLE_TOP",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_double_top
        )
        p == "VCP" -> PatternTheoryMeta(
            code = "04",
            typeSubtitle = "Mark Minervini Base",
            patternTitle = displayName ?: "Volatility Contraction Pattern",
            institutionalRule = "Successive price swings contract from left to right with volume progressively drying up before breakout.",
            diagramType = "VCP",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_vcp
        )
        p == "FLAT_BASE" -> PatternTheoryMeta(
            code = "04B",
            typeSubtitle = "Mid-Trend Consolidation",
            patternTitle = displayName ?: "Flat Base Consolidation",
            institutionalRule = "Horizontal price digestion with maximum depth under 15% occurring after an initial 20%+ upward thrust.",
            diagramType = "FLAT_BASE",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_flat_base
        )
        p == "MULTI_YEAR_BASE" -> PatternTheoryMeta(
            code = "04C",
            typeSubtitle = "Macro Structural Breakout",
            patternTitle = displayName ?: "Multi-Year Structural Base",
            institutionalRule = "Extended multi-year accumulation (1 to 5+ years) breaking out into fresh all-time or decade highs.",
            diagramType = "MULTI_YEAR",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_multi_year
        )
        p == "IPO_BASE" -> PatternTheoryMeta(
            code = "04D",
            typeSubtitle = "Early Institutional Discovery",
            patternTitle = displayName ?: "IPO Debut Base",
            institutionalRule = "First consolidation base formed within 25 to 65 days of public listing before initial supply overhang clears.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_multi_year
        )
        p == "FAIR_VALUE_GAP" -> PatternTheoryMeta(
            code = "05",
            typeSubtitle = "ICT & Smart Money Concepts",
            patternTitle = displayName ?: "Fair Value Gap (FVG)",
            institutionalRule = "Three-candle imbalance leaving an unfilled liquidity void. Returns to mitigate the zone for low-risk entry.",
            diagramType = "FVG",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_fvg
        )
        p == "BULLISH_ENGULFING" -> PatternTheoryMeta(
            code = "06",
            typeSubtitle = "Candlestick Reversal Pattern",
            patternTitle = displayName ?: "Bullish Engulfing",
            institutionalRule = "Large green candle body completely engulfs previous red candle body following a downtrend.",
            diagramType = "BULLISH_ENGULFING",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_bullish_candle
        )
        p == "BEARISH_ENGULFING" -> PatternTheoryMeta(
            code = "06B",
            typeSubtitle = "Candlestick Distribution Pattern",
            patternTitle = displayName ?: "Bearish Engulfing",
            institutionalRule = "Large red candle body completely eclipses prior green candle body near key resistance.",
            diagramType = "BEARISH_ENGULFING",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_bearish_candle
        )
        p == "HAMMER" -> PatternTheoryMeta(
            code = "06C",
            typeSubtitle = "Intraday Rejection of Lows",
            patternTitle = displayName ?: "Hammer Candlestick",
            institutionalRule = "Small real body near top of range with long lower shadow at least twice the length of the body.",
            diagramType = "HAMMER",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_bullish_candle
        )
        p == "SHOOTING_STAR" -> PatternTheoryMeta(
            code = "06D",
            typeSubtitle = "Overhead Supply Rejection",
            patternTitle = displayName ?: "Shooting Star Reversal",
            institutionalRule = "Small body near lower range with long upper shadow at least 2x body height at peak resistance.",
            diagramType = "SHOOTING_STAR",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_bearish_candle
        )
        p == "MORNING_STAR" -> PatternTheoryMeta(
            code = "06E",
            typeSubtitle = "3-Candle Bottom Reversal",
            patternTitle = displayName ?: "Morning Star Reversal",
            institutionalRule = "Tall bearish candle, followed by indecision star/doji, followed by strong bullish candle.",
            diagramType = "MORNING_STAR",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_morning_star
        )
        p == "EVENING_STAR" -> PatternTheoryMeta(
            code = "06F",
            typeSubtitle = "3-Candle Top Reversal",
            patternTitle = displayName ?: "Evening Star Reversal",
            institutionalRule = "Tall bullish candle, followed by indecision star, followed by dark candle closing deep in candle 1.",
            diagramType = "EVENING_STAR",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_evening_star
        )
        p == "DOJI" -> PatternTheoryMeta(
            code = "06G",
            typeSubtitle = "Equilibrium & Inflection",
            patternTitle = displayName ?: "Indecision Doji at Level",
            institutionalRule = "Open and close prices are virtually identical with upper and lower shadows, marking equilibrium.",
            diagramType = "HAMMER",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_morning_star
        )
        p == "SUPPORT_RESISTANCE_TOUCH" || p == "SUPPORT" -> PatternTheoryMeta(
            code = "11",
            typeSubtitle = "Key Price Reaction Levels",
            patternTitle = displayName ?: "Support / Resistance Reaction",
            institutionalRule = "Horizontal price shelves where institutional supply or demand repeatedly reverses price trajectory.",
            diagramType = "SUPPORT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_support
        )
        p == "TRENDLINE_BREAK" -> PatternTheoryMeta(
            code = "13",
            typeSubtitle = "Dynamic Structural Shift",
            patternTitle = displayName ?: "Trendline Breakout",
            institutionalRule = "Decisive breach of descending resistance line connecting at least 3 distinct swing highs.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_trendline_break
        )
        p == "MA_CROSSOVER" -> PatternTheoryMeta(
            code = "16",
            typeSubtitle = "Trend-Following Signal",
            patternTitle = displayName ?: "Moving Average Golden Cross",
            institutionalRule = "Faster moving average crosses decisively above slower baseline (e.g. 50 SMA / 200 SMA).",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_ma_cross
        )
        p == "SUPERTREND_FLIP" -> PatternTheoryMeta(
            code = "17",
            typeSubtitle = "Volatility Trend Filter",
            patternTitle = displayName ?: "Supertrend Direction Flip",
            institutionalRule = "Price closes above ATR-based trailing stop band, flipping Supertrend from Bear to Bull.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_ma_cross
        )
        p == "RSI_EXTREME" -> PatternTheoryMeta(
            code = "18",
            typeSubtitle = "RSI Mean Reversion",
            patternTitle = displayName ?: "RSI Oversold / Extreme",
            institutionalRule = "14-period RSI enters extreme oversold zone (<30) signaling imminent inflection.",
            diagramType = "SUPPORT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_rsi_divergence
        )
        p == "STOCHASTIC_CROSS" -> PatternTheoryMeta(
            code = "19",
            typeSubtitle = "Cyclical Momentum Turn",
            patternTitle = displayName ?: "Stochastic Bullish Cross",
            institutionalRule = "%K line crosses above %D line from below 20 oversold threshold.",
            diagramType = "SUPPORT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_rsi_divergence
        )
        p == "MOMENTUM_ROC" -> PatternTheoryMeta(
            code = "15",
            typeSubtitle = "Velocity Indicator",
            patternTitle = displayName ?: "Momentum Rate of Change",
            institutionalRule = "Surge in Rate of Change indicates acceleration of price velocity compared to baseline.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_rsi_divergence
        )
        p == "DIVERGENCE" -> PatternTheoryMeta(
            code = "20",
            typeSubtitle = "Oscillator Structural Divergence",
            patternTitle = displayName ?: "RSI Momentum Divergence",
            institutionalRule = "Price prints a lower swing low while RSI forms a higher swing low, signaling weakening sell pressure.",
            diagramType = "SUPPORT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_rsi_divergence
        )
        p == "RELATIVE_VOLUME_SPIKE" -> PatternTheoryMeta(
            code = "21",
            typeSubtitle = "Institutional Footprint",
            patternTitle = displayName ?: "Relative Volume Spike (RVOL)",
            institutionalRule = "Daily trading volume exceeds 2.5x 50-day average, signaling institutional capital deployment.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_volume_spike
        )
        p == "VOLUME_PROFILE_POC_REACTION" -> PatternTheoryMeta(
            code = "23",
            typeSubtitle = "Point of Control Value Area",
            patternTitle = displayName ?: "Volume Profile POC Reaction",
            institutionalRule = "Price interacts with Point of Control—the single price level with greatest historical volume.",
            diagramType = "SUPPORT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_volume_spike
        )
        p == "SUPPLY_DEMAND_ZONE" -> PatternTheoryMeta(
            code = "24",
            typeSubtitle = "Order Block Imbalances",
            patternTitle = displayName ?: "Demand Zone Retest",
            institutionalRule = "Identifies institutional order blocks where aggressive buying originated.",
            diagramType = "SUPPORT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_support
        )
        p == "MARKET_STRUCTURE_BOS" -> PatternTheoryMeta(
            code = "26",
            typeSubtitle = "Trend Continuation Signal",
            patternTitle = displayName ?: "Break of Structure (BOS)",
            institutionalRule = "Price closes beyond the most recent higher swing high in an uptrend, confirming continuation.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_market_structure
        )
        p == "MARKET_STRUCTURE_CHOCH" -> PatternTheoryMeta(
            code = "27",
            typeSubtitle = "Trend Reversal Signal",
            patternTitle = displayName ?: "Change of Character (CHoCH)",
            institutionalRule = "Price violates key structural swing low, signaling institutional regime shift.",
            diagramType = "DOUBLE_BOTTOM",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_market_structure
        )
        p == "HARMONIC_PATTERN" -> PatternTheoryMeta(
            code = "10",
            typeSubtitle = "Fibonacci Geometry",
            patternTitle = displayName ?: "Harmonic Pattern Reversal",
            institutionalRule = "Geometric XABCD price structure adhering to exact Fibonacci retracement ratios.",
            diagramType = "DOUBLE_BOTTOM",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_harmonic
        )
        p == "HEIKIN_ASHI_TREND" -> PatternTheoryMeta(
            code = "07",
            typeSubtitle = "Noise Reduction Smoothing",
            patternTitle = displayName ?: "Heikin-Ashi Trend",
            institutionalRule = "Consecutive flat-bottom green Heikin Ashi candles indicate clean directional bull momentum.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_trend_impulse
        )
        p == "MOON_PHASE_CORRELATION" -> PatternTheoryMeta(
            code = "08",
            typeSubtitle = "Astronomical Market Rhythms",
            patternTitle = displayName ?: "Moon Phase Lunar Cycle",
            institutionalRule = "New Moon and Full Moon cycle correlation across equity market swings.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_trend_impulse
        )
        p == "RENKO_TREND" -> PatternTheoryMeta(
            code = "28",
            typeSubtitle = "Box Size Filtered Trend",
            patternTitle = displayName ?: "Renko ATR Brick Trend",
            institutionalRule = "Fixed-size ATR bricks eliminate noise and highlight clear directional trend impulse.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_trend_impulse
        )
        p == "FIBONACCI_RETRACEMENT_REACTION" -> PatternTheoryMeta(
            code = "29",
            typeSubtitle = "Golden Pocket 0.618",
            patternTitle = displayName ?: "Fibonacci Retracement",
            institutionalRule = "Price reacts at golden ratio inflection points (61.8% / 78.6%) of previous impulse swing.",
            diagramType = "SUPPORT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_fibonacci
        )
        p == "FIBONACCI_EXTENSION_TARGET" -> PatternTheoryMeta(
            code = "30",
            typeSubtitle = "Expansion Target Projection",
            patternTitle = displayName ?: "Fibonacci Extension Target",
            institutionalRule = "Measured move expansion targeting 1.272 and 1.618 impulse Fibonacci levels.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_fibonacci
        )
        p == "GANN_ANGLE_TOUCH" -> PatternTheoryMeta(
            code = "31",
            typeSubtitle = "Geometric Angle Matrix",
            patternTitle = displayName ?: "Gann Angle Reaction",
            institutionalRule = "Price interacts with classical Gann 1x1 or 2x1 geometric time-and-price angles.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_fibonacci
        )
        p == "52W_LOW" || p == "FIFTY_TWO_WEEK_LOW" -> PatternTheoryMeta(
            code = "01",
            typeSubtitle = "Value Accumulation Setup",
            patternTitle = displayName ?: "52-Week Low Accumulation",
            institutionalRule = "Price trades within 0-6% of 52-week trough with institutional accumulation volume divergence.",
            diagramType = "52W_LOW",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_52w_low
        )
        p == "52W_HIGH" || p == "FIFTY_TWO_WEEK_HIGH" -> PatternTheoryMeta(
            code = "02",
            typeSubtitle = "Momentum Continuation",
            patternTitle = displayName ?: "52-Week High Breakout",
            institutionalRule = "Leader within 5% of 52-week highs with volume expansion and blue-sky overhead resistance.",
            diagramType = "52W_HIGH",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_52w_high
        )
        else -> PatternTheoryMeta(
            code = "01",
            typeSubtitle = "${category ?: "Technical"} Setup",
            patternTitle = displayName ?: p.replace("_", " "),
            institutionalRule = "Institutional price formation audited with historical backtest evidence and algorithmic rule filters.",
            diagramType = "BREAKOUT",
            patternId = p,
            imageResId = com.stocktics.R.drawable.setup_3d_breakout
        )
    }
}
