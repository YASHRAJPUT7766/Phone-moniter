package com.apptimemachine.app.data.repository

import com.apptimemachine.app.data.local.entity.TimelineEventType
import com.apptimemachine.app.domain.model.TimelineEvent
import kotlinx.coroutines.flow.Flow

interface TimelineRepository {
    fun observeTimelineForApp(packageName: String): Flow<List<TimelineEvent>>
    fun observeRecentEvents(limit: Int = 50): Flow<List<TimelineEvent>>
    fun observeTotalEventCount(): Flow<Int>

    /**
     * Writes one permanent timeline row. [sourceApi] is required (not
     * optional/nullable) so every event stays traceable to the Android API
     * that produced it — enforced at the call site, never inferred here.
     */
    suspend fun recordEvent(
        packageName: String,
        type: TimelineEventType,
        sourceApi: String,
        description: String,
        previousValue: String? = null,
        newValue: String? = null,
        differenceValue: String? = null
    )
}
