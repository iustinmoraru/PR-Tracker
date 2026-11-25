package com.iustinmoraru.prtracker.domain.validation

/** Reasons a form field can be rejected. The UI maps each one to a message. */
enum class FieldError {
    REQUIRED,
    INVALID_EMAIL,
    PASSWORD_TOO_SHORT,
    PASSWORD_NEEDS_LETTER_AND_DIGIT,
    NAME_TOO_SHORT,
    NAME_TOO_LONG,
}

/**
 * Client-side validation for the login and sign-up forms.
 * Pure Kotlin (no Android types) so it can be unit tested on the JVM.
 */
object AuthValidator {

    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 40

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): FieldError? {
        val value = email.trim()
        return when {
            value.isEmpty() -> FieldError.REQUIRED
            !emailRegex.matches(value) -> FieldError.INVALID_EMAIL
            else -> null
        }
    }

    /**
     * On sign-up the password must be strong; on login we only check that it is present,
     * because older accounts may predate the current rules.
     */
    fun validatePassword(password: String, isSignUp: Boolean): FieldError? = when {
        password.isEmpty() -> FieldError.REQUIRED
        !isSignUp -> null
        password.length < MIN_PASSWORD_LENGTH -> FieldError.PASSWORD_TOO_SHORT
        password.none { it.isLetter() } || password.none { it.isDigit() } -> FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT
        else -> null
    }

    fun validateName(name: String): FieldError? {
        val value = name.trim()
        return when {
            value.isEmpty() -> FieldError.REQUIRED
            value.length < MIN_NAME_LENGTH -> FieldError.NAME_TOO_SHORT
            value.length > MAX_NAME_LENGTH -> FieldError.NAME_TOO_LONG
            else -> null
        }
    }
}
