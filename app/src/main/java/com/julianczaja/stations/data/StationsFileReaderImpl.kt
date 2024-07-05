package com.julianczaja.stations.data

import android.content.Context
import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.StationKeyword
import com.julianczaja.stations.di.IoDispatcher
import com.julianczaja.stations.domain.StationsFileReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject


class StationsFileReaderImpl @Inject constructor(
    private val context: Context,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StationsFileReader {

    override suspend fun readStations() = withContext(ioDispatcher) {
        return@withContext try {
            context.assets.open("stations.json").bufferedReader().use { reader ->
                val jsonText = reader.readText()
                val stations = json.decodeFromString<List<Station>>(jsonText)
                Result.success(stations)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun readStationKeywords() = withContext(ioDispatcher) {
        return@withContext try {
            context.assets.open("station_keywords.json").bufferedReader().use { reader ->
                val jsonText = reader.readText()
                val stationKeywords = json.decodeFromString<List<StationKeyword>>(jsonText)
                Result.success(stationKeywords)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
