package com.apptimemachine.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per version WE OBSERVED, starting with the version present when
 * monitoring began. We do not attempt to reconstruct versions the app had
 * before that point — Android does not expose that history.
 */
@Entity(
    tableName = "version_history",
    foreignKeys = [
        ForeignKey(
            entity = InstalledAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packageName")]
)
data class VersionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val apkSizeBytes: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val signingCertSha256: String?,
    val recordedAt: Long
)
