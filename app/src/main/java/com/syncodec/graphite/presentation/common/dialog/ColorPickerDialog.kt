package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.toColor
import com.syncodec.graphite.utils.toHexString


@Composable
fun ColorPickerDialog(
	color: Color,
	showDialog: Boolean,
	onSelectColor: (Color) -> Unit,
	onDismiss: () -> Unit
) {
	var _color by remember { mutableStateOf(color) }
	var hexColorString by remember { mutableStateOf(color.toHexString().substring(1)) }

	LaunchedEffect(key1 = color) {
		_color = color
	}

	GenericDialog(
		showDialog = showDialog,
		title = "Color Picker",
		onDismissRequest = onDismiss
	) {
		ClassicColorPicker(
			color = _color,
			onColorChanged = {
				_color = it.toColor()
				hexColorString = _color.toHexString().substring(1)
			},
			showAlphaBar = false,
			modifier = Modifier
				.fillMaxWidth()
				.height(256.dp)
		)

		Spacer(modifier = Modifier.height(8.dp))

		DialogTextField(
			text = hexColorString,
			label = "Hex Color",
			placeholder = color.toHexString(),
			leadingIcon = {
				MenuButton(
					icon = R.drawable.ic_hashtag_2,
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
					isEnabled = false
				) {
					// Do nothing
				}
			},
			trailingIcon = {
				MenuButton(
					icon = R.drawable.ic_check,
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				) {
					_color = hexColorString.toColor(color)
					hexColorString = _color.toHexString().substring(1)
				}
			},
			maxLines = 1
		) {
			if (it != null) {
				hexColorString = it
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		DualActionButtons(
			primaryText = "Save",
			onPrimaryClick = {
				onSelectColor(_color)
				onDismiss()
			},
			secondaryText = "Cancel",
			onSecondaryClick = onDismiss,
			primaryColor = _color,
			secondaryColor = color,
			isOutlinedButton = false
		)
	}
}
