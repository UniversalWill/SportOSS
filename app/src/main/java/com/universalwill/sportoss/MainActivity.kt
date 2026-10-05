package com.universalwill.sportoss

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.universalwill.sportoss.ui.SportOSSApp
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.universalwill.sportoss.recording.RecordingService

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var openRecordingRequest by mutableIntStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && intent?.action == RecordingService.ACTION_OPEN_RECORDING) openRecordingRequest++
        enableEdgeToEdge()
        setContent {
            SportOSSApp(openRecordingRequest = openRecordingRequest)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == RecordingService.ACTION_OPEN_RECORDING) openRecordingRequest++
    }
}
