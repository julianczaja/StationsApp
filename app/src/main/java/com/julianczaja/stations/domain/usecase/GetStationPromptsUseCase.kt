package com.julianczaja.stations.domain.usecase

import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.StationKeyword
import javax.inject.Inject

class GetStationPromptsUseCase @Inject constructor() {

    operator fun invoke(
        stations: List<Station>,
        stationKeywords: List<StationKeyword>,
        query: String,
        maxItemsForEmptyQuery: Int = 10
    ) = when (query.isEmpty()) {
        true -> getPromptsForEmptyQuery(stations, maxItemsForEmptyQuery)
        false -> getPromptsForQuery(stations, stationKeywords, query)
    }

    private fun getPromptsForEmptyQuery(stations: List<Station>, maxItemsForEmptyQuery: Int) =
        stations
            .sortedByDescending { it.hits }
            .take(maxItemsForEmptyQuery)
            .map { it.name }

    private fun getPromptsForQuery(
        stations: List<Station>,
        stationKeywords: List<StationKeyword>,
        query: String
    ) = stationKeywords
        .filter { it.keyword.startsWith(query) }
        .mapNotNull { stationKeyword -> stations.find { it.id == stationKeyword.stationId } }
        .sortedByDescending { it.hits }
        .map { it.name }
}
