package com.stocktics.presentation.heatmap

import androidx.compose.ui.graphics.Color
import com.stocktics.data.api.ApiHeatmapStock
import kotlin.math.max

data class TreemapNode<T>(
    val id: String,
    val value: Double,
    val data: T
)

data class TreemapRect<T>(
    val id: String,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val data: T
)

object TreemapLayoutEngine {

    /**
     * Institutional 7-tier color gradient based on percentage change.
     * Matches web application getTileColor in MarketHeatmapView.jsx.
     */
    fun getTileColor(pct: Double): Color {
        return when {
            pct >= 3.0 -> Color(0xFF00D09C)   // Strong Gain Green (Groww Mint)
            pct >= 1.5 -> Color(0xFF00B386)   // Moderate Gain Green
            pct >= 0.5 -> Color(0xFF00875A)   // Subtle Gain Green
            pct > -0.5 -> Color(0xFF1C2333)   // Neutral Slate
            pct > -1.5 -> Color(0xFFB91C1C)   // Subtle Loss Red
            pct > -3.0 -> Color(0xFFDC2626)   // Moderate Loss Red
            else -> Color(0xFFF04438)         // Strong Loss Red
        }
    }

    /**
     * Formats large market cap / numbers into financial shorthand (T, B, M, K).
     */
    fun formatLargeCurrency(num: Double, currSymbol: String = "$"): String {
        return when {
            num <= 0 -> "${currSymbol}0"
            num >= 1e12 -> "$currSymbol${String.format(java.util.Locale.US, "%.2f", num / 1e12)}T"
            num >= 1e9 -> "$currSymbol${String.format(java.util.Locale.US, "%.2f", num / 1e9)}B"
            num >= 1e6 -> "$currSymbol${String.format(java.util.Locale.US, "%.2f", num / 1e6)}M"
            num >= 1e3 -> "$currSymbol${String.format(java.util.Locale.US, "%.1f", num / 1e3)}K"
            else -> "$currSymbol${String.format(java.util.Locale.US, "%.2f", num)}"
        }
    }

    /**
     * Squarified Treemap Algorithm implementation.
     * Recursively partitions space ensuring tile aspect ratios are balanced (avoiding long thin slivers).
     */
    fun <T> computeTreemap(
        nodes: List<TreemapNode<T>>,
        width: Float,
        height: Float
    ): List<TreemapRect<T>> {
        val sorted = nodes.filter { it.value > 0 }.sortedByDescending { it.value }
        val totalValue = sorted.sumOf { it.value }
        if (totalValue <= 0 || width <= 1 || height <= 1) return emptyList()

        val rects = mutableListOf<TreemapRect<T>>()

        fun layout(x: Float, y: Float, w: Float, h: Float, currentNodes: List<TreemapNode<T>>) {
            if (currentNodes.isEmpty()) return
            if (w <= 1f || h <= 1f) return

            if (currentNodes.size == 1) {
                rects.add(
                    TreemapRect(
                        id = currentNodes[0].id,
                        x = x,
                        y = y,
                        w = w,
                        h = h,
                        data = currentNodes[0].data
                    )
                )
                return
            }

            val nodeValueSum = currentNodes.sumOf { it.value }
            if (nodeValueSum <= 0) return

            val isVertical = w >= h
            val size = if (isVertical) w else h

            var bestSplitIndex = 1
            var minMaxRatio = Float.MAX_VALUE

            for (i in 1..currentNodes.size) {
                val leftNodes = currentNodes.subList(0, i)
                val leftSum = leftNodes.sumOf { it.value }
                val leftRatio = (leftSum / nodeValueSum).toFloat()
                val thickness = size * leftRatio

                if (thickness <= 0f) continue

                var maxRatio = 0f
                val otherDim = if (isVertical) h else w

                for (node in leftNodes) {
                    val nodeRatio = (node.value / leftSum).toFloat()
                    val length = otherDim * nodeRatio
                    if (length <= 0f) continue

                    val aspect = max(thickness / length, length / thickness)
                    if (aspect > maxRatio) maxRatio = aspect
                }

                if (maxRatio < minMaxRatio) {
                    minMaxRatio = maxRatio
                    bestSplitIndex = i
                } else {
                    break
                }
            }

            val groupNodes = currentNodes.subList(0, bestSplitIndex)
            val remainingNodes = currentNodes.subList(bestSplitIndex, currentNodes.size)

            val groupSum = groupNodes.sumOf { it.value }
            val groupRatio = (groupSum / nodeValueSum).toFloat()

            if (isVertical) {
                val groupWidth = w * groupRatio
                var currentY = y
                for (node in groupNodes) {
                    val nodeRatio = (node.value / groupSum).toFloat()
                    val nodeHeight = h * nodeRatio
                    rects.add(
                        TreemapRect(
                            id = node.id,
                            x = x,
                            y = currentY,
                            w = groupWidth,
                            h = nodeHeight,
                            data = node.data
                        )
                    )
                    currentY += nodeHeight
                }
                layout(x + groupWidth, y, w - groupWidth, h, remainingNodes)
            } else {
                val groupHeight = h * groupRatio
                var currentX = x
                for (node in groupNodes) {
                    val nodeRatio = (node.value / groupSum).toFloat()
                    val nodeWidth = w * nodeRatio
                    rects.add(
                        TreemapRect(
                            id = node.id,
                            x = currentX,
                            y = y,
                            w = nodeWidth,
                            h = groupHeight,
                            data = node.data
                        )
                    )
                    currentX += nodeWidth
                }
                layout(x, y + groupHeight, w, h - groupHeight, remainingNodes)
            }
        }

        layout(0f, 0f, width, height, sorted)
        return rects
    }
}
