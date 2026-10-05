package com.universalwill.sportoss.data.recording

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.universalwill.sportoss.domain.recording.GpsFix
import com.universalwill.sportoss.domain.recording.GpsQuality
import com.universalwill.sportoss.domain.recording.GpsReading
import com.universalwill.sportoss.domain.recording.GpsSource
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingClock
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AndroidGpsSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: RecordingClock,
) : GpsSource, LocationListener {
    private val manager = context.getSystemService(LocationManager::class.java)
    private val mutableReading = MutableStateFlow(GpsReading())
    override val reading = mutableReading.asStateFlow()
    private var requested = false
    private var subscribed = false
    private var lastFix: GpsFix? = null

    override fun start() {
        requested = true
        refresh()
    }

    override fun stop() {
        requested = false
        removeUpdates()
        lastFix = null
        refresh()
    }

    override fun refresh() {
        val unavailable = when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) GpsStatus.PrecisePermissionRequired
                else GpsStatus.PermissionRequired
            }
            manager == null || LocationManager.GPS_PROVIDER !in manager.allProviders -> GpsStatus.Unavailable
            !manager.isLocationEnabled || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> GpsStatus.Disabled
            else -> null
        }
        if (unavailable != null) {
            removeUpdates()
            lastFix = null
            mutableReading.value = GpsReading(unavailable)
            return
        }
        if (requested && !subscribed) subscribe()
        if (subscribed || !requested) {
            mutableReading.value = GpsReading(GpsQuality.status(lastFix, clock.elapsedRealtimeMillis()), lastFix)
        }
    }

    private fun subscribe() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) return
        try {
            manager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000L, 0f, this, Looper.getMainLooper())
            subscribed = true
        } catch (_: SecurityException) {
            mutableReading.value = GpsReading(GpsStatus.PermissionRequired)
        } catch (_: IllegalArgumentException) {
            mutableReading.value = GpsReading(GpsStatus.Unavailable)
        }
    }

    private fun removeUpdates() {
        if (subscribed) manager?.removeUpdates(this)
        subscribed = false
    }

    override fun onLocationChanged(location: Location) {
        if (!requested) return
        lastFix = GpsFix(
            location.latitude, location.longitude, location.time,
            location.elapsedRealtimeNanos / 1_000_000,
            if (location.hasAccuracy()) location.accuracy else Float.POSITIVE_INFINITY,
        )
        refresh()
    }

    override fun onProviderEnabled(provider: String) = refresh()
    override fun onProviderDisabled(provider: String) = refresh()
}
