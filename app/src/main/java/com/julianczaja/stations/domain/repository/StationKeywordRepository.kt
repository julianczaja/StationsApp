package com.julianczaja.stations.domain.repository

import com.julianczaja.stations.data.model.StationKeyword
import kotlinx.coroutines.flow.Flow

interface StationKeywordRepository {
    fun getStationKeywordsFromDatabase(): Flow<List<StationKeyword>>
    suspend fun isEmpty(): Boolean
    suspend fun updateStationKeywordsRemote(): Result<Unit>
    suspend fun updateDatabase(stations: List<StationKeyword>)
}
