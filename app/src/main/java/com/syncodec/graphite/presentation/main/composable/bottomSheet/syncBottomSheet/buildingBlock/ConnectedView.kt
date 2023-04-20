package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncActivity
import com.syncodec.graphite.service.SyncerService


@Preview
@Composable
fun ConnectedView(
	email : String = if (BuildConfig.DEBUG) "" else "",
	name : String? = null,
	spaceTotal : Long? = null,
	spaceUsed : Long? = null,
	syncStatus : SyncerService.Companion.SyncStatus = SyncerService.Companion.SyncStatus.Init,
	onForceSync : () -> Unit = {},
	onSync : () -> Unit = {},
) {
	val context = LocalContext.current

	Column(
		modifier = Modifier
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(MaterialTheme.shapes.medium)
				.background(
					MaterialTheme.colorScheme
						.surfaceColorAtElevation(8.dp)
						.copy(alpha = 0.17f)
				)
				.padding(16.dp)
		) {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = name ?: "Unknown",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)
				Text(
					text = email ?: "Unknown",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface
				)
				Spacer(modifier = Modifier.height(8.dp))

				DropboxSpaceUsage(
					spaceTotal = spaceTotal,
					spaceUsed = spaceUsed,
				)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		SyncStatusCard(
			syncStatus = syncStatus,
		)

		Spacer(modifier = Modifier.height(2.dp))

		SyncButton(
			onSync = onSync,
			onForceSync = onForceSync,
		)
	}
}

@Preview
@Composable
private fun ColumnScope.DropboxSpaceUsage(
	spaceTotal : Long? = null,
	spaceUsed : Long? = null,
) {
	this.apply {
		if (spaceTotal != null && spaceUsed != null) {
			val spaceTotalMb = spaceTotal / 1024 / 1024
			val spaceUsedMb = spaceUsed / 1024 / 1024
			val spaceUsedPercent = (spaceUsed.toFloat() / spaceTotal.toFloat()) * 100

			Row(
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(
					text = "Used: ${spaceUsedMb}MB / ${spaceTotalMb}MB",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = "${spaceUsedPercent.toInt()}%",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}

			Spacer(modifier = Modifier.height(6.dp))

			var startAnimation by remember { mutableStateOf(false) }
			val progress by animateFloatAsState(
				targetValue = if (startAnimation) (spaceUsed.toFloat() / spaceTotal.toFloat()) else 0f,
				animationSpec = tween(2400),
				label = ""
			)

			LaunchedEffect(key1 = startAnimation) { startAnimation = true }

			LinearProgressIndicator(
				progress = progress,
				trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
				color = if (spaceUsedPercent > 90) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
				strokeCap = StrokeCap.Round,
				modifier = Modifier
					.fillMaxWidth()
					.height(4.dp)
			)
		}

		val spaceUsedPercent = spaceUsed?.toFloat()?.div(spaceTotal?.toFloat() ?: 1f) ?: 0f
		if (spaceUsedPercent > 0.9) {
			Spacer(modifier = Modifier.height(12.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.clip(MaterialTheme.shapes.medium)
					.background(MaterialTheme.colorScheme.errorContainer)
					.padding(16.dp, 12.dp)
			) {

				Icon(
					painter = painterResource(id = R.drawable.ic_warning),
					contentDescription = "Warning",
					tint = MaterialTheme.colorScheme.onErrorContainer,
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = "You are running out of space. Synchronization might fail if no more space is available.",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onErrorContainer,
				)
			}
		}
	}
}

@Preview
@Composable
private fun ColumnScope.SyncButton(
	onSync : () -> Unit = {},
	onForceSync : () -> Unit = {},
) {
	val context = LocalContext.current

	this.apply {
		Button(
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
				contentColor = MaterialTheme.colorScheme.onSurface,
			),
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier.fillMaxWidth(),
			onClick = { context.startActivity(Intent(context, DropboxSyncActivity::class.java)) },
		) {
			Text(text = "Manage")
		}

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			Button(
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.errorContainer,
					contentColor = MaterialTheme.colorScheme.onErrorContainer,
				),
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onForceSync,
			) {
				Text(text = "Force Sync")
			}

			Spacer(modifier = Modifier.width(4.dp))

			Button(
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
				),
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onSync,
			) {
				Text(text = "Sync Now")
			}
		}
	}
}

@Preview
@Composable
private fun SyncStatusCard(
	syncStatus : SyncerService.Companion.SyncStatus = SyncerService.Companion.SyncStatus.Init,
) {
	when (syncStatus) {
		is SyncerService.Companion.SyncStatus.Init -> SyncStatusCardMessage(message = "Initializing... Try to connect with Dropbox.")
		is SyncerService.Companion.SyncStatus.Idle -> SyncStatusCardMessage(message = "Idle")
		is SyncerService.Companion.SyncStatus.Locked -> SyncStatusCardMessage(
			message = "It seems that another device is syncing. Trying again in few moments...\nIf you believe no other device is syncing or problem persists, try to force sync.",
			containerColor = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer,
		)

		is SyncerService.Companion.SyncStatus.Connected -> SyncStatusCardMessage(message = "Connected to Dropbox. Syncing...")
		is SyncerService.Companion.SyncStatus.Syncing -> SyncStatusSyncingCardMessage(
			message = "Syncing...",
			syncStatus = syncStatus,
			containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
			contentColor = MaterialTheme.colorScheme.onSurface,
		)
		is SyncerService.Companion.SyncStatus.Paused -> SyncStatusCardMessage(
			message = "Sync is disabled. Enable it in settings.",
			containerColor = Color(0x71FFD93D),
			contentColor = MaterialTheme.colorScheme.onSurface,
		)
		is SyncerService.Companion.SyncStatus.Failed -> SyncStatusCardMessage(
			message = "Failed. Try to sync again or force sync if problem persists.",
			containerColor = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer,
		)
		is SyncerService.Companion.SyncStatus.CredentialError -> SyncStatusCardMessage(
			message = "It seems like your credentials are expired. Try to reconnect with Dropbox from settings.",
			containerColor = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer,
		)
	}
}

@Preview
@Composable
private fun SyncStatusCardMessage(
	message : String = "Syncing...",
	containerColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor, MaterialTheme.shapes.medium)
			.padding(16.dp)
	) {
		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = contentColor,
		)
	}
}

@Composable
private fun SyncStatusSyncingCardMessage(
	message : String = "Syncing",
	syncStatus : SyncerService.Companion.SyncStatus.Syncing,
	containerColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor, MaterialTheme.shapes.medium)
			.padding(16.dp),
	) {
		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = contentColor,
		)
		Spacer(modifier = Modifier.width(4.dp))
		Spacer(modifier = Modifier.weight(1f))
		CircularProgressIndicator(
			color = MaterialTheme.colorScheme.onSurface,
			strokeWidth = 2.dp,
			modifier = Modifier.requiredSize(20.dp),
		)
	}
}

@Preview
@Composable
private fun SyncingStatusObjectView(
	objectType : String = "Chapter",
	isSynced : Boolean? = null,
	toUpSyncCount : Int = 0,
	toDownSyncCount : Int = 0,
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = objectType,
			style = MaterialTheme.typography.bodyMedium,
			color = contentColor,
		)
		Spacer(modifier = Modifier.width(4.dp))
		Spacer(modifier = Modifier.weight(1f))
		Spacer(modifier = Modifier.width(4.dp))
		when {
			isSynced == null -> Text(
				text = "~",
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor,
				modifier = Modifier.heightIn(16.dp),
			)
			isSynced == true-> Icon(
				painter = painterResource(id = R.drawable.ic_check_circle),
				contentDescription = "Synced",
				tint = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.requiredSize(16.dp),
			)
			else -> CircularProgressIndicator(
				color = MaterialTheme.colorScheme.onSurface,
				strokeWidth = 1.dp,
				modifier = Modifier.requiredSize(16.dp),
			)
		}
		Spacer(modifier =   Modifier.width(16.dp))
	}

}
