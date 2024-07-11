package com.julianczaja.stations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.julianczaja.stations.common.TestTags
import com.julianczaja.stations.presentation.main.SearchBoxType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun mainScreen(
        composeTestRule: ComposeTestRule,
        body: MainScreenRobot.() -> Unit
    ) = MainScreenRobot(composeTestRule).apply(body)

    @Test
    fun test_afterSelectingBothStations_distanceShouldBeCalculated() {
        mainScreen(composeTestRule) {
            waitUntilLoadingScreenIsDisplayed()
            waitUntilLoadingScreenIsNotDisplayed()
            search(SearchBoxType.A) { assertIsDisplayed() }
            search(SearchBoxType.B) { assertIsDisplayed() }
            prompts { assertDoesNotExsist() }
            search(SearchBoxType.A) {
                assertIsDisplayed()
                click()
                inputText("war")
            }
            prompts {
                assertExsist()
                assertHasChildWithText("Warszawa")
                clickOnChildWithText("Warszawa")
            }
            search(SearchBoxType.B) {
                assertIsFocused()
                inputText("lodz")
            }
            prompts {
                assertHasChildWithText("Łódź")
                clickOnChildWithText("Łódź")
            }
            search(SearchBoxType.B) { assertIsNotFocused() }
            assertDistanceTextIsDisplayed()
            assertDistanceTextContainsSubstring(" km")
        }
    }

    @Test
    fun test_afterWrongQuery_notFoundScreenShouldBeDisplayed() {
        mainScreen(composeTestRule) {
            search(SearchBoxType.A) {
                assertIsDisplayed()
                click()
                inputText("asdasdasdasd")
            }
            prompts { assertDoesNotExsist() }

            composeTestRule
                .onNodeWithTag(TestTags.NO_RESULT_SCREEN)
                .assertIsDisplayed()
        }
    }
}
