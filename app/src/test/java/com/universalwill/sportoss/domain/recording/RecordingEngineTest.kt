package com.universalwill.sportoss.domain.recording

import com.universalwill.sportoss.domain.enums.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingEngineTest {
    @Test
    fun `elapsed time does not depend on timer updates and excludes pause`() {
        val engine = started()
        assertEquals(12_345, engine.snapshot(13_345)!!.durationMillis)
        engine.freeze(RecordingPhase.Paused, 13_345)
        assertEquals(12_345, engine.snapshot(100_000)!!.durationMillis)
        engine.resume(100_000)
        assertEquals(14_345, engine.snapshot(102_000)!!.durationMillis)
    }

    @Test
    fun `distance is calculated between accepted coordinates`() {
        val engine = started()
        engine.accept(fix(1_000), 1_000)
        engine.accept(fix(3_000, latitude = 0.0001), 3_000)
        assertEquals(11.119, engine.snapshot(3_000)!!.distanceMeters, 0.01)
    }

    @Test
    fun `pause movement is not recorded or connected on resume`() {
        val engine = started()
        val before = engine.accept(fix(1_000), 1_000)!!
        engine.freeze(RecordingPhase.Paused, 2_000)
        assertNull(engine.accept(fix(5_000, latitude = 1.0), 5_000))
        engine.resume(10_000)
        val after = engine.accept(fix(10_000, latitude = 2.0), 10_000)!!
        assertTrue(after.segment > before.segment)
        assertEquals(0.0, engine.snapshot(10_000)!!.distanceMeters, 0.0)
    }

    @Test
    fun `signal gap starts a new segment while time keeps running`() {
        val engine = started()
        val before = engine.accept(fix(1_000), 1_000)!!
        val after = engine.accept(fix(20_000, latitude = 1.0), 20_000)!!
        assertTrue(after.segment > before.segment)
        assertEquals(19_000, engine.snapshot(20_000)!!.durationMillis)
        assertEquals(0.0, engine.snapshot(20_000)!!.distanceMeters, 0.0)
    }

    @Test
    fun `fresh cached coordinates from pause are not used as a distance anchor`() {
        val engine = started()
        engine.accept(fix(1_000), 1_000)
        engine.freeze(RecordingPhase.Paused, 2_000)
        engine.resume(10_000)
        assertNull(engine.accept(fix(9_000, latitude = 1.0), 10_000))
        val first = engine.accept(fix(11_000, latitude = 1.0001), 11_000)!!
        assertEquals(1, first.segment)
        assertEquals(0.0, engine.snapshot(11_000)!!.distanceMeters, 0.0)
    }

    @Test
    fun `a delayed duplicate after a GPS gap is not recorded again`() {
        val engine = started()
        engine.accept(fix(1_000), 1_000)
        engine.breakSegment()
        assertNull(engine.accept(fix(1_000), 2_000))
        assertEquals(1, engine.snapshot(2_000)!!.nextSequence)
    }

    @Test
    fun `low accuracy stale fixes duplicate timestamps and jumps are rejected`() {
        val engine = started()
        engine.accept(fix(1_000), 1_000)
        assertNull(engine.accept(fix(1_000), 1_000))
        assertNull(engine.accept(fix(2_000, latitude = 1.0), 2_000))
        assertEquals(0.0, engine.snapshot(2_000)!!.distanceMeters, 0.0)
        assertNull(engine.accept(fix(3_000).copy(accuracyMeters = 100f), 3_000))
        assertNull(engine.accept(fix(1_000), 20_000))
    }

    @Test
    fun `small stationary jitter does not accumulate distance`() {
        val engine = started()
        engine.accept(fix(1_000), 1_000)
        assertNull(engine.accept(fix(2_000, latitude = 0.000001), 2_000))
        assertNull(engine.accept(fix(3_000, latitude = -0.000001), 3_000))
        assertEquals(0.0, engine.snapshot(3_000)!!.distanceMeters, 0.0)
    }

    @Test
    fun `explicit GPS loss creates only one gap segment`() {
        val engine = started()
        val before = engine.accept(fix(1_000), 1_000)!!
        engine.breakSegment()
        engine.breakSegment()
        val after = engine.accept(fix(2_000, latitude = 1.0), 2_000)!!
        assertEquals(before.segment + 1, after.segment)
        assertEquals(before.sequence + 1, after.sequence)
        assertEquals(0.0, engine.snapshot(2_000)!!.distanceMeters, 0.0)
    }

    @Test
    fun `stationary GPS fixes preserve continuity when movement resumes`() {
        val engine = started()
        engine.accept(fix(1_000), 1_000)
        for (time in 2_000L..14_000L step 1_000L) {
            assertNull(engine.accept(fix(time), time))
        }
        val next = engine.accept(fix(15_000, latitude = 0.0001), 15_000)!!
        assertEquals(0, next.segment)
        assertEquals(2, engine.snapshot(15_000)!!.nextSequence)
        assertEquals(11.119, engine.snapshot(15_000)!!.distanceMeters, 0.01)
    }

    @Test
    fun `recovery excludes process absence and starts a new segment`() {
        val engine = RecordingEngine()
        engine.restore(RecordingSession(7, WorkoutType.BIKING, 100, RecordingPhase.Recording,
            durationMillis = 4_500, distanceMeters = 500.0, segment = 2, nextSequence = 10))
        assertEquals(RecordingPhase.Interrupted, engine.snapshot(1_000_000)!!.phase)
        assertEquals(4_500, engine.snapshot(1_000_000)!!.durationMillis)
        engine.resume(1_000_000)
        val point = engine.accept(fix(1_000_000, latitude = 10.0), 1_000_000)!!
        assertEquals(3, point.segment)
        assertEquals(10, point.sequence)
        assertEquals(500.0, engine.snapshot(1_002_000)!!.distanceMeters, 0.0)
        assertEquals(6_500, engine.snapshot(1_002_000)!!.durationMillis)
    }

    @Test
    fun `interrupted final save remains stopped after recovery`() {
        val engine = RecordingEngine()
        engine.restore(RecordingSession(1, WorkoutType.RUNNING, 0, RecordingPhase.Saving, durationMillis = 8_000))
        assertEquals(RecordingPhase.SaveFailed, engine.snapshot(100_000)!!.phase)
        assertEquals(8_000, engine.snapshot(100_000)!!.durationMillis)
    }

    @Test
    fun `GPS readiness rejects invalid coordinates future timestamps and missing accuracy`() {
        assertEquals(GpsStatus.Searching, GpsQuality.status(null, 1_000))
        assertEquals(GpsStatus.Lost, GpsQuality.status(fix(2_000), 1_000))
        assertEquals(GpsStatus.Lost, GpsQuality.status(fix(1_000), 12_000))
        assertEquals(GpsStatus.PoorAccuracy, GpsQuality.status(fix(1_000, latitude = Double.NaN), 1_000))
        assertEquals(GpsStatus.PoorAccuracy, GpsQuality.status(fix(1_000).copy(accuracyMeters = 0f), 1_000))
        assertEquals(GpsStatus.Ready, GpsQuality.status(fix(1_000), 1_000))
    }

    private fun started() = RecordingEngine().apply {
        start(RecordingSession(1, WorkoutType.RUNNING, 0, RecordingPhase.Recording), 1_000)
    }

    private fun fix(time: Long, latitude: Double = 0.0) = GpsFix(latitude, 0.0, time, time, 5f)
}
