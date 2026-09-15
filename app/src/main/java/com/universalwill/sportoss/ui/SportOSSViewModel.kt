package com.universalwill.sportoss.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwill.sportoss.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SportOSSViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val uiState = userPreferencesRepository.userPreferences
        .map(::SportOSSUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SportOSSUiState(),
        )
}
