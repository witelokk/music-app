package com.witelokk.musicapp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object PrefKeys {
    val ACCOUNT_NAME = stringPreferencesKey("account_name")
    val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val SEARCH_HISTORY = stringPreferencesKey("search_history")
    val THEME = stringPreferencesKey("theme")
    val SERVER_URL = stringPreferencesKey("server_url")
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
        dataStore.data.map { prefs -> prefs[PrefKeys.SERVER_URL] ?: DEFAULT_BASE_URL }

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
        val normalized = if (value.isBlank()) DEFAULT_BASE_URL else value
        dataStore.edit { prefs ->
            prefs[PrefKeys.SERVER_URL] = normalized
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
