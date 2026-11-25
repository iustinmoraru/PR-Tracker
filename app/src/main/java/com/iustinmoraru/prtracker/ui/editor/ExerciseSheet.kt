package com.iustinmoraru.prtracker.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.domain.validation.WorkoutValidator
import com.iustinmoraru.prtracker.ui.common.Format
import com.iustinmoraru.prtracker.ui.components.AppTextField
import com.iustinmoraru.prtracker.ui.components.Counter
import com.iustinmoraru.prtracker.ui.components.ErrorMessage
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.IconSquareButton
import com.iustinmoraru.prtracker.ui.components.IconTile
import com.iustinmoraru.prtracker.ui.components.PrimaryButton
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.workout.style

private const val WEIGHT_STEP = 2.5
private const val MAX_WEIGHT = 500.0

/**
 * Bottom sheet for adding an exercise from the library (or editing one, when [initial] is set).
 * Mirrors the "Adaugă exercițiu" sheet from the design, plus a weight counter for PR tracking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSheet(
    initial: Exercise?,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
) {
    val colors = PrTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialPreset = initial?.let { ex -> ExercisePresets.firstOrNull { it.name.equals(ex.name, ignoreCase = true) } }
    var presetIndex by rememberSaveable { mutableIntStateOf(initialPreset?.let(ExercisePresets::indexOf) ?: 0) }
    var customName by rememberSaveable { mutableStateOf(if (initial != null && initialPreset == null) initial.name else "") }
    var sets by rememberSaveable { mutableIntStateOf(initial?.sets ?: 4) }
    var reps by rememberSaveable { mutableIntStateOf(initial?.reps ?: 8) }
    var weight by rememberSaveable { mutableDoubleStateOf(initial?.weightKg ?: ExercisePresets[0].defaultWeightKg) }
    var showError by rememberSaveable { mutableStateOf(false) }

    val selected = ExercisePresets[presetIndex]

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.sheet,
        contentColor = colors.textPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Eyebrow(stringResource(R.string.sheet_eyebrow))
                    Text(
                        stringResource(if (initial == null) R.string.sheet_title_add else R.string.sheet_title_edit),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary,
                    )
                }
                IconSquareButton(Icons.Rounded.Close, stringResource(R.string.action_close), onDismiss)
            }

            Spacer(Modifier.height(18.dp))
            // Two-column grid of presets (a plain Column of Rows, because the sheet already scrolls).
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExercisePresets.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEachIndexed { columnIndex, preset ->
                            val index = rowIndex * 2 + columnIndex
                            PresetCard(
                                preset = preset,
                                selected = index == presetIndex && customName.isBlank(),
                                onClick = {
                                    presetIndex = index
                                    customName = ""
                                    weight = preset.defaultWeightKg
                                    showError = false
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            AppTextField(
                label = stringResource(R.string.sheet_custom_name),
                value = customName,
                onValueChange = {
                    customName = it.take(40)
                    showError = false
                },
                placeholder = selected.name,
            )

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Counter(
                    label = stringResource(R.string.sheet_sets),
                    value = sets.toString(),
                    icon = Icons.Rounded.Layers,
                    onDecrement = { sets = (sets - 1).coerceAtLeast(1) },
                    onIncrement = { sets = (sets + 1).coerceAtMost(20) },
                    modifier = Modifier.weight(1f),
                )
                Counter(
                    label = stringResource(R.string.sheet_reps),
                    value = reps.toString(),
                    icon = Icons.Rounded.Repeat,
                    onDecrement = { reps = (reps - 1).coerceAtLeast(1) },
                    onIncrement = { reps = (reps + 1).coerceAtMost(100) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Counter(
                label = stringResource(R.string.sheet_weight),
                value = stringResource(R.string.weight_kg, Format.weight(weight)),
                icon = Icons.Rounded.Scale,
                onDecrement = { weight = (weight - WEIGHT_STEP).coerceAtLeast(0.0) },
                onIncrement = { weight = (weight + WEIGHT_STEP).coerceAtMost(MAX_WEIGHT) },
                modifier = Modifier.fillMaxWidth(),
            )

            if (showError) {
                Spacer(Modifier.height(12.dp))
                ErrorMessage(stringResource(R.string.error_exercise_name))
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = stringResource(if (initial == null) R.string.sheet_submit_add else R.string.sheet_submit_edit),
                trailingIcon = if (initial == null) Icons.Rounded.Add else Icons.Rounded.Check,
                onClick = {
                    val name = customName.trim().ifEmpty { selected.name }
                    if (!WorkoutValidator.isValidExerciseName(name)) {
                        showError = true
                    } else {
                        val base = initial ?: Exercise(name = name, category = selected.category, sets = sets, reps = reps, weightKg = weight)
                        onSave(base.copy(name = name, category = selected.category, sets = sets, reps = reps, weightKg = weight))
                    }
                },
            )
        }
    }
}

@Composable
private fun PresetCard(preset: ExercisePreset, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PrTheme.colors
    val style = preset.category.style
    val shape = RoundedCornerShape(17.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.accent.copy(alpha = 0.06f) else colors.card)
            .border(1.dp, if (selected) colors.accentText.copy(alpha = 0.6f) else colors.border, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconTile(style.icon, background = style.background, tint = style.content, size = 40.dp, iconSize = 22.dp)
        Column(Modifier.weight(1f)) {
            Text(preset.name, style = MaterialTheme.typography.labelMedium, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stringResource(style.label), style = MaterialTheme.typography.bodySmall, color = colors.textMuted, maxLines = 1)
        }
    }
}
