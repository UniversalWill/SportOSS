package com.universalwill.sportoss.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.maplibre.compose.material3.AttributionLinks
import org.maplibre.compose.material3.DisappearingCompassButton
import org.maplibre.compose.material3.DisappearingScaleBar
import org.maplibre.compose.material3.ExpandingAttributionButton
import org.maplibre.compose.material3.ZoomButtons
import org.maplibre.compose.overlay.AttributionDefaults
import org.maplibre.compose.overlay.AttributionStyle
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.overlay.LocalCameraPadding
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.overlay.MaplibreLogo

internal const val LOCATION_CONTROL_STACK_OFFSET_DP = 56
internal val MapOverlayContainerColor = Color(0xE61B1B1F)
internal val MapOverlayContentColor = Color.White

@Composable
internal fun SportOSSMapOverlay(
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mapState = checkNotNull(LocalMapState.current)
    val buttonColors = ButtonDefaults.elevatedButtonColors(
        containerColor = MapOverlayContainerColor,
        contentColor = MapOverlayContentColor,
    )
    val attributionStyle = AttributionStyle(
        containerColor = MapOverlayContainerColor,
        contentColor = MapOverlayContentColor,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MapOverlayContentColor),
        shadowElevation = 3.dp,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(LocalCameraPadding.current)
            .padding(MapOverlay.Spacing),
    ) {
        DisappearingScaleBar(
            metersPerDp = mapState.viewport?.metersPerDpAtTarget ?: 0.0,
            zoom = mapState.cameraPosition.zoom,
            modifier = Modifier.align(Alignment.TopStart),
            color = MapOverlayContentColor,
            haloColor = Color.Black,
            haloWidth = 1.dp,
        )
        DisappearingCompassButton(
            modifier = Modifier.align(Alignment.TopEnd),
            colors = buttonColors,
        )
        ZoomButtons(
            modifier = Modifier.align(Alignment.CenterEnd),
            colors = buttonColors,
            dividerColor = MapOverlayContentColor.copy(alpha = 0.24f),
        )
        MaplibreLogo(modifier = Modifier.align(Alignment.BottomStart))
        ExpandingAttributionButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            toggleButton = { onClick ->
                IconButton(onClick = onClick) {
                    Icon(
                        painter = AttributionDefaults.icon(),
                        contentDescription = AttributionDefaults.contentDescription(),
                        tint = MapOverlayContentColor,
                    )
                }
            },
            expandedContent = { attributions, textStyle ->
                AttributionLinks(
                    attributions = attributions,
                    textStyle = textStyle,
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            color = MapOverlayContentColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                )
            },
            expandedStyle = attributionStyle,
            collapsedStyle = attributionStyle,
        )
        LocationButton(
            onClick = onLocationClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = LOCATION_CONTROL_STACK_OFFSET_DP.dp),
            containerColor = MapOverlayContainerColor,
            contentColor = MapOverlayContentColor,
        )
    }
}
