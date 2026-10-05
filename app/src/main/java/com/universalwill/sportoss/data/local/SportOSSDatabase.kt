package com.universalwill.sportoss.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.universalwill.sportoss.data.local.dao.WorkoutDao
import com.universalwill.sportoss.data.local.entity.WorkoutEntity
import com.universalwill.sportoss.data.local.dao.RecordingDao
import com.universalwill.sportoss.data.local.entity.RecordingSessionEntity
import com.universalwill.sportoss.data.local.entity.TrackPointEntity

@Database(entities = [WorkoutEntity::class, RecordingSessionEntity::class, TrackPointEntity::class], version = 2)
abstract class SportOSSDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun recordingDao(): RecordingDao
}
