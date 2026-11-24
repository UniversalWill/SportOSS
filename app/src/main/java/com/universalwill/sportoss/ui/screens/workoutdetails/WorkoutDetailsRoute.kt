package com.universalwill.sportoss.ui.screens.workoutdetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwill.sportoss.domain.model.MapLabelLanguage

@Composable
internal fun WorkoutDetailsRoute(
    workoutId: Long,
    mapLabelLanguage: MapLabelLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<WorkoutDetailsViewModel, WorkoutDetailsViewModel.Factory>(
        creationCallback = { it.create(workoutId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) {
        if (state == WorkoutDetailsUiState.Deleted) onBack()
    }

    WorkoutDetailsScreen(
        state = state,
        onAction = { action ->
            if (action == WorkoutDetailsAction.Back) onBack() else viewModel.onAction(action)
        },
        modifier = modifier,
        mapLabelLanguage = mapLabelLanguage,
    )
}
