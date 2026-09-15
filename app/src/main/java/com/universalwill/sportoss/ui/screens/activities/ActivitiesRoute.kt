package com.universalwill.sportoss.ui.screens.activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwill.sportoss.domain.enums.WorkoutType

@Composable
fun ActivitiesRoute(
    onStartActivity: (WorkoutType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ActivitiesScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is ActivitiesAction.StartActivity -> onStartActivity(action.workoutType)
                ActivitiesAction.Retry -> viewModel.retry()
            }
        },
        modifier = modifier,
    )
}
