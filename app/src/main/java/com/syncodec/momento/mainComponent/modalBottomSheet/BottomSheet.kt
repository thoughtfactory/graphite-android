package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.MainActivity
import com.syncodec.momento.mainComponent.MainViewModel
import kotlinx.coroutines.launch


sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object BucketBottomSheet : BottomSheetType()
	object NotebookBottomSheet : BottomSheetType()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout(
	bottomSheetType: BottomSheetType,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	when (bottomSheetType) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet { onAction(it, null) }
		BottomSheetType.BucketBottomSheet -> BucketBottomSheet { bucketName, bucketType ->
			onAction(MainActivity.Action.NEW_BUCKET, Pair(bucketName, bucketType))
		}
		BottomSheetType.NotebookBottomSheet -> NotebookBottomSheet { action, notebookDbEntry ->
			onAction(action, notebookDbEntry)
		}
	}
}
