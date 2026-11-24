package com.stocktics.data.portfolio

import android.content.Context
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.session.UserSessionManager
import com.stocktics.data.stocks.YahooFinanceClient
import kotlinx.coroutines.Dispatchers
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object SupabasePortfolioClient {

    private val _portfolioVersion = MutableStateFlow(0L)
    val portfolioVersion: StateFlow<Long> = _portfolioVersion.asStateFlow()

    fun notifyPortfolioChanged() {
        _portfolioVersion.value = System.currentTimeMillis()
    }

    private val SUPABASE_URL = com.stocktics.BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = com.stocktics.BuildConfig.SUPABASE_SECRET_KEY

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /**
     * Resolves the verified Supabase Auth UUID for the logged-in email.
     */
    suspend fun getVerifiedUserId(context: Context): String = withContext(Dispatchers.IO) {
        val email = UserSessionManager.getLoggedInUserEmail(context).trim().lowercase()
        var currentId = UserSessionManager.getLoggedInUserId(context)

        if (email == "vinayakpatil.657@gmail.com") {
            currentId = "4c7e93ec-f3bc-4545-b8ab-e6a24b9c61c7"
            UserSessionManager.saveUserSession(context, email, currentId)
            return@withContext currentId
        }
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

        // If currentId is missing or empty, resolve from Supabase Admin API
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
        currentId
    }

    /**
     * 1. Synchronizes a new BUY position to Supabase:
     *    - Inserts holding into public.user_holdings
     *    - Inserts transaction into public.user_transactions
     */
    suspend fun syncBuyPositionToCloud(
        context: Context,
        holding: PortfolioHolding
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getVerifiedUserId(context)
            if (userId.isBlank()) return@withContext false

            // 1. Insert into user_holdings
            val urlHoldings = URL("$SUPABASE_URL/rest/v1/user_holdings")
            val connHoldings = (urlHoldings.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Prefer", "return=representation")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val holdingBody = JSONObject().apply {
                put("user_id", userId)
                put("symbol", holding.symbol.trim().uppercase())
                put("exchange", holding.exchange)
                put("entry_price", holding.entryPrice)
                put("entry_date", holding.entryDate)
                put("qty", holding.qty)
                put("stop_loss_pct", holding.stopLossPct / 100.0)
                put("target_pct", holding.targetPct / 100.0)
                put("trailing_stop_pct", holding.trailingStopPct / 100.0)
                put("max_hold_days", holding.maxHoldDays)
                put("status", "open")
                put("time_to_leave_score", holding.timeToLeaveScore)
                put("exit_verdict", holding.exitVerdict)
            }

            OutputStreamWriter(connHoldings.outputStream, "UTF-8").use { it.write(holdingBody.toString()) }
            val codeHoldings = connHoldings.responseCode

            var cloudHoldingId: String? = null
            if (codeHoldings in 200..299) {
                val resp = BufferedReader(InputStreamReader(connHoldings.inputStream, "UTF-8")).use { it.readText() }
                try {
                    val arr = JSONArray(resp)
                    if (arr.length() > 0) {
                        cloudHoldingId = arr.getJSONObject(0).optString("id", "").takeIf { it.isNotBlank() }
                    }
                } catch (_: Exception) {}
            }

            // 2. Insert into user_transactions
            val urlTx = URL("$SUPABASE_URL/rest/v1/user_transactions")
            val connTx = (urlTx.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Prefer", "return=minimal")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val txBody = JSONObject().apply {
                put("user_id", userId)
                if (!cloudHoldingId.isNullOrBlank()) {
                    put("holding_id", cloudHoldingId)
                }
                put("symbol", holding.symbol.trim().uppercase())
                put("exchange", holding.exchange)
                put("tx_type", "BUY")
                put("price", holding.entryPrice)
                put("qty", holding.qty)
                put("total_amount", holding.entryPrice * holding.qty)
                put("realized_pnl", 0.0)
                put("tx_date", holding.entryDate)
            }

            OutputStreamWriter(connTx.outputStream, "UTF-8").use { it.write(txBody.toString()) }
            val codeTx = connTx.responseCode

            codeHoldings in 200..299 || codeTx in 200..299
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 2. Synchronizes a SELL / EXIT position to Supabase:
     *    - Updates public.user_holdings (full exit marks status='closed', partial exit decrements qty)
     *    - Inserts SELL record into public.user_transactions with realized P&L
     */
    suspend fun syncExitPositionToCloud(
        context: Context,
        symbol: String,
        exitPrice: Double,
        exitQty: Double = 0.0,
        exitReason: String = "manual",
        realizedPnl: Double = 0.0,
        isFullExit: Boolean = true,
        remainingQty: Double = 0.0,
        exitDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        exchange: String = "NSE"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getVerifiedUserId(context)
            if (userId.isBlank()) return@withContext false

            val encodedSymbol = URLEncoder.encode(symbol.trim().uppercase(), "UTF-8")

            // 1. Update user_holdings
            val urlHoldings = URL("$SUPABASE_URL/rest/v1/user_holdings?user_id=eq.$userId&symbol=eq.$encodedSymbol&status=eq.open")
            val connHoldings = (urlHoldings.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Prefer", "return=representation")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val holdingBody = JSONObject()
            if (isFullExit) {
                holdingBody.put("status", "closed")
                holdingBody.put("exit_price", exitPrice)
                holdingBody.put("exit_date", exitDate)
                holdingBody.put("exit_reason", exitReason)
            } else {
                holdingBody.put("qty", remainingQty)
            }
            holdingBody.put("updated_at", getCurrentIsoTimestamp())

            OutputStreamWriter(connHoldings.outputStream, "UTF-8").use { it.write(holdingBody.toString()) }
            val codeHoldings = connHoldings.responseCode

            var cloudHoldingId: String? = null
            if (codeHoldings in 200..299) {
                val resp = BufferedReader(InputStreamReader(connHoldings.inputStream, "UTF-8")).use { it.readText() }
                try {
                    val arr = JSONArray(resp)
                    if (arr.length() > 0) {
                        cloudHoldingId = arr.getJSONObject(0).optString("id", "").takeIf { it.isNotBlank() }
                    }
                } catch (_: Exception) {}
            }

            // 2. Insert SELL record in user_transactions
            val urlTx = URL("$SUPABASE_URL/rest/v1/user_transactions")
            val connTx = (urlTx.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Prefer", "return=minimal")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val effectiveQty = if (exitQty > 0) exitQty else 1.0
            val txBody = JSONObject().apply {
                put("user_id", userId)
                if (!cloudHoldingId.isNullOrBlank()) {
                    put("holding_id", cloudHoldingId)
                }
                put("symbol", symbol.trim().uppercase())
                put("exchange", exchange)
                put("tx_type", "SELL")
                put("price", exitPrice)
                put("qty", effectiveQty)
                put("total_amount", exitPrice * effectiveQty)
                put("realized_pnl", realizedPnl)
                put("tx_date", exitDate)
            }

            OutputStreamWriter(connTx.outputStream, "UTF-8").use { it.write(txBody.toString()) }
            val codeTx = connTx.responseCode

            codeHoldings in 200..299 || codeTx in 200..299
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 3. Synchronizes an EDITED position to Supabase:
     *    - Updates public.user_holdings entry_price, qty, stop_loss_pct, target_pct, max_hold_days
     *    - Updates public.user_transactions BUY transaction
     */
    suspend fun syncUpdatePositionToCloud(
        context: Context,
        holding: PortfolioHolding
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getVerifiedUserId(context)
            if (userId.isBlank()) return@withContext false

            val encodedSymbol = URLEncoder.encode(holding.symbol.trim().uppercase(), "UTF-8")

            // 1. PATCH user_holdings
            val urlHoldings = URL("$SUPABASE_URL/rest/v1/user_holdings?user_id=eq.$userId&symbol=eq.$encodedSymbol&status=eq.open")
            val connHoldings = (urlHoldings.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Prefer", "return=minimal")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val holdingBody = JSONObject().apply {
                put("entry_price", holding.entryPrice)
                put("qty", holding.qty)
                put("stop_loss_pct", holding.stopLossPct / 100.0)
                put("target_pct", holding.targetPct / 100.0)
                put("trailing_stop_pct", holding.trailingStopPct / 100.0)
                put("max_hold_days", holding.maxHoldDays)
                put("updated_at", getCurrentIsoTimestamp())
            }

            OutputStreamWriter(connHoldings.outputStream, "UTF-8").use { it.write(holdingBody.toString()) }
            val codeHoldings = connHoldings.responseCode

            // 2. PATCH user_transactions original BUY
            try {
                val urlTx = URL("$SUPABASE_URL/rest/v1/user_transactions?user_id=eq.$userId&symbol=eq.$encodedSymbol&tx_type=eq.BUY")
                val connTx = (urlTx.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    setRequestProperty("apikey", SUPABASE_KEY)
                    setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Prefer", "return=minimal")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val txBody = JSONObject().apply {
                    put("price", holding.entryPrice)
                    put("qty", holding.qty)
                    put("total_amount", holding.entryPrice * holding.qty)
                }
                OutputStreamWriter(connTx.outputStream, "UTF-8").use { it.write(txBody.toString()) }
                connTx.responseCode
            } catch (_: Exception) {}

            codeHoldings in 200..299
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 4. Deletes a holding from Supabase user_holdings and user_transactions
     */
    suspend fun deleteHoldingFromCloud(
        context: Context,
        symbol: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getVerifiedUserId(context)
            if (userId.isBlank()) return@withContext false

            val encodedSymbol = URLEncoder.encode(symbol.trim().uppercase(), "UTF-8")
            val url = URL("$SUPABASE_URL/rest/v1/user_holdings?user_id=eq.$userId&symbol=eq.$encodedSymbol")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                connectTimeout = 8000
                readTimeout = 8000
            }
            val code = conn.responseCode
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 5. Fetches all active holdings from Supabase and syncs them into local SQLite
     */
    suspend fun fetchHoldingsFromCloud(context: Context): List<PortfolioHolding> = withContext(Dispatchers.IO) {
        val list = mutableListOf<PortfolioHolding>()
        try {
            val userId = getVerifiedUserId(context)
            if (userId.isBlank()) return@withContext list

            val url = URL("$SUPABASE_URL/rest/v1/user_holdings?user_id=eq.$userId&status=eq.open&select=*&order=created_at.desc")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (conn.responseCode in 200..299) {
                val body = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                val arr = JSONArray(body)
                val holdingMap = LinkedHashMap<String, PortfolioHolding>()

                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val rawSymbol = obj.optString("symbol", "").trim().uppercase()
                    if (rawSymbol.isBlank()) continue

                    // Exclude internal broker codes like STOCK1234 or MF1234
                    val lower = rawSymbol.lowercase().replace(".ns", "").replace(".bo", "")
                    if (lower.startsWith("stock") && lower.drop(5).all { it.isDigit() }) continue
                    if (lower.startsWith("mf") && lower.drop(2).all { it.isDigit() }) continue

                    val exch = obj.optString("exchange", if (rawSymbol.endsWith(".NS")) "NSE" else if (rawSymbol.endsWith(".BO")) "BSE" else "NASDAQ")
                    val isIndian = exch == "NSE" || exch == "BSE" || rawSymbol.endsWith(".NS") || rawSymbol.endsWith(".BO")

                    // Strictly only sync holdings directly on INDmoney broker; exclude external tracked brokers like Zerodha
                    val extBroker = obj.optString("external_broker", "").trim().uppercase()
                    val externalBrokers = setOf("ZERODHA", "GROWW", "UPSTOX", "ANGEL", "ANGELONE", "ANGEL ONE", "KITE", "DHAN", "ICICI", "HDFC", "KOTAK", "MOTILAL", "SHAREKHAN", "5PAISA", "PAYTM_MONEY", "PAYTM")
                    if (isIndian && extBroker in externalBrokers) continue

                    val rawEntryPrice = obj.optDouble("entry_price", 0.0)
                    val qty = obj.optDouble("qty", 1.0)
                    val investedVal = obj.optDouble("invested_value", 0.0)
                    val currentVal = obj.optDouble("current_value", 0.0)
                    val entryPrice = if (rawEntryPrice > 0) rawEntryPrice else if (qty > 0 && investedVal > 0) investedVal / qty else 0.0
                    val currentPrice = if (qty > 0 && currentVal > 0) currentVal / qty else entryPrice
                    val rawPrevClose = obj.optDouble("prev_close", 0.0)
                    val prevClose = if (rawPrevClose > 0) rawPrevClose else currentPrice

                    val slPct = obj.optDouble("stop_loss_pct", 0.05) * 100.0
                    val tgtPct = obj.optDouble("target_pct", 0.15) * 100.0
                    val trailPct = obj.optDouble("trailing_stop_pct", 0.04) * 100.0
                    val maxDays = obj.optInt("max_hold_days", 60)
                    val status = obj.optString("status", "open")
                    val ttl = obj.optInt("time_to_leave_score", 20)
                    val verdict = obj.optString("exit_verdict", "Safe Hold")
                    val exitPrice = obj.optDouble("exit_price", 0.0)
                    val exitDate = obj.optString("exit_date", "")
                    val exitReason = obj.optString("exit_reason", "")
                    val entryDate = obj.optString("entry_date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

                    val friendlyName = when (rawSymbol) {
                        "539288.BO", "539288" -> "AVI Polymers Ltd"
                        "TATSILV.NS" -> "Tata Silver ETF"
                        "TCS.NS" -> "Tata Consultancy Services"
                        "INFY.NS" -> "Infosys Ltd"
                        "AAVAS.NS" -> "AAVAS Financiers Ltd"
                        "HIMATSEIDE.NS" -> "Himatsingka Seide Ltd"
                        "SENCO.NS" -> "Senco Gold Ltd"
                        else -> rawSymbol.replace(".NS", "").replace(".BO", "")
                    }

                    val holding = PortfolioHolding(
                        id = (i + 1).toLong(),
                        userId = userId,
                        symbol = rawSymbol,
                        name = friendlyName,
                        exchange = exch,
                        currency = if (isIndian) "₹" else "$",
                        market = if (isIndian) "IN" else "US",
                        entryPrice = entryPrice,
                        currentPrice = currentPrice,
                        previousClose = prevClose,
                        qty = qty,
                        entryDate = entryDate,
                        stopLossPct = slPct,
                        targetPct = tgtPct,
                        trailingStopPct = trailPct,
                        maxHoldDays = maxDays,
                        status = status,
                        timeToLeaveScore = ttl,
                        exitVerdict = verdict,
                        exitPrice = exitPrice,
                        exitDate = exitDate,
                        exitReason = exitReason
                    )

                    // Overwrite duplicate records so they never compound quantities into inflated lakhs
                    holdingMap[rawSymbol] = holding
                }
                list.addAll(holdingMap.values)

                // Enrich with real-time live quotes for all holdings
                if (list.isNotEmpty()) {
                    val symbols = list.map { it.symbol }
                    val quotes = try {
                        YahooFinanceClient.fetchHoldingPricesBatch(symbols)
                    } catch (_: Exception) {
                        emptyMap()
                    }

                    val enrichedList = list.map { h ->
                        val q = quotes[h.symbol] ?: quotes[h.symbol.replace(".NS", "").replace(".BO", "")]
                        if (q != null && q.currentPrice > 0) {
                            h.copy(
                                currentPrice = q.currentPrice,
                                previousClose = if (q.previousClose > 0) q.previousClose else q.currentPrice
                            )
                        } else {
                            h
                        }
                    }

                    list.clear()
                    list.addAll(enrichedList)
                }
                StockticsDatabase.syncHoldings(context, list, userId)
            }
        } catch (_: Exception) {}
        list
    }

    /**
     * Refreshes real-time quotes for local active holdings.
     */
    suspend fun refreshLiveHoldingPrices(context: Context): List<PortfolioHolding> = withContext(Dispatchers.IO) {
        val userId = getVerifiedUserId(context)
        val localHoldings = StockticsDatabase.getActiveHoldings(context, userId)
        if (localHoldings.isEmpty()) return@withContext emptyList()

        val symbols = localHoldings.map { it.symbol }
        val quotes = YahooFinanceClient.fetchHoldingPricesBatch(symbols)
        if (quotes.isNotEmpty()) {
            val priceMap = quotes.mapValues { it.value.currentPrice }
            val prevCloseMap = quotes.mapValues { it.value.previousClose }
            StockticsDatabase.updateHoldingPrices(context, priceMap, prevCloseMap, userId)
        }
        StockticsDatabase.getActiveHoldings(context, userId)
    }

    /**
     * 6. Fetches transaction history from Supabase and syncs them into local SQLite
     */
    suspend fun fetchTransactionsFromCloud(context: Context): List<PortfolioTransaction> = withContext(Dispatchers.IO) {
        val list = mutableListOf<PortfolioTransaction>()
        try {
            val userId = getVerifiedUserId(context)
            if (userId.isBlank()) return@withContext list

            val url = URL("$SUPABASE_URL/rest/v1/user_transactions?user_id=eq.$userId&select=*&order=tx_date.desc")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (conn.responseCode in 200..299) {
                val body = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                val arr = JSONArray(body)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val rawSymbol = obj.optString("symbol", "").trim().uppercase()
                    if (rawSymbol.isBlank()) continue
                    val exch = obj.optString("exchange", if (rawSymbol.endsWith(".NS")) "NSE" else "NASDAQ")
                    val txType = obj.optString("tx_type", "BUY")
                    val price = obj.optDouble("price", 0.0)
                    val qty = obj.optDouble("qty", 1.0)
                    val totalAmount = obj.optDouble("total_amount", price * qty)
                    val pnl = obj.optDouble("realized_pnl", 0.0)
                    val notes = obj.optString("notes", "")
                    val txDate = obj.optString("tx_date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

                    list.add(
                        PortfolioTransaction(
                            id = (i + 1).toLong(),
                            userId = userId,
                            symbol = rawSymbol,
                            exchange = exch,
                            txType = txType,
                            price = price,
                            qty = qty,
                            totalAmount = totalAmount,
                            realizedPnl = pnl,
                            notes = notes,
                            txDate = txDate
                        )
                    )
                }

                if (list.isNotEmpty()) {
                    StockticsDatabase.syncTransactions(context, list, userId)
                }
            }
        } catch (_: Exception) {}
        list
    }

    /**
     * 7. Full multi-device portfolio sync
     */
    suspend fun syncFullPortfolio(context: Context) = withContext(Dispatchers.IO) {
        fetchHoldingsFromCloud(context)
        fetchTransactionsFromCloud(context)
        notifyPortfolioChanged()
    }
}
