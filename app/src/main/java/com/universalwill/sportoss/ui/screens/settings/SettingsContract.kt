package com.universalwill.sportoss.ui.screens.settings

import com.universalwill.sportoss.domain.model.AppLanguage
import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.UserPreferences

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val isLoading: Boolean = true,
    val hasSaveError: Boolean = false,
)

sealed interface SettingsAction {
    data class SelectAppLanguage(val language: AppLanguage) : SettingsAction
}

sealed interface StoredSettingsAction : SettingsAction {
    data class SelectThemeMode(val themeMode: AppThemeMode) : StoredSettingsAction
    data class SetDynamicColorEnabled(val enabled: Boolean) : StoredSettingsAction
    data class SelectMapLabelLanguage(val language: MapLabelLanguage) : StoredSettingsAction
    data object SaveErrorShown : StoredSettingsAction
}
