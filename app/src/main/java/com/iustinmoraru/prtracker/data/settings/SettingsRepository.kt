package com.iustinmoraru.prtracker.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Local, per-device user preferences stored with Jetpack DataStore.
 *
 * Values are exposed as [StateFlow]s started eagerly in [scope], so every activity
 * can read the current theme synchronously when it draws its first frame.
 */
class SettingsRepository(context: Context, scope: CoroutineScope) {

    private val dataStore = context.applicationContext.settingsDataStore

    private val preferences = dataStore.data.catch { error ->
        // A corrupted or unreadable file must not crash the app: fall back to defaults.
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val themeMode: StateFlow<ThemeMode> = preferences
        .map { ThemeMode.fromStorage(it[KEY_THEME_MODE]) }
        .stateIn(scope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val notificationsEnabled: StateFlow<Boolean> = preferences
        .map { it[KEY_NOTIFICATIONS] ?: true }
        .stateIn(scope, SharingStarted.Eagerly, true)

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    }
}
