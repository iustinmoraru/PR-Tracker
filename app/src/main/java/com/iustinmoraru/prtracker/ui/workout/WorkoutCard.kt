package com.iustinmoraru.prtracker.ui.workout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.ui.common.dayShort
import com.iustinmoraru.prtracker.ui.theme.PrTheme

/**
 * Workout row from the design: coloured accent bar, date tile, name, "Făcut" pill and meta.
 * A tap opens the details; a long press opens the context menu (handled by the caller).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PrTheme.colors
    val shape = RoundedCornerShape(20.dp)
    val longClickLabel = stringResource(R.string.cd_workout_options)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.border, shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick, onLongClickLabel = longClickLabel),
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(workout.accent)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 11.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DateTile(workout)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (workout.isCompleted) CompletedPill()
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    MetaItem(Icons.Rounded.Schedule, stringResource(R.string.duration_minutes, workout.durationMinutes))
                    MetaItem(
                        Icons.Rounded.FitnessCenter,
                        pluralStringResource(R.plurals.exercise_count, workout.exercises.size, workout.exercises.size),
                    )
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.textMuted)
        }
    }
}

@Composable
private fun DateTile(workout: Workout) {
    val colors = PrTheme.colors
    Column(
        modifier = Modifier
            .size(width = 48.dp, height = 55.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardStrong),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(dayShort(workout.date), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = colors.textMuted)
        Text(workout.date.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
    }
}

/** Small lime "FĂCUT" badge for completed workouts. */
@Composable
fun CompletedPill(modifier: Modifier = Modifier) {
    val colors = PrTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.accent.copy(alpha = if (colors.isDark) 0.12f else 0.55f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val tint = if (colors.isDark) colors.accentText else colors.onAccent
        Icon(Icons.Rounded.Check, contentDescription = null, tint = tint, modifier = Modifier.size(11.dp))
        Text(
            stringResource(R.string.status_done).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 0.05.sp),
            color = tint,
        )
    }
}

@Composable
private fun MetaItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    val colors = PrTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(13.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
    }
}
