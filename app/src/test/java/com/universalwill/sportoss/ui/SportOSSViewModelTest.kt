package com.universalwill.sportoss.ui

import com.universalwill.sportoss.data.repository.FakeUserPreferencesRepository
import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.UserPreferences
import com.universalwill.sportoss.ui.screens.map.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SportOSSViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `theme preferences update app state`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeUserPreferencesRepository()
        val viewModel = SportOSSViewModel(repository)
        runCurrent()

        repository.setThemeMode(AppThemeMode.DARK)
        runCurrent()

        assertEquals(
            UserPreferences(themeMode = AppThemeMode.DARK),
            viewModel.uiState.value.userPreferences,
        )
    }
}
