package com.apptimemachine.app.domain.usecase

import com.apptimemachine.app.data.repository.AppRepository
import com.apptimemachine.app.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(): Flow<List<InstalledApp>> = appRepository.observeInstalledApps()
}
