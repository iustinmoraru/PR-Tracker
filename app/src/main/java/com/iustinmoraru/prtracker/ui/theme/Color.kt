package com.iustinmoraru.prtracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand colours taken from the Figma design.
val Lime = Color(0xFFE6FF62)
val OnLime = Color(0xFF1C2500)
val Coral = Color(0xFFFF8069)
val OnCoral = Color(0xFF451207)
val Violet = Color(0xFFBA9CFF)
val OnViolet = Color(0xFF28114F)
val Teal = Color(0xFF71E5D1)
val OnTeal = Color(0xFF07342E)

/**
 * Semantic colours used by the app's custom components.
 *
 * Material's [androidx.compose.material3.ColorScheme] is derived from this palette as well,
 * so stock Material components (dialogs, snackbars, menus) match the design.
 */
@Immutable
data class PrColors(
    val isDark: Boolean,
    val background: Color,
    val sheet: Color,
    val card: Color,
    val cardStrong: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    /** Filled accent (buttons, highlights). Always paired with [onAccent]. */
    val accent: Color,
    val onAccent: Color,
    /** Accent used for text and icons drawn directly on [background]. */
    val accentText: Color,
    val violetText: Color,
    val error: Color,
    val errorContainer: Color,
)

val DarkPrColors = PrColors(
    isDark = true,
    background = Color(0xFF090A0E),
    sheet = Color(0xFF121319),
    card = Color.White.copy(alpha = 0.045f),
    cardStrong = Color.White.copy(alpha = 0.08f),
    border = Color.White.copy(alpha = 0.08f),
    textPrimary = Color.White,
    textSecondary = Color.White.copy(alpha = 0.55f),
    textMuted = Color.White.copy(alpha = 0.36f),
    accent = Lime,
    onAccent = OnLime,
    accentText = Lime,
    violetText = Violet,
    error = Color(0xFFFF9A88),
    errorContainer = Color(0xFFFF6C58).copy(alpha = 0.09f),
)

val LightPrColors = PrColors(
    isDark = false,
    background = Color(0xFFF3F4EE),
    sheet = Color.White,
    card = Color.White,
    cardStrong = Color(0xFF121318).copy(alpha = 0.06f),
    border = Color(0xFF121318).copy(alpha = 0.08f),
    textPrimary = Color(0xFF121318),
    textSecondary = Color(0xFF121318).copy(alpha = 0.62f),
    textMuted = Color(0xFF121318).copy(alpha = 0.45f),
    accent = Lime,
    onAccent = OnLime,
    accentText = Color(0xFF526A00),
    violetText = Color(0xFF6A47D6),
    error = Color(0xFFC8371F),
    errorContainer = Color(0xFFFF6C58).copy(alpha = 0.12f),
)

val LocalPrColors = staticCompositionLocalOf { DarkPrColors }
