package com.julianczaja.stations.domain.repository

import com.julianczaja.stations.data.model.Station
import kotlinx.coroutines.flow.Flow

interface StationRepository {
    fun getStationsFromDatabase(): Flow<List<Station>>
    suspend fun isEmpty(): Boolean
    suspend fun updateStationsRemote(): Result<Unit>
    suspend fun updateDatabase(stations: List<Station>)
}
