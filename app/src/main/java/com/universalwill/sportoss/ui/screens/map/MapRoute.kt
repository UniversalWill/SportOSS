package com.universalwill.sportoss.ui.screens.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwill.sportoss.domain.enums.WorkoutType

@Composable
fun MapRoute(
    requestedWorkoutType: WorkoutType?,
    onWorkoutTypeRequestHandled: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(requestedWorkoutType) {
        requestedWorkoutType?.let { workoutType ->
            viewModel.onAction(MapAction.SelectWorkoutType(workoutType))
            onWorkoutTypeRequestHandled()
        }
    }

    MapScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
