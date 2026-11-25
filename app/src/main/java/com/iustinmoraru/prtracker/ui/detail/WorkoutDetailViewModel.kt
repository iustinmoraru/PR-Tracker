package com.iustinmoraru.prtracker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iustinmoraru.prtracker.PrTrackerApp
import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.data.workout.SaveResult
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.data.workout.WorkoutRepository
import com.iustinmoraru.prtracker.data.workout.WorkoutStatus
import com.iustinmoraru.prtracker.domain.stats.WorkoutStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One-off messages shown as a snackbar. */
sealed interface DetailMessage {
    data class StatusChanged(val completed: Boolean, val result: SaveResult) : DetailMessage
    data class NewRecords(val exercises: List<Exercise>) : DetailMessage
    data object ActionFailed : DetailMessage
}

data class DetailUiState(
    val isLoading: Boolean = true,
    val workout: Workout? = null,
    /** Exercises of this workout that currently hold the user's personal record. */
    val recordKeys: Set<String> = emptySet(),
    val loadFailed: Boolean = false,
    val isBusy: Boolean = false,
    val deleted: Boolean = false,
    val message: DetailMessage? = null,
)

/** Shows one workout and lets the user complete, edit or delete it. */
class WorkoutDetailViewModel(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val workoutId: String = checkNotNull(savedStateHandle[WorkoutDetailActivity.EXTRA_WORKOUT_ID])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /** Latest list of all workouts, needed to detect new records when completing. */
    private var allWorkouts: List<Workout> = emptyList()

    init {
        viewModelScope.launch {
            combine(repository.observeWorkout(workoutId), repository.observeWorkouts()) { workout, all ->
                allWorkouts = all
                workout to all
            }
                .catch { _uiState.update { it.copy(isLoading = false, loadFailed = true) } }
                .collect { (workout, all) ->
                    val records = WorkoutStats.personalRecords(all)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            workout = workout,
                            loadFailed = workout == null && !it.deleted,
                            recordKeys = workout?.let { w -> WorkoutStats.recordKeysIn(w, records) }.orEmpty(),
                        )
                    }
                }
        }
    }

    /** Switches between planned and completed. Completing may unlock new personal records. */
    fun toggleStatus() {
        val workout = _uiState.value.workout ?: return
        val completing = !workout.isCompleted
        val newStatus = if (completing) WorkoutStatus.COMPLETED else WorkoutStatus.PLANNED
        val newRecords = if (completing) {
            WorkoutStats.newRecords(allWorkouts, workout.copy(status = WorkoutStatus.COMPLETED))
        } else {
            emptyList()
        }

        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            repository.setStatus(workout.id, newStatus)
                .onSuccess { result ->
                    val message = if (newRecords.isNotEmpty()) {
                        DetailMessage.NewRecords(newRecords)
                    } else {
                        DetailMessage.StatusChanged(completing, result)
                    }
                    _uiState.update { it.copy(isBusy = false, message = message) }
                }
                .onFailure { _uiState.update { it.copy(isBusy = false, message = DetailMessage.ActionFailed) } }
        }
    }

    fun delete() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            repository.delete(workoutId)
                .onSuccess { _uiState.update { it.copy(isBusy = false, deleted = true) } }
                .onFailure { _uiState.update { it.copy(isBusy = false, message = DetailMessage.ActionFailed) } }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrTrackerApp
                WorkoutDetailViewModel(app.container.workoutRepository, createSavedStateHandle())
            }
        }
    }
}
