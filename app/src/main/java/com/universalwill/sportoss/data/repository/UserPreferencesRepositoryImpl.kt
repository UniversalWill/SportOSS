package com.universalwill.sportoss.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
)

private object UserPreferencesKeys {
    val themeMode = stringPreferencesKey("theme_mode")
    val dynamicColorEnabled = booleanPreferencesKey("dynamic_color_enabled")
    val mapLabelLanguage = stringPreferencesKey("map_label_language")
}

class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {
    override val userPreferences: Flow<UserPreferences> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map(::toUserPreferences)

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[UserPreferencesKeys.themeMode] = themeMode.toStorageValue()
        }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[UserPreferencesKeys.dynamicColorEnabled] = enabled
        }
    }

    override suspend fun setMapLabelLanguage(language: MapLabelLanguage) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[UserPreferencesKeys.mapLabelLanguage] = language.toStorageValue()
        }
    }
}

private fun toUserPreferences(preferences: Preferences): UserPreferences = UserPreferences(
    themeMode = preferences[UserPreferencesKeys.themeMode].toThemeMode(),
    dynamicColorEnabled = preferences[UserPreferencesKeys.dynamicColorEnabled]
        ?: true,
    mapLabelLanguage = preferences[UserPreferencesKeys.mapLabelLanguage]
        .toMapLabelLanguage(),
)

private fun AppThemeMode.toStorageValue(): String = when (this) {
    AppThemeMode.SYSTEM -> "system"
    AppThemeMode.LIGHT -> "light"
    AppThemeMode.DARK -> "dark"
}

private fun String?.toThemeMode(): AppThemeMode = when (this) {
    "light" -> AppThemeMode.LIGHT
    "dark" -> AppThemeMode.DARK
    else -> AppThemeMode.SYSTEM
}

private fun String?.toMapLabelLanguage(): MapLabelLanguage = when (this) {
    MapLabelLanguage.ENGLISH.languageTag -> MapLabelLanguage.ENGLISH
    MapLabelLanguage.RUSSIAN.languageTag -> MapLabelLanguage.RUSSIAN
    else -> MapLabelLanguage.APPLICATION
}

private fun MapLabelLanguage.toStorageValue(): String = when (this) {
    MapLabelLanguage.APPLICATION -> "application"
    MapLabelLanguage.RUSSIAN -> requireNotNull(languageTag)
    MapLabelLanguage.ENGLISH -> requireNotNull(languageTag)
}
