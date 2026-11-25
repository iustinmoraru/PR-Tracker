package com.iustinmoraru.prtracker.data.workout

import java.time.LocalDate
import java.util.Locale
import java.util.UUID

/** Exercise families; each one has its own colour and icon in the UI. */
enum class ExerciseCategory { STRENGTH, MOBILITY, GRIP, CARDIO }

enum class WorkoutStatus { PLANNED, COMPLETED }

/** One exercise inside a workout: e.g. Bench press, 4 × 8 @ 60 kg. */
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: ExerciseCategory,
    val sets: Int,
    val reps: Int,
    val weightKg: Double,
) {
    /** Training volume (kg) = sets × reps × weight. */
    val volumeKg: Double
        get() = sets * reps * weightKg

    /** Key used to group the same exercise across workouts ("Bench Press" == "bench press"). */
    val key: String
        get() = name.trim().lowercase(Locale.ROOT)
}

/** A planned or completed training session. */
data class Workout(
    val id: String = "",
    val name: String,
    val date: LocalDate,
    val durationMinutes: Int,
    val status: WorkoutStatus = WorkoutStatus.PLANNED,
    val exercises: List<Exercise> = emptyList(),
    val notes: String = "",
) {
    val isCompleted: Boolean
        get() = status == WorkoutStatus.COMPLETED

    val totalVolumeKg: Double
        get() = exercises.sumOf { it.volumeKg }

    companion object {
        const val MAX_NAME_LENGTH = 80
        const val MAX_NOTES_LENGTH = 500
        const val MIN_DURATION = 10
        const val MAX_DURATION = 300
        const val MAX_EXERCISES = 20
    }
}
