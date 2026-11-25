package com.iustinmoraru.prtracker.ui.detail

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
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.data.workout.SaveResult
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.ui.common.Format
import com.iustinmoraru.prtracker.ui.common.dateLong
import com.iustinmoraru.prtracker.ui.components.ErrorMessage
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.IconSquareButton
import com.iustinmoraru.prtracker.ui.components.IconTile
import com.iustinmoraru.prtracker.ui.components.PrCard
import com.iustinmoraru.prtracker.ui.components.PrimaryButton
import com.iustinmoraru.prtracker.ui.components.ScreenHeader
import com.iustinmoraru.prtracker.ui.components.SecondaryButton
import com.iustinmoraru.prtracker.ui.editor.exerciseSummary
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.workout.CompletedPill
import com.iustinmoraru.prtracker.ui.workout.style

@Composable
fun WorkoutDetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val colors = PrTheme.colors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val workout = state.workout

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        val text = when (message) {
            is DetailMessage.NewRecords ->
                context.getString(R.string.toast_new_record, message.exercises.joinToString { it.name })
            is DetailMessage.StatusChanged -> when {
                message.result == SaveResult.PENDING_SYNC -> context.getString(R.string.toast_saved_offline)
                message.completed -> context.getString(R.string.detail_marked_done)
                else -> context.getString(R.string.detail_marked_planned)
            }
            DetailMessage.ActionFailed -> context.getString(R.string.detail_action_failed)
        }
        onMessageShown()
        snackbarHostState.showSnackbar(text)
    }

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            ScreenHeader(
                eyebrow = workout?.let { dateLong(it.date) } ?: stringResource(R.string.detail_eyebrow),
                title = workout?.name ?: "",
                onBack = onBack,
                actions = {
                    if (workout != null) {
                        Box {
                            IconSquareButton(Icons.Rounded.MoreVert, stringResource(R.string.cd_more_options), { menuOpen = true })
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_edit)) },
                                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                    onClick = {
                                        menuOpen = false
                                        onEdit(workout.id)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_delete), color = colors.error) },
                                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = colors.error) },
                                    onClick = {
                                        menuOpen = false
                                        confirmDelete = true
                                    },
                                )
                            }
                        }
                    }
                },
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.accentText)
                }
                state.loadFailed || workout == null -> {
                    Spacer(Modifier.height(20.dp))
                    ErrorMessage(stringResource(R.string.editor_load_failed))
                }
                else -> WorkoutContent(
                    workout = workout,
                    recordKeys = state.recordKeys,
                    isBusy = state.isBusy,
                    onToggleStatus = onToggleStatus,
                )
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_message, workout?.name.orEmpty())) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.action_delete), color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel), color = colors.textPrimary) }
            },
            containerColor = colors.sheet,
        )
    }
}

@Composable
private fun WorkoutContent(
    workout: Workout,
    recordKeys: Set<String>,
    isBusy: Boolean,
    onToggleStatus: () -> Unit,
) {
    val colors = PrTheme.colors

    Spacer(Modifier.height(16.dp))
    // Lime summary card, like the "hero-stat" in the design.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Lime)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.detail_volume), color = OnLime.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
            if (workout.isCompleted) CompletedPill() else Eyebrow(stringResource(R.string.status_planned), color = OnLime.copy(alpha = 0.6f))
        }
        Text(
            text = stringResource(R.string.weight_kg, Format.volume(workout.totalVolumeKg)),
            style = MaterialTheme.typography.headlineLarge,
            color = OnLime,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.duration_minutes, workout.durationMinutes) + " · " +
                pluralStringResource(R.plurals.exercise_count, workout.exercises.size, workout.exercises.size),
            style = MaterialTheme.typography.labelMedium,
            color = OnLime.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 6.dp),
        )
    }

    Spacer(Modifier.height(24.dp))
    Eyebrow(stringResource(R.string.editor_exercises))
    Spacer(Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        workout.exercises.forEach { exercise ->
            ExerciseDetailRow(exercise, isRecord = exercise.key in recordKeys)
        }
    }

    if (workout.notes.isNotBlank()) {
        Spacer(Modifier.height(24.dp))
        Eyebrow(stringResource(R.string.detail_notes))
        Spacer(Modifier.height(10.dp))
        PrCard(Modifier.fillMaxWidth()) {
            Text(workout.notes, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }

    Spacer(Modifier.height(24.dp))
    if (workout.isCompleted) {
        SecondaryButton(
            text = stringResource(R.string.detail_mark_planned),
            onClick = onToggleStatus,
            enabled = !isBusy,
            leadingIcon = Icons.AutoMirrored.Rounded.Undo,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        PrimaryButton(
            text = stringResource(R.string.detail_mark_done),
            onClick = onToggleStatus,
            loading = isBusy,
            trailingIcon = Icons.Rounded.CheckCircle,
        )
    }
    Spacer(Modifier.height(80.dp))
}

@Composable
private fun ExerciseDetailRow(exercise: Exercise, isRecord: Boolean) {
    val colors = PrTheme.colors
    val style = exercise.category.style
    PrCard(Modifier.fillMaxWidth(), contentPadding = 11.dp, shape = RoundedCornerShape(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile(style.icon, background = style.background, tint = style.content, size = 44.dp, iconSize = 22.dp, cornerRadius = 13.dp)
            Column(Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(exerciseSummary(exercise), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            if (isRecord) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.violetText.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = colors.violetText, modifier = Modifier.size(13.dp))
                    Text(stringResource(R.string.badge_pr), style = MaterialTheme.typography.labelSmall, color = colors.violetText)
                }
            }
        }
    }
}
