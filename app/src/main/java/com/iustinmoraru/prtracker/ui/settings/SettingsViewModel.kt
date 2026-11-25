package com.iustinmoraru.prtracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iustinmoraru.prtracker.PrTrackerApp
import com.iustinmoraru.prtracker.data.settings.SettingsRepository
import com.iustinmoraru.prtracker.data.settings.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Exposes and updates the locally stored user preferences. */
class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode
    val notificationsEnabled: StateFlow<Boolean> = settings.notificationsEnabled

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch { settings.setNotificationsEnabled(enabled) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrTrackerApp
                SettingsViewModel(app.container.settingsRepository)
            }
        }
    }
}
