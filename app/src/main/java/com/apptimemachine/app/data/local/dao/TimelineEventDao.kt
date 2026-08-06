package com.apptimemachine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.apptimemachine.app.data.local.entity.TimelineEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineEventDao {

    @Query("SELECT * FROM timeline_events WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun observeTimelineForApp(packageName: String): Flow<List<TimelineEventEntity>>

    @Query("SELECT * FROM timeline_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentEvents(limit: Int = 50): Flow<List<TimelineEventEntity>>

    @Query("SELECT * FROM timeline_events WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun observeEventsBetween(start: Long, end: Long): Flow<List<TimelineEventEntity>>

    /** Backs the Dashboard's "Total Timeline Events" card. */
    @Query("SELECT COUNT(*) FROM timeline_events")
    fun observeTotalEventCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM timeline_events
        WHERE packageName = :packageName AND type = 'MONITORING_STARTED'
    """)
    suspend fun hasMonitoringStartedEvent(packageName: String): Int

    @Insert
    suspend fun insert(event: TimelineEventEntity)

    @Insert
    suspend fun insertAll(events: List<TimelineEventEntity>)
}
