package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	onDismissRequest: () -> Unit = { },
	putBucket: (String?, String?, BucketType) -> Unit = { _, _, _ -> },  //  Title, description, bucketType
) {
	val keyboardController = LocalSoftwareKeyboardController.current

	var selectedBucketType by remember { mutableStateOf<BucketType?>(null) }
	var bucketTitleText by rememberSaveable { mutableStateOf("") }
	var bucketDescriptionText by rememberSaveable { mutableStateOf("") }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		Text(
			text = stringResource(id = R.string.bucket_new_list),
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(start = 24.dp)
		)

		Spacer(modifier = Modifier.height(12.dp))

		BucketSelector(
			selectedBucketType = selectedBucketType,
			onClickBucketSelector = { selectedBucketType = it }
		)

		Spacer(modifier = Modifier.height(12.dp))

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
		) {
			OutlinedTextField(
				value = bucketTitleText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { bucketTitleText = it },
				label = { Text(text = stringResource(id = R.string.title)) },
				placeholder = { Text(text = stringResource(id = R.string.bucket_title_placeholder)) },
				trailingIcon = { CancelButton { bucketTitleText = "" } },
				maxLines = 1,
				singleLine = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(4.dp))

			OutlinedTextField(
				value = bucketDescriptionText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { bucketDescriptionText = it },
				label = { Text(text = stringResource(id = R.string.description)) },
				placeholder = { Text(text = stringResource(id = R.string.bucket_description_placeholder)) },
				trailingIcon = { CancelButton { bucketDescriptionText = "" } },
				maxLines = 1,
				singleLine = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(8.dp))

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
					if (selectedBucketType != null) putBucket(bucketTitleText, bucketDescriptionText, selectedBucketType!!)

					bucketTitleText = ""
					bucketDescriptionText = ""
					selectedBucketType = null
					keyboardController?.hide()
					onDismissRequest()
				},
			) {
				Text(text = stringResource(id = R.string.save))
			}
		}

		Spacer(modifier = Modifier.height(24.dp))
	}
}

@Preview
@Composable
private fun BucketSelector(
	selectedBucketType: BucketType? = null,
	onClickBucketSelector: (BucketType) -> Unit = {},
) {
	val context = LocalContext.current

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
	) {
		Spacer(modifier = Modifier.width(24.dp))
		BucketSelectorItem(
			title = stringResource(id = R.string.bucket_type_todo),
			icon = painterResource(id = R.drawable.ic_fa_bucket_todo),
			description = stringResource(id = R.string.bucket_description_todo),
			isSelected = selectedBucketType == BucketType.TODO,
			onClick = { onClickBucketSelector(BucketType.TODO) },
		)

		Spacer(modifier = Modifier.width(12.dp))

		BucketSelectorItem(
			title = stringResource(id = R.string.bucket_type_book),
			icon = painterResource(id = R.drawable.ic_fa_bucket_book),
			description = stringResource(id = R.string.bucket_description_book),
			isSelected = selectedBucketType == BucketType.BOOK,
			onClick = { onClickBucketSelector(BucketType.BOOK) },
		)

		Spacer(modifier = Modifier.width(12.dp))

		BucketSelectorItem(
			title = stringResource(id = R.string.bucket_type_show),
			icon = painterResource(id = R.drawable.ic_fa_bucket_show),
			description = stringResource(id = R.string.bucket_description_show),
			isSelected = selectedBucketType == BucketType.SHOW,
			onClick = { onClickBucketSelector(BucketType.SHOW) },
		)

		Spacer(modifier = Modifier.width(12.dp))

		BucketSelectorItem(
			title = stringResource(id = R.string.bucket_type_link),
			icon = painterResource(id = R.drawable.ic_fa_bucket_link),
			description = stringResource(id = R.string.bucket_description_link),
			isSelected = selectedBucketType == BucketType.LINK,
			onClick = { onClickBucketSelector(BucketType.LINK) },
		)

		Spacer(modifier = Modifier.width(12.dp))

		BucketSelectorItem(
			title = stringResource(id = R.string.stay_tuned),
			icon = painterResource(id = R.drawable.ic_fa_circle_dot_duotone),
			description = stringResource(id = R.string.bucket_description_coming_soon),
			isSelected = false,
			onClick = { Toast.makeText(context, context.getText(R.string.toast_suggest_bucket_list), Toast.LENGTH_SHORT).show() },
		)

		Spacer(modifier = Modifier.width(24.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketSelectorItem(
	title: String = stringResource(id = R.string.bucket_type_todo),
	icon: Painter = painterResource(id = R.drawable.ic_fa_bucket_todo),
	description: String = stringResource(id = R.string.bucket_description_todo),
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
		animationSpec = tween(600),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
		animationSpec = tween(600),
		label = "contentColor_animation"
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.requiredWidth(160.dp)
	) {
		Card(
			shape = MaterialTheme.shapes.large,
			colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
			modifier = Modifier
				.width(160.dp)
				.height(84.dp)
				.focusable(true)
				.clip(MaterialTheme.shapes.medium),
			onClick = onClick
		) {
			Column(
				horizontalAlignment = Alignment.Start,
				modifier = Modifier
					.padding(12.dp)
					.fillMaxSize()
			) {
				Icon(
					painter = icon,
					contentDescription = title,
					modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
				)

				Spacer(modifier = Modifier.weight(1f))

				Text(
					text = title,
					style = MaterialTheme.typography.bodyMedium,
				)
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = description,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp, 0.dp)
		)
	}
}
