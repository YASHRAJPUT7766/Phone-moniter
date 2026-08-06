package com.apptimemachine.app.di

import android.content.Context
import androidx.room.Room
import com.apptimemachine.app.data.local.AppDatabase
import com.apptimemachine.app.data.local.dao.BatterySnapshotDao
import com.apptimemachine.app.data.local.dao.InstalledAppDao
import com.apptimemachine.app.data.local.dao.NetworkSnapshotDao
import com.apptimemachine.app.data.local.dao.NotificationStatDao
import com.apptimemachine.app.data.local.dao.PermissionSnapshotDao
import com.apptimemachine.app.data.local.dao.StorageSnapshotDao
import com.apptimemachine.app.data.local.dao.TimelineEventDao
import com.apptimemachine.app.data.local.dao.UsageStatDao
import com.apptimemachine.app.data.local.dao.VersionHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            // .openHelperFactory(SupportFactory(passphrase)) // enable for SQLCipher encryption
            .fallbackToDestructiveMigration() // replace with real Migrations before release
            .build()

    @Provides fun provideInstalledAppDao(db: AppDatabase): InstalledAppDao = db.installedAppDao()
    @Provides fun provideTimelineEventDao(db: AppDatabase): TimelineEventDao = db.timelineEventDao()
    @Provides fun provideStorageSnapshotDao(db: AppDatabase): StorageSnapshotDao = db.storageSnapshotDao()
    @Provides fun providePermissionSnapshotDao(db: AppDatabase): PermissionSnapshotDao = db.permissionSnapshotDao()
    @Provides fun provideVersionHistoryDao(db: AppDatabase): VersionHistoryDao = db.versionHistoryDao()
    @Provides fun provideUsageStatDao(db: AppDatabase): UsageStatDao = db.usageStatDao()
    @Provides fun provideBatterySnapshotDao(db: AppDatabase): BatterySnapshotDao = db.batterySnapshotDao()
    @Provides fun provideNetworkSnapshotDao(db: AppDatabase): NetworkSnapshotDao = db.networkSnapshotDao()
    @Provides fun provideNotificationStatDao(db: AppDatabase): NotificationStatDao = db.notificationStatDao()
}
