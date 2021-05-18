package com.syncodec.momento.notebookComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.notebookComponent.NotebookViewModel

sealed class BottomSheetType {
	object NewChapterBottomSheet : BottomSheetType()
	object NewNoteBottomSheet : BottomSheetType()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout() {
	val scope = rememberCoroutineScope()
	val notebookViewModel: NotebookViewModel = viewModel()
	when (notebookViewModel.activityState.bottomSheetType.value) {
		BottomSheetType.NewChapterBottomSheet -> NewChapterBottomSheet()
		BottomSheetType.NewNoteBottomSheet -> NewNoteBottomSheet()
	}
}
