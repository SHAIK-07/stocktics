package com.stocktics.data.stocks

// ==============================================================================
// 1. Explore Screen Models: Market Indices & Live Search
// ==============================================================================

data class MarketIndexItem(
    val name: String,
    val symbol: String,
    val price: String,
    val change: String,
    val isPositive: Boolean
)

data class StockSearchItem(
    val symbol: String,
    val name: String,
    val exchange: String,
    val currency: String,
    val price: String,
    val change: String,
    val isPositive: Boolean,
    val logoUrl: String = "",
    val category: String = "General"
)

// ==============================================================================
// 2. Stocktics Proprietary Quant Score & Verdict Models
// ==============================================================================

data class QuantSignalItem(
    val pillar: String,
    val metric: String,
    val value: String,
    val signal: String, // "🟢 Positive", "🟡 Neutral", "🔴 Negative"
    val diagnosis: String
)

data class QuantVerdict(
    val score: Int, // 0 to 100
    val rating: String, // "🟢 STRONG BUY", "🟢 BUY / ACCUMULATE", "🟡 HOLD / DO NOTHING", "🔴 SELL / REDUCE"
    val action: String, // Strategic action explanation
    val pillars: Map<String, String>, // "Valuation": "20 / 20", etc.
    val signals: List<QuantSignalItem>,
    val bullishDrivers: List<String>,
    val watchoutRisks: List<String>
)

// ==============================================================================
// 3. Complete 16-Metrics Table (Exact 4x4 Grid)
// ==============================================================================

data class MetricsGrid16(
    val previousClose: String,
    val open: String,
    val bid: String,
    val ask: String,
    val daysRange: String,
    val fiftyTwoWeekRange: String,
    val volume: String,
    val avgVolume: String,
    val marketCap: String,
    val beta: String,
    val peRatio: String,
    val eps: String,
    val earningsDate: String,
    val forwardDividend: String,
    val exDividendDate: String,
    val targetEst: String
)

// ==============================================================================
// 4. Company Overview & Corporate Profile
// ==============================================================================

data class CompanyProfileData(
    val symbol: String,
    val name: String,
    val title: String,
    val summary: String,
    val website: String,
    val employees: String,
    val fiscalYearEnd: String,
    val sector: String,
    val industry: String
)

// ==============================================================================
// 5. Corporate Events Calendar
// ==============================================================================

data class CorporateEventItem(
    val type: String, // "Earnings Release", "Corporate Action", "Capital Restructuring"
    val name: String,
    val dateTime: String,
    val details: String
)

// ==============================================================================
// 6. Financial Statements (4-Year Bar Chart Data)
// ==============================================================================

data class StatementMetricRow(
    val metric: String,
    val year2023: String,
    val year2024: String,
    val year2025: String,
    val year2026: String,
    val val2023: Double = 0.0,
    val val2024: Double = 0.0,
    val val2025: Double = 0.0,
    val val2026: Double = 0.0
)

data class FinancialStatementsData(
    val incomeStatement: List<StatementMetricRow>,
    val balanceSheet: List<StatementMetricRow>,
    val cashFlow: List<StatementMetricRow>
)

// ==============================================================================
// 7. Wall Street Analyst Consensus & Projections
// ==============================================================================

data class EpsTrendItem(
    val quarterDate: String,
    val epsEstimate: String,
    val reportedEps: String,
    val surprise: String,
    val outcome: String // "Beat (+0.42)", "Missed (-0.18)", "Upcoming"
)

data class EstimateRow(
    val period: String,
    val analystCount: String,
    val avgEstimate: String,
    val lowEstimate: String,
    val highEstimate: String,
    val growth: String
)

data class AnalystConsensusData(
    val epsTrends: List<EpsTrendItem>,
    val revenueEstimates: List<EstimateRow>,
    val epsEstimates: List<EstimateRow>,
    val currentPrice: Double,
    val meanTarget: Double,
    val highTarget: Double,
    val lowTarget: Double,
    val buyRatings: Int = 28,
    val holdRatings: Int = 6,
    val sellRatings: Int = 2
)

// ==============================================================================
// 8. Major Holders & Smart Money Footprint
// ==============================================================================

data class MajorHolderItem(
    val breakdown: String,
    val value: String
)

data class InsiderRosterItem(
    val nameAndRole: String,
    val transaction: String,
    val date: String,
    val shares: String
)

data class InsiderPurchaseItem(
    val metric: String,
    val shares: String,
    val transactions: String
)

data class HoldersData(
    val majorHolders: List<MajorHolderItem>,
    val insiderRoster: List<InsiderRosterItem>,
    val insiderPurchases: List<InsiderPurchaseItem>
)

// ==============================================================================
// 9. Verified News Articles (Last 2 Months, Zero Ads)
// ==============================================================================

data class NewsArticleItem(
    val title: String,
    val publisher: String,
    val timeAgo: String,
    val publishedAt: String,
    val url: String,
    val thumbnail: String = "",
    val relatedTickers: String = "",
    val description: String = "",
    val sentiment: String = "neutral", // "positive", "neutral", "negative"
    val newsDate: String = "",
    val isToday: Boolean = false
)

data class NewsSentimentSummary(
    val positiveCount: Int = 0,
    val neutralCount: Int = 0,
    val negativeCount: Int = 0,
    val label: String = ""
)

// ==============================================================================
// 10. Interactive Chart Candle Data
// ==============================================================================

data class ChartCandlePoint(
    val dateLabel: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long
)

// ==============================================================================
// 11. Technical Analysis Data
// ==============================================================================

data class TechnicalAnalysisData(
    val bullishCount: Int,
    val neutralCount: Int,
    val bearishCount: Int,
    val summaryVerdict: String, // "Bullish", "Neutral", "Bearish"
    val gaugeIndex: Int, // 0 to 19
    val rsi14: Double,
    val rsiSignal: String, // "Overbought", "Oversold", "Neutral"
    val macd: Double,
    val macdSignal: Double,
    val macdStatus: String, // "Bullish Cross", "Bearish Cross"
    val beta: String,
    val sma20: Double,
    val sma50: Double,
    val sma200: Double,
    val stochK: Double,
    val stochStatus: String,
    val pivot: Double,
    val r1: Double,
    val r2: Double,
    val s1: Double,
    val s2: Double
)

// ==============================================================================
// 12. Master Deep Research Payload
// ==============================================================================

data class DeepStockResearchPayload(
    val symbol: String,
    val name: String,
    val exchange: String,
    val currency: String,
    val price: Double,
    val prevClose: Double,
    val changeAbs: Double,
    val changePct: Double,
    val logoUrl: String,
    val aiStorySummary: String,
    val timeframeReturns: Map<String, String>,
    val chartPoints: List<ChartCandlePoint>,
    val timeframeCharts: Map<String, List<ChartCandlePoint>>,
    val metrics16: MetricsGrid16,
    val profile: CompanyProfileData,
    val upcomingEvents: List<CorporateEventItem>,
    val recentEvents: List<CorporateEventItem>,
    val valuationStats: List<Pair<String, String>>,
    val profitabilityStats: List<Pair<String, String>>,
    val solvencyStats: List<Pair<String, String>>,
    val trendStats: List<Pair<String, String>>,
    val financialStatements: FinancialStatementsData,
    val consensus: AnalystConsensusData,
    val holders: HoldersData,
    val news: List<NewsArticleItem>,
    val quantVerdict: QuantVerdict,
    val technicals: TechnicalAnalysisData? = null,
    val indmoneyEvents: List<NewsArticleItem> = emptyList(),
    val yahooNews: List<NewsArticleItem> = emptyList()
)

