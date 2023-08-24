package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock.ConnectedView
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock.NotLoggedInView
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncActivity
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncActivity
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.NetworkUtils.Companion.isInternetAvailable
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance


@Preview
@Composable
fun SyncBottomSheet(
	syncStatus : SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	testConnectionResponse : DBox.Companion.TestConnectionResponse? = null,
	onClickTestConnection : () -> Unit = {},
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
	closeSheet : () -> Unit = {},
) {
	val context = LocalContext.current
	val syncDataStoreInstance = remember { SyncDataStoreInstance(context) }
	val syncProvider by syncDataStoreInstance.syncProvider.collectAsState(initial = null)

	fun onClickManage() {
		Log.d("npr71", "SyncBottomSheet: onClickManage: syncProvider: $syncProvider")
		val activity = when (syncProvider) {
			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> DropboxSyncActivity::class.java
			SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> GoogleDriveSyncActivity::class.java
			else -> null
		}
		activity?.let { context.startActivity(Intent(context, activity)) }
		closeSheet()
	}

	GenericBottomSheet(
		title = "Sync",
		icon = R.drawable.ic_cloud
	) {
		when (testConnectionResponse) {
			is DBox.Companion.TestConnectionResponse.Loading -> LoadingCard(
				onClickTestConnection = onClickTestConnection,
				onClickManage = ::onClickManage
			)

			is DBox.Companion.TestConnectionResponse.Success -> ConnectedView(
				email = testConnectionResponse.email,
				name = testConnectionResponse.name,
				spaceTotal = testConnectionResponse.spaceTotal,
				spaceUsed = testConnectionResponse.spaceUsed,
				syncStatus = syncStatus,
				onForceSync = onClickForceSync,
				onSync = onClickSyncNow,
				onClickManage = ::onClickManage,
			)

			is DBox.Companion.TestConnectionResponse.NotLoggedIn -> NotLoggedInView {
				Intent(context, SettingsActivity::class.java).apply {
//					putExtra(SettingsActivity.Companion.Extras.SettingsScreen.name, SettingsActivity.Companion.SettingsScreen.BackupAndSync.name)
					context.startActivity(this)
				}
				closeSheet()
			}
			is DBox.Companion.TestConnectionResponse.Error -> ErrorCard(
				onClickTestConnection = onClickTestConnection,
				onClickManage = ::onClickManage,
			)
			else -> LoadingCard(
				onClickTestConnection = onClickTestConnection,
				onClickManage = ::onClickManage,
			)
		}
	}
}

@Preview
@Composable
private fun ErrorCard(
	onClickTestConnection : () -> Unit = {},
	onClickManage : () -> Unit = {},
) {
	val context = LocalContext.current
	val isInternetAvailable = context.isInternetAvailable()

	val title = if (isInternetAvailable) "Error connecting with cloud" else "No internet connection"
	val description = if (isInternetAvailable) "Ensure you have a working internet connection and try again." else "It seems you are not connected to internet. Please connect to internet and try again."

	Card(
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer,
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(0.dp, 4.dp),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth(),
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_warning),
					contentDescription = title,
					modifier = Modifier.requiredSize(32.dp)
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodyMedium,
			)
			Spacer(modifier = Modifier.height(24.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth(),
			) {
				Button(
					shape = MaterialTheme.shapes.medium,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.error,
						contentColor = MaterialTheme.colorScheme.onError,
					),
					modifier = Modifier.weight(1f),
					onClick = onClickManage,
				) {
					Text(text = "Manage")
				}
				Spacer(modifier = Modifier.width(2.dp))
				GenericButton(
					icon = R.drawable.ic_refresh,
					colors = GenericButtonDefaults.deleteButtonColors(
						containerColor = MaterialTheme.colorScheme.error,
						iconColor = MaterialTheme.colorScheme.onError,
					),
					onClick = onClickTestConnection
				)
			}
		}
	}
}

@Preview
@Composable
private fun LoadingCard(
	onClickTestConnection : () -> Unit = {},
	onClickManage : () -> Unit = {},
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		LoadingView(
			modifier = Modifier
				.requiredSize(48.dp)
				.padding(8.dp)
		)
		Spacer(modifier = Modifier.height(24.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth(),
		) {
			Button(
				shape = MaterialTheme.shapes.medium,
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
					contentColor = MaterialTheme.colorScheme.onSurface,
				),
				modifier = Modifier.weight(1f),
				onClick = onClickManage,
			) {
				Text(text = "Manage")
			}
			Spacer(modifier = Modifier.width(2.dp))
			GenericButton(
				icon = R.drawable.ic_refresh,
				colors = GenericButtonDefaults.deleteButtonColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
					iconColor = MaterialTheme.colorScheme.onSurface,
				),
				onClick = onClickTestConnection
			)
		}
	}
}
