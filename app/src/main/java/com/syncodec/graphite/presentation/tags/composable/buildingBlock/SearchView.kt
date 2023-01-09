package com.syncodec.graphite.presentation.tags.composable.buildingBlock

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
	value: String = "",
	placeholder: String = "Add or search tags",
	isTagPresent: Boolean = false,
	onAddTag: () -> Unit = {},
	onValueChange: (String) -> Unit = {},
) {
	TextField(
		value = value,
		onValueChange = onValueChange,
		textStyle = MaterialTheme.typography.bodyMedium,
		label = { Text(text = "Tag") },
		placeholder = { Text(text = placeholder) },
		shape = MaterialTheme.shapes.medium,
		singleLine = true,
		colors = TextFieldDefaults.textFieldColors(
			textColor = MaterialTheme.colorScheme.onSurface,
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
			cursorColor = MaterialTheme.colorScheme.onSurface,
			focusedIndicatorColor = Color.Transparent,
			unfocusedIndicatorColor = Color.Transparent,
			placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
			focusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
			unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
		),
		trailingIcon = {
			IconButton(
				onClick = onAddTag
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_add),
					contentDescription = "Add tag",
					modifier = Modifier.requiredSize(IconButtonSize)
				)
			}
		},
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	)
}
