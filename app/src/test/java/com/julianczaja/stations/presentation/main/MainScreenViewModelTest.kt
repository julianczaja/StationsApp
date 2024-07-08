package com.julianczaja.stations.presentation.main

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.julianczaja.stations.MainDispatcherRule
import com.julianczaja.stations.domain.StationsFileReader
import com.julianczaja.stations.domain.repository.AppDataRepository
import com.julianczaja.stations.domain.repository.StationKeywordRepository
import com.julianczaja.stations.domain.repository.StationRepository
import com.julianczaja.stations.domain.usecase.CalculateShouldRefreshDataUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class MainScreenViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var stationsFileReader: StationsFileReader
    private lateinit var stationRepository: StationRepository
    private lateinit var stationKeywordRepository: StationKeywordRepository
    private lateinit var appDataRepository: AppDataRepository
    private lateinit var calculateShouldRefreshDataUseCase: CalculateShouldRefreshDataUseCase

    @Before
    fun setup() {
        stationsFileReader = mockk()
        stationRepository = mockk(relaxed = true)
        stationKeywordRepository = mockk(relaxed = true)
        appDataRepository = mockk(relaxUnitFun = true)
        every { appDataRepository.getLastDataUpdateTimestamp() } returns flowOf(0L)
        calculateShouldRefreshDataUseCase = mockk()
    }

    private fun getViewModel() = MainScreenViewModel(
        stationsFileReader = stationsFileReader,
        stationRepository = stationRepository,
        stationKeywordRepository = stationKeywordRepository,
        appDataRepository = appDataRepository,
        calculateShouldRefreshDataUseCase = calculateShouldRefreshDataUseCase,
        ioDispatcher = dispatcherRule.testDispatcher
    )

    @Test
    fun `isUpdating should be false on start and true during data update`() = runTest {
        coEvery { stationsFileReader.readStations() } coAnswers {
            delay(100L)
            Result.success(emptyList())
        }
        coEvery { stationsFileReader.readStationKeywords() } coAnswers {
            delay(100L)
            Result.success(emptyList())
        }
        coEvery { stationRepository.isEmpty() } returns true
        coEvery { stationKeywordRepository.isEmpty() } returns true

        val viewModel = getViewModel()

        viewModel.isUpdating.test {
            assertThat(awaitItem()).isFalse()
            viewModel.updateData()
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `updateData function uses stationsFileReader when database is empty`() {
        coEvery { stationsFileReader.readStations() } returns Result.success(emptyList())
        coEvery { stationsFileReader.readStationKeywords() } returns Result.success(emptyList())

        coEvery { stationRepository.isEmpty() } returns true
        coEvery { stationKeywordRepository.isEmpty() } returns true

        val viewModel = getViewModel()

        viewModel.updateData()

        coVerify(exactly = 1) { stationsFileReader.readStations() }
        coVerify(exactly = 1) { stationsFileReader.readStationKeywords() }
    }
}
