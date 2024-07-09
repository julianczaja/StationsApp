package com.julianczaja.stations.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class NormalizeStringUseCaseTest {

    @Test
    fun `output string should not contain polish signs`() {
        val inputString = "Łódź"
        val expected = "lodz"
        val useCase = NormalizeStringUseCase()

        assertThat(useCase.invoke(inputString)).isEqualTo(expected)
    }

    @Test
    fun `input with space should be mapped to correct output without polish signs`() {
        val inputString = "Warszawa Śródmieście"
        val expected = "warszawa srodmiescie"
        val useCase = NormalizeStringUseCase()

        assertThat(useCase.invoke(inputString)).isEqualTo(expected)
    }

    @Test
    fun `input with spaces and number should be mapped to correct output without polish signs`() {
        val inputString = "Małczew 8 kier. Andrespol"
        val expected = "malczew 8 kier. andrespol"
        val useCase = NormalizeStringUseCase()

        assertThat(useCase.invoke(inputString)).isEqualTo(expected)
    }
}
