package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dropbox.core.v2.users.SpaceUsage
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.dropbox.DropboxActivity
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import com.syncodec.graphite.service.DropboxService


@Preview
@Composable
fun SyncBottomSheet(
	syncStatus : DropboxService.Companion.DropboxSyncStatus = DropboxService.Companion.DropboxSyncStatus.Init,
	name : String? = "Dropbox",
	email : String? = "",
	spaceUsage : SpaceUsage? = null,
	lastSynced : Long? = null,
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
) {
	val context = LocalContext.current

	GenericBottomSheet(
		title = "Sync",
		icon = R.drawable.ic_cloud
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f), MaterialTheme.shapes.medium)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 12.dp)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_logo_dropbox),
					contentDescription = "Dropbox",
					tint = Color.Unspecified,
					modifier = Modifier.requiredSize(32.dp)
				)

				Spacer(modifier = Modifier.width(16.dp))

				Column(
					horizontalAlignment = Alignment.Start,
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = email ?: "No email",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)

					Text(
						text = name ?: "No name",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier
					)
				}

				Spacer(modifier = Modifier.width(16.dp))

				MenuButton(
					icon = R.drawable.ic_setting,
					modifier = Modifier.requiredSize(32.dp)
				) {
					Intent(context, DropboxActivity::class.java).apply {
						context.startActivity(this)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		when (syncStatus) {
			is DropboxService.Companion.DropboxSyncStatus.Init -> DriveStatusView(message = "Initializing sync")

			is DropboxService.Companion.DropboxSyncStatus.SyncNotConfigured -> DriveStatusView(
				message = "Sync is not configured. Please configure sync to use this feature."
			)

			is DropboxService.Companion.DropboxSyncStatus.SyncDisabled -> DriveStatusView(
				message = "Sync is disabled. Please enable sync to use this feature."
			)

			is DropboxService.Companion.DropboxSyncStatus.NoInternet -> DriveStatusView(
				message = "No internet connection. Sync will resume when connection is established."
			)

			is DropboxService.Companion.DropboxSyncStatus.NotLoggedIn -> DriveStatusView(message = "Connect to Dropbox to use this feature.")
			is DropboxService.Companion.DropboxSyncStatus.Loading -> DriveStatusView(message = "Loading...")
			is DropboxService.Companion.DropboxSyncStatus.Connected -> DriveStatusView(message = "Connected to Dropbox")
			is DropboxService.Companion.DropboxSyncStatus.Syncing -> DriveStatusSyncingView(syncStatus = syncStatus)
			is DropboxService.Companion.DropboxSyncStatus.SyncError -> DriveStatusErrorView(syncStatus = syncStatus)
			is DropboxService.Companion.DropboxSyncStatus.DriveLocked -> DriveStatusView(
				message = "Drive is blocked by another device. Sync will resume when the other device release the lock."
			)

			is DropboxService.Companion.DropboxSyncStatus.Idle -> DriveStatusView(message = "Sync completed")
		}

		Spacer(modifier = Modifier.height(6.dp))

		Button(
			onClick = onClickSyncNow,
			modifier = Modifier.fillMaxWidth(),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
			),
		) {
			Text(
				text = "Sync Now",
				modifier = Modifier
			)
		}

		Button(
			onClick = onClickForceSync,
			modifier = Modifier.fillMaxWidth(),
			colors = ButtonDefaults.buttonColors(
				containerColor = Color.Companion.DeleteContainer,
				contentColor = Color.Companion.DeleteContent,
			),
		) {
			Text(
				text = "Force Sync",
				modifier = Modifier
			)
		}
	}
}

@Preview
@Composable
private fun DriveStatusView(
	message : String = "Drive status message"
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f), MaterialTheme.shapes.medium)
	) {
		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(12.dp)
		)
	}
}

@Preview
@Composable
private fun DriveStatusErrorView(
	syncStatus : DropboxService.Companion.DropboxSyncStatus.SyncError = DropboxService.Companion.DropboxSyncStatus.SyncError("Error message"),
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f), MaterialTheme.shapes.medium)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Text(
				text = "Error syncing to Dropbox",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = syncStatus.message,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier
			)
		}
	}
}


@Composable
private fun DriveStatusSyncingView(
	syncStatus : DropboxService.Companion.DropboxSyncStatus.Syncing
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
		) {
			SyncCounter(
				title = "Chapter",
				toUpSync = syncStatus.toUpSyncChapterCount,
				toDownSync = syncStatus.toDownSyncChapterCount,
				modifier = Modifier.weight(1f),
			)
			Spacer(modifier = Modifier.width(4.dp))
			SyncCounter(
				title = "Note",
				toUpSync = syncStatus.toUpSyncNoteCount,
				toDownSync = syncStatus.toDownSyncNoteCount,
				modifier = Modifier.weight(1f),
			)
		}
		Spacer(modifier = Modifier.height(2.dp))
		Row(
			modifier = Modifier
		) {
			SyncCounter(
				title = "Bucket",
				toUpSync = syncStatus.toUpSyncBucketCount,
				toDownSync = syncStatus.toDownSyncBucketCount,
				modifier = Modifier.weight(1f),
			)
			Spacer(modifier = Modifier.width(4.dp))
			SyncCounter(
				title = "Bucket Item",
				toUpSync = syncStatus.toUpSyncBucketItemCount,
				toDownSync = syncStatus.toDownSyncBucketItemCount,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@Preview
@Composable
private fun SyncCounter(
	modifier : Modifier = Modifier,
	title : String = "Chapter",
	toUpSync : Int = 0,
	toDownSync : Int = 0
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f), MaterialTheme.shapes.large)
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.padding(0.dp, 8.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier
			)

			Spacer(modifier = Modifier.width(2.dp))

			Row(
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_caret),
					contentDescription = "Up sync",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.requiredSize(10.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = toUpSync.toString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
				)

				Spacer(modifier = Modifier.width(12.dp))

				Icon(
					painter = painterResource(id = R.drawable.ic_caret),
					contentDescription = "Down sync",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.requiredSize(10.dp)
						.graphicsLayer { rotationZ = 180f }
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = toDownSync.toString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
				)
			}
		}
	}
}
