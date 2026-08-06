package com.apptimemachine.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apptimemachine.app.domain.usecase.ScanInstalledAppsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic full scan: re-reads PackageManager, diffs against Room via
 * ScanInstalledAppsUseCase -> AppRepository.refreshFromSystem(), and writes
 * TimelineEvents for anything that changed. For a package seen for the
 * first time, refreshFromSystem() is responsible for writing exactly one
 * MONITORING_STARTED event — see the TODO in AppRepositoryImpl for the
 * exact rule (monitoringStartedAt = now, never backdated).
 */
@HiltWorker
class AppScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scanInstalledAppsUseCase: ScanInstalledAppsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        scanInstalledAppsUseCase()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        const val WORK_NAME = "app_scan_worker"
    }
}
