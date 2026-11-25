package com.iustinmoraru.prtracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iustinmoraru.prtracker.PrTrackerApp
import com.iustinmoraru.prtracker.data.auth.AuthError
import com.iustinmoraru.prtracker.data.auth.AuthRepository
import com.iustinmoraru.prtracker.data.auth.UserProfile
import com.iustinmoraru.prtracker.data.auth.toAuthError
import com.iustinmoraru.prtracker.domain.validation.AuthValidator
import com.iustinmoraru.prtracker.domain.validation.FieldError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { LOGIN, SIGN_UP }

/** Everything the login screen needs to draw itself. */
data class LoginUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: FieldError? = null,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val authError: AuthError? = null,
    val isLoading: Boolean = false,
    val isFirebaseConfigured: Boolean = true,
    val signedInUser: UserProfile? = null,
)

/**
 * Handles validation and sign-in logic for [LoginActivity].
 * The view only forwards user input here and renders [uiState].
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(isFirebaseConfigured = authRepository.isConfigured))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null, authError = null) }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null, authError = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null, authError = null) }

    fun toggleMode() = _uiState.update {
        it.copy(
            mode = if (it.mode == AuthMode.LOGIN) AuthMode.SIGN_UP else AuthMode.LOGIN,
            nameError = null,
            emailError = null,
            passwordError = null,
            authError = null,
        )
    }

    /** Validates the form and, if it is valid, signs in or creates the account. */
    fun submitEmailForm() {
        val state = _uiState.value
        if (state.isLoading) return
        val isSignUp = state.mode == AuthMode.SIGN_UP

        val nameError = if (isSignUp) AuthValidator.validateName(state.name) else null
        val emailError = AuthValidator.validateEmail(state.email)
        val passwordError = AuthValidator.validatePassword(state.password, isSignUp)
        if (nameError != null || emailError != null || passwordError != null) {
            _uiState.update { it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError) }
            return
        }

        runSignIn {
            val email = state.email.trim()
            if (isSignUp) {
                authRepository.signUpWithEmail(state.name.trim(), email, state.password)
            } else {
                authRepository.signInWithEmail(email, state.password)
            }
        }
    }

    /** Called by the activity after Credential Manager returns (or fails to return) a token. */
    fun onGoogleResult(outcome: GoogleSignInHelper.Outcome) {
        when (outcome) {
            is GoogleSignInHelper.Outcome.Token -> runSignIn { authRepository.signInWithGoogle(outcome.idToken) }
            is GoogleSignInHelper.Outcome.Error -> _uiState.update {
                // Closing the Google sheet is a normal user choice, not an error worth showing.
                it.copy(isLoading = false, authError = outcome.error.takeUnless { e -> e == AuthError.GOOGLE_CANCELLED })
            }
        }
    }

    fun onGoogleStarted() = _uiState.update { it.copy(isLoading = true, authError = null) }

    private fun runSignIn(action: suspend () -> Result<UserProfile>) {
        _uiState.update { it.copy(isLoading = true, authError = null) }
        viewModelScope.launch {
            action()
                .onSuccess { user -> _uiState.update { it.copy(isLoading = false, signedInUser = user) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, authError = error.toAuthError()) } }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrTrackerApp
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}
