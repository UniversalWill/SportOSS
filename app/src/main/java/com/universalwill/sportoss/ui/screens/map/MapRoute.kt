package com.universalwill.sportoss.ui.screens.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwill.sportoss.domain.enums.WorkoutType
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect

@Composable
fun MapRoute(
    requestedWorkoutType: WorkoutType?,
    onWorkoutTypeRequestHandled: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var notificationRequested by rememberSaveable { mutableStateOf(false) }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.onAction(MapAction.UiVisible(true))
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onAction(MapAction.ToggleRecording)
    }
    LifecycleStartEffect(Unit) {
        viewModel.onAction(MapAction.UiVisible(true))
        onStopOrDispose { viewModel.onAction(MapAction.UiVisible(false)) }
    }

    LaunchedEffect(requestedWorkoutType) {
        requestedWorkoutType?.let { workoutType ->
            viewModel.onAction(MapAction.SelectWorkoutType(workoutType))
            onWorkoutTypeRequestHandled()
        }
    }

    MapScreen(
        state = state,
        onAction = { action ->
            when (action) {
                MapAction.RequestLocationPermission -> locationPermission.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                ))
                MapAction.OpenLocationSettings -> context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                MapAction.OpenAppSettings -> context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)))
                MapAction.ToggleRecording -> {
                    if (state.canToggle && state.recordingState != RecordingState.Recording &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationRequested &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED) {
                        notificationRequested = true
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.onAction(action)
                    }
                }
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}
