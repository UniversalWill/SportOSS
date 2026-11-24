package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.BuildConfig
import org.maplibre.compose.style.BaseStyle

internal fun stadiaOutdoorsStyle() = BaseStyle.Uri(
    "https://tiles.stadiamaps.com/styles/outdoors.json?api_key=${BuildConfig.STADIA_API_KEY}",
)
