package com.apptimemachine.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.apptimemachine.app.ui.screens.appdetails.AppDetailsScreen
import com.apptimemachine.app.ui.screens.dashboard.DashboardScreen
import com.apptimemachine.app.ui.screens.installedapps.InstalledAppsScreen
import com.apptimemachine.app.ui.screens.timeline.TimelineScreen

/**
 * Wires the four fully-built screens (Dashboard, Installed Apps, Timeline,
 * App Details). Routes for Splash, Onboarding, Permission Setup, Statistics,
 * Reports, Compare Apps, Search, Filters, Favorites, Export, Backup &
 * Restore, Settings, and About are reserved in Screen.kt — add a
 * `composable(Screen.X.route) { ... }` block per screen following the
 * pattern below as each is built. Splash/Onboarding/PermissionSetup should
 * become the real startDestination once implemented, gating entry to
 * Dashboard behind UserPreferencesRepository.isOnboardingComplete /
 * isMonitoringActive.
 */
@Composable
fun AppTimeMachineNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAppClick = { packageName -> navController.navigate(Screen.AppDetails.createRoute(packageName)) },
                onSeeAllInstalled = { navController.navigate(Screen.InstalledApps.route) },
                onSeeTimeline = { navController.navigate(Screen.Timeline.route) }
            )
        }

        composable(Screen.InstalledApps.route) {
            InstalledAppsScreen(
                onAppClick = { packageName -> navController.navigate(Screen.AppDetails.createRoute(packageName)) }
            )
        }

        composable(Screen.Timeline.route) {
            TimelineScreen(onAppClick = { packageName -> navController.navigate(Screen.AppDetails.createRoute(packageName)) })
        }

        composable(
            route = Screen.AppDetails.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName").orEmpty()
            AppDetailsScreen(packageName = packageName, onBack = { navController.popBackStack() })
        }
    }
}
