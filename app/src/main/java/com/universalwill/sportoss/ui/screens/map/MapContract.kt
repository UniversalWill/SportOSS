package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.RecordingSnapshot

typealias RecordingState = RecordingPhase

data class MapUiState(
    val workoutType: WorkoutType = WorkoutType.RUNNING,
    val mapLabelLanguage: MapLabelLanguage = MapLabelLanguage.APPLICATION,
    val recording: RecordingSnapshot = RecordingSnapshot(),
) {
    val recordingState get() = recording.phase
    val elapsedSeconds get() = (recording.session?.durationMillis ?: 0) / 1_000
    val distanceMeters get() = recording.session?.distanceMeters ?: 0.0
    val canToggle get() = !recording.isLoading && when (recordingState) {
        RecordingState.Recording -> true
        RecordingState.Idle, RecordingState.Paused, RecordingState.Interrupted -> recording.gps == GpsStatus.Ready
        else -> false
    }
}

sealed interface MapAction {
    data class SelectWorkoutType(val workoutType: WorkoutType) : MapAction
    data class UiVisible(val visible: Boolean) : MapAction
    data object ToggleRecording : MapAction
    data object FinishRecording : MapAction
    data object RetryLoad : MapAction
    data object RequestLocationPermission : MapAction
    data object OpenLocationSettings : MapAction
    data object OpenAppSettings : MapAction
}
