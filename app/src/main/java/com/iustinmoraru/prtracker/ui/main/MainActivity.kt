package com.iustinmoraru.prtracker.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.iustinmoraru.prtracker.ui.auth.GoogleSignInHelper
import com.iustinmoraru.prtracker.ui.auth.LoginActivity
import com.iustinmoraru.prtracker.ui.common.ThemedActivity
import com.iustinmoraru.prtracker.ui.detail.WorkoutDetailActivity
import com.iustinmoraru.prtracker.ui.editor.WorkoutEditorActivity
import com.iustinmoraru.prtracker.ui.history.ExerciseHistoryActivity
import com.iustinmoraru.prtracker.ui.settings.SettingsActivity
import com.iustinmoraru.prtracker.ui.timer.TimerTab
import com.iustinmoraru.prtracker.ui.timer.TimerViewModel
import kotlinx.coroutines.launch

/**
 * Main activity with the bottom navigation (Home, Progress, Timer, Profile).
 * Other screens (editor, details, settings) are separate activities started from here.
 */
class MainActivity : ThemedActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private val timerViewModel: TimerViewModel by viewModels { TimerViewModel.Factory }

    /** Android 13+ asks the user before an app may post notifications. */
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* the app works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (container.authRepository.currentUser == null) {
            openLogin()
            return
        }

        requestNotificationPermissionIfNeeded()

        val navigation = MainNavigation(
            addWorkout = { startActivity(WorkoutEditorActivity.intent(this)) },
            openWorkout = { startActivity(WorkoutDetailActivity.intent(this, it.id)) },
            editWorkout = { startActivity(WorkoutEditorActivity.intent(this, it.id)) },
            openRecord = { startActivity(ExerciseHistoryActivity.intent(this, it)) },
            openSettings = { startActivity(SettingsActivity.intent(this)) },
        )

        setThemedContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.signedOut) {
                if (state.signedOut) openLogin()
            }

            MainScreen(
                state = state,
                navigation = navigation,
                onToggleStatus = viewModel::toggleStatus,
                onDelete = viewModel::delete,
                onSignOut = ::signOut,
                onMessageShown = viewModel::onMessageShown,
                timerContent = {
                    val timerState by timerViewModel.uiState.collectAsStateWithLifecycle()
                    TimerTab(
                        state = timerState,
                        onToggle = timerViewModel::toggle,
                        onReset = timerViewModel::reset,
                        onSelectDuration = timerViewModel::selectDuration,
                    )
                },
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            container.settingsRepository.notificationsEnabled.value &&
            !container.notifier.hasPermission()
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** Signs out of Firebase and forgets the Google account choice. */
    private fun signOut() {
        lifecycleScope.launch {
            GoogleSignInHelper(this@MainActivity).clearCredentials()
            viewModel.signOut()
        }
    }

    private fun openLogin() {
        startActivity(LoginActivity.intent(this))
        finish()
    }

    companion object {
        fun intent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
