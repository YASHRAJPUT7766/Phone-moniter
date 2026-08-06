package com.apptimemachine.app.domain.model

import com.apptimemachine.app.data.local.entity.TimelineEventType

data class TimelineEvent(
    val id: Long,
    val packageName: String,
    val type: TimelineEventType,
    val timestamp: Long,
    val description: String,
    val sourceApi: String,
    val previousValue: String?,
    val newValue: String?,
    val differenceValue: String?
)
