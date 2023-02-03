package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Preview
@Composable
fun RestoreSnapshotDialog(
	showDialog : Boolean = true,
	onRestore : () -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	var timeout by remember { mutableStateOf(1) }

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			timeout = 10
			scope.launch {
				for (i in 1 .. 10) {
					timeout --
					delay(1000)
				}
			}
		}
	}

	DisposableEffect(key1 = showDialog) {
		onDispose {
			if (!showDialog) timeout = 0
		}
	}

	val restoreButtonContainerColor by animateColorAsState(
		targetValue = if (timeout < 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)

	GenericDialog(
		showDialog = showDialog,
		title = "Restore Snapshot",
		onDismissRequest = onDismiss
	) {
		Box(
			modifier = Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.47f), RoundedCornerShape(8.dp))
		) {
			Text(
				text = "Restoring snapshot is a destructive operation. All data will be overwritten.",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.padding(8.dp)
			)
		}

		Spacer(modifier = Modifier.height(12.dp))

		DualActionButtons(
			primaryText = "Restore${if (timeout > 0) " ($timeout)" else ""}",
			secondaryText = "Cancel",
			primaryColor = restoreButtonContainerColor,
			onClickPrimary = {
				if (timeout < 1) {
					onRestore()
					onDismiss()
				}
			},
			onClickSecondary = onDismiss
		)
	}
}
