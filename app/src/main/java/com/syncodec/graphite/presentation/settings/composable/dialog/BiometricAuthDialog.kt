package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun BiometricAuthDialog(
	showDialog : Boolean = false,
	onAddBiometricAuth : () -> Unit = { },
	onDismiss : () -> Unit = { },
) {

	GenericDialog(
		showDialog = showDialog,
		icon = R.drawable.ic_biometric,
		title = "Biometric Authentication",
		contentText = "Graphite only accessible with biometric authentication.\n\n" +
				"Biometric will only prompt if you have enabled it in your device settings, else it will not request for authentication.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Enable",
				secondaryText = "Cancel",
				onClickPrimary = onAddBiometricAuth,
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss,
	)
}
