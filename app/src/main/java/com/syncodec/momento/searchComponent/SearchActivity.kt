package com.syncodec.momento.searchComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.syncodec.momento.ui.theme.MomentoTheme

class SearchActivity: ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

		setContent {
			MomentoTheme {
				SearchComponent()
			}
		}
	}

	@Composable
	fun SearchComponent() {


	}
}
