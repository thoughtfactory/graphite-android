package com.syncodec.graphite.presentation.dropbox.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.dropbox.DropboxActivity


enum class DropboxDialogType {
	ENTER_OAUTH2_CODE_DIALOG,
}

@Composable
fun DropboxDialog() {

	val showEnterOAuth2CodeDialog =  DropboxActivity.LocalShowEnterOAuth2CodeDialog.current

	val onEnterOAuthCode = DropboxActivity.LocalEnterOAuth2Code.current

	val closeDialog = DropboxActivity.LocalCloseDialog.current

	EnterOAuth2CodeDialog(
		showDialog = showEnterOAuth2CodeDialog,
		onEnterCode = onEnterOAuthCode,
	) {
		closeDialog(DropboxDialogType.ENTER_OAUTH2_CODE_DIALOG)
	}

}
