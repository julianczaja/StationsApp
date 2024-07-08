package com.julianczaja.stations.domain.usecase

import com.julianczaja.stations.domain.repository.AppDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class CalculateShouldRefreshDataUseCase @Inject constructor(
    private val appDataRepository: AppDataRepository
) {
    suspend operator fun invoke(
        currentTimestamp: Long,
        refreshInterval: Long
    ): Boolean {
        val lastTimestamp = appDataRepository.getLastDataUpdateTimestamp().first()
        return (currentTimestamp - lastTimestamp) > refreshInterval
    }
}
