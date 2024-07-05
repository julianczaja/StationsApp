package com.julianczaja.stations.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julianczaja.stations.ui.theme.AppTheme


@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = modifier,
        ) {
            content()
        }
    }
}
