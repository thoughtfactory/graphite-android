package com.syncodec.graphite.custom.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone


@Composable
fun LargeButton(
	text: String,
	enabled: Boolean,
	modifier: Modifier,
	onClick: () -> Unit
) {
	val containerColor by animateColorAsState(
		targetValue = if (enabled) MaterialTheme.colorScheme.primary
		else MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	)

	val contentColor by animateColorAsState(
		targetValue = if (enabled) MaterialTheme.colorScheme.onPrimary
		else MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)
	)


	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.height(48.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(containerColor)
			.clickable { onClick() },
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = contentColor
		)
	}
}
