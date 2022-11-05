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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyCard
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTitleCard
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCreatedTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalCompositionModifiedTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionParentChapterId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun MetadataBottomSheet(
	onUpdateTitle : (String?) -> Unit
) {
	val isViewing = LocalCompositionIsViewing.current

	val id = LocalCompositionNoteId.current
	val parentChapterId = LocalCompositionParentChapterId.current
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

		BottomSheetTitleCard(
			title = title,
			placeholder = "Note title",
			onUpdateTitle = onUpdateTitle
		)

		Spacer(modifier = Modifier.height(8.dp))

		ParentCard(
			parentChapterId = parentChapterId,
		) {
			openDialog(NoteDialogType.CHAPTER_SELECTION, null)
			closeSheet()
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentCard(
	parentChapterId: ObjectId?,
	onClick: () -> Unit
) {
	val scope = rememberCoroutineScope()
	var chapterTitle by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(key1 = parentChapterId) {
		scope.launch(Dispatchers.IO) {
//			chapterTitle = parentChapterId?.let { Repository.getChapterTitle(id = it) }
		}
	}

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
					text = "$chapterTitle",
					style = MaterialTheme.typography.headlineMedium,
				)

				Text(
					text = "$parentChapterId",
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
