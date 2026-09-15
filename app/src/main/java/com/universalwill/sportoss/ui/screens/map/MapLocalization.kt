package com.universalwill.sportoss.ui.screens.map

import com.universalwill.sportoss.domain.model.MapLabelLanguage
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.maplibre.compose.map.MapState

private val localizedSourceLayers = setOf(
    "place",
    "poi",
    "park",
    "mountain_peak",
    "water_name",
    "transportation_name",
    "airport",
)

internal fun MapLabelLanguage.resolveLanguageTag(appLanguageTag: String): String =
    languageTag ?: if (appLanguageTag == "ru") "ru" else "en"

internal suspend fun MapState.localizeLabels(language: String) {
    val localizedName = localizedNameExpression(language)

    for (layer in style.layers) {
        if (
            layer.type == "symbol" &&
            layer.sourceLayer in localizedSourceLayers &&
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
