package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.di.model.BucketType


enum class MainBottomSheetType {
	Menu,
	Filter,
	Bucket,
	Notebook,
	Sync
}

@Composable
fun SheetLayout(
	bottomSheetType : MainBottomSheetType = MainBottomSheetType.Menu,
	putBucket : (String?, String?, BucketType) -> Unit = { _, _, _ -> },
	putNotebook : (String, String, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
	closeSheet : () -> Unit = {},
) {
	when (bottomSheetType) {
		MainBottomSheetType.Menu -> MenuBottomSheet(closeSheet = closeSheet)
		MainBottomSheetType.Filter -> FilterBottomSheet()
		MainBottomSheetType.Bucket -> BucketBottomSheet(putBucket = putBucket)
		MainBottomSheetType.Notebook -> NotebookBottomSheet(
			putNotebook = putNotebook,
			closeSheet = closeSheet,
		)
		MainBottomSheetType.Sync -> SyncBottomSheet(
			onClickSyncNow = onClickSyncNow,
			onClickForceSync = onClickForceSync,
		)
	}
}
