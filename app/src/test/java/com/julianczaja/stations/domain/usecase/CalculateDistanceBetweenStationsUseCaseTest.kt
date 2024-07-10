package com.julianczaja.stations.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.julianczaja.stations.data.model.Station
import org.junit.Test


class CalculateDistanceBetweenStationsUseCaseTest {

    private val useCase = CalculateDistanceBetweenStationsUseCase()

    @Test
    fun `distance between stations should be correct`() {
        val stationA = Station(
            id = 1L,
            name = "Name 1",
            latitude = 51.09833743,
            longitude = 17.03889026,
            hits = 20
        )
        val stationB = Station(
            id = 1L,
            name = "Name 2",
            latitude = 50.257603,
            longitude = 19.017186,
            hits = 10
        )
        val expected = 167.83096644227174.toFloat()


        assertThat(useCase.invoke(stationA, stationB)).isEqualTo(expected)
    }

    @Test
    fun `distance between stations should be correct 2`() {
        val stationA = Station(
            id = 1L,
            name = "Name 1",
            latitude = 52.232222,
            longitude = 21.008333,
            hits = 10
        )
        val stationB = Station(
            id = 1L,
            name = "Name 2",
            latitude = 53.41889316,
            longitude = 14.55000116,
            hits = 20
        )
        val expected = 453.3610100668323.toFloat()


        assertThat(useCase.invoke(stationA, stationB)).isEqualTo(expected)
    }

    @Test
    fun `distance between the same stations should be zero`() {
        val station = Station(
            id = 1L,
            name = "Name 1",
            latitude = 52.232222,
            longitude = 21.008333,
            hits = 10
        )
        val expected = 0f

        assertThat(useCase.invoke(station, station)).isEqualTo(expected)
    }
}
