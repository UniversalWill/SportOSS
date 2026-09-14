package com.universalwill.sportoss.data.repository.di

import com.universalwill.sportoss.data.repository.OfflineWorkoutRepository
import com.universalwill.sportoss.data.repository.OfflineWorkoutRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindOfflineWorkoutRepository(
        implementation: OfflineWorkoutRepositoryImpl,
    ): OfflineWorkoutRepository
}
