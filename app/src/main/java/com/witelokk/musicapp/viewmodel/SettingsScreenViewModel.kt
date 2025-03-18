package com.witelokk.musicapp.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsScreenState(
    val accountName: String? = null,
    val accountEmail: String? = null,
    val songCachingEnabled: Boolean? = null,
    val theme: String? = null,
    val isLoggedOut: Boolean = false,
)

class SettingsScreenViewModel(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        _state.update {
            it.copy(
                accountName = sharedPreferences.getString("accountName", null),
                accountEmail = sharedPreferences.getString("accountEmail", null),
                songCachingEnabled = sharedPreferences.getBoolean("songCachingEnabled", false),
                theme = sharedPreferences.getString("theme", "system"),
            )
        }
    }

    fun setSongCachingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("songCachingEnabled", enabled).apply()
        _state.update { it.copy(songCachingEnabled = enabled) }
    }

    fun setTheme(theme: String) {
        sharedPreferences.edit().putString("theme", theme).apply()
        _state.update { it.copy(theme = theme) }
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
        _state.update { it.copy(isLoggedOut = true) }
    }
}