package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextField
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.bucketItemNameMap
import com.syncodec.graphite.utils.bucketTypeToIcon


private data class BucketButtonData(
	val subtitle : String,
	val bucketType : BucketType?,
	val highlight : Boolean = false,
	val onClick : () -> Unit
)

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun BucketBottomSheet(
	putBucket : (String?, String?, BucketType) -> Unit = { _, _, _ -> },
) {
	val context = LocalContext.current

	val keyboardController = LocalSoftwareKeyboardController.current

	var selectedBucketType by remember { mutableStateOf<BucketType?>(null) }
	var bucketTitleText by rememberSaveable { mutableStateOf("") }
	var bucketDescriptionText by rememberSaveable { mutableStateOf("") }

	val bucketButtonDataList : List<BucketButtonData> = listOf(
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

	GenericBottomSheet(
		title = "New List",
		icon = R.drawable.ic_bucket
	) {
		Row(
			modifier = Modifier.horizontalScroll(rememberScrollState())
		) {
			bucketButtonDataList.forEach { BucketButton(it) }
		}

		Spacer(modifier = Modifier.height(12.dp))

		BottomSheetTextField(
			value = bucketTitleText,
			label = "Title",
			placeholder = "Name your bucket",
			onValueChange = { bucketTitleText = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetTextField(
			value = bucketDescriptionText,
			label = "Description",
			placeholder = "What is it about?",
			onValueChange = { bucketDescriptionText = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		Button(
			shape = MaterialTheme.shapes.medium,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
				disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
				disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
			),
			enabled = selectedBucketType != null && bucketTitleText.isNotBlank(),
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				if (selectedBucketType != null) putBucket(bucketTitleText, bucketDescriptionText, selectedBucketType !!)

				bucketTitleText = ""
				bucketDescriptionText = ""
				selectedBucketType = null
				keyboardController?.hide()
			},
		) {
			Text(text = "Create")
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BucketButton(
	bucketButtonData : BucketButtonData
) {
	val containerColor by animateColorAsState(
		targetValue = if (bucketButtonData.highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
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
			shape = MaterialTheme.shapes.medium,
			border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
			colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
			modifier = Modifier
				.width(160.dp)
				.height(84.dp)
				.padding(6.dp, 0.dp)
				.focusable(true)
				.clip(RoundedCornerShape(12.dp)),
			onClick = bucketButtonData.onClick
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
					modifier = Modifier.requiredSize(IconButtonSize)
				)

				Text(
					text = bucketItemNameMap[bucketButtonData.bucketType] ?: "Stay tuned...",
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor
				)
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = bucketButtonData.subtitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
			maxLines = 3,
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp, 0.dp)
		)
	}
}
