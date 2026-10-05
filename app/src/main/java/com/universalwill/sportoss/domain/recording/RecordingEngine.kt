package com.universalwill.sportoss.domain.recording

import com.universalwill.sportoss.domain.enums.WorkoutType

/** Mutated only by the recording coordinator's serialized commands. */
class RecordingEngine {
    private var session: RecordingSession? = null
    private var activeSince: Long? = null
    private var lastPoint: TrackPoint? = null
    private var lastObservedFixMillis: Long? = null

    fun restore(saved: RecordingSession) {
        session = saved.copy(phase = if (saved.phase in setOf(RecordingPhase.SaveFailed, RecordingPhase.Saving)) {
            RecordingPhase.SaveFailed
        } else {
            RecordingPhase.Interrupted
        })
        activeSince = null
        lastPoint = null
        lastObservedFixMillis = null
    }

    fun start(created: RecordingSession, now: Long) {
        check(session == null)
        session = created.copy(phase = RecordingPhase.Recording)
        activeSince = now
        lastPoint = null
        lastObservedFixMillis = null
    }

    fun resume(now: Long) {
        val current = checkNotNull(session)
        check(current.phase in setOf(RecordingPhase.Paused, RecordingPhase.Interrupted))
        session = current.copy(phase = RecordingPhase.Recording, segment = current.segment + 1)
        activeSince = now
        lastPoint = null
    }

    fun snapshot(now: Long): RecordingSession? = session?.let { current ->
        current.copy(durationMillis = current.durationMillis +
            (activeSince?.let { (now - it).coerceAtLeast(0) } ?: 0))
    }

    fun freeze(phase: RecordingPhase, now: Long) {
        session = snapshot(now)?.copy(phase = phase)
        activeSince = null
        lastPoint = null
    }

    fun breakSegment() {
        if (lastPoint != null) {
            session = session?.let { it.copy(segment = it.segment + 1) }
            lastPoint = null
        }
    }

    fun accept(fix: GpsFix, now: Long): TrackPoint? {
        val current = session ?: return null
        if (current.phase != RecordingPhase.Recording) return null
        if (fix.elapsedRealtimeMillis < checkNotNull(activeSince) ||
            lastObservedFixMillis?.let { fix.elapsedRealtimeMillis <= it } == true) return null
        if (GpsQuality.status(fix, now) != GpsStatus.Ready) {
            breakSegment()
            return null
        }
        val observedInterval = lastObservedFixMillis?.let { fix.elapsedRealtimeMillis - it } ?: 0
        lastObservedFixMillis = fix.elapsedRealtimeMillis
        val previous = lastPoint
        var distance = 0.0
        if (previous != null) {
            val interval = fix.elapsedRealtimeMillis - previous.fix.elapsedRealtimeMillis
            if (interval <= 0) return null
            if (observedInterval > GpsQuality.MAX_FIX_AGE_MILLIS) {
                breakSegment()
            } else {
                distance = GpsQuality.distanceMeters(previous.fix, fix)
                val maximumSpeed = when (current.workoutType) {
                    WorkoutType.RUNNING -> 12.0
                    WorkoutType.BIKING -> 35.0
                }
                if (distance / (interval / 1_000.0) > maximumSpeed) {
                    breakSegment()
                    return null
                }
                // Accumulate movement from the last accepted point instead of GPS jitter.
                val minimumMovement = maxOf(3.0, minOf(previous.fix.accuracyMeters, fix.accuracyMeters) / 2.0)
                if (distance < minimumMovement) return null
            }
        }
        val updated = checkNotNull(session)
        val point = TrackPoint(updated.workoutId, updated.nextSequence, updated.segment, fix)
        session = updated.copy(
            nextSequence = updated.nextSequence + 1,
            distanceMeters = updated.distanceMeters + distance,
        )
        lastPoint = point
        return point
    }

    fun clear() {
        session = null
        activeSince = null
        lastPoint = null
        lastObservedFixMillis = null
    }
}
