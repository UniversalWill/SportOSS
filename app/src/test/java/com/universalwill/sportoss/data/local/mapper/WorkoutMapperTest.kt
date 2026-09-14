package com.universalwill.sportoss.data.local.mapper

import com.universalwill.sportoss.data.local.entity.WorkoutEntity
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.Workout
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutMapperTest {
    @Test
    fun `workout maps to entity and back without losing data`() {
        val workout = Workout(
            id = 7,
            type = WorkoutType.RUNNING,
            startedAtEpochMillis = 123_456,
            durationSeconds = 0,
            distanceMeters = 0.0,
        )

        assertEquals(workout, workout.toEntity().toDomain())
    }

    @Test
    fun `entity maps to workout and back without losing data`() {
        val entity = WorkoutEntity(
            id = 11,
            type = WorkoutType.BIKING,
            startedAtEpochMillis = 987_654,
            durationSeconds = 3_600,
            distanceMeters = 25_000.0,
        )

        assertEquals(entity, entity.toDomain().toEntity())
    }
}
