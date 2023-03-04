package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun SettingsContentTitle(
	title : String = "TITLE",
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f))
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 12.dp, 16.dp, 8.dp)
		)
	}
}
