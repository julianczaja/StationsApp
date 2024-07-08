package com.julianczaja.stations.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julianczaja.stations.di.IoDispatcher
import com.julianczaja.stations.domain.StationsFileReader
import com.julianczaja.stations.domain.repository.AppDataRepository
import com.julianczaja.stations.domain.repository.StationKeywordRepository
import com.julianczaja.stations.domain.repository.StationRepository
import com.julianczaja.stations.domain.usecase.CalculateShouldRefreshDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val stationsFileReader: StationsFileReader,
    private val stationRepository: StationRepository,
    private val stationKeywordRepository: StationKeywordRepository,
    private val appDataRepository: AppDataRepository,
    private val calculateShouldRefreshDataUseCase: CalculateShouldRefreshDataUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private companion object {
        const val ONE_DAY_MILLIS = 86_400_000L
        const val REFRESH_INTERVAL_MILLIS = ONE_DAY_MILLIS
    }

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    val stations = stationRepository.getStationsFromDatabase()
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val stationKeywords = stationKeywordRepository.getStationKeywordsFromDatabase()
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun updateData() {
        val isConnectedToNetwork = false // TODO: Check internet connection

        viewModelScope.launch(ioDispatcher) {
            _isUpdating.update { true }
            when (isConnectedToNetwork) {
                true -> updateDatabaseFromNetwork()
                false -> updateDatabaseFromJson()
            }
            _isUpdating.update { false }
        }
    }

    private suspend fun updateDatabaseFromNetwork() {
        val shouldRefresh = calculateShouldRefreshDataUseCase(
            currentTimestamp = getCurrentTimestamp(),
            refreshInterval = REFRESH_INTERVAL_MILLIS
        )

        if (!shouldRefresh) return

        stationRepository.updateStationsRemote()
            .onFailure { Timber.e("updateStationsRemote error: $it") }
            .onSuccess { appDataRepository.setLastDataUpdateTimestamp(getCurrentTimestamp()) }
    }

    private suspend fun updateDatabaseFromJson() {
        if (stationRepository.isEmpty()) {
            stationsFileReader.readStations()
                .onFailure { e -> Timber.e("Failed to read stations from file: $e") }
                .onSuccess { stations -> stationRepository.updateDatabase(stations) }
        }
        if (stationKeywordRepository.isEmpty()) {
            stationsFileReader.readStationKeywords()
                .onFailure { e -> Timber.e("Failed to read station keywords from file: $e") }
                .onSuccess { keywords -> stationKeywordRepository.updateDatabase(keywords) }
        }
    }

    private fun getCurrentTimestamp() = System.currentTimeMillis()
}
