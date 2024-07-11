package com.julianczaja.stations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.julianczaja.stations.common.TestTags

class PromptsRobot(private val composeTestRule: ComposeTestRule) {

    private val node get() = composeTestRule.onNodeWithTag(TestTags.PROMPTS_COLUMN)

    fun assertExsist() = node.assertExists()

    fun assertDoesNotExsist() = node.assertDoesNotExist()

    fun assertHasChildWithText(text: String) = node
        .onChildren()
        .filterToOne(hasText(text))
        .assertIsDisplayed()

    fun clickOnChildWithText(text: String) = node
        .onChildren()
        .filterToOne(hasText(text))
        .performClick()
}
