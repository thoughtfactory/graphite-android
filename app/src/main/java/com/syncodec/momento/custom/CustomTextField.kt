package com.syncodec.momento.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.TextUnit


@Composable
fun CustomTextField(
	modifier: Modifier = Modifier,
	text: String,
	placeholderText: String = "Placeholder",
	fontSize: TextUnit = MaterialTheme.typography.body2.fontSize,
	onValueChange: (String) -> Unit
) {
	BasicTextField(modifier = modifier
		.background(
			MaterialTheme.colors.surface,
			MaterialTheme.shapes.small,
		)
		.fillMaxWidth(),
		value = text,
		onValueChange = { onValueChange(it) },
		singleLine = true,
		cursorBrush = SolidColor(MaterialTheme.colors.primary),
		textStyle = LocalTextStyle.current.copy(
			color = MaterialTheme.colors.onSurface,
			fontSize = fontSize
		),
		decorationBox = { innerTextField ->
			Box(
			) {
				if (text.isEmpty()) Text(
					placeholderText,
					style = LocalTextStyle.current.copy(
						color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
						fontSize = fontSize
					)
				)
				innerTextField()
			}
		}
	)
}
