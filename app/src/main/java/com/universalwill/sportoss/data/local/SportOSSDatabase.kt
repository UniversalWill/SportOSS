package com.universalwill.sportoss.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.universalwill.sportoss.data.local.dao.WorkoutDao
import com.universalwill.sportoss.data.local.entity.WorkoutEntity

@Database(entities = [WorkoutEntity::class], version = 1)
abstract class SportOSSDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}