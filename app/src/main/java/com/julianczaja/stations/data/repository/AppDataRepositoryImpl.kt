package com.julianczaja.stations.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.julianczaja.stations.data.local.datastore.DataStoreKeys.LAST_DATA_UPDATE_KEY
import com.julianczaja.stations.domain.repository.AppDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppDataRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppDataRepository {

    private companion object {
        const val DEFAULT_LAST_UPDATE_TIMESTAMP = 0L
    }

    override fun getLastDataUpdateTimestamp(): Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[LAST_DATA_UPDATE_KEY] ?: DEFAULT_LAST_UPDATE_TIMESTAMP
        }

    override suspend fun setLastDataUpdateTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_DATA_UPDATE_KEY] = timestamp
        }
    }
}
