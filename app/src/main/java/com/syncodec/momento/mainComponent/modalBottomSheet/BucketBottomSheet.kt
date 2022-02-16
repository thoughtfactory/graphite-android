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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import compose.icons.tablericons.Bucket


data class BucketButtonData(
	val title: String,
	val subtitle: String,
	val bucketItemType: BucketItemType.Type?,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketBottomSheet(
	onCreate: (String, BucketItemType.Type) -> Unit = { _, _ -> }
) {
	var selectedBucketType by remember { mutableStateOf<BucketItemType.Type?>(null) }
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
			bucketItemType = BucketItemType.Type.TODO,
			highlight = selectedBucketType == BucketItemType.Type.TODO,
		) {
			selectedBucketType = BucketItemType.Type.TODO
		},
		BucketButtonData(
			title = "Books",
			subtitle = "A little fiction here, and a little fantasy there",
			bucketItemType = BucketItemType.Type.BOOKS,
			highlight = selectedBucketType == BucketItemType.Type.BOOKS,
		) {
			selectedBucketType = BucketItemType.Type.BOOKS
		},
		BucketButtonData(
			title = "Movies",
			subtitle = "Ah! Don't have enough time",
			bucketItemType = BucketItemType.Type.MOVIES,
			highlight = selectedBucketType == BucketItemType.Type.MOVIES,
		) {
			selectedBucketType = BucketItemType.Type.MOVIES
		},
		BucketButtonData(
			title = "TV Shows",
			subtitle = "Aren't those characters real!?",
			bucketItemType = BucketItemType.Type.TVSHOWS,
			highlight = selectedBucketType == BucketItemType.Type.TVSHOWS,
		) {
			selectedBucketType = BucketItemType.Type.TVSHOWS
		},
		BucketButtonData(
			title = "Media",
			subtitle = "Gotta keep them safe",
			bucketItemType = BucketItemType.Type.MEDIA,
			highlight = selectedBucketType == BucketItemType.Type.MEDIA,
		) {
			selectedBucketType = BucketItemType.Type.MEDIA
		},
		BucketButtonData(
			title = "Links",
			bucketItemType = BucketItemType.Type.LINKS,
			highlight = selectedBucketType == BucketItemType.Type.LINKS,
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

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "And name it",
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)

		Spacer(modifier = Modifier.height(8.dp))

		BasicTextField(
			value = bucketNameText,
			onValueChange = { bucketNameText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium.copy(
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.Bold
			),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.clip(RoundedCornerShape(12.dp))
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
			decorationBox = { innerTextField ->
				Card(
					modifier = Modifier
						.fillMaxWidth(),
					backgroundColor = Color.Transparent,
					elevation = 0.dp,
					shape = RoundedCornerShape(12.dp),
					border = BorderStroke(2.dp, if (isBucketNameTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray)
				) {
					Box(
						contentAlignment = Alignment.CenterStart,
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp, 0.dp)
					) {
						if (bucketNameText.isEmpty()) {
							Text(
								"Umm... Let me think...",
								style = MaterialTheme.typography.bodyMedium,
								color = Color.LightGray,
								fontWeight = FontWeight.Bold
							)
						}
						innerTextField()
					}
				}
			}
		)

		Spacer(modifier = Modifier.height(8.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
				.clip(RoundedCornerShape(12.dp))
				.background(createButtonColors.containerColor(enabled = selectedBucketType != null && bucketNameText.isNotBlank()).value)
				.clickable(selectedBucketType != null && bucketNameText.isNotBlank()) {
					onCreate(bucketNameText, selectedBucketType!!)
					bucketNameText = ""
					selectedBucketType = null
				},
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Create",
				style = MaterialTheme.typography.titleMedium,
				color = createButtonColors.contentColor(enabled = selectedBucketType != null && bucketNameText.isNotBlank()).value,
				textAlign = TextAlign.Center,
				lineHeight = 0.sp,
				maxLines = 1,
			)
		}

		Spacer(modifier = Modifier.height(24.dp))
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
					imageVector = ResourceMap.bucketTypeToIcon[bucketButtonData.bucketItemType]!!,
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
			maxLines = 3,
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp)
				.alpha(0.47f)
		)
	}
}
