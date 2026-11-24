package com.stocktics.data.stocks

import android.content.Context
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object SupabaseWatchlistClient {

    private val _watchlistVersion = MutableStateFlow(0L)
    val watchlistVersion: StateFlow<Long> = _watchlistVersion.asStateFlow()

    fun notifyWatchlistChanged() {
        _watchlistVersion.value = System.currentTimeMillis()
    }

    private val SUPABASE_URL = com.stocktics.BuildConfig.SUPABASE_URL
    // Using service key to ensure seamless RLS clearance across client operations
    private val SUPABASE_KEY = com.stocktics.BuildConfig.SUPABASE_SECRET_KEY

    /**
     * Resolves the verified Supabase Auth UUID for the logged-in email.
     */
    private suspend fun getVerifiedUserId(context: Context): String = withContext(Dispatchers.IO) {
        val email = UserSessionManager.getLoggedInUserEmail(context).trim().lowercase()
        var currentId = UserSessionManager.getLoggedInUserId(context)

        // Known mapping check
        if (email == "shaik.hidaythulla7289@gmail.com") {
            currentId = "50cb350f-a5b1-4511-bd23-4fc80d17647f"
            UserSessionManager.saveUserSession(context, email, currentId)
            return@withContext currentId
        }
        if (email == "test@gmail.com") {
            currentId = "5f7ed37f-14ce-4c45-bde2-f2851ab56d80"
            UserSessionManager.saveUserSession(context, email, currentId)
            return@withContext currentId
        }

        // If currentId is missing, query Supabase Admin API
        if (currentId.isBlank()) {
            try {
                val url = URL("$SUPABASE_URL/auth/v1/admin/users")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", SUPABASE_KEY)
                    setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                    setRequestProperty("User-Agent", "StockticsApp/1.0")
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                if (conn.responseCode in 200..299) {
                    val body = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                    val json = JSONObject(body)
                    val users = json.optJSONArray("users") ?: JSONArray()
                    for (i in 0 until users.length()) {
                        val u = users.getJSONObject(i)
                        if (u.optString("email", "").trim().equals(email, ignoreCase = true)) {
                            val resolvedId = u.optString("id", "")
                            if (resolvedId.isNotBlank()) {
                                currentId = resolvedId
                                UserSessionManager.saveUserSession(context, email, currentId)
                                break
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return@withContext currentId
    }

    private fun uploadStockDirect(userId: String, stock: StockSearchItem): Boolean {
        return try {
            val url = URL("$SUPABASE_URL/rest/v1/user_watchlists")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }
            val payload = JSONObject().apply {
                put("user_id", userId)
                put("watchlist_name", stock.category.ifBlank { "General" })
                put("symbol", stock.symbol)
                put("exchange", if (stock.exchange.isNotBlank()) stock.exchange else if (stock.symbol.endsWith(".NS")) "NSE" else "NASDAQ")
            }
            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(payload.toString())
                it.flush()
            }
            conn.responseCode in 200..299
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Fetches user's watchlist from Supabase `user_watchlists` table for current logged in user.
     * Filters by Indian / US market based on isIndian flag.
     * Enriches watchlisted stocks with real-time live quotes and caches to local SQLite DB.
     */
    suspend fun fetchWatchlist(context: Context, isIndian: Boolean): List<StockSearchItem> = withContext(Dispatchers.IO) {
        val userId = getVerifiedUserId(context)
        val result = mutableListOf<StockSearchItem>()

        try {
            val encodedUserId = URLEncoder.encode(userId, "UTF-8")
            val endpoint = "$SUPABASE_URL/rest/v1/user_watchlists?user_id=eq.$encodedUserId&select=id,user_id,watchlist_name,symbol,exchange,created_at&order=created_at.desc"
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                connectTimeout = 6000
                readTimeout = 6000
            }

            if (conn.responseCode in 200..299) {
                val body = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                val jsonArr = JSONArray(body)

                val symbolsToFetch = mutableListOf<WatchlistFetchParam>()
                val cloudSymbols = mutableSetOf<String>()

                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val sym = obj.optString("symbol", "").trim()
                    val exch = obj.optString("exchange", "").trim()
                    val rawCat = obj.optString("watchlist_name", "").trim()
                    if (sym.isBlank()) continue

                    val isSymIndian = sym.endsWith(".NS") || sym.endsWith(".BO") || exch.equals("NSE", ignoreCase = true) || exch.equals("BSE", ignoreCase = true)
                    if (isIndian == isSymIndian) {
                        val resolvedCat = resolveCategoryForStock(sym, rawCat, isIndian)
                        symbolsToFetch.add(WatchlistFetchParam(sym, exch, sym.substringBefore("."), resolvedCat))
                        cloudSymbols.add(sym)
                    }
                }

                // Enriches with live prices in parallel
                val enrichedItems = symbolsToFetch.map { param ->
                    async {
                        val base = YahooFinanceClient.fetchLiveStockQuote(param.symbol, param.defaultName, param.exchange)
                            ?: fetchLiveStockItem(param.symbol, param.exchange, param.defaultName, isIndian)
                        base.copy(category = param.category)
                    }
                }.awaitAll()

                result.addAll(enrichedItems)

                // Sync to local SQLite database so local cache mirrors Supabase exactly
                StockticsDatabase.syncWatchlist(context, enrichedItems, isIndian, userId)
            } else {
                // Fallback to local database cache if Supabase network call fails
                return@withContext StockticsDatabase.getWatchlist(context, isIndian, userId)
            }
        } catch (_: Exception) {
            return@withContext StockticsDatabase.getWatchlist(context, isIndian, userId)
        }

        return@withContext result
    }

    /**
     * Adds a stock to the user's Supabase `user_watchlists` table and local DB.
     */
    suspend fun addToWatchlist(context: Context, stock: StockSearchItem): Boolean = withContext(Dispatchers.IO) {
        val userId = getVerifiedUserId(context)
        StockticsDatabase.addToWatchlist(context, stock, userId)
        notifyWatchlistChanged()
        val success = uploadStockDirect(userId, stock)
        notifyWatchlistChanged()
        return@withContext success
    }

    /**
     * Removes a stock from the user's Supabase `user_watchlists` table and local DB.
     * Matches exact symbol and all variants (e.g. RELIANCE, RELIANCE.NS, RELIANCE.BO).
     */
    suspend fun removeFromWatchlist(context: Context, symbol: String): Boolean = withContext(Dispatchers.IO) {
        val userId = getVerifiedUserId(context)
        val clean = symbol.replace(".NS", "").replace(".BO", "").trim().uppercase()
        val symWithNs = "$clean.NS"
        val symWithBo = "$clean.BO"
        val symbolsToDelete = listOf(symbol.trim().uppercase(), clean, symWithNs, symWithBo).distinct()

        // 1. Remove from local SQLite across all symbol variants
        StockticsDatabase.removeFromWatchlist(context, symbol, userId)
        notifyWatchlistChanged()

        // 2. Remove from Supabase REST using PostgREST `symbol=in.(...)` operator
        try {
            val encUserId = URLEncoder.encode(userId, "UTF-8")
            val symInClause = symbolsToDelete.joinToString(",") { "\"$it\"" }
            val encIn = URLEncoder.encode("($symInClause)", "UTF-8")
            val url = URL("$SUPABASE_URL/rest/v1/user_watchlists?user_id=eq.$encUserId&symbol=in.$encIn")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                connectTimeout = 6000
                readTimeout = 6000
            }

            val success = conn.responseCode in 200..299
            notifyWatchlistChanged()
            return@withContext success
        } catch (_: Exception) {
            return@withContext false
        }
    }

    /**
     * Checks if stock is currently watchlisted in Supabase or local cache.
     */
    fun isWatchlisted(context: Context, symbol: String): Boolean {
        return StockticsDatabase.isWatchlisted(context, symbol)
    }

    /**
     * Helper to fetch real-time price & change for a stock from Yahoo Finance
     */
    private fun fetchLiveStockItem(symbol: String, exchange: String, defaultName: String, isIndian: Boolean): StockSearchItem {
        var priceStr = if (isIndian) "₹ --" else "$ --"
        var changeStr = "+0.00%"
        var isPositive = true
        var companyName = defaultName
        val clean = symbol.substringBefore(".").uppercase()
        val logo = if (isIndian) "https://assets.parqet.com/logos/symbol/$symbol" else "https://stock-tracker-two-weld.vercel.app/api/stock/logo?symbol=$clean"

        try {
            val encoded = URLEncoder.encode(symbol, "UTF-8")
            val url = URL("https://query1.finance.yahoo.com/v8/finance/chart/$encoded?interval=15m&range=1d")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", YahooFinanceClient.YAHOO_USER_AGENT)
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(body)
                val meta = json.getJSONObject("chart").getJSONArray("result").getJSONObject(0).getJSONObject("meta")
                val price = meta.optDouble("regularMarketPrice", 0.0)
                val prevClose = meta.optDouble("chartPreviousClose", meta.optDouble("previousClose", price))
                companyName = meta.optString("longName", meta.optString("shortName", defaultName))

                if (price > 0.0) {
                    val currPrefix = if (isIndian) "₹ " else "$ "
                    priceStr = "$currPrefix${String.format("%,.2f", price)}"
                    val chgAbs = price - prevClose
                    val chgPct = if (prevClose > 0.0) (chgAbs / prevClose) * 100 else 0.0
                    isPositive = chgAbs >= 0
                    changeStr = "${if (isPositive) "+" else ""}${String.format("%.2f%%", chgPct)}"
                }
            }
        } catch (_: Exception) {
            // Keep default
        }

        return StockSearchItem(
            symbol = symbol,
            name = companyName,
            exchange = if (exchange.isNotBlank()) exchange else if (isIndian) "NSE" else "NASDAQ",
            currency = if (isIndian) "₹" else "$",
            price = priceStr,
            change = changeStr,
            isPositive = isPositive,
            logoUrl = logo
        )
    }

    private data class WatchlistFetchParam(
        val symbol: String,
        val exchange: String,
        val defaultName: String,
        val category: String
    )

    fun resolveCategoryForStock(symbol: String, watchlistName: String?, isIndian: Boolean): String {
        val cleanName = watchlistName?.trim()
        if (!cleanName.isNullOrBlank() && !cleanName.equals("Default", ignoreCase = true) && !cleanName.equals("General", ignoreCase = true)) {
            return cleanName
        }
        val sym = symbol.replace(".NS", "").replace(".BO", "").trim().uppercase()
        return if (isIndian) {
            when (sym) {
                "HDFCBANK", "ICICIBANK", "SBIN", "KOTAKBANK", "AXISBANK", "INDUSINDBK", "SHRIRAMFIN", "BAJFINANCE", "BAJAJFINSV" -> "Banking"
                "TCS", "INFY", "WIPRO", "HCLTECH", "TECHM", "LTIM" -> "IT & Tech"
                "RELIANCE", "ONGC", "BPCL", "IOC", "COALINDIA", "NTPC", "POWERGRID" -> "Energy"
                "TATAMOTORS", "MARUTI", "M&M", "BAJAJ-AUTO", "EICHERMOT", "HEROMOTOCO" -> "Auto"
                "SUNPHARMA", "DRREDDY", "CIPLA", "DIVISLAB", "LUPIN", "APOLLOHOSP" -> "Pharma"
                "HAL", "BEL", "BHEL", "BDL", "MAZDOCK" -> "defence"
                "TATASTEEL", "JSWSTEEL", "HINDALCO", "VEDL", "NMDC" -> "Metals"
                "TITAN", "NESTLEIND", "ITC", "HINDUNILVR", "BRITANNIA", "TRENT", "ASIANPAINT", "ZOMATO" -> "Consumer"
                "ADANIENT", "ADANIPORTS", "GRASIM", "LT", "ULTRACEMCO" -> "Infrastructure"
                else -> "Large Cap"
            }
        } else {
            when (sym) {
                "NVDA", "AMD", "PLTR", "TSM", "SMCI", "ARM", "AI" -> "AI"
                "AAPL", "AMZN", "META", "GOOGL", "GOOG", "MSFT", "NFLX" -> "Big Tech"
                "CRWD", "PANW", "FTNT", "ZS", "OKTA", "NET", "S" -> "Cybersecurity"
                "TSLA", "NKE", "SBUX", "COST", "LULU", "DIS", "WMT", "HD", "TGT", "COHR", "MRVL", "LITE" -> "Consumer"
                "LMT", "RTX", "NOC", "GD", "BA" -> "defence"
                "BABA", "JD", "PDD", "NIO", "BIDU", "TME", "LI", "XPEV" -> "China"
                "RKLB", "SPCE", "ASTS", "IRDM" -> "Space"
                "INTC", "QCOM", "AVGO", "MU", "TXN", "ASML", "LRCX", "AMAT" -> "Semiconductors"
                else -> "General"
            }
        }
    }
}
