package com.julianczaja.stations.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.julianczaja.stations.data.model.StationKeyword

@Entity(tableName = "station_keyword")
data class StationKeywordEntity(
    @PrimaryKey val id: Long,
    val keyword: String,
    val stationId: Long,
)

fun StationKeywordEntity.toStationKeyword() = StationKeyword(
    id = id,
    keyword = keyword,
    stationId = stationId
)
