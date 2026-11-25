package com.iustinmoraru.prtracker

import android.app.Application
import android.content.Context
import com.iustinmoraru.prtracker.di.AppContainer

/**
 * Application entry point. Creates the [AppContainer] shared by all activities.
 */
class PrTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifier.createChannels()
    }
}

/** Convenience accessor for the dependency container from any [Context]. */
val Context.appContainer: AppContainer
    get() = (applicationContext as PrTrackerApp).container
