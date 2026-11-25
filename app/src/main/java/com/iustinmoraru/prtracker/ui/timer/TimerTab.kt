package com.iustinmoraru.prtracker.ui.timer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.sensors.ShakeDetector
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.ProgressRing
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.PrTheme

/**
 * "Deadhang" tab: countdown ring, duration chips and controls.
 * Uses the accelerometer (shake to start/pause), vibration and keeps the screen on while running.
 */
@Composable
fun TimerTab(state: TimerUiState, onToggle: () -> Unit, onReset: () -> Unit, onSelectDuration: (Int) -> Unit) {
    val colors = PrTheme.colors
    val context = LocalContext.current
    val view = LocalView.current
    val currentOnToggle by rememberUpdatedState(onToggle)

    // Shake-to-toggle while this tab is on screen.
    val shakeDetector = remember { ShakeDetector(context) { currentOnToggle() } }
    DisposableEffect(shakeDetector) {
        shakeDetector.start()
        onDispose { shakeDetector.stop() }
    }

    // Keep the display awake during a hang.
    DisposableEffect(state.isRunning) {
        view.keepScreenOn = state.isRunning
        onDispose { view.keepScreenOn = false }
    }

    // Vibrate once for every completed hang (not when the tab is re-opened).
    var handledFinishes by rememberSaveable { mutableIntStateOf(state.finishedCount) }
    LaunchedEffect(state.finishedCount) {
        if (state.finishedCount > handledFinishes) {
            handledFinishes = state.finishedCount
            vibrate(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(20.dp))
        Eyebrow(stringResource(R.string.timer_eyebrow))
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.timer_title), style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
        Text(
            stringResource(R.string.timer_description),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(top = 8.dp),
        )

        Spacer(Modifier.height(32.dp))
        ProgressRing(
            progress = state.progress,
            size = 232.dp,
            strokeWidth = 9.dp,
            animate = state.isRunning,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.PanTool, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(27.dp))
                Text(
                    text = "%02d:%02d".format(state.remainingSeconds / 60, state.remainingSeconds % 60),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp, letterSpacing = (-0.05).em),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Eyebrow(
                    stringResource(
                        when {
                            state.isRunning -> R.string.timer_status_hold
                            state.remainingSeconds == 0 -> R.string.timer_status_done
                            state.isPaused -> R.string.timer_status_paused
                            else -> R.string.timer_status_ready
                        }
                    )
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimerViewModel.DURATIONS.forEach { seconds ->
                val active = seconds == state.durationSeconds
                Text(
                    text = stringResource(R.string.timer_seconds, seconds),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) colors.accentText else colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(min = 52.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (active) colors.accent.copy(alpha = if (colors.isDark) 0.13f else 0.5f) else colors.card)
                        .clickable(role = Role.RadioButton) { onSelectDuration(seconds) }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                )
            }
        }

        Spacer(Modifier.height(26.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(shape)
                    .background(colors.card)
                    .border(1.dp, colors.border, shape)
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.timer_reset), onClick = onReset),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = stringResource(R.string.timer_reset), tint = colors.textPrimary)
            }
            Row(
                modifier = Modifier
                    .widthIn(min = 170.dp)
                    .height(52.dp)
                    .clip(shape)
                    .background(Lime)
                    .clickable(role = Role.Button, onClick = onToggle)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (state.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null, tint = OnLime)
                Spacer(Modifier.size(8.dp))
                Text(
                    stringResource(
                        when {
                            state.isRunning -> R.string.timer_pause
                            state.isPaused -> R.string.timer_resume
                            else -> R.string.timer_start
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = OnLime,
                )
            }
        }

        if (shakeDetector.isAvailable) {
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.card)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Rounded.Vibration, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(15.dp))
                Text(stringResource(R.string.timer_shake_hint), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }

        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.violetText.copy(alpha = 0.08f))
                .border(1.dp, colors.violetText.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.Speed, contentDescription = null, tint = colors.violetText, modifier = Modifier.size(20.dp))
            Column {
                Text(stringResource(R.string.timer_tip_title), style = MaterialTheme.typography.titleSmall, color = colors.violetText)
                Text(stringResource(R.string.timer_tip_text), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
    }
}

/** Short double vibration at the end of a hang. */
private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrator?.hasVibrator() == true) {
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
    }
}
