package com.iustinmoraru.prtracker.ui.editor

import com.iustinmoraru.prtracker.data.workout.ExerciseCategory

/** Quick-pick exercise from the library sheet. */
data class ExercisePreset(val name: String, val category: ExerciseCategory, val defaultWeightKg: Double)

/** Exercise library shown in the "Adaugă exercițiu" sheet (names kept in English, as in the gym). */
val ExercisePresets = listOf(
    ExercisePreset("Bench press", ExerciseCategory.STRENGTH, 40.0),
    ExercisePreset("Squat", ExerciseCategory.STRENGTH, 50.0),
    ExercisePreset("Deadlift", ExerciseCategory.STRENGTH, 60.0),
    ExercisePreset("Romanian deadlift", ExerciseCategory.STRENGTH, 40.0),
    ExercisePreset("Overhead press", ExerciseCategory.STRENGTH, 25.0),
    ExercisePreset("Pull-up", ExerciseCategory.STRENGTH, 0.0),
    ExercisePreset("Deadhang", ExerciseCategory.GRIP, 0.0),
    ExercisePreset("Farmer's walk", ExerciseCategory.GRIP, 20.0),
    ExercisePreset("Mobility flow", ExerciseCategory.MOBILITY, 0.0),
    ExercisePreset("Running", ExerciseCategory.CARDIO, 0.0),
)
