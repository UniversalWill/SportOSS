package com.universalwill.sportoss.ui.screens.workoutdetails

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.BuildConfig
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.model.MapLabelLanguage
import com.universalwill.sportoss.domain.model.WorkoutRoutePoint
import com.universalwill.sportoss.ui.screens.map.localizeLabels
import com.universalwill.sportoss.ui.screens.map.resolveLanguageTag
import com.universalwill.sportoss.ui.screens.map.stadiaOutdoorsStyle
import com.universalwill.sportoss.ui.theme.dimensions
import kotlin.math.log2
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.AndroidRenderMode
import org.maplibre.compose.map.MapEvent
import org.maplibre.compose.map.MapUiOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.overlay.include
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@Composable
internal fun WorkoutRouteMap(
    route: List<List<WorkoutRoutePoint>>,
    modifier: Modifier = Modifier,
    mapLabelLanguage: MapLabelLanguage = MapLabelLanguage.APPLICATION,
) {
    val geometry = remember(route) { WorkoutRouteGeometry(route) }
    val colors = MaterialTheme.colorScheme
    val routeDescription = stringResource(R.string.workout_route_map)
    val spacing = MaterialTheme.dimensions
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.spacingSmall)) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.35f)
                .clip(MaterialTheme.shapes.large)
                .background(colors.surfaceContainer)
                .semantics { contentDescription = routeDescription },
        ) {
            Box(
                modifier = Modifier.matchParentSize().drawRoute(
                    geometry, colors.primary, colors.surfaceContainer,
                ),
            )
            if (!LocalInspectionMode.current && BuildConfig.STADIA_API_KEY.isNotBlank() && !geometry.crossesAntimeridian) {
                val width = (maxWidth - RoutePadding * 2).value.toDouble().coerceAtLeast(1.0)
                val height = (maxHeight - RoutePadding * 2).value.toDouble().coerceAtLeast(1.0)
                val camera = CameraPosition(
                    center = Position(geometry.centerLongitude, geometry.centerLatitude),
                    zoom = log2(geometry.fitScale(width, height) / MAP_TILE_SIZE).coerceIn(0.0, 17.0),
                )
                WorkoutBasemap(
                    route, camera, colors.primary, colors.surfaceContainer, mapLabelLanguage,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.spacingLarge)) {
            RouteLegendItem(stringResource(R.string.workout_route_start), isStart = true)
            RouteLegendItem(stringResource(R.string.workout_route_finish), isStart = false)
        }
    }
}

@Composable
private fun WorkoutBasemap(
    route: List<List<WorkoutRoutePoint>>,
    camera: CameraPosition,
    routeColor: Color,
    contrastColor: Color,
    mapLabelLanguage: MapLabelLanguage,
    modifier: Modifier = Modifier,
) {
    val lines = remember(route) {
        GeoJsonData.Features(MultiLineString(route.filter { it.size > 1 }.map { segment ->
            segment.map(WorkoutRoutePoint::toPosition)
        }))
    }
    val dots = remember(route) {
        GeoJsonData.Features(MultiPoint(route.filter { it.size == 1 }.map { it.single().toPosition() }))
    }
    val start = remember(route) { GeoJsonData.Features(Point(route.first().first().toPosition())) }
    val finish = remember(route) { GeoJsonData.Features(Point(route.last().last().toPosition())) }
    val state = rememberMapState(baseStyle = stadiaOutdoorsStyle(), initialCameraPosition = camera) {
        val lineSource = rememberGeoJsonSource(lines)
        LineLayer("workout-route-outline", lineSource, color = const(contrastColor), width = const(7.dp))
        LineLayer("workout-route-line", lineSource, color = const(routeColor), width = const(4.dp))
        CircleLayer("workout-route-dots", rememberGeoJsonSource(dots), radius = const(2.dp), color = const(routeColor))
        CircleLayer(
            "workout-route-start", rememberGeoJsonSource(start), radius = const(7.dp),
            color = const(contrastColor), strokeColor = const(routeColor), strokeWidth = const(2.dp),
        )
        CircleLayer(
            "workout-route-finish", rememberGeoJsonSource(finish), radius = const(4.dp),
            color = const(routeColor), strokeColor = const(contrastColor), strokeWidth = const(1.dp),
        )
    }
    var hasSourceError by remember(state, route) { mutableStateOf(false) }
    val ready = state.style.loadState == StyleLoadState.Ready
    val showBasemap = ready && !hasSourceError
    val language = mapLabelLanguage.resolveLanguageTag(LocalConfiguration.current.locales[0].language)
    LaunchedEffect(state, camera) { state.setCameraPosition(camera) }
    LaunchedEffect(state, ready, language) {
        if (ready) state.localizeLabels(language)
    }
    LaunchedEffect(state) {
        state.events.collect { event ->
            if (event is MapEvent.SourceDataFailed) hasSourceError = true
        }
    }
    MaplibreMap(
        modifier = modifier.alpha(if (showBasemap) 1f else 0f),
        state = state,
        uiOptions = StaticMapUiOptions,
        overlay = { if (showBasemap) include(MapOverlay.AttributionOnly) },
    )
}

private fun WorkoutRoutePoint.toPosition() = Position(
    longitude, latitude.coerceIn(-MAX_MERCATOR_LATITUDE, MAX_MERCATOR_LATITUDE),
)

private fun Modifier.drawRoute(geometry: WorkoutRouteGeometry, routeColor: Color, background: Color): Modifier =
    drawWithCache {
        val availableWidth = (size.width - RoutePadding.toPx() * 2).coerceAtLeast(1f)
        val availableHeight = (size.height - RoutePadding.toPx() * 2).coerceAtLeast(1f)
        val scale = geometry.fitScale(availableWidth.toDouble(), availableHeight.toDouble())
        fun ProjectedRoutePoint.toOffset() = Offset(
            x = size.width / 2 + ((x - geometry.center.x) * scale).toFloat(),
            y = size.height / 2 + ((y - geometry.center.y) * scale).toFloat(),
        )
        val path = Path().apply {
            geometry.segments.forEach { segment ->
                val start = segment.first().toOffset()
                moveTo(start.x, start.y)
                for (index in 1 until segment.size) {
                    val next = segment[index].toOffset()
                    lineTo(next.x, next.y)
                }
            }
        }
        val dots = geometry.segments.filter { it.size == 1 }.map { it.single().toOffset() }
        val start = geometry.segments.first().first().toOffset()
        val finish = geometry.segments.last().last().toOffset()
        onDrawBehind {
            drawPath(path, routeColor, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            dots.forEach { drawCircle(routeColor, radius = 2.dp.toPx(), center = it) }
            drawCircle(background, radius = 7.dp.toPx(), center = start)
            drawCircle(routeColor, radius = 7.dp.toPx(), center = start, style = Stroke(2.dp.toPx()))
            drawCircle(background, radius = 5.dp.toPx(), center = finish)
            drawCircle(routeColor, radius = 4.dp.toPx(), center = finish)
        }
    }

@Composable
private fun RouteLegendItem(label: String, isStart: Boolean, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingExtraSmall),
    ) {
        Canvas(Modifier.size(16.dp)) {
            if (isStart) drawCircle(color, radius = 6.dp.toPx(), style = Stroke(2.dp.toPx()))
            else drawCircle(color, radius = 4.dp.toPx())
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val RoutePadding = 28.dp
private const val MAP_TILE_SIZE = 512.0
private val StaticMapUiOptions = MapUiOptions(MapUiOptions.None) { renderMode = AndroidRenderMode.Texture }
