package com.universalwill.sportoss.ui.model

import androidx.annotation.StringRes
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.enums.WorkoutType

internal data class WorkoutTypeUiModel(
    val type: WorkoutType,
    @StringRes val labelResId: Int,
    @StringRes val descriptionResId: Int,
)

internal val workoutTypeUiModels = WorkoutType.entries.map(::workoutTypeUiModel)

internal fun workoutTypeUiModel(workoutType: WorkoutType): WorkoutTypeUiModel = when (workoutType) {
    WorkoutType.RUNNING -> WorkoutTypeUiModel(
        type = workoutType,
        labelResId = R.string.workout_type_running,
        descriptionResId = R.string.workout_type_running_description,
    )
    WorkoutType.BIKING -> WorkoutTypeUiModel(
        type = workoutType,
        labelResId = R.string.workout_type_biking,
        descriptionResId = R.string.workout_type_biking_description,
    )
}
