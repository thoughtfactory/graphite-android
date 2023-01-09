package com.syncodec.graphite.presentation.dropbox.composable.screen

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.syncodec.graphite.presentation.bugReport.BugReportActivity
import com.syncodec.graphite.presentation.dropbox.DropboxActivity
import com.syncodec.graphite.presentation.dropbox.composable.bar.TopBar
import com.syncodec.graphite.presentation.dropbox.composable.dialog.DropboxDialog
import com.syncodec.graphite.presentation.dropbox.composable.dialog.DropboxDialogType
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropboxScreen(
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current

	val spaceUsage = DropboxActivity.LocalSpaceUsage.current

	val signInWithDropbox = DropboxActivity.LocalSignInWithDropbox.current
	val testConnection = DropboxActivity.LocalTestConnection.current
	val disconnect = DropboxActivity.LocalDisconnect.current

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
				ExperimentalCard()

				SettingsButton(
					title = "Connect with Dropbox",
					icon = R.drawable.ic_logo_dropbox,
					subTitle = "Get OAuth2 token from Dropbox to sync your files",
					iconColor = Color.Unspecified
				) { signInWithDropbox(context) }

				SettingsButton(
					title = "Enter Dropbox OAuth2 code",
					icon = R.drawable.ic_keyboard,
					subTitle = "Enter OAuth2 code to connect with Dropbox"
				) {
					openDialog(DropboxDialogType.ENTER_OAUTH2_CODE_DIALOG)
				}

				SettingsButton(
					title = "Test connection",
					icon = R.drawable.ic_test_connection,
					subTitle = "Test connection to Dropbox"
				) { testConnection() }

				DropboxSpaceUsage(spaceUsage = spaceUsage)

				SettingsButton(
					title = "Disconnect from Dropbox",
					icon = R.drawable.ic_cloud_x,
					subTitle = "Remove Dropbox connection",
					containerColor = Color.Companion.DeleteContainer.copy(alpha = 0.71f),
					contentColor = Color.Companion.DeleteContent,
					iconColor = Color.Companion.DeleteContent,
				) { disconnect() }
			}
		}
	}

	DropboxDialog()
}

@Preview
@Composable
private fun DropboxSpaceUsage(
	spaceUsage : SpaceUsage? = null,
) {
	val usedSpaceB = spaceUsage?.used
	val usedSpaceKb = usedSpaceB?.div(1024)
	val usedSpaceMb = usedSpaceKb?.div(1024)
	val usedSpaceGb = usedSpaceMb?.div(1024)

	val usedSpace = if (usedSpaceGb != null && usedSpaceGb > 0) {
		"$usedSpaceGb GB"
	} else if (usedSpaceMb != null && usedSpaceMb > 0) {
		"$usedSpaceMb MB"
	} else if (usedSpaceKb != null && usedSpaceKb > 0) {
		"$usedSpaceKb KB"
	} else if (usedSpaceB != null && usedSpaceB > 0) {
		"$usedSpaceB B"
	} else {
		"0 B"
	}

	AnimatedVisibility(
		visible = spaceUsage != null,
		enter = expandVertically(tween(300)),
		exit = shrinkVertically(tween(300))
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 4.dp)
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.medium)
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
						text = "Used: $usedSpace",
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
		)
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
				onClick = {
					Intent(context, BugReportActivity::class.java).apply {
						context.startActivity(this)
					}
				},
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(text = "Report a bug")
			}
		}
	}
}
