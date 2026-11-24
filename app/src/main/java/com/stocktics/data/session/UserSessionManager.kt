package com.stocktics.data.session

import android.content.Context
import android.content.SharedPreferences

object UserSessionManager {
    private const val PREFS_NAME = "stocktics_user_session"
    private const val KEY_LOGGED_IN_EMAIL = "logged_in_user_email"
    private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
    private const val KEY_LOGGED_IN_TOKEN = "logged_in_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Verified Supabase Auth UUID mappings for existing accounts
    private val KNOWN_USER_MAP = mapOf(
        "vinayakpatil.657@gmail.com" to "4c7e93ec-f3bc-4545-b8ab-e6a24b9c61c7",
        "shaik.hidaythulla7289@gmail.com" to "50cb350f-a5b1-4511-bd23-4fc80d17647f",
        "test@gmail.com" to "5f7ed37f-14ce-4c45-bde2-f2851ab56d80"
    )

    fun generateDeterministicUserId(email: String): String {
        val clean = email.trim().lowercase()
        return KNOWN_USER_MAP[clean] ?: java.util.UUID.nameUUIDFromBytes(clean.toByteArray()).toString()
    }

    fun isUserLoggedIn(context: Context): Boolean {
        val email = getPrefs(context).getString(KEY_LOGGED_IN_EMAIL, null)
        return !email.isNullOrBlank()
    }

    fun getLoggedInUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_LOGGED_IN_EMAIL, "") ?: ""
    }

    fun getLoggedInUserId(context: Context): String {
        val email = getLoggedInUserEmail(context).trim().lowercase()
        val knownId = KNOWN_USER_MAP[email]
        val stored = getPrefs(context).getString(KEY_LOGGED_IN_USER_ID, null)

        // If authentic Supabase ID is known, prioritize it and migrate stored pref if needed
        if (knownId != null) {
            if (stored != knownId) {
                getPrefs(context).edit().putString(KEY_LOGGED_IN_USER_ID, knownId).apply()
            }
            return knownId
        }

        if (!stored.isNullOrBlank()) return stored
        return if (email.isNotBlank()) generateDeterministicUserId(email) else ""
    }

    private const val KEY_LOGGED_IN_NICKNAME = "logged_in_user_nickname"

    fun getLoggedInUserNickname(context: Context): String {
        val stored = getPrefs(context).getString(KEY_LOGGED_IN_NICKNAME, "") ?: ""
        if (stored.isNotBlank()) return stored

        val email = getLoggedInUserEmail(context)
        if (email.isBlank()) return "Trader"

        // Derive initial clean nickname: e.g. "shaik.hidaythulla7289" -> "Shaik"
        val prefix = email.substringBefore("@")
        val cleanWord = prefix.split(".", "_", "-").firstOrNull()?.filter { it.isLetter() } ?: prefix
        return if (cleanWord.isNotBlank()) {
            cleanWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            "Trader"
        }
    }

    fun saveUserNickname(context: Context, nickname: String) {
        val clean = nickname.trim()
        if (clean.isNotEmpty()) {
            getPrefs(context).edit().putString(KEY_LOGGED_IN_NICKNAME, clean).apply()
        }
    }

    fun hasCustomNickname(context: Context): Boolean {
        return !getPrefs(context).getString(KEY_LOGGED_IN_NICKNAME, "").isNullOrBlank()
    }

    fun getLoggedInToken(context: Context): String {
        return getPrefs(context).getString(KEY_LOGGED_IN_TOKEN, "") ?: ""
    }

    fun saveUserSession(
        context: Context,
        email: String,
        userId: String = "",
        token: String = ""
    ) {
        val cleanEmail = email.trim().lowercase()
        val effectiveUserId = when {
            userId.isNotBlank() -> userId
            KNOWN_USER_MAP.containsKey(cleanEmail) -> KNOWN_USER_MAP[cleanEmail]!!
            else -> generateDeterministicUserId(cleanEmail)
        }
        val editor = getPrefs(context).edit()
            .putString(KEY_LOGGED_IN_EMAIL, cleanEmail)
            .putString(KEY_LOGGED_IN_USER_ID, effectiveUserId)
        if (token.isNotBlank()) {
            editor.putString(KEY_LOGGED_IN_TOKEN, token)
        }
        editor.apply()
    }

    fun clearUserSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    private const val KEY_GLOBAL_MARKET = "stocktics_global_market"

    fun getGlobalMarket(context: Context): com.stocktics.ui.components.GlobalMarket {
        val code = getPrefs(context).getString(KEY_GLOBAL_MARKET, "IN")
        return if (code == "US") com.stocktics.ui.components.GlobalMarket.US else com.stocktics.ui.components.GlobalMarket.INDIA
    }

    fun saveGlobalMarket(context: Context, market: com.stocktics.ui.components.GlobalMarket) {
        getPrefs(context).edit().putString(KEY_GLOBAL_MARKET, market.code).apply()
    }
}
