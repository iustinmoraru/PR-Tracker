package com.iustinmoraru.prtracker.ui.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iustinmoraru.prtracker.domain.stats.PersonalRecord
import com.iustinmoraru.prtracker.ui.common.ThemedActivity
import com.iustinmoraru.prtracker.ui.detail.WorkoutDetailActivity

/** History and personal record of one exercise, opened from the Progress tab. */
class ExerciseHistoryActivity : ThemedActivity() {

    private val viewModel: ExerciseHistoryViewModel by viewModels { ExerciseHistoryViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setThemedContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ExerciseHistoryScreen(
                state = state,
                onBack = ::finish,
                onOpenWorkout = { startActivity(WorkoutDetailActivity.intent(this, it)) },
            )
        }
    }

    companion object {
        const val EXTRA_KEY = "exercise_key"
        const val EXTRA_NAME = "exercise_name"

        fun intent(context: Context, record: PersonalRecord) =
            Intent(context, ExerciseHistoryActivity::class.java)
                .putExtra(EXTRA_KEY, record.key)
                .putExtra(EXTRA_NAME, record.exerciseName)
    }
}
