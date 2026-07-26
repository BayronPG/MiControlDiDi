package com.jhon.micontroldidi.ui.settings

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Modo de tema seleccionado por el usuario. */
enum class ThemeMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromValue(value: Int): ThemeMode =
            entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}

class ThemePreferenceManager(context: Context) {

    private val prefs = context.getSharedPreferences("micontrol_settings", Context.MODE_PRIVATE)
    private val themeKey = "theme_mode"

    private val _themeMode = MutableStateFlow(load())

    /** Flujo reactivo del modo de tema seleccionado. */
    val themeModeFlow: Flow<ThemeMode> = _themeMode.asStateFlow()

    /** Valor actual del tema. */
    fun getCurrent(): ThemeMode = _themeMode.value

    /** Guarda el modo de tema seleccionado. */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putInt(themeKey, mode.value).apply()
        _themeMode.value = mode
    }

    private fun load(): ThemeMode =
        ThemeMode.fromValue(prefs.getInt(themeKey, ThemeMode.SYSTEM.value))
}
