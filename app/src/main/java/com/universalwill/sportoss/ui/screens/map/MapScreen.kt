package com.universalwill.sportoss.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.BuildConfig
import com.universalwill.sportoss.ui.theme.SportOSSTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultHeadingProvider
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberLocationState
import org.maplibre.compose.material3.Material3Full
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.overlay.include
import org.maplibre.compose.style.BaseStyle
import kotlin.time.Duration.Companion.milliseconds

private const val MAP_LANGUAGE = "ru"
private const val MAP_STYLE_ID = "outdoors"
private const val RECORDING_PANEL_HEIGHT_DP = 196

private val LOCALIZED_SOURCE_LAYERS = setOf(
    "place",
    "poi",
    "park",
    "mountain_peak",
    "water_name",
    "transportation_name",
    "airport",
)

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    var recordingState by rememberSaveable { mutableStateOf(RecordingState.Idle) }
    var elapsedSeconds by rememberSaveable { mutableLongStateOf(0L) }
    var hasCenteredInitially by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val locationState = rememberLocationState(
        provider = rememberDefaultLocationProvider(),
        headingProvider = rememberDefaultHeadingProvider(),
    )
    val styleUrl = BaseStyle.Uri(
        "https://tiles.stadiamaps.com/styles/$MAP_STYLE_ID.json?api_key=${BuildConfig.STADIA_API_KEY}"
    )
    val mapState = rememberMapState(baseStyle = styleUrl) {
        val currentMapState = checkNotNull(LocalMapState.current)

        LocationPuck(
            idPrefix = "user",
            locationState = locationState,
        )
        LocationTrackingEffect(
            locationState = locationState,
            enabled = !hasCenteredInitially,
        ) {
            currentMapState.animateCameraPosition(
                CameraPosition(
                    target = currentLocation.position,
                    zoom = 15.0,
                )
            )
            hasCenteredInitially = true
        }
    }
    val styleLoadState = mapState.style.loadState

    LaunchedEffect(styleLoadState) {
        if (styleLoadState == StyleLoadState.Ready) {
            mapState.localizeLabels(MAP_LANGUAGE)
        }
    }

    LaunchedEffect(recordingState) {
        while (recordingState == RecordingState.Recording) {
            delay(1_000.milliseconds)
            elapsedSeconds++
        }
    }

    Box(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            cameraPadding = PaddingValues(bottom = RECORDING_PANEL_HEIGHT_DP.dp),
            contentWindowInsets = WindowInsets(bottom = RECORDING_PANEL_HEIGHT_DP.dp),
        ) {
            include(MapOverlay.Material3Full)
        }

        LocationButton(
            onClick = {
                val position = locationState.lastLocation?.position
                if (position == null) {
                    locationState.requestPermission()
                } else {
                    coroutineScope.launch {
                        mapState.animateCameraPosition(
                            CameraPosition(
                                target = position,
                                zoom = maxOf(mapState.cameraPosition.zoom, 15.0),
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = (RECORDING_PANEL_HEIGHT_DP + 8).dp,
                ),
        )

        RecordingPanel(
            state = recordingState,
            elapsedSeconds = elapsedSeconds,
            hasLocation = locationState.lastLocation != null,
            onPrimaryAction = {
                when (recordingState) {
                    RecordingState.Idle -> {
                        if (locationState.lastLocation == null) {
                            locationState.requestPermission()
                        } else {
                            recordingState = RecordingState.Recording
                        }
                    }
                    RecordingState.Recording -> recordingState = RecordingState.Paused
                    RecordingState.Paused -> recordingState = RecordingState.Recording
                }
            },
            onFinish = {
                recordingState = RecordingState.Idle
                elapsedSeconds = 0
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun LocationButton(
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
private fun RecordingPanel(
    state: RecordingState,
    elapsedSeconds: Long,
    hasLocation: Boolean,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Metric("Время", formatElapsedTime(elapsedSeconds), Modifier.weight(1f))
                Metric("Дистанция", "0,00 км", Modifier.weight(1f))
                Metric("Темп", "--:-- /км", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(18.dp))
            RecordingActions(
                state = state,
                hasLocation = hasLocation,
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
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
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
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(primaryLabel)
        }
        if (state != RecordingState.Idle) {
            FilledTonalButton(
                onClick = onFinish,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text("Завершить")
            }
        }
    }
}

private fun formatElapsedTime(totalSeconds: Long): String {
    val hours = totalSeconds / 3_600
    val minutes = totalSeconds % 3_600 / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private enum class RecordingState {
    Idle,
    Recording,
    Paused,
}

private suspend fun MapState.localizeLabels(language: String) {
    val localizedName = localizedNameExpression(language)

    for (layer in style.layers) {
        if (
            layer.type == "symbol" &&
            layer.sourceLayer in LOCALIZED_SOURCE_LAYERS &&
            layer.getProperty("text-field") != null
        ) {
            layer.asMutable?.setLayoutProperty("text-field", localizedName)
        }
    }
}

private fun localizedNameExpression(language: String): JsonArray = buildJsonArray {
    add(JsonPrimitive("coalesce"))
    add(nameExpression("name:$language"))
    add(nameExpression("name:latin"))
    add(nameExpression("name"))
}

private fun nameExpression(property: String): JsonArray = buildJsonArray {
    add(JsonPrimitive("get"))
    add(JsonPrimitive(property))
}

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
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 12.dp,
                        bottom = (RECORDING_PANEL_HEIGHT_DP + 8).dp,
                    ),
            )
            RecordingPanel(
                state = RecordingState.Idle,
                elapsedSeconds = 0,
                hasLocation = true,
                onPrimaryAction = {},
                onFinish = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
        }
    }
}
