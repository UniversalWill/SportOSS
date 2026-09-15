package com.universalwill.sportoss.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class SportOSSDimensions(
    val spacingExtraSmall: Dp = 4.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingMedium: Dp = 12.dp,
    val spacingLarge: Dp = 16.dp,
    val spacingExtraLarge: Dp = 24.dp,
    val spacingHuge: Dp = 32.dp,
    val iconLarge: Dp = 32.dp,
)

internal val DefaultSportOSSDimensions = SportOSSDimensions()

internal val LocalSportOSSDimensions = staticCompositionLocalOf {
    DefaultSportOSSDimensions
}

val MaterialTheme.dimensions: SportOSSDimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalSportOSSDimensions.current
