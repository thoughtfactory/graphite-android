package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyText
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.button.LargeButton
import com.syncodec.graphite.presentation.common.text.LargeTextField
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionPutBucket
import com.syncodec.graphite.utils.bucketItemNameMap
import com.syncodec.graphite.utils.bucketTypeToIcon


private data class BucketButtonData(
	val subtitle: String,
	val bucketType: BucketType?,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun BucketBottomSheet() {
	val context = LocalContext.current

	val closeSheet = LocalCompositionCloseBottomSheet.current

	val putBucket = LocalCompositionPutBucket.current

	val keyboardController = LocalSoftwareKeyboardController.current

	var selectedBucketType by remember { mutableStateOf<BucketType?>(null) }

	var bucketTitleText by rememberSaveable { mutableStateOf("") }
	var isBucketNameTitleFocused by remember { mutableStateOf(false) }
	val bucketTitleTextFocusRequester = remember { FocusRequester() }

	var bucketDescriptionText by rememberSaveable { mutableStateOf("") }
	var isBucketDescriptionTextFocused by remember { mutableStateOf(false) }

	val bucketButtonDataList: List<BucketButtonData> = listOf(
		BucketButtonData(
			subtitle = "Have any pending tasks?",
			bucketType = BucketType.TODO,
			highlight = selectedBucketType == BucketType.TODO,
		) { selectedBucketType = BucketType.TODO },
		BucketButtonData(
			subtitle = "A little fiction here, and a little fantasy there",
			bucketType = BucketType.BOOK,
			highlight = selectedBucketType == BucketType.BOOK,
		) { selectedBucketType = BucketType.BOOK },
		BucketButtonData(
			subtitle = "Aren't those characters real!?",
			bucketType = BucketType.SHOW,
			highlight = selectedBucketType == BucketType.SHOW,
		) { selectedBucketType = BucketType.SHOW },
		BucketButtonData(
			subtitle = "Maybe I will visit this someday",
			bucketType = BucketType.LINK,
			highlight = selectedBucketType == BucketType.LINK,
		) { selectedBucketType = BucketType.LINK },
		BucketButtonData(
			subtitle = "More lists coming soon...",
			bucketType = null,
			highlight = false,
		) {
			Toast.makeText(
				context,
				"More lists coming soon... Stay tuned...",
				Toast.LENGTH_SHORT
			).show()
		}
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
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

		BottomSheetKeyText(text = "And name it")

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = bucketTitleText,
			placeholder = "Umm... Let me think...",
			isFocused = isBucketNameTitleFocused,
			onFocusChanged = { isBucketNameTitleFocused = it },
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
		) { bucketTitleText = it }

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			text = bucketDescriptionText,
			placeholder = "What is it about?",
			isFocused = isBucketDescriptionTextFocused,
			focusRequester = bucketTitleTextFocusRequester,
			onFocusChanged = { isBucketDescriptionTextFocused = it },
		) { bucketDescriptionText = it }

		Spacer(modifier = Modifier.height(8.dp))

		LargeButton(
			text = "Create",
			enabled = selectedBucketType != null && bucketTitleText.isNotBlank(),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			if (selectedBucketType != null) putBucket(bucketTitleText, bucketDescriptionText, selectedBucketType!!)


			bucketTitleText = ""
			bucketDescriptionText = ""
			selectedBucketType = null
			bucketTitleTextFocusRequester.freeFocus()
			keyboardController?.hide()
			closeSheet()
		}

		Spacer(modifier = Modifier.height(32.dp))
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
		OutlinedCard(
			shape = RoundedCornerShape(12.dp),
			border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
			colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
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
						id = bucketTypeToIcon[bucketButtonData.bucketType] ?: R.drawable.ic_state
					),
					contentDescription = null,
					tint = contentColor,
					modifier = Modifier
						.size(24.dp)
						.alpha(0.71f)
				)

				Text(
					text = bucketItemNameMap[bucketButtonData.bucketType] ?: "Stay tuned...",
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor
				)
			}
		}

		Text(
			text = bucketButtonData.subtitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 3,
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp, 0.dp)
				.alpha(0.47f)
		)
	}
}
