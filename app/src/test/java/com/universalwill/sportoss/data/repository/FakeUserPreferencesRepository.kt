package com.universalwill.sportoss.data.repository

import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(
    initialPreferences: UserPreferences = UserPreferences(),
    var updateFailure: Exception? = null,
) : UserPreferencesRepository {
    val preferences = MutableStateFlow(initialPreferences)

    override val userPreferences = preferences

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        throwUpdateFailureIfNeeded()
        preferences.value = preferences.value.copy(themeMode = themeMode)
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        throwUpdateFailureIfNeeded()
        preferences.value = preferences.value.copy(dynamicColorEnabled = enabled)
    }

    override suspend fun setMapLabelLanguage(language: MapLabelLanguage) {
        throwUpdateFailureIfNeeded()
        preferences.value = preferences.value.copy(mapLabelLanguage = language)
    }

    private fun throwUpdateFailureIfNeeded() {
        updateFailure?.let { throw it }
    }
}
