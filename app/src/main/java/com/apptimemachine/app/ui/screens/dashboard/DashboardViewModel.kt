package com.apptimemachine.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimemachine.app.data.preferences.UserPreferencesRepository
import com.apptimemachine.app.data.repository.AppRepository
import com.apptimemachine.app.data.repository.TimelineRepository
import com.apptimemachine.app.domain.model.DashboardSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    appRepository: AppRepository,
    timelineRepository: TimelineRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
    private val endOfToday = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    val uiState: StateFlow<DashboardUiState> = combine(
        appRepository.observeInstalledCount(),
        appRepository.observeRecentlyUpdated(limit = 5),
        appRepository.observeNewlyInstalledSinceMonitoring(limit = 5),
        timelineRepository.observeRecentEvents(limit = 10),
        timelineRepository.observeTotalEventCount(),
        appRepository.observeAppsFirstMonitoredToday(startOfToday, endOfToday),
        userPreferencesRepository.isMonitoringActive
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val totalCount = values[0] as Int
        val recentlyUpdated = values[1] as List<com.apptimemachine.app.domain.model.InstalledApp>
        val newlyInstalled = values[2] as List<com.apptimemachine.app.domain.model.InstalledApp>
        val recentEvents = values[3] as List<com.apptimemachine.app.domain.model.TimelineEvent>
        val totalEvents = values[4] as Int
        val monitoredToday = values[5] as Int
        val monitoringActive = values[6] as Boolean

        DashboardUiState.Success(
            DashboardSummary(
                totalInstalledApps = totalCount,
                recentlyUpdated = recentlyUpdated,
                newlyInstalledSinceMonitoring = newlyInstalled,
                // Storage/battery/usage ranking wire up once their DAOs are joined
                // against InstalledApp in AppRepository — left empty so the screen
                // compiles and renders the sections it already has real data for.
                topStorageConsumers = emptyList(),
                topBatteryConsumers = emptyList(),
                mostUsedApps = emptyList(),
                recentTimelineEvents = recentEvents,
                totalTimelineEvents = totalEvents,
                appsMonitoredToday = monitoredToday,
                isMonitoringActive = monitoringActive
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading
    )
}
