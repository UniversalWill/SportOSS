package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.data.repository.OfflineWorkoutRepository
import com.universalwill.sportoss.domain.model.Workout
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `toggle starts recording`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel(FakeWorkoutRepository())

        viewModel.onAction(MapAction.ToggleRecording)

        assertEquals(RecordingState.Recording, viewModel.uiState.value.recordingState)
        assertEquals(0, viewModel.uiState.value.elapsedSeconds)
        assertNotNull(viewModel.uiState.value.startedAtEpochMillis)
        viewModel.onAction(MapAction.FinishRecording)
        runCurrent()
    }

    @Test
    fun `recording timer advances once per second`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel(FakeWorkoutRepository())
        viewModel.onAction(MapAction.ToggleRecording)

        advanceTimeBy(3_000)
        runCurrent()

        assertEquals(3, viewModel.uiState.value.elapsedSeconds)
        viewModel.onAction(MapAction.FinishRecording)
        runCurrent()
    }

    @Test
    fun `pause stops the recording timer`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel(FakeWorkoutRepository())
        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(2_000)
        runCurrent()

        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(RecordingState.Paused, viewModel.uiState.value.recordingState)
        assertEquals(2, viewModel.uiState.value.elapsedSeconds)
        viewModel.onAction(MapAction.FinishRecording)
        runCurrent()
    }

    @Test
    fun `resume continues from elapsed time`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = MapViewModel(FakeWorkoutRepository())
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
        runCurrent()
    }

    @Test
    fun `finish resets state and cancels timer`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeWorkoutRepository()
        val viewModel = MapViewModel(repository)
        viewModel.onAction(MapAction.ToggleRecording)
        advanceTimeBy(2_000)
        runCurrent()

        viewModel.onAction(MapAction.FinishRecording)
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(1, repository.savedWorkouts.size)
        assertEquals(2, repository.savedWorkouts.single().durationSeconds)
        assertEquals(MapUiState(), viewModel.uiState.value)
    }

    @Test
    fun `zero duration workout is saved`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeWorkoutRepository()
        val viewModel = MapViewModel(repository)
        viewModel.onAction(MapAction.ToggleRecording)

        viewModel.onAction(MapAction.FinishRecording)
        runCurrent()

        assertEquals(0, repository.savedWorkouts.single().durationSeconds)
        assertEquals(MapUiState(), viewModel.uiState.value)
    }

    @Test
    fun `repeated finish while saving does not create duplicates`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val saveGate = CompletableDeferred<Unit>()
            val repository = FakeWorkoutRepository(saveGate = saveGate)
            val viewModel = MapViewModel(repository)
            viewModel.onAction(MapAction.ToggleRecording)

            viewModel.onAction(MapAction.FinishRecording)
            runCurrent()
            viewModel.onAction(MapAction.FinishRecording)
            viewModel.onAction(MapAction.ToggleRecording)

            assertEquals(1, repository.saveCalls)
            assertTrue(viewModel.uiState.value.isSaving)
            assertEquals(RecordingState.Recording, viewModel.uiState.value.recordingState)

            saveGate.complete(Unit)
            runCurrent()

            assertEquals(MapUiState(), viewModel.uiState.value)
        }

    @Test
    fun `save failure keeps recording and restarts timer`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeWorkoutRepository(saveFailure = IllegalStateException("failed"))
            val viewModel = MapViewModel(repository)
            viewModel.onAction(MapAction.ToggleRecording)
            advanceTimeBy(2_000)
            runCurrent()

            viewModel.onAction(MapAction.FinishRecording)
            runCurrent()

            assertEquals(RecordingState.Recording, viewModel.uiState.value.recordingState)
            assertFalse(viewModel.uiState.value.isSaving)
            assertTrue(viewModel.uiState.value.hasSaveError)

            advanceTimeBy(1_000)
            runCurrent()

            assertEquals(3, viewModel.uiState.value.elapsedSeconds)
            viewModel.onAction(MapAction.ToggleRecording)
        }

    private class FakeWorkoutRepository(
        private val saveGate: CompletableDeferred<Unit>? = null,
        private val saveFailure: Throwable? = null,
    ) : OfflineWorkoutRepository {
        val savedWorkouts = mutableListOf<Workout>()
        var saveCalls = 0
            private set

        override fun observeWorkouts(): Flow<List<Workout>> = flowOf(savedWorkouts)

        override suspend fun saveWorkout(workout: Workout) {
            saveCalls += 1
            savedWorkouts += workout
            saveGate?.await()
            saveFailure?.let { throw it }
        }
    }
}
