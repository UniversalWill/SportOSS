package com.universalwill.sportoss.ui.screens.workoutdetails

import com.universalwill.sportoss.data.repository.OfflineWorkoutRepository
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.Workout
import com.universalwill.sportoss.domain.model.WorkoutDetails
import com.universalwill.sportoss.ui.screens.map.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads the requested workout after retrying a failed read`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeWorkoutRepository().apply { failLoad = true }
        val viewModel = WorkoutDetailsViewModel(workout.id, repository)
        runCurrent()
        assertEquals(WorkoutDetailsUiState.LoadFailed, viewModel.state.value)

        repository.failLoad = false
        viewModel.onAction(WorkoutDetailsAction.RetryLoad)
        runCurrent()

        assertEquals(WorkoutDetailsUiState.Content(details), viewModel.state.value)
    }

    @Test
    fun `deletion requires confirmation and can retry once the current attempt fails`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeWorkoutRepository()
            val viewModel = WorkoutDetailsViewModel(workout.id, repository)
            runCurrent()
            viewModel.onAction(WorkoutDetailsAction.ConfirmDelete)
            viewModel.onAction(WorkoutDetailsAction.RequestDelete)
            viewModel.onAction(WorkoutDetailsAction.CancelDelete)
            runCurrent()
            assertEquals(WorkoutDetailsUiState.Content(details), viewModel.state.value)
            assertEquals(emptyList<Long>(), repository.deletedIds)

            viewModel.onAction(WorkoutDetailsAction.RequestDelete)
            viewModel.onAction(WorkoutDetailsAction.ConfirmDelete)
            viewModel.onAction(WorkoutDetailsAction.ConfirmDelete)
            runCurrent()
            assertEquals(WorkoutDetailsUiState.Content(details, WorkoutDeletion.Deleting), viewModel.state.value)
            assertEquals(listOf(workout.id), repository.deletedIds)

            repository.deleteResult.completeExceptionally(IllegalStateException("write failed"))
            runCurrent()
            assertEquals(WorkoutDetailsUiState.Content(details, WorkoutDeletion.Failed), viewModel.state.value)

            repository.deleteResult = CompletableDeferred(Unit)
            viewModel.onAction(WorkoutDetailsAction.ConfirmDelete)
            runCurrent()
            assertEquals(WorkoutDetailsUiState.Deleted, viewModel.state.value)
            assertEquals(listOf(workout.id, workout.id), repository.deletedIds)
        }

    private class FakeWorkoutRepository : OfflineWorkoutRepository {
        var failLoad = false
        var deleteResult = CompletableDeferred<Unit>()
        val deletedIds = mutableListOf<Long>()

        override fun observeWorkouts() = flowOf(listOf(workout))
        override suspend fun saveWorkout(workout: Workout) = Unit

        override suspend fun getWorkoutDetails(id: Long): WorkoutDetails? {
            check(!failLoad)
            return details.takeIf { it.workout.id == id }
        }

        override suspend fun deleteWorkout(id: Long) {
            deletedIds.add(id)
            deleteResult.await()
        }
    }

    private companion object {
        val workout = Workout(12, WorkoutType.BIKING, 0, 1_800, 5_000.0)
        val details = WorkoutDetails(workout, emptyList())
    }
}
