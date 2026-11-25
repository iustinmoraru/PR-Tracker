package com.iustinmoraru.prtracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.ui.theme.PrTheme

/**
 * Circular progress ring (the "2/3" ring on Home and the deadhang timer).
 * [progress] is between 0 and 1; [content] is drawn in the middle.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    strokeWidth: Dp = 5.dp,
    color: Color = PrTheme.colors.accent,
    trackColor: Color = PrTheme.colors.cardStrong,
    animate: Boolean = true,
    content: @Composable () -> Unit,
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(targetValue = target, animationSpec = tween(if (animate) 600 else 0), label = "ring")
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(trackColor, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/**
 * Simple vertical bar chart with a label under each bar ("Ultimele 7 zile").
 * Bars are scaled relative to the largest value.
 */
@Composable
fun BarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    highlightLast: Boolean = true,
    contentDescription: String,
) {
    val colors = PrTheme.colors
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { index, value ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.Bottom),
            ) {
                val fraction = if (value == 0) 0.04f else (value.toFloat() / max).coerceAtLeast(0.04f)
                val isToday = highlightLast && index == values.lastIndex
                // The weighted box takes the free height; the bar fills a fraction of it.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.55f)
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(colors.accent, colors.accent.copy(alpha = if (isToday) 0.6f else 0.18f))
                                )
                            )
                    )
                }
                Text(labels.getOrElse(index) { "" }, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
    }
}

/**
 * Minimal line chart for the evolution of an exercise (weight per session).
 * Points are spread evenly on X and scaled between the min and max value on Y.
 */
@Composable
fun LineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = PrTheme.colors.accentText,
    contentDescription: String,
) {
    val pointColor = PrTheme.colors.accent
    val gridColor = PrTheme.colors.border
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        if (values.isEmpty()) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0f } ?: 1f
        val padding = 8.dp.toPx()
        val usableHeight = size.height - padding * 2
        val stepX = if (values.size > 1) (size.width - padding * 2) / (values.size - 1) else 0f

        // Three horizontal guide lines.
        repeat(3) { i ->
            val y = padding + usableHeight * i / 2f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        val points = values.mapIndexed { index, value ->
            val x = if (values.size > 1) padding + stepX * index else size.width / 2
            val y = padding + usableHeight * (1f - (value - min) / range)
            Offset(x, y)
        }
        points.zipWithNext().forEach { (a, b) ->
            drawLine(lineColor, a, b, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        }
        points.forEach { point ->
            drawCircle(pointColor, radius = 5.dp.toPx(), center = point)
            drawCircle(lineColor, radius = 5.dp.toPx(), center = point, style = Stroke(2.dp.toPx()))
        }
    }
}
