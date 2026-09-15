package com.universalwill.sportoss.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwill.sportoss.data.repository.OfflineWorkoutRepository
import com.universalwill.sportoss.data.repository.UserPreferencesRepository
import com.universalwill.sportoss.domain.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val workoutRepository: OfflineWorkoutRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(MapUiState())
    val uiState = mutableUiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeMapPreferences()
    }

    fun onAction(action: MapAction) {
        when (action) {
            is MapAction.SelectWorkoutType -> selectWorkoutType(action)
            MapAction.ToggleRecording -> toggleRecording()
            MapAction.FinishRecording -> finishRecording()
            MapAction.SaveErrorShown -> dismissSaveError()
        }
    }

    private fun selectWorkoutType(action: MapAction.SelectWorkoutType) {
        mutableUiState.update { it.reduce(action) }
    }

    private fun observeMapPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                mutableUiState.update {
                    it.copy(mapLabelLanguage = preferences.mapLabelLanguage)
                }
            }
        }
    }

    private fun toggleRecording() {
        if (mutableUiState.value.isSaving) return

        val startedAtEpochMillis = if (
            mutableUiState.value.recordingState == RecordingState.Idle
        ) {
            System.currentTimeMillis()
        } else {
            null
        }

        mutableUiState.update { currentState ->
            val nextState = currentState.reduce(MapAction.ToggleRecording)

            if (startedAtEpochMillis != null) {
                nextState.copy(startedAtEpochMillis = startedAtEpochMillis)
            } else {
                nextState
            }
        }

        updateTimer()
    }

    private fun finishRecording() {
        val snapshot = mutableUiState.value

        if (snapshot.recordingState == RecordingState.Idle || snapshot.isSaving) return
        val startedAtEpochMillis = snapshot.startedAtEpochMillis ?: return

        stopTimer()
        mutableUiState.update {
            it.copy(
                isSaving = true,
                hasSaveError = false,
            )
        }

        val workout = Workout(
            id = 0,
            type = snapshot.workoutType,
            startedAtEpochMillis = startedAtEpochMillis,
            durationSeconds = snapshot.elapsedSeconds,
            distanceMeters = 0.0,
        )

        viewModelScope.launch {
            try {
                workoutRepository.saveWorkout(workout)
                mutableUiState.update { currentState ->
                    currentState.reduce(MapAction.FinishRecording)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update {
                    it.copy(
                        isSaving = false,
                        hasSaveError = true,
                    )
                }
                if (snapshot.recordingState == RecordingState.Recording) {
                    startTimer()
                }
            }
        }
    }

    private fun dismissSaveError() {
        mutableUiState.update { it.reduce(MapAction.SaveErrorShown) }
    }

    private fun updateTimer() {
        if (mutableUiState.value.recordingState == RecordingState.Recording) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1.seconds)
                mutableUiState.update { state ->
                    if (state.recordingState == RecordingState.Recording) {
                        state.copy(elapsedSeconds = state.elapsedSeconds + 1)
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        stopTimer()
    }
}
