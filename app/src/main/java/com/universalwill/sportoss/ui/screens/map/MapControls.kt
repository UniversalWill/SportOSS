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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.ui.formatters.formatDuration

@Composable
internal fun LocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Показать моё положение",
        )
    }
}

@Composable
internal fun RecordingPanel(
    state: RecordingState,
    elapsedSeconds: Long,
    hasLocation: Boolean,
    isSaving: Boolean,
    onPrimaryAction: () -> Unit,
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
            RecordingStatus(state)
            Spacer(modifier = Modifier.height(16.dp))
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
private fun RecordingStatus(state: RecordingState) {
    val (label, color) = when (state) {
        RecordingState.Idle -> "Бег · готово к старту" to MaterialTheme.colorScheme.primary
        RecordingState.Recording -> "Бег · идёт запись" to Color(0xFF2E7D32)
        RecordingState.Paused -> "Бег · пауза" to MaterialTheme.colorScheme.tertiary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        Metric("Время", formatDuration(elapsedSeconds), Modifier.weight(1f))
        Metric("Дистанция", "0,00 км", Modifier.weight(1f))
        Metric("Темп", "--:-- /км", Modifier.weight(1f))
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
        RecordingState.Idle -> if (hasLocation) "Начать запись" else "Разрешить геопозицию"
        RecordingState.Recording -> "Пауза"
        RecordingState.Paused -> "Продолжить"
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
                Text(if (isSaving) "Сохранение…" else "Завершить")
            }
        }
    }
}
