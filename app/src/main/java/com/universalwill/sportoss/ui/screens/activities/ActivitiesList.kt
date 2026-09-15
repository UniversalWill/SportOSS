package com.universalwill.sportoss.ui.screens.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.model.Workout
import com.universalwill.sportoss.ui.formatters.formatActivityDate
import com.universalwill.sportoss.ui.formatters.formatDuration
import com.universalwill.sportoss.ui.model.workoutTypeUiModel
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
internal fun ActivitiesList(
    workouts: List<Workout>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = MaterialTheme.dimensions.spacingExtraLarge,
        ),
    ) {
        items(
            items = workouts,
            key = Workout::id,
        ) { workout ->
            ActivityRow(workout = workout)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun ActivityRow(
    workout: Workout,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(52.dp)
                .background(activityColor(workout.type), CircleShape),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatActivityDate(workout.startedAtEpochMillis, locale = locale),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingExtraSmall))
            Text(
                text = stringResource(workoutTypeUiModel(workout.type).labelResId),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatDuration(workout.durationSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingExtraSmall))
            Text(
                text = stringResource(
                    R.string.activity_distance_km,
                    workout.distanceMeters / METERS_PER_KILOMETER,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun activityColor(type: WorkoutType): Color = when (type) {
    WorkoutType.RUNNING -> MaterialTheme.colorScheme.primary
    WorkoutType.BIKING -> MaterialTheme.colorScheme.tertiary
}

private const val METERS_PER_KILOMETER = 1_000.0
