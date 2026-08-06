package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.NotificationStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationStatDao {

    @Query("SELECT * FROM notification_stats WHERE packageName = :packageName ORDER BY dateEpochDay ASC")
    fun observeHistoryForApp(packageName: String): Flow<List<NotificationStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: NotificationStatEntity)
}
