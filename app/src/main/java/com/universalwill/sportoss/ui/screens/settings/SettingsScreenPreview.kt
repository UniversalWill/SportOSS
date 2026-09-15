package com.universalwill.sportoss.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.AppLanguage
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.UserPreferences
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SportOSSTheme(dynamicColor = false) {
        SettingsScreen(
            state = SettingsUiState(
                appLanguage = AppLanguage.SYSTEM,
                userPreferences = UserPreferences(
                    themeMode = AppThemeMode.SYSTEM,
                    dynamicColorEnabled = true,
                    mapLabelLanguage = MapLabelLanguage.APPLICATION,
                ),
                isLoading = false,
            ),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsLoadingPreview() {
    SportOSSTheme(dynamicColor = false) {
        SettingsScreen(
            state = SettingsUiState(),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
