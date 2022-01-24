package com.syncodec.momento.notebookComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.ui.theme.MomentoTheme

class NotebookActivity: ComponentActivity() {

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			MomentoTheme {
				NotebookScreen()
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
	@Composable
	private fun NotebookScreen() {

		val systemUiController = rememberSystemUiController()
		systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)


	}
}
