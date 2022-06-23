package com.syncodec.graphite.mainComponent.modalBottomSheet

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LargeTextField
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.LargeButton
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.konstant.ResourceMap


private data class BucketButtonData(
	val subtitle: String,
	val bucketItemType: BucketItemType?,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BucketBottomSheet(
	onCreate: (String, BucketItemType) -> Unit
) {
	val context = LocalContext.current
	val keyboardController = LocalSoftwareKeyboardController.current
	var selectedBucketType by remember { mutableStateOf<BucketItemType?>(null) }
	var bucketNameText by rememberSaveable { mutableStateOf("") }
	var isBucketNameTextFocused by remember { mutableStateOf(false) }

	val bucketButtonDataList: List<BucketButtonData> = listOf(
		BucketButtonData(
			subtitle = "Have any pending tasks?",
			bucketItemType = BucketItemType.TODO,
			highlight = selectedBucketType == BucketItemType.TODO,
		) { selectedBucketType = BucketItemType.TODO },
		BucketButtonData(
			subtitle = "A little fiction here, and a little fantasy there",
			bucketItemType = BucketItemType.BOOK,
			highlight = selectedBucketType == BucketItemType.BOOK,
		) { selectedBucketType = BucketItemType.BOOK },
		BucketButtonData(
			subtitle = "Aren't those characters real!?",
			bucketItemType = BucketItemType.SHOW,
			highlight = selectedBucketType == BucketItemType.SHOW,
		) { selectedBucketType = BucketItemType.SHOW },
		BucketButtonData(
			subtitle = "Maybe I will visit this someday",
			bucketItemType = BucketItemType.LINK,
			highlight = selectedBucketType == BucketItemType.LINK,
		) { selectedBucketType = BucketItemType.LINK },
		BucketButtonData(
			subtitle = "More lists coming soon...",
			bucketItemType = null,
			highlight = false,
		) {
			Toast.makeText(
				context,
				"More lists coming soon... Stay tuned...",
				Toast.LENGTH_SHORT
			).show()
		}
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {

			BottomSheetStrip()

			BottomSheetHeader(
				title = "Pick a bucket",
				icon = R.drawable.ic_bucket
			)

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
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			)

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp),
				text = bucketNameText,
				placeholder = "Umm... Let me think...",
				isFocused = isBucketNameTextFocused,
				onFocusChanged = { isBucketNameTextFocused = it },
			) { bucketNameText = it }

			Spacer(modifier = Modifier.height(8.dp))

			LargeButton(
				text = "Create",
				enabled = selectedBucketType != null && bucketNameText.isNotBlank(),
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) {
				onCreate(bucketNameText, selectedBucketType!!)
				bucketNameText = ""
				selectedBucketType = null
				keyboardController?.hide()
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

@Composable
private fun BucketButton(
	bucketButtonData: BucketButtonData
) {
	val containerColor by animateColorAsState(
		targetValue = if (bucketButtonData.highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
		animationSpec = tween(600)
	)
	val contentColor by animateColorAsState(
		targetValue = if (bucketButtonData.highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
		animationSpec = tween(600)
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.requiredWidth(160.dp)
	) {
		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			backgroundColor = containerColor,
			border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
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
					painter = painterResource(
						id = ResourceMap.bucketTypeToIcon[bucketButtonData.bucketItemType]
							?: R.drawable.ic_state
					),
					contentDescription = null,
					tint = contentColor,
					modifier = Modifier
						.size(24.dp)
						.alpha(0.8f)
				)

				Text(
					text = ResourceMap.BucketItemNameMap[bucketButtonData.bucketItemType]
						?: "Stay tuned...",
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
