package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
) {
	val context = LocalContext.current

	val dropboxEmail = "pnp.parmar@gmail.com"
	val lastSynced = "Last synced 2 days ago"

	GenericBottomSheet(
		title = "Sync",
		icon = R.drawable.ic_cloud
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.medium)
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
						text = dropboxEmail,
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)

					Text(
						text = lastSynced,
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

		if (syncStatus is DropboxService.Companion.DropboxSyncStatus.Syncing) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.medium)
			) {
				Column(modifier = Modifier) {
					Text(
						text = "To upSync ${syncStatus.toUpSyncCount} files",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onBackground,
					)

					Spacer(modifier = Modifier.height(4.dp))

					Text(
						text = "To downSync ${syncStatus.toDownSyncCount} files",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}
			}
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
