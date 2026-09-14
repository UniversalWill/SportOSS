package com.universalwill.sportoss.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.universalwill.sportoss.ui.navigation.SportOSSNavigation
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Composable
fun SportOSSApp() {
    SportOSSTheme {
        SportOSSNavigation(modifier = Modifier.fillMaxSize())
    }
}
