package com.syncodec.graphite.presentation.common.dialog.buildingBlock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DialogTextField(
	value : String = "text",
	label : String = "label",
	placeholder : String = "placeholder",
	supportingText : String? = null,
	maxLines : Int = 1,
	leadingIcon : @Composable (() -> Unit)? = null,
	trailingIcon : @Composable (() -> Unit)? = null,
	containerColor : Color = MaterialTheme.colorScheme.background,
	contentColor : Color = MaterialTheme.colorScheme.onBackground,
	focusRequester : FocusRequester = remember { FocusRequester() },
	onKeyboardAction : () -> Unit = { },
	keyboardOptions : KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
	keyboardActions : KeyboardActions = KeyboardActions(onNext = { onKeyboardAction() }),
	onValueChange : (String?) -> Unit = { },
) {
	TextField(
		value = value,
		onValueChange = onValueChange,
		textStyle = MaterialTheme.typography.bodyMedium,
		label = { Text(text = label,) },
		placeholder = { Text(text = placeholder,) },
		supportingText = {
			supportingText?.let {
				Text(
					text = it,
					style = MaterialTheme.typography.bodySmall,
				)
			}
		},
		shape = MaterialTheme.shapes.medium,
		colors = TextFieldDefaults.textFieldColors(
			textColor = MaterialTheme.colorScheme.onSurface,
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
			cursorColor = MaterialTheme.colorScheme.onSurface,
			focusedIndicatorColor = Color.Transparent,
			unfocusedIndicatorColor = Color.Transparent,
			placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
		),
		modifier = Modifier.fillMaxWidth(),
	)
}
