package com.syncodec.graphite

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipeLeft
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SwipeableButton
import org.junit.Rule
import org.junit.Test


class ComposeTest {

	@get:Rule
	val composeTestRule = createComposeRule()
	// use createAndroidComposeRule<YourActivity>() if you need access to
	// an activity

	@Test
	fun myTest() {
		// Start the app
		composeTestRule.setContent {
			SwipeableButton()
		}

		composeTestRule.onNodeWithText("tmp").performGesture { swipeLeft() }
		composeTestRule.onNodeWithText("tmp").assertHasClickAction()
	}
}
