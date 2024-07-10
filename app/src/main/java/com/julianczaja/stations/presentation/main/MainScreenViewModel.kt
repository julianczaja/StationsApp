package com.julianczaja.stations.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julianczaja.stations.data.NetworkManager
import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.StationKeyword
import com.julianczaja.stations.di.IoDispatcher
import com.julianczaja.stations.domain.StationsFileReader
import com.julianczaja.stations.domain.repository.AppDataRepository
import com.julianczaja.stations.domain.repository.StationKeywordRepository
import com.julianczaja.stations.domain.repository.StationRepository
import com.julianczaja.stations.domain.usecase.CalculateDistanceBetweenStationsUseCase
import com.julianczaja.stations.domain.usecase.CalculateShouldRefreshDataUseCase
import com.julianczaja.stations.domain.usecase.GetStationPromptsUseCase
import com.julianczaja.stations.domain.usecase.NormalizeStringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val stationsFileReader: StationsFileReader,
    private val networkManager: NetworkManager,
    private val stationRepository: StationRepository,
    private val stationKeywordRepository: StationKeywordRepository,
    private val appDataRepository: AppDataRepository,
    private val calculateShouldRefreshDataUseCase: CalculateShouldRefreshDataUseCase,
    private val getStationPromptsUseCase: GetStationPromptsUseCase,
    private val normalizeStringUseCase: NormalizeStringUseCase,
    private val calculateDistanceBetweenStationsUseCase: CalculateDistanceBetweenStationsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private companion object {
        const val ONE_DAY_MILLIS = 86_400_000L
        const val REFRESH_INTERVAL_MILLIS = ONE_DAY_MILLIS
    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _searchBoxAData = MutableStateFlow(SearchBoxData())
    val searchBoxAData = _searchBoxAData.asStateFlow()

    private val _searchBoxBData = MutableStateFlow(SearchBoxData())
    val searchBoxBData = _searchBoxBData.asStateFlow()

    private val _distance = MutableStateFlow<Float?>(null)
    val distance = _distance.asStateFlow()

    private val _selectedSearchBox = MutableStateFlow<SearchBoxType?>(null)

    private val _stations = stationRepository.getStationsFromDatabase()
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _stationKeywords = stationKeywordRepository.getStationKeywordsFromDatabase()
        .normalize()
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val prompts: StateFlow<ImmutableList<String>> = combine(
        _stations,
        _stationKeywords,
        _searchBoxAData,
        _searchBoxBData,
        _selectedSearchBox
    ) { stations, stationKeywords, searchBoxAData, searchBoxBData, selectedSearchBox ->
        return@combine getPrompts(
            stations = stations,
            stationKeywords = stationKeywords,
            searchBoxAData = searchBoxAData,
            searchBoxBData = searchBoxBData,
            selectedSearchBox = selectedSearchBox
        ).toImmutableList()
    }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf()
        )

    private fun getPrompts(
        stations: List<Station>,
        stationKeywords: List<StationKeyword>,
        searchBoxAData: SearchBoxData,
        searchBoxBData: SearchBoxData,
        selectedSearchBox: SearchBoxType?
    ) = when (selectedSearchBox) {
        SearchBoxType.A,
        SearchBoxType.B -> try {
            getStationPromptsUseCase(
                stations = stations,
                stationKeywords = stationKeywords,
                query = when (selectedSearchBox) {
                    SearchBoxType.A -> searchBoxAData.value
                    SearchBoxType.B -> searchBoxBData.value
                }
                    .trim()
                    .normalize()
            )
                .filterNot { prompt ->
                    prompt == when (selectedSearchBox) {
                        SearchBoxType.A -> searchBoxBData.value
                        SearchBoxType.B -> searchBoxAData.value
                    }
                }
        } catch (e: Exception) {
            Timber.e("Failed to get prompts: $e")
            emptyList()
        }

        null -> emptyList()
    }

    fun onPromptClicked(prompt: String) {
        when (_selectedSearchBox.value) {
            SearchBoxType.A -> _searchBoxAData.update { SearchBoxData(prompt, true) }
            SearchBoxType.B -> _searchBoxBData.update { SearchBoxData(prompt, true) }
            null -> Unit
        }
    }

    fun onSearchBoxSelected(searchBoxType: SearchBoxType?) {
        _selectedSearchBox.update { searchBoxType }
    }

    fun onSearchBoxAValueChanged(value: String) {
        _searchBoxAData.update { SearchBoxData(value) }
        _distance.update { null }
    }

    fun onSearchBoxBValueChanged(value: String) {
        _searchBoxBData.update { SearchBoxData(value) }
        _distance.update { null }
    }

    fun calculateDistance() = viewModelScope.launch(ioDispatcher) {
        val searchBoxA = _searchBoxAData.value
        val searchBoxB = _searchBoxBData.value
        val stationA = _stations.value.find { it.name == searchBoxA.value }
        val stationB = _stations.value.find { it.name == searchBoxB.value }

        try {
            if (stationA == null || stationB == null) {
                throw Exception("Can't calculate distance because one of stations is null")
            } else {
                _distance.update { calculateDistanceBetweenStationsUseCase(stationA, stationB) }
            }
        } catch (e: Exception) {
            Timber.e("Distance calculation error: $e")
            _searchBoxAData.update { SearchBoxData() }
            _searchBoxBData.update { SearchBoxData() }
            _distance.update { null }
            _eventFlow.emit(Event.DISTANCE_CALCULATION_ERROR)
        }
    }

    fun updateData() {
        val isConnectedToNetwork = networkManager.isCurrentlyConnected()

        viewModelScope.launch(ioDispatcher) {
            when (isConnectedToNetwork) {
                true -> updateDatabaseFromNetwork()
                false -> preloadDatabaseFromJsonFile()
            }
        }
    }

    private suspend fun updateDatabaseFromNetwork() {
        val shouldRefresh = calculateShouldRefreshDataUseCase(
            currentTimestamp = getCurrentTimestamp(),
            refreshInterval = REFRESH_INTERVAL_MILLIS
        )

        if (!shouldRefresh) return

        _isUpdating.update { true }

        try {
            stationRepository.updateStationsRemote().getOrThrow()
            stationKeywordRepository.updateStationKeywordsRemote().getOrThrow()
            appDataRepository.setLastDataUpdateTimestamp(getCurrentTimestamp())
        } catch (e: Exception) {
            Timber.e("Remote data update error: $e")
            _eventFlow.emit(Event.REMOTE_UPDATE_ERROR)
            preloadDatabaseFromJsonFile()
        }

        _isUpdating.update { false }
    }

    private suspend fun preloadDatabaseFromJsonFile() {
        val areStationsEmpty = stationRepository.isEmpty()
        val areStationKeywordsEmpty = stationKeywordRepository.isEmpty()

        if (!areStationsEmpty && !areStationKeywordsEmpty) return

        _isUpdating.update { true }

        try {
            if (areStationsEmpty) {
                stationsFileReader.readStations()
                    .onSuccess { stations -> stationRepository.updateDatabase(stations) }
                    .getOrThrow()
            }
            if (areStationKeywordsEmpty) {
                stationsFileReader.readStationKeywords()
                    .onSuccess { keywords -> stationKeywordRepository.updateDatabase(keywords) }
                    .getOrThrow()
            }
        } catch (e: Exception) {
            Timber.e("Failed to read stations files: $e")
            _eventFlow.emit(Event.CACHED_DATA_ERROR)
        }

        _isUpdating.update { false }
    }

    private fun getCurrentTimestamp() = System.currentTimeMillis()

    private fun String.normalize() = normalizeStringUseCase(this)

    private fun Flow<List<StationKeyword>>.normalize() = this.map { stationKeywords ->
        stationKeywords.map { staionKeyword ->
            staionKeyword.copy(keyword = normalizeStringUseCase(staionKeyword.keyword))
        }
    }

    enum class Event {
        DISTANCE_CALCULATION_ERROR,
        REMOTE_UPDATE_ERROR,
        CACHED_DATA_ERROR
    }
}
