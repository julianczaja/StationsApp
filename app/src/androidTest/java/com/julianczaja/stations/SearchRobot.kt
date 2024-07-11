package com.julianczaja.stations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.julianczaja.stations.common.TestTags
import com.julianczaja.stations.presentation.main.SearchBoxType

class SearchRobot(
    private val composeTestRule: ComposeTestRule,
    searchBoxType: SearchBoxType
) {
    private val tag = when (searchBoxType) {
        SearchBoxType.A -> TestTags.SEARCH_BOX_A
        SearchBoxType.B -> TestTags.SEARCH_BOX_B
    }

    private val node get() = composeTestRule.onNodeWithTag(tag)

    fun assertIsDisplayed() = node.assertIsDisplayed()

    fun assertIsFocused() = node.assertIsFocused()

    fun assertIsNotFocused() = node.assertIsNotFocused()

    fun click() = node.performClick()

    fun inputText(text: String) = node.performTextInput(text)
}
