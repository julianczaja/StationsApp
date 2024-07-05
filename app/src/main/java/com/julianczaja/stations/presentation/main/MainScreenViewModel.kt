package com.julianczaja.stations.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julianczaja.stations.data.local.database.dao.StationDao
import com.julianczaja.stations.data.local.database.dao.StationKeywordDao
import com.julianczaja.stations.data.model.toStationEntity
import com.julianczaja.stations.data.model.toStationKeywordEntity
import com.julianczaja.stations.di.IoDispatcher
import com.julianczaja.stations.domain.StationsFileReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val stationsFileReader: StationsFileReader,
    private val stationDao: StationDao,
    private val stationKeywordDao: StationKeywordDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

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
        val shouldRefresh = false // TODO: Check if data was fetched in last 24h

        if (shouldRefresh) {
            // TODO: Fetch data from network and update database
        }
    }

    private suspend fun updateDatabaseFromJson() {
        if (stationDao.isEmpty()) {
            stationsFileReader.readStations()
                .onFailure { e -> Timber.e("Failed to read stations from file: $e") }
                .onSuccess { stations ->
                    val entites = stations.map { it.toStationEntity() }
                    stationDao.insertAll(entites)
                }
        }
        if (stationKeywordDao.isEmpty()) {
            stationsFileReader.readStationKeywords()
                .onFailure { e -> Timber.e("Failed to read station keywords from file: $e") }
                .onSuccess { keywords ->
                    val entites = keywords.map { it.toStationKeywordEntity() }
                    stationKeywordDao.insertAll(entites)
                }
        }
    }
}
