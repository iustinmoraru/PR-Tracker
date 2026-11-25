package com.iustinmoraru.prtracker.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Authentication failures the UI knows how to explain to the user. */
enum class AuthError {
    NOT_CONFIGURED,
    INVALID_CREDENTIALS,
    USER_NOT_FOUND,
    EMAIL_IN_USE,
    WEAK_PASSWORD,
    NETWORK,
    TOO_MANY_REQUESTS,
    GOOGLE_CANCELLED,
    GOOGLE_NO_ACCOUNT,
    GOOGLE_FAILED,
    UNKNOWN,
}

/** Exception carrying an [AuthError], thrown by [AuthRepository]. */
class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)

/** Translates Firebase exceptions into [AuthError] values. Order matters: subclasses first. */
fun Throwable.toAuthError(): AuthError = when (this) {
    is AuthException -> error
    is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is FirebaseAuthInvalidUserException -> AuthError.USER_NOT_FOUND
    is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
    is FirebaseNetworkException -> AuthError.NETWORK
    is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
    else -> AuthError.UNKNOWN
}
