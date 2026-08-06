package com.apptimemachine.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records the granted/revoked state of a single dangerous permission at a
 * point in time. AppScanWorker diffs against the most recent row per
 * (packageName, permission) and only writes a new row + TimelineEvent when
 * the state actually changes after monitoring has started for that app.
 */
@Entity(
    tableName = "permission_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = InstalledAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packageName"), Index("permission")]
)
data class PermissionSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val permission: String,
    val isGranted: Boolean,
    val recordedAt: Long
)
