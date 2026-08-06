package com.apptimemachine.app.ui.navigation

/** Central route registry — one entry per spec screen. */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object PermissionSetup : Screen("permission_setup")
    data object Dashboard : Screen("dashboard")
    data object InstalledApps : Screen("installed_apps")
    data object Timeline : Screen("timeline")
    data object AppDetails : Screen("app_details/{packageName}") {
        fun createRoute(packageName: String) = "app_details/$packageName"
    }
    data object Statistics : Screen("statistics")
    data object Reports : Screen("reports")
    data object CompareApps : Screen("compare_apps")
    data object Search : Screen("search")
    data object Filters : Screen("filters")
    data object Favorites : Screen("favorites")
    data object Export : Screen("export")
    data object BackupRestore : Screen("backup_restore")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
