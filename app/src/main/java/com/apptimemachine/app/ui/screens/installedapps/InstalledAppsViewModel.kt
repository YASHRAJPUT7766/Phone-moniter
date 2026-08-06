package com.apptimemachine.app.ui.screens.installedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimemachine.app.data.repository.AppRepository
import com.apptimemachine.app.domain.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InstalledAppsViewModel @Inject constructor(
    appRepository: AppRepository
) : ViewModel() {

    val apps: StateFlow<List<InstalledApp>> = appRepository.observeInstalledApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
