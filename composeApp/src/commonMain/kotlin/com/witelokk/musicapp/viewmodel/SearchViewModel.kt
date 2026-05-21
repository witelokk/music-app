package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.api.models.SearchResponse
import com.witelokk.musicapp.api.models.SearchResultItem
import com.witelokk.musicapp.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class SearchViewModelState(
    val isLoading: Boolean = false,
    val isFailure: Boolean = false,
    val results: SearchResponse? = null,
    val history: List<SearchResultItem> = listOf(),
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchViewModelState())
    val state = _state.asStateFlow()

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() = viewModelScope.launch {
        settings.searchHistory.collect { history ->
            _state.update {
                it.copy(
                    history = if (history.isEmpty()) {
                        listOf()
                    } else {
                        Json.decodeFromString(history)
                    }
                )
            }
        }
    }

    fun addToSearchHistory(result: SearchResultItem) {
        val history = _state.value.history.toMutableList()
        if (history.contains(result)) {
            return
        }

        history.add(result)
        if (history.size > 10) {
            history.removeAt(0)
        }

        viewModelScope.launch {
            settings.setSearchHistory(Json.encodeToString(history))
        }
        _state.update { it.copy(history = history) }
    }

    fun search(query: String) {
        if (query.isEmpty()) {
            return
        }

        _state.update { it.copy(isLoading = true) }

        launchCatching(action = "search for '$query'", onError = {
            _state.update {
                it.copy(
                    isLoading = false,
                    isFailure = true,
                    results = null
                )
            }
        }) {
            val results = searchRepository.search(query, page = 1, limit = 10)
            _state.update {
                it.copy(
                    isLoading = false,
                    isFailure = false,
                    results = results
                )
            }
        }
    }

    fun clearSearchHistory() = viewModelScope.launch {
        settings.setSearchHistory("[]")
        _state.update {
            it.copy(history = listOf())
        }
    }

    fun clearSearchState() {
        _state.update {
            it.copy(
                isLoading = false,
                isFailure = false,
                results = null
            )
        }
    }
}
