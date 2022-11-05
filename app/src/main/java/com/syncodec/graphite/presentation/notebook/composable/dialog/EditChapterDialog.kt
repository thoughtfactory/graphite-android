package com.syncodec.graphite.presentation.notebook.composable.dialog

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.ColorPickerDialog
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.presentation.common.notebook.NotebookImageChooser
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.toHexString


@Composable
fun EditChapterDialog(
	title: String?,
	description: String?,
	color: Color?,
	thumbnail: Bitmap?,
	showDialog: Boolean,
	onSave: (String?, String?, Color?, Bitmap?) -> Unit,
	onDismiss: () -> Unit,
) {
	val context = LocalContext.current

	var _title by remember { mutableStateOf(title) }
	var _description by remember { mutableStateOf(description) }
	var _color by remember { mutableStateOf(color) }

	var showColorPickerDialog by remember { mutableStateOf(false) }

	var showImagePicker by remember { mutableStateOf(false) }

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

	var currentImage by remember { mutableStateOf<Int?>(null) }
	var currentImageUri by remember { mutableStateOf<Uri?>(null) }

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
			colors = ButtonDefaults.buttonColors(containerColor = _color ?: MaterialTheme.colorScheme.primary),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
		) {
			Text(
				text = _color?.toHexString() ?: MaterialTheme.colorScheme.primary.toHexString(),
				color = _color?.getInverseBWColor() ?: MaterialTheme.colorScheme.onPrimary,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		NotebookImageChooser(
			currentImage = currentImage,
			currentImageUri = currentImageUri,
			keepStartPadding = false,
			onPickImage = {
				currentImageUri = it
				currentImage = null
			},
			onChooseImage = {
				currentImage = it
				currentImageUri = null
			}
		)

		DualActionButtons(
			primaryText = "Save",
			onPrimaryClick = {

				val bitmap = if (currentImage != null) {
					BitmapFactory.decodeResource(context.resources, currentImage!!)
				} else {
					currentImageUri?.let { it1 -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1)) }
				}

				val aspectRatio = if (bitmap != null) { bitmap.width.toFloat() / bitmap.height.toFloat() } else { 1f }

				val thumbnail = bitmap?.let { ThumbnailUtils.extractThumbnail(it, (512 * aspectRatio).toInt(), 512) }

				onSave(_title, _description, _color, thumbnail)
				onDismiss()
			},
			secondaryText = "Discard",
			onSecondaryClick = onDismiss
		)

	}

	ColorPickerDialog(
		color = _color ?: MaterialTheme.colorScheme.primary,
		showDialog = showColorPickerDialog,
		onSelectColor = { _color = it }
	) {
		showColorPickerDialog = false
	}
}
