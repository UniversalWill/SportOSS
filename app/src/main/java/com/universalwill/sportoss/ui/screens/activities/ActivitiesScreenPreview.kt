package com.universalwill.sportoss.ui.screens.activities

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.Workout
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Preview(showBackground = true)
@Composable
private fun ActivitiesContentPreview() {
    SportOSSTheme {
        ActivitiesScreen(
            state = ActivitiesUiState(
                workouts = listOf(
                    Workout(
                        id = 1,
                        type = WorkoutType.RUNNING,
                        startedAtEpochMillis = 1_757_840_400_000,
                        durationSeconds = 2_538,
                        distanceMeters = 7_350.0,
                    ),
                    Workout(
                        id = 2,
                        type = WorkoutType.BIKING,
                        startedAtEpochMillis = 1_757_667_600_000,
                        durationSeconds = 4_684,
                        distanceMeters = 24_600.0,
                    ),
                ),
                isLoading = false,
            ),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivitiesEmptyPreview() {
    SportOSSTheme {
        ActivitiesScreen(
            state = ActivitiesUiState(isLoading = false),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
