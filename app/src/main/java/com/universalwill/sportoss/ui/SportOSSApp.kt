package com.universalwill.sportoss.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwill.sportoss.domain.model.AppThemeMode
import com.universalwill.sportoss.ui.navigation.SportOSSNavigation
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Composable
fun SportOSSApp(
    viewModel: SportOSSViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (state.userPreferences.themeMode) {
        AppThemeMode.SYSTEM -> systemInDarkTheme
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    SportOSSTheme(
        darkTheme = darkTheme,
        dynamicColor = state.userPreferences.dynamicColorEnabled,
    ) {
        SportOSSNavigation(modifier = Modifier.fillMaxSize())
    }
}
