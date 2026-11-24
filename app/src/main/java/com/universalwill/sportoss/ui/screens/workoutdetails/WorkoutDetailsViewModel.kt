package com.universalwill.sportoss.ui.screens.workoutdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwill.sportoss.data.repository.OfflineWorkoutRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WorkoutDetailsViewModel.Factory::class)
internal class WorkoutDetailsViewModel @AssistedInject constructor(
    @Assisted private val workoutId: Long,
    private val repository: OfflineWorkoutRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow<WorkoutDetailsUiState>(WorkoutDetailsUiState.Loading)
    val state = mutableState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: WorkoutDetailsAction) {
        when (action) {
            WorkoutDetailsAction.Back -> Unit
            WorkoutDetailsAction.RetryLoad -> {
                if (state.value == WorkoutDetailsUiState.LoadFailed) load()
            }
            WorkoutDetailsAction.RequestDelete -> changeDeletion(WorkoutDeletion.None, WorkoutDeletion.Confirming)
            WorkoutDetailsAction.CancelDelete -> cancelDeletion()
            WorkoutDetailsAction.ConfirmDelete -> delete()
        }
    }

    private fun load() {
        mutableState.value = WorkoutDetailsUiState.Loading
        viewModelScope.launch {
            mutableState.value = try {
                repository.getWorkoutDetails(workoutId)?.let { WorkoutDetailsUiState.Content(it) }
                    ?: WorkoutDetailsUiState.NotFound
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                WorkoutDetailsUiState.LoadFailed
            }
        }
    }

    private fun changeDeletion(from: WorkoutDeletion, to: WorkoutDeletion) {
        val current = state.value as? WorkoutDetailsUiState.Content ?: return
        if (current.deletion == from) mutableState.value = current.copy(deletion = to)
    }

    private fun cancelDeletion() {
        val current = state.value as? WorkoutDetailsUiState.Content ?: return
        if (current.deletion != WorkoutDeletion.Deleting) {
            mutableState.value = current.copy(deletion = WorkoutDeletion.None)
        }
    }

    private fun delete() {
        val current = state.value as? WorkoutDetailsUiState.Content ?: return
        if (current.deletion != WorkoutDeletion.Confirming && current.deletion != WorkoutDeletion.Failed) return

        mutableState.value = current.copy(deletion = WorkoutDeletion.Deleting)
        viewModelScope.launch {
            mutableState.value = try {
                repository.deleteWorkout(workoutId)
                WorkoutDetailsUiState.Deleted
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                current.copy(deletion = WorkoutDeletion.Failed)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(workoutId: Long): WorkoutDetailsViewModel
    }
}
