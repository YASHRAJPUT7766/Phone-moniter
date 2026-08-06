package com.apptimemachine.app.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimemachine.app.data.repository.TimelineRepository
import com.apptimemachine.app.domain.model.TimelineEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    timelineRepository: TimelineRepository
) : ViewModel() {

    val events: StateFlow<List<TimelineEvent>> = timelineRepository
        .observeRecentEvents(limit = 200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
