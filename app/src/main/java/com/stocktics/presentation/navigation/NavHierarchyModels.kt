package com.stocktics.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.ui.graphics.vector.ImageVector
import com.stocktics.ui.components.GlobalMarket

/**
 * Root tabs shown in the floating pill when the user is at the Home level.
 * [Home | IND | US | Tools]
 */
enum class RootNavTab(
    val title: String,
    val icon: ImageVector
) {
    HOME(
        title = "Home",
        icon = Icons.Rounded.Home
    ),
    IND(
        title = "IND",
        icon = Icons.Rounded.CurrencyRupee
    ),
    US(
        title = "US",
        icon = Icons.Rounded.AttachMoney
    ),
    TOOLS(
        title = "Tools",
        icon = Icons.Rounded.AutoGraph
    );

    companion object {
        val CHARTS = TOOLS
    }
}

/**
 * Contextual sub-destinations when inside Indian or US Market.
 * Exactly 4 tabs: [Explore | Holdings | Buy | Watchlist]
 */
enum class MarketSubTab(
    val title: String,
    val icon: ImageVector
) {
    EXPLORE(
        title = "Explore",
        icon = Icons.Rounded.Explore
    ),
    HOLDINGS(
        title = "Holdings",
        icon = Icons.Rounded.AccountBalanceWallet
    ),
    BUY(
        title = "Buy",
        icon = Icons.AutoMirrored.Rounded.TrendingUp
    ),
    WATCHLIST(
        title = "Watchlist",
        icon = Icons.Rounded.FormatListBulleted
    );

    companion object {
        val BUY_PIPELINE = BUY
        val SELL_PIPELINE = HOLDINGS
        val MORE = WATCHLIST
    }
}

/**
 * Contextual sub-destinations when inside Charts / Analytics view.
 * [Patterns | Heatmap | Compare | Academy]
 */
enum class ChartsSubTab(
    val title: String,
    val icon: ImageVector
) {
    PATTERNS(
        title = "Patterns",
        icon = Icons.Rounded.AutoGraph
    ),
    HEATMAP(
        title = "Heatmap",
        icon = Icons.Rounded.GridView
    ),
    COMPARE(
        title = "Compare",
        icon = Icons.AutoMirrored.Rounded.CompareArrows
    ),
    SECTORS(
        title = "Sectors",
        icon = Icons.Rounded.PieChart
    ),
    ACADEMY(
        title = "Academy",
        icon = Icons.Rounded.School
    )
}

/**
 * Overall active navigation context for the application.
 */
sealed class AppNavContext {
    object HomeRoot : AppNavContext()
    data class Market(val market: GlobalMarket, val subTab: MarketSubTab) : AppNavContext()
    data class Charts(val subTab: ChartsSubTab) : AppNavContext()
}
