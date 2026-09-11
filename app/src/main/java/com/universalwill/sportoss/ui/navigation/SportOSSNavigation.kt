package com.universalwill.sportoss.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.universalwill.sportoss.ui.screens.map.MapScreen
import kotlinx.serialization.Serializable

@Serializable
data object MapRoute : NavKey

@Composable
fun SportOSSNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(MapRoute)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<MapRoute> {
                MapScreen(modifier = Modifier.fillMaxSize())
            }
        },
    )
}
