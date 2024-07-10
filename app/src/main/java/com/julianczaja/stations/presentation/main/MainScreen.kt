package com.julianczaja.stations.presentation.main

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.julianczaja.stations.R
import com.julianczaja.stations.presentation.components.AppBackground
import com.julianczaja.stations.presentation.main.MainScreenViewModel.Event.CACHED_DATA_ERROR
import com.julianczaja.stations.presentation.main.MainScreenViewModel.Event.DISTANCE_CALCULATION_ERROR
import com.julianczaja.stations.presentation.main.MainScreenViewModel.Event.REMOTE_UPDATE_ERROR
import com.julianczaja.stations.presentation.main.components.SearchBox


@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var dataUpdated by rememberSaveable { mutableStateOf(false) }

    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()
    val searchBoxAData by viewModel.searchBoxAData.collectAsStateWithLifecycle()
    val searchBoxBData by viewModel.searchBoxBData.collectAsStateWithLifecycle()
    val prompts by viewModel.prompts.collectAsStateWithLifecycle()
    val distance by viewModel.distance.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (!dataUpdated) {
            viewModel.updateData()
            dataUpdated = true
        }

        viewModel.eventFlow.collect { event ->
            when (event) {
                DISTANCE_CALCULATION_ERROR -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.distance_calculation_error_message),
                    withDismissAction = true
                )

                REMOTE_UPDATE_ERROR -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.remote_update_error_message),
                    withDismissAction = true
                )

                CACHED_DATA_ERROR -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.cached_data_error_message),
                        actionLabel = context.getString(R.string.retry_action_label),
                        duration = SnackbarDuration.Short
                    )
                    when (result) {
                        SnackbarResult.Dismissed -> Unit
                        SnackbarResult.ActionPerformed -> viewModel.updateData()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = {
            MainScreenContent(
                modifier = Modifier.fillMaxSize(),
                isUpdating = isUpdating,
                searchBoxAData = searchBoxAData,
                searchBoxBData = searchBoxBData,
                prompts = prompts,
                distance = distance,
                onSearchBoxSelected = viewModel::onSearchBoxSelected,
                onPromptClicked = viewModel::onPromptClicked,
                onSearchBoxAValueChanged = viewModel::onSearchBoxAValueChanged,
                onSearchBoxBValueChanged = viewModel::onSearchBoxBValueChanged,
                calculateDistance = viewModel::calculateDistance
            )
        }
    )
}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    isUpdating: Boolean,
    searchBoxAData: SearchBoxData,
    searchBoxBData: SearchBoxData,
    prompts: List<String>,
    distance: Float?,
    onSearchBoxSelected: (SearchBoxType?) -> Unit,
    onPromptClicked: (String) -> Unit,
    onSearchBoxAValueChanged: (String) -> Unit,
    onSearchBoxBValueChanged: (String) -> Unit,
    calculateDistance: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusARequester = remember { FocusRequester() }
    val focusBRequester = remember { FocusRequester() }
    var currentSearchBoxFocus by remember { mutableStateOf<SearchBoxType?>(null) }

    val distanceAnimatable = remember { Animatable(0f, 0.01f) }

    LaunchedEffect(distance) {
        when (distance) {
            null -> distanceAnimatable.snapTo(0f)
            else -> distanceAnimatable.animateTo(
                targetValue = distance,
                animationSpec = tween(
                    durationMillis = 1500,
                    delayMillis = 300,
                    easing = EaseOutExpo
                )
            )
        }
    }

    fun onFocusChanged(searchBoxType: SearchBoxType?, isFocused: Boolean) {
        when (searchBoxType) {
            SearchBoxType.A -> when {
                isFocused -> {
                    currentSearchBoxFocus = searchBoxType
                    onSearchBoxSelected(searchBoxType)
                }

                !isFocused && !searchBoxAData.isValid -> onSearchBoxAValueChanged("")
            }

            SearchBoxType.B -> when {
                isFocused -> {
                    currentSearchBoxFocus = searchBoxType
                    onSearchBoxSelected(searchBoxType)
                }

                !isFocused && !searchBoxBData.isValid -> onSearchBoxBValueChanged("")
            }

            null -> {
                currentSearchBoxFocus = null
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

        if (currentSearchBoxFocus == SearchBoxType.A && searchBoxBData.value.isEmpty()) {
            focusBRequester.requestFocus()
        } else if (currentSearchBoxFocus == SearchBoxType.B && searchBoxAData.value.isEmpty()) {
            focusARequester.requestFocus()
        } else {
            clearFocus()
            calculateDistance()
        }
    }

    BackHandler(
        enabled = currentSearchBoxFocus != null,
        onBack = ::clearFocus
    )

    Column(modifier) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .statusBarsPadding()
                .displayCutoutPadding()
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
                visible = currentSearchBoxFocus == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DistanceScreen(
                    modifier = modifier,
                    distance = if (distance == null) null else distanceAnimatable.value,
                )
            }

            this@Column.AnimatedVisibility(
                visible = currentSearchBoxFocus != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = fadeOut()
            ) {
                when {
                    prompts.isEmpty() && currentSearchBoxFocus != null -> NoResultsScreen(
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
private fun DistanceScreen(
    modifier: Modifier = Modifier,
    distance: Float?
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (distance == null) {
            Text(
                text = stringResource(R.string.choose_stations_text),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )
        } else {
            Text(
                text = stringResource(R.string.distance_between_stations_text),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = when (distance < 1f) {
                    true -> stringResource(R.string.km_precise_number_format).format(distance)
                    false -> stringResource(R.string.km_number_format).format(distance)
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PromptsContent(
    modifier: Modifier = Modifier,
    prompts: List<String>,
    onPromptClicked: (String) -> Unit
) {
    val orientation = LocalConfiguration.current.orientation

    LazyColumn(modifier) {
        items(prompts) { prompt ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPromptClicked(prompt) }
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .run {
                            when (orientation) {
                                ORIENTATION_LANDSCAPE -> this.displayCutoutPadding()
                                else -> this
                            }
                        },
                    text = prompt
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun NoResultsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Icon(
            modifier = Modifier.size(100.dp),
            painter = painterResource(id = R.drawable.search_not_found),
            contentDescription = null
        )
        Text(
            text = stringResource(R.string.no_results_label),
            style = MaterialTheme.typography.headlineMedium
        )
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
            searchBoxAData = SearchBoxData(),
            searchBoxBData = SearchBoxData("Some text", isValid = true),
            prompts = emptyList(),
            distance = null,
            onSearchBoxSelected = {},
            onPromptClicked = {},
            onSearchBoxAValueChanged = {},
            onSearchBoxBValueChanged = {},
            calculateDistance = {}
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
            searchBoxAData = SearchBoxData(),
            searchBoxBData = SearchBoxData("Some text", isValid = true),
            prompts = emptyList(),
            distance = null,
            onSearchBoxSelected = {},
            onPromptClicked = {},
            onSearchBoxAValueChanged = {},
            onSearchBoxBValueChanged = {},
            calculateDistance = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun MainScreenDistancePreview() {
    AppBackground {
        MainScreenContent(
            modifier = Modifier.fillMaxSize(),
            isUpdating = false,
            searchBoxAData = SearchBoxData("Some text A", isValid = true),
            searchBoxBData = SearchBoxData("Some text B", isValid = true),
            prompts = emptyList(),
            distance = 123.5f,
            onSearchBoxSelected = {},
            onPromptClicked = {},
            onSearchBoxAValueChanged = {},
            onSearchBoxBValueChanged = {},
            calculateDistance = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun PromptsContentPreview() {
    AppBackground {
        PromptsContent(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            prompts = listOf("Prompt A", "Prompt B", "Prompt C"),
            onPromptClicked = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun NoResultsPreview() {
    AppBackground {
        NoResultsScreen(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}
//endregion
