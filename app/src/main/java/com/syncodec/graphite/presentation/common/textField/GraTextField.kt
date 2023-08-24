package com.syncodec.graphite.presentation.common.textField

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Preview
@Composable
fun GraTextField(
	value : String = "",
	placeholder : String = "",
	actionButtons : @Composable RowScope.() -> Unit = {},
	keyboardOptions : KeyboardOptions = KeyboardOptions.Default,
	keyboardActions : KeyboardActions = KeyboardActions.Default,
	colors: GraTextFieldColors = GraTextFieldDefaults.textFieldColors(),
	onValueChange : (String) -> Unit = {},
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth(),
	) {
		BasicTextField(
			value = value,
			textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textColor),
			cursorBrush = SolidColor(colors.cursorColor),
			singleLine = true,
			keyboardOptions = keyboardOptions,
			keyboardActions = keyboardActions,
			onValueChange = onValueChange,
			modifier = Modifier.weight(1f),
		) {
			Box(
				contentAlignment = Alignment.CenterStart,
				modifier = Modifier
					.fillMaxWidth()
					.height((IconButtonSize * 2) + 2.dp)
					.background(colors.containerColor, MaterialTheme.shapes.medium)
					.padding(12.dp, 0.dp),
			) {
				androidx.compose.animation.AnimatedVisibility(
					visible = value.isEmpty(),
					enter = fadeIn(tween(300)),
					exit = fadeOut(tween(300)),
				) {
					Text(
						text = placeholder,
						color = colors.placeholderColor,
						style = MaterialTheme.typography.bodyMedium,
					)
				}
				it()
			}
		}

		Spacer(modifier = Modifier.width(2.dp))
		GenericButton(
			icon = R.drawable.ic_close,
			colors = GenericButtonDefaults.genericButtonColorsOnSurface()
		) { onValueChange("") }
		actionButtons()
	}
}

@Immutable
data class GraTextFieldColors(
	val textColor : Color,
	val disabledTextColor : Color,
	val containerColor : Color,
	val cursorColor : Color,
	val errorCursorColor : Color,
	val selectionColors : TextSelectionColors,
	val focusedTrailingIconColor : Color,
	val unfocusedTrailingIconColor : Color,
	val disabledTrailingIconColor : Color,
	val errorTrailingIconColor : Color,
	val placeholderColor : Color,
	val disabledPlaceholderColor : Color,
)
object GraTextFieldDefaults {
	@Composable
	fun textFieldColors(
		textColor : Color = MaterialTheme.colorScheme.onSurface,
		disabledTextColor : Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.17f),
		containerColor : Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
		cursorColor : Color = MaterialTheme.colorScheme.onSurface,
		errorCursorColor : Color = MaterialTheme.colorScheme.error,
		selectionColors : TextSelectionColors = TextSelectionColors(
			handleColor = MaterialTheme.colorScheme.onSurface,
			backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
		),
		focusedTrailingIconColor : Color = MaterialTheme.colorScheme.onSurface,
		unfocusedTrailingIconColor : Color = MaterialTheme.colorScheme.onSurface,
		disabledTrailingIconColor : Color = MaterialTheme.colorScheme.onSurface,
		errorTrailingIconColor : Color = MaterialTheme.colorScheme.error,
		placeholderColor : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
		disabledPlaceholderColor : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
	) : GraTextFieldColors = GraTextFieldColors(
		textColor = textColor,
		disabledTextColor = disabledTextColor,
		containerColor = containerColor,
		cursorColor = cursorColor,
		errorCursorColor = errorCursorColor,
		selectionColors = selectionColors,
		focusedTrailingIconColor = focusedTrailingIconColor,
		unfocusedTrailingIconColor = unfocusedTrailingIconColor,
		disabledTrailingIconColor = disabledTrailingIconColor,
		errorTrailingIconColor = errorTrailingIconColor,
		placeholderColor = placeholderColor,
		disabledPlaceholderColor = disabledPlaceholderColor,
	)
}

