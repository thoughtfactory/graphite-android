package com.syncodec.graphite.presentation.note.screen.editorScreen.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun LocationSnackbarHost(snackbarData: SnackbarData) {
	Card(
		colors = CardDefaults.cardColors(
			contentColor = MaterialTheme.colorScheme.onPrimary,
			containerColor = MaterialTheme.colorScheme.primary
		),
		modifier = Modifier.padding(12.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 8.dp)
		) {
			Text(
				text = snackbarData.visuals.message,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
			)
		}
	}
}
