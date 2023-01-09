package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bottomSheet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextField
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.chapter.ChapterCoverPicker
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ChapterBottomSheet(
	putChapter : (String, String, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val keyboardController = LocalSoftwareKeyboardController.current

	var titleText by rememberSaveable { mutableStateOf("") }
	var descriptionText by rememberSaveable { mutableStateOf("") }

	var coverColor by remember { mutableStateOf<Color?>(null) }
	var coverImage by remember { mutableStateOf<Int?>(null) }
	var coverUri by remember { mutableStateOf<Uri?>(null) }

	val closeSheet = NotebookActivity.LocalCloseBottomSheet.current

	GenericBottomSheet(
		title = "Writing a new chapter?",
		icon = R.drawable.ic_notebook,
	) {

		BottomSheetTextField(
			label = "Title",
			placeholder = "Give your chapter a title",
			value = titleText,
			onValueChange = { titleText = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetTextField(
			label = "Description",
			placeholder = "Add a little description",
			value = descriptionText,
			onValueChange = { descriptionText = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		ChapterCoverPicker(
			coverColor = coverColor,
			coverImage = coverImage,
			coverUri = coverUri,
			onPickColor = { coverColor = it; coverUri = null; coverImage = null },
			onChooseImage = { coverColor = null; coverUri = null; coverImage = it },
			onPickImage = { coverColor = null; coverUri = it; coverImage = null },
		)

		Spacer(modifier = Modifier.height(2.dp))

		Button(
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
				disabledContainerColor = MaterialTheme.colorScheme.surface,
				disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
			),
			enabled = ! (coverColor == null && coverImage == null && coverUri == null) && titleText.isNotBlank(),
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				scope.launch(Dispatchers.Default) {
					when {
						titleText.isEmpty() -> Toast.makeText(context, "Notebook title cannot be empty", Toast.LENGTH_SHORT).show()
						coverColor == null && coverImage == null && coverUri == null ->
							Toast.makeText(context, "Select a color or image for notebook", Toast.LENGTH_SHORT).show()

						else -> {

							val bitmap = if (coverImage != null) {
								BitmapFactory.decodeResource(context.resources, coverImage !!)
							} else {
								coverUri?.let { it1 -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1)) }
							}

							val aspectRatio = if (bitmap != null) bitmap.width.toFloat() / bitmap.height.toFloat() else 1f

							val thumbnail = bitmap?.let { ThumbnailUtils.extractThumbnail(it, (192 * aspectRatio).toInt(), 192) }

							putChapter(titleText, descriptionText, coverColor, thumbnail)

							keyboardController?.hide()
							closeSheet()

							titleText = ""
							descriptionText = ""
							coverColor = null
							coverImage = null
							coverUri = null
						}
					}
				}
			}
		) {
			Text(text = "Create")
		}
	}
}
