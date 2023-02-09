package com.syncodec.graphite.presentation.dropbox.screen.dialog

import androidx.compose.runtime.Composable


enum class DropboxDialogType {
	EnterOAuthCodeDialog,
}

@Composable
fun DropboxDialog(
	showEnterOAuth2CodeDialog: Boolean = false,
	onEnterOAuthCode: (String) -> Unit = {},
	closeDialog: (DropboxDialogType) -> Unit = {},
) {
	EnterOAuth2CodeDialog(
		showDialog = showEnterOAuth2CodeDialog,
		onEnterCode = onEnterOAuthCode,
	) {
		closeDialog(DropboxDialogType.EnterOAuthCodeDialog)
	}
}
