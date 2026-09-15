package com.universalwill.sportoss.ui

import com.universalwill.sportoss.domain.model.UserPreferences

data class SportOSSUiState(
    val userPreferences: UserPreferences = UserPreferences(),
)
