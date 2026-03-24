package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
//    val theme: StateFlow<String> = settingsRepository.theme.stateIn(
//        viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = ""
//    )
    val _theme = MutableStateFlow("system")
    val theme = _theme.asStateFlow()

    init {
//        coro
    }

    fun setTheme(theme: String) {
//        viewModelScope.launch {
//            settingsRepository.setTheme(theme)
//        }
    }
}