package com.apptimemachine.app.domain.usecase

import com.apptimemachine.app.data.repository.AppRepository
import javax.inject.Inject

/** Triggers an on-demand refresh; the periodic version runs via AppScanWorker. */
class ScanInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke() = appRepository.refreshFromSystem()
}
