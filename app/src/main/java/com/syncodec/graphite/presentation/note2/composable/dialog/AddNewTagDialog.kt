package com.syncodec.graphite.presentation.note2.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults
import io.mhssn.colorpicker.ColorPicker
import io.mhssn.colorpicker.ColorPickerType


@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun AddNewTagDialog(
	isDialogVisible: Boolean = true,
	onConfirmDelete: () -> Unit = {},
	onDismiss: () -> Unit = {},
) {
//	ColorPickerDialog(
//		show = isDialogVisible,
//		type = ColorPickerType.SimpleRing(),
//		properties = DialogProperties(),
//		onDismissRequest = {
////			showDialog = false
//		},
//		onPickedColor = {
////			color = it
//		},
//	)
	GenericDialog2(
		isDialogVisible = isDialogVisible,
		title = "New tag",
		primaryButton = GenericDialogDefaults.genericDialogButtonWarning(text = "Delete", onClick = onConfirmDelete),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismiss),
	) {
		ColorPicker(
			type = ColorPickerType.Classic(
				showAlphaBar = true
			),
			onPickedColor = {},
			modifier = Modifier
		)
	}
}


