package com.syncodec.graphite.presentation.sync.oneDrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.syncodec.graphite.presentation.sync.googleDrive.composable.screen.GoogleDriveSyncScreen
import com.syncodec.graphite.presentation.ui.BaseContent


class OneDriveSyncActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {
				GoogleDriveSyncScreen(
					onClickConnect = {  }
				)
			}
		}
	}
}
