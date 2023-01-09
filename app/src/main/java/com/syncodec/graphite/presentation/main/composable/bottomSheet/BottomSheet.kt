package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


enum class MainBottomSheetType {
	MENU,
	FILTER,
	BUCKET,
	NOTEBOOK,
	SYNC
}

@Composable
fun ColumnScope.SheetLayout(
	bottomSheetType: MainBottomSheetType,
	putNotebook: (String, String, Color?, Bitmap?) -> Unit,
	onClickSyncNow: () -> Unit,
	onClickForceSync: () -> Unit
) {
	when (bottomSheetType) {
		MainBottomSheetType.MENU -> MenuBottomSheet()
		MainBottomSheetType.FILTER -> FilterBottomSheet()
		MainBottomSheetType.BUCKET -> BucketBottomSheet()
		MainBottomSheetType.NOTEBOOK -> NotebookBottomSheet(putNotebook)
		MainBottomSheetType.SYNC -> SyncBottomSheet(
			onClickSyncNow = onClickSyncNow,
			onClickForceSync = onClickForceSync,
		)
	}
}
