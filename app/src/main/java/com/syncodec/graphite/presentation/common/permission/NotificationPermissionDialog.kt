package com.syncodec.graphite.presentation.common.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults


@Composable
fun NotificationPermissionDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
) {
	val context = LocalContext.current

	GenericDialog2(
		isDialogVisible = isDialogVisible,
		onDismissRequest = onDismissRequest,
		title = stringResource(R.string.notification_permission_dialog_title),
		contentText = stringResource(R.string.get_notification_permission_message),
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(
			text = stringResource(R.string.request_permission),
			onClick = {
				onDismissRequest()
				Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
					addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					this.data = Uri.fromParts("package", context.packageName, null)
					startActivity(context, this, null)
				}
			}
		),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onDismissRequest)
	)
}
