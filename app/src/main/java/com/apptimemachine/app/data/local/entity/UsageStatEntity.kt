package com.apptimemachine.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily rollup sourced from UsageStatsManager (requires the user-granted
 * "Usage Access" special permission). [mostActiveHour] (0-23) backs the
 * spec's "Most Active Hours" / usage heatmap — left nullable since it's
 * only computable once UsageEvents (not just aggregate stats) are queried.
 */
@Entity(
    tableName = "usage_stats",
    foreignKeys = [
        ForeignKey(
            entity = InstalledAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packageName"), Index("dateEpochDay")]
)
data class UsageStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val dateEpochDay: Long,
    val foregroundTimeMillis: Long,
    val backgroundTimeMillis: Long,
    val openCount: Int,
    val lastOpenedAt: Long?,
    val mostActiveHour: Int? = null
)
