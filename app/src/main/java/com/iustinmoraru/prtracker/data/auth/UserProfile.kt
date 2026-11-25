package com.iustinmoraru.prtracker.data.auth

/** The signed-in user, independent of the Firebase SDK types. */
data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    val isGoogleAccount: Boolean,
) {
    /** First name used in greetings ("Salut, Iustin."). */
    val firstName: String
        get() = displayName.trim().split(" ").firstOrNull().orEmpty().ifBlank { email.substringBefore('@') }

    /** Up to two initials for the avatar. */
    val initials: String
        get() {
            val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
            val letters = when {
                parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
                parts.size == 1 -> parts[0].take(2)
                else -> email.take(2)
            }
            return letters.uppercase()
        }
}
