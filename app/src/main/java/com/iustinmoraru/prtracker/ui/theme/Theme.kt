package com.iustinmoraru.prtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.data.settings.ThemeMode

private fun PrColors.toMaterial(): ColorScheme {
    val scheme = if (isDark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent,
        onPrimaryContainer = onAccent,
        secondary = Violet,
        onSecondary = OnViolet,
        tertiary = Coral,
        onTertiary = OnCoral,
        background = background,
        onBackground = textPrimary,
        surface = sheet,
        onSurface = textPrimary,
        surfaceVariant = cardStrong,
        onSurfaceVariant = textSecondary,
        surfaceContainer = sheet,
        surfaceContainerHigh = sheet,
        surfaceContainerHighest = sheet,
        surfaceContainerLow = sheet,
        outline = border,
        outlineVariant = border,
        error = error,
        inverseSurface = if (isDark) Lime else textPrimary,
        inverseOnSurface = if (isDark) OnLime else background,
        inversePrimary = if (isDark) OnLime else accent,
    )
}

private val PrShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Resolves a stored [ThemeMode] into "should the UI be dark right now?". */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * App theme. [darkTheme] normally comes from the user's preference (Profil → Setări),
 * which defaults to following the device theme.
 */
@Composable
fun PrTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkPrColors else LightPrColors
    CompositionLocalProvider(LocalPrColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterial(),
            typography = PrTypography,
            shapes = PrShapes,
            content = content,
        )
    }
}

/** Shortcut for the app palette: `PrTheme.colors.accent`. */
object PrTheme {
    val colors: PrColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPrColors.current
}
