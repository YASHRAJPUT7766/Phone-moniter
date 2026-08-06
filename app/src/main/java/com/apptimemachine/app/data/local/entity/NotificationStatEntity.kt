package com.apptimemachine.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily rollup of notification activity — requires a NotificationListenerService
 * grant (separate special permission, own Play Store review scrutiny). Only
 * counts Android actually exposes: posted count and category breakdown.
 * "Blocked notifications" from the original spec draft is NOT modeled here,
 * since Android doesn't expose a reliable per-app blocked-count API; the
 * closest real signal is the channel importance level, which callers can
 * read live from NotificationManager rather than needing history for it.
 */
@Entity(
    tableName = "notification_stats",
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
data class NotificationStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val dateEpochDay: Long,
    val postedCount: Int,
    val category: String? = null
)
