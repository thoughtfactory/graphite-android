package com.syncodec.graphite.presentation.sync.googleDrive.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextFieldDefaults
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Preview
@Composable
fun EnterAuthCodeDialog(
	showDialog : Boolean = false,
	onAuthorize : (String) -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	val clipboardManager = LocalClipboardManager.current
	var authCode by remember { mutableStateOf("") }

	GenericDialog(
		showDialog = showDialog,
		title = "Enter Auth Code",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Authorize",
				secondaryText = "Cancel",
				onClickPrimary = {
					onAuthorize(authCode)
					authCode = ""
					onDismiss()
				},
				onClickSecondary = onDismiss,
			)
		},
		onDismissRequest = onDismiss,
	) {
		DialogTextField(
			value = authCode,
			label = "Auth Code",
			placeholder = "Enter auth code",
			supportingText = "Get the OAuth2 code by clicking \"Connect with Dropbox\"",
			onValueChange = { authCode = it ?: "" },
			actionButtons = {
				MenuButton(
					icon = R.drawable.ic_paste,
					colors = MenuButtonDefaults.menuButtonColors(
						containerColor = DialogTextFieldDefaults.textFieldColors().containerColor,
						iconColor = DialogTextFieldDefaults.textFieldColors().textColor,
					)
				) {
					try {
						clipboardManager.getText()?.text?.let { text -> authCode = text }
					} catch (e : Exception) {
					}
				}
			}
		)
	}
}
