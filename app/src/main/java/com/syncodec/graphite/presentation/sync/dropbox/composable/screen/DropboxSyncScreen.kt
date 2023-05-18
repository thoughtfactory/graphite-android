package com.syncodec.graphite.presentation.sync.dropbox.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dropbox.core.v2.files.Metadata
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.presentation.report.ReportActivity
import com.syncodec.graphite.presentation.common.bar.GenericTopBar
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import com.syncodec.graphite.presentation.common.info.PlainTextWarning
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingSwitch
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsContentTitle
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.SnapshotButton
import com.syncodec.graphite.presentation.sync.dropbox.composable.buildingBlock.DropboxConnection
import com.syncodec.graphite.presentation.sync.dropbox.composable.buildingBlock.DropboxEmptySnapshot
import com.syncodec.graphite.presentation.sync.dropbox.composable.dialog.DropboxDialog
import com.syncodec.graphite.presentation.sync.dropbox.composable.dialog.DropboxDialogType
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.NetworkUtils.Companion.isInternetAvailable


@Preview
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropboxSyncScreen(
	testConnectionResponse : DBox.Companion.TestConnectionResponse? = null,
	snapshotList : List<Metadata>? = null,
	isGeneratingSnapshot : Boolean = false,
	isRestoringSnapshot : Boolean = false,
	onTestConnection : () -> Unit = {},
	onAuthorize : (String) -> Unit = {},
	onClickReconnect : () -> Unit = {},
	onClickDisconnect : () -> Unit = {},
	onClickTakeSnapshot : () -> Unit = {},
	onClickShareSnapshot : (Metadata) -> Unit = {},
	onClickRestoreSnapshot : (Metadata) -> Unit = {},
	refreshSnapshot : () -> Unit = {},
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val uriHandler = LocalUriHandler.current

	val isAutoSyncEnabled by dataStoreInstance.isAutoSyncEnabled.collectAsState(initial = null)
	val showPlainTextWarning by dataStoreInstance.showPlainTextWarningDropbox.collectAsState(initial = false)

	var isEnterAuthCodeDialogVisible by remember { mutableStateOf(false) }
	var isReconnectDialogVisible by remember { mutableStateOf(false) }
	var isDisconnectDialogVisible by remember { mutableStateOf(false) }
	var selectedSnapshot by remember { mutableStateOf<Metadata?>(null) }
	fun openDialog(dialogType : DropboxDialogType) = when (dialogType) {
		DropboxDialogType.EnterAuthCode -> isEnterAuthCodeDialogVisible = true
		DropboxDialogType.Reconnect -> isReconnectDialogVisible = true
		DropboxDialogType.Disconnect -> isDisconnectDialogVisible = true
		DropboxDialogType.RestoreSnapshot -> null
		DropboxDialogType.RestoringSnapshot -> null
	}

	fun closeDialog(dialogType : DropboxDialogType) = when (dialogType) {
		DropboxDialogType.EnterAuthCode -> isEnterAuthCodeDialogVisible = false
		DropboxDialogType.Reconnect -> isReconnectDialogVisible = false
		DropboxDialogType.Disconnect -> isDisconnectDialogVisible = false
		DropboxDialogType.RestoreSnapshot -> selectedSnapshot = null
		DropboxDialogType.RestoringSnapshot -> null
	}

	var refreshing by remember { mutableStateOf(false) }
	val pullRefreshState = rememberPullRefreshState(
		refreshing = refreshing,
		onRefresh = {
			refreshing = true
			refreshSnapshot()
			refreshing = false
		}
	)

	val isInternetAvailable = context.isInternetAvailable()

	GenericScaffold(
		topBar = { GenericTopBar(title = "Dropbox") },
		dialogContent = {
			DropboxDialog(
				showEnterAuthCodeDialog = isEnterAuthCodeDialogVisible,
				showReconnectDialog = isReconnectDialogVisible,
				showDisconnectDialog = isDisconnectDialogVisible,
				showRestoringSnapshotDialog = isRestoringSnapshot,
				snapshot = selectedSnapshot,
				testConnectionResponse = testConnectionResponse,
				onAuthorize = onAuthorize,
				onReconnect = onClickReconnect,
				onDisconnect = {
					onClickDisconnect()
					closeDialog(DropboxDialogType.Disconnect)
				},
				onShare = {
					onClickShareSnapshot(it)
					closeDialog(DropboxDialogType.RestoreSnapshot)
				},
				onRestore = {
					onClickRestoreSnapshot(it)
				},
				closeDialog = ::closeDialog,
			)
		}
	) {
		Box(
			modifier = Modifier.pullRefresh(state = pullRefreshState)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
			) {
				AnimatedVisibility(
					visible = ! isInternetAvailable,
					enter = expandVertically(tween(300)),
					exit = shrinkVertically(tween(300)),
				) {
					InfoCard(
						title = "No internet connection",
						description = "It seems you are not connected to internet. Please connect to internet and try again.",
						icon = R.drawable.ic_no_internet,
						colors = InfoCardDefaults.warningCardColors(),
						buttonText = "Retry",
						modifier = Modifier.padding(horizontal = 12.dp),
					) { onTestConnection() }
				}
				InfoCard(
					title = "Experimental",
					description = "This feature is still in development and may not work as expected. Please report any bugs you encounter.",
					icon = R.drawable.ic_warning,
					colors = InfoCardDefaults.warningCardColors(),
					buttonText = "Report Bug",
					modifier = Modifier.padding(horizontal = 12.dp)
				) { context.startActivity(Intent(context, ReportActivity::class.java)) }
				InfoCard(
					title = "Backup vs Sync",
					description = "Backup is a one time process that saves your data to the cloud at a regular interval (not available yet) or manually. Sync is a continuous process that keeps your data in sync with the cloud to be available on other devices.",
					icon = R.drawable.ic_warning,
					colors = InfoCardDefaults.infoCardColors(),
					buttonText = "Learn More",
					modifier = Modifier.padding(horizontal = 12.dp)
				) { uriHandler.openUri("https://graphite.syncodec.com/#/backup_and_sync") }
				PlainTextWarning(
					isVisible = showPlainTextWarning,
					modifier = Modifier.padding(horizontal = 12.dp),
				) {
					dataStoreInstance.putShowPlainTextWarningDropbox(false)
				}
				DropboxConnection(testConnectionResponse = testConnectionResponse)
				SettingButton(
					text = "Connect with Dropbox",
					icon = R.drawable.ic_logo_dropbox,
					subIcon = if (testConnectionResponse is DBox.Companion.TestConnectionResponse.Success) R.drawable.ic_checkmark else null,
					tint = Color.Unspecified,
					subIconTint = Color.Unspecified,
				) {
					if (isInternetAvailable) {
						if (testConnectionResponse is DBox.Companion.TestConnectionResponse.Success) openDialog(DropboxDialogType.Reconnect)
						else uriHandler.openUri(DBox.DROPBOX_CONNECT)
					} else Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
				}
				SettingButton(
					text = "Enter OAuth2 Code",
					icon = R.drawable.ic_keyboard,
				) {
					if (isInternetAvailable) openDialog(DropboxDialogType.EnterAuthCode)
					else Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
				}
				SettingButton(
					text = "Test connection",
					icon = R.drawable.ic_test_connection,
					onClick = onTestConnection
				)
				SettingButton(
					text = "Disconnect",
					icon = R.drawable.ic_cloud_x,
					enabled = testConnectionResponse is DBox.Companion.TestConnectionResponse.Success,
				) { openDialog(DropboxDialogType.Disconnect) }
				SettingSwitch(
					text = "Auto Sync",
					subText = if (isAutoSyncEnabled == true) "Auto sync is enabled" else "Auto sync is disabled",
					icon = R.drawable.ic_sync,
					isChecked = isAutoSyncEnabled != false,
					enabled = isAutoSyncEnabled != null && testConnectionResponse is DBox.Companion.TestConnectionResponse.Success,
				) {
					dataStoreInstance.setIsAutoSyncEnabled(it)
					if (it) Toast.makeText(context, "Auto sync is enabled", Toast.LENGTH_SHORT).show()
					else Toast.makeText(context, "Auto sync is disabled", Toast.LENGTH_SHORT).show()
				}
				SettingButton(
					text = "Take Snapshot",
					icon = R.drawable.ic_easy,
					subText = "Save everything from database",
					enabled = testConnectionResponse is DBox.Companion.TestConnectionResponse.Success,
				) {
					if (isGeneratingSnapshot) Toast.makeText(context, "Please wait for the current snapshot to finish", Toast.LENGTH_SHORT).show()
					else onClickTakeSnapshot()
				}
				SettingButton(
					text = "Learn more about backup and sync",
					icon = R.drawable.ic_info,
				) { uriHandler.openUri("https://graphite.syncodec.com/#/backup_and_sync") }
				SettingsContentTitle(
					title = "SNAPSHOT WAREHOUSE"
				) {
					AnimatedVisibility(visible = isGeneratingSnapshot) {
						CircularProgressIndicator(
							color = MaterialTheme.colorScheme.onSurface,
							strokeWidth = 2.dp,
							modifier = Modifier.size(16.dp),
						)
					}
				}
				snapshotList?.map {
					Pair(
						it, try {
							it.name.split("_").last().split(".").first().toLong()
						} catch (e : Exception) {
							- 1
						}
					)
				}
					?.sortedByDescending { it.second }
					?.forEachIndexed { index, pair ->
						SnapshotButton(
							fileName = pair.first.name,
							createdTimestamp = pair.second,
							isLatest = index == 0,
						) { selectedSnapshot = pair.first }
					} ?: DropboxEmptySnapshot()
				Spacer(modifier = Modifier.height(128.dp))
			}

			PullRefreshIndicator(
				refreshing = refreshing || isGeneratingSnapshot,
				state = pullRefreshState,
				backgroundColor = MaterialTheme.colorScheme.surface,
				contentColor = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.align(Alignment.TopCenter)
			)
		}
	}
}
