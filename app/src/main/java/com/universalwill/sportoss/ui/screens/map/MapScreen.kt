package com.universalwill.sportoss.ui.screens.map

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.ui.theme.dimensions
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.layers.LocationIndicatorLayer
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultHeadingProvider
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberLocationState
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState

internal const val RECORDING_PANEL_HEIGHT_DP = 320

@Composable
fun MapScreen(
    state: MapUiState,
    onAction: (MapAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hasCenteredInitially by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var panelHeight by remember { mutableStateOf(RECORDING_PANEL_HEIGHT_DP.dp) }
    val density = LocalDensity.current
    val locationState = rememberLocationState(
        provider = rememberDefaultLocationProvider(),
        headingProvider = rememberDefaultHeadingProvider(),
    )
    val mapState = rememberMapState(baseStyle = stadiaOutdoorsStyle()) {
        val currentMapState = checkNotNull(LocalMapState.current)

        LocationIndicatorLayer(
            id = "user-location",
            locationState = locationState,
        )
        LocationTrackingEffect(
            locationState = locationState,
            enabled = !hasCenteredInitially,
        ) {
            currentMapState.animateCamera(
                CameraPosition(
                    center = currentLocation.position,
                    zoom = 15.0,
                ).toCameraUpdate(),
                animation = CameraAnimation.Fly.Standard,
            )
            hasCenteredInitially = true
        }
    }
    val styleLoadState = mapState.style.loadState
    val appLanguageTag = LocalConfiguration.current.locales[0].language
    val mapLanguageTag = state.mapLabelLanguage.resolveLanguageTag(appLanguageTag)

    LaunchedEffect(styleLoadState, mapLanguageTag) {
        if (styleLoadState == StyleLoadState.Ready) {
            mapState.localizeLabels(mapLanguageTag)
        }
    }

    BoxWithConstraints(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            viewportInsets = PaddingValues(bottom = panelHeight),
        ) {
            SportOSSMapOverlay(
                onLocationClick = {
                    val position = locationState.lastLocation?.position
                    if (position == null) {
                        locationState.requestPermission()
                    } else {
                        coroutineScope.launch {
                            mapState.animateCamera(
                                CameraPosition(
                                    center = position,
                                    zoom = maxOf(mapState.cameraPosition.zoom, 15.0),
                                ).toCameraUpdate(),
                                animation = CameraAnimation.Fly.Standard,
                            )
                        }
                    }
                },
            )
        }

        RecordingPanel(
            state = state,
            onAction = onAction,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .heightIn(max = maxHeight)
                .onSizeChanged { panelHeight = with(density) { it.height.toDp() } }
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = MaterialTheme.dimensions.spacingMedium,
                    vertical = MaterialTheme.dimensions.spacingMedium,
                ),
        )
    }
}
