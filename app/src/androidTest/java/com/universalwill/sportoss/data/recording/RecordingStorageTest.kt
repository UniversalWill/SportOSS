package com.universalwill.sportoss.data.recording

import android.content.Context
import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.universalwill.sportoss.data.local.RECORDING_MIGRATION_1_2
import com.universalwill.sportoss.data.local.SportOSSDatabase
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.GpsFix
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.TrackPoint
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordingStorageTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test
    fun migrationPreservesExistingWorkoutsAndCreatesRecordingTables() = runBlocking {
        val name = "recording-migration-${UUID.randomUUID()}.db"
        val schema = instrumentation.context.assets.open(
            "com.universalwill.sportoss.data.local.SportOSSDatabase/1.json",
        ).bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { legacy ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                legacy.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) legacy.execSQL(setup.getString(index))
            legacy.execSQL("INSERT INTO workouts VALUES (7, 'RUNNING', 123456, 42, 500.0)")
            legacy.version = 1
        }
        val database = Room.databaseBuilder(context, SportOSSDatabase::class.java, name)
            .addMigrations(RECORDING_MIGRATION_1_2).build()
        try {
            val workouts = database.workoutDao().getAllWorkouts().first()
            assertEquals(1, workouts.size)
            assertEquals(7, workouts.single().id)
            assertEquals(42, workouts.single().durationSeconds)
            assertEquals(500.0, workouts.single().distanceMeters, 0.0)
            assertTrue(workouts.single().isCompleted)
            assertNull(RoomRecordingStore(database).loadSession())
            assertTrue(database.recordingDao().getTrack(7).isEmpty())
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun draftIsHiddenFromHistoryAndCompletionPreservesOrderedTrackWithoutDuplicates() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, SportOSSDatabase::class.java).build()
        try {
            val store = RoomRecordingStore(database)
            val session = store.createSession(WorkoutType.BIKING, 123)
            assertTrue(database.workoutDao().getAllWorkouts().first().isEmpty())
            val snapshot = session.copy(durationMillis = 5_500, distanceMeters = 25.0,
                segment = 1, nextSequence = 2)
            val points = listOf(point(session.workoutId, 1, 1), point(session.workoutId, 0, 0))
            store.checkpoint(snapshot, points)
            assertEquals(snapshot, store.loadSession())
            store.finish(snapshot.copy(phase = RecordingPhase.Saving), emptyList())
            store.finish(snapshot, emptyList())
            assertNull(store.loadSession())
            val workouts = database.workoutDao().getAllWorkouts().first()
            assertEquals(1, workouts.size)
            assertEquals(5, workouts.single().durationSeconds)
            assertEquals(25.0, workouts.single().distanceMeters, 0.0)
            val track = database.recordingDao().getTrack(session.workoutId)
            assertEquals(listOf(0L, 1L), track.map { it.sequence })
            assertEquals(listOf(0, 1), track.map { it.segment })
        } finally {
            database.close()
        }
    }

    @Test
    fun failedPointInsertRollsBackSessionAndPointsTogether() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, SportOSSDatabase::class.java).build()
        try {
            val store = RoomRecordingStore(database)
            val session = store.createSession(WorkoutType.RUNNING, 0).copy(nextSequence = 1)
            val first = point(session.workoutId, 0, 0)
            store.checkpoint(session, listOf(first))
            val result = runCatching {
                store.checkpoint(session.copy(nextSequence = 2, distanceMeters = 100.0),
                    listOf(point(session.workoutId, 1, 0), first))
            }
            assertTrue(result.isFailure)
            assertEquals(session, store.loadSession())
            assertEquals(1, database.recordingDao().getTrack(session.workoutId).size)
        } finally {
            database.close()
        }
    }

    @Test
    fun unfinishedSessionSurvivesDatabaseReopenAndSecondStartIsRejected() = runBlocking {
        val name = "recording-recovery-${UUID.randomUUID()}.db"
        var database = Room.databaseBuilder(context, SportOSSDatabase::class.java, name).build()
        try {
            val store = RoomRecordingStore(database)
            val snapshot = store.createSession(WorkoutType.RUNNING, 123).copy(
                durationMillis = 12_345, distanceMeters = 22.0, nextSequence = 1)
            store.checkpoint(snapshot, listOf(point(snapshot.workoutId, 0, 0)))
            assertTrue(runCatching { store.createSession(WorkoutType.BIKING, 456) }.isFailure)
            database.close()
            database = Room.databaseBuilder(context, SportOSSDatabase::class.java, name).build()
            assertEquals(snapshot, RoomRecordingStore(database).loadSession())
            assertNotNull(database.recordingDao().getTrack(snapshot.workoutId).single())
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    private fun point(workoutId: Long, sequence: Long, segment: Int) = TrackPoint(
        workoutId, sequence, segment, GpsFix(0.0, sequence * 0.0001, sequence * 2_000,
            sequence * 2_000, 5f),
    )
}
