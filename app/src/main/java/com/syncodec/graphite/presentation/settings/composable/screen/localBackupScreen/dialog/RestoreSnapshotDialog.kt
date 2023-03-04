package com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.dialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.getInverseBWColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Preview
@Composable
fun RestoreSnapshotDialog(
	showDialog : Boolean = true,
	onShare : () -> Unit = {},
	onRestore : () -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()
	var job by remember { mutableStateOf(null as Job?) }

	var timeout by remember { mutableStateOf(1) }

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			timeout = if (BuildConfig.DEBUG) 0 else 10
			job?.cancel()
			job = scope.launch {
				while (timeout > 0) {
					delay(1000)
					timeout--
				}
			}
		}
	}

	val restoreButtonContainerColor by animateColorAsState(
		targetValue = if (timeout < 1) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)

	GenericDialog(
		showDialog = showDialog,
		icon = R.drawable.ic_warning,
		iconTint = MaterialTheme.colorScheme.error,
		title = "Restore Snapshot",
		contentText = "Restoring snapshot is a destructive operation. Take a snapshot of the current state of data in case something goes wrong.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Restore${if (timeout > 0) " ($timeout)" else ""}",
				secondaryText = "Cancel",
				primaryColor = restoreButtonContainerColor,
				primaryEnabled = timeout < 1,
				onClickPrimary = {
					if (timeout < 1) {
						onRestore()
						onDismiss()
					}
				},
				onClickSecondary = onDismiss
			)
		},
		thirdActionButton = {
			Button(
				onClick = {
					if (timeout < 1) {
						onShare()
						onDismiss()
					}
				},
				shape = MaterialTheme.shapes.medium,
				colors = ButtonDefaults.buttonColors(
					containerColor = restoreButtonContainerColor,
					contentColor = restoreButtonContainerColor.getInverseBWColor(),
				),
				enabled = timeout < 1,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Share Snapshot${if (timeout > 0) " ($timeout)" else ""}",
					color = restoreButtonContainerColor.getInverseBWColor(),
				)
			}
		},
		onDismissRequest = onDismiss,
	)
}
