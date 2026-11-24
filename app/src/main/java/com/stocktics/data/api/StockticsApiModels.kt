package com.stocktics.data.api

// ==============================================================================
// 1. Single Stock Deep Analysis Models (7 Financial Chapters)
// ==============================================================================

data class ApiStockMetric(
    val id: String,
    val name: String,
    val value: String,
    val benchmark: String = "",
    val diagnosis: String = "",
    val isPositive: Boolean = true,
    val category: String = ""
)

data class ApiStockDimension(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val metrics: List<ApiStockMetric> = emptyList()
)

data class ApiStockAnalysis(
    val ticker: String,
    val companyName: String,
    val logoUrl: String = "",
    val exchange: String = "",
    val market: String = "IN",
    val currencySymbol: String = "₹",
    val currentPrice: Double = 0.0,
    val dayChangePct: Double = 0.0,
    val fiftyTwoWeekHigh: Double = 0.0,
    val fiftyTwoWeekLow: Double = 0.0,
    val performance: Map<String, Double> = emptyMap(),
    val dimensions: List<ApiStockDimension> = emptyList()
)

// ==============================================================================
// 2. Pattern Scanner & 33 Setups Catalog Models
// ==============================================================================

data class ApiPatternMatch(
    val ticker: String,
    val companyName: String,
    val exchange: String = "",
    val market: String = "IN",
    val price: Double = 0.0,
    val dayChangePct: Double = 0.0,
    val pattern: String,
    val displayName: String = "",
    val patternScore: Int = 85,
    val pivot: Double = 0.0,
    val target: Double = 0.0,
    val stopLoss: Double = 0.0,
    val winRate: Int = 74,
    val reasons: List<String> = emptyList(),
    val logoUrl: String = ""
)

data class ApiPatternDefinition(
    val pattern: String,
    val displayName: String,
    val category: String = "BULLISH",
    val description: String = "",
    val winRate: Int = 72,
    val avgGain: Double = 18.5,
    val activeCount: Int = 0
)

data class ApiPatternBacktestStats(
    val pattern: String,
    val winRate: Double,
    val avgReturn: Double,
    val profitFactor: Double,
    val maxDrawdown: Double,
    val totalTrades: Int,
    val avgHoldingDays: Int
)

// ==============================================================================
// 3. Market Heatmap Treemap Models
// ==============================================================================

data class ApiHeatmapStock(
    val ticker: String,
    val name: String,
    val sector: String,
    val marketCap: Double,
    val price: Double,
    val changePct: Double,
    val peRatio: String = "--",
    val currencySymbol: String = "$"
)

data class ApiHeatmapSector(
    val sectorName: String,
    val totalMarketCap: Double,
    val avgChangePct: Double,
    val stocks: List<ApiHeatmapStock>
)

// ==============================================================================
// 4. Multi-Stock Comparison Models
// ==============================================================================

data class ApiComparisonStock(
    val ticker: String,
    val name: String,
    val price: Double,
    val changePct: Double,
    val peRatio: String,
    val pbRatio: String,
    val roe: String,
    val debtToEquity: String,
    val revenueGrowth: String,
    val quantScore: Int
)

data class ComparisonPoint(
    val date: String,
    val timestamp: Long,
    val price: Double,
    val percent: Double
)

data class ComparisonStat(
    val symbol: String,
    val periodReturn: Double,
    val peakGain: Double,
    val maxDrawdown: Double,
    val annualizedVolatility: Double
)

data class CorrelationPair(
    val symbol1: String,
    val symbol2: String,
    val coefficient: Double,
    val strength: String,
    val direction: String
)

data class FullComparisonData(
    val dates: List<String>,
    val series: Map<String, List<ComparisonPoint>>,
    val stats: List<ComparisonStat>,
    val correlations: List<CorrelationPair>,
    val stocks: List<ApiComparisonStock>
)

// ==============================================================================
// 5. INDmoney MCP Sync Status Models
// ==============================================================================

data class ApiIndmoneyStatus(
    val connected: Boolean = false,
    val status: String = "disconnected",
    val lastSync: String? = null,
    val holdingsCount: Int = 0,
    val indianStocksCount: Int = 0,
    val usStocksCount: Int = 0,
    val watchlistsCount: Int = 0,
    val totalInvested: Double = 0.0,
    val currentValue: Double = 0.0
)

data class ApiIndmoneySyncResponse(
    val success: Boolean = false,
    val indianStocksSynced: Int = 0,
    val usStocksSynced: Int = 0,
    val watchlistsSynced: Int = 0,
    val message: String = "",
    val error: String? = null
)
