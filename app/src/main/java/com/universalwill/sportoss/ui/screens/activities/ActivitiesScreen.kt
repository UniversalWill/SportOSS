package com.universalwill.sportoss.ui.screens.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.R

@Composable
fun ActivitiesScreen(
    state: ActivitiesUiState,
    onAction: (ActivitiesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.activities_title),
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        when {
            state.isLoading -> LoadingContent(modifier = Modifier.weight(1f))
            state.hasLoadError -> ErrorContent(
                onRetry = { onAction(ActivitiesAction.Retry) },
                modifier = Modifier.weight(1f),
            )
            state.workouts.isEmpty() -> EmptyContent(
                onStartActivity = { onAction(ActivitiesAction.StartActivity) },
                modifier = Modifier.weight(1f),
            )
            else -> ActivitiesList(
                workouts = state.workouts,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
