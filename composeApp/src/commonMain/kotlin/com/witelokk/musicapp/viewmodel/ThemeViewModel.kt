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

    private val _dynamicColor = MutableStateFlow(true)
    val dynamicColor = _dynamicColor.asStateFlow()

    init {
        runBlocking {
            _theme.value = settingsRepository.theme.first()
            _dynamicColor.value = settingsRepository.useDynamicColors.first()
        }
        viewModelScope.launch {
            settingsRepository.theme.collect { theme ->
                _theme.value = theme
            }
        }
        viewModelScope.launch {
            settingsRepository.useDynamicColors.collect { enabled ->
                _dynamicColor.value = enabled
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
        _theme.value = theme
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColors(enabled)
        }
        _dynamicColor.value = enabled
    }
}
