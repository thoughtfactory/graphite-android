package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.atlas.AtlasActivity
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.calendar.CalendarActivity
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyValueCard
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.dialog.NotebookDialogType
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.timeStampToPrettyFull


@Preview
@Composable
fun MenuBottomSheet() {
	val context = LocalContext.current

	val chapterId = NotebookActivity.LocalChapterId.current
	val createdTimestamp = NotebookActivity.LocalCreatedTimestamp.current
	val modifiedTimestamp = NotebookActivity.LocalModifiedTimestamp.current
	val title = NotebookActivity.LocalTitle.current
	val description = NotebookActivity.LocalDescription.current
	val color = NotebookActivity.LocalColor.current
	val thumbnail = NotebookActivity.LocalThumbnail.current

	val tagList = listOf<TagObject>()

	val onSetDefaultChapter = NotebookActivity.LocalOnSetDefaultChapter.current

	val openDialog = NotebookActivity.LocalOpenDialog.current
	val closeSheet = NotebookActivity.LocalCloseBottomSheet.current

	GenericBottomSheet(
		title = "Menu",
		icon = R.drawable.ic_menu,
	) {

		BottomSheetButtonGrid(
			buttonList = listOf(
				{
					BottomSheetButton(title = "Set as Default", icon = R.drawable.ic_sparkle, onClick = onSetDefaultChapter)
				},
				{
					BottomSheetButton(title = "Edit", icon = R.drawable.ic_pencil) {
						closeSheet()
						openDialog(NotebookDialogType.EDIT_CHAPTER)
					}
				},
				{
					BottomSheetButton(
						title = "Delete",
						icon = R.drawable.ic_delete,
						containerColor = Color.DeleteContainer,
						contentColor = Color.DeleteContent
					) {
						closeSheet()
						openDialog(NotebookDialogType.DELETE_CHAPTER)
					}
				},
				{
					BottomSheetButton(title = "Attachment", icon = R.drawable.ic_file) {
						closeSheet()
						Intent(context, AttachmentActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId?.bytes)
							context.startActivity(this)
						}
					}
				},
				{
					BottomSheetButton(title = "Calendar", icon = R.drawable.ic_calendar) {
						closeSheet()
						Intent(context, CalendarActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId?.bytes)
							context.startActivity(this)
						}
					}
				},
				{
					BottomSheetButton(title = "Atlas", icon = R.drawable.ic_atlas) {
						closeSheet()
						Intent(context, AtlasActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId?.bytes)
							context.startActivity(this)
						}
					}
				}
			)
		)

		Spacer(modifier = Modifier.height(6.dp))
		Spacer(
			modifier = Modifier
				.fillMaxWidth(0.71f)
				.height(1.dp)
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
		)
		Spacer(modifier = Modifier.height(6.dp))

		BottomSheetKeyValueCard(
			key = "ID",
			value = chapterId?.toString() ?: "",
		)
		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Created On",
			value = createdTimestamp?.timeStampToPrettyFull(),
		)
		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Modified On",
			value = modifiedTimestamp?.timeStampToPrettyFull(),
		)
		Spacer(modifier = Modifier.height(4.dp))

		DescriptionCard(
			description = description,
			color = color,
			thumbnail = thumbnail,
		)
	}
}

@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DescriptionCard(
	description : String? = null,
	color : Color? = null,
	thumbnail : Bitmap? = null,
) {
	val context = LocalContext.current
	val clipboardManager : ClipboardManager = LocalClipboardManager.current

	var size by remember { mutableStateOf(IntSize(0, 0)) }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(color = color ?: Color.Transparent, shape = MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
			.combinedClickable(
				onClick = {},
				onLongClick = {
					description?.let { clipboardManager.setText(AnnotatedString(it)) }
						?: run {
							Toast
								.makeText(context, "No value to copy", Toast.LENGTH_SHORT)
								.show()
						}
				}
			)
			.onGloballyPositioned {
				size = it.size
			}
	) {
		thumbnail?.let {
			Image(
				bitmap = it.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = with(LocalDensity.current) {
					Modifier
						.width(size.width.toDp())
						.height(size.height.toDp())
				}
			)
			Box(
				modifier = with(LocalDensity.current) {
					Modifier
						.width(size.width.toDp())
						.height(size.height.toDp())
				}.background(Color.Black.copy(alpha = 0.13f))
			)
		}

		Column(
			modifier = Modifier.padding(12.dp)
		) {
			Text(
				text = "Description",
				style = MaterialTheme.typography.bodySmall,
				color = color?.getInverseBWColor() ?: Color.White,
			)
			Text(
				text = if (description.isNullOrEmpty()) "No value" else description ?: "No value",
				style = MaterialTheme.typography.bodyMedium,
				color = color?.getInverseBWColor() ?: Color.White,
				fontWeight = FontWeight.Bold,
				fontStyle = if (description.isNullOrEmpty()) FontStyle.Italic else null
			)
		}
	}
}
