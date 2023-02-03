package com.syncodec.graphite.presentation.note.screen.editorScreen.dialog

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import io.github.esentsov.PackagePrivate


@PackagePrivate
@Preview
@Composable
fun LocationPermissionDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current

	GenericDialog(
		showDialog = showDialog,
		title = "Location Permission",
		contentText = "We will need your permission access the location from your device.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Request",
				secondaryText = "Cancel",
				onClickPrimary = {
					onDismiss()
					Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
						addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						this.data = Uri.fromParts("package", context.packageName, null)
						ContextCompat.startActivity(context, this, null)
					}
				},
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss
	)
}
