package com.apptimemachine.app.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.apptimemachine.app.data.local.dao.InstalledAppDao
import com.apptimemachine.app.data.local.entity.InstalledAppEntity
import com.apptimemachine.app.data.local.entity.TimelineEventType
import com.apptimemachine.app.domain.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val SOURCE_API = "PackageManager"

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val installedAppDao: InstalledAppDao,
    private val timelineRepository: TimelineRepository
) : AppRepository {

    override fun observeInstalledApps(): Flow<List<InstalledApp>> =
        installedAppDao.observeInstalledApps().map { list -> list.map { it.toDomain() } }

    override fun observeRemovedApps(): Flow<List<InstalledApp>> =
        installedAppDao.observeRemovedApps().map { list -> list.map { it.toDomain() } }

    override fun observeApp(packageName: String): Flow<InstalledApp?> =
        installedAppDao.observeApp(packageName).map { it?.toDomain() }

    override fun observeRecentlyUpdated(limit: Int): Flow<List<InstalledApp>> =
        installedAppDao.observeRecentlyUpdated(limit).map { list -> list.map { it.toDomain() } }

    override fun observeNewlyInstalledSinceMonitoring(limit: Int): Flow<List<InstalledApp>> =
        installedAppDao.observeNewlyInstalledSinceMonitoring(limit).map { list -> list.map { it.toDomain() } }

    override fun observeInstalledCount(): Flow<Int> = installedAppDao.observeInstalledCount()

    override fun observeAppsFirstMonitoredToday(startOfDayMillis: Long, endOfDayMillis: Long): Flow<Int> =
        installedAppDao.observeAppsFirstMonitoredToday(startOfDayMillis, endOfDayMillis)

    override suspend fun setFavorite(packageName: String, isFavorite: Boolean) =
        installedAppDao.setFavorite(packageName, isFavorite)

    override suspend fun refreshFromSystem() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val now = System.currentTimeMillis()

        // Real, live read of every installed package. GET_META_DATA is enough for
        // everything we display; heavier flags (GET_PERMISSIONS etc.) are fetched
        // separately by the permission-tracking path so this scan stays cheap.
        val packages: List<PackageInfo> = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        val seenPackageNames = mutableSetOf<String>()

        for (pkgInfo in packages) {
            val packageName = pkgInfo.packageName
            seenPackageNames += packageName

            val appInfo: ApplicationInfo = pkgInfo.applicationInfo ?: continue
            val appName = pm.getApplicationLabel(appInfo).toString()
            val apkSize = runCatching { java.io.File(appInfo.sourceDir).length() }.getOrDefault(0L)
            @Suppress("DEPRECATION")
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                pkgInfo.versionCode.toLong()
            }
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            val existing = installedAppDao.getApp(packageName)

            if (existing == null) {
                // First time we've ever seen this package on this device. "now" is the
                // true first-observation time — never backdated to Android's own
                // firstInstallTime, per the app's core rule.
                installedAppDao.upsert(
                    InstalledAppEntity(
                        packageName = packageName,
                        appName = appName,
                        developerName = null, // not reliably exposed by PackageManager; left null rather than guessed
                        category = categoryLabel(appInfo.category),
                        iconPath = null, // icons are drawn live from PackageManager in the UI, not persisted
                        firstInstalledAt = pkgInfo.firstInstallTime,
                        lastUpdatedAt = pkgInfo.lastUpdateTime,
                        monitoringStartedAt = now,
                        currentVersionName = pkgInfo.versionName ?: "",
                        currentVersionCode = versionCode,
                        currentApkSizeBytes = apkSize,
                        targetSdk = appInfo.targetSdkVersion,
                        minSdk = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) appInfo.minSdkVersion else 0,
                        isSystemApp = isSystemApp,
                        uninstalledAt = null
                    )
                )
                timelineRepository.recordEvent(
                    packageName = packageName,
                    type = TimelineEventType.MONITORING_STARTED,
                    sourceApi = SOURCE_API,
                    description = "Started monitoring $appName",
                    newValue = pkgInfo.versionName
                )
            } else {
                // Package already tracked. Diff mutable fields against the stored
                // snapshot and write one timeline event per real change detected —
                // nothing here is estimated or backfilled.
                if (existing.uninstalledAt != null) {
                    // Reinstalled after being marked uninstalled: clear the flag,
                    // but monitoringStartedAt/firstInstalledAt stay untouched since
                    // we were already tracking this package's identity.
                    installedAppDao.update(existing.copy(uninstalledAt = null))
                }

                if (pkgInfo.versionName != existing.currentVersionName || versionCode != existing.currentVersionCode) {
                    timelineRepository.recordEvent(
                        packageName = packageName,
                        type = TimelineEventType.VERSION_UPDATED,
                        sourceApi = SOURCE_API,
                        description = "$appName updated to ${pkgInfo.versionName ?: versionCode}",
                        previousValue = existing.currentVersionName,
                        newValue = pkgInfo.versionName
                    )
                }

                if (pkgInfo.lastUpdateTime != existing.lastUpdatedAt && pkgInfo.lastUpdateTime != pkgInfo.firstInstallTime) {
                    timelineRepository.recordEvent(
                        packageName = packageName,
                        type = TimelineEventType.APK_UPDATED,
                        sourceApi = SOURCE_API,
                        description = "$appName APK was updated",
                        previousValue = existing.lastUpdatedAt.toString(),
                        newValue = pkgInfo.lastUpdateTime.toString()
                    )
                }

                if (apkSize != existing.currentApkSizeBytes && existing.currentApkSizeBytes > 0L) {
                    val delta = apkSize - existing.currentApkSizeBytes
                    timelineRepository.recordEvent(
                        packageName = packageName,
                        type = if (delta > 0) TimelineEventType.STORAGE_INCREASED else TimelineEventType.STORAGE_DECREASED,
                        sourceApi = SOURCE_API,
                        description = "$appName APK size changed",
                        previousValue = existing.currentApkSizeBytes.toString(),
                        newValue = apkSize.toString(),
                        differenceValue = "${if (delta > 0) "+" else ""}$delta bytes"
                    )
                }

                installedAppDao.update(
                    existing.copy(
                        appName = appName,
                        category = categoryLabel(appInfo.category),
                        lastUpdatedAt = pkgInfo.lastUpdateTime,
                        currentVersionName = pkgInfo.versionName ?: existing.currentVersionName,
                        currentVersionCode = versionCode,
                        currentApkSizeBytes = apkSize,
                        targetSdk = appInfo.targetSdkVersion,
                        isSystemApp = isSystemApp
                    )
                )
            }
        }

        // Anything we were tracking that PackageManager no longer reports has been
        // uninstalled. Mark it and record exactly one UNINSTALLED event — the row
        // itself is kept (not deleted) so its history remains visible.
        val trackedPackages = installedAppDao.observeInstalledApps().first()
        for (tracked in trackedPackages) {
            if (tracked.packageName !in seenPackageNames) {
                installedAppDao.markUninstalled(tracked.packageName, now)
                timelineRepository.recordEvent(
                    packageName = tracked.packageName,
                    type = TimelineEventType.UNINSTALLED,
                    sourceApi = SOURCE_API,
                    description = "${tracked.appName} was uninstalled"
                )
            }
        }
    }

    private fun categoryLabel(category: Int): String? = when (category) {
        ApplicationInfo.CATEGORY_GAME -> "Games"
        ApplicationInfo.CATEGORY_AUDIO -> "Media"
        ApplicationInfo.CATEGORY_VIDEO -> "Media"
        ApplicationInfo.CATEGORY_IMAGE -> "Media"
        ApplicationInfo.CATEGORY_SOCIAL -> "Social"
        ApplicationInfo.CATEGORY_NEWS -> "Media"
        ApplicationInfo.CATEGORY_MAPS -> "Tools"
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Tools"
        else -> null // ApplicationInfo.CATEGORY_UNDEFINED or unmapped — left null, never guessed
    }

    private fun InstalledAppEntity.toDomain() = InstalledApp(
        packageName = packageName,
        appName = appName,
        developerName = developerName,
        category = category,
        iconPath = iconPath,
        firstInstalledAt = firstInstalledAt,
        lastUpdatedAt = lastUpdatedAt,
        monitoringStartedAt = monitoringStartedAt,
        currentVersionName = currentVersionName,
        currentVersionCode = currentVersionCode,
        currentApkSizeBytes = currentApkSizeBytes,
        targetSdk = targetSdk,
        minSdk = minSdk,
        isSystemApp = isSystemApp,
        isFavorite = isFavorite,
        uninstalledAt = uninstalledAt
    )
}
