package com.julianczaja.stations.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppDataRepository {
    fun getLastDataUpdateTimestamp(): Flow<Long>
    suspend fun setLastDataUpdateTimestamp(timestamp: Long)
}
