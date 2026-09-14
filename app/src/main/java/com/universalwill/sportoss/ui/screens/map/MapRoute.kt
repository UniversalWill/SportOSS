package com.universalwill.sportoss.ui.screens.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MapRoute(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MapScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
