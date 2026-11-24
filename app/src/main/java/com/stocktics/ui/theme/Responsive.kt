package com.stocktics.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Responsive Device Category Standard for Stocktics:
 * - SMALL  (~320–360dp): Compact spacing, smaller cards, scaled down typography
 * - NORMAL (~360–420dp): Standard balanced layout (Nothing Phone 3, Pixel)
 * - LARGE  (~420–600dp): Expanded content, 3-column stats
 * - TABLET (600dp+)    : Multi-column wide layout, 4-column stats
 */
enum class ScreenDeviceClass {
    SMALL,
    NORMAL,
    LARGE,
    TABLET
}

data class ScreenDimensions(
    val deviceClass: ScreenDeviceClass,
    val widthDp: Dp,
    val heightDp: Dp,
    val horizontalPadding: Dp,
    val cardPadding: Dp,
    val cardSpacing: Dp,
    val statColumns: Int,
    val isSmall: Boolean,
    val isTablet: Boolean,
    val fontScale: Float = 1.0f
) {
    /**
     * Proportional responsive text size helper:
     * Automatically scales text down slightly on compact/narrow screens (e.g. Nothing Phone)
     * and balances readability on larger screens (Pixel 9 Pro).
     */
    fun respSp(baseSp: Float): TextUnit = (baseSp * fontScale).sp
    fun respSp(baseSp: Int): TextUnit = (baseSp.toFloat() * fontScale).sp

    val titleSp: TextUnit get() = respSp(24f)
    val h1Sp: TextUnit get() = respSp(20f)
    val h2Sp: TextUnit get() = respSp(16f)
    val sectionHeaderSp: TextUnit get() = respSp(15f)
    val bodySp: TextUnit get() = respSp(12.5f)
    val captionSp: TextUnit get() = respSp(10.5f)
    val badgeSp: TextUnit get() = respSp(10f)
}

@Composable
fun rememberScreenDimensions(): ScreenDimensions {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val width = config.screenWidthDp.dp
    val height = config.screenHeightDp.dp
    val sysFontScale = density.fontScale

    val deviceClass = when {
        width < 360.dp -> ScreenDeviceClass.SMALL
        width < 420.dp -> ScreenDeviceClass.NORMAL
        width < 600.dp -> ScreenDeviceClass.LARGE
        else -> ScreenDeviceClass.TABLET
    }

    // Adaptive font scaling factor based on physical screen width
    // Ensures narrow or dense screens (Nothing Phone 3) don't experience crowded text or line wraps
    val widthFontFactor = when {
        width < 360.dp -> 0.88f
        width < 400.dp -> 0.94f
        width < 450.dp -> 1.00f
        else -> 1.05f
    }

    // Defensive clamping against extreme OS font scales (e.g. 1.3x - 1.5x)
    val finalFontScale = if (sysFontScale > 1.15f) {
        widthFontFactor * (1.15f / sysFontScale)
    } else {
        widthFontFactor
    }

    return ScreenDimensions(
        deviceClass = deviceClass,
        widthDp = width,
        heightDp = height,
        horizontalPadding = when (deviceClass) {
            ScreenDeviceClass.SMALL -> 12.dp
            ScreenDeviceClass.NORMAL -> 16.dp
            ScreenDeviceClass.LARGE -> 20.dp
            ScreenDeviceClass.TABLET -> 28.dp
        },
        cardPadding = when (deviceClass) {
            ScreenDeviceClass.SMALL -> 12.dp
            ScreenDeviceClass.NORMAL -> 14.dp
            ScreenDeviceClass.LARGE -> 16.dp
            ScreenDeviceClass.TABLET -> 20.dp
        },
        cardSpacing = when (deviceClass) {
            ScreenDeviceClass.SMALL -> 10.dp
            ScreenDeviceClass.NORMAL -> 14.dp
            ScreenDeviceClass.LARGE -> 16.dp
            ScreenDeviceClass.TABLET -> 20.dp
        },
        statColumns = when (deviceClass) {
            ScreenDeviceClass.SMALL -> 2
            ScreenDeviceClass.NORMAL -> 2
            ScreenDeviceClass.LARGE -> if (width < 540.dp) 2 else 3
            ScreenDeviceClass.TABLET -> 4
        },
        isSmall = deviceClass == ScreenDeviceClass.SMALL,
        isTablet = deviceClass == ScreenDeviceClass.TABLET,
        fontScale = finalFontScale
    )
}
