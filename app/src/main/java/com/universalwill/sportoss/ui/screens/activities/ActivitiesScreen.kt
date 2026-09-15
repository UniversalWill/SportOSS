package com.universalwill.sportoss.ui.screens.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.universalwill.sportoss.R
import com.universalwill.sportoss.ui.components.WorkoutTypePicker
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
fun ActivitiesScreen(
    state: ActivitiesUiState,
    onAction: (ActivitiesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isWorkoutTypePickerVisible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(
                start = MaterialTheme.dimensions.spacingExtraLarge,
                top = MaterialTheme.dimensions.spacingExtraLarge,
                end = MaterialTheme.dimensions.spacingExtraLarge,
                bottom = MaterialTheme.dimensions.spacingMedium,
            ),
        ) {
            Text(
                text = stringResource(R.string.activities_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            if (!state.isLoading && !state.hasLoadError && state.workouts.isNotEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalButton(onClick = { isWorkoutTypePickerVisible = true }) {
                    Text(stringResource(R.string.start_workout_short))
                }
            }
        }

        when {
            state.isLoading -> LoadingContent(modifier = Modifier.weight(1f))
            state.hasLoadError -> ErrorContent(
                onRetry = { onAction(ActivitiesAction.Retry) },
                modifier = Modifier.weight(1f),
            )
            state.workouts.isEmpty() -> EmptyContent(
                onStartActivity = { isWorkoutTypePickerVisible = true },
                modifier = Modifier.weight(1f),
            )
            else -> ActivitiesList(
                workouts = state.workouts,
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (isWorkoutTypePickerVisible) {
        WorkoutTypePicker(
            selectedWorkoutType = null,
            onWorkoutTypeSelected = { workoutType ->
                isWorkoutTypePickerVisible = false
                onAction(ActivitiesAction.StartActivity(workoutType))
            },
            onDismissRequest = { isWorkoutTypePickerVisible = false },
        )
    }
}
