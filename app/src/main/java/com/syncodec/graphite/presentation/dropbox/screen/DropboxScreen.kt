package com.syncodec.graphite.presentation.dropbox.screen

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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dropbox.core.v2.users.SpaceUsage
import com.syncodec.graphite.R
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.presentation.bugReport.BugReportActivity
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.dropbox.screen.bar.TopBar
import com.syncodec.graphite.presentation.dropbox.screen.dialog.DropboxDialog
import com.syncodec.graphite.presentation.dropbox.screen.dialog.DropboxDialogType
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import org.koin.androidx.compose.koinViewModel


@Preview
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropboxScreen() {
	val viewModel : DropboxScreenViewModel = koinViewModel()

	val uriHandler = LocalUriHandler.current

	val dropboxState by viewModel.dropboxState.collectAsState()

	var isEnterOAuthCodeDialogVisible by remember { mutableStateOf(false) }
	fun openDialog(dialogType : DropboxDialogType) = when (dialogType) {
		DropboxDialogType.EnterOAuthCodeDialog -> isEnterOAuthCodeDialogVisible = true
	}

	fun closeDialog(dialogType : DropboxDialogType) = when (dialogType) {
		DropboxDialogType.EnterOAuthCodeDialog -> isEnterOAuthCodeDialogVisible = false
	}

	GenericScaffold(
		topBar = { TopBar() },
		dialogContent = {
			DropboxDialog(
				showEnterOAuth2CodeDialog = isEnterOAuthCodeDialogVisible,
				onEnterOAuthCode = {
					viewModel.exchangeCodeForToken(it)
					closeDialog(DropboxDialogType.EnterOAuthCodeDialog)
				},
				closeDialog = ::closeDialog,
			)
		},
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			ExperimentalCard()
			SettingButton(
				text = "Connect with Dropbox",
				icon = R.drawable.ic_logo_dropbox,
				tint = Color.Unspecified,
			) {
//				uriHandler.openUri(DBox.DROPBOX_AUTH_URL)
				uriHandler.openUri(DBox.DROPBOX_CONNECT)
			}
			SettingButton(
				text = "Enter OAuth2 Code",
				icon = R.drawable.ic_keyboard,
			) { openDialog(DropboxDialogType.EnterOAuthCodeDialog) }
			SettingButton(
				text = "Test connection",
				icon = R.drawable.ic_test_connection,
			) { viewModel.testConnection() }
			SettingButton(
				text = "Disconnect",
				icon = R.drawable.ic_cloud_x,
			)
			dropboxState.let {
				AnimatedVisibility(visible = it is DBox.Companion.DropboxState.Connected) {
					if (it is DBox.Companion.DropboxState.Connected) {
						DropboxSpaceUsage(spaceUsage = it.spaceUsage)
					}
				}
			}
		}
	}
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
				.background(
					MaterialTheme.colorScheme
						.surfaceColorAtElevation(8.dp)
						.copy(alpha = 0.47f), MaterialTheme.shapes.medium
				)
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
				onClick = { context.startActivity(Intent(context, BugReportActivity::class.java)) },
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(text = "Report a bug")
			}
		}
	}
}
