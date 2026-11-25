package com.iustinmoraru.prtracker.ui.editor

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
import com.iustinmoraru.prtracker.domain.validation.WorkoutError
import com.iustinmoraru.prtracker.domain.validation.WorkoutValidator
import com.iustinmoraru.prtracker.notifications.RecordNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Emitted once the workout was written, so the activity can close and notify. */
data class SavedEvent(val result: SaveResult, val newRecords: List<Exercise>)

data class EditorUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val date: LocalDate = LocalDate.now(),
    val durationMinutes: Int = 50,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val exercises: List<Exercise> = emptyList(),
    val error: WorkoutError? = null,
    val loadFailed: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val hasChanges: Boolean = false,
    val saved: SavedEvent? = null,
)

/**
 * Create / edit form for a workout ([WorkoutEditorActivity]).
 * When a workout id is passed in the intent, the existing workout is loaded and overwritten.
 */
class WorkoutEditorViewModel(
    private val repository: WorkoutRepository,
    private val notifier: RecordNotifier,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val workoutId: String? = savedStateHandle[WorkoutEditorActivity.EXTRA_WORKOUT_ID]

    private val _uiState = MutableStateFlow(EditorUiState(isEditing = workoutId != null, isLoading = workoutId != null))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (workoutId != null) load(workoutId)
    }

    private fun load(id: String) = viewModelScope.launch {
        val workout = runCatching { repository.observeWorkout(id).first() }.getOrNull()
        _uiState.update {
            if (workout == null) {
                it.copy(isLoading = false, loadFailed = true)
            } else {
                it.copy(
                    isLoading = false,
                    name = workout.name,
                    date = workout.date,
                    durationMinutes = workout.durationMinutes,
                    isCompleted = workout.isCompleted,
                    notes = workout.notes,
                    exercises = workout.exercises,
                )
            }
        }
    }

    /** Applies a change made by the user and marks the form as modified. */
    private fun edit(change: (EditorUiState) -> EditorUiState) =
        _uiState.update { change(it).copy(hasChanges = true, error = null, saveFailed = false) }

    fun onNameChange(value: String) = edit { it.copy(name = value.take(Workout.MAX_NAME_LENGTH)) }

    fun onDateChange(value: LocalDate) = edit { it.copy(date = value) }

    fun onDurationChange(delta: Int) = edit {
        it.copy(durationMinutes = (it.durationMinutes + delta).coerceIn(Workout.MIN_DURATION, Workout.MAX_DURATION))
    }

    fun onCompletedChange(value: Boolean) = edit { it.copy(isCompleted = value) }

    fun onNotesChange(value: String) = edit { it.copy(notes = value.take(Workout.MAX_NOTES_LENGTH)) }

    /** Adds a new exercise, or replaces the one with the same id (editing from the sheet). */
    fun onExerciseSaved(exercise: Exercise) = edit { state ->
        val exists = state.exercises.any { it.id == exercise.id }
        state.copy(
            exercises = if (exists) state.exercises.map { if (it.id == exercise.id) exercise else it } else state.exercises + exercise,
        )
    }

    fun onExerciseRemoved(id: String) = edit { state -> state.copy(exercises = state.exercises.filterNot { it.id == id }) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading) return
        val workout = Workout(
            id = workoutId.orEmpty(),
            name = state.name.trim(),
            date = state.date,
            durationMinutes = state.durationMinutes,
            status = if (state.isCompleted) WorkoutStatus.COMPLETED else WorkoutStatus.PLANNED,
            exercises = state.exercises,
            notes = state.notes.trim(),
        )
        WorkoutValidator.validate(workout)?.let { error ->
            _uiState.update { it.copy(error = error) }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            // Compare against the history *before* saving, to detect newly beaten records.
            val history = if (workout.isCompleted) {
                runCatching { repository.observeWorkouts().first() }.getOrDefault(emptyList())
            } else {
                emptyList()
            }
            repository.save(workout)
                .onSuccess { (_, result) ->
                    val records = if (workout.isCompleted) WorkoutStats.newRecords(history, workout) else emptyList()
                    notifier.notifyNewRecords(records)
                    _uiState.update { it.copy(isSaving = false, hasChanges = false, saved = SavedEvent(result, records)) }
                }
                .onFailure { _uiState.update { it.copy(isSaving = false, saveFailed = true) } }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrTrackerApp
                WorkoutEditorViewModel(app.container.workoutRepository, app.container.notifier, createSavedStateHandle())
            }
        }
    }
}
