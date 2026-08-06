package com.apptimemachine.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apptimemachine.app.data.local.dao.BatterySnapshotDao
import com.apptimemachine.app.data.local.dao.InstalledAppDao
import com.apptimemachine.app.data.local.dao.NetworkSnapshotDao
import com.apptimemachine.app.data.local.dao.NotificationStatDao
import com.apptimemachine.app.data.local.dao.PermissionSnapshotDao
import com.apptimemachine.app.data.local.dao.StorageSnapshotDao
import com.apptimemachine.app.data.local.dao.TimelineEventDao
import com.apptimemachine.app.data.local.dao.UsageStatDao
import com.apptimemachine.app.data.local.dao.VersionHistoryDao
import com.apptimemachine.app.data.local.entity.BatterySnapshotEntity
import com.apptimemachine.app.data.local.entity.InstalledAppEntity
import com.apptimemachine.app.data.local.entity.NetworkSnapshotEntity
import com.apptimemachine.app.data.local.entity.NotificationStatEntity
import com.apptimemachine.app.data.local.entity.PermissionSnapshotEntity
import com.apptimemachine.app.data.local.entity.StorageSnapshotEntity
import com.apptimemachine.app.data.local.entity.TimelineEventEntity
import com.apptimemachine.app.data.local.entity.UsageStatEntity
import com.apptimemachine.app.data.local.entity.VersionHistoryEntity

/**
 * NOTE on "Encrypted Room Database" from the spec: standard Room has no
 * built-in encryption. Swap the SupportSQLiteOpenHelper factory for
 * SQLCipher's (net.zetetic:android-database-sqlcipher) via
 * .openHelperFactory(SupportFactory(passphrase)) below when ready to ship.
 * Left unencrypted here to keep the scaffold dependency-light.
 */
@Database(
    entities = [
        InstalledAppEntity::class,
        TimelineEventEntity::class,
        StorageSnapshotEntity::class,
        PermissionSnapshotEntity::class,
        VersionHistoryEntity::class,
        UsageStatEntity::class,
        BatterySnapshotEntity::class,
        NetworkSnapshotEntity::class,
        NotificationStatEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun timelineEventDao(): TimelineEventDao
    abstract fun storageSnapshotDao(): StorageSnapshotDao
    abstract fun permissionSnapshotDao(): PermissionSnapshotDao
    abstract fun versionHistoryDao(): VersionHistoryDao
    abstract fun usageStatDao(): UsageStatDao
    abstract fun batterySnapshotDao(): BatterySnapshotDao
    abstract fun networkSnapshotDao(): NetworkSnapshotDao
    abstract fun notificationStatDao(): NotificationStatDao

    companion object {
        const val DATABASE_NAME = "app_time_machine.db"
    }
}
