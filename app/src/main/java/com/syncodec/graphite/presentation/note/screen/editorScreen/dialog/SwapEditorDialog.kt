package com.syncodec.graphite.presentation.note.screen.editorScreen.dialog

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Preview
@Composable
fun SwapEditorDialog(
	showDialog : Boolean = false,
	onSwapEditor : (Uri) -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		uri?.let {
			onSwapEditor(it)
			onDismiss()
		} ?: Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
	}

	GenericDialog(
		showDialog = showDialog,
		title = "Swap Editor",
		contentText = "You can swap the editor to another editor. Select index.html file that you want to swap with the current editor.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Select",
				secondaryText = "Cancel",
				onClickPrimary = { openFilePicker.launch(arrayOf("text/html")) },
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss
	)
}
