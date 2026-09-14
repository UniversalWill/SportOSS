package com.universalwill.sportoss.ui.screens.activities

import com.universalwill.sportoss.domain.model.Workout

data class ActivitiesUiState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
)

sealed interface ActivitiesAction {
    data object StartActivity : ActivitiesAction
    data object Retry : ActivitiesAction
}
