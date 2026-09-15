package com.universalwill.sportoss.ui.screens.activities

import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.Workout

data class ActivitiesUiState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
)

sealed interface ActivitiesAction {
    data class StartActivity(val workoutType: WorkoutType) : ActivitiesAction
    data object Retry : ActivitiesAction
}
