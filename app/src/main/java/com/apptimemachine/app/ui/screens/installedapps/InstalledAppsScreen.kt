package com.apptimemachine.app.ui.screens.installedapps

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppsScreen(
    onAppClick: (String) -> Unit,
    viewModel: InstalledAppsViewModel = hiltViewModel()
) {
    val apps by viewModel.apps.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Installed Apps") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onAppClick(app.packageName) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(app.appName, style = MaterialTheme.typography.titleMedium)
                        Text("v${app.currentVersionName}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
