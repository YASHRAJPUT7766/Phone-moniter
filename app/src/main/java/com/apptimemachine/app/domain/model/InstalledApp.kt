package com.apptimemachine.app.domain.model

/** UI/domain-facing model — mapped from InstalledAppEntity, decoupled from Room. */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val developerName: String?,
    val category: String?,
    val iconPath: String?,
    val firstInstalledAt: Long,
    val lastUpdatedAt: Long,
    val monitoringStartedAt: Long,
    val currentVersionName: String,
    val currentVersionCode: Long,
    val currentApkSizeBytes: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val isSystemApp: Boolean,
    val isFavorite: Boolean,
    val uninstalledAt: Long?
) {
    val isUninstalled: Boolean get() = uninstalledAt != null
    /** True if this app existed before App Time Machine started watching it —
     *  drives the UI hint "history shown only from <date>" on App Details. */
    val existedBeforeMonitoring: Boolean get() = firstInstalledAt < monitoringStartedAt
}
