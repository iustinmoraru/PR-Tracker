package com.iustinmoraru.prtracker.ui.timer

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iustinmoraru.prtracker.PrTrackerApp
import com.iustinmoraru.prtracker.notifications.AppNotifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerUiState(
    val durationSeconds: Int = 60,
    val remainingSeconds: Int = 60,
    val isRunning: Boolean = false,
    /** Incremented each time a hang is completed, so the UI can react once (vibration, message). */
    val finishedCount: Int = 0,
) {
    val progress: Float
        get() = 1f - remainingSeconds.toFloat() / durationSeconds

    val isPaused: Boolean
        get() = !isRunning && remainingSeconds in 1 until durationSeconds
}

/**
 * Countdown for the deadhang exercise (hanging from the bar to build grip strength).
 * Lives in the activity's ViewModel, so it keeps running while switching tabs.
 */
class TimerViewModel(private val notifier: AppNotifier) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null

    fun selectDuration(seconds: Int) {
        tickJob?.cancel()
        _uiState.update { it.copy(durationSeconds = seconds, remainingSeconds = seconds, isRunning = false) }
    }

    /** Start, pause or resume — the main button and the shake gesture both call this. */
    fun toggle() {
        if (_uiState.value.isRunning) pause() else start()
    }

    fun reset() {
        tickJob?.cancel()
        _uiState.update { it.copy(remainingSeconds = it.durationSeconds, isRunning = false) }
    }

    private fun start() {
        if (_uiState.value.remainingSeconds <= 0) reset()
        _uiState.update { it.copy(isRunning = true) }
        tickJob = viewModelScope.launch {
            // Measure against the monotonic clock so the countdown does not drift.
            val startRemaining = _uiState.value.remainingSeconds
            val startedAt = SystemClock.elapsedRealtime()
            while (isActive) {
                delay(TICK_MS)
                val elapsed = ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt()
                val remaining = (startRemaining - elapsed).coerceAtLeast(0)
                _uiState.update { it.copy(remainingSeconds = remaining) }
                if (remaining == 0) {
                    finish()
                    break
                }
            }
        }
    }

    private fun pause() {
        tickJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun finish() {
        val duration = _uiState.value.durationSeconds
        _uiState.update { it.copy(isRunning = false, finishedCount = it.finishedCount + 1) }
        notifier.notifyTimerFinished(duration)
    }

    companion object {
        private const val TICK_MS = 200L

        val DURATIONS = listOf(30, 45, 60, 90)

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrTrackerApp
                TimerViewModel(app.container.notifier)
            }
        }
    }
}
