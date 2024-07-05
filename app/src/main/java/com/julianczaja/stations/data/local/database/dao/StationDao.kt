package com.julianczaja.stations.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.julianczaja.stations.data.local.database.entity.StationEntity

@Dao
interface StationDao : BaseDao<StationEntity> {

    @Query("SELECT * FROM station")
    fun getAll(): List<StationEntity>

    @Query("DELETE FROM station")
    suspend fun deleteAll()

    @Query("SELECT (SELECT COUNT(*) FROM station_keyword) == 0")
    suspend fun isEmpty(): Boolean
}
