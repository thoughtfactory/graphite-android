package com.syncodec.graphite.notebookComponent.modalBottomSheet

import androidx.compose.runtime.Composable
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.notebookComponent.NotebookActivity

sealed class BottomSheetType {
	object NewChapterBottomSheet : BottomSheetType()
	object NewNoteBottomSheet : BottomSheetType()
	object MenuBottomSheet : BottomSheetType()
	object MetadataBottomSheet : BottomSheetType()
	object EditBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout(
	bottomSheetType: BottomSheetType,
	notebookDbEntry: NotebookDbEntry?,
	chapterSize: Int,
	noteSize: Int,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	when (bottomSheetType) {
		BottomSheetType.NewChapterBottomSheet -> NewChapterBottomSheet { action, data ->
			onAction(action, data)
		}
		BottomSheetType.NewNoteBottomSheet -> NewNoteBottomSheet { action, data ->
			onAction(action, data)
		}
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet { onAction(it, null) }
		BottomSheetType.MetadataBottomSheet -> MetadataBottomSheet(
			key = notebookDbEntry?.key,
			noteSize = noteSize,
			chapterSize = chapterSize
		)
		BottomSheetType.EditBottomSheet -> EditBottomSheet(
			notebookDbEntry = notebookDbEntry
		) { action, data -> onAction(action, data) }
	}
}
