package com.apptimemachine.app.ui.screens.timeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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

/**
 * Chronological feed across every app — the "Git History" master view.
 * Each row surfaces the full event schema from the spec: description,
 * source API, and the precomputed difference where one was recorded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onAppClick: (String) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Timeline") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(events, key = { it.id }) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onAppClick(event.packageName) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(event.description, style = MaterialTheme.typography.bodyLarge)
                        Text(event.packageName, style = MaterialTheme.typography.bodyMedium)
                        val meta = buildString {
                            append("via ${event.sourceApi}")
                            event.differenceValue?.let { append(" · $it") }
                        }
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
