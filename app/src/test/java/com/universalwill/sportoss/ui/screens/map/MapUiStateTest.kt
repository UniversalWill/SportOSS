package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.domain.enums.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Test

class MapUiStateTest {
    @Test
    fun `workout type can be selected while idle`() {
        val result = MapUiState().reduce(MapAction.SelectWorkoutType(WorkoutType.BIKING))

        assertEquals(WorkoutType.BIKING, result.workoutType)
    }

    @Test
    fun `workout type cannot change after recording starts`() {
        val recording = MapUiState(
            workoutType = WorkoutType.RUNNING,
            recordingState = RecordingState.Recording,
        )

        val result = recording.reduce(MapAction.SelectWorkoutType(WorkoutType.BIKING))

        assertEquals(WorkoutType.RUNNING, result.workoutType)
    }

    @Test
    fun `toggle starts an idle recording`() {
        val result = MapUiState().reduce(MapAction.ToggleRecording)

        assertEquals(RecordingState.Recording, result.recordingState)
    }

    @Test
    fun `toggle pauses and resumes a recording`() {
        val paused = MapUiState(
            recordingState = RecordingState.Recording,
            elapsedSeconds = 42,
        ).reduce(MapAction.ToggleRecording)
        val resumed = paused.reduce(MapAction.ToggleRecording)

        assertEquals(RecordingState.Paused, paused.recordingState)
        assertEquals(42, paused.elapsedSeconds)
        assertEquals(RecordingState.Recording, resumed.recordingState)
        assertEquals(42, resumed.elapsedSeconds)
    }

    @Test
    fun `finish resets the recording`() {
        val recording = MapUiState(
            workoutType = WorkoutType.BIKING,
            recordingState = RecordingState.Recording,
            elapsedSeconds = 42,
        )

        assertEquals(
            MapUiState(workoutType = WorkoutType.BIKING),
            recording.reduce(MapAction.FinishRecording),
        )
    }
}
