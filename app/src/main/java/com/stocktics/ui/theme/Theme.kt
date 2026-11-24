package com.stocktics.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dynamic Semantic Color Tokens for Stocktics (Dark + Light Apple Liquid Glass)
data class FintechColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val borderFocused: Color,
    val primary: Color,
    val primaryDark: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val gainGreen: Color,
    val gainGreenBg: Color,
    val lossRed: Color,
    val lossRedBg: Color,
    val warningAmber: Color,
    val warningAmberBg: Color,
    val pillBackground: Color,
    val pillBorder: Color,
    val glassSurfaceGradient: List<Color>,
    val glassBorderGradient: List<Color>,
    val glassLipColor: Color
)

val DarkFintechColors = FintechColors(
    isDark = true,
    background = Color(0xFF070A0F),
    surface = Color(0xFF131722),
    surfaceElevated = Color(0xFF1A2030),
    border = Color(0xFF1E2638),
    borderFocused = Color(0xFF1455FE),
    primary = Color(0xFF1455FE), // Royal Tech Blue (INDmoney Standard)
    primaryDark = Color(0xFF0C46E0),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textTertiary = Color(0xFF64748B),
    gainGreen = Color(0xFF00D09C),
    gainGreenBg = Color(0x1A00D09C),
    lossRed = Color(0xFFF04438),
    lossRedBg = Color(0x1AF04438),
    warningAmber = Color(0xFFF59E0B),
    warningAmberBg = Color(0x1AF59E0B),
    pillBackground = Color(0xF7161C28),
    pillBorder = Color(0x55FFFFFF),
    glassSurfaceGradient = listOf(Color(0xF5161C28), Color(0xEB0F141E)),
    glassBorderGradient = listOf(Color(0x66FFFFFF), Color(0x1F94A3B8), Color(0x0A222A3B)),
    glassLipColor = Color(0x33FFFFFF)
)

val LightFintechColors = FintechColors(
    isDark = false,
    background = Color(0xFFF1F5F9),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF8FAFC),
    border = Color(0xFFE2E8F0),
    borderFocused = Color(0xFF1455FE),
    primary = Color(0xFF1455FE),
    primaryDark = Color(0xFF0C46E0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textTertiary = Color(0xFF94A3B8),
    gainGreen = Color(0xFF00D09C),
    gainGreenBg = Color(0x1A00D09C),
    lossRed = Color(0xFFF04438),
    lossRedBg = Color(0x1AF04438),
    warningAmber = Color(0xFFF59E0B),
    warningAmberBg = Color(0x1AF59E0B),
    pillBackground = Color(0xFFFFFFFF),
    pillBorder = Color(0xFFE2E8F0),
    glassSurfaceGradient = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC)),
    glassBorderGradient = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFFE2E8F0)),
    glassLipColor = Color(0x50E2E8F0)
)

val LocalFintechColors = staticCompositionLocalOf { DarkFintechColors }

private val StockticsDarkColorScheme = darkColorScheme(
    primary = Color(0xFF1455FE),
    onPrimary = Color.Black,
    secondary = Color(0xFF1455FE),
    onSecondary = Color.White,
    background = Color(0xFF0B0E14),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF141923),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1C2333),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF222A3B),
    error = Color(0xFFF04438),
    onError = Color.White
)

private val StockticsLightColorScheme = lightColorScheme(
    primary = Color(0xFF1455FE),
    onPrimary = Color.White,
    secondary = Color(0xFF1455FE),
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    error = Color(0xFFF04438),
    onError = Color.White
)

@Composable
fun StockticsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StockticsDarkColorScheme else StockticsLightColorScheme
    val fintechColors = if (darkTheme) DarkFintechColors else LightFintechColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalFintechColors provides fintechColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}