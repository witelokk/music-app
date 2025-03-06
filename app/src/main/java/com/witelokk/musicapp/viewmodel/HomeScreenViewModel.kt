package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsSearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException

data class HomeViewModelState(
    val isLoading: Boolean = true,
    val isFailure: Boolean = false,
    val searchResults: ComwitelokkmusicmodelsSearchResult? = null,
)

class HomeScreenViewModel(
    private val searchApi: SearchApi
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewModelState())
    val state = _state.asStateFlow()

    fun search(query: String) {
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

    fun clearResults() {
        _state.update {
            it.copy(
                isLoading = false,
                isFailure = false,
                searchResults = null
            )
        }
    }
}