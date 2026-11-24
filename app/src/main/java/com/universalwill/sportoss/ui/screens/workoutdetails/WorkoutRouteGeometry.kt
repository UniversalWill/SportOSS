package com.universalwill.sportoss.ui.screens.workoutdetails

import com.universalwill.sportoss.domain.model.WorkoutRoutePoint
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sinh
import kotlin.math.tan

internal const val MAX_MERCATOR_LATITUDE = 85.05112878

internal data class ProjectedRoutePoint(val x: Double, val y: Double)

internal class WorkoutRouteGeometry(route: List<List<WorkoutRoutePoint>>) {
    val segments: List<List<ProjectedRoutePoint>>
    val crossesAntimeridian: Boolean
    val center: ProjectedRoutePoint
    private val width: Double
    private val height: Double

    init {
        var previousLongitude = route.first().first().longitude
        var crossesAntimeridian = false
        segments = route.map { segment ->
            segment.map { point ->
                // Keep points on the nearest world copy across the date line.
                val delta = point.longitude - previousLongitude
                val wrappedDelta = ((delta + 180.0) % 360.0 + 360.0) % 360.0 - 180.0
                val longitude = previousLongitude + wrappedDelta
                if (longitude !in -180.0..180.0) crossesAntimeridian = true
                previousLongitude = longitude
                ProjectedRoutePoint((longitude + 180.0) / 360.0, projectLatitude(point.latitude))
            }
        }
        this.crossesAntimeridian = crossesAntimeridian
        val points = segments.flatten()
        val left = points.minOf { it.x }
        val right = points.maxOf { it.x }
        val top = points.minOf { it.y }
        val bottom = points.maxOf { it.y }
        width = right - left
        height = bottom - top
        center = ProjectedRoutePoint((left + right) / 2, (top + bottom) / 2)
    }

    val centerLongitude: Double get() = center.x * 360.0 - 180.0
    val centerLatitude: Double get() = atan(sinh(PI * (1.0 - 2.0 * center.y))) * 180.0 / PI

    fun fitScale(availableWidth: Double, availableHeight: Double): Double =
        min(availableWidth / width.coerceAtLeast(MIN_SPAN), availableHeight / height.coerceAtLeast(MIN_SPAN))
}

private fun projectLatitude(latitude: Double): Double {
    val radians = latitude.coerceIn(-MAX_MERCATOR_LATITUDE, MAX_MERCATOR_LATITUDE) * PI / 180.0
    return (1.0 - ln(tan(PI / 4.0 + radians / 2.0)) / PI) / 2.0
}

private const val MIN_SPAN = 0.00001
