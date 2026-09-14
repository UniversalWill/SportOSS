package com.universalwill.sportoss.ui.screens.map

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `toggle starts recording`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel()

        viewModel.onAction(MapAction.ToggleRecording)

        assertEquals(RecordingState.Recording, viewModel.uiState.value.recordingState)
        assertEquals(0, viewModel.uiState.value.elapsedSeconds)
        viewModel.onAction(MapAction.FinishRecording)
    }

    @Test
    fun `recording timer advances once per second`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel()
        viewModel.onAction(MapAction.ToggleRecording)

        advanceTimeBy(3_000)
        runCurrent()

        assertEquals(3, viewModel.uiState.value.elapsedSeconds)
        viewModel.onAction(MapAction.FinishRecording)
    }

    @Test
    fun `pause stops the recording timer`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel()
        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(2_000)
        runCurrent()

        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(RecordingState.Paused, viewModel.uiState.value.recordingState)
        assertEquals(2, viewModel.uiState.value.elapsedSeconds)
        viewModel.onAction(MapAction.FinishRecording)
    }

    @Test
    fun `resume continues from elapsed time`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel()
        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(2_000)
        runCurrent()
        viewModel.onAction(MapAction.ToggleRecording)

        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(RecordingState.Recording, viewModel.uiState.value.recordingState)
        assertEquals(3, viewModel.uiState.value.elapsedSeconds)
        viewModel.onAction(MapAction.FinishRecording)
    }

    @Test
    fun `finish resets state and cancels timer`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel()
        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(2_000)
        runCurrent()

        viewModel.onAction(MapAction.FinishRecording)
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(MapUiState(), viewModel.uiState.value)
    }
}
