package com.universalwill.sportoss.data.recording

import androidx.room3.withWriteTransaction
import com.universalwill.sportoss.data.local.SportOSSDatabase
import com.universalwill.sportoss.data.local.entity.RecordingSessionEntity
import com.universalwill.sportoss.data.local.entity.TrackPointEntity
import com.universalwill.sportoss.data.local.entity.WorkoutEntity
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.RecordingSession
import com.universalwill.sportoss.domain.recording.RecordingStore
import com.universalwill.sportoss.domain.recording.TrackPoint
import javax.inject.Inject

class RoomRecordingStore @Inject constructor(
    private val database: SportOSSDatabase,
) : RecordingStore {
    private val dao get() = database.recordingDao()

    override suspend fun loadSession(): RecordingSession? = dao.getSession()?.let {
        RecordingSession(
            it.workoutId, WorkoutType.valueOf(it.workoutType), it.startedAtEpochMillis,
            RecordingPhase.valueOf(it.phase), it.durationMillis, it.distanceMeters,
            it.segment, it.nextSequence,
        )
    }

    override suspend fun createSession(type: WorkoutType, startedAt: Long): RecordingSession =
        database.withWriteTransaction {
            check(dao.getSession() == null) { "An unfinished recording already exists" }
            val id = dao.insertWorkout(WorkoutEntity(0, type, startedAt, 0, 0.0, false))
            val session = RecordingSession(id, type, startedAt, RecordingPhase.Recording)
            dao.putSession(session.toEntity())
            session
        }

    override suspend fun checkpoint(session: RecordingSession, points: List<TrackPoint>) {
        database.withWriteTransaction {
            check(dao.getSession()?.workoutId == session.workoutId)
            dao.insertPoints(points.map { it.toEntity() })
            dao.putSession(session.toEntity())
        }
    }

    override suspend fun finish(session: RecordingSession, points: List<TrackPoint>) {
        database.withWriteTransaction {
            val saved = dao.getSession() ?: return@withWriteTransaction
            check(saved.workoutId == session.workoutId)
            dao.insertPoints(points.map { it.toEntity() })
            dao.completeWorkout(session.workoutId, session.durationMillis / 1_000, session.distanceMeters)
            dao.deleteSession(session.workoutId)
        }
    }
}

private fun RecordingSession.toEntity() = RecordingSessionEntity(
    workoutId = workoutId, workoutType = workoutType.name, startedAtEpochMillis = startedAtEpochMillis,
    phase = phase.name, durationMillis = durationMillis, distanceMeters = distanceMeters,
    segment = segment, nextSequence = nextSequence,
)

private fun TrackPoint.toEntity() = TrackPointEntity(
    workoutId, sequence, segment, fix.latitude, fix.longitude, fix.epochMillis,
    fix.elapsedRealtimeMillis, fix.accuracyMeters,
)
