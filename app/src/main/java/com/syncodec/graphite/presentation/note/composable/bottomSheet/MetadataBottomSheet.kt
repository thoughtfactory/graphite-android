package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyCard
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCreatedTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionModifiedTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionParentChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import io.realm.kotlin.types.RealmUUID


@Composable
fun MetadataBottomSheet() {
	val id = LocalCompositionNoteId.current
	val parentChapter = LocalCompositionParentChapter.current
	val createdTimestamp = LocalCompositionCreatedTimestamp.current
	val modifiedTimestamp = LocalCompositionModifiedTimestamp.current
	val title = LocalCompositionTitle.current

	val closeSheet = LocalCompositionCloseBottomSheet.current
	val openDialog = LocalCompositionOpenDialog.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		BottomSheetStrip()

		BottomSheetHeader(
			title = "Metadata",
			icon = R.drawable.ic_info
		)

		BottomSheetKeyCard(
			id = id,
			createdTimestamp = createdTimestamp ?: 0,
			modifiedTimestamp = modifiedTimestamp ?: 0
		)

		Spacer(modifier = Modifier.height(8.dp))

		Spacer(modifier = Modifier.height(8.dp))

		ParentCard(
			chapterId = parentChapter?.id,
			chapterTitle = parentChapter?.title,
		) {
			openDialog(NoteDialogType.CHAPTER_SELECTION, parentChapter?.id)
			closeSheet()
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentCard(
	chapterId: RealmUUID?,
	chapterTitle: String?,
	onClick: () -> Unit
) {
	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground
		),
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		Row(
			modifier = Modifier
				.padding(12.dp, 8.dp)
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier
			) {
				Text(
					text = chapterTitle ?: "Chapter Untitled",
					style = MaterialTheme.typography.titleMedium,
					fontStyle = if (chapterTitle == null) FontStyle.Italic else FontStyle.Normal,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)

				Text(
					text = "$chapterId",
					style = MaterialTheme.typography.bodyMedium,
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			Spacer(modifier = Modifier.weight(1f))

			Icon(
				painter = painterResource(id = R.drawable.ic_notebook),
				contentDescription = "Parent chapter",
				modifier = Modifier.requiredSize(32.dp),
				tint = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}
