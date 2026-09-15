package com.universalwill.sportoss.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Preview(showBackground = true, heightDp = 320)
@Composable
private fun WorkoutTypePickerPreview() {
    SportOSSTheme {
        Surface(
            modifier = Modifier,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            WorkoutTypePickerContent(
                selectedWorkoutType = WorkoutType.BIKING,
                onWorkoutTypeSelected = {},
            )
        }
    }
}
