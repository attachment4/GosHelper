package com.gospomoshnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.gospomoshnik.ui.NavGraph
import com.gospomoshnik.ui.theme.GospomoshnikTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GospomoshnikTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
