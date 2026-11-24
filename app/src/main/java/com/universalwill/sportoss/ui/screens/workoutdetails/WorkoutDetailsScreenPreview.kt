package com.universalwill.sportoss.ui.screens.workoutdetails

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.Workout
import com.universalwill.sportoss.domain.model.WorkoutDetails
import com.universalwill.sportoss.domain.model.WorkoutRoutePoint
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Preview(showBackground = true, widthDp = 393, heightDp = 720, locale = "ru")
@Composable
private fun WorkoutWithRoutePreview() {
    WorkoutDetailsPreview(WorkoutDetailsUiState.Content(PreviewWorkout))
}

@Preview(showBackground = true, widthDp = 393, heightDp = 720, locale = "en")
@Composable
private fun WorkoutWithRouteDarkPreview() {
    WorkoutDetailsPreview(WorkoutDetailsUiState.Content(PreviewWorkout), darkTheme = true)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 620, locale = "ru")
@Composable
private fun WorkoutWithoutRoutePreview() {
    val workout = PreviewWorkout.workout.copy(type = WorkoutType.RUNNING, distanceMeters = 0.0)
    WorkoutDetailsPreview(WorkoutDetailsUiState.Content(WorkoutDetails(workout, emptyList())))
}

@Preview(showBackground = true, widthDp = 393, heightDp = 720, locale = "ru")
@Composable
private fun DeleteWorkoutConfirmationPreview() {
    WorkoutDetailsPreview(WorkoutDetailsUiState.Content(PreviewWorkout, WorkoutDeletion.Confirming))
}

@Preview(showBackground = true, widthDp = 393, heightDp = 720, locale = "ru")
@Composable
private fun DeleteWorkoutFailurePreview() {
    WorkoutDetailsPreview(WorkoutDetailsUiState.Content(PreviewWorkout, WorkoutDeletion.Failed))
}

@Composable
private fun WorkoutDetailsPreview(state: WorkoutDetailsUiState, darkTheme: Boolean = false) {
    SportOSSTheme(darkTheme = darkTheme, dynamicColor = false) {
        Surface {
            WorkoutDetailsScreen(state = state, onAction = {})
        }
    }
}

private val PreviewWorkout = WorkoutDetails(
    workout = Workout(
        id = 1,
        type = WorkoutType.BIKING,
        startedAtEpochMillis = 1_757_840_400_000,
        durationSeconds = 2_718,
        distanceMeters = 12_850.0,
    ),
    routeSegments = listOf(
        listOf(
            WorkoutRoutePoint(44.831, 65.491), WorkoutRoutePoint(44.834, 65.493),
            WorkoutRoutePoint(44.836, 65.498), WorkoutRoutePoint(44.837, 65.503),
            WorkoutRoutePoint(44.841, 65.506), WorkoutRoutePoint(44.843, 65.513),
        ),
        listOf(
            WorkoutRoutePoint(44.842, 65.517), WorkoutRoutePoint(44.838, 65.520),
            WorkoutRoutePoint(44.832, 65.516), WorkoutRoutePoint(44.831, 65.511),
            WorkoutRoutePoint(44.827, 65.506), WorkoutRoutePoint(44.828, 65.500),
            WorkoutRoutePoint(44.830, 65.497),
        ),
    ),
)
