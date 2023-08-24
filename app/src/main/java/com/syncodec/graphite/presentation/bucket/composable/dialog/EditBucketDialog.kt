package com.syncodec.graphite.presentation.bucket.composable.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Preview
@Composable
fun EditBucketDialog(
	title : String? = null,
	description : String? = null,
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
	onSave : (String?, String?) -> Unit = { _, _ -> }
) {

	var newTitle by remember { mutableStateOf(title) }
	var newDescription by remember { mutableStateOf(description) }

	LaunchedEffect(key1 = title) { newTitle = title }
	LaunchedEffect(key1 = description) { newDescription = description }

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			newTitle = title
			newDescription = description
		}
	}

	val focusManager = LocalFocusManager.current

	GenericDialog(
		showDialog = showDialog,
		title = "Edit Bucket",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Save",
				onClickPrimary = {
					onSave(newTitle, newDescription)
					onDismiss()
				},
				secondaryText = "Discard",
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss
	) {
		Spacer(modifier = Modifier.height(16.dp))
		DialogTextField(
			value = newTitle ?: "",
			label = "Title",
			placeholder = "An interesting title",
			onKeyboardAction = {
//				titleFocusRequester.freeFocus()
//				descriptionFocusRequester.captureFocus()
			},
		) { newTitle = it ?: "" }

		Spacer(modifier = Modifier.height(4.dp))

		DialogTextField(
			value = newDescription ?: "",
			label = "Description",
			placeholder = "What is it about?",
			onKeyboardAction = { focusManager.clearFocus(false) },
		) { newDescription = it }
	}
}
