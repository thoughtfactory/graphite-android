package com.syncodec.graphite.presentation.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.search.composable.screen.SearchScreen
import com.syncodec.graphite.presentation.ui.BaseContent


class SearchActivity: ComponentActivity() {

	private val viewModel by viewModels<SearchViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			setContent {
				BaseContent {

					val systemUiController = rememberSystemUiController()
					systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
					systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

					SearchScreen()
				}
			}
		}
	}

	@Deprecated("Must update in next release")
	override fun onBackPressed() {
		if(viewModel.showResultScreen.value) {
			viewModel.showResultScreen.value = false
			viewModel.showTag.value = null
			viewModel.searchQuery.value = null
		} else {
			super.onBackPressed()
		}
	}
}
