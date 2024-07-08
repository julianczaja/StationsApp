package com.julianczaja.stations.data.repository

import com.julianczaja.stations.data.local.database.dao.StationKeywordDao
import com.julianczaja.stations.data.local.database.entity.toStationKeyword
import com.julianczaja.stations.data.model.StationKeyword
import com.julianczaja.stations.data.model.toStationKeywordEntity
import com.julianczaja.stations.data.remote.KoleoApi
import com.julianczaja.stations.domain.repository.StationKeywordRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StationKeywordRepositoryImpl @Inject constructor(
    private val koleoApi: KoleoApi,
    private val stationKeywordDao: StationKeywordDao,
) : StationKeywordRepository {

    override fun getStationKeywordsFromDatabase() =
        stationKeywordDao.getAll().map { entities -> entities.map { it.toStationKeyword() } }

    override suspend fun isEmpty() = stationKeywordDao.isEmpty()

    override suspend fun updateStationKeywordsRemote(): Result<Unit> {
        return try {
            val stationKeywords = koleoApi.getStationKeywords()
            updateDatabase(stationKeywords)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDatabase(stations: List<StationKeyword>) {
        stationKeywordDao.withTransaction {
            stationKeywordDao.deleteAll()
            stationKeywordDao.insertAll(stations.map { it.toStationKeywordEntity() })
        }
    }
}
