package com.universalwill.sportoss.ui.navigation

import androidx.lifecycle.ViewModel
import com.universalwill.sportoss.domain.recording.RecordingControl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordingNavigationViewModel @Inject constructor(control: RecordingControl) : ViewModel() {
    val recordingState = control.state
}
