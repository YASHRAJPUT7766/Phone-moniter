package com.apptimemachine.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apptimemachine.app.ui.components.AnimatedStatCard
import com.apptimemachine.app.ui.components.MonitoringStatusBadge
import com.apptimemachine.app.ui.components.SectionHeader
import com.apptimemachine.app.ui.theme.BatteryAccent
import com.apptimemachine.app.ui.theme.StorageAccent

/**
 * Home screen. Includes the spec's Monitoring Status badge in the top bar
 * plus a 6-card stat grid: Installed Apps, Recently Updated, Newly
 * Installed (since monitoring began), Total Timeline Events, Apps
 * Monitored Today, and the storage/battery ranking cards. Storage/battery
 * cards render 0 until their DAOs are joined in DashboardViewModel — see
 * the TODO there.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAppClick: (String) -> Unit,
    onSeeAllInstalled: () -> Unit,
    onSeeTimeline: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Time Machine") },
                actions = {
                    val isActive = (uiState as? DashboardUiState.Success)?.summary?.isMonitoringActive ?: false
                    MonitoringStatusBadge(isActive = isActive, modifier = Modifier.padding(end = 12.dp))
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> LoadingState(paddingValues)
            is DashboardUiState.Error -> Text(state.message, modifier = Modifier.padding(paddingValues).padding(16.dp))
            is DashboardUiState.Success -> DashboardContent(
                state = state,
                paddingValues = paddingValues,
                onAppClick = onAppClick,
                onSeeAllInstalled = onSeeAllInstalled,
                onSeeTimeline = onSeeTimeline
            )
        }
    }
}

@Composable
private fun LoadingState(paddingValues: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    paddingValues: PaddingValues,
    onAppClick: (String) -> Unit,
    onSeeAllInstalled: () -> Unit,
    onSeeTimeline: () -> Unit
) {
    val summary = state.summary

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnimatedStatCard(
                        title = "Installed Apps",
                        value = summary.totalInstalledApps.toString(),
                        icon = Icons.Filled.Apps,
                        onClick = onSeeAllInstalled
                    )
                }
                item {
                    AnimatedStatCard(
                        title = "Recently Updated",
                        value = summary.recentlyUpdated.size.toString(),
                        icon = Icons.Filled.NewReleases
                    )
                }
                item {
                    AnimatedStatCard(
                        title = "Newly Installed",
                        value = summary.newlyInstalledSinceMonitoring.size.toString(),
                        icon = Icons.Filled.Apps
                    )
                }
                item {
                    AnimatedStatCard(
                        title = "Top Storage",
                        value = summary.topStorageConsumers.size.toString(),
                        icon = Icons.Filled.Storage,
                        accentColor = StorageAccent
                    )
                }
                item {
                    AnimatedStatCard(
                        title = "Battery Hungry",
                        value = summary.topBatteryConsumers.size.toString(),
                        icon = Icons.Filled.BatteryAlert,
                        accentColor = BatteryAccent
                    )
                }
                item {
                    AnimatedStatCard(
                        title = "Total Timeline Events",
                        value = summary.totalTimelineEvents.toString(),
                        icon = Icons.Filled.EventNote,
                        onClick = onSeeTimeline
                    )
                }
                item {
                    AnimatedStatCard(
                        title = "Apps Monitored Today",
                        value = summary.appsMonitoredToday.toString(),
                        icon = Icons.Filled.Today
                    )
                }
            }
        }

        item {
            SectionHeader(title = "Timeline Summary", actionLabel = "See all", onActionClick = onSeeTimeline)
        }
        items(summary.recentTimelineEvents, key = { it.id }) { event ->
            Text(
                text = "${event.description} — ${event.packageName}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }
    }
}
