package com.syncodec.graphite.custom

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.R


@Composable
fun LargeTextField(
	modifier: Modifier = Modifier,
	text: String,
	placeholder: String,
	keyboardOptions: KeyboardOptions? = null,
	keyboardActions: KeyboardActions? = null,
	isFocused: Boolean,
	onFocusChanged: (Boolean) -> Unit,
	onValueChanged: (String) -> Unit
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
		modifier = modifier
			.height(48.dp)
			.clip(RoundedCornerShape(12.dp))
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
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 0.dp, 0.dp, 0.dp),
				) {
					Box(
						modifier = Modifier,
						contentAlignment = Alignment.CenterStart,
					) {
						Crossfade(targetState = text.isEmpty()) {
							if (it) {
								Text(
									text = placeholder,
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.surface.tone(
										isSystemInDarkTheme(),
										5
									),
									fontWeight = FontWeight.Bold
								)
							}
						}
						innerTextField()
					}
					IconButton(onClick = { onValueChanged("") }) {
						Icon(
							painter = painterResource(id = R.drawable.ic_close),
							contentDescription = "Clear search text",
							tint = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 5),
							modifier = Modifier.requiredSize(16.dp)
						)
					}
				}
			}
		}
	)
}
