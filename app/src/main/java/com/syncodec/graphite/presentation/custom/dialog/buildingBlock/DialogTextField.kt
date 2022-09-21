package com.syncodec.graphite.presentation.custom.dialog.buildingBlock

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp


@Composable
fun DialogTextField(
	modifier: Modifier = Modifier,
	text: String?,
	label: String,
	placeholder: String,
	maxLines: Int = 1,
	leadingIcon: @Composable (() -> Unit)? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	containerColor: Color = MaterialTheme.colorScheme.background,
	contentColor: Color = MaterialTheme.colorScheme.onBackground,
	focusRequester: FocusRequester = remember { FocusRequester() },
	onKeyboardAction: () -> Unit = { },
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
	keyboardActions: KeyboardActions = KeyboardActions(onNext = { onKeyboardAction() }),
	onTextChange: (String?) -> Unit,
) {

	val spanStyle = SpanStyle(
		color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
		fontSize = MaterialTheme.typography.titleMedium.fontSize,
		letterSpacing = MaterialTheme.typography.titleMedium.letterSpacing,
		fontWeight = FontWeight.Bold,
		fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
	)

	val paragraphStyle = ParagraphStyle(
		lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
	)

	BasicTextField(
		value = text ?: "",
		onValueChange = onTextChange,
		keyboardOptions = keyboardOptions,
		keyboardActions = keyboardActions,
		modifier = modifier.fillMaxWidth(),
		maxLines = maxLines,
		cursorBrush = SolidColor(contentColor),
		visualTransformation = { _text ->
			TransformedText(
				AnnotatedString(
					text = _text.toString(),
					spanStyle = spanStyle,
					paragraphStyle = paragraphStyle
				),
				OffsetMapping.Identity
			)
		}
	) { innerTextField ->
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(containerColor.copy(alpha = 0.71f), RoundedCornerShape(24.dp))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 12.dp, 0.dp, 12.dp)
			) {
				Text(
					text = label,
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor.copy(alpha = 0.31f),
					modifier = Modifier.padding(start = if (leadingIcon == null) 0.dp else 40.dp)
				)

				Row(
					verticalAlignment = Alignment.Top,
					modifier = Modifier.fillMaxWidth()
				) {
					leadingIcon?.let { icon -> icon() }

					Box(
						modifier = Modifier
							.weight(1f)
							.padding(top = 12.dp)
					) {
						Crossfade(targetState = text.isNullOrBlank()) {
							if (it) {
								Text(
									text = placeholder,
									style = MaterialTheme.typography.titleMedium,
									color = contentColor.copy(alpha = 0.47f),
									fontWeight = FontWeight.Bold,
									modifier = Modifier
								)
							}
						}

						innerTextField()
					}

					trailingIcon?.let { icon -> icon() }
				}
			}
		}
	}
}
