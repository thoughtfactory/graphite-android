package com.syncodec.momento.noteComponent.modalBottomSheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.NoteViewModel

sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object MetadataBottomSheet : BottomSheetType()
	object AttachmentBottomSheet : BottomSheetType()
	object TagBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout(
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val viewModel: NoteViewModel = viewModel()
	val tagList by viewModel.tagList.collectAsState(listOf())

	when (viewModel.activityState.bottomSheetType.value) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet()
		BottomSheetType.MetadataBottomSheet -> MetadataBottomSheet { click, data ->
			onAction(click, data)
		}
		BottomSheetType.AttachmentBottomSheet -> AttachmentBottomSheet(
			attachmentMap = viewModel.attachmentMap
		) { click, data -> onAction(click, data) }
		BottomSheetType.TagBottomSheet -> TagBottomSheet(
			tagList = tagList,
			connectedTag = viewModel.connectedTag
		) { click, data -> onAction(click, data) }
	}
}
