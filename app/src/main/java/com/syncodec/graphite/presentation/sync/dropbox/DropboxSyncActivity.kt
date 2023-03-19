package com.syncodec.graphite.presentation.sync.dropbox

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.presentation.sync.dropbox.composable.screen.DropboxSyncScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class DropboxSyncActivity : ComponentActivity() {
	private val viewModel by viewModel<DropboxSyncViewModel>()

	private val testConnectionResponse = MutableStateFlow<DBox.Companion.TestConnectionResponse?>(null)

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		try {
			val code = intent?.data?.getQueryParameter("code")
			code?.let {
				viewModel.exchangeCodeForToken(it) {
					viewModel.testConnection { testConnectionResponse.tryEmit(it) }
				}
			} ?: viewModel.testConnection { testConnectionResponse.tryEmit(it) }
		} catch (e : Exception) {
			viewModel.testConnection { testConnectionResponse.tryEmit(it) }
		}

		setContent {
			val context = LocalContext.current
			val scope = rememberCoroutineScope()
			val _testConnectionResponse by testConnectionResponse.collectAsState()

			val snapshotList by viewModel.snapshotList.collectAsState()
			var isGeneratingSnapshot by remember { mutableStateOf(false) }
			var isRestoringSnapshot by remember { mutableStateOf(false) }

			BaseContent {
				DropboxSyncScreen(
					testConnectionResponse = _testConnectionResponse,
					snapshotList = snapshotList,
					isGeneratingSnapshot = isGeneratingSnapshot,
					isRestoringSnapshot = isRestoringSnapshot,
					onTestConnection = { viewModel.testConnection { testConnectionResponse.tryEmit(it) } },
					onAuthorize = {
						viewModel.exchangeCodeForToken(it) {
							viewModel.testConnection { testConnectionResponse.tryEmit(it) }
						}
					},
					onReconnect = {},
					onDisconnect = { viewModel.disconnect { viewModel.testConnection { testConnectionResponse.tryEmit(it) } } },
					onClickTakeSnapshot = {
						viewModel.generateSnapshot() { isGeneratingSnapshot = it }
					},
					onClickShareSnapshot = { metadata ->
						viewModel.downloadSnapshot(metadata) { downloadSnapshotResponse ->
							if (downloadSnapshotResponse is DBox.Companion.DownloadSnapshotResponse.Success) {
								scope.launch(Dispatchers.IO) {
									val snapshotDir = File(context.cacheDir, "shareSnapshot").apply {
										deleteRecursively()
										mkdirs()
									}
									val file = File(snapshotDir, downloadSnapshotResponse.fileName)
									file.outputStream().use { downloadSnapshotResponse.inputStream.copyTo(it) }
									scope.launch(Dispatchers.Main) {
										file.share(context)
									}
								}
							}
						}
					},
					onClickRestoreSnapshot = { metadata ->
						isRestoringSnapshot = true
						viewModel.downloadSnapshot(metadata) { downloadSnapshotResponse ->
							if (downloadSnapshotResponse is DBox.Companion.DownloadSnapshotResponse.Success) {
								viewModel.restore(downloadSnapshotResponse.inputStream) {
									if (! it) {
										scope.launch(Dispatchers.Main) {
											Toast.makeText(context, "Failed to restore snapshot", Toast.LENGTH_SHORT).show()
										}
									}
								}
							}
						}
					},
					refreshSnapshot = { viewModel.refreshSnapshot() }
				)
			}
		}
	}
}
