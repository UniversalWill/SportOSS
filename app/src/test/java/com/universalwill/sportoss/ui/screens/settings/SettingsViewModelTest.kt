package com.universalwill.sportoss.ui.screens.settings

import com.universalwill.sportoss.data.repository.FakeUserPreferencesRepository
import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.UserPreferences
import com.universalwill.sportoss.ui.screens.map.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `repository preferences are exposed as screen state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val preferences = UserPreferences(
                themeMode = AppThemeMode.DARK,
                dynamicColorEnabled = false,
                mapLabelLanguage = MapLabelLanguage.ENGLISH,
            )
            val repository = FakeUserPreferencesRepository(preferences)
            val viewModel = SettingsViewModel(repository)

            runCurrent()

            assertEquals(preferences, viewModel.uiState.value.userPreferences)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `actions persist every supported setting`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserPreferencesRepository()
            val viewModel = SettingsViewModel(repository)
            runCurrent()

            viewModel.onAction(StoredSettingsAction.SelectThemeMode(AppThemeMode.DARK))
            runCurrent()
            viewModel.onAction(StoredSettingsAction.SetDynamicColorEnabled(false))
            runCurrent()
            viewModel.onAction(
                StoredSettingsAction.SelectMapLabelLanguage(MapLabelLanguage.ENGLISH)
            )
            runCurrent()

            assertEquals(
                UserPreferences(
                    themeMode = AppThemeMode.DARK,
                    dynamicColorEnabled = false,
                    mapLabelLanguage = MapLabelLanguage.ENGLISH,
                ),
                repository.preferences.value,
            )
        }

    @Test
    fun `save failure is exposed and can be dismissed`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserPreferencesRepository(
                updateFailure = IllegalStateException("failed"),
            )
            val viewModel = SettingsViewModel(repository)
            runCurrent()

            viewModel.onAction(StoredSettingsAction.SelectThemeMode(AppThemeMode.DARK))
            runCurrent()

            assertTrue(viewModel.uiState.value.hasSaveError)

            viewModel.onAction(StoredSettingsAction.SaveErrorShown)

            assertFalse(viewModel.uiState.value.hasSaveError)
        }
}
