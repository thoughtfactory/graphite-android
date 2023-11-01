package com.syncodec.graphite.presentation.sync.dropbox2.composable

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.cloud.dropbox.DropboxConnector
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.di.snapshot.SnapshotInator
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.common.bar.GenericTopBar
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.RemoteSnapshot
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.presentation.settings.composable.dialog.RestoreSnapshotDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.RestoringSnapshotDialog
import com.syncodec.graphite.presentation.sync.dropbox2.composable.dialog.DropboxOAuth2CodeDialog
import com.syncodec.graphite.presentation.sync.dropbox2.composable.dropboxConnectionCard.DropboxProfileCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DropboxScreen(
	dropBoxConnection: DropboxConnector.Companion.DropBoxConnection = DropboxConnector.Companion.DropBoxConnection.Init,
	dropboxAccountInfo: NetworkRequest<DropboxApi.Companion.DropboxAccountInfo> = NetworkRequest.Init,
	remoteSnapshotList: List<DropboxApi.Companion.RemoteSnapshot> = emptyList(),
	onConnectWithOAuth2Code: (String) -> Unit = {},
	onClickTestConnection: () -> Unit = {},
	onClickDisconnect: () -> Unit = {},
	onRefreshSnapshot: () -> Unit = {},
	onClickTakeSnapshot: () -> Unit = {},
	onGetSnapshot: suspend (String) -> Pair<ByteArray, SnapshotInator.Companion.SnapshotMetadata?>? = { null },
	onRestoreSnapshot: suspend (ByteArray, Boolean) -> Boolean = { _, _ -> false }
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current

	val appDataStore = LocalAppDataStore.current
	val isAutoSyncEnabled by appDataStore.isAutoSyncEnabled.collectAsState(initial = true)

	var selectedSnapshot by remember { mutableStateOf<Triple<DropboxApi.Companion.RemoteSnapshot, SnapshotInator.Companion.SnapshotMetadata?, ByteArray>?>(null) }

	var isEnterOAuth2CodeDialogVisible by rememberSaveable { mutableStateOf(false) }
	var isRestoreSnapshotDialogVisible by rememberSaveable { mutableStateOf(false) }
	var isRestoringSnapshotDialogVisible by rememberSaveable { mutableStateOf(false) }

	GenericScaffold2(
		topBar = { GenericTopBar(title = "Dropbox") },
		dialogContent = {
			DropboxOAuth2CodeDialog(
				isDialogVisible = isEnterOAuth2CodeDialogVisible,
				onDismissRequest = { isEnterOAuth2CodeDialogVisible = false },
				onConnect = { onConnectWithOAuth2Code(it); isEnterOAuth2CodeDialogVisible = false },
			)

			RestoreSnapshotDialog(
				isDialogVisible = isRestoreSnapshotDialogVisible,
				onDismissRequest = { isRestoreSnapshotDialogVisible = false; selectedSnapshot = null },
				remoteSnapshot = selectedSnapshot?.first,
				snapshotMetadata = selectedSnapshot?.second,
				snapshotByteArray = selectedSnapshot?.third,
				onClickRestore = { dataByteArray ->
					isRestoreSnapshotDialogVisible = false
					isRestoringSnapshotDialogVisible = true
					scope.launch(Dispatchers.IO) {
						val isSnapshotRestored = onRestoreSnapshot(dataByteArray, selectedSnapshot?.first?.name?.endsWith(".7z") ?: false)
						withContext(Dispatchers.Main) {
							isRestoringSnapshotDialogVisible = false
							if (!isSnapshotRestored) Toast.makeText(context, context.getText(R.string.toast_error_restoring_snapshot), Toast.LENGTH_SHORT).show()
						}
					}
				}
			)

			RestoringSnapshotDialog(isDialogVisible = isRestoringSnapshotDialogVisible)
		}
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
		) {
			  DropboxProfileCard(
				dropboxAccountInfo = dropboxAccountInfo,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp, vertical = 8.dp),
			)
			SettingsButton(
				title = stringResource(R.string.connect_with_dropbox),
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_logo_dropbox, color = Color.Unspecified, size = 28.dp),
				enabled = dropBoxConnection !is DropboxConnector.Companion.DropBoxConnection.Connected,
			) {
				uriHandler.openUri(DropboxConnector.DROPBOX_CONNECT_URL)
			}
			SettingsButton(
				title = stringResource(R.string.enter_oauth2_code),
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_keyboard),
				enabled = dropBoxConnection !is DropboxConnector.Companion.DropBoxConnection.Connected,
			) {
				isEnterOAuth2CodeDialogVisible = true
			}
			SettingsButton(
				title = stringResource(R.string.test_connection),
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_cloud_question),
				onClick = onClickTestConnection,
			)
			SettingsButton(
				title = stringResource(R.string.disconnect),
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_cloud_x),
				enabled = dropBoxConnection is DropboxConnector.Companion.DropBoxConnection.Connected,
				onClick = onClickDisconnect,
			)
			SettingsSwitch(
				title = stringResource(R.string.auto_sync),
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_cloud_word),
				enabled = dropBoxConnection is DropboxConnector.Companion.DropBoxConnection.Connected,
				checked = isAutoSyncEnabled,
				onCheckChanged = { appDataStore.setIsAutoSyncEnabled(!isAutoSyncEnabled) }
			)
			SettingsButton(
				title = stringResource(R.string.take_snapshot),
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_wand),
				enabled = dropBoxConnection is DropboxConnector.Companion.DropBoxConnection.Connected,
				onClick = onClickTakeSnapshot,
			)
			Divider()
			SettingsButton(
				title = stringResource(id = R.string.snapshot_warehouse),
				trailingIcon = null,
				enabled = false
			)

			remoteSnapshotList.forEach { remoteSnapshot ->
				RemoteSnapshot(remoteSnapshot = remoteSnapshot) {
					isRestoreSnapshotDialogVisible = true
					scope.launch(Dispatchers.IO) {
						val remoteSnapshotData = onGetSnapshot(remoteSnapshot.pathLower) ?: return@launch
						withContext(Dispatchers.Main) {
							selectedSnapshot = Triple(remoteSnapshot, remoteSnapshotData.second, remoteSnapshotData.first)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(128.dp))
		}
	}
}
