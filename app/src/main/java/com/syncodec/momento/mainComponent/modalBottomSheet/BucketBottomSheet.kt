package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.konstant.ResourceMap
import compose.icons.TablerIcons
import compose.icons.tablericons.Bucket


data class BucketButtonData(
	val subtitle: String,
	val bucketItemType: BucketItemType?,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketBottomSheet(
	onCreate: (String, BucketItemType) -> Unit = { _, _ -> }
) {
	var selectedBucketType by remember { mutableStateOf<BucketItemType?>(null) }
	var bucketNameText by rememberSaveable { mutableStateOf("") }
	var isBucketNameTextFocused by remember { mutableStateOf(false) }

	val containerColor by animateColorAsState(
		targetValue = if (selectedBucketType != null && bucketNameText.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer else Color.LightGray,
		animationSpec = tween(
			durationMillis = 600
		)
	)
	val contentColor by animateColorAsState(
		targetValue = if (selectedBucketType != null && bucketNameText.isNotBlank()) MaterialTheme.colorScheme.primaryContainer else Color.DarkGray,
		animationSpec = tween(
			durationMillis = 600
		)
	)

	val bucketButtonDataList: List<BucketButtonData> = listOf(
		BucketButtonData(
			subtitle = "Have any pending tasks?",
			bucketItemType = BucketItemType.TODO,
			highlight = selectedBucketType == BucketItemType.TODO,
		) {
			selectedBucketType = BucketItemType.TODO
		},
		BucketButtonData(
			subtitle = "A little fiction here, and a little fantasy there",
			bucketItemType = BucketItemType.BOOKS,
			highlight = selectedBucketType == BucketItemType.BOOKS,
		) {
			selectedBucketType = BucketItemType.BOOKS
		},
		BucketButtonData(
			subtitle = "Aren't those characters real!?",
			bucketItemType = BucketItemType.SHOWS,
			highlight = selectedBucketType == BucketItemType.SHOWS,
		) {
			selectedBucketType = BucketItemType.SHOWS
		},
		BucketButtonData(
			subtitle = "Gotta keep them safe",
			bucketItemType = BucketItemType.MEDIA,
			highlight = selectedBucketType == BucketItemType.MEDIA,
		) {
			selectedBucketType = BucketItemType.MEDIA
		},
		BucketButtonData(
			bucketItemType = BucketItemType.LINKS,
			highlight = selectedBucketType == BucketItemType.LINKS,
			subtitle = "Those might be helpful someday",
		) {}
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.background),
	) {

		BottomSheetStrip()

		BottomSheetHeader(title = "Pick a bucket", imageVector = TablerIcons.Bucket)

		Row(
			modifier = Modifier.horizontalScroll(rememberScrollState())
		) {
			Spacer(modifier = Modifier.width(16.dp))
			bucketButtonDataList.forEach { BucketButton(it) }
			Spacer(modifier = Modifier.width(16.dp))
		}

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "And name it",
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = bucketNameText,
			placeholder = "Umm... Let me think...",
			isFocused = isBucketNameTextFocused,
			onFocusChanged = { isBucketNameTextFocused = it },
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			bucketNameText = it
		}

		Spacer(modifier = Modifier.height(8.dp))

		LargeButton(
			text = "Create",
			containerColor = containerColor,
			contentColor = contentColor,
			isElevated = selectedBucketType != null && bucketNameText.isNotBlank(),
			isClickable = selectedBucketType != null && bucketNameText.isNotBlank(),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			onCreate(bucketNameText, selectedBucketType!!)
			bucketNameText = ""
			selectedBucketType = null
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun BucketButton(
	bucketButtonData: BucketButtonData
) {
	val containerColor by animateColorAsState(
		targetValue = if (bucketButtonData.highlight) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
		animationSpec = tween(600)
	)
	val contentColor by animateColorAsState(
		targetValue = if (bucketButtonData.highlight) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
		animationSpec = tween(600)
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.requiredWidth(160.dp)
	) {
		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			backgroundColor = containerColor,
			border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
			modifier = Modifier
				.width(160.dp)
				.height(96.dp)
				.padding(6.dp)
				.focusable(true)
				.clip(RoundedCornerShape(12.dp))
				.clickable { bucketButtonData.onClick() },
		) {
			Column(
				modifier = Modifier
					.padding(12.dp)
					.fillMaxWidth()
					.fillMaxHeight(),
				verticalArrangement = Arrangement.SpaceBetween,
				horizontalAlignment = Alignment.Start
			) {
				Icon(
					imageVector = ResourceMap.bucketTypeToIcon[bucketButtonData.bucketItemType]!!,
					contentDescription = null,
					tint = contentColor,
					modifier = Modifier
						.size(24.dp)
						.alpha(0.8f)
				)

				Text(
					text = ResourceMap.BucketItemNameMap[bucketButtonData.bucketItemType]!!,
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor
				)
			}
		}

		Text(
			text = bucketButtonData.subtitle,
			style = MaterialTheme.typography.bodySmall,
			maxLines = 3,
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp, 0.dp)
				.alpha(0.47f)
		)
	}
}
