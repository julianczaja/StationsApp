package com.julianczaja.stations.presentation.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.julianczaja.stations.R
import com.julianczaja.stations.presentation.components.AppBackground
import com.julianczaja.stations.presentation.main.SearchBoxData
import com.julianczaja.stations.ui.theme.validColor


@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    data: SearchBoxData,
    placeholderText: String,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()
    val targetHeight = when (isFocused.value) {
        true -> OutlinedTextFieldDefaults.MinHeight
        false -> OutlinedTextFieldDefaults.MinHeight - 8.dp
    }
    val height by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(easing = EaseOut),
        label = "height"
    )
    val borderColor = when (isFocused.value) {
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.outline
    }
    val targetBorderThickness = when (isFocused.value) {
        true -> 2.dp
        false -> 1.dp
    }
    val borderThickness by animateDpAsState(
        targetValue = targetBorderThickness,
        animationSpec = tween(easing = EaseOut),
        label = "thickness"
    )
    val targetTextSize = when (isFocused.value) {
        true -> 16f
        false -> 14f
    }
    val textSize by animateFloatAsState(
        targetValue = targetTextSize,
        animationSpec = tween(easing = EaseOut),
        label = "text_size"
    )

    LaunchedEffect(isFocused.value) {
        onFocusChange(isFocused.value)
    }

    BasicTextField(
        modifier = modifier
            .height(height)
            .border(
                width = borderThickness,
                shape = RoundedCornerShape(8.dp),
                color = borderColor
            )
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        interactionSource = interactionSource,
        value = data.value,
        onValueChange = onValueChange,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        maxLines = 1,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = TextUnit(textSize, TextUnitType.Sp)
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    if (data.value.isEmpty()) PlaceholderText(placeholderText)
                    innerTextField()
                }
                AnimatedVisibility(
                    visible = isFocused.value && data.value.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ClearButton(onValueChange)
                }
                AnimatedVisibility(
                    visible = data.isValid,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ValidIcon()
                }
            }
        }
    )
}

@Composable
private fun PlaceholderText(text: String) {
    Text(
        text = text,
        style = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            fontSize = 14.sp
        )
    )
}

@Composable
private fun ClearButton(onValueChange: (String) -> Unit) {
    TextButton(onClick = { onValueChange("") }) {
        Text(
            text = stringResource(R.string.clear_text_field_label).uppercase(),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun ValidIcon() {
    Icon(
        imageVector = Icons.Default.Check,
        tint = validColor,
        contentDescription = null
    )
}

//region Preview
@PreviewLightDark
@Composable
private fun SearchBoxPreview() {
    AppBackground {
        SearchBox(
            data = SearchBoxData("Some text"),
            onValueChange = {},
            placeholderText = "Placeholder",
            onFocusChange = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchBoxValidPreview() {
    AppBackground {
        SearchBox(
            data = SearchBoxData("Some text", isValid = true),
            onValueChange = {},
            placeholderText = "Placeholder",
            onFocusChange = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchBoxEmptyPreview() {
    AppBackground {
        SearchBox(
            data = SearchBoxData(),
            onValueChange = {},
            placeholderText = "Placeholder",
            onFocusChange = {}
        )
    }
}
//endregion