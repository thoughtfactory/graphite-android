package com.syncodec.momento.bucketComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.bucketComponent.BucketViewModel


sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object AddBookSheet : BottomSheetType()
	object AddMovieSheet : BottomSheetType()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout(
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	val viewModel: BucketViewModel = viewModel()

	val bucketDbEntry by viewModel.bucketDbEntry

	when (viewModel.activityState.bottomSheetType.value) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet(
			createdTimestamp = bucketDbEntry?.createdTimestamp ?: -1,
			modifiedTimestamp = bucketDbEntry?.modifiedTimestamp ?: -1,
		)
		BottomSheetType.AddBookSheet -> AddBookSheet {
			onAction(BucketActivity.Action.ADD_BOOK, it)
		}
		BottomSheetType.AddMovieSheet -> AddShowSheet(
			dataType = viewModel.activityState.dataType.value,
			onAction = onAction
		)
	}
}
