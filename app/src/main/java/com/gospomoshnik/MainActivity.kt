package com.gospomoshnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.gospomoshnik.ui.NavGraph
import com.gospomoshnik.ui.Screen
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
            val loaded by settingsViewModel.firstLoaded.collectAsState()

            GospomoshnikTheme(settings = settings) {
                // Ждём загрузки настроек, чтобы не мигнуть онбордингом возвращающемуся пользователю
                if (loaded) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController    = navController,
                        startDestination = if (settings.onboardingDone) Screen.Main.route
                                           else Screen.Onboarding.route
                    )
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                }
            }
        }
    }
}
