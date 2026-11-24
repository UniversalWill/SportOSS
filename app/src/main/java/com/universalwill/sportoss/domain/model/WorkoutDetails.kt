package com.universalwill.sportoss.domain.model

data class WorkoutDetails(
    val workout: Workout,
    val routeSegments: List<List<WorkoutRoutePoint>>,
) {
    val averageSpeedKilometersPerHour: Double?
        get() = workout.distanceMeters
            .takeIf { it.isFinite() && it > 0 && workout.durationSeconds > 0 }
            ?.let { it / workout.durationSeconds * 3.6 }
}

data class WorkoutRoutePoint(val latitude: Double, val longitude: Double)
