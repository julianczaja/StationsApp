package com.julianczaja.stations.domain

import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.StationKeyword

interface StationsFileReader {
    suspend fun readStations(): Result<List<Station>>
    suspend fun readStationKeywords(): Result<List<StationKeyword>>
}
