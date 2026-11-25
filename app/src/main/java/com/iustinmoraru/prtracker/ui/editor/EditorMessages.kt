package com.iustinmoraru.prtracker.ui.editor

import androidx.annotation.StringRes
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.domain.validation.WorkoutError

/** Maps workout validation errors to user-facing messages. */
@StringRes
fun WorkoutError.messageRes(): Int = when (this) {
    WorkoutError.NAME_REQUIRED -> R.string.error_workout_name
    WorkoutError.NAME_TOO_LONG -> R.string.error_workout_name_long
    WorkoutError.NO_EXERCISES -> R.string.error_no_exercises
    WorkoutError.TOO_MANY_EXERCISES -> R.string.error_too_many_exercises
    WorkoutError.INVALID_DURATION -> R.string.error_duration
    WorkoutError.NOTES_TOO_LONG -> R.string.error_notes_long
}
