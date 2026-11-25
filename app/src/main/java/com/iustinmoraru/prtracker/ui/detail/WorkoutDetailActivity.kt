package com.iustinmoraru.prtracker.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.ui.common.ThemedActivity
import com.iustinmoraru.prtracker.ui.editor.WorkoutEditorActivity

/** Details of a single workout: exercises, volume, PR badges and actions. */
class WorkoutDetailActivity : ThemedActivity() {

    private val viewModel: WorkoutDetailViewModel by viewModels { WorkoutDetailViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getStringExtra(EXTRA_WORKOUT_ID) == null) {
            finish()
            return
        }
        setThemedContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.deleted) {
                if (state.deleted) {
                    Toast.makeText(this@WorkoutDetailActivity, R.string.toast_workout_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            WorkoutDetailScreen(
                state = state,
                onBack = ::finish,
                onEdit = { id -> startActivity(WorkoutEditorActivity.intent(this, id)) },
                onToggleStatus = viewModel::toggleStatus,
                onDelete = viewModel::delete,
                onMessageShown = viewModel::onMessageShown,
            )
        }
    }

    companion object {
        const val EXTRA_WORKOUT_ID = "workout_id"

        fun intent(context: Context, workoutId: String) =
            Intent(context, WorkoutDetailActivity::class.java).putExtra(EXTRA_WORKOUT_ID, workoutId)
    }
}
