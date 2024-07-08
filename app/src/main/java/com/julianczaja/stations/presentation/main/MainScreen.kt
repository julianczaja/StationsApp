package com.julianczaja.stations.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.julianczaja.stations.R
import com.julianczaja.stations.data.model.Station
import com.julianczaja.stations.data.model.StationKeyword
import com.julianczaja.stations.presentation.components.AppBackground

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val stationKeywords by viewModel.stationKeywords.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updateData()
    }

    MainScreenContent(
        modifier = Modifier.fillMaxSize(),
        isUpdating = isUpdating,
        stations = stations,
        stationKeywords = stationKeywords,
    )
}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    isUpdating: Boolean,
    stations: List<Station>,
    stationKeywords: List<StationKeyword>
) {
    when (isUpdating) {
        true -> LoadingScreen(modifier)
        false -> {
            LazyColumn(
                modifier = modifier
            ) {
                items(stations) { station ->
                    Text(text = station.name)
                }
                items(stationKeywords) { station ->
                    Text(text = station.keyword)
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(text = stringResource(R.string.loading_data))
    }
}

//region Preview
@PreviewLightDark
@Composable
private fun MainScreenPreview() {
    AppBackground {
        MainScreenContent(
            modifier = Modifier.fillMaxSize(),
            isUpdating = false,
            stations = listOf(Station(1L, "name", 12.0, 15.0, 200)),
            stationKeywords = listOf(StationKeyword(1L, "keyword", 12L))
        )
    }
}

@PreviewLightDark
@Composable
private fun MainScreenLoadingPreview() {
    AppBackground {
        MainScreenContent(
            modifier = Modifier.fillMaxSize(),
            isUpdating = true,
            stations = listOf(Station(1L, "name", 12.0, 15.0, 200)),
            stationKeywords = listOf(StationKeyword(1L, "keyword", 12L))
        )
    }
}
//endregion