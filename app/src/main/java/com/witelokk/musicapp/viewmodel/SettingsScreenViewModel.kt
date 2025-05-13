package com.witelokk.musicapp.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.api.apis.UsersApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsScreenState(
    val accountName: String? = null,
    val accountEmail: String? = null,
    val songCachingEnabled: Boolean? = null,
    val theme: String? = null,
    val isLoggedOut: Boolean = false,
)

class SettingsScreenViewModel(
    private val sharedPreferences: SharedPreferences,
    private val usersApi: UsersApi,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
        loadSettings()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val response = usersApi.usersMeGet()

            if (!response.success) {
                return@launch
            }

            val me = response.body()

            sharedPreferences.edit()
                .putString("accountName", me.name)
                .putString("accountEmail", me.email)
                .apply()

            _state.update {
                it.copy(
                    accountName = me.name,
                    accountEmail = me.email,
                )
            }
        }
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