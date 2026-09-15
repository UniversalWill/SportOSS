package com.universalwill.sportoss.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.R
import com.universalwill.sportoss.ui.screens.activities.ActivitiesRoute
import com.universalwill.sportoss.ui.screens.map.MapRoute
import com.universalwill.sportoss.ui.screens.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object MapDestination : NavKey

@Serializable
data object ActivitiesDestination : NavKey

@Serializable
data object SettingsDestination : NavKey

private enum class TopLevelTab {
    Activities,
    Map,
    Settings,
}

private data class TopLevelDestination(
    val tab: TopLevelTab,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        tab = TopLevelTab.Activities,
        labelResId = R.string.tab_activities,
        icon = Icons.AutoMirrored.Filled.List,
    ),
    TopLevelDestination(
        tab = TopLevelTab.Map,
        labelResId = R.string.tab_map,
        icon = Icons.Filled.LocationOn,
    ),
    TopLevelDestination(
        tab = TopLevelTab.Settings,
        labelResId = R.string.tab_settings,
        icon = Icons.Filled.Settings,
    ),
)

@Composable
fun SportOSSNavigation(modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableStateOf(TopLevelTab.Activities) }
    var requestedWorkoutType by rememberSaveable { mutableStateOf<WorkoutType?>(null) }
    val activitiesBackStack = rememberNavBackStack(ActivitiesDestination)
    val mapBackStack = rememberNavBackStack(MapDestination)
    val settingsBackStack = rememberNavBackStack(SettingsDestination)
    val entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        rememberViewModelStoreNavEntryDecorator<NavKey>(),
    )

    val selectedTabIsAtRoot = when (selectedTab) {
        TopLevelTab.Activities -> activitiesBackStack.size == 1
        TopLevelTab.Map -> mapBackStack.size == 1
        TopLevelTab.Settings -> settingsBackStack.size == 1
    }

    BackHandler(enabled = selectedTab != TopLevelTab.Activities && selectedTabIsAtRoot) {
        selectedTab = TopLevelTab.Activities
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            SportOSSBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        when (selectedTab) {
            TopLevelTab.Activities -> NavDisplay(
                backStack = activitiesBackStack,
                entryDecorators = entryDecorators,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                onBack = { activitiesBackStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<ActivitiesDestination> {
                        ActivitiesRoute(
                            modifier = Modifier.fillMaxSize(),
                            onStartActivity = { workoutType ->
                                requestedWorkoutType = workoutType
                                selectedTab = TopLevelTab.Map
                            },
                        )
                    }
                },
            )

            TopLevelTab.Map -> NavDisplay(
                backStack = mapBackStack,
                entryDecorators = entryDecorators,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                onBack = {
                    if (mapBackStack.size > 1) {
                        mapBackStack.removeLastOrNull()
                    } else {
                        selectedTab = TopLevelTab.Activities
                    }
                },
                entryProvider = entryProvider {
                    entry<MapDestination> {
                        MapRoute(
                            requestedWorkoutType = requestedWorkoutType,
                            onWorkoutTypeRequestHandled = { requestedWorkoutType = null },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            )

            TopLevelTab.Settings -> NavDisplay(
                backStack = settingsBackStack,
                entryDecorators = entryDecorators,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                onBack = {
                    if (settingsBackStack.size > 1) {
                        settingsBackStack.removeLastOrNull()
                    } else {
                        selectedTab = TopLevelTab.Activities
                    }
                },
                entryProvider = entryProvider {
                    entry<SettingsDestination> {
                        SettingsRoute(modifier = Modifier.fillMaxSize())
                    }
                },
            )
        }
    }
}

@Composable
private fun SportOSSBottomBar(
    selectedTab: TopLevelTab,
    onTabSelected: (TopLevelTab) -> Unit,
) {
    NavigationBar {
        topLevelDestinations.forEach { destination ->
            val label = stringResource(destination.labelResId)
            NavigationBarItem(
                selected = selectedTab == destination.tab,
                onClick = { onTabSelected(destination.tab) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}
