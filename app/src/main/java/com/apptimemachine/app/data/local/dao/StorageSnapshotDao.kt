package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.StorageSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageSnapshotDao {

    @Query("SELECT * FROM storage_snapshots WHERE packageName = :packageName ORDER BY dateEpochDay ASC")
    fun observeHistoryForApp(packageName: String): Flow<List<StorageSnapshotEntity>>

    @Query("""
        SELECT * FROM storage_snapshots
        WHERE packageName = :packageName
        ORDER BY dateEpochDay DESC LIMIT 1
    """)
    suspend fun getLatestForApp(packageName: String): StorageSnapshotEntity?

    @Query("""
        SELECT * FROM storage_snapshots
        WHERE dateEpochDay = (SELECT MAX(dateEpochDay) FROM storage_snapshots)
        ORDER BY (appSizeBytes + dataSizeBytes + cacheSizeBytes) DESC LIMIT :limit
    """)
    fun observeTopStorageConsumers(limit: Int = 10): Flow<List<StorageSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: StorageSnapshotEntity)
}
