package com.julianczaja.stations.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.julianczaja.stations.data.local.database.dao.StationDao
import com.julianczaja.stations.data.local.database.dao.StationKeywordDao
import com.julianczaja.stations.data.local.database.entity.StationEntity
import com.julianczaja.stations.data.local.database.entity.StationKeywordEntity

@Database(
    entities = [
        StationEntity::class,
        StationKeywordEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun stationKeywordDao(): StationKeywordDao
}
