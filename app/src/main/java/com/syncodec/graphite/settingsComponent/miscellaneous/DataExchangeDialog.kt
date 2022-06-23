package com.syncodec.graphite.settingsComponent.miscellaneous

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.settingsComponent.SettingsActivity


@Composable
fun DataExchangeDialog(
	dataExchange: SettingsActivity.DataExchange,
	dataExchangeSize: Int,
	currentImportFileIndex: Int,
	currentImportFileName: String?,
) {
	if (dataExchange != SettingsActivity.DataExchange.NONE) {
		AlertDialog(
			onDismissRequest = {},
			title = {
				Text(
					text = when (dataExchange) {
						SettingsActivity.DataExchange.IMPORT -> "Importing..."
						SettingsActivity.DataExchange.EXPORT -> "Exporting..."
						SettingsActivity.DataExchange.NONE -> ""
					},
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
							trackColor = MaterialTheme.colorScheme.onSurface
						)
					} else {
						LinearProgressIndicator(
							progress = currentImportFileIndex.toFloat() / dataExchangeSize,
							color = MaterialTheme.colorScheme.primary,
							trackColor = MaterialTheme.colorScheme.onPrimary
						)
					}
					Spacer(modifier = Modifier.height(12.dp))

					Row(
						modifier = Modifier.fillMaxWidth()
					) {
						Text(
							text = currentImportFileName ?: "",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1,
							modifier = Modifier.weight(1f)
						)

						Spacer(modifier = Modifier.width(16.dp))

						Text(
							text = "$currentImportFileIndex/$dataExchangeSize",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface
						)

						if (currentImportFileName == null) Spacer(modifier = Modifier.width(16.dp))
						if (currentImportFileName == null) Spacer(modifier = Modifier.weight(1f))
					}
				}
			},
			confirmButton = { },
		)
	}
}
