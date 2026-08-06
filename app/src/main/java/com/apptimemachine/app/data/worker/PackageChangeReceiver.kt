package com.apptimemachine.app.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fires immediately on install/update/uninstall so the timeline reflects
 * changes in near-real-time rather than waiting for the next periodic
 * AppScanWorker run. Enqueues a one-off scan rather than doing DB work
 * directly here, since BroadcastReceivers must return quickly. If
 * monitoring hasn't started yet (Permission Setup incomplete), the
 * enqueued AppScanWorker/refreshFromSystem() still runs — an install
 * detected in this state correctly gets monitoringStartedAt = now, since
 * "now" IS the true first-observation time in that case.
 */
@AndroidEntryPoint
class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val request = OneTimeWorkRequestBuilder<AppScanWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
