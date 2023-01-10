package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog


@Preview
@Composable
fun ImportingJourneySnapshotDialog(
	showDialog: Boolean = true,
	importDataCount: Int = 0,
	importDataProcessed: Int = 0,
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Importing Data",
		onDismissRequest = { /*TODO*/ }
	) {
		ProgressView(text = "Data", total = importDataCount, processed = importDataProcessed)
		Spacer(modifier = Modifier.height(2.dp))

		Spacer(modifier = Modifier.height(4.dp))

		LinearProgressIndicator(
			progress = (importDataProcessed.toFloat() / maxOf(1, importDataCount).toFloat()),
			color = MaterialTheme.colorScheme.onBackground,
			trackColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth(),
		)

		Spacer(modifier = Modifier.height(8.dp))
	}
}

@Composable
private fun ProgressView(
	text : String,
	total : Int,
	processed : Int,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
			.padding(12.dp, 8.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			Text(
				text = "$text: ",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			Text(
				text = " $processed of $total",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold
			)
		}
	}
}
