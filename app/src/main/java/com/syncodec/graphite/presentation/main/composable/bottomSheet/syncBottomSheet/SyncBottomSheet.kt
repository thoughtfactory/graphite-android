package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock.ConnectedView
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock.NotLoggedInView
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncActivity
import com.syncodec.graphite.service.syncService.SyncerService
import com.syncodec.graphite.utils.NetworkUtils.Companion.isInternetAvailable


@Preview
@Composable
fun SyncBottomSheet(
	syncStatus : SyncerService.Companion.SyncStatus = SyncerService.Companion.SyncStatus.Init,
	testConnectionResponse : DBox.Companion.TestConnectionResponse? = null,
	onClickTestConnection : () -> Unit = {},
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
	closeSheet : () -> Unit = {},
) {
	val context = LocalContext.current

	GenericBottomSheet(
		title = "Sync",
		icon = R.drawable.ic_cloud
	) {
		when (testConnectionResponse) {
			is DBox.Companion.TestConnectionResponse.Loading -> LoadingCard(
				onClickTestConnection = onClickTestConnection,
				onClickManage = {
					context.startActivity(Intent(context, DropboxSyncActivity::class.java))
					closeSheet()
				}
			)

			is DBox.Companion.TestConnectionResponse.Success -> ConnectedView(
				email = testConnectionResponse.fullAccount.email,
				name = testConnectionResponse.fullAccount.name?.displayName,
				spaceTotal = testConnectionResponse.spaceUsage.allocation?.individualValue?.allocated,
				spaceUsed = testConnectionResponse.spaceUsage.used,
				syncStatus = syncStatus,
				onForceSync = onClickForceSync,
				onSync = onClickSyncNow,
				onClickManage = {
					context.startActivity(Intent(context, DropboxSyncActivity::class.java))
					closeSheet()
				},
			)

			is DBox.Companion.TestConnectionResponse.NotLoggedIn -> NotLoggedInView()
			is DBox.Companion.TestConnectionResponse.Error -> ErrorCard(onClickTestConnection = onClickTestConnection) {
				closeSheet()
				context.startActivity(Intent(context, DropboxSyncActivity::class.java))
			}
			else -> LoadingCard(
				onClickTestConnection = onClickTestConnection,
				onClickManage = {
					closeSheet()
					context.startActivity(Intent(context, DropboxSyncActivity::class.java))
				}
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

	val title = if (isInternetAvailable) "Error connecting with Dropbox" else "No internet connection"
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
				MenuButton(
					icon = R.drawable.ic_refresh,
					colors = MenuButtonDefaults.deleteButtonColors(
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
			MenuButton(
				icon = R.drawable.ic_refresh,
				colors = MenuButtonDefaults.deleteButtonColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
					iconColor = MaterialTheme.colorScheme.onSurface,
				),
				onClick = onClickTestConnection
			)
		}
	}
}
