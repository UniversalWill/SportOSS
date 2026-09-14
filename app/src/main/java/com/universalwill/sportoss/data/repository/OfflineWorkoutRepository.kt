package com.universalwill.sportoss.data.repository

import com.universalwill.sportoss.data.local.dao.WorkoutDao
import com.universalwill.sportoss.data.local.mapper.toDomain
import com.universalwill.sportoss.data.local.mapper.toEntity
import com.universalwill.sportoss.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface OfflineWorkoutRepository {
    fun observeWorkouts(): Flow<List<Workout>>
    suspend fun saveWorkout(workout: Workout)
}

class OfflineWorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
) : OfflineWorkoutRepository {

    override fun observeWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun saveWorkout(workout: Workout) {
        workoutDao.saveWorkout(workout.toEntity())
    }
}
