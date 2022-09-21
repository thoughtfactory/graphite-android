package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.text.LargeTextField
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.custom.button.LargeButton
import com.syncodec.graphite.presentation.custom.notebook.NotebookColorChooser
import com.syncodec.graphite.presentation.main.MainViewModel


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NotebookBottomSheet(
	closeSheet: () -> Unit
) {
	val context = LocalContext.current
	val viewModel: MainViewModel = viewModel()
	val keyboardController = LocalSoftwareKeyboardController.current

	var titleText by rememberSaveable { mutableStateOf("") }
	var isTitleTextFocused by remember { mutableStateOf(false) }
	val titleTextFocusRequester = remember { FocusRequester() }

	var descriptionText by rememberSaveable { mutableStateOf("") }
	var isDescriptionTextFocused by remember { mutableStateOf(false) }
	val descriptionTextFocusRequester = remember { FocusRequester() }

	var notebookColor by remember { mutableStateOf<Color?>(null) }
	var notebookImage by remember { mutableStateOf<Int?>(null) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(color = MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Writing a new book?",
			icon = R.drawable.ic_notebook,
			subTitle = "Keep your notes organized in notebooks"
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			text = titleText,
			placeholder = "Give your book a title",
			isFocused = isTitleTextFocused,
			focusRequester = titleTextFocusRequester,
			onFocusChanged = { isTitleTextFocused = it },
		) { titleText = it }

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			text = descriptionText,
			placeholder = "And a little description",
			isFocused = isDescriptionTextFocused,
			focusRequester = descriptionTextFocusRequester,
			onFocusChanged = { isDescriptionTextFocused = it },
		) { descriptionText = it }

		Spacer(modifier = Modifier.height(6.dp))

		NotebookColorChooser(
			currentColor = notebookColor
		) { color ->
			notebookColor = color
			notebookImage = null
		}

		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "Create",
			enabled = !(notebookColor == null && notebookImage == null) && titleText.isNotBlank(),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			when {
				titleText.isEmpty() -> Toast.makeText(context, "Notebook title cannot be empty", Toast.LENGTH_SHORT).show()
				notebookColor == null -> Toast.makeText(context, "Select a color for notebook", Toast.LENGTH_SHORT).show()
				else -> {

					viewModel.putNotebook(title = titleText, description = descriptionText, color = notebookColor, bitmap = null)

					titleTextFocusRequester.freeFocus()
					descriptionTextFocusRequester.freeFocus()
					keyboardController?.hide()
					closeSheet()

					titleText = ""
					descriptionText = ""
				}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
