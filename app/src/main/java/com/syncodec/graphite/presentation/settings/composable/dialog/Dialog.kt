package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.clearData.ClearDataDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.exportData.ExportDataDialog


enum class SettingsDialogType {
	Biometric,
	ExportData,
	ClearData,
	NotificationPermission,
	DeleteAccount,
	ManageSubscription,
}

@Preview
@Composable
fun SettingsDialog(
	showBiometricDialog : Boolean = false,
	showExportDataDialog : Boolean = false,
	showClearDataDialog : Boolean = false,
	showNotificationPermissionDialog : Boolean = false,
	showDeleteAccountDialog : Boolean = false,
	showManageSubscriptionDialog : Boolean = false,
	onAddBiometricAuth : () -> Unit = { },
	onNotificationPermissionAvailable : () -> Unit = { },
	onDeleteAccount : () -> Unit = { },
	closeDialog : (SettingsDialogType) -> Unit = { },
) {
	BiometricAuthDialog(
		showDialog = showBiometricDialog,
		onAddBiometricAuth = onAddBiometricAuth,
	) { closeDialog(SettingsDialogType.Biometric) }
	ExportDataDialog(showDialog = showExportDataDialog) { closeDialog(SettingsDialogType.ExportData) }
	ClearDataDialog(showDialog = showClearDataDialog) { closeDialog(SettingsDialogType.ClearData) }
	NotificationPermissionDialog(
	)
	DeleteAccountDialog(
		showDialog = showDeleteAccountDialog,
		onDelete = onDeleteAccount,
		onDismiss = { closeDialog(SettingsDialogType.DeleteAccount) }
	)
	ManageSubscriptionDialog(
		showDialog = showManageSubscriptionDialog,
		onDismiss = { closeDialog(SettingsDialogType.ManageSubscription) }
	)
}
