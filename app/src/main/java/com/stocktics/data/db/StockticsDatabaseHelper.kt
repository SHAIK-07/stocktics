package com.stocktics.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.stocktics.data.stocks.StockSearchItem

class StockticsDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "stocktics.db"
        const val DATABASE_VERSION = 3

        // Watchlist Table
        const val TABLE_WATCHLIST = "watchlist"
        const val COL_ID = "id"
        const val COL_USER_ID = "user_id"
        const val COL_SYMBOL = "symbol"
        const val COL_NAME = "name"
        const val COL_EXCHANGE = "exchange"
        const val COL_CURRENCY = "currency"
        const val COL_MARKET = "market" // "IN" or "US"
        const val COL_PRICE = "price"
        const val COL_CHANGE = "change"
        const val COL_IS_POSITIVE = "is_positive"
        const val COL_LOGO_URL = "logo_url"
        const val COL_CATEGORY = "category"
        const val COL_UPDATED_AT = "updated_at"

        // Recent Searches Table
        const val TABLE_RECENT_STOCKS = "recent_stocks"
        const val COL_SEARCHED_AT = "searched_at"

        // Holdings Table (Active Portfolio Positions)
        const val TABLE_HOLDINGS = "holdings"
        const val COL_ENTRY_PRICE = "entry_price"
        const val COL_CURRENT_PRICE = "current_price"
        const val COL_QTY = "qty"
        const val COL_ENTRY_DATE = "entry_date"
        const val COL_STOP_LOSS_PCT = "stop_loss_pct"
        const val COL_TARGET_PCT = "target_pct"
        const val COL_TRAILING_STOP_PCT = "trailing_stop_pct"
        const val COL_MAX_HOLD_DAYS = "max_hold_days"
        const val COL_STATUS = "status" // "open" or "closed"
        const val COL_TIME_TO_LEAVE_SCORE = "time_to_leave_score" // 0-100
        const val COL_EXIT_VERDICT = "exit_verdict" // "Safe Hold", "Hold Position", "Consider Exiting", "Exit Now"
        const val COL_EXIT_PRICE = "exit_price"
        const val COL_EXIT_DATE = "exit_date"
        const val COL_EXIT_REASON = "exit_reason"
        const val COL_PREV_CLOSE = "prev_close"

        // Transactions Table (Realized Audit Log)
        const val TABLE_TRANSACTIONS = "transactions"
        const val COL_HOLDING_ID = "holding_id"
        const val COL_TX_TYPE = "tx_type" // "BUY" or "SELL"
        const val COL_TOTAL_AMOUNT = "total_amount"
        const val COL_REALIZED_PNL = "realized_pnl"
        const val COL_NOTES = "notes"
        const val COL_TX_DATE = "tx_date"

        // Lesson Progress Table (Quantitative Learning System)
        const val TABLE_LESSON_PROGRESS = "lesson_progress"
        const val COL_LESSON_NUMBER = "lesson_number"
        const val COL_COMPLETED_AT = "completed_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Create Watchlist Table (partitioned strictly per user_id)
        val createWatchlist = """
            CREATE TABLE $TABLE_WATCHLIST (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_SYMBOL TEXT NOT NULL,
                $COL_NAME TEXT NOT NULL,
                $COL_EXCHANGE TEXT NOT NULL,
                $COL_CURRENCY TEXT NOT NULL,
                $COL_MARKET TEXT NOT NULL,
                $COL_PRICE TEXT NOT NULL,
                $COL_CHANGE TEXT NOT NULL,
                $COL_IS_POSITIVE INTEGER NOT NULL DEFAULT 1,
                $COL_LOGO_URL TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL DEFAULT 'General',
                $COL_UPDATED_AT INTEGER NOT NULL,
                UNIQUE($COL_USER_ID, $COL_SYMBOL)
            )
        """.trimIndent()
        db.execSQL(createWatchlist)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_watchlist_user_market ON $TABLE_WATCHLIST($COL_USER_ID, $COL_MARKET)")

        // 2. Create Recent Searches Table
        val createRecent = """
            CREATE TABLE $TABLE_RECENT_STOCKS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL DEFAULT '',
                $COL_SYMBOL TEXT NOT NULL,
                $COL_NAME TEXT NOT NULL,
                $COL_EXCHANGE TEXT NOT NULL,
                $COL_CURRENCY TEXT NOT NULL,
                $COL_MARKET TEXT NOT NULL,
                $COL_PRICE TEXT NOT NULL,
                $COL_CHANGE TEXT NOT NULL,
                $COL_IS_POSITIVE INTEGER NOT NULL DEFAULT 1,
                $COL_LOGO_URL TEXT NOT NULL,
                $COL_SEARCHED_AT INTEGER NOT NULL,
                UNIQUE($COL_USER_ID, $COL_SYMBOL)
            )
        """.trimIndent()
        db.execSQL(createRecent)

        // 3. Create Holdings Table
        createHoldingsTable(db)

        // 4. Create Transactions Table
        createTransactionsTable(db)

        // 5. Create Lesson Progress Table
        createLessonProgressTable(db)
    }

    private fun createHoldingsTable(db: SQLiteDatabase) {
        val createHoldings = """
            CREATE TABLE IF NOT EXISTS $TABLE_HOLDINGS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_SYMBOL TEXT NOT NULL,
                $COL_NAME TEXT NOT NULL,
                $COL_EXCHANGE TEXT NOT NULL,
                $COL_CURRENCY TEXT NOT NULL DEFAULT '₹',
                $COL_MARKET TEXT NOT NULL DEFAULT 'IN',
                $COL_ENTRY_PRICE REAL NOT NULL,
                $COL_CURRENT_PRICE REAL NOT NULL,
                $COL_QTY REAL NOT NULL,
                $COL_ENTRY_DATE TEXT NOT NULL,
                $COL_STOP_LOSS_PCT REAL NOT NULL DEFAULT 5.0,
                $COL_TARGET_PCT REAL NOT NULL DEFAULT 15.0,
                $COL_TRAILING_STOP_PCT REAL NOT NULL DEFAULT 4.0,
                $COL_MAX_HOLD_DAYS INTEGER NOT NULL DEFAULT 60,
                $COL_STATUS TEXT NOT NULL DEFAULT 'open',
                $COL_TIME_TO_LEAVE_SCORE INTEGER NOT NULL DEFAULT 20,
                $COL_EXIT_VERDICT TEXT NOT NULL DEFAULT 'Safe Hold',
                $COL_EXIT_PRICE REAL DEFAULT 0.0,
                $COL_EXIT_DATE TEXT DEFAULT '',
                $COL_EXIT_REASON TEXT DEFAULT '',
                $COL_LOGO_URL TEXT NOT NULL DEFAULT '',
                $COL_PREV_CLOSE REAL DEFAULT 0.0,
                $COL_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createHoldings)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_holdings_user_status ON $TABLE_HOLDINGS($COL_USER_ID, $COL_STATUS)")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        try {
            db.execSQL("ALTER TABLE $TABLE_HOLDINGS ADD COLUMN $COL_PREV_CLOSE REAL DEFAULT 0.0")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_WATCHLIST ADD COLUMN $COL_CATEGORY TEXT NOT NULL DEFAULT 'General'")
        } catch (_: Exception) {}
        try {
            createLessonProgressTable(db)
        } catch (_: Exception) {}
    }

    private fun createLessonProgressTable(db: SQLiteDatabase) {
        val createProgress = """
            CREATE TABLE IF NOT EXISTS $TABLE_LESSON_PROGRESS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_LESSON_NUMBER INTEGER NOT NULL,
                $COL_COMPLETED_AT TEXT NOT NULL,
                UNIQUE($COL_USER_ID, $COL_LESSON_NUMBER)
            )
        """.trimIndent()
        db.execSQL(createProgress)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_lesson_progress_user ON $TABLE_LESSON_PROGRESS($COL_USER_ID)")
    }

    private fun createTransactionsTable(db: SQLiteDatabase) {
        val createTransactions = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_HOLDING_ID INTEGER,
                $COL_SYMBOL TEXT NOT NULL,
                $COL_EXCHANGE TEXT NOT NULL,
                $COL_TX_TYPE TEXT NOT NULL,
                $COL_ENTRY_PRICE REAL NOT NULL,
                $COL_QTY REAL NOT NULL,
                $COL_TOTAL_AMOUNT REAL NOT NULL,
                $COL_REALIZED_PNL REAL DEFAULT 0.0,
                $COL_NOTES TEXT DEFAULT '',
                $COL_TX_DATE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTransactions)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_user ON $TABLE_TRANSACTIONS($COL_USER_ID)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_WATCHLIST")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RECENT_STOCKS")
            onCreate(db)
        } else if (oldVersion < 3) {
            createHoldingsTable(db)
            createTransactionsTable(db)
            createLessonProgressTable(db)
        } else if (oldVersion < 4) {
            createLessonProgressTable(db)
        }
    }
}
