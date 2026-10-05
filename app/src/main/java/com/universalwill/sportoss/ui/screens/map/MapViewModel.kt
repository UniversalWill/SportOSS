package com.universalwill.sportoss.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwill.sportoss.data.repository.UserPreferencesRepository
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.RecordingControl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MapViewModel @Inject constructor(
    private val recordingControl: RecordingControl,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val selectedWorkoutType = MutableStateFlow(WorkoutType.RUNNING)
    val uiState = combine(recordingControl.state, userPreferencesRepository.userPreferences, selectedWorkoutType) {
        recording, preferences, selected ->
        MapUiState(
            workoutType = recording.session?.workoutType ?: selected,
            mapLabelLanguage = preferences.mapLabelLanguage,
            recording = recording,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MapUiState())

    fun onAction(action: MapAction) {
        when (action) {
            is MapAction.SelectWorkoutType -> if (recordingControl.state.value.phase == RecordingState.Idle &&
                recordingControl.state.value.session == null) selectedWorkoutType.value = action.workoutType
            is MapAction.UiVisible -> recordingControl.setUiVisible(action.visible)
            MapAction.ToggleRecording -> recordingControl.toggle(uiState.value.workoutType)
            MapAction.FinishRecording -> {
                selectedWorkoutType.value = uiState.value.workoutType
                recordingControl.finish()
            }
            MapAction.RetryLoad -> recordingControl.retryLoad()
            MapAction.RequestLocationPermission, MapAction.OpenLocationSettings, MapAction.OpenAppSettings -> Unit
        }
    }
}
