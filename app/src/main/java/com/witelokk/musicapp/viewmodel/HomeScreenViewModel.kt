package com.witelokk.musicapp.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.api.models.SearchResult
import com.witelokk.musicapp.api.models.SearchResultItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.UnknownHostException

data class HomeViewModelState(
    val isLoading: Boolean = false,
    val isFailure: Boolean = false,
    val searchResults: SearchResult? = null,
    val searchHistory: List<SearchResultItem> = emptyList()
)

class HomeScreenViewModel(
    private val searchApi: SearchApi,
    private val sharedPreferences: SharedPreferences,
    private val json: Json,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewModelState())
    val state = _state.asStateFlow()

    init {
        loadSearchHistory()
    }

    fun loadSearchHistory() {
        val history = sharedPreferences.getString("searchHistory", null)

        if (history != null) {
            _state.update {
                it.copy(searchHistory = json.decodeFromString(history))
            }
        } else {
            _state.update {
                it.copy(searchHistory = emptyList())
            }
        }
    }

    fun addToSearchHistory(result: SearchResultItem) {
        val history = _state.value.searchHistory.toMutableList()
        history.add(result)
        if (history.size > 10) {
            history.removeAt(0)
        }
        sharedPreferences.edit().putString("searchHistory", json.encodeToString(history)).apply()
        _state.update { it.copy(searchHistory = history) }
    }

    fun search(query: String) {
        if (query.isEmpty()) {
            return
        }

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                searchApi.searchGet(query, type = null, page = "1", limit = "10").let { response ->
                    if (response.success) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isFailure = false,
                                searchResults = response.body()
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isFailure = true,
                                searchResults = null
                            )
                        }
                    }
                }
            } catch (_: UnknownHostException) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isFailure = true,
                        searchResults = null
                    )
                }
            }
        }
    }

    fun clearSearchHistory() {
        sharedPreferences.edit().remove("searchHistory").apply()
        _state.update {
            it.copy(searchHistory = emptyList())
        }
    }

    fun clearSearchState() {
        _state.update {
            it.copy(
                isLoading = false,
                isFailure = false,
                searchResults = null
            )
        }
    }
}