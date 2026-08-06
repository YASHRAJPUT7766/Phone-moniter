package com.apptimemachine.app.data.repository

import com.apptimemachine.app.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

/**
 * Source of truth for "what apps exist and what's their current state".
 * Implementations combine PackageManager (live) with Room (historical/cached)
 * so the UI works offline-first.
 */
interface AppRepository {
    fun observeInstalledApps(): Flow<List<InstalledApp>>
    fun observeRemovedApps(): Flow<List<InstalledApp>>
    fun observeApp(packageName: String): Flow<InstalledApp?>
    fun observeRecentlyUpdated(limit: Int = 10): Flow<List<InstalledApp>>
    fun observeNewlyInstalledSinceMonitoring(limit: Int = 10): Flow<List<InstalledApp>>
    fun observeInstalledCount(): Flow<Int>
    fun observeAppsFirstMonitoredToday(startOfDayMillis: Long, endOfDayMillis: Long): Flow<Int>
    suspend fun setFavorite(packageName: String, isFavorite: Boolean)

    /**
     * Re-reads PackageManager, diffs against Room, and persists any changes.
     * For a package never seen before, this is the ONLY place
     * monitoringStartedAt is set (to "now") — see AppRepositoryImpl.
     * Never call anything that assigns monitoringStartedAt to a value other
     * than the actual wall-clock time of first observation.
     */
    suspend fun refreshFromSystem()
}
