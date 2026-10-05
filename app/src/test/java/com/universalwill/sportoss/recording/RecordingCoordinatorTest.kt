package com.universalwill.sportoss.recording

import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.GpsFix
import com.universalwill.sportoss.domain.recording.GpsReading
import com.universalwill.sportoss.domain.recording.GpsSource
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingClock
import com.universalwill.sportoss.domain.recording.RecordingError
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.RecordingServiceLauncher
import com.universalwill.sportoss.domain.recording.RecordingSession
import com.universalwill.sportoss.domain.recording.RecordingStore
import com.universalwill.sportoss.domain.recording.TrackPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingCoordinatorTest {
    @Test
    fun `start requires fresh accurate GPS and does not auto start on fix`() = runTest {
        val fixture = Fixture(this)
        fixture.gps.reading.value = GpsReading(GpsStatus.Searching)
        runCurrent()
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        assertEquals(0, fixture.launches)
        fixture.fix()
        runCurrent()
        assertEquals(RecordingPhase.Idle, fixture.coordinator.state.value.phase)
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        assertEquals(RecordingPhase.Recording, fixture.coordinator.state.value.phase)
        assertEquals(1, fixture.store.points.size)
    }

    @Test
    fun `stale GPS cannot start even if provider last reported ready`() = runTest {
        val fixture = Fixture(this)
        runCurrent()
        advanceTimeBy(11_000)
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        assertEquals(0, fixture.launches)
    }

    @Test
    fun `recording persists time with screen hidden and no GPS`() = runTest {
        val fixture = Fixture(this)
        fixture.start()
        fixture.coordinator.setUiVisible(false)
        fixture.gps.reading.value = GpsReading(GpsStatus.Lost)
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(5_000, fixture.store.saved!!.durationMillis)
        assertTrue(fixture.gps.started)
    }

    @Test
    fun `pause excludes time and movement and resume starts a segment`() = runTest {
        val fixture = Fixture(this)
        fixture.start()
        advanceTimeBy(2_000)
        fixture.coordinator.pause()
        runCurrent()
        advanceTimeBy(5_000)
        fixture.fix(latitude = 1.0)
        runCurrent()
        assertEquals(1, fixture.store.points.size)
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(3_000, fixture.coordinator.state.value.session!!.durationMillis)
        assertEquals(0.0, fixture.coordinator.state.value.session!!.distanceMeters, 0.0)
        assertEquals(1, fixture.store.points.last().segment)
    }

    @Test
    fun `loss and return of GPS do not add a connecting distance`() = runTest {
        val fixture = Fixture(this)
        fixture.start()
        fixture.gps.reading.value = GpsReading(GpsStatus.Disabled)
        runCurrent()
        advanceTimeBy(2_000)
        fixture.fix(latitude = 1.0)
        runCurrent()
        assertEquals(1, fixture.store.points.last().segment)
        assertEquals(0.0, fixture.coordinator.state.value.session!!.distanceMeters, 0.0)
        assertEquals(2_000, fixture.coordinator.state.value.session!!.durationMillis)
    }

    @Test
    fun `repeated start requests do not create multiple sessions`() = runTest {
        val fixture = Fixture(this)
        runCurrent()
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        fixture.coordinator.toggle(WorkoutType.BIKING)
        runCurrent()
        assertEquals(1, fixture.launches)
        assertEquals(1, fixture.store.starts)
        assertEquals(WorkoutType.RUNNING, fixture.store.saved!!.workoutType)
    }

    @Test
    fun `finish failure freezes recording and retry preserves result without duplicates`() = runTest {
        val fixture = Fixture(this)
        fixture.start()
        advanceTimeBy(2_000)
        fixture.store.failFinish = true
        fixture.coordinator.finish()
        runCurrent()
        assertEquals(RecordingPhase.SaveFailed, fixture.coordinator.state.value.phase)
        assertEquals(RecordingError.Save, fixture.coordinator.state.value.error)
        advanceTimeBy(5_000)
        fixture.fix(latitude = 1.0)
        runCurrent()
        assertEquals(2_000, fixture.coordinator.state.value.session!!.durationMillis)
        assertEquals(1, fixture.store.points.size)
        fixture.store.failFinish = false
        fixture.coordinator.finish()
        fixture.coordinator.finish()
        runCurrent()
        assertEquals(1, fixture.store.completed.size)
        assertEquals(2_000, fixture.store.completed.single().durationMillis)
        assertEquals(RecordingPhase.Idle, fixture.coordinator.state.value.phase)
    }

    @Test
    fun `slow finalization accepts only one completion and no pause or resume`() = runTest {
        val fixture = Fixture(this)
        fixture.start()
        fixture.store.finishGate = CompletableDeferred()
        fixture.coordinator.finish()
        runCurrent()
        assertEquals(RecordingPhase.Saving, fixture.coordinator.state.value.phase)
        fixture.coordinator.finish()
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        fixture.store.finishGate!!.complete(Unit)
        runCurrent()
        assertEquals(1, fixture.store.completed.size)
        assertEquals(RecordingPhase.Idle, fixture.coordinator.state.value.phase)
    }

    @Test
    fun `checkpoint failure keeps unsaved point for retry and stops recording`() = runTest {
        val fixture = Fixture(this)
        fixture.start()
        advanceTimeBy(2_000)
        runCurrent()
        fixture.store.failCheckpoint = true
        fixture.fix(latitude = 0.0001)
        runCurrent()
        assertEquals(RecordingPhase.SaveFailed, fixture.coordinator.state.value.phase)
        fixture.store.failCheckpoint = false
        fixture.coordinator.finish()
        runCurrent()
        assertEquals(2, fixture.store.points.size)
        assertEquals(1, fixture.store.completed.size)
        assertTrue(fixture.store.completed.single().distanceMeters > 10)
    }

    @Test
    fun `recovery offers interrupted session and excludes time until explicit resume`() = runTest {
        val saved = RecordingSession(7, WorkoutType.BIKING, 123, RecordingPhase.Recording,
            durationMillis = 4_000, distanceMeters = 500.0, nextSequence = 8)
        val fixture = Fixture(this, saved)
        runCurrent()
        assertEquals(RecordingPhase.Interrupted, fixture.coordinator.state.value.phase)
        assertEquals(0, fixture.launches)
        advanceTimeBy(20_000)
        fixture.fix(latitude = 2.0)
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        assertEquals(4_000, fixture.coordinator.state.value.session!!.durationMillis)
        assertEquals(WorkoutType.BIKING, fixture.coordinator.state.value.session!!.workoutType)
        assertEquals(500.0, fixture.coordinator.state.value.session!!.distanceMeters, 0.0)
        assertEquals(1, fixture.store.points.last().segment)
    }

    @Test
    fun `failed service launch leaves no new workout and allows retry`() = runTest {
        val fixture = Fixture(this)
        fixture.failLaunch = true
        runCurrent()
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        assertEquals(RecordingPhase.Idle, fixture.coordinator.state.value.phase)
        assertEquals(RecordingError.ServiceStart, fixture.coordinator.state.value.error)
        assertEquals(0, fixture.store.starts)
        fixture.failLaunch = false
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        assertEquals(RecordingPhase.Recording, fixture.coordinator.state.value.phase)
    }

    @Test
    fun `failed initialization cannot start and can be retried`() = runTest {
        val fixture = Fixture(this)
        fixture.store.failLoad = true
        runCurrent()
        assertTrue(fixture.coordinator.state.value.isLoading)
        fixture.coordinator.toggle(WorkoutType.RUNNING)
        runCurrent()
        assertEquals(0, fixture.launches)
        fixture.store.failLoad = false
        fixture.coordinator.retryLoad()
        runCurrent()
        assertFalse(fixture.coordinator.state.value.isLoading)
    }

    private class Fixture(val scope: TestScope, saved: RecordingSession? = null) {
        val gps = FakeGps()
        val store = FakeStore(saved)
        var launches = 0
        var failLaunch = false
        val clock = object : RecordingClock {
            override fun elapsedRealtimeMillis() = scope.testScheduler.currentTime + 1_000
            override fun epochMillis() = scope.testScheduler.currentTime + 100_000
        }
        val coordinator: RecordingCoordinator = RecordingCoordinator(store, gps, clock, object : RecordingServiceLauncher {
            override fun launch(type: WorkoutType) {
                if (failLaunch) throw IllegalStateException("service unavailable")
                launches++
                coordinator.activate(type)
            }
        }, scope.backgroundScope)

        init { fix() }

        fun fix(latitude: Double = 0.0) {
            gps.reading.value = GpsReading(GpsStatus.Ready,
                GpsFix(latitude, 0.0, clock.epochMillis(), clock.elapsedRealtimeMillis(), 5f))
        }

        fun start() {
            scope.runCurrent()
            coordinator.setUiVisible(true)
            coordinator.toggle(WorkoutType.RUNNING)
            scope.runCurrent()
        }
    }

    private class FakeGps : GpsSource {
        override val reading = MutableStateFlow(GpsReading())
        var started = false
        override fun start() { started = true }
        override fun stop() { started = false }
        override fun refresh() = Unit
    }

    private class FakeStore(var saved: RecordingSession?) : RecordingStore {
        val points = mutableListOf<TrackPoint>()
        val completed = mutableListOf<RecordingSession>()
        var starts = 0
        var failLoad = false
        var failFinish = false
        var failCheckpoint = false
        var finishGate: CompletableDeferred<Unit>? = null
        override suspend fun loadSession(): RecordingSession? {
            check(!failLoad)
            return saved
        }
        override suspend fun createSession(type: WorkoutType, startedAt: Long): RecordingSession {
            check(saved == null)
            starts++
            return RecordingSession(starts.toLong(), type, startedAt, RecordingPhase.Recording).also { saved = it }
        }
        override suspend fun checkpoint(session: RecordingSession, points: List<TrackPoint>) {
            check(!failCheckpoint)
            saved = session
            this.points += points
        }
        override suspend fun finish(session: RecordingSession, points: List<TrackPoint>) {
            finishGate?.await()
            check(!failFinish)
            if (saved == null) return
            this.points += points
            completed += session
            saved = null
        }
    }
}
