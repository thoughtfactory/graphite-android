package com.syncodec.graphite.bucketItemComponent.miscellaneous

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign


@Composable
fun TaglineCard(
	tagline: String
) {
	Box(
		modifier = Modifier,
		contentAlignment = Alignment.Center
	) {
		Text(
			text = tagline,
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.headlineSmall,
			textAlign = TextAlign.Center,
			modifier = Modifier,
		)
	}
}
