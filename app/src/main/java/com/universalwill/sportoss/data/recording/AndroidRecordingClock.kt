package com.universalwill.sportoss.data.recording

import android.os.SystemClock
import com.universalwill.sportoss.domain.recording.RecordingClock
import javax.inject.Inject

class AndroidRecordingClock @Inject constructor() : RecordingClock {
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
    override fun epochMillis(): Long = System.currentTimeMillis()
}
