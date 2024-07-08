package com.julianczaja.stations.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.julianczaja.stations.domain.repository.AppDataRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CalculateShouldRefreshDataUseCaseTest {

    private lateinit var appDataRepository: AppDataRepository
    private val refreshInterval = 86_400_000L

    @Before
    fun setup() {
        appDataRepository = mockk<AppDataRepository>(relaxUnitFun = true)
    }

    @Test
    fun `usecase returns true when last update was before refresh interval`() = runTest {
        val lastTimestamp = System.currentTimeMillis() - refreshInterval - 10_000L // 10 s
        val currentTimestamp = System.currentTimeMillis()

        every { appDataRepository.getLastDataUpdateTimestamp() } returns flowOf(lastTimestamp)
        val useCase = CalculateShouldRefreshDataUseCase(appDataRepository)

        val shouldRefresh = useCase.invoke(
            currentTimestamp = currentTimestamp,
            refreshInterval = refreshInterval
        )
        assertThat(shouldRefresh).isTrue()
    }

    @Test
    fun `usecase returns false when last update was after refresh interval`() = runTest {
        val lastTimestamp = System.currentTimeMillis() - 10_000L // 10 s
        val currentTimestamp = System.currentTimeMillis()

        every { appDataRepository.getLastDataUpdateTimestamp() } returns flowOf(lastTimestamp)
        val useCase = CalculateShouldRefreshDataUseCase(appDataRepository)

        val shouldRefresh = useCase.invoke(
            currentTimestamp = currentTimestamp,
            refreshInterval = refreshInterval
        )
        assertThat(shouldRefresh).isFalse()
    }
}
