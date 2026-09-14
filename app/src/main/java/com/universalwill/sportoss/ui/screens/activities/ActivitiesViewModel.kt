package com.universalwill.sportoss.ui.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwill.sportoss.data.repository.OfflineWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val workoutRepository: OfflineWorkoutRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ActivitiesUiState())
    val uiState = mutableUiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeWorkouts()
    }

    fun retry() {
        observeWorkouts()
    }

    private fun observeWorkouts() {
        observationJob?.cancel()
        mutableUiState.update {
            it.copy(
                isLoading = true,
                hasLoadError = false,
            )
        }
        observationJob = viewModelScope.launch {
            workoutRepository.observeWorkouts()
                .catch {
                    mutableUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            hasLoadError = true,
                        )
                    }
                }
                .collect { workouts ->
                    mutableUiState.value = ActivitiesUiState(
                        workouts = workouts,
                        isLoading = false,
                    )
                }
        }
    }
}
