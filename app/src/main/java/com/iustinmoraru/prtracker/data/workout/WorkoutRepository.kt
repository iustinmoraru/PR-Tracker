package com.iustinmoraru.prtracker.data.workout

import kotlinx.coroutines.flow.Flow

/** Result of a write: confirmed by the server, or stored locally and synced later. */
enum class SaveResult { SYNCED, PENDING_SYNC }

/**
 * Access to the signed-in user's workouts. The interface keeps the UI independent of
 * Firestore, so the storage can be swapped or faked in tests.
 */
interface WorkoutRepository {

    /** All workouts of the current user, newest first; updates live. */
    fun observeWorkouts(): Flow<List<Workout>>

    /** A single workout (null if it was deleted); updates live. */
    fun observeWorkout(id: String): Flow<Workout?>

    /** Creates the workout when [Workout.id] is blank, otherwise overwrites it. Returns the id. */
    suspend fun save(workout: Workout): Result<Pair<String, SaveResult>>

    suspend fun setStatus(id: String, status: WorkoutStatus): Result<SaveResult>

    suspend fun delete(id: String): Result<SaveResult>
}
