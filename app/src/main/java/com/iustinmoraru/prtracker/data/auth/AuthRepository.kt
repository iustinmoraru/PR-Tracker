package com.iustinmoraru.prtracker.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Authentication backed by Firebase Authentication (Google and email/password).
 *
 * [auth] is null when the app was built without `google-services.json`; every call then
 * fails with [AuthError.NOT_CONFIGURED] instead of crashing.
 */
class AuthRepository(private val auth: FirebaseAuth?) {

    val isConfigured: Boolean
        get() = auth != null

    val currentUser: UserProfile?
        get() = auth?.currentUser?.toProfile()

    /** Emits the signed-in user (or null) every time the authentication state changes. */
    fun observeUser(): Flow<UserProfile?> {
        val firebaseAuth = auth ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toProfile()) }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> = runAuth {
        requireAuth().signInWithEmailAndPassword(email, password).await().user
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<UserProfile> = runAuth {
        val firebaseAuth = requireAuth()
        val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
            ?: throw AuthException(AuthError.UNKNOWN)
        val profileUpdate = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(profileUpdate).await()
        firebaseAuth.currentUser
    }

    /** Exchanges the Google ID token obtained through Credential Manager for a Firebase session. */
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile> = runAuth {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        requireAuth().signInWithCredential(credential).await().user
    }

    fun signOut() {
        auth?.signOut()
    }

    private fun requireAuth(): FirebaseAuth = auth ?: throw AuthException(AuthError.NOT_CONFIGURED)

    private inline fun runAuth(block: () -> FirebaseUser?): Result<UserProfile> = try {
        val user = block() ?: throw AuthException(AuthError.UNKNOWN)
        Result.success(user.toProfile())
    } catch (error: Exception) {
        Result.failure(if (error is AuthException) error else AuthException(error.toAuthError(), error))
    }
}

private fun FirebaseUser.toProfile() = UserProfile(
    uid = uid,
    displayName = displayName.orEmpty(),
    email = email.orEmpty(),
    photoUrl = photoUrl?.toString(),
    isGoogleAccount = providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID },
)
