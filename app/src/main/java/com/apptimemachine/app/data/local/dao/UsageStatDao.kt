package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.UsageStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageStatDao {

    @Query("SELECT * FROM usage_stats WHERE packageName = :packageName ORDER BY dateEpochDay ASC")
    fun observeHistoryForApp(packageName: String): Flow<List<UsageStatEntity>>

    @Query("""
        SELECT * FROM usage_stats
        WHERE dateEpochDay = :day
        ORDER BY foregroundTimeMillis DESC LIMIT :limit
    """)
    fun observeMostUsedOnDay(day: Long, limit: Int = 10): Flow<List<UsageStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: UsageStatEntity)
}
