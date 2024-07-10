package com.julianczaja.stations.domain.usecase

import com.julianczaja.stations.data.model.Station
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class CalculateDistanceBetweenStationsUseCase @Inject constructor() {

    operator fun invoke(stationA: Station, stationB: Station) = haversine(
        lat1 = stationA.latitude,
        lon1 = stationA.longitude,
        lat2 = stationB.latitude,
        lon2 = stationB.longitude
    ).toFloat()

    /**
     * https://en.wikipedia.org/wiki/Haversine_formula
     */
    @Suppress("LocalVariableName")
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return R * c
    }
}
