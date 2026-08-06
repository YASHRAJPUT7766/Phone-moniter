package com.apptimemachine.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Current-state snapshot for one package. History lives in the separate
 * *_snapshots / timeline_events tables below, keyed by [packageName].
 *
 * [firstInstalledAt] / [lastUpdatedAt] come straight from Android
 * (PackageInfo.firstInstallTime / lastUpdateTime) and are trustworthy even
 * for apps that predate this app being installed.
 *
 * [monitoringStartedAt], by contrast, is OUR timestamp: the moment
 * AppScanWorker first observed this package on THIS device. It is set
 * exactly once, on first insert, and never overwritten. Every screen that
 * renders history must treat [monitoringStartedAt] as the hard floor of
 * what we actually know — nothing before it is real, and nothing before
 * it should ever be displayed as a timeline event. This is the field that
 * enforces the "never generate fake past history" rule at the data layer.
 */
@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val developerName: String?,
    val category: String?,
    val iconPath: String?,
    val firstInstalledAt: Long,   // from Android — trustworthy even pre-monitoring
    val lastUpdatedAt: Long,      // from Android — trustworthy even pre-monitoring
    val monitoringStartedAt: Long, // ours — set once, never overwritten
    val currentVersionName: String,
    val currentVersionCode: Long,
    val currentApkSizeBytes: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val isSystemApp: Boolean,
    val isFavorite: Boolean = false,
    /** null while installed; set when an uninstall broadcast is observed */
    val uninstalledAt: Long? = null
) {
    /** True if we were monitoring before Android's own firstInstallTime — i.e. we
     *  witnessed the actual install rather than discovering a pre-existing app. */
    val sawActualInstall: Boolean get() = monitoringStartedAt <= firstInstalledAt
}
