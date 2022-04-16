package com.syncodec.momento.bucketComponent.miscellaneous

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.database.bucketItem.BucketItemType

@OptIn(ExperimentalMaterialApi::class)
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
						.padding(12.dp, 0.dp)
				) { onAction(BucketActivity.Action.DELETE_ITEM) }
			} else {
				LargeButton(
					text = when (bucketItemType) {
						BucketItemType.TODO -> "Add a new Task"
						BucketItemType.BOOKS -> "A new story?"
						BucketItemType.SHOWS -> "What did you watch?"
					},
					enabled = true,
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 0.dp)
				) { onAction(BucketActivity.Action.OPEN_ADD_SHEET) }
			}
		}
	}
}
