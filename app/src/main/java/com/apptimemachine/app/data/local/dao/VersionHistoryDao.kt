package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.VersionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VersionHistoryDao {

    @Query("SELECT * FROM version_history WHERE packageName = :packageName ORDER BY recordedAt DESC")
    fun observeHistoryForApp(packageName: String): Flow<List<VersionHistoryEntity>>

    @Query("""
        SELECT * FROM version_history
        WHERE packageName = :packageName
        ORDER BY recordedAt DESC LIMIT 1
    """)
    suspend fun getLatest(packageName: String): VersionHistoryEntity?

    @Insert
    suspend fun insert(entry: VersionHistoryEntity)
}
