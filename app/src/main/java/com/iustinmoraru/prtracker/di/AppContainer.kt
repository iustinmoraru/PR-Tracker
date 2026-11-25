package com.iustinmoraru.prtracker.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iustinmoraru.prtracker.data.auth.AuthRepository
import com.iustinmoraru.prtracker.data.settings.SettingsRepository
import com.iustinmoraru.prtracker.data.workout.FirestoreWorkoutRepository
import com.iustinmoraru.prtracker.data.workout.WorkoutRepository
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

    /** False when the app was built without app/google-services.json (see README). */
    val isFirebaseConfigured: Boolean = FirebaseApp.getApps(context).isNotEmpty()

    val authRepository = AuthRepository(if (isFirebaseConfigured) FirebaseAuth.getInstance() else null)

    /** Only accessed after sign-in, which itself requires Firebase to be configured. */
    val workoutRepository: WorkoutRepository by lazy {
        FirestoreWorkoutRepository(FirebaseFirestore.getInstance(), authRepository)
    }
}
