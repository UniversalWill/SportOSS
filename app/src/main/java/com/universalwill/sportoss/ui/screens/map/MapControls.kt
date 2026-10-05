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
import androidx.compose.material3.TextButton
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
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingError

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
    state: MapUiState,
    onAction: (MapAction) -> Unit,
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
                selectedWorkoutType = state.workoutType,
                enabled = state.recordingState == RecordingState.Idle && !state.recording.isLoading,
                onWorkoutTypeSelected = { onAction(MapAction.SelectWorkoutType(it)) },
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (state.recording.isLoading) {
                Text(stringResource(R.string.recording_loading))
            } else {
                RecordingStatus(state.recordingState)
            }
            GpsAvailability(state.recording.gps, onAction)
            if (state.recordingState == RecordingState.Interrupted) {
                Text(stringResource(R.string.recording_recovery_description), style = MaterialTheme.typography.bodySmall)
            }
            if (state.recordingState == RecordingState.SaveFailed) {
                Text(stringResource(R.string.recording_save_failed), style = MaterialTheme.typography.bodySmall)
            }
            state.recording.error?.let { error ->
                val message = when (error) {
                    RecordingError.Storage -> R.string.recording_storage_error
                    RecordingError.ServiceStart -> R.string.recording_service_error
                    RecordingError.Save -> R.string.workout_save_error
                }
                Text(stringResource(message), color = MaterialTheme.colorScheme.error)
                if (state.recording.isLoading) {
                    TextButton(onClick = { onAction(MapAction.RetryLoad) }) { Text(stringResource(R.string.retry)) }
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))
            RecordingMetrics(state.elapsedSeconds, state.distanceMeters)
            Spacer(modifier = Modifier.height(18.dp))
            RecordingActions(
                state = state,
                onAction = onAction,
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
            MaterialTheme.colorScheme.primary
        RecordingState.Paused -> stringResource(R.string.recording_status_paused) to
            MaterialTheme.colorScheme.tertiary
        RecordingState.Interrupted -> stringResource(R.string.recording_interrupted) to MaterialTheme.colorScheme.tertiary
        RecordingState.Starting -> stringResource(R.string.recording_starting) to MaterialTheme.colorScheme.primary
        RecordingState.Saving -> stringResource(R.string.saving_workout) to MaterialTheme.colorScheme.primary
        RecordingState.SaveFailed -> stringResource(R.string.workout_save_error) to MaterialTheme.colorScheme.error
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
private fun RecordingMetrics(elapsedSeconds: Long, distanceMeters: Double) {
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
            stringResource(R.string.activity_distance_km, distanceMeters / 1_000),
            Modifier.weight(1f),
        )
        Metric(
            stringResource(R.string.metric_pace),
            if (distanceMeters >= 10.0 && elapsedSeconds > 0) {
                val seconds = (elapsedSeconds / (distanceMeters / 1_000)).toLong()
                stringResource(R.string.activity_pace_per_km, "%d:%02d".format(seconds / 60, seconds % 60))
            } else stringResource(R.string.empty_pace_per_km),
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
    state: MapUiState,
    onAction: (MapAction) -> Unit,
) {
    val isSaving = state.recordingState == RecordingState.Saving
    val busy = isSaving || state.recordingState == RecordingState.Starting || state.recording.isLoading
    val primaryLabel = when (state.recordingState) {
        RecordingState.Idle -> stringResource(R.string.start_recording)
        RecordingState.Recording -> stringResource(R.string.pause_recording)
        RecordingState.Paused, RecordingState.Interrupted -> stringResource(R.string.resume_recording)
        RecordingState.Starting -> stringResource(R.string.recording_starting)
        RecordingState.Saving -> stringResource(R.string.saving_workout)
        RecordingState.SaveFailed -> stringResource(R.string.recording_retry_save)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.recordingState != RecordingState.SaveFailed) {
            Button(
                onClick = { onAction(MapAction.ToggleRecording) },
                enabled = state.canToggle,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(primaryLabel)
            }
        }
        if (state.recording.session != null) {
            FilledTonalButton(
                onClick = { onAction(MapAction.FinishRecording) },
                enabled = !busy,
                modifier = if (state.recordingState == RecordingState.SaveFailed) Modifier.weight(1f) else Modifier,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(
                    if (isSaving) {
                        stringResource(R.string.saving_workout)
                    } else if (state.recordingState == RecordingState.SaveFailed) {
                        stringResource(R.string.recording_retry_save)
                    } else {
                        stringResource(R.string.finish_recording)
                    }
                )
            }
        }
    }
}

@Composable
private fun GpsAvailability(status: GpsStatus, onAction: (MapAction) -> Unit) {
    val label = when (status) {
        GpsStatus.PermissionRequired -> R.string.gps_permission_required
        GpsStatus.PrecisePermissionRequired -> R.string.gps_precise_required
        GpsStatus.Disabled -> R.string.gps_disabled
        GpsStatus.Unavailable -> R.string.gps_unavailable
        GpsStatus.Searching -> R.string.gps_searching
        GpsStatus.PoorAccuracy -> R.string.gps_poor_accuracy
        GpsStatus.Ready -> R.string.gps_ready
        GpsStatus.Lost -> R.string.gps_lost
    }
    Text(stringResource(label), style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (status == GpsStatus.PermissionRequired || status == GpsStatus.PrecisePermissionRequired) {
        Row {
            TextButton(onClick = { onAction(MapAction.RequestLocationPermission) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.allow_location))
            }
            TextButton(onClick = { onAction(MapAction.OpenAppSettings) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.recording_open_settings))
            }
        }
    } else if (status == GpsStatus.Disabled) {
        TextButton(onClick = { onAction(MapAction.OpenLocationSettings) }) {
            Text(stringResource(R.string.recording_enable_gps))
        }
    }
}
