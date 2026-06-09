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

sealed class Screen(val route: String) {
    object Main     : Screen("main")
    object Chat     : Screen("chat/{category}") {
        fun createRoute(category: String) = "chat/$category"
    }
    object Document : Screen("document/{sessionId}") {
        fun createRoute(sessionId: Long) = "document/$sessionId"
    }
    object Paywall  : Screen("paywall")
    object Profile  : Screen("profile")
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
                onHistoryItemClick = { sessionId ->
                    navController.navigate(Screen.Chat.createRoute("session/$sessionId"))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(
            route     = Screen.Chat.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
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
                onUpgradeClick = { navController.navigate(Screen.Paywall.route) },
                onBack         = { navController.popBackStack() }
            )
        }
    }
}
