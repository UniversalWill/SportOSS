package com.universalwill.sportoss.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.ui.model.workoutTypeUiModels
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun WorkoutTypePicker(
    selectedWorkoutType: WorkoutType?,
    onWorkoutTypeSelected: (WorkoutType) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        WorkoutTypePickerContent(
            selectedWorkoutType = selectedWorkoutType,
            onWorkoutTypeSelected = onWorkoutTypeSelected,
        )
    }
}

@Composable
internal fun WorkoutTypePickerContent(
    selectedWorkoutType: WorkoutType?,
    onWorkoutTypeSelected: (WorkoutType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = MaterialTheme.dimensions.spacingLarge),
    ) {
        item {
            Text(
                text = stringResource(R.string.choose_workout_type),
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.dimensions.spacingExtraLarge,
                    vertical = MaterialTheme.dimensions.spacingMedium,
                ),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        itemsIndexed(
            items = workoutTypeUiModels,
            key = { _, item -> item.type.name },
        ) { index, item ->
            ListItem(
                headlineContent = { Text(stringResource(item.labelResId)) },
                supportingContent = { Text(stringResource(item.descriptionResId)) },
                trailingContent = {
                    RadioButton(
                        selected = item.type == selectedWorkoutType,
                        onClick = null,
                    )
                },
                modifier = Modifier.clickable {
                    onWorkoutTypeSelected(item.type)
                },
            )
            if (index < workoutTypeUiModels.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.dimensions.spacingExtraLarge,
                    ),
                )
            }
        }
    }
}
