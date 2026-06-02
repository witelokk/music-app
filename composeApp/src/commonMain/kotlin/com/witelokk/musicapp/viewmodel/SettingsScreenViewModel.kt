package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.OfflineLibrarySync
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.auth.AuthSession
import com.witelokk.musicapp.CompatAuthApi
import com.witelokk.musicapp.loge
import com.witelokk.musicapp.openAppLanguageSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsScreenState(
    val accountName: String? = null,
    val accountEmail: String? = null,
    val theme: String? = null,
    val useDynamicColors: Boolean = true,
    val autoDownloadFavorites: Boolean = true,
    val autoDownloadPlaylists: Boolean = true,
    val downloadOnlyOnWifi: Boolean = true,
    val isLoggedOut: Boolean = false,
)

class SettingsScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val authApi: CompatAuthApi,
    private val authSession: AuthSession,
    private val offlineLibrarySync: OfflineLibrarySync,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
        loadSettings()
    }

    private fun loadProfile() {
        launchCatching(action = "load settings profile") {
            val response = authApi.getCurrentUser()

            if (response.logIfFailure("load settings profile")) {
                return@launchCatching
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

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.theme,
                settingsRepository.accountEmail,
                settingsRepository.accountName,
            ) { theme, accountEmail, accountName ->
                Triple(theme, accountEmail, accountName)
            }.collect { (theme, accountEmail, accountName) ->
                _state.update {
                    it.copy(
                        theme = theme,
                        accountEmail = accountEmail,
                        accountName = accountName,
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                settingsRepository.autoDownloadFavorites,
                settingsRepository.autoDownloadPlaylists,
                settingsRepository.downloadOnlyOnWifi,
            ) { autoDownloadFavorites, autoDownloadPlaylists, downloadOnlyOnWifi ->
                Triple(autoDownloadFavorites, autoDownloadPlaylists, downloadOnlyOnWifi)
            }.collect { (autoDownloadFavorites, autoDownloadPlaylists, downloadOnlyOnWifi) ->
                _state.update {
                    it.copy(
                        autoDownloadFavorites = autoDownloadFavorites,
                        autoDownloadPlaylists = autoDownloadPlaylists,
                        downloadOnlyOnWifi = downloadOnlyOnWifi,
                    )
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.useDynamicColors.collect { useDynamicColors ->
                _state.update {
                    it.copy(useDynamicColors = useDynamicColors)
                }
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
        _state.update { it.copy(theme = theme) }
    }

    fun setUseDynamicColors(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColors(value)
        }
        _state.update { it.copy(useDynamicColors = value) }
    }

    fun openLanguageSettings(): Boolean {
        return openAppLanguageSettings()
    }

    fun setAutoDownloadFavorites(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoDownloadFavorites(value)
            if (value) {
                syncOfflineLibrary()
            }
        }
        _state.update { it.copy(autoDownloadFavorites = value) }
    }

    fun setAutoDownloadPlaylists(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoDownloadPlaylists(value)
            if (value) {
                syncOfflineLibrary()
            }
        }
        _state.update { it.copy(autoDownloadPlaylists = value) }
    }

    fun setDownloadOnlyOnWifi(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDownloadOnlyOnWifi(value)
            if (!value) {
                syncOfflineLibrary()
            }
        }
        _state.update { it.copy(downloadOnlyOnWifi = value) }
    }

    fun logout() {
        viewModelScope.launch {
            authSession.logout()
            _state.update { it.copy(isLoggedOut = true) }
        }
    }

    fun clearAppData() {
        viewModelScope.launch {
            settingsRepository.clear()
            _state.update { it.copy(isLoggedOut = true) }
        }
    }

    private suspend fun syncOfflineLibrary() {
        try {
            offlineLibrarySync.sync()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }
            loge("APP_LAUNCH_CACHE", "Sync after settings change failed: ${throwable.stackTraceToString()}")
        }
    }
}
