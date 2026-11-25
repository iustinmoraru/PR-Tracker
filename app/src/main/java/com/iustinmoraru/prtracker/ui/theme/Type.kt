package com.iustinmoraru.prtracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iustinmoraru.prtracker.R

/** Manrope, the typeface used in the Figma design (bundled in res/font). */
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semi_bold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extra_bold, FontWeight.ExtraBold),
    // Manrope has no Black weight; fall back to ExtraBold for it.
    Font(R.font.manrope_extra_bold, FontWeight.Black),
)

private val base = TextStyle(fontFamily = Manrope)

val PrTypography = Typography(
    displaySmall = base.copy(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp, letterSpacing = (-0.05).em),
    headlineLarge = base.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 33.sp, letterSpacing = (-0.05).em),
    headlineMedium = base.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp, letterSpacing = (-0.045).em),
    headlineSmall = base.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 26.sp, letterSpacing = (-0.035).em),
    titleLarge = base.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 24.sp, letterSpacing = (-0.03).em),
    titleMedium = base.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 20.sp),
    titleSmall = base.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp),
    bodyLarge = base.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
    bodyMedium = base.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    bodySmall = base.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp),
    labelLarge = base.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 18.sp),
    labelMedium = base.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp),
    labelSmall = base.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 14.sp, letterSpacing = 0.16.em),
)
