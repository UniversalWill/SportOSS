package com.universalwill.sportoss.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.BuildConfig
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultHeadingProvider
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberLocationState
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.style.BaseStyle

private const val MAP_STYLE_ID = "outdoors"
internal const val RECORDING_PANEL_HEIGHT_DP = 196

@Composable
fun MapScreen(
    state: MapUiState,
    onAction: (MapAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hasCenteredInitially by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
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

    LaunchedEffect(state.hasSaveError) {
        if (state.hasSaveError) {
            snackbarHostState.showSnackbar("Не удалось сохранить тренировку")
            onAction(MapAction.SaveErrorShown)
        }
    }

    Box(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            cameraPadding = PaddingValues(bottom = RECORDING_PANEL_HEIGHT_DP.dp),
            contentWindowInsets = WindowInsets(bottom = RECORDING_PANEL_HEIGHT_DP.dp),
        ) {
            SportOSSMapOverlay(
                onLocationClick = {
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
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (RECORDING_PANEL_HEIGHT_DP + 16).dp),
        )

        RecordingPanel(
            state = state.recordingState,
            elapsedSeconds = state.elapsedSeconds,
            hasLocation = locationState.lastLocation != null,
            isSaving = state.isSaving,
            onPrimaryAction = {
                when (state.recordingState) {
                    RecordingState.Idle -> {
                        if (locationState.lastLocation == null) {
                            locationState.requestPermission()
                        } else {
                            onAction(MapAction.ToggleRecording)
                        }
                    }
                    RecordingState.Recording,
                    RecordingState.Paused,
                    -> onAction(MapAction.ToggleRecording)
                }
            },
            onFinish = { onAction(MapAction.FinishRecording) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        )
    }
}
