package com.syncodec.graphite.presentation.bucketItem.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun InfoSurface(
	containerColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
	onClick : (() -> Unit)? = null,
	content : @Composable () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp, 0.dp)
			.background(containerColor, MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
			.clickable(enabled = onClick != null) { onClick?.invoke() },
	) {
		Box(modifier = Modifier.padding(16.dp)) {
			content()
		}
	}
}
