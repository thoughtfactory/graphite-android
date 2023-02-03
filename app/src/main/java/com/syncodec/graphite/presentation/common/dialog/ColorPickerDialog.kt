package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.toHexString


@Preview
@Composable
fun ColorPickerDialog(
	color : Color = MaterialTheme.colorScheme.primary,
	showDialog : Boolean = true,
	onSelectColor : (Color) -> Unit = {},
	onDismiss : () -> Unit = {}
) {
	var _color by remember { mutableStateOf(color) }
	var hexColorString by remember { mutableStateOf(color.toHexString().substring(1)) }

	LaunchedEffect(key1 = color) {
		_color = color
	}

	GenericDialog(
		showDialog = showDialog,
		title = "Color Picker",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Select",
				secondaryText = "Cancel",
				onClickPrimary = { onSelectColor(_color) },
				onClickSecondary = onDismiss,
				primaryColor = _color,
				secondaryColor = color
			)
		},
		onDismissRequest = onDismiss
	) {
		Spacer(modifier = Modifier.height(12.dp))

		ClassicColorPicker(
			color = HsvColor.from(color = _color), showAlphaBar = false,
			modifier = Modifier
				.fillMaxWidth()
				.height(256.dp),
			onColorChanged = {
				_color = it.toColor()
				hexColorString = _color.toHexString().substring(1)
			}
		)
	}
}
