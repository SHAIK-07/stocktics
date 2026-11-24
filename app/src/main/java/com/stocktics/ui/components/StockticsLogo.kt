package com.stocktics.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.R
import com.stocktics.ui.theme.BorderGlow
import com.stocktics.ui.theme.BrandPrimary
import com.stocktics.ui.theme.BrandSecondary
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary

@Composable
fun StockticsLogoIcon(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = size * 0.26f
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black,
                spotColor = Color(0xFF00D09C).copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                color = Color(0xFF00D09C).copy(alpha = 0.35f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_stocktics_logo),
            contentDescription = "Stocktics Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun StockticsBrandingHeader(
    iconSize: Dp = 80.dp,
    showSubtitle: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StockticsLogoIcon(size = iconSize)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "STOCKTICS",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            color = TextPrimary
        )

        if (showSubtitle) {
            Text(
                text = "Quantitative Stock Research & AI Forecasting",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun getLocalLogoRes(symbol: String): Int? {
    val clean = symbol.substringBefore(".").trim().uppercase()
    return when (clean) {
        "RELIANCE" -> R.drawable.logo_reliance
        "TCS" -> R.drawable.logo_tcs
        "INFY" -> R.drawable.logo_infy
        "HDFCBANK" -> R.drawable.logo_hdfcbank
        "ICICIBANK" -> R.drawable.logo_icicibank
        "BHARTIARTL" -> R.drawable.logo_bhartiartl
        "SBIN" -> R.drawable.logo_sbin
        "ITC" -> R.drawable.logo_itc
        "LT" -> R.drawable.logo_lt
        "SUNPHARMA" -> R.drawable.logo_sunpharma
        "TITAN" -> R.drawable.logo_titan
        "BAJFINANCE" -> R.drawable.logo_bajfinance
        "KOTAKBANK" -> R.drawable.logo_kotakbank
        "ZAGGLE" -> R.drawable.logo_zaggle
        "TATAMOTORS" -> R.drawable.logo_tatamotors
        "WIPRO" -> R.drawable.logo_wipro
        "AAPL" -> R.drawable.logo_aapl
        "NVDA" -> R.drawable.logo_nvda
        "MSFT" -> R.drawable.logo_msft
        else -> null
    }
}

@Composable
fun StockCompanyLogo(
    symbol: String,
    initials: String = symbol.take(2),
    logoUrl: String = "",
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imageLoader = remember(context) {
        coil.ImageLoader.Builder(context)
            .components {
                add(coil.decode.SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }

    val cleanSym = symbol.substringBefore(".").trim().uppercase()
    val localRes = getLocalLogoRes(symbol)

    // Exact multi-tier logo resolution matching stock-tracker-two-weld.vercel.app
    val effectiveUrl = when {
        symbol.endsWith(".NS") || symbol.endsWith(".BO") -> "https://assets.parqet.com/logos/symbol/$symbol"
        logoUrl.isNotBlank() && !logoUrl.contains("clearbit.com") -> logoUrl
        else -> "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=$cleanSym"
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White) // High-contrast clean white background like stock-tracker
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (localRes != null) {
            Image(
                painter = painterResource(id = localRes),
                contentDescription = "$symbol Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(size - 8.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            coil.compose.SubcomposeAsyncImage(
                model = coil.request.ImageRequest.Builder(context)
                    .data(effectiveUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = initials,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(size - 8.dp)
                    .clip(RoundedCornerShape(6.dp)),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = (size.value * 0.35f).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = (size.value * 0.35f).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            )
        }
    }
}
