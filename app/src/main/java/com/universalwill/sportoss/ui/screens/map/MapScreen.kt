package com.universalwill.sportoss.ui.screens.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.universalwill.sportoss.BuildConfig
import com.universalwill.sportoss.ui.theme.SportOSSTheme
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultHeadingProvider
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberLocationState
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.style.BaseStyle

private const val MAP_LANGUAGE = "ru"
private const val MAP_STYLE_ID = "outdoors"

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
        LocationTrackingEffect(locationState = locationState) {
            currentMapState.animateCameraPosition(
                CameraPosition(
                    target = currentLocation.position,
                    zoom = 15.0,
                )
            )
        }
    }
    val styleLoadState = mapState.style.loadState

    LaunchedEffect(styleLoadState) {
        if (styleLoadState == StyleLoadState.Ready) {
            mapState.localizeLabels(MAP_LANGUAGE)
        }
    }

    MaplibreMap(
        modifier = modifier,
        state = mapState,
    )
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
        MapScreen()
    }
}
