package com.iustinmoraru.prtracker.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iustinmoraru.prtracker.PrTrackerApp
import com.iustinmoraru.prtracker.data.workout.WorkoutRepository
import com.iustinmoraru.prtracker.domain.stats.ExerciseEntry
import com.iustinmoraru.prtracker.domain.stats.PersonalRecord
import com.iustinmoraru.prtracker.domain.stats.WorkoutStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val record: PersonalRecord? = null,
    /** Oldest first, as needed by the chart. */
    val entries: List<ExerciseEntry> = emptyList(),
    val loadFailed: Boolean = false,
)

/** Progress of a single exercise across all completed workouts. */
class ExerciseHistoryViewModel(
    repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val key: String = savedStateHandle[ExerciseHistoryActivity.EXTRA_KEY] ?: ""
    private val fallbackName: String = savedStateHandle[ExerciseHistoryActivity.EXTRA_NAME] ?: ""

    val uiState: StateFlow<HistoryUiState> = repository.observeWorkouts()
        .map { workouts ->
            val entries = WorkoutStats.exerciseHistory(workouts, key)
            HistoryUiState(
                isLoading = false,
                exerciseName = entries.lastOrNull()?.exercise?.name ?: fallbackName,
                record = WorkoutStats.personalRecords(workouts).firstOrNull { it.key == key },
                entries = entries,
            )
        }
        .catch { emit(HistoryUiState(isLoading = false, exerciseName = fallbackName, loadFailed = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState(exerciseName = fallbackName))

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrTrackerApp
                ExerciseHistoryViewModel(app.container.workoutRepository, createSavedStateHandle())
            }
        }
    }
}
