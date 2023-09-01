package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.getRandomColor
import io.mhssn.colorpicker.ColorPicker
import io.mhssn.colorpicker.ColorPickerType


@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ColorPickerDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	onSelectColor: (Color) -> Unit = {}
) {

	var selectedColor by remember { mutableStateOf(getRandomColor()) }

	GenericDialog2(
		isDialogVisible = isDialogVisible,
		title = stringResource(id = R.string.color_picker),
		primaryButton = GenericDialogDefaults.genericDialogButton(
			text = stringResource(id = R.string.select),
			buttonColors = ButtonDefaults.buttonColors(
				containerColor = selectedColor,
				contentColor = selectedColor.getInverseBWColor()
			),
			onClick = { onSelectColor(selectedColor) }
		),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
		onDismissRequest = onDismissRequest,
	) {
		ColorPicker(
			type = ColorPickerType.Classic(showAlphaBar = false),
			modifier = Modifier.align(Alignment.CenterHorizontally),
			onPickedColor = { selectedColor = it }
		)
	}
}
