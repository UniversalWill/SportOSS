package com.universalwill.sportoss.ui.screens.workoutdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.Workout
import com.universalwill.sportoss.domain.model.WorkoutDetails
import com.universalwill.sportoss.ui.formatters.formatActivityDate
import com.universalwill.sportoss.ui.formatters.formatDuration
import com.universalwill.sportoss.ui.model.workoutTypeUiModel
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
internal fun WorkoutDetailsScreen(
    state: WorkoutDetailsUiState,
    onAction: (WorkoutDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
    mapLabelLanguage: MapLabelLanguage = MapLabelLanguage.APPLICATION,
) {
    val content = state as? WorkoutDetailsUiState.Content
    Column(modifier = modifier.fillMaxSize()) {
        WorkoutDetailsHeader(
            workout = content?.details?.workout,
            onBack = { onAction(WorkoutDetailsAction.Back) },
            onDelete = { onAction(WorkoutDetailsAction.RequestDelete) },
        )
        when (state) {
            WorkoutDetailsUiState.Loading, WorkoutDetailsUiState.Deleted -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            WorkoutDetailsUiState.LoadFailed -> WorkoutDetailsMessage(
                message = stringResource(R.string.workout_details_load_error),
                onRetry = { onAction(WorkoutDetailsAction.RetryLoad) },
            )

            WorkoutDetailsUiState.NotFound -> WorkoutDetailsMessage(
                message = stringResource(R.string.workout_details_not_found),
            )

            is WorkoutDetailsUiState.Content -> WorkoutDetailsContent(state.details, mapLabelLanguage)
        }
    }

    val deletion = content?.deletion ?: WorkoutDeletion.None
    if (deletion != WorkoutDeletion.None) {
        DeleteWorkoutDialog(
            deletion = deletion,
            onDismiss = { onAction(WorkoutDetailsAction.CancelDelete) },
            onConfirm = { onAction(WorkoutDetailsAction.ConfirmDelete) },
        )
    }
}

@Composable
private fun WorkoutDetailsHeader(
    workout: Workout?,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.dimensions
    val locale = LocalConfiguration.current.locales[0]
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth().padding(
            start = spacing.spacingSmall,
            end = spacing.spacingSmall,
            top = spacing.spacingMedium,
            bottom = spacing.spacingLarge,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.spacingSmall),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.workout_details_back))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (workout == null) stringResource(R.string.workout_details_title)
                else stringResource(workoutTypeUiModel(workout.type).labelResId),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
            if (workout != null) {
                Text(
                    text = formatActivityDate(workout.startedAtEpochMillis, locale = locale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (workout != null) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, stringResource(R.string.workout_details_actions))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.workout_delete), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetailsContent(
    details: WorkoutDetails,
    mapLabelLanguage: MapLabelLanguage,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.dimensions
    val hasRoute = details.routeSegments.isNotEmpty()
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.spacingExtraLarge)
            .padding(bottom = spacing.spacingExtraLarge),
    ) {
        if (hasRoute) {
            WorkoutRouteMap(
                route = details.routeSegments,
                modifier = Modifier.fillMaxWidth().padding(bottom = spacing.spacingLarge),
                mapLabelLanguage = mapLabelLanguage,
            )
        }
        WorkoutMetric(stringResource(R.string.metric_time), formatDuration(details.workout.durationSeconds))
        if (hasRoute || details.workout.distanceMeters > 0.0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            WorkoutMetric(
                stringResource(R.string.metric_distance),
                String.format(locale, stringResource(R.string.activity_distance_km), details.workout.distanceMeters / 1_000.0),
            )
        }
        details.averageSpeedKilometersPerHour?.let { averageSpeed ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            WorkoutMetric(
                stringResource(R.string.metric_average_speed),
                String.format(locale, stringResource(R.string.activity_speed_kmh), averageSpeed),
            )
        }
        if (!hasRoute) {
            Text(
                text = stringResource(R.string.workout_route_missing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.spacingLarge),
            )
        }
    }
}

@Composable
private fun WorkoutMetric(label: String, value: String, modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.dimensions
    Column(modifier = modifier.fillMaxWidth().padding(vertical = spacing.spacingMedium)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WorkoutDetailsMessage(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(MaterialTheme.dimensions.spacingExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            if (onRetry != null) {
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}

@Composable
private fun DeleteWorkoutDialog(
    deletion: WorkoutDeletion,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deleting = deletion == WorkoutDeletion.Deleting
    val failed = deletion == WorkoutDeletion.Failed
    val message = when (deletion) {
        WorkoutDeletion.Deleting -> R.string.workout_deleting
        WorkoutDeletion.Failed -> R.string.workout_delete_failed
        else -> R.string.workout_delete_description
    }
    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        modifier = modifier,
        title = { Text(stringResource(R.string.workout_delete_title)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !deleting) {
                Text(
                    text = stringResource(if (failed) R.string.retry else R.string.workout_delete_confirm),
                    color = if (deleting) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !deleting) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
