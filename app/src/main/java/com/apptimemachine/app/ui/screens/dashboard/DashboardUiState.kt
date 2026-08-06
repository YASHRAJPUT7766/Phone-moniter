package com.apptimemachine.app.ui.screens.dashboard

import com.apptimemachine.app.domain.model.DashboardSummary

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val summary: DashboardSummary) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
