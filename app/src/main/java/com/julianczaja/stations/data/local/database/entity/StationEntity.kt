package com.julianczaja.stations.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.julianczaja.stations.data.model.Station

@Entity(tableName = "station")
data class StationEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val hits: Int
)

fun StationEntity.toStation() = Station(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    hits = hits
)
