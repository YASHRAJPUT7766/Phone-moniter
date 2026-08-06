package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.BatterySnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatterySnapshotDao {

    @Query("SELECT * FROM battery_snapshots WHERE packageName = :packageName ORDER BY dateEpochDay ASC")
    fun observeHistoryForApp(packageName: String): Flow<List<BatterySnapshotEntity>>

    @Query("""
        SELECT * FROM battery_snapshots
        WHERE dateEpochDay = :day
        ORDER BY (foregroundMah + backgroundMah) DESC LIMIT :limit
    """)
    fun observeTopConsumersOnDay(day: Long, limit: Int = 10): Flow<List<BatterySnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: BatterySnapshotEntity)
}
