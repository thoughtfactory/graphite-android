package com.syncodec.momento.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LargeButton(
	modifier: Modifier = Modifier,
	text: String,
	backgroundColor: Color,
	textColor: Color,
	onClick: () -> Unit
) {
	Card(
		elevation = 8.dp,
		backgroundColor = backgroundColor,
		modifier = modifier,
		onClick = { onClick() }
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
		) {
			Text(
				text = text,
				style = MaterialTheme.typography.bodyLarge,
				color = textColor,
				textAlign = TextAlign.Center,
				lineHeight = 0.sp,
				maxLines = 1,
			)
		}
	}
}
