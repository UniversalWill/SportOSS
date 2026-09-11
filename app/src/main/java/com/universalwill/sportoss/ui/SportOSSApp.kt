package com.universalwill.sportoss.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.universalwill.sportoss.ui.navigation.SportOSSNavigation
import com.universalwill.sportoss.ui.theme.SportOSSTheme

@Composable
fun SportOSSApp() {
    SportOSSTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SportOSSNavigation(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    }
}
