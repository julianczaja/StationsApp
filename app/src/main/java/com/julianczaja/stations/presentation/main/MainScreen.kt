package com.julianczaja.stations.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
    val prompts by viewModel.prompts.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updateData()
    }

    MainScreenContent(
        modifier = Modifier.fillMaxSize(),
        isUpdating = isUpdating,
        searchBoxAData = searchBoxAData,
        searchBoxBData = searchBoxBData,
        prompts = prompts,
        onSearchBoxSelected = viewModel::onSearchBoxSelected,
        onPromptClicked = viewModel::onPromptClicked,
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
    prompts: List<String>,
    onSearchBoxSelected: (SearchBoxType?) -> Unit,
    onPromptClicked: (String) -> Unit,
    onSearchBoxAValueChanged: (String) -> Unit,
    onSearchBoxBValueChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusARequester = FocusRequester()
    val focusBRequester = FocusRequester()
    val currentSearchBoxFocus = remember { mutableStateOf<SearchBoxType?>(null) }

    fun onFocusChanged(searchBoxType: SearchBoxType?, isFocused: Boolean) {
        when (searchBoxType) {
            SearchBoxType.A -> when {
                isFocused -> {
                    currentSearchBoxFocus.value = searchBoxType
                    onSearchBoxSelected(searchBoxType)
                }

                !isFocused && !searchBoxAData.isValid -> onSearchBoxAValueChanged("")
            }

            SearchBoxType.B -> when {
                isFocused -> {
                    currentSearchBoxFocus.value = searchBoxType
                    onSearchBoxSelected(searchBoxType)
                }

                !isFocused && !searchBoxBData.isValid -> onSearchBoxBValueChanged("")
            }

            null -> {
                currentSearchBoxFocus.value = null
                onSearchBoxSelected(null)
            }
        }
    }

    fun clearFocus() {
        focusManager.clearFocus()
        onFocusChanged(null, false)
    }

    fun promptClicked(prompt: String) {
        onPromptClicked(prompt)

        if (currentSearchBoxFocus.value == SearchBoxType.A && searchBoxBData.value.isEmpty()) {
            focusBRequester.requestFocus()
        } else if (currentSearchBoxFocus.value == SearchBoxType.B && searchBoxAData.value.isEmpty()) {
            focusARequester.requestFocus()
        } else {
            clearFocus()
        }
    }

    BackHandler(
        enabled = currentSearchBoxFocus.value != null,
        onBack = ::clearFocus
    )

    Column(modifier) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusARequester),
                data = searchBoxAData,
                onValueChange = onSearchBoxAValueChanged,
                placeholderText = stringResource(R.string.from_label),
                onFocusChange = { onFocusChanged(SearchBoxType.A, it) }
            )
            SearchBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusBRequester),
                data = searchBoxBData,
                onValueChange = onSearchBoxBValueChanged,
                placeholderText = stringResource(R.string.to_label),
                onFocusChange = { onFocusChanged(SearchBoxType.B, it) }
            )
        }
        HorizontalDivider()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            this@Column.AnimatedVisibility(
                visible = currentSearchBoxFocus.value == null,
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
                    Text(text = "DISTANCE")
                }
            }

            this@Column.AnimatedVisibility(
                visible = currentSearchBoxFocus.value != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = fadeOut()
            ) {
                when {
                    prompts.isEmpty() && currentSearchBoxFocus.value != null -> NoResultsScreen(
                        modifier = modifier
                    )

                    else -> PromptsContent(
                        modifier = modifier,
                        prompts = prompts,
                        onPromptClicked = ::promptClicked
                    )
                }
            }
        }
    }
    AnimatedVisibility(
        visible = isUpdating,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box {
            LoadingScreen(modifier)
        }
    }
}

@Composable
private fun PromptsContent(
    modifier: Modifier = Modifier,
    prompts: List<String>,
    onPromptClicked: (String) -> Unit
) {
    LazyColumn(modifier) {
        items(prompts) { prompt ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPromptClicked(prompt) }
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = prompt
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun NoResultsScreen(modifier: Modifier = Modifier) {
    Box(modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                modifier = Modifier.size(100.dp),
                painter = painterResource(id = R.drawable.search_not_found),
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.no_results_label),
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = .7f)),
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
            searchBoxAData = SearchBoxData("Some text A"),
            searchBoxBData = SearchBoxData("Some text B", isValid = true),
            prompts = listOf("A", "B", "C"),
            onSearchBoxSelected = {},
            onPromptClicked = {},
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
            searchBoxBData = SearchBoxData("Some text B", isValid = true),
            prompts = listOf("A", "B", "C"),
            onSearchBoxSelected = {},
            onPromptClicked = {},
            onSearchBoxAValueChanged = {},
            onSearchBoxBValueChanged = {}
        )
    }
}
//endregion
