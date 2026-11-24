package com.universalwill.sportoss.data.repository

import androidx.room3.withReadTransaction
import com.universalwill.sportoss.data.local.SportOSSDatabase
import com.universalwill.sportoss.data.local.mapper.toDomain
import com.universalwill.sportoss.data.local.mapper.toEntity
import com.universalwill.sportoss.domain.model.Workout
import com.universalwill.sportoss.domain.model.WorkoutDetails
import com.universalwill.sportoss.domain.model.WorkoutRoutePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface OfflineWorkoutRepository {
    fun observeWorkouts(): Flow<List<Workout>>
    suspend fun saveWorkout(workout: Workout)
    suspend fun getWorkoutDetails(id: Long): WorkoutDetails?
    suspend fun deleteWorkout(id: Long)
}

class OfflineWorkoutRepositoryImpl @Inject constructor(
    private val database: SportOSSDatabase,
) : OfflineWorkoutRepository {
    private val workoutDao = database.workoutDao()

    override fun observeWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun saveWorkout(workout: Workout) {
        workoutDao.saveWorkout(workout.toEntity())
    }

    override suspend fun getWorkoutDetails(id: Long): WorkoutDetails? = database.withReadTransaction {
        val workout = workoutDao.getCompletedWorkout(id) ?: return@withReadTransaction null
        val routeSegments = database.recordingDao().getTrack(id)
            .groupBy { it.segment }
            .values.map { segment ->
                segment.map { WorkoutRoutePoint(it.latitude, it.longitude) }
            }
        WorkoutDetails(workout.toDomain(), routeSegments)
    }

    override suspend fun deleteWorkout(id: Long) {
        workoutDao.deleteCompletedWorkout(id)
    }
}
