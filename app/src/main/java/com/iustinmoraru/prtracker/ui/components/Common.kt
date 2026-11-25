package com.iustinmoraru.prtracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.ui.theme.PrTheme

/** Small uppercase, letter-spaced label shown above titles ("PLANUL TĂU"). */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = PrTheme.colors.textMuted) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier,
    )
}

/**
 * Rounded, bordered card used across the app. Supports a click and a long click,
 * which is how list items open their context menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    background: Color = PrTheme.colors.card,
    borderColor: Color = PrTheme.colors.border,
    contentPadding: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick,
        )
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .then(clickModifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Coloured rounded square holding an icon (exercise visuals, metric icons). */
@Composable
fun IconTile(
    icon: ImageVector,
    background: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    cornerRadius: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Red inline error banner used for validation and network errors. */
@Composable
fun ErrorMessage(text: String, modifier: Modifier = Modifier) {
    val colors = PrTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.errorContainer)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
        Text(text, color = colors.error, style = MaterialTheme.typography.bodySmall)
    }
}

/** Title block: eyebrow + title, optionally followed by an action on the right. */
@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Eyebrow(eyebrow)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = PrTheme.colors.textPrimary)
        }
        action?.invoke()
    }
}

/**
 * Header for secondary activities: back button, eyebrow + title and an optional action
 * (matches the "Antrenament nou" screen from the design).
 */
@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconSquareButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Eyebrow(eyebrow)
            Text(title, style = MaterialTheme.typography.headlineSmall, color = PrTheme.colors.textPrimary, maxLines = 1)
        }
        actions?.invoke()
    }
}
