package com.universalwill.sportoss.domain.model

import com.universalwill.sportoss.domain.enums.WorkoutType

data class Workout(
    val id: Long,
    val type: WorkoutType,
    val startedAtEpochMillis: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
)