package com.stocktics.data.api

/**
 * Curated Institutional Fallback Datasets for Market Treemap Heatmap.
 * Ensures the treemap visualization renders instantly without blank screens
 * when the cloud backend is cold-starting, rate-limited, or the device is offline.
 */
object MarketHeatmapFallback {

    fun getFallbackSectors(market: String): List<ApiHeatmapSector> {
        return if (market.equals("IN", ignoreCase = true)) {
            getNifty50Fallback()
        } else {
            getSp500Fallback()
        }
    }

    private fun getSp500Fallback(): List<ApiHeatmapSector> {
        val techStocks = listOf(
            ApiHeatmapStock("MSFT", "Microsoft Corp", "Technology", 3150000000000.0, 422.50, 1.35, "34.2", "$"),
            ApiHeatmapStock("AAPL", "Apple Inc", "Technology", 3420000000000.0, 229.10, 0.85, "33.8", "$"),
            ApiHeatmapStock("ORCL", "Oracle Corp", "Technology", 395000000000.0, 142.30, 2.45, "38.5", "$"),
            ApiHeatmapStock("CRM", "Salesforce Inc", "Technology", 295000000000.0, 302.10, 1.62, "42.1", "$"),
            ApiHeatmapStock("ADBE", "Adobe Inc", "Technology", 235000000000.0, 515.40, -0.42, "36.4", "$"),
            ApiHeatmapStock("ACN", "Accenture Plc", "Technology", 220000000000.0, 348.20, -0.65, "28.3", "$"),
            ApiHeatmapStock("CSCO", "Cisco Systems", "Technology", 215000000000.0, 53.40, 0.35, "18.2", "$"),
            ApiHeatmapStock("INTU", "Intuit Inc", "Technology", 185000000000.0, 652.80, 0.92, "55.4", "$")
        )

        val semiStocks = listOf(
            ApiHeatmapStock("NVDA", "NVIDIA Corp", "Semiconductors", 3120000000000.0, 126.90, 3.65, "48.2", "$"),
            ApiHeatmapStock("AVGO", "Broadcom Inc", "Semiconductors", 790000000000.0, 169.50, 2.15, "35.1", "$"),
            ApiHeatmapStock("AMD", "Advanced Micro Dev", "Semiconductors", 255000000000.0, 159.40, 1.84, "95.2", "$"),
            ApiHeatmapStock("QCOM", "Qualcomm Inc", "Semiconductors", 195000000000.0, 174.20, 0.72, "19.5", "$"),
            ApiHeatmapStock("TXN", "Texas Instruments", "Semiconductors", 188000000000.0, 203.10, -0.38, "31.2", "$"),
            ApiHeatmapStock("AMAT", "Applied Materials", "Semiconductors", 172000000000.0, 210.40, 1.70, "22.6", "$")
        )

        val commStocks = listOf(
            ApiHeatmapStock("GOOGL", "Alphabet Inc A", "Communication Services", 2140000000000.0, 173.80, 1.15, "24.5", "$"),
            ApiHeatmapStock("META", "Meta Platforms", "Communication Services", 1460000000000.0, 578.40, 2.65, "26.8", "$"),
            ApiHeatmapStock("DIS", "Walt Disney Co", "Communication Services", 182000000000.0, 99.20, -1.18, "38.2", "$"),
            ApiHeatmapStock("VZ", "Verizon Comm", "Communication Services", 172000000000.0, 41.50, -0.17, "9.8", "$")
        )

        val consumerCyclicStocks = listOf(
            ApiHeatmapStock("AMZN", "Amazon.com Inc", "Consumer Cyclical", 2010000000000.0, 191.20, 1.75, "41.2", "$"),
            ApiHeatmapStock("TSLA", "Tesla Inc", "Consumer Cyclical", 795000000000.0, 251.30, 3.82, "68.4", "$"),
            ApiHeatmapStock("HD", "Home Depot", "Consumer Cyclical", 385000000000.0, 390.10, -0.58, "25.1", "$"),
            ApiHeatmapStock("MCD", "McDonald's Corp", "Consumer Cyclical", 218000000000.0, 301.20, 0.45, "24.7", "$"),
            ApiHeatmapStock("BKNG", "Booking Holdings", "Consumer Cyclical", 132000000000.0, 3810.0, 0.72, "27.5", "$")
        )

        val financialStocks = listOf(
            ApiHeatmapStock("BRK-B", "Berkshire Hathaway", "Financial Services", 985000000000.0, 454.20, 0.32, "21.4", "$"),
            ApiHeatmapStock("JPM", "JPMorgan Chase", "Financial Services", 615000000000.0, 219.80, 1.45, "12.3", "$"),
            ApiHeatmapStock("V", "Visa Inc", "Financial Services", 555000000000.0, 279.40, 0.55, "29.8", "$"),
            ApiHeatmapStock("MA", "Mastercard Inc", "Financial Services", 445000000000.0, 478.10, 0.82, "33.4", "$"),
            ApiHeatmapStock("BAC", "Bank of America", "Financial Services", 312000000000.0, 40.50, 1.15, "13.8", "$")
        )

        val healthcareStocks = listOf(
            ApiHeatmapStock("LLY", "Eli Lilly & Co", "Healthcare", 865000000000.0, 915.20, 2.85, "78.4", "$"),
            ApiHeatmapStock("UNH", "UnitedHealth Group", "Healthcare", 545000000000.0, 588.30, 0.42, "26.5", "$"),
            ApiHeatmapStock("JNJ", "Johnson & Johnson", "Healthcare", 392000000000.0, 163.10, -0.28, "16.8", "$"),
            ApiHeatmapStock("ABBV", "AbbVie Inc", "Healthcare", 342000000000.0, 193.80, 1.24, "35.2", "$"),
            ApiHeatmapStock("MRK", "Merck & Co", "Healthcare", 292000000000.0, 115.40, -0.72, "14.5", "$"),
            ApiHeatmapStock("TMO", "Thermo Fisher", "Healthcare", 232000000000.0, 598.10, 0.54, "34.1", "$"),
            ApiHeatmapStock("AMGN", "Amgen Inc", "Healthcare", 176000000000.0, 327.20, 0.95, "19.8", "$"),
            ApiHeatmapStock("PFE", "Pfizer Inc", "Healthcare", 162000000000.0, 28.80, -1.14, "15.2", "$")
        )

        val consumerDefensiveStocks = listOf(
            ApiHeatmapStock("WMT", "Walmart Inc", "Consumer Defensive", 645000000000.0, 80.40, 1.25, "32.1", "$"),
            ApiHeatmapStock("PG", "Procter & Gamble", "Consumer Defensive", 402000000000.0, 172.10, 0.22, "26.4", "$"),
            ApiHeatmapStock("COST", "Costco Wholesale", "Consumer Defensive", 392000000000.0, 889.50, 1.54, "51.2", "$"),
            ApiHeatmapStock("PEP", "PepsiCo Inc", "Consumer Defensive", 242000000000.0, 175.20, -0.38, "24.5", "$")
        )

        val energyStocks = listOf(
            ApiHeatmapStock("XOM", "Exxon Mobil", "Energy", 465000000000.0, 116.40, 0.85, "14.2", "$"),
            ApiHeatmapStock("CVX", "Chevron Corp", "Energy", 282000000000.0, 152.10, 0.52, "13.8", "$"),
            ApiHeatmapStock("COP", "ConocoPhillips", "Energy", 142000000000.0, 123.50, 0.24, "12.5", "$")
        )

        val industrialStocks = listOf(
            ApiHeatmapStock("GE", "GE Aerospace", "Industrials", 212000000000.0, 187.20, 1.95, "38.2", "$"),
            ApiHeatmapStock("CAT", "Caterpillar Inc", "Industrials", 182000000000.0, 370.50, 1.12, "17.4", "$"),
            ApiHeatmapStock("HON", "Honeywell Intl", "Industrials", 136000000000.0, 209.80, -0.28, "22.5", "$")
        )

        return listOf(
            createSector("Technology", techStocks),
            createSector("Semiconductors", semiStocks),
            createSector("Consumer Cyclical", consumerCyclicStocks),
            createSector("Communication Services", commStocks),
            createSector("Financial Services", financialStocks),
            createSector("Healthcare", healthcareStocks),
            createSector("Consumer Defensive", consumerDefensiveStocks),
            createSector("Energy", energyStocks),
            createSector("Industrials", industrialStocks)
        ).sortedByDescending { it.totalMarketCap }
    }

    private fun getNifty50Fallback(): List<ApiHeatmapSector> {
        val bankingStocks = listOf(
            ApiHeatmapStock("HDFCBANK.NS", "HDFC Bank", "Banking & Finance", 12600000000000.0, 1685.20, 0.95, "18.5", "₹"),
            ApiHeatmapStock("ICICIBANK.NS", "ICICI Bank", "Banking & Finance", 8950000000000.0, 1275.40, 1.42, "17.8", "₹"),
            ApiHeatmapStock("SBIN.NS", "State Bank of India", "Banking & Finance", 7250000000000.0, 815.10, 1.85, "10.4", "₹"),
            ApiHeatmapStock("BAJFINANCE.NS", "Bajaj Finance", "Banking & Finance", 4350000000000.0, 7080.00, 2.15, "28.4", "₹"),
            ApiHeatmapStock("AXISBANK.NS", "Axis Bank", "Banking & Finance", 3850000000000.0, 1220.50, 0.75, "13.2", "₹"),
            ApiHeatmapStock("KOTAKBANK.NS", "Kotak Mahindra Bank", "Banking & Finance", 3650000000000.0, 1830.00, 0.45, "19.5", "₹"),
            ApiHeatmapStock("BAJAJFINSV.NS", "Bajaj Finserv", "Banking & Finance", 2950000000000.0, 1835.00, 1.35, "32.1", "₹"),
            ApiHeatmapStock("JIOFIN.NS", "Jio Financial Services", "Banking & Finance", 2150000000000.0, 348.50, 2.45, "115.0", "₹")
        )

        val itStocks = listOf(
            ApiHeatmapStock("TCS.NS", "Tata Consultancy Services", "Information Technology", 14900000000000.0, 4135.00, 1.15, "31.2", "₹"),
            ApiHeatmapStock("INFY.NS", "Infosys Ltd", "Information Technology", 7950000000000.0, 1898.20, 1.62, "27.5", "₹"),
            ApiHeatmapStock("HCLTECH.NS", "HCL Technologies", "Information Technology", 4850000000000.0, 1789.00, 0.85, "28.1", "₹"),
            ApiHeatmapStock("WIPRO.NS", "Wipro Ltd", "Information Technology", 2850000000000.0, 534.50, 0.55, "22.4", "₹"),
            ApiHeatmapStock("LTIM.NS", "LTIMindtree", "Information Technology", 1780000000000.0, 6010.00, 0.95, "34.5", "₹"),
            ApiHeatmapStock("TECHM.NS", "Tech Mahindra", "Information Technology", 1620000000000.0, 1660.00, 1.25, "38.2", "₹")
        )

        val energyStocks = listOf(
            ApiHeatmapStock("RELIANCE.NS", "Reliance Industries", "Energy & Oil", 19900000000000.0, 2950.00, 1.35, "27.4", "₹"),
            ApiHeatmapStock("NTPC.NS", "NTPC Ltd", "Energy & Oil", 4150000000000.0, 423.50, 0.75, "17.8", "₹"),
            ApiHeatmapStock("ONGC.NS", "Oil & Natural Gas Corp", "Energy & Oil", 3750000000000.0, 298.20, 2.05, "7.4", "₹"),
            ApiHeatmapStock("POWERGRID.NS", "Power Grid Corp", "Energy & Oil", 3250000000000.0, 342.80, 0.35, "18.5", "₹"),
            ApiHeatmapStock("COALINDIA.NS", "Coal India", "Energy & Oil", 3150000000000.0, 508.40, 1.55, "7.9", "₹"),
            ApiHeatmapStock("BPCL.NS", "Bharat Petroleum", "Energy & Oil", 1520000000000.0, 352.10, -0.42, "5.8", "₹")
        )

        val autoStocks = listOf(
            ApiHeatmapStock("MARUTI.NS", "Maruti Suzuki", "Automobile", 3950000000000.0, 12520.00, 1.25, "28.5", "₹"),
            ApiHeatmapStock("M&M.NS", "Mahindra & Mahindra", "Automobile", 3650000000000.0, 3010.00, 2.85, "31.4", "₹"),
            ApiHeatmapStock("TATAMOTORS.NS", "Tata Motors", "Automobile", 3450000000000.0, 948.20, 1.72, "11.2", "₹"),
            ApiHeatmapStock("BAJAJ-AUTO.NS", "Bajaj Auto", "Automobile", 2850000000000.0, 10280.00, 0.65, "34.1", "₹"),
            ApiHeatmapStock("EICHERMOT.NS", "Eicher Motors", "Automobile", 1320000000000.0, 4880.00, 1.45, "32.8", "₹")
        )

        val fmcgStocks = listOf(
            ApiHeatmapStock("HINDUNILVR.NS", "Hindustan Unilever", "Consumer FMCG", 6750000000000.0, 2855.00, 0.42, "62.4", "₹"),
            ApiHeatmapStock("ITC.NS", "ITC Ltd", "Consumer FMCG", 6250000000000.0, 508.20, 0.65, "29.5", "₹"),
            ApiHeatmapStock("TITAN.NS", "Titan Company", "Consumer FMCG", 3350000000000.0, 3735.00, 1.55, "84.2", "₹"),
            ApiHeatmapStock("NESTLEIND.NS", "Nestle India", "Consumer FMCG", 2420000000000.0, 2525.00, -0.25, "74.1", "₹"),
            ApiHeatmapStock("ASIANPAINT.NS", "Asian Paints", "Consumer FMCG", 3120000000000.0, 3255.00, -0.52, "58.4", "₹"),
            ApiHeatmapStock("BRITANNIA.NS", "Britannia Industries", "Consumer FMCG", 1420000000000.0, 5890.00, 0.85, "56.8", "₹")
        )

        val infraMetalsStocks = listOf(
            ApiHeatmapStock("LT.NS", "Larsen & Toubro", "Infrastructure & Capital Goods", 5150000000000.0, 3735.00, 1.55, "36.5", "₹"),
            ApiHeatmapStock("ULTRACEMCO.NS", "UltraTech Cement", "Infrastructure & Capital Goods", 3350000000000.0, 11520.00, 0.85, "42.1", "₹"),
            ApiHeatmapStock("ADANIENT.NS", "Adani Enterprises", "Infrastructure & Capital Goods", 3550000000000.0, 3110.00, 2.15, "95.0", "₹"),
            ApiHeatmapStock("ADANIPORTS.NS", "Adani Ports & SEZ", "Infrastructure & Capital Goods", 3180000000000.0, 1475.00, 1.15, "34.5", "₹"),
            ApiHeatmapStock("JSWSTEEL.NS", "JSW Steel", "Metals & Mining", 2480000000000.0, 1020.00, 1.25, "26.4", "₹"),
            ApiHeatmapStock("BEL.NS", "Bharat Electronics", "Defense & Aerospace", 2250000000000.0, 306.50, 3.25, "48.2", "₹"),
            ApiHeatmapStock("HAL.NS", "Hindustan Aeronautics", "Defense & Aerospace", 3150000000000.0, 4690.00, 2.55, "39.5", "₹"),
            ApiHeatmapStock("TATASTEEL.NS", "Tata Steel", "Metals & Mining", 2080000000000.0, 167.50, 1.85, "45.0", "₹"),
            ApiHeatmapStock("HINDALCO.NS", "Hindalco Industries", "Metals & Mining", 1620000000000.0, 722.00, 2.35, "15.2", "₹")
        )

        val pharmaStocks = listOf(
            ApiHeatmapStock("SUNPHARMA.NS", "Sun Pharma", "Healthcare & Pharma", 4350000000000.0, 1810.00, 1.05, "38.5", "₹"),
            ApiHeatmapStock("DIVISLAB.NS", "Divi's Laboratories", "Healthcare & Pharma", 1480000000000.0, 5490.00, 1.65, "68.2", "₹"),
            ApiHeatmapStock("CIPLA.NS", "Cipla Ltd", "Healthcare & Pharma", 1320000000000.0, 1625.00, 0.55, "28.4", "₹"),
            ApiHeatmapStock("DRREDDY.NS", "Dr. Reddy's Labs", "Healthcare & Pharma", 1120000000000.0, 6620.00, 0.45, "20.1", "₹")
        )

        return listOf(
            createSector("Energy & Oil", energyStocks),
            createSector("Information Technology", itStocks),
            createSector("Banking & Finance", bankingStocks),
            createSector("Consumer FMCG", fmcgStocks),
            createSector("Infrastructure & Capital Goods", infraMetalsStocks),
            createSector("Automobile", autoStocks),
            createSector("Healthcare & Pharma", pharmaStocks)
        ).sortedByDescending { it.totalMarketCap }
    }

    private fun createSector(name: String, stocks: List<ApiHeatmapStock>): ApiHeatmapSector {
        val totalCap = stocks.sumOf { it.marketCap }
        val avgChange = if (stocks.isNotEmpty()) stocks.map { it.changePct }.average() else 0.0
        return ApiHeatmapSector(
            sectorName = name,
            totalMarketCap = totalCap,
            avgChangePct = Math.round(avgChange * 100.0) / 100.0,
            stocks = stocks.sortedByDescending { it.marketCap }
        )
    }
}
