package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.PermissionSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionSnapshotDao {

    @Query("SELECT * FROM permission_snapshots WHERE packageName = :packageName ORDER BY recordedAt DESC")
    fun observeHistoryForApp(packageName: String): Flow<List<PermissionSnapshotEntity>>

    @Query("""
        SELECT * FROM permission_snapshots
        WHERE packageName = :packageName AND permission = :permission
        ORDER BY recordedAt DESC LIMIT 1
    """)
    suspend fun getLatest(packageName: String, permission: String): PermissionSnapshotEntity?

    @Insert
    suspend fun insert(snapshot: PermissionSnapshotEntity)
}
