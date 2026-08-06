package com.apptimemachine.app.data.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.apptimemachine.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers all periodic background jobs. scheduleAll() should only be
 * called AFTER Permission Setup completes (i.e. UserPreferencesRepository
 * .isMonitoringActive becomes true) — starting workers before that would
 * mean the very first scan silently assumes permissions it doesn't have.
 * WorkManager dedupes via ExistingPeriodicWorkPolicy.KEEP, so re-calling on
 * every app launch is safe.
 */
@Singleton
class WorkScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun scheduleAllIfMonitoringActive() {
        if (userPreferencesRepository.isMonitoringActive.first()) {
            scheduleAppScan()
            scheduleStorageTracking()
        }
    }

    private fun scheduleAppScan() {
        val request = PeriodicWorkRequestBuilder<AppScanWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        workManager.enqueueUniquePeriodicWork(
            AppScanWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    private fun scheduleStorageTracking() {
        val request = PeriodicWorkRequestBuilder<StorageTrackingWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            StorageTrackingWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }
}
