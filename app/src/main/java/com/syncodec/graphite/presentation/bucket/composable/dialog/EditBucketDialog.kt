package com.syncodec.graphite.presentation.bucket.composable.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun EditBucketDialog(
	title: String?,
	description: String?,
	showDialog: Boolean,
	onDismiss: () -> Unit,
	onSave: (String?, String?) -> Unit,
) {

	val context = LocalContext.current

	var _title by remember { mutableStateOf(title) }
	var _description by remember { mutableStateOf(description) }

	LaunchedEffect(key1 = title) {
		_title = title
	}
	LaunchedEffect(key1 = description) {
		_description = description
	}

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			_title = title
			_description = description
		}
	}

	val focusManager = LocalFocusManager.current
	val titleFocusRequester = remember { FocusRequester() }
	val descriptionFocusRequester = remember { FocusRequester() }


	GenericDialog(
		showDialog = showDialog,
		title = "Edit Bucket",
		onDismissRequest = onDismiss
	) {
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

		DualActionButtons(
			primaryText = "Save",
			onPrimaryClick = {
				onSave(_title, _description)
				onDismiss()
			},
			secondaryText = "Discard",
			onSecondaryClick = onDismiss
		)
	}
}
