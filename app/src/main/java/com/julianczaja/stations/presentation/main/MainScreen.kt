package com.julianczaja.stations.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.julianczaja.stations.R
import com.julianczaja.stations.presentation.components.AppBackground
import com.julianczaja.stations.presentation.main.components.SearchBox


@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()
    val searchBoxAData by viewModel.searchBoxAData.collectAsStateWithLifecycle()
    val searchBoxBData by viewModel.searchBoxBData.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updateData()
    }

    MainScreenContent(
        modifier = Modifier.fillMaxSize(),
        isUpdating = isUpdating,
        searchBoxAData = searchBoxAData,
        searchBoxBData = searchBoxBData,
        onSearchBoxAValueChanged = viewModel::onSearchBoxAValueChanged,
        onSearchBoxBValueChanged = viewModel::onSearchBoxBValueChanged
    )
}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    isUpdating: Boolean,
    searchBoxAData: SearchBoxData,
    searchBoxBData: SearchBoxData,
    onSearchBoxAValueChanged: (String) -> Unit,
    onSearchBoxBValueChanged: (String) -> Unit,
) {
    val isFirstFocused = remember { mutableStateOf(false) }
    val isSecondFocused = remember { mutableStateOf(false) }
    val areBothNotFocused = !isFirstFocused.value && !isSecondFocused.value
    val isAnyFocused = isFirstFocused.value || isSecondFocused.value

    Column(
        modifier = modifier.safeDrawingPadding()
    ) {
        SearchBox(
            data = searchBoxAData,
            onValueChange = onSearchBoxAValueChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            placeholderText = "From",
            onFocusChange = { isFirstFocused.value = it }
        )
        SearchBox(
            data = searchBoxBData,
            onValueChange = onSearchBoxBValueChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            placeholderText = "To",
            onFocusChange = { isSecondFocused.value = it }
        )
        HorizontalDivider(Modifier.padding(vertical = 4.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            this@Column.AnimatedVisibility(
                visible = areBothNotFocused,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.TopStart),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // TODO: Add distance calculation content
                }
            }

            this@Column.AnimatedVisibility(
                visible = isAnyFocused,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // TODO: Add prompt items
                }
            }
        }
    }
    AnimatedVisibility(
        visible = isUpdating,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = .7f)),
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = stringResource(R.string.loading_data))
            }
        }
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
            searchBoxAData = SearchBoxData("Some text A"),
            searchBoxBData = SearchBoxData("Some text B"),
            onSearchBoxAValueChanged = {},
            onSearchBoxBValueChanged = {}
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
            searchBoxAData = SearchBoxData("Some text A"),
            searchBoxBData = SearchBoxData("Some text B"),
            onSearchBoxAValueChanged = {},
            onSearchBoxBValueChanged = {}
        )
    }
}
//endregion
