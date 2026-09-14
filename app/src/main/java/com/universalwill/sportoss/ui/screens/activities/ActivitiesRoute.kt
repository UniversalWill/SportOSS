package com.universalwill.sportoss.ui.screens.activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ActivitiesRoute(
    onStartActivity: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ActivitiesScreen(
        state = state,
        onAction = { action ->
            when (action) {
                ActivitiesAction.StartActivity -> onStartActivity()
                ActivitiesAction.Retry -> viewModel.retry()
            }
        },
        modifier = modifier,
    )
}
