package com.julianczaja.stations.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.julianczaja.stations.presentation.components.AppBackground

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updateData()
    }

    MainScreenContent(
        modifier = Modifier.fillMaxSize(),
        isUpdating = isUpdating
    )
}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    isUpdating: Boolean
) {
    when (isUpdating) {
        true -> LoadingScreen(modifier)
        false -> {
            Box(
                modifier = modifier
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Hello world!"
                )
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
        MainScreenContent(modifier = Modifier.fillMaxSize(), isUpdating = false)
    }
}

@PreviewLightDark
@Composable
private fun MainScreenLoadingPreview() {
    AppBackground {
        MainScreenContent(modifier = Modifier.fillMaxSize(), isUpdating = true)
    }
}
//endregion