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
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.database.bucketItem.BucketItemType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddNewBucketItemButton(
	bucketItemType: BucketItemType,
	isSelected: Boolean,
	selectedBucketItemList: List<String>,
	onClick: () -> Unit
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
				) {
					onClick()
				}
			} else {
				LargeButton(
					text = when (bucketItemType) {
						BucketItemType.TODO -> "Add New Task"
						BucketItemType.BOOKS -> "What did you read?"
						BucketItemType.SHOWS -> "A new story?"
						BucketItemType.MEDIA -> "Add media"
						BucketItemType.LINKS -> "Add link"
					},
					enabled = true,
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 0.dp)
				) {
					onClick()
				}
			}
		}
	}
}
