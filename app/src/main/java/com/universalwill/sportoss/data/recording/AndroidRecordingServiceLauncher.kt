package com.universalwill.sportoss.data.recording

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.RecordingServiceLauncher
import com.universalwill.sportoss.recording.RecordingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidRecordingServiceLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecordingServiceLauncher {
    override fun launch(type: WorkoutType) {
        ContextCompat.startForegroundService(context, Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_ACTIVATE
            putExtra(RecordingService.EXTRA_WORKOUT_TYPE, type.name)
        })
    }
}
