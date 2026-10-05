package com.universalwill.sportoss.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val RECORDING_MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE workouts ADD COLUMN is_completed INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS recording_session (
                id INTEGER NOT NULL PRIMARY KEY, workoutId INTEGER NOT NULL,
                workoutType TEXT NOT NULL, startedAtEpochMillis INTEGER NOT NULL,
                phase TEXT NOT NULL, durationMillis INTEGER NOT NULL,
                distanceMeters REAL NOT NULL, segment INTEGER NOT NULL, nextSequence INTEGER NOT NULL,
                FOREIGN KEY(workoutId) REFERENCES workouts(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recording_session_workoutId ON recording_session(workoutId)")
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS track_points (
                workoutId INTEGER NOT NULL, sequence INTEGER NOT NULL, segment INTEGER NOT NULL,
                latitude REAL NOT NULL, longitude REAL NOT NULL, epochMillis INTEGER NOT NULL,
                elapsedRealtimeMillis INTEGER NOT NULL, accuracyMeters REAL NOT NULL,
                PRIMARY KEY(workoutId, sequence),
                FOREIGN KEY(workoutId) REFERENCES workouts(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
    }
}
