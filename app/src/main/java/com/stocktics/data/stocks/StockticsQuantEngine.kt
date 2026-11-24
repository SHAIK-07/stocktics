package com.stocktics.data.stocks

object StockticsQuantEngine {

    fun computeQuantVerdict(
        symbol: String,
        price: Double,
        currencyPrefix: String,
        pe: Double?,
        pb: Double?,
        targetPrice: Double?,
        profitMargin: Double?,
        operatingMargin: Double?,
        revenueGrowth: Double?,
        earningsGrowth: Double?,
        debtToEquity: Double?,
        totalCash: Double?,
        totalDebt: Double?,
        bookValue: Double?,
        dma50: Double?,
        dma200: Double?,
        beta: Double?,
        insiderPercent: Double?,
        institutionPercent: Double?,
        buyCount: Int = 28,
        sellCount: Int = 2
    ): QuantVerdict {
        val signals = mutableListOf<QuantSignalItem>()
        val bullishDrivers = mutableListOf<String>()
        val watchoutRisks = mutableListOf<String>()

        // -----------------------------------------------------------------
        // Pillar 1: Valuation (Max 20 pts)
        // -----------------------------------------------------------------
        var valPts = 0
        if (pe != null && pe > 0 && pe <= 25.0) {
            valPts += 8
            signals.add(QuantSignalItem("Valuation", "P/E Ratio", String.format("%.2f", pe), "🟢 Positive", "Attractive / Reasonable multiple (<=25)"))
            bullishDrivers.add("Compelling P/E ratio of ${String.format("%.1f", pe)}x")
        } else if (pe != null && pe > 25.0 && pe <= 40.0) {
            valPts += 4
            signals.add(QuantSignalItem("Valuation", "P/E Ratio", String.format("%.2f", pe), "🟡 Neutral", "Moderate earnings multiple (25-40)"))
        } else {
            val peStr = if (pe != null) String.format("%.2f", pe) else "--"
            signals.add(QuantSignalItem("Valuation", "P/E Ratio", peStr, "🔴 Negative", "High multiple or unprofitable"))
            watchoutRisks.add("Elevated P/E valuation relative to historical mean")
        }

        if (pb != null && pb > 0 && pb <= 3.0) {
            valPts += 6
            signals.add(QuantSignalItem("Valuation", "Price / Book", String.format("%.2f", pb), "🟢 Positive", "Strong tangible asset backing (P/B <= 3.0)"))
        } else if (pb != null && pb > 3.0 && pb <= 8.0) {
            valPts += 3
            signals.add(QuantSignalItem("Valuation", "Price / Book", String.format("%.2f", pb), "🟡 Neutral", "Standard asset premium (P/B 3-8)"))
        } else {
            val pbStr = if (pb != null) String.format("%.2f", pb) else "--"
            signals.add(QuantSignalItem("Valuation", "Price / Book", pbStr, "🔴 Negative", "High book multiple premium"))
        }

        val targetUpside = if (targetPrice != null && price > 0) ((targetPrice - price) / price * 100) else 0.0
        if (targetUpside >= 15.0) {
            valPts += 6
            signals.add(QuantSignalItem("Valuation", "1Y Target Upside", String.format("%+.1f%%", targetUpside), "🟢 Positive", "Strong analyst upside to $currencyPrefix ${String.format("%,.2f", targetPrice)}"))
            bullishDrivers.add("${String.format("%+.1f%%", targetUpside)} upside to consensus 1Y target")
        } else if (targetUpside >= 0.0) {
            valPts += 3
            signals.add(QuantSignalItem("Valuation", "1Y Target Upside", String.format("%+.1f%%", targetUpside), "🟡 Neutral", "Modest upside to consensus target"))
        } else {
            signals.add(QuantSignalItem("Valuation", "1Y Target Upside", String.format("%+.1f%%", targetUpside), "🔴 Negative", "Trading above analyst target price"))
            watchoutRisks.add("Currently trading ahead of Wall Street price targets")
        }

        // -----------------------------------------------------------------
        // Pillar 2: Profitability & Growth (Max 20 pts)
        // -----------------------------------------------------------------
        var profPts = 0
        if (profitMargin != null && profitMargin >= 0.10) {
            profPts += 6
            signals.add(QuantSignalItem("Profitability", "Net Margin", String.format("%.2f%%", profitMargin * 100), "🟢 Positive", "High double-digit net profit margin (>=10%)"))
            bullishDrivers.add("Excellent net profit margin of ${String.format("%.1f%%", profitMargin * 100)}")
        } else if (profitMargin != null && profitMargin >= 0.05) {
            profPts += 4
            signals.add(QuantSignalItem("Profitability", "Net Margin", String.format("%.2f%%", profitMargin * 100), "🟡 Neutral", "Stable single-digit net margin (5-10%)"))
        } else {
            val pmStr = if (profitMargin != null) String.format("%.2f%%", profitMargin * 100) else "--"
            signals.add(QuantSignalItem("Profitability", "Net Margin", pmStr, "🔴 Negative", "Thin or negative net margin (<5%)"))
        }

        if (operatingMargin != null && operatingMargin >= 0.12) {
            profPts += 4
            signals.add(QuantSignalItem("Profitability", "Operating Margin", String.format("%.2f%%", operatingMargin * 100), "🟢 Positive", "Efficient operating margin (>=12%)"))
        } else if (operatingMargin != null && operatingMargin >= 0.05) {
            profPts += 2
            signals.add(QuantSignalItem("Profitability", "Operating Margin", String.format("%.2f%%", operatingMargin * 100), "🟡 Neutral", "Moderate operating efficiency"))
        } else {
            val omStr = if (operatingMargin != null) String.format("%.2f%%", operatingMargin * 100) else "--"
            signals.add(QuantSignalItem("Profitability", "Operating Margin", omStr, "🔴 Negative", "Sub-par operating conversion"))
        }

        if (revenueGrowth != null && revenueGrowth >= 0.15) {
            profPts += 5
            signals.add(QuantSignalItem("Growth", "Revenue Growth (YoY)", String.format("%+.2f%%", revenueGrowth * 100), "🟢 Positive", "Rapid top-line acceleration (>=15%)"))
            bullishDrivers.add("Accelerating quarterly revenue (${String.format("%+.1f%%", revenueGrowth * 100)} YoY)")
        } else if (revenueGrowth != null && revenueGrowth >= 0.05) {
            profPts += 3
            signals.add(QuantSignalItem("Growth", "Revenue Growth (YoY)", String.format("%+.2f%%", revenueGrowth * 100), "🟡 Neutral", "Moderate revenue growth (5-15%)"))
        } else {
            val rgStr = if (revenueGrowth != null) String.format("%+.2f%%", revenueGrowth * 100) else "--"
            signals.add(QuantSignalItem("Growth", "Revenue Growth (YoY)", rgStr, "🔴 Negative", "Subdued revenue momentum"))
            watchoutRisks.add("Subdued quarterly revenue growth")
        }

        if (earningsGrowth != null && earningsGrowth >= 0.10) {
            profPts += 5
            signals.add(QuantSignalItem("Growth", "Earnings Growth (YoY)", String.format("%+.2f%%", earningsGrowth * 100), "🟢 Positive", "Strong bottom-line expansion (>=10%)"))
            bullishDrivers.add("Robust quarterly earnings growth (${String.format("%+.1f%%", earningsGrowth * 100)} YoY)")
        } else if (earningsGrowth != null && earningsGrowth >= 0.0) {
            profPts += 2
            signals.add(QuantSignalItem("Growth", "Earnings Growth (YoY)", String.format("%+.2f%%", earningsGrowth * 100), "🟡 Neutral", "Stable earnings expansion"))
        } else {
            val egStr = if (earningsGrowth != null) String.format("%+.2f%%", earningsGrowth * 100) else "--"
            signals.add(QuantSignalItem("Growth", "Earnings Growth (YoY)", egStr, "🔴 Negative", "Quarterly earnings contraction"))
            watchoutRisks.add("Quarterly earnings contraction recorded YoY")
        }

        // -----------------------------------------------------------------
        // Pillar 3: Financial Health & Solvency (Max 20 pts)
        // -----------------------------------------------------------------
        var solvPts = 0
        if (debtToEquity != null && debtToEquity <= 50.0) {
            solvPts += 8
            signals.add(QuantSignalItem("Solvency", "Debt to Equity", String.format("%.2f%%", debtToEquity), "🟢 Positive", "Conservative leverage / low debt burden (<=50%)"))
            bullishDrivers.add("Strong balance sheet with low Debt-to-Equity")
        } else if (debtToEquity != null && debtToEquity <= 100.0) {
            solvPts += 4
            signals.add(QuantSignalItem("Solvency", "Debt to Equity", String.format("%.2f%%", debtToEquity), "🟡 Neutral", "Manageable debt leverage (50-100%)"))
        } else {
            val deStr = if (debtToEquity != null) String.format("%.2f%%", debtToEquity) else "--"
            signals.add(QuantSignalItem("Solvency", "Debt to Equity", deStr, "🔴 Negative", "Elevated debt level (>100%)"))
            watchoutRisks.add("High Debt-to-Equity ratio")
        }

        if (totalCash != null && totalDebt != null && totalDebt > 0 && (totalCash >= totalDebt * 0.5)) {
            solvPts += 6
            signals.add(QuantSignalItem("Solvency", "Cash / Debt Ratio", String.format("%.1f%%", (totalCash / totalDebt) * 100), "🟢 Positive", "Solid cash cushion relative to total debt"))
        } else if (totalCash != null && totalCash > 0) {
            solvPts += 3
            signals.add(QuantSignalItem("Solvency", "Cash Reserves", "Adequate", "🟡 Neutral", "Positive liquidity reserve on hand"))
        } else {
            signals.add(QuantSignalItem("Solvency", "Cash Reserves", "Low", "🔴 Negative", "Constrained liquidity reserves"))
        }

        if (bookValue != null && bookValue > 0) {
            solvPts += 6
            signals.add(QuantSignalItem("Solvency", "Book Value Safety", "$currencyPrefix ${String.format("%,.2f", bookValue)}", "🟢 Positive", "Positive net asset solvency"))
        } else {
            signals.add(QuantSignalItem("Solvency", "Book Value Safety", "--", "🔴 Negative", "Negative net tangible assets"))
        }

        // -----------------------------------------------------------------
        // Pillar 4: Trend & Technical Momentum (Max 20 pts)
        // -----------------------------------------------------------------
        var trendPts = 0
        if (dma200 != null && price > dma200) {
            trendPts += 8
            signals.add(QuantSignalItem("Technical", "200 DMA Trend", String.format("%+.2f%%", (price - dma200) / dma200 * 100), "🟢 Positive", "Trading comfortably above 200 DMA (Long-term Bullish)"))
            bullishDrivers.add("Strong primary uptrend trading above 200 DMA")
        } else {
            val dma200Str = if (dma200 != null && price > 0) String.format("%+.2f%%", (price - dma200) / dma200 * 100) else "--"
            signals.add(QuantSignalItem("Technical", "200 DMA Trend", dma200Str, "🔴 Negative", "Trading below 200 DMA (Long-term Bearish)"))
            watchoutRisks.add("Positioned below 200-day moving average")
        }

        if (dma50 != null && price > dma50) {
            trendPts += 6
            signals.add(QuantSignalItem("Technical", "50 DMA Momentum", String.format("%+.2f%%", (price - dma50) / dma50 * 100), "🟢 Positive", "Constructive short-term momentum (Above 50 DMA)"))
        } else {
            val dma50Str = if (dma50 != null && price > 0) String.format("%+.2f%%", (price - dma50) / dma50 * 100) else "--"
            signals.add(QuantSignalItem("Technical", "50 DMA Momentum", dma50Str, "🔴 Negative", "Short-term consolidation (Below 50 DMA)"))
        }

        if (beta != null && beta > 0.0 && beta <= 1.2) {
            trendPts += 6
            signals.add(QuantSignalItem("Technical", "Beta Volatility", String.format("%.2f", beta), "🟢 Positive", "Controlled beta and defensive market profile"))
        } else {
            val betaStr = if (beta != null) String.format("%.2f", beta) else "--"
            signals.add(QuantSignalItem("Technical", "Beta Volatility", betaStr, "🟡 Neutral", "Elevated market beta volatility"))
        }

        // -----------------------------------------------------------------
        // Pillar 5: Smart Money & Ownership (Max 20 pts)
        // -----------------------------------------------------------------
        var smartPts = 0
        if (insiderPercent != null && insiderPercent >= 0.40) {
            smartPts += 7
            signals.add(QuantSignalItem("Smart Money", "Promoter / Insider Stake", String.format("%.2f%%", insiderPercent * 100), "🟢 Positive", "High skin-in-the-game promoter holding (>=40%)"))
            bullishDrivers.add("Exceptional promoter commitment (${String.format("%.1f%%", insiderPercent * 100)} insider stake)")
        } else if (insiderPercent != null && insiderPercent >= 0.15) {
            smartPts += 4
            signals.add(QuantSignalItem("Smart Money", "Promoter / Insider Stake", String.format("%.2f%%", insiderPercent * 100), "🟡 Neutral", "Moderate insider ownership (15-40%)"))
        } else {
            val insStr = if (insiderPercent != null) String.format("%.2f%%", insiderPercent * 100) else "--"
            signals.add(QuantSignalItem("Smart Money", "Promoter / Insider Stake", insStr, "🟡 Neutral", "Dispersed public ownership"))
        }

        if (institutionPercent != null && institutionPercent >= 0.25) {
            smartPts += 7
            signals.add(QuantSignalItem("Smart Money", "Institutional Backing", String.format("%.2f%%", institutionPercent * 100), "🟢 Positive", "Deep institutional backing (FII + DII >= 25%)"))
            bullishDrivers.add("Strong institutional sponsorship (${String.format("%.1f%%", institutionPercent * 100)} backing)")
        } else if (institutionPercent != null && institutionPercent >= 0.10) {
            smartPts += 4
            signals.add(QuantSignalItem("Smart Money", "Institutional Backing", String.format("%.2f%%", institutionPercent * 100), "🟡 Neutral", "Moderate institutional participation"))
        } else {
            val instStr = if (institutionPercent != null) String.format("%.2f%%", institutionPercent * 100) else "--"
            signals.add(QuantSignalItem("Smart Money", "Institutional Backing", instStr, "🔴 Negative", "Low institutional participation"))
        }

        if (buyCount > sellCount) {
            smartPts += 6
            signals.add(QuantSignalItem("Smart Money", "Analyst Consensus", "$buyCount Buys vs $sellCount Sells", "🟢 Positive", "Overwhelming Wall Street Buy consensus"))
        } else {
            smartPts += 3
            signals.add(QuantSignalItem("Smart Money", "Analyst Consensus", "Neutral Bias", "🟡 Neutral", "Mixed or Neutral ratings"))
        }

        // -----------------------------------------------------------------
        // Total Score & Actionable Verdict
        // -----------------------------------------------------------------
        val totalScore = (valPts + profPts + solvPts + trendPts + smartPts).coerceIn(0, 100)

        val verdict: String
        val action: String

        when {
            totalScore >= 80 -> {
                verdict = "🟢 STRONG BUY"
                action = "Institutional Accumulation Profile — High conviction buy"
            }
            totalScore >= 60 -> {
                verdict = "🟢 BUY / ACCUMULATE"
                action = "Solid Fundamentals — Accumulate on dips"
            }
            totalScore >= 40 -> {
                verdict = "🟡 HOLD / DO NOTHING"
                action = "Mixed Signals / Fairly Valued — Wait for clearer catalysts"
            }
            else -> {
                verdict = "🔴 SELL / REDUCE"
                action = "Elevated Risk / High Valuation — Consider taking profits or trimming"
            }
        }

        return QuantVerdict(
            score = totalScore,
            rating = verdict,
            action = action,
            pillars = mapOf(
                "Valuation" to "$valPts / 20",
                "Profitability" to "$profPts / 20",
                "Solvency" to "$solvPts / 20",
                "Trend & Momentum" to "$trendPts / 20",
                "Smart Money" to "$smartPts / 20"
            ),
            signals = signals,
            bullishDrivers = bullishDrivers,
            watchoutRisks = watchoutRisks
        )
    }
}
