package com.syncodec.graphite.presentation.common.dialog.buildingBlock

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE


@Preview
@Composable
fun DialogTextField(
	value : String = "text",
	label : String = "label",
	placeholder : String = "placeholder",
	actionButtons : @Composable RowScope.() -> Unit = {},
	supportingText : String? = null,
	onKeyboardAction : () -> Unit = { },
	keyboardOptions : KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
	keyboardActions : KeyboardActions = KeyboardActions(onNext = { onKeyboardAction() }),
	colors : DialogTextFieldColors = DialogTextFieldDefaults.textFieldColors(),
	onValueChange : (String?) -> Unit = { },
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
			onValueChange = onValueChange,
			modifier = Modifier.weight(1f),
		) {
			Box(
				contentAlignment = Alignment.CenterStart,
				modifier = Modifier
					.fillMaxWidth()
					.height((ICON_SIZE * 2) + 2.dp)
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

		Spacer(modifier = Modifier.width(4.dp))
		MenuButton(
			icon = R.drawable.ic_close,
			colors = MenuButtonDefaults.menuButtonColors(
				containerColor = colors.containerColor,
				iconColor = colors.textColor,
			)
		) { onValueChange("") }
		actionButtons()
	}
}

@Immutable
data class DialogTextFieldColors constructor(
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
) {
	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is DialogTextFieldColors) return false

		if (textColor != other.textColor) return false
		if (disabledTextColor != other.disabledTextColor) return false
		if (containerColor != other.containerColor) return false
		if (cursorColor != other.cursorColor) return false
		if (errorCursorColor != other.errorCursorColor) return false
		if (selectionColors != other.selectionColors) return false
		if (focusedTrailingIconColor != other.focusedTrailingIconColor) return false
		if (unfocusedTrailingIconColor != other.unfocusedTrailingIconColor) return false
		if (disabledTrailingIconColor != other.disabledTrailingIconColor) return false
		if (errorTrailingIconColor != other.errorTrailingIconColor) return false
		if (placeholderColor != other.placeholderColor) return false
		if (disabledPlaceholderColor != other.disabledPlaceholderColor) return false

		return true
	}

	override fun hashCode() : Int {
		var result = textColor.hashCode()
		result = 31 * result + disabledTextColor.hashCode()
		result = 31 * result + containerColor.hashCode()
		result = 31 * result + cursorColor.hashCode()
		result = 31 * result + errorCursorColor.hashCode()
		result = 31 * result + selectionColors.hashCode()
		result = 31 * result + focusedTrailingIconColor.hashCode()
		result = 31 * result + unfocusedTrailingIconColor.hashCode()
		result = 31 * result + disabledTrailingIconColor.hashCode()
		result = 31 * result + errorTrailingIconColor.hashCode()
		result = 31 * result + placeholderColor.hashCode()
		result = 31 * result + disabledPlaceholderColor.hashCode()
		return result
	}

}

object DialogTextFieldDefaults {
	@Composable
	fun textFieldColors(
		textColor : Color = MaterialTheme.colorScheme.onSurface,
		disabledTextColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
		containerColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
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
	) : DialogTextFieldColors = DialogTextFieldColors(
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
