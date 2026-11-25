package com.iustinmoraru.prtracker.domain.validation

import com.iustinmoraru.prtracker.data.workout.Workout

/** Problems that prevent a workout from being saved. */
enum class WorkoutError {
    NAME_REQUIRED,
    NAME_TOO_LONG,
    NO_EXERCISES,
    TOO_MANY_EXERCISES,
    INVALID_DURATION,
    NOTES_TOO_LONG,
}

/** Validates workouts before they are written to the database. */
object WorkoutValidator {

    const val MIN_EXERCISE_NAME_LENGTH = 2

    fun validate(workout: Workout): WorkoutError? = when {
        workout.name.isBlank() -> WorkoutError.NAME_REQUIRED
        workout.name.trim().length > Workout.MAX_NAME_LENGTH -> WorkoutError.NAME_TOO_LONG
        workout.durationMinutes !in Workout.MIN_DURATION..Workout.MAX_DURATION -> WorkoutError.INVALID_DURATION
        workout.exercises.isEmpty() -> WorkoutError.NO_EXERCISES
        workout.exercises.size > Workout.MAX_EXERCISES -> WorkoutError.TOO_MANY_EXERCISES
        workout.notes.length > Workout.MAX_NOTES_LENGTH -> WorkoutError.NOTES_TOO_LONG
        else -> null
    }

    fun isValidExerciseName(name: String): Boolean = name.trim().length >= MIN_EXERCISE_NAME_LENGTH
}
