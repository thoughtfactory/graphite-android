package com.syncodec.graphite.mainComponent.modalBottomSheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.syncodec.graphite.mainComponent.MainActivity
import com.syncodec.graphite.ui.theme.PremiumCompositionLocal


sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object BucketBottomSheet : BottomSheetType()
	object NotebookBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout(
	bottomSheetType: BottomSheetType,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val isPremium = PremiumCompositionLocal.current

	when (bottomSheetType) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet { onAction(it, null) }
		BottomSheetType.BucketBottomSheet -> BucketBottomSheet { bucketName, bucketType ->
			onAction(MainActivity.Action.NEW_BUCKET, Pair(Pair(bucketName, bucketType), isPremium))
		}
		BottomSheetType.NotebookBottomSheet -> NotebookBottomSheet { action, notebookDbEntry ->
			onAction(action, notebookDbEntry)
		}
	}
}
