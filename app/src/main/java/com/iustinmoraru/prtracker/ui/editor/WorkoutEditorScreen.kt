package com.iustinmoraru.prtracker.ui.editor

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.domain.validation.WorkoutError
import com.iustinmoraru.prtracker.ui.common.Format
import com.iustinmoraru.prtracker.ui.common.dateShort
import com.iustinmoraru.prtracker.ui.components.AppTextField
import com.iustinmoraru.prtracker.ui.components.Counter
import com.iustinmoraru.prtracker.ui.components.ErrorMessage
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.IconTile
import com.iustinmoraru.prtracker.ui.components.PrCard
import com.iustinmoraru.prtracker.ui.components.PrimaryButton
import com.iustinmoraru.prtracker.ui.components.ScreenHeader
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.workout.style
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** "Antrenament nou" / "Editează antrenamentul" screen. */
@Composable
fun WorkoutEditorScreen(
    state: EditorUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onDurationChange: (Int) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onNotesChange: (String) -> Unit,
    onExerciseSaved: (Exercise) -> Unit,
    onExerciseRemoved: (String) -> Unit,
    onSave: () -> Unit,
) {
    val colors = PrTheme.colors
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var editingExerciseId by rememberSaveable { mutableStateOf<String?>(null) }

    // Leaving with unsaved changes asks for confirmation first.
    val requestClose: () -> Unit = {
        if (state.hasChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }
    BackHandler(onBack = requestClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.editor_eyebrow),
            title = stringResource(if (state.isEditing) R.string.editor_title_edit else R.string.editor_title_new),
            onBack = requestClose,
            actions = {
                TextButton(onClick = onSave, enabled = !state.isSaving && !state.isLoading) {
                    Text(stringResource(R.string.action_save), color = colors.accentText, style = MaterialTheme.typography.labelMedium)
                }
            },
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accentText)
            }
            return@Column
        }
        if (state.loadFailed) {
            Spacer(Modifier.height(20.dp))
            ErrorMessage(stringResource(R.string.editor_load_failed))
            return@Column
        }

        Spacer(Modifier.height(18.dp))
        PrCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp, shape = RoundedCornerShape(22.dp)) {
            Eyebrow(stringResource(R.string.editor_name_label))
            BigTextField(
                value = state.name,
                onValueChange = onNameChange,
                placeholder = stringResource(R.string.editor_name_placeholder),
                isError = state.error == WorkoutError.NAME_REQUIRED || state.error == WorkoutError.NAME_TOO_LONG,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateField(date = state.date, onClick = { showDatePicker = true }, modifier = Modifier.weight(1f))
                Counter(
                    label = stringResource(R.string.editor_duration),
                    value = stringResource(R.string.duration_minutes, state.durationMinutes),
                    icon = Icons.Rounded.Schedule,
                    onDecrement = { onDurationChange(-5) },
                    onIncrement = { onDurationChange(5) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = colors.border)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.editor_completed), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                    Text(stringResource(R.string.editor_completed_desc), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
                Switch(
                    checked = state.isCompleted,
                    onCheckedChange = onCompletedChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = OnLime, checkedTrackColor = Lime),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.editor_exercises))
                Text(
                    text = if (state.exercises.isEmpty()) {
                        stringResource(R.string.editor_no_exercises)
                    } else {
                        pluralStringResource(R.plurals.exercise_count, state.exercises.size, state.exercises.size)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
            }
            AddExerciseButton(onClick = {
                editingExerciseId = null
                sheetOpen = true
            })
        }

        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.exercises.forEachIndexed { index, exercise ->
                ExerciseRow(
                    index = index,
                    exercise = exercise,
                    onClick = {
                        editingExerciseId = exercise.id
                        sheetOpen = true
                    },
                    onRemove = { onExerciseRemoved(exercise.id) },
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        AppTextField(
            label = stringResource(R.string.editor_notes),
            value = state.notes,
            onValueChange = onNotesChange,
            placeholder = stringResource(R.string.editor_notes_placeholder),
            singleLine = false,
        )

        state.error?.let {
            Spacer(Modifier.height(14.dp))
            ErrorMessage(stringResource(it.messageRes()))
        }
        if (state.saveFailed) {
            Spacer(Modifier.height(14.dp))
            ErrorMessage(stringResource(R.string.editor_save_failed))
        }

        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = stringResource(if (state.isSaving) R.string.editor_saving else R.string.editor_save),
            onClick = onSave,
            loading = state.isSaving,
            trailingIcon = Icons.Rounded.Check,
        )
        Spacer(Modifier.height(24.dp))
    }

    if (sheetOpen) {
        ExerciseSheet(
            initial = state.exercises.firstOrNull { it.id == editingExerciseId },
            onDismiss = { sheetOpen = false },
            onSave = {
                onExerciseSaved(it)
                sheetOpen = false
            },
        )
    }

    if (showDatePicker) {
        WorkoutDatePicker(
            initial = state.date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onDateChange(it)
                showDatePicker = false
            },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.editor_discard_title)) },
            text = { Text(stringResource(R.string.editor_discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBack()
                }) { Text(stringResource(R.string.action_discard), color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.action_keep_editing), color = colors.textPrimary)
                }
            },
            containerColor = colors.sheet,
        )
    }
}

/** Large borderless input for the workout name, as in the design. */
@Composable
private fun BigTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, isError: Boolean) {
    val colors = PrTheme.colors
    val style = MaterialTheme.typography.headlineSmall
    Column(Modifier.padding(top = 6.dp)) {
        Box {
            if (value.isEmpty()) Text(placeholder, style = style, color = colors.textMuted)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = style.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accentText),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = if (isError) colors.error else colors.border)
    }
}

@Composable
private fun DateField(date: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PrTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(colors.card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
            Text(stringResource(R.string.editor_date), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.height(30.dp), contentAlignment = Alignment.CenterStart) {
            Text(dateShort(date) + " ${date.year}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
    }
}

@Composable
private fun AddExerciseButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Lime)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = OnLime, modifier = Modifier.size(17.dp))
        Text(stringResource(R.string.editor_add_exercise), style = MaterialTheme.typography.labelMedium, color = OnLime)
    }
}

/** Exercise row with category tile, details, remove button and the faint index number. */
@Composable
private fun ExerciseRow(index: Int, exercise: Exercise, onClick: () -> Unit, onRemove: () -> Unit) {
    val colors = PrTheme.colors
    val style = exercise.category.style
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = "%02d".format(index + 1),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
            color = colors.textPrimary.copy(alpha = 0.04f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 48.dp),
        )
        Row(
            modifier = Modifier.padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTile(style.icon, background = style.background, tint = style.content, size = 48.dp, iconSize = 23.dp, cornerRadius = 14.dp)
            Column(Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(exerciseSummary(exercise), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_remove_exercise, exercise.name), tint = colors.textMuted)
            }
        }
    }
}

/** "4 seturi × 8 repetări · 60 kg" (weight omitted for bodyweight exercises). */
@Composable
fun exerciseSummary(exercise: Exercise): String {
    val base = stringResource(R.string.exercise_sets_reps, exercise.sets, exercise.reps)
    return if (exercise.weightKg > 0) "$base · " + stringResource(R.string.weight_kg, Format.weight(exercise.weightKg)) else base
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDatePicker(initial: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    // The Material date picker works with UTC midnight timestamps.
    val initialMillis = remember(initial) { initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = pickerState.selectedDateMillis
                if (millis != null) onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()) else onDismiss()
            }) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DatePicker(state = pickerState)
    }
}
