package com.apptimemachine.app.ui.screens.appdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apptimemachine.app.ui.components.SectionHeader

/**
 * Per-app deep dive. Shows the Android-sourced static info first, then —
 * if the app predates monitoring — a one-line disclosure that history only
 * starts from the recorded monitoringStartedAt, before the actual
 * chronological timeline. This is the one place in the UI the spec's "no
 * fake history" rule needs to be surfaced explicitly to the user, not just
 * enforced silently in the data layer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: AppDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.app?.appName ?: packageName) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.app?.let { app ->
                item {
                    Column {
                        Text("Version ${app.currentVersionName} (${app.currentVersionCode})", style = MaterialTheme.typography.bodyLarge)
                        Text("Package: ${app.packageName}", style = MaterialTheme.typography.bodyMedium)
                        if (app.existedBeforeMonitoring) {
                            Text(
                                text = "This app was installed before monitoring began — timeline starts from first scan.",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item { SectionHeader(title = "Timeline") }
            items(uiState.timeline, key = { it.id }) { event ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                    val meta = buildString {
                        append("via ${event.sourceApi}")
                        event.differenceValue?.let { append(" · $it") }
                    }
                    Text(meta, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
