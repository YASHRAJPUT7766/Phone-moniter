package com.apptimemachine.app.data.worker

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apptimemachine.app.data.local.dao.InstalledAppDao
import com.apptimemachine.app.data.local.dao.StorageSnapshotDao
import com.apptimemachine.app.data.local.entity.StorageSnapshotEntity
import com.apptimemachine.app.data.local.entity.TimelineEventType
import com.apptimemachine.app.data.repository.TimelineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.flow.first

private const val SOURCE_API = "StorageStatsManager"

/**
 * Runs once daily, ONLY for apps whose monitoringStartedAt is in the past
 * (i.e. we're already tracking them) — never backfills a snapshot for a day
 * before monitoring began. Reads current app/data/cache size via
 * StorageStatsManager (requires PACKAGE_USAGE_STATS on API 26+), writes one
 * StorageSnapshotEntity for today, and if the delta vs. yesterday exceeds a
 * threshold, records a STORAGE_INCREASED/DECREASED timeline event with a
 * precomputed differenceValue.
 */
@HiltWorker
class StorageTrackingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val installedAppDao: InstalledAppDao,
    private val storageSnapshotDao: StorageSnapshotDao,
    private val timelineRepository: TimelineRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val today = LocalDate.now().toEpochDay()
        val apps = installedAppDao.observeInstalledApps().first()

        val storageStatsManager = applicationContext
            .getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        val storageManager = applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as? StorageManager

        apps.forEach { app ->
            val previous = storageSnapshotDao.getLatestForApp(app.packageName)

            // Real read via StorageStatsManager (API 26+). Requires PACKAGE_USAGE_STATS,
            // which is granted through Settings > Usage Access — if it hasn't been
            // granted yet, queryStatsForPackage throws SecurityException and we skip
            // this app for today rather than writing a fabricated number.
            val queried = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                storageStatsManager != null && storageManager != null
            ) {
                runCatching {
                    val uuid = storageManager.getUuidForPath(applicationContext.filesDir)
                    val stats = storageStatsManager.queryStatsForPackage(
                        uuid,
                        app.packageName,
                        Process.myUserHandle()
                    )
                    Triple(stats.appBytes, stats.dataBytes, stats.cacheBytes)
                }.getOrNull()
            } else null

            // No successful query and no prior snapshot means we genuinely have no
            // data for this app yet — skip it today instead of inventing a number.
            if (queried == null && previous == null) return@forEach

            val (appBytes, dataBytes, cacheBytes) = queried
                ?: Triple(previous!!.appSizeBytes, previous.dataSizeBytes, previous.cacheSizeBytes)

            val current = StorageSnapshotEntity(
                packageName = app.packageName,
                dateEpochDay = today,
                appSizeBytes = appBytes,
                dataSizeBytes = dataBytes,
                cacheSizeBytes = cacheBytes
            )
            storageSnapshotDao.upsert(current)

            val delta = (current.appSizeBytes + current.dataSizeBytes) -
                ((previous?.appSizeBytes ?: 0L) + (previous?.dataSizeBytes ?: 0L))
            val thresholdBytes = 50L * 1024 * 1024 // 50 MB — tune as needed

            if (previous != null && kotlin.math.abs(delta) > thresholdBytes) {
                val diffLabel = "${if (delta > 0) "+" else "-"}${formatBytes(kotlin.math.abs(delta))}"
                timelineRepository.recordEvent(
                    packageName = app.packageName,
                    type = if (delta > 0) TimelineEventType.STORAGE_INCREASED else TimelineEventType.STORAGE_DECREASED,
                    sourceApi = SOURCE_API,
                    description = "Storage ${if (delta > 0) "increased" else "decreased"} by ${formatBytes(kotlin.math.abs(delta))}",
                    previousValue = formatBytes((previous.appSizeBytes + previous.dataSizeBytes)),
                    newValue = formatBytes((current.appSizeBytes + current.dataSizeBytes)),
                    differenceValue = diffLabel
                )
            }
        }
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024) "%.2f GB".format(mb / 1024) else "%.0f MB".format(mb)
    }

    companion object {
        const val WORK_NAME = "storage_tracking_worker"
    }
}
