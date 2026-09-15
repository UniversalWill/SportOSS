package com.universalwill.sportoss.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.ui.components.WorkoutTypePicker
import com.universalwill.sportoss.ui.formatters.formatDuration
import com.universalwill.sportoss.ui.model.workoutTypeUiModel
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
internal fun LocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = stringResource(R.string.show_my_location),
        )
    }
}

@Composable
internal fun RecordingPanel(
    state: RecordingState,
    workoutType: WorkoutType,
    elapsedSeconds: Long,
    hasLocation: Boolean,
    isSaving: Boolean,
    onPrimaryAction: () -> Unit,
    onWorkoutTypeSelected: (WorkoutType) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            WorkoutTypeSelector(
                selectedWorkoutType = workoutType,
                enabled = state == RecordingState.Idle && !isSaving,
                onWorkoutTypeSelected = onWorkoutTypeSelected,
            )
            Spacer(modifier = Modifier.height(14.dp))
            RecordingStatus(state)
            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))
            RecordingMetrics(elapsedSeconds)
            Spacer(modifier = Modifier.height(18.dp))
            RecordingActions(
                state = state,
                hasLocation = hasLocation,
                isSaving = isSaving,
                onPrimaryAction = onPrimaryAction,
                onFinish = onFinish,
            )
        }
    }
}

@Composable
private fun WorkoutTypeSelector(
    selectedWorkoutType: WorkoutType,
    enabled: Boolean,
    onWorkoutTypeSelected: (WorkoutType) -> Unit,
) {
    var isPickerVisible by rememberSaveable { mutableStateOf(false) }
    val selectedTypeLabel = stringResource(
        workoutTypeUiModel(selectedWorkoutType).labelResId,
    )

    OutlinedButton(
        onClick = { isPickerVisible = true },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    ) {
        Text(
            text = stringResource(R.string.selected_workout_type, selectedTypeLabel),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
        )
    }

    if (isPickerVisible) {
        WorkoutTypePicker(
            selectedWorkoutType = selectedWorkoutType,
            onWorkoutTypeSelected = { workoutType ->
                isPickerVisible = false
                onWorkoutTypeSelected(workoutType)
            },
            onDismissRequest = { isPickerVisible = false },
        )
    }
}

@Composable
private fun RecordingStatus(state: RecordingState) {
    val (label, color) = when (state) {
        RecordingState.Idle -> stringResource(R.string.recording_status_ready) to
            MaterialTheme.colorScheme.primary
        RecordingState.Recording -> stringResource(R.string.recording_status_recording) to
            Color(0xFF2E7D32)
        RecordingState.Paused -> stringResource(R.string.recording_status_paused) to
            MaterialTheme.colorScheme.tertiary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingSmall),
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecordingMetrics(elapsedSeconds: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Metric(
            stringResource(R.string.metric_time),
            formatDuration(elapsedSeconds),
            Modifier.weight(1f),
        )
        Metric(
            stringResource(R.string.metric_distance),
            stringResource(R.string.activity_distance_km, 0.0),
            Modifier.weight(1f),
        )
        Metric(
            stringResource(R.string.metric_pace),
            stringResource(R.string.empty_pace_per_km),
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecordingActions(
    state: RecordingState,
    hasLocation: Boolean,
    isSaving: Boolean,
    onPrimaryAction: () -> Unit,
    onFinish: () -> Unit,
) {
    val primaryLabel = when (state) {
        RecordingState.Idle -> if (hasLocation) {
            stringResource(R.string.start_recording)
        } else {
            stringResource(R.string.allow_location)
        }
        RecordingState.Recording -> stringResource(R.string.pause_recording)
        RecordingState.Paused -> stringResource(R.string.resume_recording)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = onPrimaryAction,
            enabled = !isSaving,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(primaryLabel)
        }
        if (state != RecordingState.Idle) {
            FilledTonalButton(
                onClick = onFinish,
                enabled = !isSaving,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(
                    if (isSaving) {
                        stringResource(R.string.saving_workout)
                    } else {
                        stringResource(R.string.finish_recording)
                    }
                )
            }
        }
    }
}
