package com.gospomoshnik.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gospomoshnik.ui.chat.ChatScreen
import com.gospomoshnik.ui.document.DocumentScreen
import com.gospomoshnik.ui.main.MainScreen
import com.gospomoshnik.ui.paywall.PaywallScreen
import com.gospomoshnik.ui.profile.ProfileScreen
import com.gospomoshnik.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Main     : Screen("main")
    object Chat     : Screen("chat/{category}?sessionId={sessionId}") {
        fun createRoute(category: String, sessionId: Long = 0L) =
            "chat/$category?sessionId=$sessionId"
    }
    object Document : Screen("document/{sessionId}") {
        fun createRoute(sessionId: Long) = "document/$sessionId"
    }
    object Paywall  : Screen("paywall")
    object Profile  : Screen("profile")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Main.route
    ) {

        composable(Screen.Main.route) {
            MainScreen(
                onCategoryClick = { category ->
                    navController.navigate(Screen.Chat.createRoute(category))
                },
                onHistoryItemClick = { category, sessionId ->
                    navController.navigate(Screen.Chat.createRoute(category, sessionId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(
            route     = Screen.Chat.route,
            arguments = listOf(
                navArgument("category")  { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStack ->
            val category = backStack.arguments?.getString("category") ?: "general"
            ChatScreen(
                category = category,
                onGenerateDocument = { sessionId ->
                    navController.navigate(Screen.Document.createRoute(sessionId))
                },
                onPaywallRequired = {
                    navController.navigate(Screen.Paywall.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.Document.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            DocumentScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Paywall.route) {
            PaywallScreen(
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onUpgradeClick  = { navController.navigate(Screen.Paywall.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onBack          = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
