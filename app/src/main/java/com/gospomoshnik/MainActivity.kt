package com.gospomoshnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.gospomoshnik.ui.NavGraph
import com.gospomoshnik.ui.settings.SettingsViewModel
import com.gospomoshnik.ui.theme.GospomoshnikTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()

            GospomoshnikTheme(settings = settings) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
