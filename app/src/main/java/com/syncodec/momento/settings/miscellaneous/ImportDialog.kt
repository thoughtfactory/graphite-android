package com.syncodec.momento.settings.miscellaneous

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDialog(
	isImportingData: Boolean,
	importFileSize: Int,
	currentImportFileIndex: Int
) {
	if (isImportingData) {
		AlertDialog(
			onDismissRequest = {},
			title = {
				Text(
					text = "Importing...",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
			},
			text = {
				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					if (currentImportFileIndex == 0) {
						LinearProgressIndicator(
							color = MaterialTheme.colorScheme.primaryContainer,
							trackColor = MaterialTheme.colorScheme.onPrimaryContainer
						)
					} else {
						LinearProgressIndicator(
							progress = currentImportFileIndex.toFloat() / importFileSize,
							color = MaterialTheme.colorScheme.primaryContainer,
							trackColor = MaterialTheme.colorScheme.onPrimaryContainer
						)
					}
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "$currentImportFileIndex/$importFileSize",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimaryContainer
					)
				}
			},
			confirmButton = {
			},
		)
	}
}
