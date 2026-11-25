package com.iustinmoraru.prtracker.di

import android.content.Context
import com.iustinmoraru.prtracker.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency container (a lightweight alternative to Hilt/Dagger).
 *
 * It is created once in [com.iustinmoraru.prtracker.PrTrackerApp] and owns every
 * repository, so activities and view models never construct data sources themselves.
 */
class AppContainer(context: Context) {

    /** Scope tied to the application process, used for app-wide flows. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsRepository = SettingsRepository(context, applicationScope)
}
