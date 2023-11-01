package com.syncodec.graphite.presentation.sync.dropbox2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.sync.dropbox2.composable.DropboxScreen
import org.koin.androidx.viewmodel.ext.android.viewModel


class DropboxActivity : ComponentActivity() {

	private val viewModel by viewModel<DropboxViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val code = intent?.data?.getQueryParameter("code")
		if (!code.isNullOrEmpty()) viewModel.exchangeCode(code = code)
		else viewModel.refreshConnection()

		setContent {
			BaseComposable {

				val dropBoxConnection by viewModel.dropBoxConnection.collectAsState()
				val dropboxAccountInfo by viewModel.dropboxAccountInfo.collectAsState()
				val remoteSnapshotList by viewModel.remoteSnapshotList.collectAsState()

				DropboxScreen(
					dropBoxConnection = dropBoxConnection,
					dropboxAccountInfo = dropboxAccountInfo,
					remoteSnapshotList = remoteSnapshotList,
					onConnectWithOAuth2Code = viewModel::exchangeCode,
					onClickTestConnection = viewModel::refreshConnection,
					onClickDisconnect = viewModel::disconnect,
					onRefreshSnapshot = {},
					onClickTakeSnapshot = viewModel::takeSnapshot,
					onGetSnapshot = viewModel::downloadSnapshot,
					onRestoreSnapshot = viewModel::restoreSnapshot,
				)
			}
		}
	}
}