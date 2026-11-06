package com.wordglass.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigationOpensEveryPrimaryPage() {
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.onNodeWithText("Alex Chen").assertIsDisplayed()

        composeRule.onNodeWithTag("tab_study").performClick()
        composeRule.onNodeWithText("experience").assertIsDisplayed()

        composeRule.onNodeWithTag("tab_library").performClick()
        composeRule.onNodeWithText("所有词，都在一个清晰的视图里。").assertIsDisplayed()

        composeRule.onNodeWithTag("tab_home").performClick()
        composeRule.onNodeWithText("今日学习").assertIsDisplayed()
    }
}
