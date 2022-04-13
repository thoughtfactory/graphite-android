package com.syncodec.momento.attachmentComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.attachmentComponent.AttachmentActivity
import com.syncodec.momento.attachmentComponent.AttachmentViewModel


sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout(
	onAction: (AttachmentActivity.Action, Any?) -> Unit
) {
	val viewModel: AttachmentViewModel = viewModel()
	when (viewModel.activityState.bottomSheetType.value) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet { onAction(it, null) }
	}
}
