package com.universalwill.sportoss.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "recording_session",
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class, parentColumns = ["id"], childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["workoutId"], unique = true)],
)
data class RecordingSessionEntity(
    @PrimaryKey val id: Int = 1,
    val workoutId: Long,
    val workoutType: String,
    val startedAtEpochMillis: Long,
    val phase: String,
    val durationMillis: Long,
    val distanceMeters: Double,
    val segment: Int,
    val nextSequence: Long,
)

@Entity(
    tableName = "track_points",
    primaryKeys = ["workoutId", "sequence"],
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class, parentColumns = ["id"], childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class TrackPointEntity(
    val workoutId: Long,
    val sequence: Long,
    val segment: Int,
    val latitude: Double,
    val longitude: Double,
    val epochMillis: Long,
    val elapsedRealtimeMillis: Long,
    val accuracyMeters: Float,
)
