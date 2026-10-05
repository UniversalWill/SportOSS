package com.universalwill.sportoss.ui.screens.map

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.RecordingSession
import com.universalwill.sportoss.domain.recording.RecordingSnapshot
import com.universalwill.sportoss.ui.theme.SportOSSTheme
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecordingPanelTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun startIsDisabledUntilGpsReady() {
        compose.setContent {
            SportOSSTheme(dynamicColor = false) {
                RecordingPanel(MapUiState(recording = RecordingSnapshot(
                    gps = GpsStatus.Searching, isLoading = false)), {})
            }
        }
        compose.onNodeWithText(if (isRussian()) "Начать запись" else "Start recording").assertIsNotEnabled()
        compose.onNodeWithText(if (isRussian()) "Поиск GPS…" else "Searching for GPS…").assertExists()
    }

    @Test
    fun interruptedWorkoutCanFinishWithoutGps() {
        var action: MapAction? = null
        compose.setContent {
            SportOSSTheme(dynamicColor = false) {
                RecordingPanel(MapUiState(recording = RecordingSnapshot(
                    session = RecordingSession(1, WorkoutType.RUNNING, 0, RecordingPhase.Interrupted),
                    phase = RecordingPhase.Interrupted, gps = GpsStatus.Searching, isLoading = false,
                )), { action = it })
            }
        }
        compose.onNodeWithText(if (isRussian()) "Продолжить" else "Resume").assertIsNotEnabled()
        compose.onNodeWithText(if (isRussian()) "Завершить" else "Finish").assertIsEnabled().performClick()
        assertEquals(MapAction.FinishRecording, action)
    }

    @Test
    fun saveFailureOffersRetryWithoutResumingRecording() {
        var action: MapAction? = null
        compose.setContent {
            SportOSSTheme(dynamicColor = false) {
                RecordingPanel(MapUiState(recording = RecordingSnapshot(
                    session = RecordingSession(1, WorkoutType.RUNNING, 0, RecordingPhase.SaveFailed),
                    phase = RecordingPhase.SaveFailed, gps = GpsStatus.Lost, isLoading = false,
                )), { action = it })
            }
        }
        compose.onNodeWithText(if (isRussian()) "Повторить сохранение" else "Retry saving")
            .assertIsEnabled().performClick()
        assertEquals(MapAction.FinishRecording, action)
    }

    private fun isRussian() = Locale.getDefault().language == "ru"
}
