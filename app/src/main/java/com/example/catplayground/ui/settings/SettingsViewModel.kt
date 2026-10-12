package com.example.catplayground.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catplayground.data.local.SettingsStore
import com.example.catplayground.data.repository.CatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val repository: CatRepository
) : ViewModel() {

    val themeMode: StateFlow<String> = settingsStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val reduceMotion: StateFlow<Boolean> = settingsStore.reduceMotion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsStore.setThemeMode(mode)
        }
    }

    fun setReduceMotion(reduce: Boolean) {
        viewModelScope.launch {
            settingsStore.setReduceMotion(reduce)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
