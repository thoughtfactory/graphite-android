package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.ResourceMap
import compose.icons.TablerIcons
import compose.icons.tablericons.*


data class BucketButtonData(
	val title: String,
	val subtitle: String,
	val imageVector: ImageVector,
	val bucketItemType: BucketItemType?,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketBottomSheet() {
	val selectedBucketItemType = remember { mutableStateOf<BucketItemType?>(null) }
	var bucketNameText by rememberSaveable { mutableStateOf("") }
	var isBucketNameTextFocused by remember { mutableStateOf(false) }

	val createButtonColors = ButtonDefaults.buttonColors(
		contentColor = MaterialTheme.colorScheme.primaryContainer,
		disabledContentColor = Color.LightGray,
		containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
		disabledContainerColor = Color.LightGray.copy(alpha = 0.13f)
	)

	val bucketButtonDataList: List<BucketButtonData> = listOf(
		BucketButtonData(
			title = "To Do",
			subtitle = "Have any pending tasks?",
			bucketItemType = BucketItemType.TODO,
			highlight = selectedBucketItemType.value == BucketItemType.TODO,
			imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.TODO]!!
		) {
			selectedBucketItemType.value = BucketItemType.TODO
		},
		BucketButtonData(
			title = "Books",
			subtitle = "A little fiction here, and a little fantasy there",
			bucketItemType = BucketItemType.BOOKS,
			highlight = selectedBucketItemType.value == BucketItemType.BOOKS,
			imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.BOOKS]!!
		) {
			selectedBucketItemType.value = BucketItemType.BOOKS
		},
		BucketButtonData(
			title = "Movies",
			subtitle = "Ah! Don't have enough time",
			bucketItemType = BucketItemType.MOVIES,
			highlight = selectedBucketItemType.value == BucketItemType.MOVIES,
			imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.MOVIES]!!
		) {
			selectedBucketItemType.value = BucketItemType.MOVIES
		},
		BucketButtonData(
			title = "TV Shows",
			subtitle = "Aren't those characters real!?",
			bucketItemType = BucketItemType.TVSHOWS,
			highlight = selectedBucketItemType.value == BucketItemType.TVSHOWS,
			imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.MEDIA]!!
		) {
			selectedBucketItemType.value = BucketItemType.TVSHOWS
		},
		BucketButtonData(
			title = "Media",
			subtitle = "Gotta keep them safe",
			bucketItemType = BucketItemType.MEDIA,
			highlight = selectedBucketItemType.value == BucketItemType.MEDIA,
			imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.MEDIA]!!
		) {
			selectedBucketItemType.value = BucketItemType.MEDIA
		},
		BucketButtonData(
			title = "Links",
			bucketItemType = BucketItemType.LINKS,
			highlight = selectedBucketItemType.value == BucketItemType.LINKS,
			subtitle = "Those might be helpful someday",
			imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.LINKS]!!
		) {
			selectedBucketItemType.value = BucketItemType.LINKS
		}
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
			Box(
				modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp)
			) {
				BucketButton(bucketButtonDataList[0])
			}
			BucketButton(bucketButtonDataList[1])
			BucketButton(bucketButtonDataList[2])
			BucketButton(bucketButtonDataList[3])
			BucketButton(bucketButtonDataList[4])
			Box(
				modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp)
			) {
				BucketButton(bucketButtonDataList[5])
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "And name it",
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)

		Spacer(modifier = Modifier.height(12.dp))

		BasicTextField(
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.background(
					if (bucketNameText.isEmpty() && !isBucketNameTextFocused) {
						Color.LightGray.copy(alpha = 0.13f)
					} else {
						MaterialTheme.colorScheme.background
					}
				)
				.onFocusChanged { focusState ->
					isBucketNameTextFocused = focusState.isFocused
				},
			value = bucketNameText,
			onValueChange = { bucketNameText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			decorationBox = { innerTextField ->
				Card(
					backgroundColor = Color.Transparent,
					shape = RoundedCornerShape(2.dp),
					border = BorderStroke(1.dp, if (isBucketNameTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray),
					elevation = 0.dp
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(16.dp, 0.dp)
					) {
						Box {
							if (bucketNameText.isEmpty()) {
								Text(
									"Umm... Let me think...",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.LightGray
								)
							}
							innerTextField()
						}
					}
				}
			}
		)

		Spacer(modifier = Modifier.height(12.dp))

		Card(
			elevation = 0.dp,
			backgroundColor = createButtonColors.containerColor(enabled = selectedBucketItemType.value != null && bucketNameText.isNotBlank()).value,
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
				.clickable { },
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center,
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
			) {
				Text(
					text = "Create",
					style = MaterialTheme.typography.titleMedium,
					color = createButtonColors.contentColor(enabled = bucketNameText.isNotBlank()).value,
					textAlign = TextAlign.Center,
					lineHeight = 0.sp,
					maxLines = 1,
				)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun BucketButton(
	bucketButtonData: BucketButtonData
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.requiredWidth(160.dp)
	) {
		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(8.dp),
			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
			border = BorderStroke(2.dp, if (bucketButtonData.highlight) MaterialTheme.colorScheme.primary else Color.Transparent),
			modifier = Modifier
				.width(160.dp)
				.height(96.dp)
				.padding(6.dp)
				.focusable(true)
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
					imageVector = bucketButtonData.imageVector,
					contentDescription = null,
					tint = if (bucketButtonData.highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
						.size(24.dp)
						.alpha(0.8f)
				)

				Text(
					text = bucketButtonData.title,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.ExtraBold,
					color = if (bucketButtonData.highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
				)
			}
		}

		Text(
			text = bucketButtonData.subtitle,
			style = MaterialTheme.typography.bodySmall,
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp)
				.alpha(0.47f)
		)
	}
}
