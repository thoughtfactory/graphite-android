package com.syncodec.graphite.bucketComponent.miscellaneous

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.BucketActivity


@Composable
fun ShowSearchLargeTextField(
	text: String,
	placeholder: String,
	dataType: BucketActivity.DataType,
	isFocused: Boolean,
	onFocusChanged: (Boolean) -> Unit,
	keyboardOptions: KeyboardOptions? = null,
	keyboardActions: KeyboardActions? = null,
	onValueChanged: (String) -> Unit,
	onAction: (BucketActivity.Action) -> Unit
) {
	val textColor = MaterialTheme.colorScheme.onBackground

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))

		BasicTextField(
			value = text,
			onValueChange = { onValueChanged(it) },
			singleLine = true,
			keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
			keyboardActions = keyboardActions ?:  KeyboardActions.Default,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.weight(1f)
				.onFocusChanged { onFocusChanged(it.isFocused) },
			visualTransformation = { text ->
				TransformedText(
					AnnotatedString(
						text.toString(),
						SpanStyle(color = textColor, fontWeight = FontWeight.Bold)
					),
					OffsetMapping.Identity
				)
			},
			decorationBox = { innerTextField ->
				Crossfade(targetState = text.isEmpty() && !isFocused) {
					if (it) {
						Text(
							text = placeholder,
							style = MaterialTheme.typography.bodyMedium,
							color = textColor.copy(alpha = 0.31f),
							fontWeight = FontWeight.Bold
						)
					} else {
						innerTextField()
					}
				}
			}
		)

		IconButton(onClick = { onValueChanged("") }) {
			Icon(
				painter = painterResource(id = R.drawable.ic_close),
				contentDescription = "Clear text",
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				modifier = Modifier
			)
		}

		DataTypeCard(dataType = dataType) { onAction(it) }
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DataTypeCard(
	dataType: BucketActivity.DataType,
	onClick: (BucketActivity.Action) -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.clickable { onClick(BucketActivity.Action.DATA_TYPE_SELECT) },
	) {
		Spacer(modifier = Modifier.width(6.dp))
		AnimatedContent(targetState = dataType) {
			when (it) {
				BucketActivity.DataType.TV -> Icon(
					painter = painterResource(id = R.drawable.ic_tv),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(20.dp)
				)
				BucketActivity.DataType.MOVIE -> Icon(
					painter = painterResource(R.drawable.ic_show),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(20.dp)
				)
			}
		}
		Spacer(modifier = Modifier.width(6.dp))
		AnimatedContent(targetState = dataType) {
			when (it) {
				BucketActivity.DataType.TV -> Text(
					text = "Show",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				)
				BucketActivity.DataType.MOVIE -> Text(
					text = "Movie",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				)
			}
		}
		Icon(
			painter = painterResource(id = com.syncodec.graphite.R.drawable.ic_caret_down),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(16.dp)
		)
		Spacer(modifier = Modifier.width(12.dp))
	}
}
