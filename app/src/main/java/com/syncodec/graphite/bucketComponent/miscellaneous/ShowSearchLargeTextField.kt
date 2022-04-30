package com.syncodec.graphite.bucketComponent.miscellaneous

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.R


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowSearchLargeTextField(
	text: String,
	placeholder: String,
	dataType: BucketActivity.DataType,
	keyboardOptions: KeyboardOptions? = null,
	keyboardActions: KeyboardActions? = null,
	isFocused: Boolean,
	onFocusChanged: (Boolean) -> Unit,
	onValueChanged: (String) -> Unit,
	onAction: (BucketActivity.Action) -> Unit
) {
	BasicTextField(
		value = text,
		onValueChange = { onValueChanged(it) },
		singleLine = true,
		keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
		keyboardActions = keyboardActions ?: KeyboardActions.Default,
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
				if (text.isEmpty() && !isFocused) {
					Color.LightGray.copy(alpha = 0.13f)
				} else {
					MaterialTheme.colorScheme.background
				}
			)
			.onFocusChanged { onFocusChanged(it.isFocused) },
		decorationBox = { innerTextField ->
			Surface(
				border = BorderStroke(
					2.dp, if (isFocused) MaterialTheme.colorScheme.primary
					else MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 5)
				),
				shape = RoundedCornerShape(12.dp),
				modifier = Modifier.fillMaxWidth()
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Spacer(modifier = Modifier.width(12.dp))
					Box(
						modifier = Modifier.weight(1f)
					) {
						Crossfade(targetState = text.isEmpty()) {
							if (it) {
								Text(
									text = placeholder,
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 5),
									fontWeight = FontWeight.Bold
								)
							}
						}
						innerTextField()
					}
					DataTypeCard(dataType = dataType) { onAction(it) }
				}
			}
		}
	)
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
			.fillMaxHeight()
			.clickable { onClick(BucketActivity.Action.DATA_TYPE_SELECT) },
	) {
		Spacer(modifier = Modifier.width(12.dp))
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
