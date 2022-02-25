package com.syncodec.momento.custom.button

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
	text: String,
	containerColor: Color,
	contentColor: Color,
	isElevated: Boolean = false,
	isClickable: Boolean,
	modifier: Modifier,
	onClick: () -> Unit
) {
	Card(
		modifier = modifier
			.height(48.dp)
			.focusable(),
		backgroundColor = containerColor,
		elevation = if (isElevated) 8.dp else 0.dp,
		enabled = isClickable,
		shape = RoundedCornerShape(12.dp),
		onClick = { onClick() }
	) {
		Box(
			modifier = Modifier
				.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = text,
				style = MaterialTheme.typography.titleMedium,
				color = contentColor,
				textAlign = TextAlign.Center,
				lineHeight = 0.sp,
				maxLines = 1,
			)
		}
	}
}
