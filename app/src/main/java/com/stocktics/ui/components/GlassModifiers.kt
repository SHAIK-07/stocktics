package com.stocktics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stocktics.ui.theme.LocalFintechColors

/**
 * Modifier extension to apply Apple Liquid Frosted Glass styling to any Compose layout.
 */
@Composable
fun Modifier.liquidGlass(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 4.dp
): Modifier {
    val fintech = LocalFintechColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val shadowSpotColor = if (fintech.isDark) {
        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.35f)
    } else {
        androidx.compose.ui.graphics.Color(0x140F172A)
    }
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = if (fintech.isDark) 0.25f else 0.05f),
            spotColor = shadowSpotColor
        )
        .clip(shape)
        .background(Brush.verticalGradient(fintech.glassSurfaceGradient))
        .border(width = 1.dp, brush = Brush.verticalGradient(fintech.glassBorderGradient), shape = shape)
}

/**
 * Elevated frosted glass modifier for inputs, inner pills, and nested controls.
 */
@Composable
fun Modifier.liquidGlassElevated(
    cornerRadius: Dp = 14.dp,
    elevation: Dp = 2.dp
): Modifier {
    val fintech = LocalFintechColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val elevatedGradient = if (fintech.isDark) {
        listOf(androidx.compose.ui.graphics.Color(0x4D2A3448), androidx.compose.ui.graphics.Color(0x331C2333))
    } else {
        listOf(androidx.compose.ui.graphics.Color(0xF5FFFFFF), androidx.compose.ui.graphics.Color(0xE6F1F5F9))
    }
    val shadowSpotColor = if (fintech.isDark) {
        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.25f)
    } else {
        androidx.compose.ui.graphics.Color(0x100F172A)
    }
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = if (fintech.isDark) 0.15f else 0.04f),
            spotColor = shadowSpotColor
        )
        .clip(shape)
        .background(Brush.verticalGradient(elevatedGradient))
        .border(width = 1.dp, brush = Brush.verticalGradient(fintech.glassBorderGradient), shape = shape)
}

/**
 * Solid frosted glass modifier with high-density opacity (96-98%) and ambient shadow,
 * preventing any content beneath from bleeding through while preserving the
 * Apple specular hairline edge and refraction horizon. Ideal for navigation bars,
 * floating toolbars, and sticky controls.
 */
@Composable
fun Modifier.solidLiquidGlass(
    cornerRadius: Dp = 28.dp,
    elevation: Dp = 10.dp
): Modifier {
    val fintech = LocalFintechColors.current
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceColors = if (fintech.isDark) {
        listOf(
            androidx.compose.ui.graphics.Color(0xF7161C28),
            androidx.compose.ui.graphics.Color(0xF50F141E)
        )
    } else {
        listOf(
            androidx.compose.ui.graphics.Color(0xFAFFFFFF),
            androidx.compose.ui.graphics.Color(0xF5F1F5F9)
        )
    }

    val borderColors = if (fintech.isDark) {
        listOf(
            androidx.compose.ui.graphics.Color(0x66FFFFFF),
            androidx.compose.ui.graphics.Color(0x1F94A3B8),
            androidx.compose.ui.graphics.Color(0x0A222A3B)
        )
    } else {
        listOf(
            androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            androidx.compose.ui.graphics.Color(0x60CBD5E1),
            androidx.compose.ui.graphics.Color(0x3094A3B8)
        )
    }

    val shadowSpotColor = if (fintech.isDark) {
        androidx.compose.ui.graphics.Color(0x99000000)
    } else {
        androidx.compose.ui.graphics.Color(0x2E0F172A)
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = shadowSpotColor,
            spotColor = shadowSpotColor
        )
        .clip(shape)
        .background(Brush.verticalGradient(surfaceColors))
        .border(width = 1.dp, brush = Brush.verticalGradient(borderColors), shape = shape)
}
