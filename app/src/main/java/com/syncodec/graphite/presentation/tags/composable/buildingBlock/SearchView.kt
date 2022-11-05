package com.syncodec.graphite.presentation.tags.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Composable
fun SearchView(
	text: String,
	placeholder: String,
	isTagPresent: Boolean,
	onAddTag: () -> Unit,
	onValueChanged: (String) -> Unit,
) {

	val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.71f)
	val contentColor = MaterialTheme.colorScheme.onSurface

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.background(containerColor, RoundedCornerShape(12.dp))
			.clip(RoundedCornerShape(12.dp))
	) {
		Spacer(modifier = Modifier.width(12.dp))

		BasicTextField(
			value = text,
			onValueChange = { onValueChanged(it) },
			singleLine = true,
			keyboardOptions = KeyboardOptions.Default,
			keyboardActions = KeyboardActions.Default,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.weight(1f),
			visualTransformation = { text ->
				TransformedText(
					AnnotatedString(
						text.toString(),
						SpanStyle(color = contentColor, fontWeight = FontWeight.Bold)
					),
					OffsetMapping.Identity
				)
			},
			decorationBox = { innerTextField ->
				AnimatedVisibility(
					visible = text.isEmpty(),
					enter = fadeIn(tween(300)),
					exit = fadeOut(tween(300))
				) {
					Crossfade(targetState = placeholder) {
						Text(
							text = it,
							style = MaterialTheme.typography.bodyMedium,
							color = contentColor.copy(alpha = 0.31f),
						)
					}
				}
				innerTextField()
			}
		)

		AnimatedVisibility(visible = isTagPresent) {
			IconButton(
				onClick = onAddTag
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_add),
					contentDescription = "Add tag",
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
					modifier = Modifier
				)
			}
		}
	}
}
