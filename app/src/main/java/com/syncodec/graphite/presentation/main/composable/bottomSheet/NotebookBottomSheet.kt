package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.button.LargeButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.dialog.ColorPickerDialog
import com.syncodec.graphite.presentation.common.notebook.NotebookColorChooser
import com.syncodec.graphite.presentation.common.notebook.NotebookImageChooser
import com.syncodec.graphite.presentation.common.text.LargeTextField
import com.syncodec.graphite.presentation.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NotebookBottomSheet(
	closeSheet: () -> Unit
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val viewModel: MainViewModel = viewModel()
	val keyboardController = LocalSoftwareKeyboardController.current

	var titleText by rememberSaveable { mutableStateOf("") }
	var isTitleTextFocused by remember { mutableStateOf(false) }
	val titleTextFocusRequester = remember { FocusRequester() }

	var descriptionText by rememberSaveable { mutableStateOf("") }
	var isDescriptionTextFocused by remember { mutableStateOf(false) }
	val descriptionTextFocusRequester = remember { FocusRequester() }

	val chooserStateList = listOf(
		StateData(title = "Color"),
		StateData(title = "Image"),
	)
	var chooserType by rememberSaveable { mutableStateOf(0) }

	var notebookColor by remember { mutableStateOf<Color?>(null) }
	var notebookImage by remember { mutableStateOf<Int?>(null) }
	var notebookImageUri by remember { mutableStateOf<Uri?>(null) }

	var showColorPicker by remember { mutableStateOf(false) }

	ColorPickerDialog(
		color = notebookColor ?: MaterialTheme.colorScheme.primary,
		showDialog = showColorPicker,
		onSelectColor = {
			notebookColor = it
			showColorPicker = false
		}
	) { showColorPicker = false }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
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

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = chooserStateList,
			currentState = chooserType,
			containerColor = MaterialTheme.colorScheme.background,
			modifier = Modifier
				.fillMaxWidth()
				.height(36.dp)
				.padding(24.dp, 0.dp)
		) { chooserType = it }

		Spacer(modifier = Modifier.height(8.dp))

		Crossfade(targetState = chooserType) {
			when (it) {
				0 -> NotebookColorChooser(
					currentColor = notebookColor,
					onChooseColor = { color ->
						notebookColor = color
						notebookImage = null
					},
					onClickColorPicker = {
						notebookImage = null
						notebookImageUri = null
						showColorPicker = true
					}
				)

				1 -> NotebookImageChooser(
					currentImage = notebookImage,
					currentImageUri = notebookImageUri,
					onChooseImage = { image ->
						notebookImage = image
						notebookColor = null
						notebookImageUri = null
					},
					onPickImage = {
						notebookImageUri = it
						notebookImage = null
						notebookColor = null
					}
				)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "Create",
			enabled = !(notebookColor == null && notebookImage == null && notebookImageUri == null) && titleText.isNotBlank(),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			scope.launch(Dispatchers.IO) {
				when {
					titleText.isEmpty() -> Toast.makeText(context, "Notebook title cannot be empty", Toast.LENGTH_SHORT).show()
					notebookColor == null && notebookImage == null && notebookImageUri == null ->
						Toast.makeText(context, "Select a color or image for notebook", Toast.LENGTH_SHORT).show()

					else -> {

						val bitmap = if (notebookImage != null) {
							BitmapFactory.decodeResource(context.resources, notebookImage!!)
						} else {
							notebookImageUri?.let { it1 -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1)) }
						}

						val aspectRatio = if (bitmap != null) bitmap.width.toFloat() / bitmap.height.toFloat() else 1f

						val thumbnail = bitmap?.let { ThumbnailUtils.extractThumbnail(it, (192 * aspectRatio).toInt(), 192) }

						viewModel.putNotebook(title = titleText, description = descriptionText, color = notebookColor, bitmap = thumbnail)

						titleTextFocusRequester.freeFocus()
						descriptionTextFocusRequester.freeFocus()
						keyboardController?.hide()
						closeSheet()

						titleText = ""
						descriptionText = ""
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
