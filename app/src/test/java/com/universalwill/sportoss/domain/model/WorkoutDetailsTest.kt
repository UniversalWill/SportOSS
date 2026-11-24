package com.universalwill.sportoss.domain.model

import com.universalwill.sportoss.domain.enums.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutDetailsTest {
    @Test
    fun `average speed uses the saved distance and full active duration for both sports`() {
        for (type in WorkoutType.entries) {
            val workout = Workout(1, type, 0, 1_800, 5_000.0)
            val details = WorkoutDetails(workout, emptyList())
            assertEquals(10.0, checkNotNull(details.averageSpeedKilometersPerHour), 0.001)
        }
    }

    @Test
    fun `average speed is absent when duration or distance is zero`() {
        val workout = Workout(1, WorkoutType.RUNNING, 0, 1_800, 0.0)
        assertNull(WorkoutDetails(workout, emptyList()).averageSpeedKilometersPerHour)
        val zeroDuration = workout.copy(durationSeconds = 0, distanceMeters = 100.0)
        assertNull(WorkoutDetails(zeroDuration, emptyList()).averageSpeedKilometersPerHour)
    }
}
