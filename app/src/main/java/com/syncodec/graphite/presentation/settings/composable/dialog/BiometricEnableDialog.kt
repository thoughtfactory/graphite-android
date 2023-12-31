package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericAlertDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults


@Preview
@Composable
fun BiometricEnableDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	onClickEnable: () -> Unit = {},
) {
	GenericAlertDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_fa_fingerprint),
		title = stringResource(id = R.string.biometric_authentication),
		contentText = stringResource(id = R.string.biometric_authentication_dialog_context),
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(id = R.string.enable), onClick = onClickEnable),
		secondaryButton = GenericDialogDefaults.genericDialogButtonSecondary(text = stringResource(id = R.string.dismiss), onClick = onDismissRequest),
		onDismissRequest = onDismissRequest,
	)
}
