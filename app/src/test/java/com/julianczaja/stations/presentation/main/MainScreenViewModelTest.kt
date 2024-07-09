package com.julianczaja.stations.presentation.main

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.julianczaja.stations.MainDispatcherRule
import com.julianczaja.stations.data.NetworkManager
import com.julianczaja.stations.domain.StationsFileReader
import com.julianczaja.stations.domain.repository.AppDataRepository
import com.julianczaja.stations.domain.repository.StationKeywordRepository
import com.julianczaja.stations.domain.repository.StationRepository
import com.julianczaja.stations.domain.usecase.CalculateShouldRefreshDataUseCase
import com.julianczaja.stations.domain.usecase.GetStationPromptsUseCase
import com.julianczaja.stations.domain.usecase.NormalizeStringUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private lateinit var networkManager: NetworkManager
    private lateinit var stationRepository: StationRepository
    private lateinit var stationKeywordRepository: StationKeywordRepository
    private lateinit var appDataRepository: AppDataRepository
    private lateinit var calculateShouldRefreshDataUseCase: CalculateShouldRefreshDataUseCase
    private lateinit var getStationPromptsUseCase: GetStationPromptsUseCase
    private lateinit var normalizeStringUseCase: NormalizeStringUseCase

    @Before
    fun setup() {
        stationsFileReader = mockk()
        coEvery { stationsFileReader.readStations() } returns Result.success(emptyList())
        coEvery { stationsFileReader.readStationKeywords() } returns Result.success(emptyList())
        networkManager = mockk()
        stationRepository = mockk(relaxed = true)
        stationKeywordRepository = mockk(relaxed = true)
        appDataRepository = mockk(relaxUnitFun = true)
        every { appDataRepository.getLastDataUpdateTimestamp() } returns flowOf(0L)
        calculateShouldRefreshDataUseCase = mockk()
        getStationPromptsUseCase = mockk()
        normalizeStringUseCase = NormalizeStringUseCase()
    }

    private fun getViewModel() = MainScreenViewModel(
        stationsFileReader = stationsFileReader,
        networkManager = networkManager,
        stationRepository = stationRepository,
        stationKeywordRepository = stationKeywordRepository,
        appDataRepository = appDataRepository,
        calculateShouldRefreshDataUseCase = calculateShouldRefreshDataUseCase,
        getStationPromptsUseCase = getStationPromptsUseCase,
        normalizeStringUseCase = normalizeStringUseCase,
        ioDispatcher = dispatcherRule.testDispatcher
    )

    @Test
    fun `isUpdating should be false on start and true during offline data update`() = runTest {
        coEvery { networkManager.isCurrentlyConnected() } returns false
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
    fun `isUpdating should be false on start and true during online data update`() = runTest {
        coEvery { networkManager.isCurrentlyConnected() } returns true
        coEvery { calculateShouldRefreshDataUseCase.invoke(any(), any()) } returns true
        coEvery { stationRepository.updateStationsRemote() } coAnswers {
            delay(100L)
            Result.success(Unit)
        }
        coEvery { stationKeywordRepository.updateStationKeywordsRemote() } coAnswers {
            delay(100L)
            Result.success(Unit)
        }

        val viewModel = getViewModel()

        viewModel.isUpdating.test {
            assertThat(awaitItem()).isFalse()
            viewModel.updateData()
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `database should be updated from local file when no internet and database is empty`() {
        coEvery { networkManager.isCurrentlyConnected() } returns false
        coEvery { stationsFileReader.readStations() } returns Result.success(emptyList())
        coEvery { stationsFileReader.readStationKeywords() } returns Result.success(emptyList())
        coEvery { stationRepository.isEmpty() } returns true
        coEvery { stationKeywordRepository.isEmpty() } returns true

        val viewModel = getViewModel()

        viewModel.updateData()

        coVerify(exactly = 1) { stationsFileReader.readStations() }
        coVerify(exactly = 1) { stationsFileReader.readStationKeywords() }
        coVerify(exactly = 1) { stationRepository.updateDatabase(any()) }
        coVerify(exactly = 1) { stationKeywordRepository.updateDatabase(any()) }
    }

    @Test
    fun `database should not be updated when there is no internet and database is not empty`() {
        coEvery { networkManager.isCurrentlyConnected() } returns false
        coEvery { stationsFileReader.readStations() } returns Result.success(emptyList())
        coEvery { stationsFileReader.readStationKeywords() } returns Result.success(emptyList())
        coEvery { stationRepository.isEmpty() } returns false
        coEvery { stationKeywordRepository.isEmpty() } returns false

        val viewModel = getViewModel()

        viewModel.updateData()

        coVerify(exactly = 0) { stationsFileReader.readStations() }
        coVerify(exactly = 0) { stationsFileReader.readStationKeywords() }
        coVerify(exactly = 0) { stationRepository.updateDatabase(any()) }
        coVerify(exactly = 0) { stationKeywordRepository.updateDatabase(any()) }
    }

    @Test
    fun `remote update triggers when there is interent connection and should refresh`() {
        val remoteResult = Result.success(Unit)
        coEvery { networkManager.isCurrentlyConnected() } returns true
        coEvery { calculateShouldRefreshDataUseCase.invoke(any(), any()) } returns true
        coEvery { stationRepository.updateStationsRemote() } returns remoteResult
        coEvery { stationKeywordRepository.updateStationKeywordsRemote() } returns remoteResult

        val viewModel = getViewModel()

        viewModel.updateData()

        coVerify(exactly = 1) { stationRepository.updateStationsRemote() }
        coVerify(exactly = 1) { stationKeywordRepository.updateStationKeywordsRemote() }
        coVerify(exactly = 0) { stationsFileReader.readStations() }
        coVerify(exactly = 0) { stationsFileReader.readStationKeywords() }
    }

    @Test
    fun `remote update does not trigger when there is interent connection and should not refresh`() {
        val remoteResult = Result.success(Unit)
        coEvery { networkManager.isCurrentlyConnected() } returns true
        coEvery { calculateShouldRefreshDataUseCase.invoke(any(), any()) } returns false
        coEvery { stationRepository.updateStationsRemote() } returns remoteResult
        coEvery { stationKeywordRepository.updateStationKeywordsRemote() } returns remoteResult

        val viewModel = getViewModel()

        viewModel.updateData()

        coVerify(exactly = 0) { stationRepository.updateStationsRemote() }
        coVerify(exactly = 0) { stationKeywordRepository.updateStationKeywordsRemote() }
        coVerify(exactly = 0) { stationsFileReader.readStations() }
        coVerify(exactly = 0) { stationsFileReader.readStationKeywords() }
    }

    @Test
    fun `data should be loaded from file when remote update throws exception and database is empty`() {
        val remoteResult = Result.failure<Unit>(Exception())
        coEvery { networkManager.isCurrentlyConnected() } returns true
        coEvery { calculateShouldRefreshDataUseCase.invoke(any(), any()) } returns true
        coEvery { stationRepository.updateStationsRemote() } returns remoteResult
        coEvery { stationRepository.isEmpty() } returns true
        coEvery { stationKeywordRepository.isEmpty() } returns true
        val viewModel = getViewModel()

        viewModel.updateData()

        coVerify(exactly = 1) { stationsFileReader.readStations() }
        coVerify(exactly = 1) { stationsFileReader.readStationKeywords() }
    }

    @Test
    fun `search box A data should change on value changed`() = runTest {
        val initialData = SearchBoxData()
        val newData = SearchBoxData(value = "new data", isValid = false)

        val viewModel = getViewModel()

        viewModel.searchBoxAData.test {
            assertThat(awaitItem()).isEqualTo(initialData)
            viewModel.onSearchBoxAValueChanged(newData.value)
            assertThat(awaitItem()).isEqualTo(newData)
        }
    }

    @Test
    fun `search box B data should change on value changed`() = runTest {
        val initialData = SearchBoxData()
        val newData = SearchBoxData(value = "new data", isValid = false)

        val viewModel = getViewModel()

        viewModel.searchBoxBData.test {
            assertThat(awaitItem()).isEqualTo(initialData)
            viewModel.onSearchBoxBValueChanged(newData.value)
            assertThat(awaitItem()).isEqualTo(newData)
        }
    }

    @Test
    fun `prompt list is empty when search box is not selected`() = runTest {
        val viewModel = getViewModel()
        viewModel.onSearchBoxSelected(null)

        viewModel.prompts.test {
            assertThat(awaitItem()).isEmpty()
        }

        verify(exactly = 0) { getStationPromptsUseCase.invoke(any(), any(), any()) }
    }

    @Test
    fun `prompt list is correct when search box is selected`() = runTest {
        val prompts = listOf("prompt 1", "prompt 2", "prompt 3")
        every { getStationPromptsUseCase.invoke(any(), any(), any(), any()) } returns prompts

        val viewModel = getViewModel()
        viewModel.onSearchBoxSelected(SearchBoxType.A)

        viewModel.prompts.test {
            assertThat(awaitItem()).isEqualTo(prompts)
        }

        verify(exactly = 1) { getStationPromptsUseCase.invoke(any(), any(), any()) }
    }

    @Test
    fun `prompt list does not contain value of opposite search box`() = runTest {
        val useCasePrompts = listOf("prompt 1", "prompt 2", "prompt 3")
        val expectedPrompts = listOf(useCasePrompts[1], useCasePrompts[2])
        every { getStationPromptsUseCase.invoke(any(), any(), any(), any()) } returns useCasePrompts

        val viewModel = getViewModel()
        viewModel.onSearchBoxSelected(SearchBoxType.A)
        viewModel.onSearchBoxBValueChanged(useCasePrompts[0])

        viewModel.prompts.test {
            assertThat(awaitItem()).isEqualTo(expectedPrompts)
        }

        verify(exactly = 1) { getStationPromptsUseCase.invoke(any(), any(), any()) }
    }
}
