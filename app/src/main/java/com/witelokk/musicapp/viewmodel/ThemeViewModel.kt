package com.witelokk.musicapp.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ThemeViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {
    private val _theme = MutableStateFlow(loadTheme())
    val theme = _theme.asStateFlow()

    private fun loadTheme(): String {
        return sharedPreferences.getString("theme", "system") ?: "system"
    }

    fun setTheme(theme: String) {
        _theme.update { theme }
    }
}