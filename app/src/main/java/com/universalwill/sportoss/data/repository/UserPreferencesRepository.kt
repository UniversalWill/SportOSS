package com.universalwill.sportoss.data.repository

import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>

    suspend fun setThemeMode(themeMode: AppThemeMode)

    suspend fun setDynamicColorEnabled(enabled: Boolean)

    suspend fun setMapLabelLanguage(language: MapLabelLanguage)
}
