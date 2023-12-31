package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet2

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetButton
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetButtonDefaults
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetInfo
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetInfoDefaults
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.sync.dropbox2.DropboxActivity
import com.syncodec.graphite.presentation.sync.dropbox2.composable.dropboxConnectionCard.DropboxProfileCard
import com.syncodec.graphite.service.syncInator.ObjectStatus
import com.syncodec.graphite.service.syncInator.ObjectSyncStatus
import com.syncodec.graphite.service.syncInator.SyncStat
import kotlinx.datetime.Clock


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SyncBottomSheet2(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	onDismissRequest: () -> Unit = { },
	syncStat: SyncStat = SyncStat.Init,
	dropboxAccountInfo: NetworkRequest<DropboxApi.Companion.DropboxAccountInfo> = NetworkRequest.Init,
	onClickTestConnection: () -> Unit = {},
	onClickForceSync: () -> Unit = {},
	onClickSyncNow: () -> Unit = {},
) {
	val context = LocalContext.current
	fun onClickManage() {
		context.startActivity(Intent(context, DropboxActivity::class.java))
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.sync)
		) {
			Spacer(modifier = Modifier.height(8.dp))
			DropboxProfileCard(
				dropboxAccountInfo = dropboxAccountInfo,
				hideOnError = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(8.dp))

			when (syncStat) {
				is SyncStat.Init -> LoadingView(
					modifier = Modifier
						.requiredSize(48.dp)
						.padding(8.dp)
						.align(Alignment.CenterHorizontally)
				)

				is SyncStat.Connecting -> LoadingView(
					modifier = Modifier
						.requiredSize(48.dp)
						.padding(8.dp)
						.align(Alignment.CenterHorizontally)
				)

				is SyncStat.NotConnected -> Surface(
					color = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
					shape = MaterialTheme.shapes.medium,
					modifier = Modifier.fillMaxWidth(),
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp),
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_logo_dropbox),
							contentDescription = "Dropbox",
							tint = Color.Unspecified,
							modifier = Modifier.requiredSize(48.dp),
						)
						Spacer(modifier = Modifier.width(12.dp))
						Column {
							Text(
								text = stringResource(id = R.string.connect_with_dropbox),
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold
							)
							Spacer(modifier = Modifier.height(4.dp))
							Text(
								text = stringResource(id = R.string.sync_your_data_across_devices),
								style = MaterialTheme.typography.bodyMedium,
							)
						}
					}
				}

				is SyncStat.Idle -> {
					GenericBottomSheetInfo(
						key = stringResource(id = R.string.last_synced_at),
						value = syncStat.lastSyncedAt?.let {
							val syncTimeDiff = it.minus(Clock.System.now())
							when {
								syncTimeDiff.inWholeSeconds < 60 -> stringResource(id = R.string.just_now)
								syncTimeDiff.inWholeMinutes < 60L -> "${stringResource(id = R.string.about)} ${syncTimeDiff.inWholeMinutes} ${stringResource(id = R.string.minutes_ago)}"
								else -> "At $it"
							}
						} ?: stringResource(id = R.string.unknown),
						colors = GenericBottomSheetInfoDefaults.successColors(),
					)

					if (!syncStat.isAutoSyncEnabled) {
						GenericBottomSheetInfo(
							key = stringResource(id = R.string.status),
							value = stringResource(id = R.string.auto_sync_disabled),
							colors = GenericBottomSheetInfoDefaults.warningInfoColors(),
						)
					}
				}

				is SyncStat.Locked -> GenericBottomSheetInfo(
					key = stringResource(id = R.string.status),
					value = stringResource(id = R.string.dropbox_locked),
					colors = GenericBottomSheetInfoDefaults.errorInfoColors(),
				)

				is SyncStat.Syncing -> {
					GenericBottomSheetInfo(
						key = stringResource(id = R.string.status),
						value = stringResource(id = R.string.syncing),
						colors = GenericBottomSheetInfoDefaults.pendingColors(),
					)

					Spacer(modifier = Modifier.height(4.dp))

					Surface(
						color = GenericBottomSheetInfoDefaults.pendingColors().containerColor,
						contentColor = GenericBottomSheetInfoDefaults.pendingColors().contentColor,
						shape = MaterialTheme.shapes.medium,
						modifier = Modifier.fillMaxWidth(),
					) {
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.padding(8.dp)
						) {
							ObjectStatusView(objectStatus = syncStat.chapterSyncObjectStatus, title = stringResource(id = R.string.chapter), icon = R.drawable.ic_fa_notebook)
							Hr()
							ObjectStatusView(objectStatus = syncStat.noteSyncObjectStatus, title = stringResource(id = R.string.note), icon = R.drawable.ic_fa_note)
							Hr()
							ObjectStatusView(objectStatus = syncStat.bucketSyncObjectStatus, title = stringResource(id = R.string.bucket), icon = R.drawable.ic_fa_bucket_list)
							Hr()
							ObjectStatusView(objectStatus = syncStat.bucketItemSyncObjectStatus, title = stringResource(id = R.string.bucket_item), icon = R.drawable.ic_fa_bucket_todo)
							Hr()
							ObjectStatusView(objectStatus = syncStat.tagSyncObjectStatus, title = stringResource(id = R.string.tag), icon = R.drawable.ic_fa_tag)
							Hr()
							ObjectStatusView(objectStatus = syncStat.attachmentSyncObjectStatus, title = stringResource(id = R.string.attachment), icon = R.drawable.ic_fa_file)
						}
					}
				}

				is SyncStat.Error -> GenericBottomSheetInfo(
					key = stringResource(id = R.string.status),
					value = when (syncStat) {
						is SyncStat.Error.NetworkError -> stringResource(id = R.string.syncing_network_error)
						is SyncStat.Error.CredentialsError -> stringResource(id = R.string.syncing_credentials_error)
						is SyncStat.Error.UnknownError -> stringResource(id = R.string.syncing_unknown_error)
					},
					colors = GenericBottomSheetInfoDefaults.warningInfoColors(),
				)
			}

			GenericBottomSheetButton(
				text = stringResource(id = R.string.manage),
				colors = GenericBottomSheetButtonDefaults.secondaryButtonColors(),
				modifier = Modifier.fillMaxWidth(),
				onClick = ::onClickManage,
			)

			Row {
				GenericBottomSheetButton(
					text = stringResource(id = R.string.force_sync),
					colors = GenericBottomSheetButtonDefaults.warningButtonColors(),
					modifier = Modifier.weight(1f),
					onClick = onClickForceSync,
				)

				Spacer(modifier = Modifier.width(4.dp))

				GenericBottomSheetButton(
					text = stringResource(id = R.string.sync_now),
					colors = GenericBottomSheetButtonDefaults.primaryButtonColors(),
					modifier = Modifier.weight(1f),
					onClick = onClickSyncNow,
				)
			}
		}
	}
}

@Composable
private fun ObjectStatusView(
	objectStatus: ObjectStatus,
	title: String,
	icon: Int,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(8.dp)
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = "Object status icon",
			modifier = Modifier.requiredSize(16.dp)
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold
		)
		Spacer(modifier = Modifier.weight(1f))

		when (objectStatus.objectSyncStatus) {
			ObjectSyncStatus.Waiting -> Icon(
				painter = painterResource(id = R.drawable.ic_fa_question),
				contentDescription = "Waiting icon",
				modifier = Modifier.requiredSize(16.dp)
			)

			ObjectSyncStatus.Syncing -> {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_down_sync_object),
					contentDescription = "Sync icon",
					modifier = Modifier.requiredSize(16.dp)
				)
				Text(
					text = objectStatus.toDownSync.toString(),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold
				)

				Spacer(modifier = Modifier.width(24.dp))

				Icon(
					painter = painterResource(id = R.drawable.ic_fa_up_sync_object),
					contentDescription = "Sync icon",
					modifier = Modifier.requiredSize(16.dp)
				)
				Text(
					text = objectStatus.toUpSync.toString(),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold
				)
			}

			ObjectSyncStatus.Success -> Icon(
				painter = painterResource(id = R.drawable.ic_fa_circle_check),
				contentDescription = "Sync success icon",
				modifier = Modifier.requiredSize(16.dp)
			)

			ObjectSyncStatus.Error -> Icon(
				painter = painterResource(id = R.drawable.ic_fa_warning),
				contentDescription = "Sync error icon",
				modifier = Modifier.requiredSize(16.dp)
			)
		}
	}
}

@Preview
@Composable
private fun Hr() {
	Spacer(modifier = Modifier
		.fillMaxWidth()
		.height(1.dp))
}
