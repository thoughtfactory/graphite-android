package com.syncodec.graphite.bucketComponent.miscellaneous

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.custom.button.LargeButton
import com.syncodec.graphite.database.bucketItem.BucketItemType

@Composable
fun AddNewBucketItemButton(
	bucketItemType: BucketItemType,
	isSelected: Boolean,
	selectedBucketItemList: List<String>,
	onAction: (BucketActivity.Action) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(0.dp, 0.dp, 0.dp, 24.dp),
		contentAlignment = Alignment.BottomCenter
	) {
		Crossfade(targetState = isSelected) {
			if (it) {
				LargeButton(
					text = if (selectedBucketItemList.isEmpty()) "Select item to delete" else "Delete ${if (selectedBucketItemList.size == 1) "1 item" else "${selectedBucketItemList.size} items"}?",
					enabled = true,
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp, 0.dp)
				) { onAction(BucketActivity.Action.DELETE_ITEM) }
			} else {
				LargeButton(
					text = when (bucketItemType) {
						BucketItemType.TODO -> "Add a new Task"
						BucketItemType.BOOK -> "A new story?"
						BucketItemType.SHOW -> "What did you watch?"
						BucketItemType.LINK -> "Save a link"
					},
					enabled = true,
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp, 0.dp)
				) { onAction(BucketActivity.Action.OPEN_ADD_SHEET) }
			}
		}
	}
}
