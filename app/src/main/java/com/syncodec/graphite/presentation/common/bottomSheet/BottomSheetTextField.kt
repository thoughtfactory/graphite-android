package com.syncodec.graphite.presentation.common.bottomSheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetTextField(
	value : String = "Title",
	label : String = "Label",
	placeholder : String = "Placeholder",
	onValueChange : (String) -> Unit = {},
) {
	TextField(
		value = value,
		onValueChange = onValueChange,
		textStyle = MaterialTheme.typography.bodyMedium,
		label = { Text(label) },
		placeholder = { Text(placeholder) },
		shape = MaterialTheme.shapes.medium,
		singleLine = true,
		maxLines = 1,
		colors = TextFieldDefaults.textFieldColors(
			textColor = MaterialTheme.colorScheme.onSurface,
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
			cursorColor = MaterialTheme.colorScheme.onSurface,
			focusedIndicatorColor = Color.Transparent,
			unfocusedIndicatorColor = Color.Transparent,
			placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
			focusedLabelColor = MaterialTheme.colorScheme.onSurface,
			unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
		),
		modifier = Modifier.fillMaxWidth()
	)
}
