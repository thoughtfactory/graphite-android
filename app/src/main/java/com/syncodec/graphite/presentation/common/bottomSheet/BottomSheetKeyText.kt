package com.syncodec.graphite.presentation.common.bottomSheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight


@Composable
fun BottomSheetKeyText(
	text: String
) {
	Text(
		text = text,
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
		fontWeight = FontWeight.Bold,
		modifier = Modifier.fillMaxWidth()
	)
}
