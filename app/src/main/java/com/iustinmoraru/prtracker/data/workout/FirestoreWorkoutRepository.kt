package com.iustinmoraru.prtracker.data.workout

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.iustinmoraru.prtracker.data.auth.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Cloud Firestore implementation. Data layout:
 *
 * ```
 * users/{uid}/workouts/{workoutId}
 *   name, scheduledAt (Timestamp), durationMinutes, status, notes, updatedAt,
 *   exercises: [{ id, name, category, sets, reps, weightKg }]
 * ```
 *
 * Firestore keeps an offline cache, so the app works without internet and syncs later.
 * Writes wait at most [SERVER_ACK_TIMEOUT_MS] for the server; after that they are reported
 * as [SaveResult.PENDING_SYNC] instead of blocking the UI.
 */
class FirestoreWorkoutRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : WorkoutRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeWorkouts(): Flow<List<Workout>> =
        authRepository.observeUser()
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList()) else observeCollection(uid)
            }

    private fun observeCollection(uid: String): Flow<List<Workout>> = callbackFlow {
        val registration = workouts(uid)
            .orderBy(FIELD_SCHEDULED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toWorkout() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    override fun observeWorkout(id: String): Flow<Workout?> {
        val uid = authRepository.currentUser?.uid ?: return flowOf(null)
        return callbackFlow {
            val registration = workouts(uid).document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toWorkout())
            }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun save(workout: Workout): Result<Pair<String, SaveResult>> = runCatching {
        val collection = workouts(requireUid())
        val document = if (workout.id.isBlank()) collection.document() else collection.document(workout.id)
        val result = document.set(workout.toMap()).awaitOrPending()
        document.id to result
    }

    override suspend fun setStatus(id: String, status: WorkoutStatus): Result<SaveResult> = runCatching {
        workouts(requireUid()).document(id)
            .set(mapOf(FIELD_STATUS to status.name, FIELD_UPDATED_AT to Timestamp.now()), SetOptions.merge())
            .awaitOrPending()
    }

    override suspend fun delete(id: String): Result<SaveResult> = runCatching {
        workouts(requireUid()).document(id).delete().awaitOrPending()
    }

    private fun workouts(uid: String): CollectionReference =
        firestore.collection(COLLECTION_USERS).document(uid).collection(COLLECTION_WORKOUTS)

    private fun requireUid(): String =
        authRepository.currentUser?.uid ?: throw IllegalStateException("No signed-in user")

    /** Waits for the server acknowledgement, but never longer than the timeout. */
    private suspend fun Task<Void>.awaitOrPending(): SaveResult =
        // Task<Void>.await() returns null on success, so return an explicit marker from the block.
        if (withTimeoutOrNull(SERVER_ACK_TIMEOUT_MS) { await(); true } == true) SaveResult.SYNCED else SaveResult.PENDING_SYNC

    private companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_WORKOUTS = "workouts"
        const val FIELD_NAME = "name"
        const val FIELD_SCHEDULED_AT = "scheduledAt"
        const val FIELD_DURATION = "durationMinutes"
        const val FIELD_STATUS = "status"
        const val FIELD_NOTES = "notes"
        const val FIELD_EXERCISES = "exercises"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val SERVER_ACK_TIMEOUT_MS = 4_000L
    }

    // ---- Mapping between the model and Firestore documents ----

    private fun Workout.toMap(): Map<String, Any> = mapOf(
        FIELD_NAME to name.trim(),
        FIELD_SCHEDULED_AT to date.toTimestamp(),
        FIELD_DURATION to durationMinutes,
        FIELD_STATUS to status.name,
        FIELD_NOTES to notes.trim(),
        FIELD_UPDATED_AT to Timestamp.now(),
        FIELD_EXERCISES to exercises.map { exercise ->
            mapOf(
                "id" to exercise.id,
                "name" to exercise.name.trim(),
                "category" to exercise.category.name,
                "sets" to exercise.sets,
                "reps" to exercise.reps,
                "weightKg" to exercise.weightKg,
            )
        },
    )

    /** Returns null for documents that are missing or malformed instead of crashing. */
    private fun DocumentSnapshot.toWorkout(): Workout? {
        if (!exists()) return null
        val name = getString(FIELD_NAME) ?: return null
        val scheduledAt = getTimestamp(FIELD_SCHEDULED_AT) ?: return null
        @Suppress("UNCHECKED_CAST")
        val rawExercises = get(FIELD_EXERCISES) as? List<Map<String, Any?>> ?: emptyList()
        return Workout(
            id = id,
            name = name,
            date = scheduledAt.toLocalDate(),
            durationMinutes = getLong(FIELD_DURATION)?.toInt() ?: Workout.MIN_DURATION,
            status = enumValueOrDefault(getString(FIELD_STATUS), WorkoutStatus.PLANNED),
            notes = getString(FIELD_NOTES).orEmpty(),
            exercises = rawExercises.mapNotNull { it.toExercise() },
        )
    }

    private fun Map<String, Any?>.toExercise(): Exercise? {
        val name = this["name"] as? String ?: return null
        return Exercise(
            id = this["id"] as? String ?: name,
            name = name,
            category = enumValueOrDefault(this["category"] as? String, ExerciseCategory.STRENGTH),
            sets = (this["sets"] as? Number)?.toInt() ?: 1,
            reps = (this["reps"] as? Number)?.toInt() ?: 1,
            weightKg = (this["weightKg"] as? Number)?.toDouble() ?: 0.0,
        )
    }
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default

private fun LocalDate.toTimestamp(): Timestamp =
    Timestamp(Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant()))

private fun Timestamp.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(toDate().time).atZone(ZoneId.systemDefault()).toLocalDate()
