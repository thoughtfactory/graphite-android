package com.syncodec.graphite.bucketComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.database.bucket.BucketDbEntry
import com.syncodec.graphite.database.bucketItem.BucketItemPreviewDbEntry


sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object AddTodoSheet : BottomSheetType()
	object AddBookSheet : BottomSheetType()
	object AddMovieSheet : BottomSheetType()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout(
	bucketDbEntry: BucketDbEntry?,
	bucketItemDbEntry: BucketItemPreviewDbEntry?,
	bottomSheetType: BottomSheetType,
	dataType: BucketActivity.DataType,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	when (bottomSheetType) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet(
			createdTimestamp = bucketDbEntry?.createdTimestamp ?: -1,
			modifiedTimestamp = bucketDbEntry?.modifiedTimestamp ?: -1,
		)
		BottomSheetType.AddTodoSheet -> AddTodoSheet(
			key = bucketItemDbEntry?.key,
			title = bucketItemDbEntry?.title ?: "",
			state = bucketItemDbEntry?.state?.ordinal ?: 0,
			hashCode = bucketItemDbEntry.hashCode(),
		) { key, title, state ->
			onAction(BucketActivity.Action.ADD_TODO, Triple(key, title, state))
		}
		BottomSheetType.AddBookSheet -> AddBookSheet {
			onAction(BucketActivity.Action.ADD_BOOK, it)
		}
		BottomSheetType.AddMovieSheet -> AddShowSheet(
			dataType = dataType,
			onAction = onAction
		)
	}
}
