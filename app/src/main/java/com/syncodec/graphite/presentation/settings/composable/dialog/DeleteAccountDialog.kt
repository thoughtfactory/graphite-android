package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun DeleteAccountDialog(
	showDialog : Boolean = false,
	onDelete : () -> Unit = { },
	onDismiss : () -> Unit = { },
) {
	val scope = rememberCoroutineScope()
	var timeout by remember { mutableStateOf(1) }

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			timeout = if (BuildConfig.DEBUG) 1 else 10
			scope.launch {
				while (timeout > 0) {
					delay(1000)
					timeout --
				}
			}
		}
	}

	DisposableEffect(key1 = showDialog) {
		onDispose { if (! showDialog) timeout = 0 }
	}

	val deleteButtonContainerColor by animateColorAsState(
		targetValue = if (timeout < 1) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)

	GenericDialog(
		showDialog = showDialog,
		icon = R.drawable.ic_warning,
		iconTint = MaterialTheme.colorScheme.error,
		title = "Delete Account",
		contentText = "Deleting your account will remove all your data from our servers including all the subscriptions and purchases you have made. This action cannot be undone.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Delete${if (timeout > 0) " ($timeout)" else ""}",
				secondaryText = "Cancel",
				primaryColor = deleteButtonContainerColor,
				primaryEnabled = timeout < 1,
				onClickPrimary = onDelete,
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss,
	)
}
