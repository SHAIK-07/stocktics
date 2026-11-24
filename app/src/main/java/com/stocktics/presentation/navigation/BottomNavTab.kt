package com.stocktics.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavTab(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME(
        title = "Home",
        icon = Icons.Rounded.Home,
        route = "home"
    ),
    SCANNER(
        title = "Setups",
        icon = Icons.Rounded.AutoGraph,
        route = "scanner"
    ),
    BUY(
        title = "Buy",
        icon = Icons.AutoMirrored.Rounded.TrendingUp,
        route = "buy"
    ),
    SELL(
        title = "Sell",
        icon = Icons.AutoMirrored.Rounded.TrendingDown,
        route = "sell"
    ),
    LEARNING(
        title = "Academy",
        icon = Icons.Rounded.School,
        route = "learning"
    )
}
