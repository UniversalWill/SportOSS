package com.universalwill.sportoss.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwill.sportoss.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                mutableUiState.update {
                    it.copy(
                        userPreferences = preferences,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onAction(action: StoredSettingsAction) {
        when (action) {
            is StoredSettingsAction.SelectThemeMode -> updatePreference {
                userPreferencesRepository.setThemeMode(action.themeMode)
            }
            is StoredSettingsAction.SetDynamicColorEnabled -> updatePreference {
                userPreferencesRepository.setDynamicColorEnabled(action.enabled)
            }
            is StoredSettingsAction.SelectMapLabelLanguage -> updatePreference {
                userPreferencesRepository.setMapLabelLanguage(action.language)
            }
            StoredSettingsAction.SaveErrorShown -> mutableUiState.update {
                it.copy(hasSaveError = false)
            }
        }
    }

    private fun updatePreference(update: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                update()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update { it.copy(hasSaveError = true) }
            }
        }
    }
}
