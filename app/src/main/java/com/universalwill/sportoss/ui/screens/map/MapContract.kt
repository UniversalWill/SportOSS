package com.universalwill.sportoss.ui.screens.map

data class MapUiState(
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
    data object ToggleRecording : MapAction
    data object FinishRecording : MapAction
    data object SaveErrorShown : MapAction
}

internal fun MapUiState.reduce(action: MapAction): MapUiState = when (action) {
    MapAction.ToggleRecording -> copy(
        recordingState = recordingState.next(),
        hasSaveError = false,
    )
    MapAction.FinishRecording -> MapUiState()
    MapAction.SaveErrorShown -> copy(hasSaveError = false)
}

private fun RecordingState.next(): RecordingState = when (this) {
    RecordingState.Idle -> RecordingState.Recording
    RecordingState.Recording -> RecordingState.Paused
    RecordingState.Paused -> RecordingState.Recording
}
