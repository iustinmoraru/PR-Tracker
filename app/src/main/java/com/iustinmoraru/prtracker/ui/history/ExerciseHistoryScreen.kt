package com.iustinmoraru.prtracker.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.ui.common.Format
import com.iustinmoraru.prtracker.ui.common.dateLong
import com.iustinmoraru.prtracker.ui.common.dateShort
import com.iustinmoraru.prtracker.ui.components.ErrorMessage
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.LineChart
import com.iustinmoraru.prtracker.ui.components.PrCard
import com.iustinmoraru.prtracker.ui.components.ScreenHeader
import com.iustinmoraru.prtracker.ui.editor.exerciseSummary
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.PrTheme

@Composable
fun ExerciseHistoryScreen(state: HistoryUiState, onBack: () -> Unit, onOpenWorkout: (String) -> Unit) {
    val colors = PrTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        ScreenHeader(eyebrow = stringResource(R.string.history_eyebrow), title = state.exerciseName, onBack = onBack)

        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accentText)
            }
            state.loadFailed -> {
                Spacer(Modifier.height(20.dp))
                ErrorMessage(stringResource(R.string.home_load_failed))
            }
            else -> {
                state.record?.let { record ->
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Lime)
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Eyebrow(stringResource(R.string.history_record), color = OnLime.copy(alpha = 0.55f))
                            Text(
                                text = if (record.weightKg > 0) {
                                    stringResource(R.string.record_weight_reps, Format.weight(record.weightKg), record.reps)
                                } else {
                                    stringResource(R.string.record_reps_only, record.reps)
                                },
                                style = MaterialTheme.typography.headlineLarge,
                                color = OnLime,
                            )
                            Text(dateLong(record.date), style = MaterialTheme.typography.labelMedium, color = OnLime.copy(alpha = 0.75f))
                        }
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = OnLime, modifier = Modifier.size(38.dp))
                    }
                }

                if (state.entries.size >= 2) {
                    Spacer(Modifier.height(16.dp))
                    PrCard(Modifier.fillMaxWidth(), contentPadding = 18.dp) {
                        Eyebrow(stringResource(R.string.history_chart_eyebrow))
                        Text(stringResource(R.string.history_chart_title), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(14.dp))
                        val values = state.entries.map { it.exercise.weightKg.toFloat() }
                        LineChart(
                            values = values,
                            contentDescription = stringResource(R.string.cd_history_chart, state.exerciseName),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Eyebrow(stringResource(R.string.history_sessions))
                Spacer(Modifier.height(10.dp))
                if (state.entries.isEmpty()) {
                    PrCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.entries.asReversed().forEach { entry ->
                        PrCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            onClick = { onOpenWorkout(entry.workoutId) },
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${dateShort(entry.date)} ${entry.date.year} · ${entry.workoutName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(exerciseSummary(entry.exercise), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                }
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.textMuted)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
