package com.syncodec.graphite.presentation.notebook.composable.dialog

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.presentation.common.chapter.ChapterCoverPicker


@Preview
@Composable
fun EditChapterDialog(
	title: String? = null,
	description: String? = null,
	color: Color? = null,
	thumbnail: Bitmap? = null,
	showDialog: Boolean = true,
	onSave: (String?, String?, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
	onDismiss: () -> Unit = { },
) {
	val context = LocalContext.current

	var _title by remember { mutableStateOf(title) }
	var _description by remember { mutableStateOf(description) }
	var coverColor by remember { mutableStateOf(color) }
	var coverImage by remember { mutableStateOf<Int?>(null) }
	var coverUri by remember { mutableStateOf<Uri?>(null) }

	LaunchedEffect(key1 = title) {
		_title = title
	}
	LaunchedEffect(key1 = description) {
		_description = description
	}
	LaunchedEffect(key1 = color) {
		coverColor = color
	}

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			_title = title
			_description = description
			coverColor = color
		}
	}

	GenericDialog(
		showDialog = showDialog,
		title = "Edit Chapter",
		onDismissRequest = onDismiss
	) {
		DialogTextField(
			value = _title ?: "",
			label = "Title",
			placeholder = "An interesting title",
			onValueChange = { _title = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		DialogTextField(
			value = _description ?: "",
			label = "Description",
			placeholder = "What is it about?",
			onValueChange = { _description = it },
		)

		Spacer(modifier = Modifier.height(8.dp))

		ChapterCoverPicker(
			coverColor = coverColor,
			coverImage = coverImage,
			coverUri = coverUri,
			onPickColor = { coverColor = it; coverUri = null; coverImage = null },
			onChooseImage = { coverColor = null; coverUri = null; coverImage = it },
			onPickImage = { coverColor = null; coverUri = it; coverImage = null },
		)

		Spacer(modifier = Modifier.height(24.dp))

		DualActionButtons(
			primaryText = "Save",
			onPrimaryClick = {

				val bitmap = coverImage?.let { BitmapFactory.decodeResource(context.resources, it) } ?:
				 coverUri?.let { it1 -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1)) }

				val aspectRatio = if (bitmap != null) { bitmap.width.toFloat() / bitmap.height.toFloat() } else { 1f }

				val _thumbnail = bitmap?.let { ThumbnailUtils.extractThumbnail(it, (512 * aspectRatio).toInt(), 512) }

				if (_thumbnail == null) onSave(_title, _description, coverColor, null)
				else onSave(_title, _description, null, _thumbnail)

				onDismiss()
			},
			secondaryText = "Discard",
			onSecondaryClick = onDismiss
		)
	}
}
