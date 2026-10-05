package com.universalwill.sportoss.domain.recording

import com.universalwill.sportoss.domain.enums.WorkoutType
import kotlinx.coroutines.flow.StateFlow

enum class RecordingPhase { Idle, Starting, Recording, Paused, Interrupted, Saving, SaveFailed }

enum class GpsStatus {
    PermissionRequired, PrecisePermissionRequired, Disabled, Unavailable,
    Searching, PoorAccuracy, Ready, Lost,
}

enum class RecordingError { Storage, ServiceStart, Save }

data class GpsFix(
    val latitude: Double,
    val longitude: Double,
    val epochMillis: Long,
    val elapsedRealtimeMillis: Long,
    val accuracyMeters: Float,
)

data class GpsReading(
    val status: GpsStatus = GpsStatus.PermissionRequired,
    val fix: GpsFix? = null,
)

data class TrackPoint(
    val workoutId: Long,
    val sequence: Long,
    val segment: Int,
    val fix: GpsFix,
)

data class RecordingSession(
    val workoutId: Long,
    val workoutType: WorkoutType,
    val startedAtEpochMillis: Long,
    val phase: RecordingPhase,
    val durationMillis: Long = 0,
    val distanceMeters: Double = 0.0,
    val segment: Int = 0,
    val nextSequence: Long = 0,
)

data class RecordingSnapshot(
    val session: RecordingSession? = null,
    val phase: RecordingPhase = RecordingPhase.Idle,
    val gps: GpsStatus = GpsStatus.PermissionRequired,
    val isLoading: Boolean = true,
    val error: RecordingError? = null,
)

interface RecordingClock {
    fun elapsedRealtimeMillis(): Long
    fun epochMillis(): Long
}

interface RecordingStore {
    suspend fun loadSession(): RecordingSession?
    suspend fun createSession(type: WorkoutType, startedAt: Long): RecordingSession
    suspend fun checkpoint(session: RecordingSession, points: List<TrackPoint>)
    suspend fun finish(session: RecordingSession, points: List<TrackPoint>)
}

interface GpsSource {
    val reading: StateFlow<GpsReading>
    fun start()
    fun stop()
    fun refresh()
}

interface RecordingServiceLauncher {
    fun launch(type: WorkoutType)
}

interface RecordingControl {
    val state: StateFlow<RecordingSnapshot>
    fun setUiVisible(visible: Boolean)
    fun toggle(type: WorkoutType)
    fun finish()
    fun retryLoad()
}
