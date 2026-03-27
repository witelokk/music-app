package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.api.apis.UsersApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsScreenState(
    val accountName: String? = null,
    val accountEmail: String? = null,
    val theme: String? = null,
    val isLoggedOut: Boolean = false,
)

class SettingsScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val usersApi: UsersApi,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
        loadSettings()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val response = usersApi.usersMeGet()

            if (!response.success) {
                return@launch
            }

            val me = response.body()

            settingsRepository.setAccountName(me.name)
            settingsRepository.setAccountEmail(me.email)

            _state.update {
                it.copy(
                    accountName = me.name,
                    accountEmail = me.email,
                )
            }
        }
    }

    private fun loadSettings() = viewModelScope.launch {
        settingsRepository.theme.collect { theme ->
            _state.update {
                it.copy(
                    theme = theme
                )
            }
        }

        settingsRepository.accountEmail.collect { accountEmail ->
            _state.update {
                it.copy(
                    accountEmail = accountEmail
                )
            }
        }

        settingsRepository.accountName.collect { accountName ->
            _state.update {
                it.copy(
                    accountName = accountName
                )
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
        _state.update { it.copy(theme = theme) }
    }

    fun logout() {
        viewModelScope.launch {
            settingsRepository.clear()
        }
        _state.update { it.copy(isLoggedOut = true) }
    }
}