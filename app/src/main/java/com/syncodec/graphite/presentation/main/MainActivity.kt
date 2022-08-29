package com.syncodec.graphite.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.tone


class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(
					MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
				)

				MainScreen()
			}
		}
	}
}
