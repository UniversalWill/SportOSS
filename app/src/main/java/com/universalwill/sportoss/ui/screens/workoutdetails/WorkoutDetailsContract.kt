package com.universalwill.sportoss.ui.screens.workoutdetails

import com.universalwill.sportoss.domain.model.WorkoutDetails

internal sealed interface WorkoutDetailsUiState {
    data object Loading : WorkoutDetailsUiState
    data object LoadFailed : WorkoutDetailsUiState
    data object NotFound : WorkoutDetailsUiState
    data object Deleted : WorkoutDetailsUiState
    data class Content(
        val details: WorkoutDetails,
        val deletion: WorkoutDeletion = WorkoutDeletion.None,
    ) : WorkoutDetailsUiState
}

internal enum class WorkoutDeletion {
    None,
    Confirming,
    Deleting,
    Failed,
}

internal sealed interface WorkoutDetailsAction {
    data object Back : WorkoutDetailsAction
    data object RetryLoad : WorkoutDetailsAction
    data object RequestDelete : WorkoutDetailsAction
    data object CancelDelete : WorkoutDetailsAction
    data object ConfirmDelete : WorkoutDetailsAction
}
