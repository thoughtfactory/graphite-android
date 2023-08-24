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
}
