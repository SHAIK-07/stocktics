package com.stocktics.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.stocktics.data.portfolio.PortfolioHolding
import com.stocktics.data.portfolio.PortfolioSummary
import com.stocktics.data.portfolio.PortfolioTransaction
import com.stocktics.data.stocks.StockSearchItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StockticsDatabase {

    @Volatile
    private var helper: StockticsDatabaseHelper? = null

    private fun getHelper(context: Context): StockticsDatabaseHelper {
        return helper ?: synchronized(this) {
            helper ?: StockticsDatabaseHelper(context.applicationContext).also { helper = it }
        }
    }

    // =========================================================================
    // 1. Watchlist Operations (Strictly per user_id)
    // =========================================================================

    fun getWatchlist(
        context: Context,
        isIndian: Boolean,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): List<StockSearchItem> {
        val db = getHelper(context).writableDatabase
        val market = if (isIndian) "IN" else "US"
        val list = mutableListOf<StockSearchItem>()
        if (userId.isBlank()) return list

        // Auto-migrate legacy MD5 or default IDs to verified user ID
        try {
            if (userId == "50cb350f-a5b1-4511-bd23-4fc80d17647f") {
                db.execSQL(
                    "UPDATE ${StockticsDatabaseHelper.TABLE_WATCHLIST} SET ${StockticsDatabaseHelper.COL_USER_ID} = ? WHERE ${StockticsDatabaseHelper.COL_USER_ID} IN (?, ?)",
                    arrayOf(userId, "be7241b2-7b03-37a4-27d9-393c68caed7e", "default_user")
                )
            }
        } catch (_: Exception) {}

        val cursor = db.query(
            StockticsDatabaseHelper.TABLE_WATCHLIST,
            null,
            "${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_MARKET} = ?",
            arrayOf(userId, market),
            null,
            null,
            "${StockticsDatabaseHelper.COL_UPDATED_AT} DESC"
        )

        cursor.use {
            val idxSymbol = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_SYMBOL)
            val idxName = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_NAME)
            val idxExch = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_EXCHANGE)
            val idxCurr = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_CURRENCY)
            val idxPrice = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_PRICE)
            val idxChange = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_CHANGE)
            val idxPos = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_IS_POSITIVE)
            val idxLogo = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_LOGO_URL)
            val idxCat = it.getColumnIndex(StockticsDatabaseHelper.COL_CATEGORY)

            while (it.moveToNext()) {
                val cat = if (idxCat != -1 && !it.isNull(idxCat)) it.getString(idxCat).ifBlank { "General" } else "General"
                list.add(
                    StockSearchItem(
                        symbol = it.getString(idxSymbol),
                        name = it.getString(idxName),
                        exchange = it.getString(idxExch),
                        currency = it.getString(idxCurr),
                        price = it.getString(idxPrice),
                        change = it.getString(idxChange),
                        isPositive = it.getInt(idxPos) == 1,
                        logoUrl = it.getString(idxLogo),
                        category = cat
                    )
                )
            }
        }
        return list
    }

    fun isWatchlisted(
        context: Context,
        symbol: String,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Boolean {
        if (userId.isBlank()) return false
        val db = getHelper(context).readableDatabase
        val clean = symbol.replace(".NS", "").replace(".BO", "").trim().uppercase()
        val symWithNs = "$clean.NS"
        val symWithBo = "$clean.BO"
        val cursor = db.rawQuery(
            "SELECT 1 FROM ${StockticsDatabaseHelper.TABLE_WATCHLIST} WHERE ${StockticsDatabaseHelper.COL_USER_ID} = ? AND (${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ?) LIMIT 1",
            arrayOf(userId, symbol.trim().uppercase(), clean, symWithNs, symWithBo)
        )
        return cursor.use { it.moveToFirst() }
    }

    fun toggleWatchlist(
        context: Context,
        stock: StockSearchItem,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Boolean {
        val currentlyWatchlisted = isWatchlisted(context, stock.symbol, userId)
        return if (currentlyWatchlisted) {
            removeFromWatchlist(context, stock.symbol, userId)
            false
        } else {
            addToWatchlist(context, stock, userId)
            true
        }
    }

    fun addToWatchlist(
        context: Context,
        stock: StockSearchItem,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ) {
        if (userId.isBlank()) return
        val db = getHelper(context).writableDatabase
        val isIndian = stock.symbol.endsWith(".NS") || stock.symbol.endsWith(".BO") || stock.currency == "₹"
        val cv = ContentValues().apply {
            put(StockticsDatabaseHelper.COL_USER_ID, userId)
            put(StockticsDatabaseHelper.COL_SYMBOL, stock.symbol)
            put(StockticsDatabaseHelper.COL_NAME, stock.name)
            put(StockticsDatabaseHelper.COL_EXCHANGE, stock.exchange)
            put(StockticsDatabaseHelper.COL_CURRENCY, stock.currency)
            put(StockticsDatabaseHelper.COL_MARKET, if (isIndian) "IN" else "US")
            put(StockticsDatabaseHelper.COL_PRICE, stock.price)
            put(StockticsDatabaseHelper.COL_CHANGE, stock.change)
            put(StockticsDatabaseHelper.COL_IS_POSITIVE, if (stock.isPositive) 1 else 0)
            put(StockticsDatabaseHelper.COL_LOGO_URL, stock.logoUrl)
            put(StockticsDatabaseHelper.COL_CATEGORY, stock.category.ifBlank { "General" })
            put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            StockticsDatabaseHelper.TABLE_WATCHLIST,
            null,
            cv,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun removeFromWatchlist(
        context: Context,
        symbol: String,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ) {
        if (userId.isBlank()) return
        val db = getHelper(context).writableDatabase
        val clean = symbol.replace(".NS", "").replace(".BO", "").trim().uppercase()
        val symWithNs = "$clean.NS"
        val symWithBo = "$clean.BO"
        db.delete(
            StockticsDatabaseHelper.TABLE_WATCHLIST,
            "${StockticsDatabaseHelper.COL_USER_ID} = ? AND (${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ?)",
            arrayOf(userId, symbol.trim().uppercase(), clean, symWithNs, symWithBo)
        )
    }

    /**
     * Atomically synchronizes local SQLite cache with verified Supabase remote stocks.
     * Prevents deleted items from resurrecting.
     */
    fun syncWatchlist(
        context: Context,
        remoteStocks: List<StockSearchItem>,
        isIndian: Boolean,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ) {
        if (userId.isBlank()) return
        val db = getHelper(context).writableDatabase
        val market = if (isIndian) "IN" else "US"

        db.beginTransaction()
        try {
            db.delete(
                StockticsDatabaseHelper.TABLE_WATCHLIST,
                "${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_MARKET} = ?",
                arrayOf(userId, market)
            )

            remoteStocks.forEach { stock ->
                val cv = ContentValues().apply {
                    put(StockticsDatabaseHelper.COL_USER_ID, userId)
                    put(StockticsDatabaseHelper.COL_SYMBOL, stock.symbol)
                    put(StockticsDatabaseHelper.COL_NAME, stock.name)
                    put(StockticsDatabaseHelper.COL_EXCHANGE, stock.exchange)
                    put(StockticsDatabaseHelper.COL_CURRENCY, stock.currency)
                    put(StockticsDatabaseHelper.COL_MARKET, market)
                    put(StockticsDatabaseHelper.COL_PRICE, stock.price)
                    put(StockticsDatabaseHelper.COL_CHANGE, stock.change)
                    put(StockticsDatabaseHelper.COL_IS_POSITIVE, if (stock.isPositive) 1 else 0)
                    put(StockticsDatabaseHelper.COL_LOGO_URL, stock.logoUrl)
                    put(StockticsDatabaseHelper.COL_CATEGORY, stock.category.ifBlank { "General" })
                    put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
                }
                db.insertWithOnConflict(
                    StockticsDatabaseHelper.TABLE_WATCHLIST,
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // =========================================================================
    // 2. Recent Searches Operations
    // =========================================================================

    fun getRecentStocks(context: Context, isIndian: Boolean? = null): List<StockSearchItem> {
        val db = getHelper(context).readableDatabase
        val list = mutableListOf<StockSearchItem>()

        val selection = if (isIndian != null) "${StockticsDatabaseHelper.COL_MARKET} = ?" else null
        val selectionArgs = if (isIndian != null) arrayOf(if (isIndian) "IN" else "US") else null

        val cursor = db.query(
            StockticsDatabaseHelper.TABLE_RECENT_STOCKS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${StockticsDatabaseHelper.COL_SEARCHED_AT} DESC",
            "5"
        )

        cursor.use {
            val idxSymbol = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_SYMBOL)
            val idxName = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_NAME)
            val idxExch = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_EXCHANGE)
            val idxCurr = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_CURRENCY)
            val idxMarket = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_MARKET)
            val idxPrice = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_PRICE)
            val idxChange = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_CHANGE)
            val idxPos = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_IS_POSITIVE)
            val idxLogo = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_LOGO_URL)

            while (it.moveToNext()) {
                val rawSym = it.getString(idxSymbol)
                val mkt = it.getString(idxMarket)
                val curr = it.getString(idxCurr)
                val isStockIndian = mkt == "IN" || curr == "₹" || rawSym.endsWith(".NS") || rawSym.endsWith(".BO")
                val normSym = if (isStockIndian && !rawSym.contains(".")) "$rawSym.NS" else rawSym

                list.add(
                    StockSearchItem(
                        symbol = normSym,
                        name = it.getString(idxName),
                        exchange = it.getString(idxExch),
                        currency = curr,
                        price = it.getString(idxPrice),
                        change = it.getString(idxChange),
                        isPositive = it.getInt(idxPos) == 1,
                        logoUrl = it.getString(idxLogo)
                    )
                )
            }
        }
        return list
    }

    fun addToRecent(context: Context, stock: StockSearchItem) {
        val db = getHelper(context).writableDatabase
        val isIndian = stock.symbol.endsWith(".NS") || stock.symbol.endsWith(".BO") || stock.currency == "₹" || stock.exchange.equals("NSE", ignoreCase = true) || stock.exchange.equals("BSE", ignoreCase = true)
        val clean = stock.symbol.replace(".NS", "").replace(".BO", "").trim().uppercase()
        val normalizedSymbol = if (isIndian && !stock.symbol.contains(".")) "$clean.NS" else stock.symbol

        // Clean up any existing legacy entries matching either the normalized symbol or clean bare symbol
        try {
            db.delete(
                StockticsDatabaseHelper.TABLE_RECENT_STOCKS,
                "${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ?",
                arrayOf(normalizedSymbol, clean)
            )
        } catch (_: Exception) {}

        val cv = ContentValues().apply {
            put(StockticsDatabaseHelper.COL_SYMBOL, normalizedSymbol)
            put(StockticsDatabaseHelper.COL_NAME, if (stock.name.isNotBlank()) stock.name else clean)
            put(StockticsDatabaseHelper.COL_EXCHANGE, if (stock.exchange.isNotBlank()) stock.exchange else if (isIndian) "NSE" else "NASDAQ")
            put(StockticsDatabaseHelper.COL_CURRENCY, stock.currency)
            put(StockticsDatabaseHelper.COL_MARKET, if (isIndian) "IN" else "US")
            put(StockticsDatabaseHelper.COL_PRICE, stock.price)
            put(StockticsDatabaseHelper.COL_CHANGE, stock.change)
            put(StockticsDatabaseHelper.COL_IS_POSITIVE, if (stock.isPositive) 1 else 0)
            put(StockticsDatabaseHelper.COL_LOGO_URL, stock.logoUrl)
            put(StockticsDatabaseHelper.COL_SEARCHED_AT, System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            StockticsDatabaseHelper.TABLE_RECENT_STOCKS,
            null,
            cv,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        // Trim excess beyond top 10
        try {
            db.execSQL("""
                DELETE FROM ${StockticsDatabaseHelper.TABLE_RECENT_STOCKS}
                WHERE ${StockticsDatabaseHelper.COL_ID} NOT IN (
                    SELECT ${StockticsDatabaseHelper.COL_ID} FROM ${StockticsDatabaseHelper.TABLE_RECENT_STOCKS}
                    ORDER BY ${StockticsDatabaseHelper.COL_SEARCHED_AT} DESC LIMIT 10
                )
            """.trimIndent())
        } catch (_: Exception) {}
    }

    /**
     * Updates recent stocks in SQLite with fresh live real-time prices & quotes.
     */
    fun updateRecentStocks(context: Context, stocks: List<StockSearchItem>) {
        if (stocks.isEmpty()) return
        val db = getHelper(context).writableDatabase
        db.beginTransaction()
        try {
            for (stock in stocks) {
                if (stock.price.contains("--") || stock.price.isBlank()) continue
                val isIndian = stock.symbol.endsWith(".NS") || stock.symbol.endsWith(".BO") || stock.currency == "₹"
                val clean = stock.symbol.replace(".NS", "").replace(".BO", "").trim().uppercase()
                val normSym = if (isIndian && !stock.symbol.contains(".")) "$clean.NS" else stock.symbol

                val cv = ContentValues().apply {
                    put(StockticsDatabaseHelper.COL_SYMBOL, normSym)
                    if (stock.name.isNotBlank() && stock.name != clean) {
                        put(StockticsDatabaseHelper.COL_NAME, stock.name)
                    }
                    put(StockticsDatabaseHelper.COL_PRICE, stock.price)
                    put(StockticsDatabaseHelper.COL_CHANGE, stock.change)
                    put(StockticsDatabaseHelper.COL_IS_POSITIVE, if (stock.isPositive) 1 else 0)
                    if (stock.logoUrl.isNotBlank()) {
                        put(StockticsDatabaseHelper.COL_LOGO_URL, stock.logoUrl)
                    }
                }
                db.update(
                    StockticsDatabaseHelper.TABLE_RECENT_STOCKS,
                    cv,
                    "${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ?",
                    arrayOf(normSym, clean)
                )
            }
            db.setTransactionSuccessful()
        } catch (_: Exception) {
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Searches local user database (watchlist + recent stocks) for instant 0ms autocomplete matches.
     */
    fun searchLocalStocks(context: Context, query: String, isIndian: Boolean? = null): List<StockSearchItem> {
        val db = getHelper(context).readableDatabase
        val q = "%${query.trim().lowercase()}%"
        val list = mutableListOf<StockSearchItem>()
        val marketFilter = when (isIndian) {
            true -> "AND market = 'IN'"
            false -> "AND market = 'US'"
            null -> ""
        }
        val sql = """
            SELECT DISTINCT symbol, name, exchange, currency, market, price, change, is_positive, logo_url
            FROM (
                SELECT symbol, name, exchange, currency, market, price, change, is_positive, logo_url, searched_at as sort_time FROM ${StockticsDatabaseHelper.TABLE_RECENT_STOCKS}
                UNION ALL
                SELECT symbol, name, exchange, currency, market, price, change, is_positive, logo_url, updated_at as sort_time FROM ${StockticsDatabaseHelper.TABLE_WATCHLIST}
            )
            WHERE (LOWER(symbol) LIKE ? OR LOWER(name) LIKE ?) $marketFilter
            ORDER BY sort_time DESC LIMIT 10
        """.trimIndent()
        try {
            val cursor = db.rawQuery(sql, arrayOf(q, q))
            cursor.use {
                val seen = mutableSetOf<String>()
                while (it.moveToNext()) {
                    val sym = it.getString(0)
                    val base = sym.replace(".NS", "").replace(".BO", "").uppercase()
                    if (!seen.add(base)) continue
                    list.add(
                        StockSearchItem(
                            symbol = sym,
                            name = it.getString(1),
                            exchange = it.getString(2),
                            currency = it.getString(3),
                            price = it.getString(5),
                            change = it.getString(6),
                            isPositive = it.getInt(7) == 1,
                            logoUrl = it.getString(8)
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return list
    }

    fun clearRecent(context: Context) {
        val db = getHelper(context).writableDatabase
        db.delete(StockticsDatabaseHelper.TABLE_RECENT_STOCKS, null, null)
    }

    // =========================================================================
    // 3. Portfolio Holdings Operations (Strictly per user_id)
    // =========================================================================

    fun getActiveHoldings(
        context: Context,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context),
        isIndian: Boolean? = null
    ): List<PortfolioHolding> {
        val list = mutableListOf<PortfolioHolding>()
        if (userId.isBlank()) return list
        val db = getHelper(context).readableDatabase

        val cursor = db.query(
            StockticsDatabaseHelper.TABLE_HOLDINGS,
            null,
            "${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_STATUS} = ?",
            arrayOf(userId, "open"),
            null,
            null,
            "${StockticsDatabaseHelper.COL_UPDATED_AT} DESC"
        )

        cursor.use {
            val idxId = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_ID)
            val idxUser = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_USER_ID)
            val idxSym = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_SYMBOL)
            val idxName = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_NAME)
            val idxExch = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_EXCHANGE)
            val idxCurr = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_CURRENCY)
            val idxMkt = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_MARKET)
            val idxEntryPrice = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_ENTRY_PRICE)
            val idxCurrPrice = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_CURRENT_PRICE)
            val idxQty = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_QTY)
            val idxEntryDate = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_ENTRY_DATE)
            val idxSl = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_STOP_LOSS_PCT)
            val idxTarget = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_TARGET_PCT)
            val idxTrail = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_TRAILING_STOP_PCT)
            val idxMaxDays = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_MAX_HOLD_DAYS)
            val idxStatus = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_STATUS)
            val idxTtl = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_TIME_TO_LEAVE_SCORE)
            val idxVerdict = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_EXIT_VERDICT)
            val idxExitPrice = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_EXIT_PRICE)
            val idxExitDate = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_EXIT_DATE)
            val idxExitReason = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_EXIT_REASON)
            val idxLogo = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_LOGO_URL)
            val idxUpdated = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_UPDATED_AT)
            val idxPrevClose = it.getColumnIndex(StockticsDatabaseHelper.COL_PREV_CLOSE)

            while (it.moveToNext()) {
                val prevCloseVal = if (idxPrevClose != -1 && !it.isNull(idxPrevClose)) it.getDouble(idxPrevClose) else 0.0
                val holding = PortfolioHolding(
                    id = it.getLong(idxId),
                    userId = it.getString(idxUser),
                    symbol = it.getString(idxSym),
                    name = it.getString(idxName),
                    exchange = it.getString(idxExch),
                    currency = it.getString(idxCurr),
                    market = it.getString(idxMkt),
                    entryPrice = it.getDouble(idxEntryPrice),
                    currentPrice = it.getDouble(idxCurrPrice),
                    qty = it.getDouble(idxQty),
                    entryDate = it.getString(idxEntryDate),
                    stopLossPct = it.getDouble(idxSl),
                    targetPct = it.getDouble(idxTarget),
                    trailingStopPct = it.getDouble(idxTrail),
                    maxHoldDays = it.getInt(idxMaxDays),
                    status = it.getString(idxStatus),
                    timeToLeaveScore = it.getInt(idxTtl),
                    exitVerdict = it.getString(idxVerdict),
                    exitPrice = it.getDouble(idxExitPrice),
                    exitDate = it.getString(idxExitDate),
                    exitReason = it.getString(idxExitReason),
                    logoUrl = it.getString(idxLogo),
                    previousClose = prevCloseVal,
                    updatedAt = it.getLong(idxUpdated)
                )

                if (isIndian != null) {
                    if (isIndian && !holding.isIndianMarket) continue
                    if (!isIndian && holding.isIndianMarket) continue
                }
                list.add(holding)
            }
        }
        return list
    }

    fun logBuyPosition(
        context: Context,
        symbol: String,
        name: String,
        exchange: String,
        currency: String = "₹",
        price: Double,
        qty: Double,
        entryDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        stopLossPct: Double = 5.0,
        targetPct: Double = 15.0,
        maxHoldDays: Int = 60,
        logoUrl: String = "",
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Long {
        if (userId.isBlank() || price <= 0 || qty <= 0) return -1L
        val db = getHelper(context).writableDatabase
        val isIndian = exchange == "NSE" || exchange == "BSE" || symbol.endsWith(".NS") || currency == "₹"

        val cvHolding = ContentValues().apply {
            put(StockticsDatabaseHelper.COL_USER_ID, userId)
            put(StockticsDatabaseHelper.COL_SYMBOL, symbol.trim().uppercase())
            put(StockticsDatabaseHelper.COL_NAME, name)
            put(StockticsDatabaseHelper.COL_EXCHANGE, exchange)
            put(StockticsDatabaseHelper.COL_CURRENCY, currency)
            put(StockticsDatabaseHelper.COL_MARKET, if (isIndian) "IN" else "US")
            put(StockticsDatabaseHelper.COL_ENTRY_PRICE, price)
            put(StockticsDatabaseHelper.COL_CURRENT_PRICE, price)
            put(StockticsDatabaseHelper.COL_QTY, qty)
            put(StockticsDatabaseHelper.COL_ENTRY_DATE, entryDate)
            put(StockticsDatabaseHelper.COL_STOP_LOSS_PCT, stopLossPct)
            put(StockticsDatabaseHelper.COL_TARGET_PCT, targetPct)
            put(StockticsDatabaseHelper.COL_TRAILING_STOP_PCT, 4.0)
            put(StockticsDatabaseHelper.COL_MAX_HOLD_DAYS, maxHoldDays)
            put(StockticsDatabaseHelper.COL_STATUS, "open")
            put(StockticsDatabaseHelper.COL_TIME_TO_LEAVE_SCORE, 22) // Defaults to Safe Hold
            put(StockticsDatabaseHelper.COL_EXIT_VERDICT, "Safe Hold")
            put(StockticsDatabaseHelper.COL_LOGO_URL, logoUrl)
            put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
        }

        val holdingId = db.insert(StockticsDatabaseHelper.TABLE_HOLDINGS, null, cvHolding)

        if (holdingId > 0) {
            // Also log to transactions table
            val cvTx = ContentValues().apply {
                put(StockticsDatabaseHelper.COL_USER_ID, userId)
                put(StockticsDatabaseHelper.COL_HOLDING_ID, holdingId)
                put(StockticsDatabaseHelper.COL_SYMBOL, symbol.trim().uppercase())
                put(StockticsDatabaseHelper.COL_EXCHANGE, exchange)
                put(StockticsDatabaseHelper.COL_TX_TYPE, "BUY")
                put(StockticsDatabaseHelper.COL_ENTRY_PRICE, price)
                put(StockticsDatabaseHelper.COL_QTY, qty)
                put(StockticsDatabaseHelper.COL_TOTAL_AMOUNT, price * qty)
                put(StockticsDatabaseHelper.COL_REALIZED_PNL, 0.0)
                put(StockticsDatabaseHelper.COL_NOTES, "Logged Buy from Stocktics")
                put(StockticsDatabaseHelper.COL_TX_DATE, entryDate)
            }
            db.insert(StockticsDatabaseHelper.TABLE_TRANSACTIONS, null, cvTx)
        }

        return holdingId
    }

    fun logExitPosition(
        context: Context,
        holdingId: Long,
        exitPrice: Double,
        exitQty: Double,
        exitReason: String,
        exitDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Boolean {
        if (userId.isBlank() || holdingId <= 0 || exitPrice <= 0 || exitQty <= 0) return false
        val db = getHelper(context).writableDatabase

        // 1. Fetch holding details to compute realized P&L
        var entryPrice = 0.0
        var currentQty = 0.0
        var symbol = ""
        var exchange = ""

        val cursor = db.query(
            StockticsDatabaseHelper.TABLE_HOLDINGS,
            arrayOf(
                StockticsDatabaseHelper.COL_ENTRY_PRICE,
                StockticsDatabaseHelper.COL_QTY,
                StockticsDatabaseHelper.COL_SYMBOL,
                StockticsDatabaseHelper.COL_EXCHANGE
            ),
            "${StockticsDatabaseHelper.COL_ID} = ? AND ${StockticsDatabaseHelper.COL_USER_ID} = ?",
            arrayOf(holdingId.toString(), userId),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                entryPrice = it.getDouble(0)
                currentQty = it.getDouble(1)
                symbol = it.getString(2)
                exchange = it.getString(3)
            } else {
                return false
            }
        }

        val realizedPnl = (exitPrice - entryPrice) * exitQty
        val isFullExit = exitQty >= currentQty

        // 2. Update or close holding
        if (isFullExit) {
            val cv = ContentValues().apply {
                put(StockticsDatabaseHelper.COL_STATUS, "closed")
                put(StockticsDatabaseHelper.COL_EXIT_PRICE, exitPrice)
                put(StockticsDatabaseHelper.COL_EXIT_DATE, exitDate)
                put(StockticsDatabaseHelper.COL_EXIT_REASON, exitReason)
                put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
            }
            db.update(
                StockticsDatabaseHelper.TABLE_HOLDINGS,
                cv,
                "${StockticsDatabaseHelper.COL_ID} = ? AND ${StockticsDatabaseHelper.COL_USER_ID} = ?",
                arrayOf(holdingId.toString(), userId)
            )
        } else {
            // Partial exit: reduce qty
            val remainingQty = currentQty - exitQty
            val cv = ContentValues().apply {
                put(StockticsDatabaseHelper.COL_QTY, remainingQty)
                put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
            }
            db.update(
                StockticsDatabaseHelper.TABLE_HOLDINGS,
                cv,
                "${StockticsDatabaseHelper.COL_ID} = ? AND ${StockticsDatabaseHelper.COL_USER_ID} = ?",
                arrayOf(holdingId.toString(), userId)
            )
        }

        // 3. Insert transaction
        val cvTx = ContentValues().apply {
            put(StockticsDatabaseHelper.COL_USER_ID, userId)
            put(StockticsDatabaseHelper.COL_HOLDING_ID, holdingId)
            put(StockticsDatabaseHelper.COL_SYMBOL, symbol)
            put(StockticsDatabaseHelper.COL_EXCHANGE, exchange)
            put(StockticsDatabaseHelper.COL_TX_TYPE, "SELL")
            put(StockticsDatabaseHelper.COL_ENTRY_PRICE, exitPrice)
            put(StockticsDatabaseHelper.COL_QTY, exitQty)
            put(StockticsDatabaseHelper.COL_TOTAL_AMOUNT, exitPrice * exitQty)
            put(StockticsDatabaseHelper.COL_REALIZED_PNL, realizedPnl)
            put(StockticsDatabaseHelper.COL_NOTES, exitReason)
            put(StockticsDatabaseHelper.COL_TX_DATE, exitDate)
        }
        db.insert(StockticsDatabaseHelper.TABLE_TRANSACTIONS, null, cvTx)

        return true
    }

    fun deleteHolding(
        context: Context,
        holdingId: Long,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Boolean {
        if (userId.isBlank() || holdingId <= 0) return false
        val db = getHelper(context).writableDatabase
        val rows = db.delete(
            StockticsDatabaseHelper.TABLE_HOLDINGS,
            "${StockticsDatabaseHelper.COL_ID} = ? AND ${StockticsDatabaseHelper.COL_USER_ID} = ?",
            arrayOf(holdingId.toString(), userId)
        )
        return rows > 0
    }

    fun updateHoldingPrices(
        context: Context,
        priceMap: Map<String, Double>,
        prevCloseMap: Map<String, Double> = emptyMap(),
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ) {
        if (userId.isBlank() || priceMap.isEmpty()) return
        val db = getHelper(context).writableDatabase
        db.beginTransaction()
        try {
            for ((sym, price) in priceMap) {
                if (price > 0) {
                    val clean = sym.replace(".NS", "").replace(".BO", "").trim().uppercase()
                    val pClose = prevCloseMap[sym] ?: prevCloseMap[clean] ?: prevCloseMap["$clean.NS"] ?: 0.0
                    val cv = ContentValues().apply {
                        put(StockticsDatabaseHelper.COL_CURRENT_PRICE, price)
                        if (pClose > 0) {
                            put(StockticsDatabaseHelper.COL_PREV_CLOSE, pClose)
                        }
                        put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
                    }
                    db.update(
                        StockticsDatabaseHelper.TABLE_HOLDINGS,
                        cv,
                        "${StockticsDatabaseHelper.COL_USER_ID} = ? AND (${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ? OR ${StockticsDatabaseHelper.COL_SYMBOL} = ?) AND ${StockticsDatabaseHelper.COL_STATUS} = 'open'",
                        arrayOf(userId, sym.trim().uppercase(), clean, "$clean.NS")
                    )
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun updateExitScore(
        context: Context,
        holdingId: Long,
        timeToLeaveScore: Int,
        exitVerdict: String,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ) {
        if (userId.isBlank() || holdingId <= 0) return
        val db = getHelper(context).writableDatabase
        val cv = ContentValues().apply {
            put(StockticsDatabaseHelper.COL_TIME_TO_LEAVE_SCORE, timeToLeaveScore.coerceIn(0, 100))
            put(StockticsDatabaseHelper.COL_EXIT_VERDICT, exitVerdict)
            put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
        }
        db.update(
            StockticsDatabaseHelper.TABLE_HOLDINGS,
            cv,
            "${StockticsDatabaseHelper.COL_ID} = ? AND ${StockticsDatabaseHelper.COL_USER_ID} = ?",
            arrayOf(holdingId.toString(), userId)
        )
    }

    fun getPortfolioSummary(
        context: Context,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context),
        isIndian: Boolean? = null
    ): PortfolioSummary {
        if (userId.isBlank()) return PortfolioSummary()
        val holdings = getActiveHoldings(context, userId, isIndian)

        var totalInvested = 0.0
        var currentValue = 0.0
        var totalTtlWeighted = 0.0
        var highRiskCount = 0
        var totalOneDayReturn = 0.0
        var totalPrevDayValue = 0.0

        for (h in holdings) {
            val invested = h.totalInvested
            val current = h.currentValue
            totalInvested += invested
            currentValue += current
            totalTtlWeighted += (h.timeToLeaveScore * current)
            if (h.timeToLeaveScore >= 70) {
                highRiskCount++
            }

            totalOneDayReturn += h.oneDayPnl
            totalPrevDayValue += (h.effectivePrevClose * h.qty)
        }

        val unrealizedPnl = currentValue - totalInvested
        val unrealizedPnlPct = if (totalInvested > 0) (unrealizedPnl / totalInvested) * 100.0 else 0.0
        val overallRiskScore = if (currentValue > 0) (totalTtlWeighted / currentValue).toInt().coerceIn(0, 100) else 25

        // Query realized P&L from transactions
        var realizedPnl = 0.0
        val db = getHelper(context).readableDatabase
        val txQuery = when (isIndian) {
            true -> "SELECT SUM(${StockticsDatabaseHelper.COL_REALIZED_PNL}) FROM ${StockticsDatabaseHelper.TABLE_TRANSACTIONS} WHERE ${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_TX_TYPE} = 'SELL' AND (${StockticsDatabaseHelper.COL_EXCHANGE} IN ('NSE', 'BSE') OR ${StockticsDatabaseHelper.COL_SYMBOL} LIKE '%.NS' OR ${StockticsDatabaseHelper.COL_SYMBOL} LIKE '%.BO')"
            false -> "SELECT SUM(${StockticsDatabaseHelper.COL_REALIZED_PNL}) FROM ${StockticsDatabaseHelper.TABLE_TRANSACTIONS} WHERE ${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_TX_TYPE} = 'SELL' AND (${StockticsDatabaseHelper.COL_EXCHANGE} NOT IN ('NSE', 'BSE') AND ${StockticsDatabaseHelper.COL_SYMBOL} NOT LIKE '%.NS' AND ${StockticsDatabaseHelper.COL_SYMBOL} NOT LIKE '%.BO')"
            null -> "SELECT SUM(${StockticsDatabaseHelper.COL_REALIZED_PNL}) FROM ${StockticsDatabaseHelper.TABLE_TRANSACTIONS} WHERE ${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_TX_TYPE} = 'SELL'"
        }
        val cursor = db.rawQuery(txQuery, arrayOf(userId))
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                realizedPnl = it.getDouble(0)
            }
        }

        val oneDayReturnPct = if (totalPrevDayValue > 0) (totalOneDayReturn / totalPrevDayValue) * 100.0 else 0.0

        return PortfolioSummary(
            totalInvested = totalInvested,
            currentValue = currentValue,
            unrealizedPnl = unrealizedPnl,
            unrealizedPnlPct = unrealizedPnlPct,
            oneDayReturn = totalOneDayReturn,
            oneDayReturnPct = oneDayReturnPct,
            realizedPnl = realizedPnl,
            activePositionsCount = holdings.size,
            overallRiskScore = overallRiskScore,
            highRiskCount = highRiskCount
        )
    }

    // =========================================================================
    // 4. Position Editing & Cloud Synchronization
    // =========================================================================

    fun updateHoldingPosition(
        context: Context,
        holdingId: Long,
        entryPrice: Double,
        qty: Double,
        stopLossPct: Double,
        targetPct: Double,
        maxHoldDays: Int,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Boolean {
        if (userId.isBlank() || holdingId <= 0 || entryPrice <= 0 || qty <= 0) return false
        val db = getHelper(context).writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put(StockticsDatabaseHelper.COL_ENTRY_PRICE, entryPrice)
                put(StockticsDatabaseHelper.COL_QTY, qty)
                put(StockticsDatabaseHelper.COL_STOP_LOSS_PCT, stopLossPct)
                put(StockticsDatabaseHelper.COL_TARGET_PCT, targetPct)
                put(StockticsDatabaseHelper.COL_MAX_HOLD_DAYS, maxHoldDays)
                put(StockticsDatabaseHelper.COL_UPDATED_AT, System.currentTimeMillis())
            }
            val rows = db.update(
                StockticsDatabaseHelper.TABLE_HOLDINGS,
                cv,
                "${StockticsDatabaseHelper.COL_ID} = ? AND ${StockticsDatabaseHelper.COL_USER_ID} = ?",
                arrayOf(holdingId.toString(), userId)
            )

            // Also update original BUY transaction if present
            val cvTx = ContentValues().apply {
                put(StockticsDatabaseHelper.COL_ENTRY_PRICE, entryPrice)
                put(StockticsDatabaseHelper.COL_QTY, qty)
                put(StockticsDatabaseHelper.COL_TOTAL_AMOUNT, entryPrice * qty)
            }
            db.update(
                StockticsDatabaseHelper.TABLE_TRANSACTIONS,
                cvTx,
                "${StockticsDatabaseHelper.COL_HOLDING_ID} = ? AND ${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_TX_TYPE} = 'BUY'",
                arrayOf(holdingId.toString(), userId)
            )

            db.setTransactionSuccessful()
            return rows > 0
        } catch (_: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun syncHoldings(
        context: Context,
        remoteHoldings: List<PortfolioHolding>,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ) {
        if (userId.isBlank()) return
        val db = getHelper(context).writableDatabase
        db.beginTransaction()
        try {
            // Snapshot existing local prices before wiping to avoid clobbering live prices with entry prices
            val existingPrices = mutableMapOf<String, Pair<Double, Double>>() // sym -> (curr, prevClose)
            val cur = db.query(
                StockticsDatabaseHelper.TABLE_HOLDINGS,
                arrayOf(
                    StockticsDatabaseHelper.COL_SYMBOL,
                    StockticsDatabaseHelper.COL_CURRENT_PRICE,
                    StockticsDatabaseHelper.COL_PREV_CLOSE
                ),
                "${StockticsDatabaseHelper.COL_USER_ID} = ?",
                arrayOf(userId),
                null, null, null
            )
            cur.use {
                val sIdx = it.getColumnIndex(StockticsDatabaseHelper.COL_SYMBOL)
                val cIdx = it.getColumnIndex(StockticsDatabaseHelper.COL_CURRENT_PRICE)
                val pIdx = it.getColumnIndex(StockticsDatabaseHelper.COL_PREV_CLOSE)
                while (it.moveToNext()) {
                    val s = it.getString(sIdx).trim().uppercase()
                    val c = if (cIdx != -1) it.getDouble(cIdx) else 0.0
                    val p = if (pIdx != -1 && !it.isNull(pIdx)) it.getDouble(pIdx) else 0.0
                    if (c > 0) existingPrices[s] = Pair(c, p)
                }
            }

            // Remove local holdings for this user and replace with fresh cloud records
            db.delete(
                StockticsDatabaseHelper.TABLE_HOLDINGS,
                "${StockticsDatabaseHelper.COL_USER_ID} = ?",
                arrayOf(userId)
            )

            for (h in remoteHoldings) {
                val cleanSym = h.symbol.trim().uppercase()
                val cached = existingPrices[cleanSym] ?: existingPrices[cleanSym.replace(".NS", "").replace(".BO", "")]
                val effectivePrice = if (h.currentPrice > 0 && h.currentPrice != h.entryPrice) {
                    h.currentPrice
                } else {
                    cached?.first ?: h.currentPrice.takeIf { it > 0 } ?: h.entryPrice
                }
                val effectivePrev = if (h.previousClose > 0) {
                    h.previousClose
                } else {
                    cached?.second ?: 0.0
                }

                val cv = ContentValues().apply {
                    put(StockticsDatabaseHelper.COL_USER_ID, userId)
                    put(StockticsDatabaseHelper.COL_SYMBOL, cleanSym)
                    put(StockticsDatabaseHelper.COL_NAME, h.name)
                    put(StockticsDatabaseHelper.COL_EXCHANGE, h.exchange)
                    put(StockticsDatabaseHelper.COL_CURRENCY, h.currency)
                    put(StockticsDatabaseHelper.COL_MARKET, h.market)
                    put(StockticsDatabaseHelper.COL_ENTRY_PRICE, h.entryPrice)
                    put(StockticsDatabaseHelper.COL_CURRENT_PRICE, effectivePrice)
                    put(StockticsDatabaseHelper.COL_PREV_CLOSE, effectivePrev)
                    put(StockticsDatabaseHelper.COL_QTY, h.qty)
                    put(StockticsDatabaseHelper.COL_ENTRY_DATE, h.entryDate)
                    put(StockticsDatabaseHelper.COL_STOP_LOSS_PCT, h.stopLossPct)
                    put(StockticsDatabaseHelper.COL_TARGET_PCT, h.targetPct)
                    put(StockticsDatabaseHelper.COL_TRAILING_STOP_PCT, h.trailingStopPct)
                    put(StockticsDatabaseHelper.COL_MAX_HOLD_DAYS, h.maxHoldDays)
                    put(StockticsDatabaseHelper.COL_STATUS, h.status)
                    put(StockticsDatabaseHelper.COL_TIME_TO_LEAVE_SCORE, h.timeToLeaveScore)
                    put(StockticsDatabaseHelper.COL_EXIT_VERDICT, h.exitVerdict)
                    put(StockticsDatabaseHelper.COL_EXIT_PRICE, h.exitPrice)
                    put(StockticsDatabaseHelper.COL_EXIT_DATE, h.exitDate)
                    put(StockticsDatabaseHelper.COL_EXIT_REASON, h.exitReason)
                    put(StockticsDatabaseHelper.COL_LOGO_URL, h.logoUrl)
                    put(StockticsDatabaseHelper.COL_UPDATED_AT, h.updatedAt)
                }
                db.insert(StockticsDatabaseHelper.TABLE_HOLDINGS, null, cv)
            }
            db.setTransactionSuccessful()
        } catch (_: Exception) {
        } finally {
            db.endTransaction()
        }
    }

    fun syncTransactions(
        context: Context,
        remoteTransactions: List<PortfolioTransaction>,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ) {
        if (userId.isBlank() || remoteTransactions.isEmpty()) return
        val db = getHelper(context).writableDatabase
        db.beginTransaction()
        try {
            db.delete(
                StockticsDatabaseHelper.TABLE_TRANSACTIONS,
                "${StockticsDatabaseHelper.COL_USER_ID} = ?",
                arrayOf(userId)
            )

            for (tx in remoteTransactions) {
                val cv = ContentValues().apply {
                    put(StockticsDatabaseHelper.COL_USER_ID, userId)
                    put(StockticsDatabaseHelper.COL_HOLDING_ID, tx.holdingId)
                    put(StockticsDatabaseHelper.COL_SYMBOL, tx.symbol.trim().uppercase())
                    put(StockticsDatabaseHelper.COL_EXCHANGE, tx.exchange)
                    put(StockticsDatabaseHelper.COL_TX_TYPE, tx.txType)
                    put(StockticsDatabaseHelper.COL_ENTRY_PRICE, tx.price)
                    put(StockticsDatabaseHelper.COL_QTY, tx.qty)
                    put(StockticsDatabaseHelper.COL_TOTAL_AMOUNT, tx.totalAmount)
                    put(StockticsDatabaseHelper.COL_REALIZED_PNL, tx.realizedPnl)
                    put(StockticsDatabaseHelper.COL_NOTES, tx.notes)
                    put(StockticsDatabaseHelper.COL_TX_DATE, tx.txDate)
                }
                db.insert(StockticsDatabaseHelper.TABLE_TRANSACTIONS, null, cv)
            }
            db.setTransactionSuccessful()
        } catch (_: Exception) {
        } finally {
            db.endTransaction()
        }
    }

    // =========================================================================
    // 6. Quantitative Learning System Progress Operations
    // =========================================================================

    fun getCompletedLessons(
        context: Context,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Set<Int> {
        val completed = mutableSetOf<Int>()
        val effectiveUser = if (userId.isBlank()) "default_user" else userId
        try {
            val db = getHelper(context).readableDatabase
            val cursor = db.query(
                StockticsDatabaseHelper.TABLE_LESSON_PROGRESS,
                arrayOf(StockticsDatabaseHelper.COL_LESSON_NUMBER),
                "${StockticsDatabaseHelper.COL_USER_ID} = ?",
                arrayOf(effectiveUser),
                null, null, null
            )
            cursor.use {
                val idx = it.getColumnIndexOrThrow(StockticsDatabaseHelper.COL_LESSON_NUMBER)
                while (it.moveToNext()) {
                    completed.add(it.getInt(idx))
                }
            }
        } catch (_: Exception) {}
        return completed
    }

    fun toggleLessonCompletion(
        context: Context,
        lessonNumber: Int,
        userId: String = com.stocktics.data.session.UserSessionManager.getLoggedInUserId(context)
    ): Boolean {
        val effectiveUser = if (userId.isBlank()) "default_user" else userId
        val currentlyCompleted = getCompletedLessons(context, effectiveUser).contains(lessonNumber)
        val shouldComplete = !currentlyCompleted
        try {
            val db = getHelper(context).writableDatabase
            if (shouldComplete) {
                val cv = ContentValues().apply {
                    put(StockticsDatabaseHelper.COL_USER_ID, effectiveUser)
                    put(StockticsDatabaseHelper.COL_LESSON_NUMBER, lessonNumber)
                    put(StockticsDatabaseHelper.COL_COMPLETED_AT, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                }
                db.insertWithOnConflict(
                    StockticsDatabaseHelper.TABLE_LESSON_PROGRESS,
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            } else {
                db.delete(
                    StockticsDatabaseHelper.TABLE_LESSON_PROGRESS,
                    "${StockticsDatabaseHelper.COL_USER_ID} = ? AND ${StockticsDatabaseHelper.COL_LESSON_NUMBER} = ?",
                    arrayOf(effectiveUser, lessonNumber.toString())
                )
            }
        } catch (_: Exception) {}
        return shouldComplete
    }
}
