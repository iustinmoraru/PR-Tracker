package com.iustinmoraru.prtracker.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.iustinmoraru.prtracker.ui.auth.GoogleSignInHelper
import com.iustinmoraru.prtracker.ui.auth.LoginActivity
import com.iustinmoraru.prtracker.ui.common.ThemedActivity
import com.iustinmoraru.prtracker.ui.detail.WorkoutDetailActivity
import com.iustinmoraru.prtracker.ui.editor.WorkoutEditorActivity
import com.iustinmoraru.prtracker.ui.settings.SettingsActivity
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import kotlinx.coroutines.launch

/**
 * Main activity with the bottom navigation (Home, Progress, Timer, Profile).
 * Other screens (editor, details, settings) are separate activities started from here.
 */
class MainActivity : ThemedActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (container.authRepository.currentUser == null) {
            openLogin()
            return
        }

        val navigation = MainNavigation(
            addWorkout = { startActivity(WorkoutEditorActivity.intent(this)) },
            openWorkout = { startActivity(WorkoutDetailActivity.intent(this, it.id)) },
            editWorkout = { startActivity(WorkoutEditorActivity.intent(this, it.id)) },
            openRecord = { startActivity(WorkoutDetailActivity.intent(this, it.workoutId)) },
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
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Deadhang timer", color = PrTheme.colors.textMuted)
                    }
                },
            )
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
