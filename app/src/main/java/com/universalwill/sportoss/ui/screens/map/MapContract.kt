package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.MapLabelLanguage

data class MapUiState(
    val workoutType: WorkoutType = WorkoutType.RUNNING,
    val mapLabelLanguage: MapLabelLanguage = MapLabelLanguage.APPLICATION,
    val recordingState: RecordingState = RecordingState.Idle,
    val elapsedSeconds: Long = 0,
    val startedAtEpochMillis: Long? = null,
    val isSaving: Boolean = false,
    val hasSaveError: Boolean = false,
)

enum class RecordingState {
    Idle,
    Recording,
    Paused,
}

sealed interface MapAction {
    data class SelectWorkoutType(val workoutType: WorkoutType) : MapAction
    data object ToggleRecording : MapAction
    data object FinishRecording : MapAction
    data object SaveErrorShown : MapAction
}

internal fun MapUiState.reduce(action: MapAction): MapUiState = when (action) {
    is MapAction.SelectWorkoutType -> if (recordingState == RecordingState.Idle && !isSaving) {
        copy(workoutType = action.workoutType)
    } else {
        this
    }
    MapAction.ToggleRecording -> copy(
        recordingState = recordingState.next(),
        hasSaveError = false,
    )
    MapAction.FinishRecording -> MapUiState(
        workoutType = workoutType,
        mapLabelLanguage = mapLabelLanguage,
    )
    MapAction.SaveErrorShown -> copy(hasSaveError = false)
}

private fun RecordingState.next(): RecordingState = when (this) {
    RecordingState.Idle -> RecordingState.Recording
    RecordingState.Recording -> RecordingState.Paused
    RecordingState.Paused -> RecordingState.Recording
}
