package com.iustinmoraru.prtracker.domain

import com.iustinmoraru.prtracker.data.workout.Exercise
import com.iustinmoraru.prtracker.data.workout.ExerciseCategory
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.data.workout.WorkoutStatus
import com.iustinmoraru.prtracker.domain.stats.WorkoutStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkoutStatsTest {

    private val today = LocalDate.of(2026, 11, 25) // a Wednesday

    private fun bench(weight: Double, reps: Int = 8) =
        Exercise(name = "Bench press", category = ExerciseCategory.STRENGTH, sets = 4, reps = reps, weightKg = weight)

    private fun workout(id: String, date: LocalDate, vararg exercises: Exercise, done: Boolean = true) = Workout(
        id = id,
        name = "W$id",
        date = date,
        durationMinutes = 50,
        status = if (done) WorkoutStatus.COMPLETED else WorkoutStatus.PLANNED,
        exercises = exercises.toList(),
    )

    @Test
    fun `personal record is the heaviest completed set`() {
        val workouts = listOf(
            workout("1", today.minusDays(10), bench(60.0)),
            workout("2", today.minusDays(3), bench(70.0)),
            workout("3", today, bench(90.0), done = false), // planned: does not count
        )
        val record = WorkoutStats.personalRecords(workouts).single()
        assertEquals(70.0, record.weightKg, 0.0)
        assertEquals("2", record.workoutId)
    }

    @Test
    fun `same weight with more reps is a better record`() {
        val workouts = listOf(
            workout("1", today.minusDays(5), bench(80.0, reps = 5)),
            workout("2", today.minusDays(1), bench(80.0, reps = 6)),
        )
        assertEquals("2", WorkoutStats.personalRecords(workouts).single().workoutId)
    }

    @Test
    fun `new records are detected only when beating a previous best`() {
        val history = listOf(workout("1", today.minusDays(7), bench(60.0)))
        assertEquals(1, WorkoutStats.newRecords(history, workout("2", today, bench(62.5))).size)
        assertTrue(WorkoutStats.newRecords(history, workout("2", today, bench(60.0))).isEmpty())
        assertTrue(WorkoutStats.newRecords(emptyList(), workout("2", today, bench(100.0))).isEmpty())
    }

    @Test
    fun `week streak counts consecutive active weeks`() {
        val workouts = listOf(
            workout("1", today, bench(50.0)),
            workout("2", today.minusWeeks(1), bench(50.0)),
            workout("3", today.minusWeeks(2), bench(50.0)),
            workout("4", today.minusWeeks(4), bench(50.0)),
        )
        assertEquals(3, WorkoutStats.weekStreak(workouts, today))
    }

    @Test
    fun `streak still counts last week when this week is empty`() {
        val workouts = listOf(workout("1", today.minusWeeks(1), bench(50.0)))
        assertEquals(1, WorkoutStats.weekStreak(workouts, today))
    }

    @Test
    fun `monthly volume change compares with the previous month`() {
        val workouts = listOf(
            workout("1", LocalDate.of(2026, 10, 10), bench(50.0)), // 4*8*50 = 1600
            workout("2", LocalDate.of(2026, 11, 10), bench(55.0)), // 4*8*55 = 1760
        )
        assertEquals(10, WorkoutStats.monthlyVolumeChangePercent(workouts, today))
        assertNull(WorkoutStats.monthlyVolumeChangePercent(emptyList(), today))
    }

    @Test
    fun `this week runs from Monday to Sunday`() {
        val workouts = listOf(
            workout("mon", LocalDate.of(2026, 11, 23), bench(50.0)),
            workout("sun", LocalDate.of(2026, 11, 29), bench(50.0)),
            workout("prev", LocalDate.of(2026, 11, 22), bench(50.0)),
        )
        assertEquals(listOf("mon", "sun"), WorkoutStats.workoutsThisWeek(workouts, today).map { it.id })
    }
}
