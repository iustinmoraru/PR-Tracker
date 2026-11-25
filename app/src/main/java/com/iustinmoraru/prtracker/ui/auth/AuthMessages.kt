package com.iustinmoraru.prtracker.ui.auth

import androidx.annotation.StringRes
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.auth.AuthError
import com.iustinmoraru.prtracker.domain.validation.FieldError

/** Maps validation errors to user-facing messages. */
@StringRes
fun FieldError.messageRes(): Int = when (this) {
    FieldError.REQUIRED -> R.string.error_required
    FieldError.INVALID_EMAIL -> R.string.error_invalid_email
    FieldError.PASSWORD_TOO_SHORT -> R.string.error_password_short
    FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT -> R.string.error_password_weak
    FieldError.NAME_TOO_SHORT -> R.string.error_name_short
    FieldError.NAME_TOO_LONG -> R.string.error_name_long
}

/** Maps authentication failures to user-facing messages. */
@StringRes
fun AuthError.messageRes(): Int = when (this) {
    AuthError.NOT_CONFIGURED -> R.string.auth_error_not_configured
    AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthError.USER_NOT_FOUND -> R.string.auth_error_user_not_found
    AuthError.EMAIL_IN_USE -> R.string.auth_error_email_in_use
    AuthError.WEAK_PASSWORD -> R.string.error_password_weak
    AuthError.NETWORK -> R.string.auth_error_network
    AuthError.TOO_MANY_REQUESTS -> R.string.auth_error_too_many
    AuthError.GOOGLE_CANCELLED -> R.string.auth_error_google_failed
    AuthError.GOOGLE_NO_ACCOUNT -> R.string.auth_error_google_no_account
    AuthError.GOOGLE_FAILED -> R.string.auth_error_google_failed
    AuthError.UNKNOWN -> R.string.auth_error_unknown
}
