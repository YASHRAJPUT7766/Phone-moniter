package com.apptimemachine.app.domain.usecase

import com.apptimemachine.app.data.repository.TimelineRepository
import com.apptimemachine.app.domain.model.TimelineEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppTimelineUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository
) {
    operator fun invoke(packageName: String): Flow<List<TimelineEvent>> =
        timelineRepository.observeTimelineForApp(packageName)
}
