package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.mainComponent.MainViewModel
import kotlinx.coroutines.launch


sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object BucketBottomSheet : BottomSheetType()
	object NotebookBottomSheet : BottomSheetType()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout() {
	val scope = rememberCoroutineScope()
	val viewModel: MainViewModel = viewModel()
	when (viewModel.activityState.bottomSheetType.value) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet()
		BottomSheetType.BucketBottomSheet -> BucketBottomSheet { bucketName, bucketType ->
			viewModel.createNewBucket(bucketType = bucketType, title = bucketName)
			scope.launch { viewModel.activityState.bottomSheetState.hide() }
		}
		BottomSheetType.NotebookBottomSheet -> NotebookBottomSheet()
	}
}
