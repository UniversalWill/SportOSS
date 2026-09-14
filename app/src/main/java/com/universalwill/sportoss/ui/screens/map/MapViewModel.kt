package com.universalwill.sportoss.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
class MapViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(MapUiState())
    val uiState = mutableUiState.asStateFlow()

    private var timerJob: Job? = null

    fun onAction(action: MapAction) {
        mutableUiState.update { it.reduce(action) }
        updateTimer()
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
        super.onCleared()
    }
}
