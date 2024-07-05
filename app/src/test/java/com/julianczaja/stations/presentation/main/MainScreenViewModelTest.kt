package com.julianczaja.stations.presentation.main

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.julianczaja.stations.MainDispatcherRule
import com.julianczaja.stations.data.local.database.dao.StationDao
import com.julianczaja.stations.data.local.database.dao.StationKeywordDao
import com.julianczaja.stations.domain.StationsFileReader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test


class MainScreenViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `isUpdating should be false on start and true during data update`() = runTest {
        val stationsFileReader: StationsFileReader = mockk()
        val stationDao: StationDao = mockk(relaxed = true)
        val stationKeywordDao: StationKeywordDao = mockk(relaxed = true)

        coEvery { stationsFileReader.readStations() } coAnswers {
            delay(100L)
            Result.success(emptyList())
        }
        coEvery { stationsFileReader.readStationKeywords() } coAnswers {
            delay(100L)
            Result.success(emptyList())
        }
        coEvery { stationDao.isEmpty() } returns true
        coEvery { stationKeywordDao.isEmpty() } returns true

        val viewModel = MainScreenViewModel(
            stationsFileReader = stationsFileReader,
            stationDao = stationDao,
            stationKeywordDao = stationKeywordDao,
            ioDispatcher = dispatcherRule.testDispatcher
        )

        viewModel.isUpdating.test {
            assertThat(awaitItem()).isFalse()
            viewModel.updateData()
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `updateData function uses stationsFileReader when database is empty`() {
        val stationsFileReader: StationsFileReader = mockk(relaxed = true)
        val stationDao: StationDao = mockk(relaxed = true)
        val stationKeywordDao: StationKeywordDao = mockk(relaxed = true)

        coEvery { stationsFileReader.readStations() } returns Result.success(emptyList())
        coEvery { stationsFileReader.readStationKeywords() } returns Result.success(emptyList())

        coEvery { stationDao.isEmpty() } returns true
        coEvery { stationKeywordDao.isEmpty() } returns true

        val viewModel = MainScreenViewModel(
            stationsFileReader = stationsFileReader,
            stationDao = stationDao,
            stationKeywordDao = stationKeywordDao,
            ioDispatcher = dispatcherRule.testDispatcher
        )

        viewModel.updateData()

        coVerify(exactly = 1) { stationsFileReader.readStations() }
        coVerify(exactly = 1) { stationsFileReader.readStationKeywords() }
    }
}
