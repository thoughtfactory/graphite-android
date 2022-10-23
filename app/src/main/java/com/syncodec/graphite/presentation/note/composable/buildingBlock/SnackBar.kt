package com.syncodec.graphite.presentation.note.composable.buildingBlock


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
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
