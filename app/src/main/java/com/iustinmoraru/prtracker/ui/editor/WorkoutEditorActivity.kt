package com.iustinmoraru.prtracker.ui.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.SaveResult
import com.iustinmoraru.prtracker.ui.common.ThemedActivity

/** Creates a new workout or edits an existing one (when [EXTRA_WORKOUT_ID] is set). */
class WorkoutEditorActivity : ThemedActivity() {

    private val viewModel: WorkoutEditorViewModel by viewModels { WorkoutEditorViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setThemedContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.saved) {
                state.saved?.let { onSaved(it) }
            }

            WorkoutEditorScreen(
                state = state,
                onBack = ::finish,
                onNameChange = viewModel::onNameChange,
                onDateChange = viewModel::onDateChange,
                onDurationChange = viewModel::onDurationChange,
                onCompletedChange = viewModel::onCompletedChange,
                onNotesChange = viewModel::onNotesChange,
                onExerciseSaved = viewModel::onExerciseSaved,
                onExerciseRemoved = viewModel::onExerciseRemoved,
                onSave = viewModel::save,
            )
        }
    }

    private fun onSaved(event: SavedEvent) {
        val message = when {
            event.newRecords.isNotEmpty() ->
                getString(R.string.toast_new_record, event.newRecords.joinToString { it.name })
            event.result == SaveResult.PENDING_SYNC -> getString(R.string.toast_saved_offline)
            else -> getString(R.string.toast_workout_saved)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    companion object {
        const val EXTRA_WORKOUT_ID = "workout_id"

        /** Pass a [workoutId] to edit an existing workout; omit it to create a new one. */
        fun intent(context: Context, workoutId: String? = null) =
            Intent(context, WorkoutEditorActivity::class.java).apply {
                if (workoutId != null) putExtra(EXTRA_WORKOUT_ID, workoutId)
            }
    }
}
