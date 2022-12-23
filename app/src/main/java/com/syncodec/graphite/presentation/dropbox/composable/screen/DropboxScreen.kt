package com.syncodec.graphite.presentation.dropbox.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dropbox.core.v2.users.SpaceUsage
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.composable.ProTag
import com.syncodec.graphite.presentation.dropbox.DropboxActivity
import com.syncodec.graphite.presentation.dropbox.composable.bar.TopBar
import com.syncodec.graphite.presentation.dropbox.composable.dialog.DropboxDialog
import com.syncodec.graphite.presentation.dropbox.composable.dialog.DropboxDialogType
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropboxScreen(
	onClickBack : () -> Unit,
) {
	val context = LocalContext.current

	val spaceUsage = DropboxActivity.LocalSpaceUsage.current

	val signInWithDropbox = DropboxActivity.LocalSignInWithDropbox.current
	val testConnection = DropboxActivity.LocalTestConnection.current

	val openDialog = DropboxActivity.LocalOpenDialog.current

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TopBar(onClickBack = onClickBack)
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
			) {
				SettingsButton(
					title = "Connect with Dropbox",
					icon = R.drawable.ic_dropbox,
					subTitle = "Connect with Dropbox to sync your data"
				) { signInWithDropbox(context) }

				SettingsButton(
					title = "Enter Dropbox OAuth2 code",
					icon = R.drawable.ic_keyboard,
					subTitle = "Enter Dropbox OAuth2 code to connect with Dropbox"
				) {
					openDialog(DropboxDialogType.ENTER_OAUTH2_CODE_DIALOG)
				}

				SettingsButton(
					title = "Test connection",
					icon = R.drawable.ic_test_connection,
					subTitle = "Test connection to Dropbox"
				) { testConnection() }

				DropboxSpaceUsage(spaceUsage = spaceUsage)
			}
		}
	}

	DropboxDialog()
}

@Composable
private fun DropboxSpaceUsage(
	spaceUsage : SpaceUsage?
) {
	AnimatedVisibility(
		visible = spaceUsage != null,
		enter = expandVertically(tween(300)),
		exit = shrinkVertically(tween(300))
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 4.dp)
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), RoundedCornerShape(12.dp))
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_cloud_storage),
					contentDescription = "Dropbox space usage",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.requiredSize(24.dp)
				)
				Spacer(modifier = Modifier.width(16.dp))

				Column(
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = "Space usage",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = "Total: ${spaceUsage?.allocation?.individualValue?.allocated?.div(1024 * 1024 * 1024)} GB",
						style = MaterialTheme.typography.bodySmall,
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = "Used: ${spaceUsage?.used?.div(1024 * 1024 * 1024)} GB",
						style = MaterialTheme.typography.bodySmall,
					)
					Spacer(modifier = Modifier.height(6.dp))
					LinearProgressIndicator(
						progress = spaceUsage?.used?.div((spaceUsage.allocation.individualValue.allocated + 1).toFloat()) ?: 0f,
						modifier = Modifier
							.fillMaxWidth()
							.height(4.dp),
						color = MaterialTheme.colorScheme.primary,
						trackColor = MaterialTheme.colorScheme.background
					)
				}
			}
		}
	}
}
