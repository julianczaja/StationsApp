package com.julianczaja.stations.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.julianczaja.stations.data.local.database.entity.StationKeywordEntity

@Dao
interface StationKeywordDao : BaseDao<StationKeywordEntity> {

    @Query("SELECT * FROM station_keyword")
    fun getAll(): List<StationKeywordEntity>

    @Query("DELETE FROM station_keyword")
    suspend fun deleteAll()
}
