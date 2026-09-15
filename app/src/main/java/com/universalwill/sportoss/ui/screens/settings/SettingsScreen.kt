package com.universalwill.sportoss.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.model.AppLanguage
import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var visiblePicker by rememberSaveable { mutableStateOf<SettingsPicker?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val saveErrorMessage = stringResource(R.string.settings_save_error)

    LaunchedEffect(state.hasSaveError) {
        if (state.hasSaveError) {
            snackbarHostState.showSnackbar(saveErrorMessage)
            onAction(StoredSettingsAction.SaveErrorShown)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.settings_title),
                modifier = Modifier.padding(
                    start = MaterialTheme.dimensions.spacingExtraLarge,
                    top = MaterialTheme.dimensions.spacingExtraLarge,
                    end = MaterialTheme.dimensions.spacingExtraLarge,
                    bottom = MaterialTheme.dimensions.spacingMedium,
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SettingsContent(
                    preferences = state.userPreferences,
                    appLanguage = state.appLanguage,
                    onAppLanguageClick = { visiblePicker = SettingsPicker.AppLanguage },
                    onThemeModeClick = { visiblePicker = SettingsPicker.Theme },
                    onDynamicColorChanged = {
                        onAction(StoredSettingsAction.SetDynamicColorEnabled(it))
                    },
                    onMapLanguageClick = { visiblePicker = SettingsPicker.MapLanguage },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    when (visiblePicker) {
        SettingsPicker.AppLanguage -> SettingsOptionPicker(
            title = stringResource(R.string.settings_app_language_picker_title),
            options = AppLanguage.entries.map { language ->
                SettingsOption(language, appLanguageLabel(language))
            },
            selectedValue = state.appLanguage,
            onValueSelected = {
                visiblePicker = null
                onAction(SettingsAction.SelectAppLanguage(it))
            },
            onDismissRequest = { visiblePicker = null },
        )
        SettingsPicker.Theme -> SettingsOptionPicker(
            title = stringResource(R.string.settings_theme_picker_title),
            options = AppThemeMode.entries.map { themeMode ->
                SettingsOption(themeMode, themeModeLabel(themeMode))
            },
            selectedValue = state.userPreferences.themeMode,
            onValueSelected = {
                visiblePicker = null
                onAction(StoredSettingsAction.SelectThemeMode(it))
            },
            onDismissRequest = { visiblePicker = null },
        )
        SettingsPicker.MapLanguage -> SettingsOptionPicker(
            title = stringResource(R.string.settings_map_language_picker_title),
            options = MapLabelLanguage.entries.map { language ->
                SettingsOption(language, mapLanguageLabel(language))
            },
            selectedValue = state.userPreferences.mapLabelLanguage,
            onValueSelected = {
                visiblePicker = null
                onAction(StoredSettingsAction.SelectMapLabelLanguage(it))
            },
            onDismissRequest = { visiblePicker = null },
        )
        null -> Unit
    }
}

private enum class SettingsPicker {
    AppLanguage,
    Theme,
    MapLanguage,
}

@Composable
internal fun appLanguageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.SYSTEM -> stringResource(R.string.settings_app_language_system)
    AppLanguage.RUSSIAN -> stringResource(R.string.settings_language_russian)
    AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
}

@Composable
internal fun themeModeLabel(themeMode: AppThemeMode): String = when (themeMode) {
    AppThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    AppThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    AppThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
internal fun mapLanguageLabel(language: MapLabelLanguage): String = when (language) {
    MapLabelLanguage.APPLICATION -> stringResource(R.string.settings_map_language_application)
    MapLabelLanguage.RUSSIAN -> stringResource(R.string.settings_language_russian)
    MapLabelLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
}
