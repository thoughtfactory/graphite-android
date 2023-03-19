package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncActivity


@Preview
@Composable
fun ConnectedView(
	email : String = "",
	name : String? = null,
	spaceTotal : Long? = null,
	spaceUsed : Long? = null,
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

		Spacer(modifier = Modifier.height(6.dp))

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
