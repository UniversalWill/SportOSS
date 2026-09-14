package com.universalwill.sportoss.ui.screens.map

import org.junit.Assert.assertEquals
import org.junit.Test

class MapUiStateTest {
    @Test
    fun `toggle starts an idle recording`() {
        val result = MapUiState().reduce(MapAction.ToggleRecording)

        assertEquals(RecordingState.Recording, result.recordingState)
    }

    @Test
    fun `toggle pauses and resumes a recording`() {
        val paused = MapUiState(RecordingState.Recording).reduce(MapAction.ToggleRecording)
        val resumed = paused.reduce(MapAction.ToggleRecording)

        assertEquals(RecordingState.Paused, paused.recordingState)
        assertEquals(RecordingState.Recording, resumed.recordingState)
    }

    @Test
    fun `finish resets the recording`() {
        val recording = MapUiState(
            recordingState = RecordingState.Recording,
            elapsedSeconds = 42,
        )

        assertEquals(MapUiState(), recording.reduce(MapAction.FinishRecording))
    }
}
