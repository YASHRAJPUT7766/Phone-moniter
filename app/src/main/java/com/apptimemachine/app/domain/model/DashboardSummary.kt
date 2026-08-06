package com.apptimemachine.app.domain.model

/** Aggregated data backing the Dashboard's stat cards. */
data class DashboardSummary(
    val totalInstalledApps: Int,
    val recentlyUpdated: List<InstalledApp>,
    val newlyInstalledSinceMonitoring: List<InstalledApp>,
    val topStorageConsumers: List<InstalledApp>,
    val topBatteryConsumers: List<InstalledApp>,
    val mostUsedApps: List<InstalledApp>,
    val recentTimelineEvents: List<TimelineEvent>,
    val totalTimelineEvents: Int,
    val appsMonitoredToday: Int,
    val isMonitoringActive: Boolean
)
