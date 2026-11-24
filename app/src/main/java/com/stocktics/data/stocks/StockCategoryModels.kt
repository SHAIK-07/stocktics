package com.stocktics.data.stocks

enum class StockCategory(
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val explanation: String
) {
    FIFTY_TWO_WEEK_LOW(
        title = "52W Low",
        subtitle = "Near 52-Week Low",
        badgeText = "Value Zone",
        explanation = "Stocks trading near their 52-week lows. Suitable for value seekers, reversal setups, and long-term contrarian accumulation."
    ),
    FIFTY_TWO_WEEK_HIGH(
        title = "52W High",
        subtitle = "Near 52-Week High",
        badgeText = "Momentum",
        explanation = "High-relative-strength leaders trading within 5% of their 52-week peak. Prime candidates for trend continuation and fresh all-time-high breakouts."
    ),
    NEAR_SUPPORT(
        title = "Near Support",
        subtitle = "Support Retest Zones",
        badgeText = "Low Risk Entry",
        explanation = "Equities hovering right at major technical support boundaries (S1/S2 or 50/200 DMA). Offers tightly defined stop-losses and favorable risk-to-reward ratios."
    ),
    NEAR_RESISTANCE(
        title = "Near Resistance",
        subtitle = "Breakout Alert",
        badgeText = "Breakout Watch",
        explanation = "Equities pressing against heavy resistance ceilings (R1/R2). Watch closely for decisive volume expansion indicating imminent explosive breakout."
    ),
    DEBT_FREE(
        title = "Debt-Free",
        subtitle = "Zero Net Debt",
        badgeText = "Balance Fortress",
        explanation = "Companies with zero or negligible debt-to-equity ratios. Insulated against rising interest rates with bulletproof balance sheets and high ROCE."
    ),
    GOOD_GROWTH(
        title = "Good Growth",
        subtitle = "High Growth Titans",
        badgeText = "Top Quant Growth",
        explanation = "Fundamental compounding engines demonstrating consistent double-digit revenue expansion, accelerating quarterly EPS, and top quantitative growth ratings."
    )
}

data class CategoryStockItem(
    val symbol: String,
    val name: String,
    val exchange: String,
    val currency: String,
    val price: String,
    val rawPrice: Double,
    val change: String,
    val isPositive: Boolean,
    val logoUrl: String,
    val initials: String,
    val category: StockCategory,
    val metricLabel: String,      // e.g. "From 52W Low", "To 52W High", "Distance to Support", "Debt/Eq", "Growth Score"
    val metricValue: String,      // e.g. "+1.8%", "-1.2%", "+0.7%", "0.00", "9.6 / 10"
    val metricHighlight: String,  // e.g. "52W L: ₹1,240", "Support: ₹2,450", "Zero Net Debt", "+32% YoY EPS"
    val isIndian: Boolean
)

enum class MarketTab(val title: String, val shortTitle: String, val flag: String) {
    INDIAN("Indian Market", "Indian", "🇮🇳"),
    US("US Market", "US", "🇺🇸")
}
