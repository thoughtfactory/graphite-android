package com.syncodec.graphite.presentation.custom.dialog.buildingBlock

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.MenuButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTextField(
	text: String?,
	label: String,
	placeholder: String,
	onTextChange: (String?) -> Unit
) {
	TextField(
		value = text ?: "",
		onValueChange = onTextChange,
		modifier = Modifier.fillMaxWidth(),
		singleLine = true,
		keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
		keyboardActions = KeyboardActions(onNext = { /*TODO*/ }),
		textStyle = TextStyle(
			fontWeight = FontWeight.Bold,
			fontSize = MaterialTheme.typography.titleLarge.fontSize,
		),
		shape = RoundedCornerShape(24.dp),
		colors = TextFieldDefaults.textFieldColors(
			textColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
			containerColor = MaterialTheme.colorScheme.background,
			cursorColor = MaterialTheme.colorScheme.onBackground,
			focusedIndicatorColor = Color.Transparent,
			unfocusedIndicatorColor = Color.Transparent,
		),
		placeholder = {
			Text(
				text = placeholder,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
			)
		},
		label = {
			Text(
				text = label,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
			)
		},
		trailingIcon = {
			MenuButton(
				icon = R.drawable.ic_close,
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
			) { onTextChange(null) }
		}
	)
}
