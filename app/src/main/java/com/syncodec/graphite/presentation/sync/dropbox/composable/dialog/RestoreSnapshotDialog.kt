package com.syncodec.graphite.presentation.sync.dropbox.composable.dialog

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import com.dropbox.core.v2.files.Metadata
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.getInverseBWColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Preview
@Composable
fun RestoreSnapshotDialog(
	snapshot : Metadata? = null,
	onShare : (Metadata) -> Unit = {},
	onRestore : (Metadata) -> Unit = {},
	onDismiss : () -> Unit = {}
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current
	var job by remember { mutableStateOf(null as Job?) }

	var timeout by remember { mutableStateOf(1) }

	LaunchedEffect(key1 = snapshot) {
		if (snapshot != null) {
			timeout = if (BuildConfig.DEBUG) 0 else 10
			job?.cancel()
			job = scope.launch {
				while (timeout > 0) {
					delay(1000)
					timeout --
				}
			}
		}
	}

	val restoreButtonContainerColor by animateColorAsState(
		targetValue = if (timeout < 1) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background,
		animationSpec = tween(300),
		label = "restoreButtonContainerColor_animation"
	)

	GenericDialog(
		showDialog = snapshot != null,
		icon = R.drawable.ic_warning,
		iconTint = MaterialTheme.colorScheme.error,
		title = "Restore Snapshot",
		contentText = "Restoring snapshot is a destructive operation. Take a snapshot of the current state of data in case something goes wrong.\n\nDeleting snapshot directly from Graphite is not supported yet but you can delete it from Dropbox.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Restore${if (timeout > 0) " ($timeout)" else ""}",
				secondaryText = "Cancel",
				primaryColor = restoreButtonContainerColor,
				primaryEnabled = timeout < 1,
				onClickPrimary = {
					if (timeout < 1) {
						snapshot?.let {
							onRestore(it)
							onDismiss()
						} ?: Toast.makeText(context, "Error restoring snapshot", Toast.LENGTH_SHORT).show()
					}
				},
				onClickSecondary = onDismiss
			)
		},
		thirdActionButton = {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth(),
			) {
				Button(
					onClick = {
						snapshot?.let {
							onShare(it)
							onDismiss()
						} ?: Toast.makeText(context, "Error sharing snapshot", Toast.LENGTH_SHORT).show()
					},
					shape = MaterialTheme.shapes.medium,
					colors = ButtonDefaults.buttonColors(
						containerColor = restoreButtonContainerColor,
						contentColor = restoreButtonContainerColor.getInverseBWColor(),
					),
					enabled = timeout < 1,
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = "Share Snapshot${if (timeout > 0) " ($timeout)" else ""}",
						color = restoreButtonContainerColor.getInverseBWColor(),
					)
				}

				MenuButton(
					icon = R.drawable.ic_launch,
					colors = MenuButtonDefaults.menuButtonColorsOnSurface()
				) {
					try {
						uriHandler.openUri("https://www.dropbox.com/home/Apps/Graphite%20Data${snapshot?.pathDisplay}")
					} catch (e : Exception) {
						Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
					}
				}
			}
		},
		onDismissRequest = onDismiss,
	)
}
