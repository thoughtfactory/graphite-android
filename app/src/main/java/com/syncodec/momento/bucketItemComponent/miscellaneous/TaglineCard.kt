package com.syncodec.momento.bucketItemComponent.miscellaneous

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterialApi::class)
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
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.Center,
			modifier = Modifier,
		)
	}
}
