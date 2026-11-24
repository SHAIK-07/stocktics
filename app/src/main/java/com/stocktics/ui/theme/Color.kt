package com.stocktics.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ==============================================================================
// Stocktics Institutional Fintech Color Palette (INDmoney & Groww Standard)
// Dynamic Semantic Tokens reacting automatically to Dark / White (Light) mode
// ==============================================================================

// Dynamic Semantic Surfaces
val FintechBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.background

val FintechSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.surface

val FintechSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.surfaceElevated

val FintechBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.border

val IsDarkTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.isDark

val GlassSurfaceGradient: List<Color>
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.glassSurfaceGradient

val GlassBorderGradient: List<Color>
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.glassBorderGradient

val GlassLipColor: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.glassLipColor

// Primary Brand Color: Royal Tech Blue (INDmoney Standard)
val FintechBlue = Color(0xFF1455FE)
val FintechBlueDark = Color(0xFF0C46E0)
val FintechBlueDim = Color(0x1F1455FE)

// Active Primary Accents
val FintechPrimary = FintechBlue
val FintechPrimaryDark = FintechBlueDark
val FintechBorderFocused = FintechBlue

// Financial Indicators (Crisp Translucent Pill Tints)
val GainGreen = Color(0xFF00D09C)
val GainGreenBg = Color(0x1A00D09C)
val LossRed = Color(0xFFF04438)
val LossRedBg = Color(0x1AF04438)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberBg = Color(0x1AF59E0B)

// Dynamic Typography Hierarchy
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.textSecondary

val TextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.textTertiary

// Floating Pill Navigation Bar
val PillBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.pillBackground

val PillBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFintechColors.current.pillBorder

val PillActiveIndicator = FintechBlueDim

// ==============================================================================
// Backward Compatibility Aliases
// ==============================================================================
val BackgroundDark: Color @Composable @ReadOnlyComposable get() = FintechBackground
val SurfaceDark: Color @Composable @ReadOnlyComposable get() = FintechSurface
val SurfaceCardDark: Color @Composable @ReadOnlyComposable get() = FintechSurface
val SurfaceCardElevated: Color @Composable @ReadOnlyComposable get() = FintechSurfaceElevated
val BorderSubtle: Color @Composable @ReadOnlyComposable get() = FintechBorder
val BorderGlow = FintechPrimary
val BrandPrimary = FintechPrimary
val BrandPrimaryLight = FintechPrimary
val BrandSecondary = FintechBlue
val NeonGreen = GainGreen
val NeonGreenDim = GainGreenBg
val NeonRed = LossRed
val NeonRedDim = LossRedBg
val NeonAmber = WarningAmber
val NeonAmberDim = WarningAmberBg
val TextMuted: Color @Composable @ReadOnlyComposable get() = TextTertiary