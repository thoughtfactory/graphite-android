package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.runtime.*
import io.realm.kotlin.types.RealmUUID


enum class BucketBottomSheetType {
	MENU,
	AddTodo,
	AddBook,
	AddShow,
	AddLink,
	PreviewTodo,
	PreviewLink,
}

@Composable
fun SheetLayout(
	sheetType : BucketBottomSheetType,
	previewBucketItemObjectId : RealmUUID? = null,
	id : RealmUUID? = null,
	description : String? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	onClickEdit : () -> Unit = {},
	onClickShare : () -> Unit = {},
	onClickDelete : () -> Unit = {},
	closeSheet : () -> Unit = {},
) {
	when (sheetType) {
		BucketBottomSheetType.MENU -> MenuBottomSheet(
			id = id,
			description = description,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
			onClickEdit = onClickEdit,
			onClickShare = onClickShare,
			onClickDelete = onClickDelete,
			closeSheet = closeSheet
		)

		BucketBottomSheetType.AddTodo -> AddTodoBottomSheet(
			closeSheet = closeSheet
		)
		BucketBottomSheetType.AddBook -> AddBookBottomSheet()
		BucketBottomSheetType.AddShow -> AddShowBottomSheet()
		BucketBottomSheetType.AddLink -> AddLinkBottomSheet(
			closeSheet = closeSheet
		)
		BucketBottomSheetType.PreviewTodo -> TodoPreviewBottomSheet(
			previewBucketItemObjectId = previewBucketItemObjectId,
			closeSheet = closeSheet,
		)
		BucketBottomSheetType.PreviewLink -> LinkPreviewBottomSheet(
			previewBucketItemObjectId = previewBucketItemObjectId,
			closeSheet = closeSheet,
		)
	}
}
