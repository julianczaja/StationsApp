package com.julianczaja.stations.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.julianczaja.stations.data.local.database.entity.StationKeywordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StationKeywordDao : BaseDao<StationKeywordEntity> {

    @Query("SELECT * FROM station_keyword")
    fun getAll(): Flow<List<StationKeywordEntity>>

    @Query("DELETE FROM station_keyword")
    suspend fun deleteAll()

    @Query("SELECT (SELECT COUNT(*) FROM station_keyword) == 0")
    suspend fun isEmpty(): Boolean
}
