package com.universalwill.sportoss.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import com.universalwill.sportoss.data.local.entity.RecordingSessionEntity
import com.universalwill.sportoss.data.local.entity.TrackPointEntity
import com.universalwill.sportoss.data.local.entity.WorkoutEntity

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recording_session WHERE id = 1")
    suspend fun getSession(): RecordingSessionEntity?

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Upsert
    suspend fun putSession(session: RecordingSessionEntity)

    @Insert
    suspend fun insertPoints(points: List<TrackPointEntity>)

    @Query("SELECT * FROM track_points WHERE workoutId = :workoutId ORDER BY sequence")
    suspend fun getTrack(workoutId: Long): List<TrackPointEntity>

    @Query("UPDATE workouts SET duration_seconds = :durationSeconds, distance_meters = :distanceMeters, is_completed = 1 WHERE id = :workoutId")
    suspend fun completeWorkout(workoutId: Long, durationSeconds: Long, distanceMeters: Double)

    @Query("DELETE FROM recording_session WHERE id = 1 AND workoutId = :workoutId")
    suspend fun deleteSession(workoutId: Long)
}
