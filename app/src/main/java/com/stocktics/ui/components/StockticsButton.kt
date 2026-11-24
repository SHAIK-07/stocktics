package com.stocktics.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.LocalFintechColors
import com.stocktics.ui.theme.LossRed

/**
 * Standard Apple Vision / Liquid Glass Primary Action Button.
 * 14dp continuous squircle curve, radiant specular hairline top highlight,
 * depth shadow, and tactile micro-bounce interaction.
 */
@Composable
fun StockticsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    cornerRadius: Dp = 14.dp,
    height: Dp = 50.dp
) {
    val fintech = LocalFintechColors.current
    val shape = RoundedCornerShape(cornerRadius)

    val isDanger = containerColor == LossRed

    // Emerald radiant gradient vs Danger red gradient vs Custom
    val baseBrush = when {
        isDanger -> Brush.verticalGradient(
            listOf(Color(0xFFF85246), Color(0xFFD92D20))
        )
        containerColor != Color.Unspecified && containerColor != FintechPrimary -> Brush.verticalGradient(
            listOf(containerColor, containerColor.copy(alpha = 0.85f))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF00E6AD), Color(0xFF00B386))
        )
    }

    val specularBorderBrush = Brush.verticalGradient(
        listOf(
            Color(0x80FFFFFF),
            Color(0x1AFFFFFF),
            Color(0x05000000)
        )
    )

    val resolvedContentColor = when {
        contentColor != Color.Unspecified && contentColor != Color.White -> contentColor
        isDanger -> Color.White
        else -> Color(0xFF070A0F) // High-contrast bold dark slate text on emerald
    }

    val shadowSpotColor = when {
        !enabled -> Color.Transparent
        isDanger -> LossRed.copy(alpha = 0.35f)
        else -> Color(0xFF00D09C).copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .height(height)
            .shadow(
                elevation = if (enabled && !isLoading) 6.dp else 0.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = shadowSpotColor
            )
            .clip(shape)
            .then(
                if (enabled) {
                    Modifier
                        .background(baseBrush)
                        .border(width = 1.dp, brush = specularBorderBrush, shape = shape)
                } else {
                    Modifier
                        .background(if (fintech.isDark) Color(0xFF1E2638) else Color(0xFFE2E8F0))
                        .border(1.dp, fintech.border, shape)
                }
            )
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .fintechPressBounce(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = resolvedContentColor,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) resolvedContentColor else fintech.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) resolvedContentColor else fintech.textTertiary,
                    letterSpacing = 0.1.sp
                )
            }
        }
    }
}

/**
 * Apple Vision Liquid Frosted Glass Secondary Button.
 * 14dp continuous squircle curve, specular hairline edge, subtle translucent glass.
 */
@Composable
fun StockticsSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    cornerRadius: Dp = 14.dp,
    height: Dp = 48.dp
) {
    val fintech = LocalFintechColors.current
    val shape = RoundedCornerShape(cornerRadius)

    val glassSurfaceGradient = if (fintech.isDark) {
        listOf(Color(0x3D2A364F), Color(0x241C2538))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xF2F8FAFC))
    }

    val glassBorderGradient = if (fintech.isDark) {
        listOf(Color(0x59FFFFFF), Color(0x1F94A3B8), Color(0x0A222A3B))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0x50CBD5E1), Color(0x2094A3B8))
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .height(height)
            .shadow(
                elevation = if (enabled) 2.dp else 0.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(Brush.verticalGradient(glassSurfaceGradient))
            .border(width = 1.dp, brush = Brush.verticalGradient(glassBorderGradient), shape = shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .fintechPressBounce(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) fintech.textSecondary else fintech.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) fintech.textPrimary else fintech.textTertiary,
                letterSpacing = 0.1.sp
            )
        }
    }
}

/**
 * Destructive / Danger Action Button (Exit engine sell actions, deletes).
 */
@Composable
fun StockticsDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    cornerRadius: Dp = 14.dp,
    height: Dp = 50.dp
) {
    StockticsPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        icon = icon,
        containerColor = LossRed,
        contentColor = Color.White,
        cornerRadius = cornerRadius,
        height = height
    )
}

/**
 * Ghost / Text Button for links, inline dismissals, and subtle controls.
 */
@Composable
fun StockticsGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Color.Unspecified
) {
    val fintech = LocalFintechColors.current
    val resolvedColor = if (color != Color.Unspecified) color else fintech.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .fintechPressBounce()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) resolvedColor else fintech.textTertiary
        )
    }
}

/**
 * Universal Apple Vision / Liquid Glass Segmented Control.
 * Synchronized 16dp outer glass container with 13dp concentric active glass thumb,
 * specular hairline borders, smooth spring animations, and tactile feedback.
 */
@Composable
fun <T> StockticsSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() },
    itemPrefix: ((T) -> String?)? = null,
    itemIcon: ((T) -> ImageVector?)? = null,
    height: Dp = 46.dp,
    outerCornerRadius: Dp = 16.dp
) {
    val fintech = LocalFintechColors.current
    val outerShape = RoundedCornerShape(outerCornerRadius)
    val innerCornerRadius = (outerCornerRadius - 3.dp).coerceAtLeast(8.dp)
    val innerShape = RoundedCornerShape(innerCornerRadius)

    val containerGradient = if (fintech.isDark) {
        listOf(Color(0xF0131722), Color(0xEB0F141E))
    } else {
        listOf(Color(0xFAFFFFFF), Color(0xF2F1F5F9))
    }

    val containerBorderGradient = if (fintech.isDark) {
        listOf(Color(0x4DFFFFFF), Color(0x1A94A3B8), Color(0x0A222A3B))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0x50CBD5E1), Color(0x2094A3B8))
    }

    Box(
        modifier = modifier
            .height(height)
            .shadow(
                elevation = 4.dp,
                shape = outerShape,
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(outerShape)
            .background(Brush.verticalGradient(containerGradient))
            .border(1.dp, Brush.verticalGradient(containerBorderGradient), outerShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem

                val itemTextColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (fintech.isDark) Color(0xFF00E6AD) else Color(0xFF00A87E)
                    } else {
                        fintech.textSecondary
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "segmentedTextColor"
                )

                val itemScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.98f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "segmentedItemScale"
                )

                val activeThumbGradient = if (fintech.isDark) {
                    listOf(Color(0xFF28344B), Color(0xFF1B2434))
                } else {
                    listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
                }

                val activeThumbBorderGradient = if (fintech.isDark) {
                    listOf(Color(0x80FFFFFF), Color(0x3300D09C), Color(0x1A2A364F))
                } else {
                    listOf(Color(0xFFFFFFFF), Color(0x4000D09C), Color(0x30CBD5E1))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(itemScale)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = innerShape,
                                        ambientColor = Color.Black.copy(alpha = 0.2f),
                                        spotColor = Color.Black.copy(alpha = 0.25f)
                                    )
                                    .clip(innerShape)
                                    .background(Brush.verticalGradient(activeThumbGradient))
                                    .border(
                                        1.dp,
                                        Brush.verticalGradient(activeThumbBorderGradient),
                                        innerShape
                                    )
                            } else {
                                Modifier
                                    .clip(innerShape)
                                    .background(Color.Transparent)
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(item) }
                        )
                        .fintechPressBounce(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        // Optional Prefix string (e.g. flag 🇮🇳, 🇺🇸)
                        itemPrefix?.invoke(item)?.let { prefix ->
                            Text(
                                text = prefix,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Optional Icon
                        itemIcon?.invoke(item)?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = itemTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = itemLabel(item),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = itemTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
