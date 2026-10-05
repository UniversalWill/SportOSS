package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.data.repository.FakeUserPreferencesRepository
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingControl
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.RecordingSession
import com.universalwill.sportoss.domain.recording.RecordingSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `route observes shared recording and cannot change an active workout type`() = runTest {
        val control = FakeControl()
        val viewModel = MapViewModel(control, FakeUserPreferencesRepository())
        runCurrent()
        control.state.value = RecordingSnapshot(
            session = RecordingSession(7, WorkoutType.BIKING, 100, RecordingPhase.Recording,
                durationMillis = 42_000, distanceMeters = 500.0),
            phase = RecordingPhase.Recording, isLoading = false,
        )
        runCurrent()
        viewModel.onAction(MapAction.SelectWorkoutType(WorkoutType.RUNNING))
        runCurrent()
        assertEquals(WorkoutType.BIKING, viewModel.uiState.value.workoutType)
        assertEquals(42, viewModel.uiState.value.elapsedSeconds)
        assertEquals(500.0, viewModel.uiState.value.distanceMeters, 0.0)
    }

    @Test
    fun `idle type selection is sent to recording owner`() = runTest {
        val control = FakeControl()
        val viewModel = MapViewModel(control, FakeUserPreferencesRepository())
        runCurrent()
        viewModel.onAction(MapAction.SelectWorkoutType(WorkoutType.BIKING))
        runCurrent()
        viewModel.onAction(MapAction.ToggleRecording)
        assertEquals(WorkoutType.BIKING, control.requestedType)
        viewModel.onAction(MapAction.FinishRecording)
        assertEquals(1, control.finishes)
    }

    @Test
    fun `map language follows preferences`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val viewModel = MapViewModel(FakeControl(), preferences)
        preferences.setMapLabelLanguage(MapLabelLanguage.ENGLISH)
        runCurrent()
        assertEquals(MapLabelLanguage.ENGLISH, viewModel.uiState.value.mapLabelLanguage)
    }

    @Test
    fun `navigation type request is retained before initial UI collection`() = runTest {
        val viewModel = MapViewModel(FakeControl(), FakeUserPreferencesRepository())
        viewModel.onAction(MapAction.SelectWorkoutType(WorkoutType.BIKING))
        runCurrent()
        assertEquals(WorkoutType.BIKING, viewModel.uiState.value.workoutType)
    }

    @Test
    fun `hiding route updates GPS observation without finishing the session`() = runTest {
        val control = FakeControl()
        val viewModel = MapViewModel(control, FakeUserPreferencesRepository())
        viewModel.onAction(MapAction.UiVisible(false))
        assertEquals(false, control.visible)
        assertEquals(0, control.finishes)
    }

    private class FakeControl : RecordingControl {
        override val state = MutableStateFlow(RecordingSnapshot(gps = GpsStatus.Ready, isLoading = false))
        var requestedType: WorkoutType? = null
        var finishes = 0
        var visible = true
        override fun toggle(type: WorkoutType) { requestedType = type }
        override fun finish() { finishes++ }
        override fun setUiVisible(visible: Boolean) { this.visible = visible }
        override fun retryLoad() = Unit
    }
}
