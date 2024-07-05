package com.julianczaja.stations.data.model

import com.julianczaja.stations.data.local.database.entity.StationEntity
import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val hits: Int
)

fun Station.toStationEntity() = StationEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    hits = hits
)
