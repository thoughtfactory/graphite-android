package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.SyncBottomSheet
import com.syncodec.graphite.service.syncInator.SyncInatorService


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
	syncStatus : SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	testConnectionResponse : DBox.Companion.TestConnectionResponse? = null,
	putBucket : (String?, String?, BucketType) -> Unit = { _, _, _ -> },
	putNotebook : (String, String, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
	onClickTestConnection : () -> Unit = {},
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
	closeSheet : () -> Unit = {},
) {
//	when (bottomSheetType) {
////		MainBottomSheetType.Menu -> MenuBottomSheet(closeSheet = closeSheet)
//		MainBottomSheetType.Filter -> FilterBottomSheet()
//		MainBottomSheetType.Bucket -> BucketBottomSheet(
//			putBucket = putBucket,
//			closeSheet = closeSheet,
//		)
//		MainBottomSheetType.Notebook -> NotebookBottomSheet(
//			putNotebook = putNotebook,
//			closeSheet = closeSheet,
//		)
//		MainBottomSheetType.Sync -> SyncBottomSheet(
//			syncStatus = syncStatus,
//			testConnectionResponse = testConnectionResponse,
//			onClickTestConnection = onClickTestConnection,
//			onClickSyncNow = onClickSyncNow,
//			onClickForceSync = onClickForceSync,
//			closeSheet = closeSheet,
//		)
//	}
}
