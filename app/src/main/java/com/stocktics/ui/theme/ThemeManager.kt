package com.stocktics.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Persists the user's theme preference (DARK / LIGHT / SYSTEM) in SharedPreferences
 * and exposes it as observable Compose state so the entire UI recomposes on change.
 */
object ThemeManager {

    enum class ThemeMode { DARK, LIGHT, SYSTEM }

    private const val PREFS_NAME = "stocktics_theme_prefs"
    private const val KEY_THEME = "theme_mode"

    /** Observable Compose state — UI recomposes automatically when changed. */
    var currentTheme by mutableStateOf(ThemeMode.DARK)
        private set

    /** Must be called once on app start (e.g. in MainActivity.onCreate). */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_THEME, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        currentTheme = try {
            ThemeMode.valueOf(stored)
        } catch (_: Exception) {
            ThemeMode.DARK
        }
    }

    /** Switch theme and persist to SharedPreferences. */
    fun setTheme(context: Context, mode: ThemeMode) {
        currentTheme = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode.name)
            .apply()
    }
}
