package com.stocktics.data.portfolio

data class PortfolioHolding(
    val id: Long = 0,
    val userId: String,
    val symbol: String,
    val name: String,
    val exchange: String,
    val currency: String = "₹",
    val market: String = "IN",
    val entryPrice: Double,
    val currentPrice: Double,
    val qty: Double,
    val entryDate: String,
    val stopLossPct: Double = 5.0,
    val targetPct: Double = 15.0,
    val trailingStopPct: Double = 4.0,
    val maxHoldDays: Int = 60,
    val status: String = "open", // "open" or "closed"
    val timeToLeaveScore: Int = 25, // 0 to 100
    val exitVerdict: String = "Safe Hold", // "Safe Hold", "Hold Position", "Consider Exiting", "Exit Now"
    val exitPrice: Double = 0.0,
    val exitDate: String = "",
    val exitReason: String = "",
    val logoUrl: String = "",
    val previousClose: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalInvested: Double get() = entryPrice * qty
    val currentValue: Double get() = currentPrice * qty
    val unrealizedPnl: Double get() = currentValue - totalInvested
    val unrealizedPnlPct: Double get() = if (totalInvested > 0) (unrealizedPnl / totalInvested) * 100.0 else 0.0
    val isPositive: Boolean get() = unrealizedPnl >= 0
    val stopLossPrice: Double get() = entryPrice * (1.0 - stopLossPct / 100.0)
    val targetPrice: Double get() = entryPrice * (1.0 + targetPct / 100.0)

    val effectivePrevClose: Double get() = if (previousClose > 0.0) previousClose else currentPrice
    val oneDayPnl: Double get() = (currentPrice - effectivePrevClose) * qty
    val oneDayPnlPct: Double get() = if (effectivePrevClose > 0.0) ((currentPrice - effectivePrevClose) / effectivePrevClose) * 100.0 else 0.0
    val isOneDayPositive: Boolean get() = oneDayPnl >= 0

    val isIndianMarket: Boolean get() = market.equals("IN", ignoreCase = true) ||
            currency == "₹" ||
            exchange.equals("NSE", ignoreCase = true) ||
            exchange.equals("BSE", ignoreCase = true) ||
            symbol.endsWith(".NS") ||
            symbol.endsWith(".BO")
}

data class PortfolioTransaction(
    val id: Long = 0,
    val userId: String,
    val holdingId: Long? = null,
    val symbol: String,
    val exchange: String,
    val txType: String, // "BUY" or "SELL"
    val price: Double,
    val qty: Double,
    val totalAmount: Double,
    val realizedPnl: Double = 0.0,
    val notes: String = "",
    val txDate: String
)

data class PortfolioSummary(
    val totalInvested: Double = 0.0,
    val currentValue: Double = 0.0,
    val unrealizedPnl: Double = 0.0,
    val unrealizedPnlPct: Double = 0.0,
    val oneDayReturn: Double = 0.0,
    val oneDayReturnPct: Double = 0.0,
    val realizedPnl: Double = 0.0,
    val activePositionsCount: Int = 0,
    val closedPositionsCount: Int = 0,
    val overallRiskScore: Int = 25, // 0 to 100
    val highRiskCount: Int = 0
)

data class BuyPipelineCandidate(
    val symbol: String,
    val name: String,
    val exchange: String,
    val currency: String = "₹",
    val price: Double,
    val change: String,
    val isPositive: Boolean,
    val buyScore: Int, // 0 to 100
    val verdict: String, // "Strong Buy", "Buy", "Hold", "Avoid"
    val technicalScore: Int,
    val mlScore: Int,
    val smartMoneyScore: Int,
    val internalsScore: Int,
    val forensicScore: Int,
    val keyCatalysts: List<String> = emptyList(),
    val keyRisks: List<String> = emptyList(),
    val targetPrice: Double = 0.0,
    val stopLossPrice: Double = 0.0,
    val logoUrl: String = ""
)
