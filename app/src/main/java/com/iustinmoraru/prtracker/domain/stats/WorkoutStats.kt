package com.iustinmoraru.prtracker.domain.stats

import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.data.workout.Workout
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/** Best performance ever recorded for one exercise. */
data class PersonalRecord(
    val exerciseName: String,
    val key: String,
    val weightKg: Double,
    val reps: Int,
    val date: LocalDate,
    val workoutId: String,
)

/** One appearance of an exercise in a completed workout (for the history screen). */
data class ExerciseEntry(
    val date: LocalDate,
    val workoutId: String,
    val workoutName: String,
    val exercise: Exercise,
)

/**
 * Statistics computed from the user's workouts. Pure functions without Android
 * dependencies, covered by unit tests. Only completed workouts count towards records.
 */
object WorkoutStats {

    /** True if [a] is a better performance than [b]: heavier, or same weight with more reps. */
    private fun isBetter(a: Exercise, b: PersonalRecord): Boolean =
        a.weightKg > b.weightKg || (a.weightKg == b.weightKg && a.reps > b.reps)

    /** Personal record per exercise, sorted by exercise name. */
    fun personalRecords(workouts: List<Workout>): List<PersonalRecord> {
        val records = linkedMapOf<String, PersonalRecord>()
        workouts.filter { it.isCompleted }.sortedBy { it.date }.forEach { workout ->
            workout.exercises.forEach { exercise ->
                val current = records[exercise.key]
                if (current == null || isBetter(exercise, current)) {
                    records[exercise.key] = PersonalRecord(
                        exerciseName = exercise.name.trim(),
                        key = exercise.key,
                        weightKg = exercise.weightKg,
                        reps = exercise.reps,
                        date = workout.date,
                        workoutId = workout.id,
                    )
                }
            }
        }
        return records.values.sortedBy { it.exerciseName.lowercase() }
    }

    /**
     * Exercises in [candidate] that beat the previous record from [others].
     * Exercises done for the first time are not reported, to avoid a notification for everything.
     */
    fun newRecords(others: List<Workout>, candidate: Workout): List<Exercise> {
        val previous = personalRecords(others.filter { it.id != candidate.id }).associateBy { it.key }
        return candidate.exercises
            .groupBy { it.key }
            .mapNotNull { (key, sameExercise) ->
                val best = sameExercise.maxWith(compareBy<Exercise> { it.weightKg }.thenBy { it.reps })
                val record = previous[key] ?: return@mapNotNull null
                best.takeIf { isBetter(it, record) }
            }
    }

    /** Keys of the exercises in [workout] that currently hold the personal record. */
    fun recordKeysIn(workout: Workout, records: List<PersonalRecord>): Set<String> =
        records.filter { it.workoutId == workout.id }.map { it.key }.toSet()

    /** History of one exercise across completed workouts, oldest first. */
    fun exerciseHistory(workouts: List<Workout>, key: String): List<ExerciseEntry> =
        workouts.filter { it.isCompleted }
            .sortedBy { it.date }
            .flatMap { workout ->
                workout.exercises.filter { it.key == key }
                    .map { ExerciseEntry(workout.date, workout.id, workout.name, it) }
            }

    fun startOfWeek(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Workouts scheduled in the Monday–Sunday week that contains [today]. */
    fun workoutsThisWeek(workouts: List<Workout>, today: LocalDate): List<Workout> {
        val start = startOfWeek(today)
        val end = start.plusDays(6)
        return workouts.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }.sortedBy { it.date }
    }

    /**
     * Number of consecutive weeks with at least one completed workout. The current week
     * counts if it already has one; otherwise the streak is measured up to last week.
     */
    fun weekStreak(workouts: List<Workout>, today: LocalDate): Int {
        val activeWeeks = workouts.filter { it.isCompleted }.map { startOfWeek(it.date) }.toSet()
        var week = startOfWeek(today)
        if (week !in activeWeeks) week = week.minusWeeks(1)
        var streak = 0
        while (week in activeWeeks) {
            streak++
            week = week.minusWeeks(1)
        }
        return streak
    }

    fun totalVolumeKg(workouts: List<Workout>): Double =
        workouts.filter { it.isCompleted }.sumOf { it.totalVolumeKg }

    fun totalMinutes(workouts: List<Workout>): Int =
        workouts.filter { it.isCompleted }.sumOf { it.durationMinutes }

    private fun volumeIn(workouts: List<Workout>, month: YearMonth): Double =
        workouts.filter { it.isCompleted && YearMonth.from(it.date) == month }.sumOf { it.totalVolumeKg }

    /** Percentage change of this month's volume compared with last month, or null without data. */
    fun monthlyVolumeChangePercent(workouts: List<Workout>, today: LocalDate): Int? {
        val thisMonth = YearMonth.from(today)
        val previous = volumeIn(workouts, thisMonth.minusMonths(1))
        if (previous <= 0.0) return null
        val current = volumeIn(workouts, thisMonth)
        return Math.round((current - previous) / previous * 100).toInt()
    }

    /** Minutes trained on each of the last 7 days (index 0 = six days ago, 6 = today). */
    fun minutesLast7Days(workouts: List<Workout>, today: LocalDate): List<Int> =
        (6 downTo 0).map { daysAgo ->
            val day = today.minusDays(daysAgo.toLong())
            workouts.filter { it.isCompleted && it.date == day }.sumOf { it.durationMinutes }
        }
}
