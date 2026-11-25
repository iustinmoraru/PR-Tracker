package com.iustinmoraru.prtracker.ui.workout

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.ExerciseCategory
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.ui.theme.Coral
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnCoral
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.OnTeal
import com.iustinmoraru.prtracker.ui.theme.OnViolet
import com.iustinmoraru.prtracker.ui.theme.Teal
import com.iustinmoraru.prtracker.ui.theme.Violet

/** Visual identity of an exercise category (colours and icon from the design). */
data class CategoryStyle(
    val icon: ImageVector,
    val background: Color,
    val content: Color,
    @StringRes val label: Int,
)

val ExerciseCategory.style: CategoryStyle
    get() = when (this) {
        ExerciseCategory.STRENGTH -> CategoryStyle(Icons.Rounded.FitnessCenter, Lime, OnLime, R.string.category_strength)
        ExerciseCategory.GRIP -> CategoryStyle(Icons.Rounded.PanTool, Violet, OnViolet, R.string.category_grip)
        ExerciseCategory.MOBILITY -> CategoryStyle(Icons.Rounded.SelfImprovement, Teal, OnTeal, R.string.category_mobility)
        ExerciseCategory.CARDIO -> CategoryStyle(Icons.AutoMirrored.Rounded.DirectionsRun, Coral, OnCoral, R.string.category_cardio)
    }

/** Accent colour of a workout card: the colour of its first exercise's category. */
val Workout.accent: Color
    get() = (exercises.firstOrNull()?.category ?: ExerciseCategory.STRENGTH).style.background
