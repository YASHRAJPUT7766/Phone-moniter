package com.apptimemachine.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Event vocabulary matches the spec's example timeline exactly. MONITORING_STARTED
 * is the one synthetic event every app gets exactly once — written the first time
 * AppScanWorker sees the package — and it doubles as the visible marker in the UI
 * that tells the user "everything before this line is Android's static metadata;
 * everything after is what we've actually observed."
 */
enum class TimelineEventType {
    MONITORING_STARTED,
    VERSION_UPDATED,
    STORAGE_INCREASED,
    STORAGE_DECREASED,
    CACHE_CLEARED,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    NOTIFICATION_SETTING_CHANGED,
    MOBILE_DATA_INCREASED,
    WIFI_USAGE_INCREASED,
    APP_OPENED,
    USAGE_INCREASED,
    BATTERY_USAGE_UPDATED,
    APK_UPDATED,
    LAST_USED,
    UNINSTALLED
}

/**
 * Append-only, chronological log — the literal "Git history" per package.
 * Rows are written ONLY when a background worker detects a real diff
 * against the previous snapshot; nothing here is ever backfilled or
 * estimated, per the spec's core rule.
 *
 * [sourceApi] records which Android API produced the value, so every event
 * is traceable and auditable (e.g. "PackageManager", "StorageStatsManager",
 * "UsageStatsManager", "NetworkStatsManager") — required by the spec's
 * per-event schema.
 * [previousValue] / [newValue] hold the raw before/after values;
 * [differenceValue] holds a precomputed human-readable delta (e.g. "+600 MB",
 * "+18%") so the UI never needs to re-derive it.
 */
@Entity(
    tableName = "timeline_events",
    foreignKeys = [
        ForeignKey(
            entity = InstalledAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packageName"), Index("timestamp")]
)
data class TimelineEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val type: TimelineEventType,
    val timestamp: Long,
    val description: String,
    val sourceApi: String,
    val previousValue: String? = null,
    val newValue: String? = null,
    val differenceValue: String? = null
)
