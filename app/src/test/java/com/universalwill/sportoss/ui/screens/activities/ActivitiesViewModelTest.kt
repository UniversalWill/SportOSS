package com.universalwill.sportoss.ui.screens.activities

import com.universalwill.sportoss.data.repository.OfflineWorkoutRepository
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.Workout
import com.universalwill.sportoss.ui.screens.map.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `observed workouts are exposed as content`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeWorkoutRepository()
            val viewModel = ActivitiesViewModel(repository)
            runCurrent()

            repository.workouts.value = listOf(testWorkout)
            runCurrent()

            assertFalse(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.hasLoadError)
            assertEquals(listOf(testWorkout), viewModel.uiState.value.workouts)
        }

    @Test
    fun `retry observes workouts again after failure`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeWorkoutRepository(shouldFail = true)
            val viewModel = ActivitiesViewModel(repository)
            runCurrent()

            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.hasLoadError)

            repository.shouldFail = false
            viewModel.retry()
            runCurrent()

            assertFalse(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.hasLoadError)
            assertEquals(2, repository.observationCount)
        }

    private class FakeWorkoutRepository(
        var shouldFail: Boolean = false,
    ) : OfflineWorkoutRepository {
        val workouts = MutableStateFlow<List<Workout>>(emptyList())
        var observationCount = 0
            private set

        override fun observeWorkouts(): Flow<List<Workout>> {
            observationCount += 1
            return if (shouldFail) {
                flow { throw IllegalStateException("failed") }
            } else {
                workouts
            }
        }

        override suspend fun saveWorkout(workout: Workout) = Unit
    }

    private companion object {
        val testWorkout = Workout(
            id = 1,
            type = WorkoutType.RUNNING,
            startedAtEpochMillis = 1_757_840_400_000,
            durationSeconds = 2_538,
            distanceMeters = 7_350.0,
        )
    }
}
