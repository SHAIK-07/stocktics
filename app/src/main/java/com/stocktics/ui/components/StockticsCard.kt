package com.stocktics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stocktics.ui.theme.LocalFintechColors

/**
 * Apple Vision / Liquid Frosted Glass Card Container.
 * Standardized for all cards across the Stocktics application.
 *
 * Core Visual Foundations:
 *   1. Translucent frosted glass body (deep smoky obsidian in dark mode, silky milky crystal in light mode).
 *   2. Specular reflection hairline border (luminous top edge catching directional light).
 *   3. Inner specular refraction horizon line (1dp horizontal light shelf).
 *   4. Continuous squircle curvature (default 16dp).
 *   5. Physical suspension depth shadow (soft ambient + directional elevation).
 *   6. Micro-spring press bounce for clickable cards.
 */
@Composable
fun StockticsCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    contentPadding: Dp = 16.dp,
    elevation: Dp = 4.dp,
    isGlass: Boolean = true,
    fillMaxWidth: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val fintech = LocalFintechColors.current
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceColors = if (isGlass) {
        fintech.glassSurfaceGradient
    } else {
        if (fintech.isDark) {
            listOf(Color(0xFF131722), Color(0xFF0F141E))
        } else {
            listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
        }
    }

    val borderColors = if (isGlass) {
        fintech.glassBorderGradient
    } else {
        listOf(fintech.border, fintech.border.copy(alpha = 0.5f))
    }

    val shadowSpotColor = if (fintech.isDark) {
        Color.Black.copy(alpha = 0.35f)
    } else {
        Color(0x180F172A)
    }

    val shadowAmbientColor = if (fintech.isDark) {
        Color.Black.copy(alpha = 0.25f)
    } else {
        Color(0x0C0F172A)
    }

    Box(
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .then(
                if (onClick != null) {
                    Modifier.fintechPressBounce(targetScale = 0.985f, onClick = onClick)
                } else Modifier
            )
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = shadowAmbientColor,
                spotColor = shadowSpotColor
            )
            .clip(shape)
            .background(Brush.verticalGradient(surfaceColors))
            .border(width = 1.dp, brush = Brush.verticalGradient(borderColors), shape = shape)
    ) {
        // Specular inner lip reflection (the subtle light catching the inside top edge of glass)
        if (isGlass) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                fintech.glassLipColor,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
