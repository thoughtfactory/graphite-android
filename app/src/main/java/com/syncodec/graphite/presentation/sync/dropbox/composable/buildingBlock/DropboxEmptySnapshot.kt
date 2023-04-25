package com.syncodec.graphite.presentation.sync.dropbox.composable.buildingBlock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun DropboxEmptySnapshot() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 8.dp)
	) {
		Text(
			text = "No snapshot found",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
	}
}
