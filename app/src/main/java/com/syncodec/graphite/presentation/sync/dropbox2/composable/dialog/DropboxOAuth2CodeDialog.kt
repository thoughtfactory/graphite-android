package com.syncodec.graphite.presentation.sync.dropbox2.composable.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.common.button.PasteButton
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults


@Preview
@Composable
fun DropboxOAuth2CodeDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	onConnect: (String) -> Unit = {},
) {

	var code by rememberSaveable { mutableStateOf("") }

	GenericDialog2(
		isDialogVisible = isDialogVisible,
		onDismissRequest = onDismissRequest,
		title = "Enter OAuth2 Code",
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = "Connect", onClick = { onConnect(code) }),
		secondaryButton = GenericDialogDefaults.genericDialogButtonSecondary(text = stringResource(R.string.dismiss), onClick = onDismissRequest),
	) {
		OutlinedTextField(
			value = code,
			shape = MaterialTheme.shapes.medium,
			onValueChange = { code = it },
			label = { Text(text = "OAuth2 Code") },
			trailingIcon = { PasteButton { code = it } },
			maxLines = 1,
			singleLine = true,
			modifier = Modifier.fillMaxWidth()
		)
	}
}
