package com.witelokk.musicapp

import com.witelokk.musicapp.auth.AuthState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private object PrefKeys {
    val ACCOUNT_NAME = stringPreferencesKey("account_name")
    val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val SEARCH_HISTORY = stringPreferencesKey("search_history")
    val THEME = stringPreferencesKey("theme")
    val SERVER_URL = stringPreferencesKey("server_url")
    val AUTO_DOWNLOAD_FAVORITES = booleanPreferencesKey("auto_download_favorites")
    val AUTO_DOWNLOAD_PLAYLISTS = booleanPreferencesKey("auto_download_playlists")
    val DOWNLOAD_ONLY_ON_WIFI = booleanPreferencesKey("download_only_on_wifi")
    val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    val accountName: Flow<String> =
        dataStore.data.map { prefs -> prefs[PrefKeys.ACCOUNT_NAME] ?: "" }

    val accountEmail: Flow<String> =
        dataStore.data.map { prefs -> prefs[PrefKeys.ACCOUNT_EMAIL] ?: "" }

    val accessToken: Flow<String> =
        dataStore.data.map { prefs -> prefs[PrefKeys.ACCESS_TOKEN] ?: "" }

    val refreshToken: Flow<String> =
        dataStore.data.map { prefs -> prefs[PrefKeys.REFRESH_TOKEN] ?: "" }

    val searchHistory: Flow<String> =
        dataStore.data.map { prefs -> prefs[PrefKeys.SEARCH_HISTORY] ?: "[]" }

    val theme: Flow<String> =
        dataStore.data.map { prefs -> prefs[PrefKeys.THEME] ?: "" }

    val serverUrl: Flow<String> =
        dataStore.data.map { prefs -> normalizeServerUrl(prefs[PrefKeys.SERVER_URL] ?: DEFAULT_BASE_URL) }

    val autoDownloadFavorites: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[PrefKeys.AUTO_DOWNLOAD_FAVORITES] ?: true }

    val autoDownloadPlaylists: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[PrefKeys.AUTO_DOWNLOAD_PLAYLISTS] ?: true }

    val downloadOnlyOnWifi: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[PrefKeys.DOWNLOAD_ONLY_ON_WIFI] ?: true }

    val useDynamicColors: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[PrefKeys.USE_DYNAMIC_COLORS] ?: true }

    val authState: Flow<AuthState> =
        combine(accountName, accountEmail, accessToken, refreshToken) { accountName, accountEmail, accessToken, refreshToken ->
            AuthState(
                accountName = accountName,
                accountEmail = accountEmail,
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        }

    suspend fun setAccountName(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.ACCOUNT_NAME] = value
        }
    }

    suspend fun setAccountEmail(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.ACCOUNT_EMAIL] = value
        }
    }

    suspend fun setAccessToken(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.ACCESS_TOKEN] = value
        }
    }

    suspend fun setRefreshToken(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.REFRESH_TOKEN] = value
        }
    }

    suspend fun updateAuthTokens(
        accessToken: String,
        refreshToken: String,
        accountEmail: String? = null,
    ) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.ACCESS_TOKEN] = accessToken
            prefs[PrefKeys.REFRESH_TOKEN] = refreshToken
            if (accountEmail != null) {
                prefs[PrefKeys.ACCOUNT_EMAIL] = accountEmail
            }
        }
    }

    suspend fun setSearchHistory(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.SEARCH_HISTORY] = value
        }
    }


    suspend fun setTheme(value: String) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.THEME] = value
        }
    }

    suspend fun setServerUrl(value: String) {
        val normalized = normalizeServerUrl(value)
        dataStore.edit { prefs ->
            prefs[PrefKeys.SERVER_URL] = normalized
        }
    }

    suspend fun setAutoDownloadFavorites(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.AUTO_DOWNLOAD_FAVORITES] = value
        }
    }

    suspend fun setAutoDownloadPlaylists(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.AUTO_DOWNLOAD_PLAYLISTS] = value
        }
    }

    suspend fun setDownloadOnlyOnWifi(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.DOWNLOAD_ONLY_ON_WIFI] = value
        }
    }

    suspend fun setUseDynamicColors(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.USE_DYNAMIC_COLORS] = value
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun clearAuth() {
        dataStore.edit { prefs ->
            prefs.remove(PrefKeys.ACCOUNT_NAME)
            prefs.remove(PrefKeys.ACCOUNT_EMAIL)
            prefs.remove(PrefKeys.ACCESS_TOKEN)
            prefs.remove(PrefKeys.REFRESH_TOKEN)
        }
    }
}
