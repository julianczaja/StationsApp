package com.julianczaja.stations.data.remote

import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.StationKeyword
import retrofit2.http.GET


interface KoleoApi {

    @GET(value = "/api/v2/main/stations")
    suspend fun getStations(): List<Station>

    @GET(value = "/api/v2/main/station_keywords")
    suspend fun getStationKeywords(): List<StationKeyword>
}
