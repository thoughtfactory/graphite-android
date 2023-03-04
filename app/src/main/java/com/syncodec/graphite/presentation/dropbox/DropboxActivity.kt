package com.syncodec.graphite.presentation.dropbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.syncodec.graphite.presentation.dropbox.screen.DropboxScreen
import com.syncodec.graphite.presentation.dropbox.screen.DropboxScreenViewModel
import com.syncodec.graphite.presentation.ui.BaseContent
import org.koin.androidx.viewmodel.ext.android.viewModel


class DropboxActivity : ComponentActivity() {

	private val dropboxScreenViewModel : DropboxScreenViewModel by viewModel()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val code = intent.data?.getQueryParameter("code")
		if (! code.isNullOrBlank()) dropboxScreenViewModel.exchangeCodeForToken(code)

		dropboxScreenViewModel.testConnection()

		setContent {
			BaseContent {
				DropboxScreen()
			}
		}
	}
}
