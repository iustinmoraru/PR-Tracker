package com.iustinmoraru.prtracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.domain.stats.PersonalRecord
import com.iustinmoraru.prtracker.ui.common.Format
import com.iustinmoraru.prtracker.ui.common.dateShort
import com.iustinmoraru.prtracker.ui.components.BarChart
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.IconTile
import com.iustinmoraru.prtracker.ui.components.PrCard
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.OnViolet
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.theme.Violet

/** "Progres" tab: total volume, key numbers, the last 7 days and the personal records list. */
@Composable
fun ProgressTab(state: MainUiState, onRecordClick: (PersonalRecord) -> Unit) {
    val colors = PrTheme.colors
    val dashboard = state.dashboard

    Spacer(Modifier.height(20.dp))
    Eyebrow(stringResource(R.string.progress_eyebrow))
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.progress_title), style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)

    Spacer(Modifier.height(22.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Lime)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Eyebrow(stringResource(R.string.progress_total_volume), color = OnLime.copy(alpha = 0.55f))
            Text(
                stringResource(R.string.weight_kg, Format.volume(dashboard.totalVolumeKg)),
                style = MaterialTheme.typography.headlineLarge,
                color = OnLime,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = OnLime, modifier = Modifier.size(14.dp))
                Text(
                    text = dashboard.monthlyVolumeChange?.let { stringResource(R.string.progress_vs_last_month, it) }
                        ?: stringResource(R.string.progress_no_comparison),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnLime,
                )
            }
        }
        Icon(Icons.Rounded.BarChart, contentDescription = null, tint = OnLime, modifier = Modifier.size(38.dp))
    }

    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MiniStat(Icons.Rounded.LocalFireDepartment, dashboard.streakWeeks.toString(), stringResource(R.string.progress_weeks), Modifier.weight(1f))
        MiniStat(Icons.Rounded.TaskAlt, dashboard.completedCount.toString(), stringResource(R.string.progress_completed), Modifier.weight(1f))
        MiniStat(Icons.Rounded.Schedule, dashboard.totalMinutes.toString(), stringResource(R.string.progress_minutes), Modifier.weight(1f))
    }

    Spacer(Modifier.height(12.dp))
    val activeDays = dashboard.minutesLast7Days.count { it > 0 }
    PrCard(Modifier.fillMaxWidth(), contentPadding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.progress_consistency))
                Text(stringResource(R.string.progress_last_7_days), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            }
            Text("${activeDays * 100 / 7}%", style = MaterialTheme.typography.titleLarge, color = colors.accentText)
        }
        Spacer(Modifier.height(18.dp))
        val letters = stringArrayResource(R.array.days_letter)
        val labels = (6 downTo 0).map { letters[dashboard.today.minusDays(it.toLong()).dayOfWeek.value - 1] }
        BarChart(
            values = dashboard.minutesLast7Days,
            labels = labels,
            contentDescription = stringResource(R.string.cd_chart_last_7_days, dashboard.minutesLast7Days.sum()),
        )
    }

    Spacer(Modifier.height(28.dp))
    Eyebrow(stringResource(R.string.progress_records_eyebrow))
    Text(stringResource(R.string.progress_records_title), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
    Spacer(Modifier.height(12.dp))
    if (dashboard.records.isEmpty()) {
        PrCard(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.progress_records_empty), style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            dashboard.records.forEach { record -> RecordRow(record, onClick = { onRecordClick(record) }) }
        }
    }
}

@Composable
private fun MiniStat(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    val colors = PrTheme.colors
    PrCard(modifier = modifier, shape = RoundedCornerShape(16.dp), contentPadding = 12.dp) {
        Icon(icon, contentDescription = null, tint = colors.violetText, modifier = Modifier.size(18.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary, modifier = Modifier.padding(top = 8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textMuted, maxLines = 1)
    }
}

@Composable
private fun RecordRow(record: PersonalRecord, onClick: () -> Unit) {
    val colors = PrTheme.colors
    PrCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), contentPadding = 12.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile(Icons.Rounded.EmojiEvents, background = Violet, tint = OnViolet, size = 42.dp, iconSize = 21.dp)
            Column(Modifier.weight(1f)) {
                Text(record.exerciseName, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(dateShort(record.date) + " ${record.date.year}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            Text(
                text = if (record.weightKg > 0) {
                    stringResource(R.string.record_weight_reps, Format.weight(record.weightKg), record.reps)
                } else {
                    stringResource(R.string.record_reps_only, record.reps)
                },
                style = MaterialTheme.typography.titleSmall,
                color = colors.accentText,
            )
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.textMuted)
        }
    }
}
