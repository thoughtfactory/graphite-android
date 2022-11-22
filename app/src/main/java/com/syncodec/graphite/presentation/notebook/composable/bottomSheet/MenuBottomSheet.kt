package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.atlas.AtlasActivity
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.calendar.CalendarActivity
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.common.info.InfoView
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.dialog.NotebookDialogType
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.getInverseBWColor


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

	val onDeleteChapter = NotebookActivity.LocalOnDeleteChapter.current
	val openDialog = NotebookActivity.LocalOpenDialog.current
	val closeSheet = NotebookActivity.LocalCloseBottomSheet.current

	val buttonList : List<BottomSheetButtonData> = listOf(
		BottomSheetButtonData(
			title = "Set as Default",
			icon = R.drawable.ic_state,
			onClick = onSetDefaultChapter
		),
		BottomSheetButtonData(
			title = "Edit",
			icon = R.drawable.ic_pencil,
			onClick = {
				closeSheet()
				openDialog(NotebookDialogType.EDIT_CHAPTER)
			}
		),
		BottomSheetButtonData(
			title = "Delete",
			icon = R.drawable.ic_delete,
			containerColor = Color.DeleteContainer,
			contentColor = Color.DeleteContent,
			onClick = {
				closeSheet()
				openDialog(NotebookDialogType.DELETE_CHAPTER)
			}
		),
		BottomSheetButtonData(
			title = "Attachment",
			icon = R.drawable.ic_attachment,
			onClick = {
				closeSheet()
				Intent(context, AttachmentActivity::class.java).apply {
					putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId?.bytes)
					context.startActivity(this)
				}
			}
		),
		BottomSheetButtonData(
			title = "Calendar",
			icon = R.drawable.ic_calendar,
			onClick = {
				closeSheet()
				Intent(context, CalendarActivity::class.java).apply {
					putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId?.bytes)
					context.startActivity(this)
				}
			}
		),
		BottomSheetButtonData(
			title = "Atlas",
			icon = R.drawable.ic_atlas,
			onClick = {
				closeSheet()
				Intent(context, AtlasActivity::class.java).apply {
					putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId?.bytes)
					context.startActivity(this)
				}
			}
		)
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			icon = R.drawable.ic_menu,
		)

		Spacer(modifier = Modifier.height(8.dp))

		BottomSheetButtonGrid(buttonList = buttonList)

		InfoView(
			id = chapterId,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
			description = description,
			color = color ?: MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
			thumbnail = thumbnail,
		)

		TagView(
			tagList = tagList.filter { it.objectIdList.contains(chapterId) }.map { it.toLite() },
		)

//		Spacer(modifier = Modifier.height(4.dp))
//
//		Button(
//			modifier = Modifier
//				.fillMaxWidth()
//				.padding(24.dp, 0.dp),
//			onClick = {
//				closeSheet()
//				Intent(context, TagsActivity::class.java).apply {
//					context.startActivity(this)
//				}
//			},
//		) {
//			Text(
//				text = "Manage Tag",
//				style = MaterialTheme.typography.bodyMedium,
//				fontWeight = FontWeight.Bold,
//			)
//		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagView(
	tagList : List<TagObjectLite>
) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
	) {
		tagList.forEach { tag ->
			SuggestionChip(
				onClick = { /*TODO*/ },
				label = {
					Text(
						text = tag.tag,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						color = Color(tag.color).getInverseBWColor()
					)
				},
				border = SuggestionChipDefaults.suggestionChipBorder(
					borderColor = Color(tag.color),
					borderWidth = 2.dp,
				),
				colors = SuggestionChipDefaults.suggestionChipColors(
					containerColor = Color(tag.color),
					labelColor = Color(tag.color).getInverseBWColor(),
				),
			)
		}
	}
}

@Composable
private fun DataView(
	totalChapter : Int?,
	totalNote : Int?
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
	) {
		DataItemView(
			text = "${totalChapter?.toString() ?: "Loading..."} Chapter${if (totalNote == 1) "" else "s"}",
			modifier = Modifier.weight(1f)
		)
		Spacer(modifier = Modifier.width(4.dp))
		DataItemView(
			text = "${totalNote?.toString() ?: "Loading..."} Note${if (totalNote == 1) "" else "s"}",
			modifier = Modifier.weight(1f)
		)
	}
}

@Composable
private fun DataItemView(
	modifier : Modifier = Modifier,
	text : String,
) {
	Card(
		shape = RoundedCornerShape(24.dp),
		colors = CardDefaults.outlinedCardColors(
			containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
			contentColor = MaterialTheme.colorScheme.onBackground
		),
		modifier = modifier.height(40.dp)
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier.fillMaxSize()
		) {
			Text(
				text = text,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
			)
		}
	}
}
