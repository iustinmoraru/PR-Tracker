package com.iustinmoraru.prtracker.ui.main

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.ui.common.weekRange
import com.iustinmoraru.prtracker.ui.components.ErrorMessage
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.IconTile
import com.iustinmoraru.prtracker.ui.components.PrCard
import com.iustinmoraru.prtracker.ui.components.ProgressRing
import com.iustinmoraru.prtracker.ui.components.SectionHeader
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.OnViolet
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.theme.Violet
import com.iustinmoraru.prtracker.ui.workout.WorkoutCard

/** Actions available on a workout from its long-press context menu. */
data class WorkoutActions(
    val open: (Workout) -> Unit,
    val edit: (Workout) -> Unit,
    val toggleStatus: (Workout) -> Unit,
    val delete: (Workout) -> Unit,
)

/** "Acasă" tab: greeting, weekly progress ring, this week's plan, history and quick metrics. */
@Composable
fun HomeTab(state: MainUiState, onAdd: () -> Unit, actions: WorkoutActions) {
    val colors = PrTheme.colors
    val dashboard = state.dashboard
    val firstName = state.user?.firstName ?: stringResource(R.string.home_default_name)

    Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Eyebrow(stringResource(R.string.home_week, weekRange(dashboard.weekStart)))
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.home_greeting, firstName))
                    append("\n")
                    withStyle(SpanStyle(color = colors.textPrimary.copy(alpha = 0.5f))) {
                        append(stringResource(R.string.home_tagline))
                    }
                },
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
        }
        val total = dashboard.thisWeek.size
        ProgressRing(
            progress = if (total == 0) 0f else dashboard.completedThisWeek.toFloat() / total,
            modifier = Modifier.padding(start = 12.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    append(dashboard.completedThisWeek.toString())
                    withStyle(SpanStyle(color = colors.textMuted)) { append("/$total") }
                },
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
            )
        }
    }

    Spacer(Modifier.height(30.dp))
    SectionHeader(
        eyebrow = stringResource(R.string.home_plan_eyebrow),
        title = stringResource(R.string.home_plan_title),
        action = {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button, onClick = onAdd)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(R.string.home_add), style = MaterialTheme.typography.labelMedium, color = colors.accentText)
                Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(15.dp))
            }
        },
    )
    Spacer(Modifier.height(14.dp))

    when {
        state.isLoading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.accentText)
        }
        state.loadFailed -> ErrorMessage(stringResource(R.string.home_load_failed))
        dashboard.thisWeek.isEmpty() -> EmptyPlanCard(onAdd)
        else -> WorkoutList(dashboard.thisWeek, actions)
    }

    Spacer(Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(
            modifier = Modifier.weight(1f),
            iconTile = { IconTile(Icons.Rounded.LocalFireDepartment, background = Lime, tint = OnLime) },
            value = pluralStringResource(R.plurals.weeks_short, dashboard.streakWeeks, dashboard.streakWeeks),
            label = stringResource(R.string.home_streak),
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            iconTile = { IconTile(Icons.AutoMirrored.Rounded.TrendingUp, background = Violet, tint = OnViolet) },
            value = dashboard.monthlyVolumeChange?.let { if (it >= 0) "+$it%" else "$it%" } ?: "—",
            label = stringResource(R.string.home_monthly_volume),
        )
    }

    if (dashboard.otherWorkouts.isNotEmpty()) {
        Spacer(Modifier.height(30.dp))
        SectionHeader(
            eyebrow = stringResource(R.string.home_history_eyebrow),
            title = stringResource(R.string.home_history_title),
        )
        Spacer(Modifier.height(14.dp))
        WorkoutList(dashboard.otherWorkouts, actions)
    }
}

/** Workout cards, each with its own long-press context menu. */
@Composable
private fun WorkoutList(workouts: List<Workout>, actions: WorkoutActions) {
    var menuFor by rememberSaveable { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        workouts.forEach { workout ->
            Box {
                WorkoutCard(
                    workout = workout,
                    onClick = { actions.open(workout) },
                    onLongClick = { menuFor = workout.id },
                )
                WorkoutContextMenu(
                    workout = workout,
                    expanded = menuFor == workout.id,
                    onDismiss = { menuFor = null },
                    actions = actions,
                )
            }
        }
    }
}

@Composable
private fun WorkoutContextMenu(workout: Workout, expanded: Boolean, onDismiss: () -> Unit, actions: WorkoutActions) {
    val colors = PrTheme.colors
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_view)) },
            leadingIcon = { Icon(Icons.Rounded.Visibility, contentDescription = null) },
            onClick = { onDismiss(); actions.open(workout) },
        )
        DropdownMenuItem(
            text = { Text(stringResource(if (workout.isCompleted) R.string.menu_mark_planned else R.string.menu_mark_done)) },
            leadingIcon = {
                Icon(if (workout.isCompleted) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.CheckCircle, contentDescription = null)
            },
            onClick = { onDismiss(); actions.toggleStatus(workout) },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_edit)) },
            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
            onClick = { onDismiss(); actions.edit(workout) },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_delete), color = colors.error) },
            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = colors.error) },
            onClick = { onDismiss(); actions.delete(workout) },
        )
    }
}

@Composable
private fun EmptyPlanCard(onAdd: () -> Unit) {
    val colors = PrTheme.colors
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.textPrimary.copy(alpha = 0.14f), shape)
            .clickable(role = Role.Button, onClick = onAdd)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        IconTile(Icons.Rounded.CalendarMonth, background = colors.cardStrong, tint = colors.textSecondary, size = 48.dp, iconSize = 23.dp, cornerRadius = 16.dp)
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            Text(stringResource(R.string.home_empty_desc), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.textSecondary)
    }
}

@Composable
private fun MetricCard(modifier: Modifier, iconTile: @Composable () -> Unit, value: String, label: String) {
    val colors = PrTheme.colors
    PrCard(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            iconTile()
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1)
                Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textMuted, maxLines = 1)
            }
        }
    }
}
