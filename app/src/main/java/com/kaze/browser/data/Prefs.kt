package com.kaze.browser.data

import android.content.Context

/** Theme selection. SYSTEM follows the OS dark-mode setting. */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/**
 * Tiny SharedPreferences wrapper for the handful of persisted settings.
 * No DataStore dependency — these are three flat values read once at startup.
 */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("kaze", Context.MODE_PRIVATE)

    var engine: SearchEngine
        get() = SearchEngine.fromName(sp.getString(KEY_ENGINE, null))
        set(value) = sp.edit().putString(KEY_ENGINE, value.name).apply()

    var adBlock: Boolean
        get() = sp.getBoolean(KEY_ADBLOCK, true)
        set(value) = sp.edit().putBoolean(KEY_ADBLOCK, value).apply()

    var theme: ThemeMode
        get() = ThemeMode.fromName(sp.getString(KEY_THEME, null))
        set(value) = sp.edit().putString(KEY_THEME, value.name).apply()

    private companion object {
        const val KEY_ENGINE = "engine"
        const val KEY_ADBLOCK = "adblock"
        const val KEY_THEME = "theme"
    }
}
