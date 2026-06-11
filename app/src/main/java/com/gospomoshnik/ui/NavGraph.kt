package com.gospomoshnik.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gospomoshnik.ui.chat.ChatScreen
import com.gospomoshnik.ui.document.DocumentScreen
import com.gospomoshnik.ui.legal.LegalScreen
import com.gospomoshnik.ui.library.DocViewerScreen
import com.gospomoshnik.ui.library.LibraryScreen
import com.gospomoshnik.ui.main.MainScreen
import com.gospomoshnik.ui.onboarding.OnboardingScreen
import com.gospomoshnik.ui.paywall.PaywallScreen
import com.gospomoshnik.ui.profile.ProfileScreen
import com.gospomoshnik.ui.settings.SettingsScreen
import com.gospomoshnik.ui.settings.SettingsViewModel

sealed class Screen(val route: String) {
    object Main     : Screen("main")
    object Chat     : Screen("chat/{category}?sessionId={sessionId}&question={question}&docId={docId}") {
        fun createRoute(category: String, sessionId: Long = 0L, question: String = "", docId: String = "") =
            "chat/$category?sessionId=$sessionId&question=${android.net.Uri.encode(question)}&docId=$docId"
    }
    object Document : Screen("document/{sessionId}") {
        fun createRoute(sessionId: Long) = "document/$sessionId"
    }
    object Paywall  : Screen("paywall")
    object Profile  : Screen("profile")
    object Settings : Screen("settings")
    object Legal    : Screen("legal/{doc}") {
        fun createRoute(doc: String) = "legal/$doc"
    }
    object Library  : Screen("library")
    object DocView  : Screen("docview/{docId}") {
        fun createRoute(docId: String) = "docview/$docId"
    }
    object Onboarding : Screen("onboarding")
}

private const val ANIM = 280

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        // Плавные переходы между экранами
        enterTransition = {
            slideIntoContainer(SlideDirection.Left, tween(ANIM)) + fadeIn(tween(ANIM))
        },
        exitTransition = {
            slideOutOfContainer(SlideDirection.Left, tween(ANIM)) + fadeOut(tween(ANIM))
        },
        popEnterTransition = {
            slideIntoContainer(SlideDirection.Right, tween(ANIM)) + fadeIn(tween(ANIM))
        },
        popExitTransition = {
            slideOutOfContainer(SlideDirection.Right, tween(ANIM)) + fadeOut(tween(ANIM))
        }
    ) {

        composable(Screen.Onboarding.route) {
            val settingsVm: SettingsViewModel = hiltViewModel()
            OnboardingScreen(
                onFinish = {
                    settingsVm.completeOnboarding()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

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
                },
                onLibraryClick = {
                    navController.navigate(Screen.Library.route)
                }
            )
        }

        composable(
            route     = Screen.Chat.route,
            arguments = listOf(
                navArgument("category")  { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("question")  { type = NavType.StringType; defaultValue = "" },
                navArgument("docId")     { type = NavType.StringType; defaultValue = "" }
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
            SettingsScreen(
                onBack       = { navController.popBackStack() },
                onOpenLegal  = { doc -> navController.navigate(Screen.Legal.createRoute(doc)) }
            )
        }

        composable(
            route     = Screen.Legal.route,
            arguments = listOf(navArgument("doc") { type = NavType.StringType })
        ) { backStack ->
            LegalScreen(
                doc    = backStack.arguments?.getString("doc") ?: "terms",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onOpenDoc = { docId -> navController.navigate(Screen.DocView.createRoute(docId)) },
                onBack    = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.DocView.route,
            arguments = listOf(navArgument("docId") { type = NavType.StringType })
        ) { backStack ->
            val docId = backStack.arguments?.getString("docId") ?: ""
            DocViewerScreen(
                docId       = docId,
                onAskInChat = { category, question ->
                    navController.navigate(Screen.Chat.createRoute(category, question = question, docId = docId))
                },
                onBack      = { navController.popBackStack() }
            )
        }
    }
}
