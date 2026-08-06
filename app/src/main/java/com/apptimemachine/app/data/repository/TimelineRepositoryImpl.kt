package com.apptimemachine.app.data.repository

import com.apptimemachine.app.data.local.dao.TimelineEventDao
import com.apptimemachine.app.data.local.entity.TimelineEventEntity
import com.apptimemachine.app.data.local.entity.TimelineEventType
import com.apptimemachine.app.domain.model.TimelineEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TimelineRepositoryImpl @Inject constructor(
    private val timelineEventDao: TimelineEventDao
) : TimelineRepository {

    override fun observeTimelineForApp(packageName: String): Flow<List<TimelineEvent>> =
        timelineEventDao.observeTimelineForApp(packageName).map { list -> list.map { it.toDomain() } }

    override fun observeRecentEvents(limit: Int): Flow<List<TimelineEvent>> =
        timelineEventDao.observeRecentEvents(limit).map { list -> list.map { it.toDomain() } }

    override fun observeTotalEventCount(): Flow<Int> = timelineEventDao.observeTotalEventCount()

    override suspend fun recordEvent(
        packageName: String,
        type: TimelineEventType,
        sourceApi: String,
        description: String,
        previousValue: String?,
        newValue: String?,
        differenceValue: String?
    ) {
        timelineEventDao.insert(
            TimelineEventEntity(
                packageName = packageName,
                type = type,
                timestamp = System.currentTimeMillis(),
                description = description,
                sourceApi = sourceApi,
                previousValue = previousValue,
                newValue = newValue,
                differenceValue = differenceValue
            )
        )
    }

    private fun TimelineEventEntity.toDomain() = TimelineEvent(
        id = id,
        packageName = packageName,
        type = type,
        timestamp = timestamp,
        description = description,
        sourceApi = sourceApi,
        previousValue = previousValue,
        newValue = newValue,
        differenceValue = differenceValue
    )
}
