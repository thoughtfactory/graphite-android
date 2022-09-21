package com.syncodec.graphite.presentation.notebook.composable.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.MenuButton
import com.syncodec.graphite.presentation.custom.dialog.ColorPickerDialog
import com.syncodec.graphite.presentation.custom.dialog.GenericDialog
import com.syncodec.graphite.presentation.custom.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.custom.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.toHexString


@Composable
fun EditChapterDialog(
	title: String,
	description: String?,
	color: Color,
	showDialog: Boolean,
	onSave: (String, String?, Color) -> Unit,
	onDismiss: () -> Unit,
) {
	var _title by remember { mutableStateOf(title) }
	var _description by remember { mutableStateOf(description) }
	var _color by remember { mutableStateOf(color) }

	var showColorPickerDialog by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = title) {
		_title = title
	}
	LaunchedEffect(key1 = description) {
		_description = description
	}
	LaunchedEffect(key1 = color) {
		_color = color
	}

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			_title = title
			_description = description
			_color = color
		}
	}

//	TODO Some bug in Compose
	val focusManager = LocalFocusManager.current
	val titleFocusRequester = remember { FocusRequester() }
	val descriptionFocusRequester = remember { FocusRequester() }

	GenericDialog(
		showDialog = showDialog,
		onDismissRequest = onDismiss
	) {
		Text(
			text = "Edit Chapter",
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		DialogTextField(
			text = _title,
			label = "Title",
			placeholder = "An interesting title",
			trailingIcon = {
				MenuButton(
					icon = R.drawable.ic_close,
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				) { _title = "" }
			},
			onKeyboardAction = {
//				titleFocusRequester.freeFocus()
//				descriptionFocusRequester.captureFocus()
			},
		) { _title = it ?: "" }

		Spacer(modifier = Modifier.height(8.dp))

		DialogTextField(
			text = _description,
			label = "Description",
			placeholder = "What is it about?",
			maxLines = 7,
			trailingIcon = {
				MenuButton(
					icon = R.drawable.ic_close,
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				) { _description = null }
			},
			onKeyboardAction = { focusManager.clearFocus(false) },
		) { _description = it }

		Spacer(modifier = Modifier.height(8.dp))

		Button(
			onClick = { showColorPickerDialog = true },
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = _color
			)
		) {
			Text(
				text = _color.toHexString(),
				color = _color.getInverseBWColor(),
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		DualActionButtons(
			primaryText = "Save",
			onPrimaryClick = {
				onSave(_title, _description, _color)
				onDismiss()
			},
			secondaryText = "Discard",
			onSecondaryClick = onDismiss
		)

	}

	ColorPickerDialog(
		color = _color,
		showDialog = showColorPickerDialog,
		onSelectColor = { _color = it }
	) {
		showColorPickerDialog = false
	}

}
