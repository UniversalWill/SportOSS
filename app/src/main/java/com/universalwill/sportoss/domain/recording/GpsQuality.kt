package com.universalwill.sportoss.domain.recording

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GpsQuality {
    const val MAX_FIX_AGE_MILLIS = 10_000L
    const val MAX_ACCURACY_METERS = 30f

    fun status(fix: GpsFix?, now: Long): GpsStatus = when {
        fix == null -> GpsStatus.Searching
        now - fix.elapsedRealtimeMillis !in 0..MAX_FIX_AGE_MILLIS -> GpsStatus.Lost
        !fix.latitude.isFinite() || fix.latitude !in -90.0..90.0 ||
            !fix.longitude.isFinite() || fix.longitude !in -180.0..180.0 ||
            !fix.accuracyMeters.isFinite() || fix.accuracyMeters <= 0f ||
            fix.accuracyMeters > MAX_ACCURACY_METERS -> GpsStatus.PoorAccuracy
        else -> GpsStatus.Ready
    }

    fun distanceMeters(from: GpsFix, to: GpsFix): Double {
        val latitudeDelta = Math.toRadians(to.latitude - from.latitude)
        val longitudeDelta = Math.toRadians(to.longitude - from.longitude)
        val a = sin(latitudeDelta / 2).pow(2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(longitudeDelta / 2).pow(2)
        return 6_371_000.0 * 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}
