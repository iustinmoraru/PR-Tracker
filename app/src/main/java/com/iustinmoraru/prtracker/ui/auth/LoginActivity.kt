package com.iustinmoraru.prtracker.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iustinmoraru.prtracker.ui.common.ThemedActivity
import com.iustinmoraru.prtracker.ui.main.MainActivity
import kotlinx.coroutines.launch

/**
 * Entry point of the app. Users sign in with Google or with email and password;
 * already signed-in users are sent straight to [MainActivity].
 */
class LoginActivity : ThemedActivity() {

    private val viewModel: LoginViewModel by viewModels { LoginViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (container.authRepository.currentUser != null) {
            openMain()
            return
        }

        val googleSignIn = GoogleSignInHelper(this)

        setThemedContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            LaunchedEffect(state.signedInUser) {
                if (state.signedInUser != null) openMain()
            }

            LoginScreen(
                state = state,
                onNameChange = viewModel::onNameChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onSubmit = viewModel::submitEmailForm,
                onToggleMode = viewModel::toggleMode,
                onGoogleClick = {
                    viewModel.onGoogleStarted()
                    scope.launch { viewModel.onGoogleResult(googleSignIn.requestIdToken()) }
                },
            )
        }
    }

    private fun openMain() {
        startActivity(MainActivity.intent(this))
        finish()
    }

    companion object {
        /** Opens the login screen and clears the back stack (used after logout). */
        fun intent(context: Context) = Intent(context, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
