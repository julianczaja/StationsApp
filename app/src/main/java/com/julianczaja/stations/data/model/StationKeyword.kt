package com.julianczaja.stations.data.model

import com.julianczaja.stations.data.local.database.entity.StationKeywordEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StationKeyword(
    val id: Long,
    val keyword: String,
    @SerialName("station_id")
    val stationId: Long,
)

fun StationKeyword.toStationKeywordEntity() = StationKeywordEntity(
    id = id,
    keyword = keyword,
    stationId = stationId
)
