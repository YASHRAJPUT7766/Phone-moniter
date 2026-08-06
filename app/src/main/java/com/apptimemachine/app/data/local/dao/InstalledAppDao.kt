package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.apptimemachine.app.data.local.entity.InstalledAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledAppDao {

    @Query("SELECT * FROM installed_apps WHERE uninstalledAt IS NULL ORDER BY appName ASC")
    fun observeInstalledApps(): Flow<List<InstalledAppEntity>>

    @Query("SELECT * FROM installed_apps WHERE uninstalledAt IS NOT NULL ORDER BY uninstalledAt DESC")
    fun observeRemovedApps(): Flow<List<InstalledAppEntity>>

    @Query("SELECT * FROM installed_apps WHERE packageName = :packageName")
    fun observeApp(packageName: String): Flow<InstalledAppEntity?>

    @Query("SELECT * FROM installed_apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): InstalledAppEntity?

    @Query("SELECT COUNT(*) FROM installed_apps WHERE uninstalledAt IS NULL")
    fun observeInstalledCount(): Flow<Int>

    @Query("""
        SELECT * FROM installed_apps
        WHERE uninstalledAt IS NULL
        ORDER BY lastUpdatedAt DESC LIMIT :limit
    """)
    fun observeRecentlyUpdated(limit: Int = 10): Flow<List<InstalledAppEntity>>

    /** "Newly Installed" per spec means detected AFTER monitoring started — i.e.
     *  monitoringStartedAt >= firstInstalledAt, not just recently installed by Android. */
    @Query("""
        SELECT * FROM installed_apps
        WHERE uninstalledAt IS NULL AND monitoringStartedAt <= firstInstalledAt
        ORDER BY firstInstalledAt DESC LIMIT :limit
    """)
    fun observeNewlyInstalledSinceMonitoring(limit: Int = 10): Flow<List<InstalledAppEntity>>

    /** Distinct count of apps whose monitoring window began today — backs the
     *  Dashboard's "Apps Monitored Today" card. */
    @Query("""
        SELECT COUNT(*) FROM installed_apps
        WHERE monitoringStartedAt BETWEEN :startOfDay AND :endOfDay
    """)
    fun observeAppsFirstMonitoredToday(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: InstalledAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(apps: List<InstalledAppEntity>)

    @Update
    suspend fun update(app: InstalledAppEntity)

    @Query("UPDATE installed_apps SET uninstalledAt = :timestamp WHERE packageName = :packageName")
    suspend fun markUninstalled(packageName: String, timestamp: Long)

    @Query("UPDATE installed_apps SET isFavorite = :isFavorite WHERE packageName = :packageName")
    suspend fun setFavorite(packageName: String, isFavorite: Boolean)
}
