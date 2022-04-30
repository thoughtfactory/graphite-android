package com.syncodec.graphite.custom.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun NoteSpacer(
	isLast: Boolean = false,
	height: Int = 0,
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.width(16.dp)
			.height(with(LocalDensity.current) { height.toDp() }),
	) {
		Box(
			modifier = Modifier
				.width(4.dp)
				.weight(1f)
				.background(MaterialTheme.colorScheme.secondaryContainer)
		)
		Box(
			modifier = Modifier
				.width(16.dp)
				.height(16.dp)
				.padding(2.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.secondaryContainer)
		)
		Box(
			modifier = Modifier
				.width(4.dp)
				.weight(4f)
				.background(if (isLast) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer)
		)
	}
}
