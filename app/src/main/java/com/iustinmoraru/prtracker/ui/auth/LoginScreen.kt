package com.iustinmoraru.prtracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.ui.components.AppTextField
import com.iustinmoraru.prtracker.ui.components.ErrorMessage
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.PrimaryButton
import com.iustinmoraru.prtracker.ui.components.SecondaryButton
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.theme.PrTrackerTheme

/** Stateless login / sign-up screen (Figma "AuthSheet"), driven entirely by [state]. */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    onGoogleClick: () -> Unit,
) {
    val colors = PrTheme.colors
    val focusManager = LocalFocusManager.current
    val isSignUp = state.mode == AuthMode.SIGN_UP

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            BrandMark()
            Spacer(Modifier.height(22.dp))
            Eyebrow(stringResource(if (isSignUp) R.string.auth_eyebrow_signup else R.string.auth_eyebrow_login))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(if (isSignUp) R.string.auth_title_signup else R.string.auth_title_login),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )

            if (!state.isFirebaseConfigured) {
                Spacer(Modifier.height(16.dp))
                ErrorMessage(stringResource(R.string.auth_error_not_configured))
            }

            Spacer(Modifier.height(24.dp))
            SecondaryButton(
                text = stringResource(R.string.auth_google),
                onClick = onGoogleClick,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                leadingContent = {
                    Image(painterResource(R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(20.dp))
                },
            )

            OrDivider()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedVisibility(visible = isSignUp) {
                    AppTextField(
                        label = stringResource(R.string.auth_name),
                        value = state.name,
                        onValueChange = onNameChange,
                        error = state.nameError?.let { stringResource(it.messageRes()) },
                        enabled = !state.isLoading,
                    )
                }
                AppTextField(
                    label = stringResource(R.string.auth_email),
                    value = state.email,
                    onValueChange = onEmailChange,
                    error = state.emailError?.let { stringResource(it.messageRes()) },
                    keyboardType = KeyboardType.Email,
                    enabled = !state.isLoading,
                )
                AppTextField(
                    label = stringResource(R.string.auth_password),
                    value = state.password,
                    onValueChange = onPasswordChange,
                    error = state.passwordError?.let { stringResource(it.messageRes()) },
                    placeholder = if (isSignUp) stringResource(R.string.auth_password_hint) else null,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    enabled = !state.isLoading,
                )
            }

            state.authError?.let {
                Spacer(Modifier.height(12.dp))
                ErrorMessage(stringResource(it.messageRes()))
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = stringResource(if (isSignUp) R.string.auth_submit_signup else R.string.auth_submit_login),
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                loading = state.isLoading,
                trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
            )

            TextButton(
                onClick = onToggleMode,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(
                    text = stringResource(if (isSignUp) R.string.auth_switch_to_login else R.string.auth_switch_to_signup),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

/** The tilted lime square with a dumbbell from the design. */
@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .rotate(-4f)
            .size(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Lime),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = OnLime, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun OrDivider() {
    val colors = PrTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = colors.border)
        Text(stringResource(R.string.auth_or), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        HorizontalDivider(Modifier.weight(1f), color = colors.border)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF090A0E)
@Composable
private fun LoginScreenPreview() {
    PrTrackerTheme(darkTheme = true) {
        LoginScreen(LoginUiState(mode = AuthMode.SIGN_UP), {}, {}, {}, {}, {}, {})
    }
}
