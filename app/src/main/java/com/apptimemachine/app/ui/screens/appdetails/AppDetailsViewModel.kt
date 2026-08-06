package com.apptimemachine.app.ui.screens.appdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimemachine.app.data.repository.AppRepository
import com.apptimemachine.app.data.repository.TimelineRepository
import com.apptimemachine.app.domain.model.InstalledApp
import com.apptimemachine.app.domain.model.TimelineEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppDetailsUiState(
    val app: InstalledApp? = null,
    val timeline: List<TimelineEvent> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    appRepository: AppRepository,
    timelineRepository: TimelineRepository
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    val uiState: StateFlow<AppDetailsUiState> = combine(
        appRepository.observeApp(packageName),
        timelineRepository.observeTimelineForApp(packageName)
    ) { app, timeline ->
        AppDetailsUiState(app = app, timeline = timeline, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppDetailsUiState()
    )
}
