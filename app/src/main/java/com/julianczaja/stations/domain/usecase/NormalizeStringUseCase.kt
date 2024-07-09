package com.julianczaja.stations.domain.usecase

import javax.inject.Inject

class NormalizeStringUseCase @Inject constructor() {

    private val polishSignsToNormalizedMap = mapOf(
        'ą' to 'a',
        'ć' to 'c',
        'ę' to 'e',
        'ł' to 'l',
        'ń' to 'n',
        'ó' to 'o',
        'ś' to 's',
        'ź' to 'z',
        'ż' to 'z',
    )

    operator fun invoke(string: String) = string
        .lowercase()
        .map { polishSignsToNormalizedMap[it] ?: it }
        .joinToString("")
}
