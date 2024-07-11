package com.julianczaja.stations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.julianczaja.stations.common.TestTags
import com.julianczaja.stations.presentation.main.SearchBoxType

class MainScreenRobot(private val composeTestRule: ComposeTestRule) {

    fun search(
        searchBoxType: SearchBoxType,
        body: SearchRobot.() -> Unit
    ) = SearchRobot(composeTestRule, searchBoxType).apply(body)

    fun prompts(body: PromptsRobot.() -> Unit) = PromptsRobot(composeTestRule).apply(body)

    fun waitUntilLoadingScreenIsDisplayed() = composeTestRule.waitUntil {
        composeTestRule.onNodeWithTag(TestTags.LOADING_SCREEN).isDisplayed()
    }

    fun waitUntilLoadingScreenIsNotDisplayed() = composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule.onNodeWithTag(TestTags.LOADING_SCREEN).isNotDisplayed()
    }

    fun assertDistanceTextIsDisplayed() = composeTestRule
        .onNodeWithTag(TestTags.DISTANCE_TEXT)
        .assertIsDisplayed()

    fun assertDistanceTextContainsSubstring(text: String) = composeTestRule
        .onNodeWithTag(TestTags.DISTANCE_TEXT)
        .assertTextContains(value = text, substring = true)

}
