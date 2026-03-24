package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ThemeViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _theme = MutableStateFlow("system")
    val theme = _theme.asStateFlow()

    init {
        runBlocking { _theme.value = settingsRepository.theme.first() }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
        _theme.value = theme
    }
}