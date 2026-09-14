package com.universalwill.sportoss.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.universalwill.sportoss.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY started_at_epoch_millis DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Insert
    suspend fun saveWorkout(workout: WorkoutEntity)
}
