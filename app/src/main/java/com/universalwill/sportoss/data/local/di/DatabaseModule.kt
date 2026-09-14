package com.universalwill.sportoss.data.local.di

import android.content.Context
import androidx.room3.Room
import com.universalwill.sportoss.data.local.SportOSSDatabase
import com.universalwill.sportoss.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): SportOSSDatabase {
        return Room.databaseBuilder(
            context,
            SportOSSDatabase::class.java,
            "sportoss.db",
        ).build()
    }

    @Provides
    fun provideWorkoutDao(database: SportOSSDatabase): WorkoutDao {
        return database.workoutDao()
    }
}
