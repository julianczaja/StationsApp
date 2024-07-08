package com.julianczaja.stations.data.repository

import com.julianczaja.stations.data.local.database.dao.StationDao
import com.julianczaja.stations.data.local.database.entity.toStation
import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.toStationEntity
import com.julianczaja.stations.data.remote.KoleoApi
import com.julianczaja.stations.domain.repository.StationRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StationRepositoryImpl @Inject constructor(
    private val koleoApi: KoleoApi,
    private val stationDao: StationDao,
) : StationRepository {

    override fun getStationsFromDatabase() =
        stationDao.getAll().map { entities -> entities.map { it.toStation() } }

    override suspend fun isEmpty() = stationDao.isEmpty()

    override suspend fun updateStationsRemote(): Result<Unit> {
        return try {
            val stations = koleoApi.getStations()
            updateDatabase(stations)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDatabase(stations: List<Station>) {
        stationDao.withTransaction {
            stationDao.deleteAll()
            stationDao.insertAll(stations.map { it.toStationEntity() })
        }
    }
}
