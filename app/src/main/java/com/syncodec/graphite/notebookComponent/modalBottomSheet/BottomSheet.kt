package com.syncodec.graphite.notebookComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.notebookComponent.NotebookActivity
import com.syncodec.graphite.notebookComponent.NotebookViewModel

sealed class BottomSheetType {
	object NewChapterBottomSheet : BottomSheetType()
	object NewNoteBottomSheet : BottomSheetType()
	object MenuBottomSheet : BottomSheetType()
	object EditBottomSheet : BottomSheetType()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout(
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	val viewModel: NotebookViewModel = viewModel()
	when (viewModel.activityState.bottomSheetType.value) {
		BottomSheetType.NewChapterBottomSheet -> NewChapterBottomSheet { action, data ->
			onAction(action, data)
		}
		BottomSheetType.NewNoteBottomSheet -> NewNoteBottomSheet { action, data ->
			onAction(action, data)
		}
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet { onAction(it, null) }
		BottomSheetType.EditBottomSheet -> EditBottomSheet(
			notebookDbEntry = viewModel.notebookDbEntry.value!!
		) { action, data -> onAction(action, data) }
	}
}
