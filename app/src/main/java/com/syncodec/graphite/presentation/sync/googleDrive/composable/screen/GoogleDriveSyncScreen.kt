package com.syncodec.graphite.presentation.sync.googleDrive.composable.screen

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.api.services.drive.model.File
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.report.ReportActivity
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncActivity
import com.syncodec.graphite.presentation.base.DeleteContainer
import com.syncodec.graphite.presentation.base.DeleteContent


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun GoogleDriveSyncScreen(
	aboutState: GoogleDriveSyncActivity.Companion.AboutState = GoogleDriveSyncActivity.Companion.AboutState.Init,
//	snapshotList: GDriveSyncInatorService.Companion.ListFiles? = null,
	isGeneratingSnapshot: Boolean = false,
	isRestoringSnapshot: Boolean = false,
	onTestConnection: () -> Unit = {},
	onClickConnect: () -> Unit = {},
	onClickDisconnect: () -> Unit = {},
	onClickGenerateSnapshot: () -> Unit = {},
	onClickShareSnapshot: (File) -> Unit = {},
	onClickRestoreSnapshot: (File) -> Unit = {},
	onClickDeleteSnapshot: (File) -> Unit = {},
	refreshSnapshot: () -> Unit = {},
) {
//	val context = LocalContext.current
//	val dataStoreInstance = remember { DataStoreInstance(context = context) }
//	val uriHandler = LocalUriHandler.current
//
//	val isAutoSyncEnabled by dataStoreInstance.isAutoSyncEnabled.collectAsState(initial = null)
//	val showPlainTextWarning by dataStoreInstance.showPlainTextWarningDropbox.collectAsState(initial = false)
//
//	var isReconnectDialogVisible by remember { mutableStateOf(false) }
//	var isDisconnectDialogVisible by remember { mutableStateOf(false) }
//	var selectedSnapshot by remember { mutableStateOf<File?>(null) }
//	fun openDialog(dialogType: GoogleDriveDialogType) = when (dialogType) {
//		GoogleDriveDialogType.Disconnect -> isDisconnectDialogVisible = true
//		GoogleDriveDialogType.RestoreSnapshot -> null
//		GoogleDriveDialogType.RestoringSnapshot -> null
//	}
//
//	fun closeDialog(dialogType: GoogleDriveDialogType) = when (dialogType) {
//		GoogleDriveDialogType.Disconnect -> isDisconnectDialogVisible = false
//		GoogleDriveDialogType.RestoreSnapshot -> selectedSnapshot = null
//		GoogleDriveDialogType.RestoringSnapshot -> null
//	}
//
//	var refreshing by remember { mutableStateOf(false) }
//	val pullRefreshState = rememberPullRefreshState(
//		refreshing = refreshing,
//		onRefresh = {
//			refreshing = true
//			refreshSnapshot()
//			refreshing = false
//		}
//	)
//
//	val isInternetAvailable = context.isInternetAvailable()
//
//	GenericScaffold(
//		topBar = { GenericTopBar(title = "Google Drive") },
//		dialogContent = {
//			GoogleDriveDialog(
//				showDisconnectDialog = isDisconnectDialogVisible,
//				snapshot = selectedSnapshot,
//				showRestoringSnapshotDialog = isRestoringSnapshot,
//				aboutState = aboutState,
//				onDisconnect = onClickDisconnect,
//				onShare = onClickShareSnapshot,
//				onRestore = onClickRestoreSnapshot,
//				onClickDelete = onClickDeleteSnapshot,
//				closeDialog = ::closeDialog,
//			)
//		},
//	) {
//		Box(
//			modifier = Modifier.pullRefresh(state = pullRefreshState)
//		) {
//			Column(
//				modifier = Modifier
//					.fillMaxSize()
//					.verticalScroll(rememberScrollState())
//			) {
//				AnimatedVisibility(
//					visible = !isInternetAvailable,
//					enter = expandVertically(tween(300)),
//					exit = shrinkVertically(tween(300)),
//				) {
//					InfoCard(
//						title = "No internet connection",
//						description = "It seems you are not connected to internet. Please connect to internet and try again.",
//						icon = R.drawable.ic_no_internet,
//						colors = InfoCardDefaults.warningCardColors(),
//						buttonText = "Retry",
//						modifier = Modifier.padding(horizontal = 12.dp),
//					) { onTestConnection() }
//				}
//				InfoCard(
//					title = "Experimental",
//					description = "This feature is still in development and may not work as expected. Please report any bugs you encounter.",
//					icon = R.drawable.ic_warning,
//					colors = InfoCardDefaults.warningCardColors(),
//					buttonText = "Report Bug",
//					modifier = Modifier.padding(horizontal = 12.dp)
//				) { context.startActivity(Intent(context, ReportActivity::class.java)) }
//				InfoCard(
//					title = "Backup vs Sync",
//					description = "Backup is a one time process that saves your data to the cloud at a regular interval (not available yet) or manually. Sync is a continuous process that keeps your data in sync with the cloud to be available on other devices.",
//					icon = R.drawable.ic_warning,
//					colors = InfoCardDefaults.infoCardColors(),
//					buttonText = "Learn More",
//					modifier = Modifier.padding(horizontal = 12.dp)
//				) { uriHandler.openUri("https://graphite.syncodec.com/#/backup_and_sync") }
//				PlainTextWarning(
//					isVisible = showPlainTextWarning,
//					modifier = Modifier.padding(horizontal = 12.dp),
//				) {
//					dataStoreInstance.putShowPlainTextWarningDropbox(false)
//				}
//				ConnectionCard(
//					aboutState = aboutState,
//				)
//				SettingButton(
//					text = "Connect with Google Drive",
//					icon = R.drawable.ic_logo_google_drive,
//					subIcon = if (aboutState is GoogleDriveSyncActivity.Companion.AboutState.Success) R.drawable.ic_checkmark else null,
//					tint = Color.Unspecified,
//					subIconTint = Color.Unspecified,
//					onClick = onClickConnect,
//				)
//				SettingButton(
//					text = "Test connection",
//					icon = R.drawable.ic_test_connection,
//					onClick = onTestConnection,
//				)
//				SettingButton(
//					text = "Disconnect",
//					icon = R.drawable.ic_cloud_x,
//					enabled = aboutState is GoogleDriveSyncActivity.Companion.AboutState.Success,
//					onClick = onClickDisconnect,
//				)
//				SettingSwitch(
//					text = "Auto Sync",
//					subText = if (isAutoSyncEnabled == true) "Auto sync is enabled" else "Auto sync is disabled",
//					icon = R.drawable.ic_sync,
//					isChecked = isAutoSyncEnabled != false,
//					enabled = aboutState is GoogleDriveSyncActivity.Companion.AboutState.Success,
//				) {
//					dataStoreInstance.setIsAutoSyncEnabled(it)
//					if (it) Toast.makeText(context, "Auto sync is enabled", Toast.LENGTH_SHORT).show()
//					else Toast.makeText(context, "Auto sync is disabled", Toast.LENGTH_SHORT).show()
//				}
//				SettingButton(
//					text = "Take Snapshot",
//					icon = R.drawable.ic_easy,
//					subText = "Save everything from database",
//					enabled = aboutState is GoogleDriveSyncActivity.Companion.AboutState.Success,
//				) {
//					if (isGeneratingSnapshot) Toast.makeText(context, "Please wait for the current snapshot to finish", Toast.LENGTH_SHORT).show()
//					else onClickGenerateSnapshot()
//				}
//				SettingButton(
//					text = "Learn more about backup and sync",
//					icon = R.drawable.ic_info,
//				) { uriHandler.openUri("https://graphite.syncodec.com/#/backup_and_sync") }
//				SettingsContentTitle(
//					title = "SNAPSHOT WAREHOUSE"
//				) {
//					AnimatedVisibility(visible = isGeneratingSnapshot) {
//						CircularProgressIndicator(
//							color = MaterialTheme.colorScheme.onSurface,
//							strokeWidth = 2.dp,
//							modifier = Modifier.size(16.dp),
//						)
//					}
//				}
//				if (snapshotList == null) {
//					DropboxEmptySnapshot()
//				}
//				else {
//					if (snapshotList is GDriveSyncInatorService.Companion.ListFiles.Success) {
//						snapshotList.fileList.map {
//							Pair(
//								it, try {
//									it.name.split("_").last().split(".").first().toLong()
//								} catch (e: Exception) {
//									-1
//								}
//							)
//						}
//							.sortedByDescending { it.second }
//							.forEachIndexed { index, pair ->
//								SnapshotButton(
//									fileName = pair.first.name,
//									createdTimestamp = pair.second,
//									isLatest = index == 0,
//								) { selectedSnapshot = pair.first }
//							}
//						Spacer(modifier = Modifier.height(128.dp))
//					}
//				}
//			}
//
//			PullRefreshIndicator(
//				refreshing = refreshing || isGeneratingSnapshot,
//				state = pullRefreshState,
//				backgroundColor = MaterialTheme.colorScheme.surface,
//				contentColor = MaterialTheme.colorScheme.onSurface,
//				modifier = Modifier.align(Alignment.TopCenter)
//			)
//		}
//	}
}

@Preview
@Composable
private fun ExperimentalCard() {
	val context = LocalContext.current

	val infiniteTransition = rememberInfiniteTransition()
	val scale by infiniteTransition.animateFloat(
		initialValue = 1f,
		targetValue = 1.47f,
		animationSpec = infiniteRepeatable(
			animation = tween(470, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		),
		label = "scale_animation"
	)

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 4.dp),
		colors = CardDefaults.cardColors(
			containerColor = Color.DeleteContainer,
			contentColor = Color.DeleteContent,
		),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_exclamation),
					contentDescription = "Experimental",
					modifier = Modifier
						.requiredSize(24.dp)
						.graphicsLayer {
							scaleX = scale
							scaleY = scale
						}
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = "Experimental",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = "This feature is experimental and may not work as expected",
				style = MaterialTheme.typography.bodyMedium,
			)

			Spacer(modifier = Modifier.height(24.dp))

			Button(
				colors = ButtonDefaults.buttonColors(
					containerColor = Color.DeleteContent,
					contentColor = Color.DeleteContainer,
				),
				onClick = { context.startActivity(Intent(context, ReportActivity::class.java)) },
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(text = "Report a bug")
			}
		}
	}
}
