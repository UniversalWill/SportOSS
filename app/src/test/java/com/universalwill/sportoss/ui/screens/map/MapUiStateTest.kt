package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapUiStateTest {
    @Test
    fun `application map language follows supported app locale`() {
        assertEquals("ru", MapLabelLanguage.APPLICATION.resolveLanguageTag("ru"))
        assertEquals("en", MapLabelLanguage.APPLICATION.resolveLanguageTag("en"))
        assertEquals("en", MapLabelLanguage.APPLICATION.resolveLanguageTag("de"))
    }

    @Test
    fun `explicit map language ignores app locale`() {
        assertEquals("ru", MapLabelLanguage.RUSSIAN.resolveLanguageTag("en"))
        assertEquals("en", MapLabelLanguage.ENGLISH.resolveLanguageTag("ru"))
    }

    @Test
    fun `start and resume require GPS while pause and save retry remain available`() {
        GpsStatus.entries.forEach { gps ->
            listOf(RecordingState.Idle, RecordingState.Paused, RecordingState.Interrupted).forEach { phase ->
                val state = MapUiState(recording = RecordingSnapshot(phase = phase, gps = gps, isLoading = false))
                assertEquals(gps == GpsStatus.Ready, state.canToggle)
            }
        }
        assertTrue(MapUiState(recording = RecordingSnapshot(
            phase = RecordingState.Recording, gps = GpsStatus.Lost, isLoading = false)).canToggle)
        listOf(RecordingState.Starting, RecordingState.Saving, RecordingState.SaveFailed).forEach { phase ->
            assertFalse(MapUiState(recording = RecordingSnapshot(
                phase = phase, gps = GpsStatus.Ready, isLoading = false)).canToggle)
        }
        assertFalse(MapUiState(recording = RecordingSnapshot(gps = GpsStatus.Ready)).canToggle)
    }
}
