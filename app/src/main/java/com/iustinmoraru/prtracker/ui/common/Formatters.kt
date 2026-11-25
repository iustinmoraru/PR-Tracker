package com.iustinmoraru.prtracker.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import com.iustinmoraru.prtracker.R
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToLong

/** Number formatting with Romanian conventions (decimal comma, dot for thousands). */
object Format {
    private val romanian: Locale = Locale.forLanguageTag("ro-RO")

    /** 60.0 → "60", 62.5 → "62,5". */
    fun weight(kg: Double): String =
        if (kg % 1.0 == 0.0) kg.toLong().toString() else String.format(romanian, "%.1f", kg)

    /** 12840.0 → "12.840". */
    fun volume(kg: Double): String = NumberFormat.getIntegerInstance(romanian).format(kg.roundToLong())
}

/** "MIE" for a Wednesday. */
@Composable
fun dayShort(date: LocalDate): String = stringArrayResource(R.array.days_short)[date.dayOfWeek.value - 1]

/** "25 noiembrie 2026". */
@Composable
fun dateLong(date: LocalDate): String =
    "${date.dayOfMonth} ${stringArrayResource(R.array.months_full)[date.monthValue - 1]} ${date.year}"

/** "25 noi". */
@Composable
fun dateShort(date: LocalDate): String =
    "${date.dayOfMonth} ${stringArrayResource(R.array.months_short)[date.monthValue - 1]}"

/** "23–29 NOI" for the week starting at [monday]. */
@Composable
fun weekRange(monday: LocalDate): String {
    val sunday = monday.plusDays(6)
    val months = stringArrayResource(R.array.months_short)
    return if (monday.month == sunday.month) {
        "${monday.dayOfMonth}–${sunday.dayOfMonth} ${months[sunday.monthValue - 1]}"
    } else {
        "${monday.dayOfMonth} ${months[monday.monthValue - 1]} – ${sunday.dayOfMonth} ${months[sunday.monthValue - 1]}"
    }.uppercase()
}
