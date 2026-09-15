package com.universalwill.sportoss.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwill.sportoss.domain.model.AppLanguage

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val appLanguage = remember(configuration) {
        AppCompatDelegate.getApplicationLocales().toAppLanguage()
    }

    SettingsScreen(
        state = state.copy(appLanguage = appLanguage),
        onAction = { action ->
            when (action) {
                is SettingsAction.SelectAppLanguage -> {
                    AppCompatDelegate.setApplicationLocales(
                        action.language.toLocaleList(),
                    )
                }
                is StoredSettingsAction -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}

private fun AppLanguage.toLocaleList(): LocaleListCompat = languageTag
    ?.let(LocaleListCompat::forLanguageTags)
    ?: LocaleListCompat.getEmptyLocaleList()

private fun LocaleListCompat.toAppLanguage(): AppLanguage = when (get(0)?.language) {
    AppLanguage.RUSSIAN.languageTag -> AppLanguage.RUSSIAN
    AppLanguage.ENGLISH.languageTag -> AppLanguage.ENGLISH
    else -> AppLanguage.SYSTEM
}
