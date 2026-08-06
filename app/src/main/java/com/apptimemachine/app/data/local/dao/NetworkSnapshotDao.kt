package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.NetworkSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkSnapshotDao {

    @Query("SELECT * FROM network_snapshots WHERE packageName = :packageName ORDER BY dateEpochDay ASC")
    fun observeHistoryForApp(packageName: String): Flow<List<NetworkSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: NetworkSnapshotEntity)
}
