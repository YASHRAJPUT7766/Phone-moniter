package com.apptimemachine.app.data.local

import androidx.room.TypeConverter
import com.apptimemachine.app.data.local.entity.TimelineEventType

class Converters {
    @TypeConverter
    fun fromTimelineEventType(type: TimelineEventType): String = type.name

    @TypeConverter
    fun toTimelineEventType(value: String): TimelineEventType = TimelineEventType.valueOf(value)
}
