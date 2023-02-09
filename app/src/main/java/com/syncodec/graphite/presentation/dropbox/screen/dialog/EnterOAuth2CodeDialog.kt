package com.syncodec.graphite.presentation.dropbox.screen.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun EnterOAuth2CodeDialog(
	showDialog : Boolean,
	onEnterCode : (String) -> Unit,
	onDismiss : () -> Unit
) {

	var oAuthCodeText by remember { mutableStateOf("") }

	GenericDialog(
		showDialog = showDialog,
		title = "OAuth2 Code",
		onDismissRequest = {
			onDismiss()
		}
	) {
		DialogTextField(
			value = oAuthCodeText,
			label = "OAuth2 Code",
			placeholder = "Enter OAuth2 Code",
			supportingText = "Get the OAuth2 code by clicking \"Connect with Dropbox\"",
		) { oAuthCodeText = it ?: "" }

		Spacer(modifier = Modifier.height(24.dp))

		DualActionButtons(
			primaryText = "Ok",
			secondaryText = "Cancel",
			onClickPrimary = {
				onEnterCode(oAuthCodeText)
			},
			onClickSecondary = {
				oAuthCodeText = ""
				onDismiss()
			}
		)
	}
}
