package com.universalwill.sportoss.recording.di

import com.universalwill.sportoss.data.recording.AndroidGpsSource
import com.universalwill.sportoss.data.recording.AndroidRecordingClock
import com.universalwill.sportoss.data.recording.AndroidRecordingServiceLauncher
import com.universalwill.sportoss.data.recording.RoomRecordingStore
import com.universalwill.sportoss.domain.recording.GpsSource
import com.universalwill.sportoss.domain.recording.RecordingClock
import com.universalwill.sportoss.domain.recording.RecordingControl
import com.universalwill.sportoss.domain.recording.RecordingServiceLauncher
import com.universalwill.sportoss.domain.recording.RecordingStore
import com.universalwill.sportoss.recording.RecordingCoordinator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RecordingScope

@Module
@InstallIn(SingletonComponent::class)
abstract class RecordingModule {
    @Binds abstract fun bindGps(implementation: AndroidGpsSource): GpsSource
    @Binds abstract fun bindClock(implementation: AndroidRecordingClock): RecordingClock
    @Binds abstract fun bindStore(implementation: RoomRecordingStore): RecordingStore
    @Binds abstract fun bindLauncher(implementation: AndroidRecordingServiceLauncher): RecordingServiceLauncher
    @Binds abstract fun bindControl(implementation: RecordingCoordinator): RecordingControl

    companion object {
        @Provides
        @Singleton
        @RecordingScope
        fun provideScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
}
