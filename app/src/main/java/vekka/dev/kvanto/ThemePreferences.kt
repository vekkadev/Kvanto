package vekka.dev.kvanto

import android.content.Context

object ThemePreferences {

    private const val PREFS_NAME = "kvanto_prefs"
    private const val KEY_DARK_THEME = "dark_theme"

    fun getDarkTheme(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_THEME, false)
    }

    fun saveDarkTheme(context: Context, isDarkTheme: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_THEME, isDarkTheme).apply()
    }
}