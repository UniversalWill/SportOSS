package com.universalwill.sportoss.data.local.mapper

import com.universalwill.sportoss.data.local.entity.WorkoutEntity
import com.universalwill.sportoss.domain.model.Workout

fun WorkoutEntity.toDomain(): Workout {
    return Workout(
        id = id,
        type = type,
        startedAtEpochMillis = startedAtEpochMillis,
        durationSeconds = durationSeconds,
        distanceMeters = distanceMeters,
    )
}

fun Workout.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        type = type,
        startedAtEpochMillis = startedAtEpochMillis,
        durationSeconds = durationSeconds,
        distanceMeters = distanceMeters,
    )
}

fun List<WorkoutEntity>.toDomain(): List<Workout> = map { it.toDomain() }
fun List<Workout>.toEntity(): List<WorkoutEntity> = map { it.toEntity() }
