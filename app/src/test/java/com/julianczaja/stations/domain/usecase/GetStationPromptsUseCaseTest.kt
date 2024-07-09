package com.julianczaja.stations.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.StationKeyword
import org.junit.Test


class GetStationPromptsUseCaseTest {

    private val useCase = GetStationPromptsUseCase()

    private val stations = listOf(
        Station(10L, "Warszawa Centralna NAME", 1.0, 2.0, 100),
        Station(20L, "Warszawa Zachodnia NAME", 1.0, 2.0, 200),
        Station(30L, "Warszawa Wschodnia NAME", 1.0, 2.0, 300),
        Station(40L, "Bydgoszcz Wschodnia NAME", 1.0, 2.0, 400),
        Station(50L, "Bydgoszcz Główna NAME", 1.0, 2.0, 500),
    )

    private val stationKeywords = listOf(
        StationKeyword(1L, "Warszawa Centralna", 10L),
        StationKeyword(2L, "Warszawa Zachodnia", 20L),
        StationKeyword(3L, "Warszawa Wschodnia", 30L),
        StationKeyword(4L, "Bydgoszcz Wschodnia", 40L),
        StationKeyword(5L, "Bydgoszcz Główna", 50L),
    )

    @Test
    fun `prompts for empty query should contain stations with highest hits value`() {
        val query = ""
        val expected = listOf(stations[4].name, stations[3].name, stations[2].name)
        val output = useCase.invoke(stations, stationKeywords, query, maxItemsForEmptyQuery = 3)

        assertThat(output).isEqualTo(expected)
    }

    @Test
    fun `prompts list should contain only sorted stations with names starting with query`() {
        val query = "Warszawa"
        val expected = listOf(stations[2].name, stations[1].name, stations[0].name)
        val output = useCase.invoke(stations, stationKeywords, query)

        assertThat(output).isEqualTo(expected)
    }

    @Test
    fun `prompts list should contain only sorted stations with names starting with query 2`() {
        val query = "B"
        val expected = listOf(stations[4].name, stations[3].name)
        val output = useCase.invoke(stations, stationKeywords, query)

        assertThat(output).isEqualTo(expected)
    }

    @Test
    fun `prompts list should be empty if prompts not found`() {
        val query = "asdas"
        val expected = emptyList<String>()
        val output = useCase.invoke(stations, stationKeywords, query)

        assertThat(output).isEqualTo(expected)
    }

    @Test
    fun `prompts list should be empty if stations and station keywords list are empty`() {
        val query = "B"
        val expected = emptyList<String>()
        val output = useCase.invoke(emptyList(), emptyList(), query)

        assertThat(output).isEqualTo(expected)
    }
}
