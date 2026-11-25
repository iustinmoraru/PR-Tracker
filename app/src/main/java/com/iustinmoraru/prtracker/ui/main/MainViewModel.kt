package com.iustinmoraru.prtracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iustinmoraru.prtracker.PrTrackerApp
import com.iustinmoraru.prtracker.data.auth.AuthRepository
import com.iustinmoraru.prtracker.data.auth.UserProfile
import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.data.workout.WorkoutRepository
import com.iustinmoraru.prtracker.data.workout.WorkoutStatus
import com.iustinmoraru.prtracker.domain.stats.PersonalRecord
import com.iustinmoraru.prtracker.domain.stats.WorkoutStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Snackbar messages for the main screen. */
sealed interface MainMessage {
    data class StatusChanged(val completed: Boolean) : MainMessage
    data class NewRecords(val exercises: List<Exercise>) : MainMessage
    data class Deleted(val name: String) : MainMessage
    data object ActionFailed : MainMessage
}

/** Dashboard numbers derived from the workouts (see [WorkoutStats]). */
data class Dashboard(
    val today: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = WorkoutStats.startOfWeek(LocalDate.now()),
    val thisWeek: List<Workout> = emptyList(),
    val otherWorkouts: List<Workout> = emptyList(),
    val completedThisWeek: Int = 0,
    val streakWeeks: Int = 0,
    val monthlyVolumeChange: Int? = null,
    val totalVolumeKg: Double = 0.0,
    val totalMinutes: Int = 0,
    val completedCount: Int = 0,
    val minutesLast7Days: List<Int> = List(7) { 0 },
    val records: List<PersonalRecord> = emptyList(),
    val nextPlanned: Workout? = null,
)

data class MainUiState(
    val user: UserProfile? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val workouts: List<Workout> = emptyList(),
    val dashboard: Dashboard = Dashboard(),
    val message: MainMessage? = null,
    val signedOut: Boolean = false,
)

/** State holder for [MainActivity] and its tabs (Home, Progress, Timer, Profile). */
class MainViewModel(
    private val authRepository: AuthRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(user = authRepository.currentUser))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workoutRepository.observeWorkouts()
                .catch { _uiState.update { it.copy(isLoading = false, loadFailed = true) } }
                .collect { workouts ->
                    _uiState.update {
                        it.copy(isLoading = false, loadFailed = false, workouts = workouts, dashboard = buildDashboard(workouts))
                    }
                }
        }
    }

    private fun buildDashboard(workouts: List<Workout>): Dashboard {
        val today = LocalDate.now()
        val thisWeek = WorkoutStats.workoutsThisWeek(workouts, today)
        val thisWeekIds = thisWeek.map { it.id }.toSet()
        return Dashboard(
            today = today,
            weekStart = WorkoutStats.startOfWeek(today),
            thisWeek = thisWeek,
            otherWorkouts = workouts.filter { it.id !in thisWeekIds },
            completedThisWeek = thisWeek.count { it.isCompleted },
            streakWeeks = WorkoutStats.weekStreak(workouts, today),
            monthlyVolumeChange = WorkoutStats.monthlyVolumeChangePercent(workouts, today),
            totalVolumeKg = WorkoutStats.totalVolumeKg(workouts),
            totalMinutes = WorkoutStats.totalMinutes(workouts),
            completedCount = workouts.count { it.isCompleted },
            minutesLast7Days = WorkoutStats.minutesLast7Days(workouts, today),
            records = WorkoutStats.personalRecords(workouts),
            nextPlanned = workouts.filter { !it.isCompleted && !it.date.isBefore(today) }.minByOrNull { it.date },
        )
    }

    /** Context-menu action: complete a planned workout or move it back to planned. */
    fun toggleStatus(workout: Workout) {
        val completing = !workout.isCompleted
        val newRecords = if (completing) {
            WorkoutStats.newRecords(_uiState.value.workouts, workout.copy(status = WorkoutStatus.COMPLETED))
        } else {
            emptyList()
        }
        viewModelScope.launch {
            workoutRepository.setStatus(workout.id, if (completing) WorkoutStatus.COMPLETED else WorkoutStatus.PLANNED)
                .onSuccess {
                    val message = if (newRecords.isNotEmpty()) MainMessage.NewRecords(newRecords) else MainMessage.StatusChanged(completing)
                    _uiState.update { it.copy(message = message) }
                }
                .onFailure { _uiState.update { it.copy(message = MainMessage.ActionFailed) } }
        }
    }

    fun delete(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.delete(workout.id)
                .onSuccess { _uiState.update { it.copy(message = MainMessage.Deleted(workout.name)) } }
                .onFailure { _uiState.update { it.copy(message = MainMessage.ActionFailed) } }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(signedOut = true) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrTrackerApp
                MainViewModel(app.container.authRepository, app.container.workoutRepository)
            }
        }
    }
}
