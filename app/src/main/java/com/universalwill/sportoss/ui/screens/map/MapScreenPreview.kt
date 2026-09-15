package com.universalwill.sportoss.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    SportOSSTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "Предпросмотр карты",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LocationButton(
                onClick = {},
                containerColor = MapOverlayContainerColor,
                contentColor = MapOverlayContentColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 12.dp,
                        bottom = (
                            RECORDING_PANEL_HEIGHT_DP +
                                LOCATION_CONTROL_STACK_OFFSET_DP +
                                8
                        ).dp,
                    ),
            )
            RecordingPanel(
                state = RecordingState.Idle,
                workoutType = WorkoutType.RUNNING,
                elapsedSeconds = 0,
                hasLocation = true,
                isSaving = false,
                onPrimaryAction = {},
                onWorkoutTypeSelected = {},
                onFinish = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
        }
    }
}
