package com.universalwill.sportoss.recording

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.universalwill.sportoss.MainActivity
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.RecordingSnapshot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecordingService : Service() {
    @Inject lateinit var coordinator: RecordingCoordinator
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observing = false

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.recording_notification_channel), NotificationManager.IMPORTANCE_LOW),
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            coordinator.serviceFailed()
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification(coordinator.state.value),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } catch (_: RuntimeException) {
            coordinator.serviceFailed()
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent.action) {
            ACTION_ACTIVATE -> {
                val type = intent.getStringExtra(EXTRA_WORKOUT_TYPE)?.let { name ->
                    WorkoutType.entries.firstOrNull { it.name == name }
                } ?: coordinator.state.value.session?.workoutType ?: WorkoutType.RUNNING
                coordinator.activate(type)
            }
            ACTION_PAUSE -> coordinator.pause()
        }
        if (!observing) {
            observing = true
            scope.launch {
                coordinator.state.collect { snapshot ->
                    if (snapshot.phase == RecordingPhase.Idle || snapshot.phase == RecordingPhase.SaveFailed ||
                        snapshot.phase == RecordingPhase.Interrupted) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    } else {
                        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(snapshot))
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun notification(snapshot: RecordingSnapshot): Notification {
        val paused = snapshot.phase == RecordingPhase.Paused
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_RECORDING
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val canResume = snapshot.gps == GpsStatus.Ready
        val command = if (paused && !canResume) open else PendingIntent.getService(this, 1,
            Intent(this, RecordingService::class.java).apply {
                action = if (paused) ACTION_ACTIVATE else ACTION_PAUSE
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val title = getString(if (paused) R.string.recording_status_paused else R.string.recording_status_recording)
        val duration = snapshot.session?.durationMillis?.div(1_000) ?: 0
        val distance = snapshot.session?.distanceMeters?.div(1_000) ?: 0.0
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_recording_notification)
            .setContentTitle(title)
            .setContentText(getString(R.string.recording_notification_metrics, duration / 60, distance))
            .setContentIntent(open)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .addAction(0, getString(if (paused) R.string.resume_recording else R.string.pause_recording), command)
            .addAction(0, getString(R.string.recording_open_app), open)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        coordinator.serviceDetached()
        super.onDestroy()
    }

    companion object {
        const val ACTION_OPEN_RECORDING = "com.universalwill.sportoss.OPEN_RECORDING"
        const val ACTION_ACTIVATE = "com.universalwill.sportoss.ACTIVATE_RECORDING"
        const val ACTION_PAUSE = "com.universalwill.sportoss.PAUSE_RECORDING"
        const val EXTRA_WORKOUT_TYPE = "workout_type"
        private const val CHANNEL_ID = "workout_recording"
        private const val NOTIFICATION_ID = 1
    }
}
