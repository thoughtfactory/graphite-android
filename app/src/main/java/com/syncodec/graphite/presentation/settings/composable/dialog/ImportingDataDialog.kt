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
fun ImportingSnapshotDialog(
	showDialog: Boolean = true,
	attachmentCount : Int = 0,
	attachmentProcessed : Int = 0,
	bucketItemCount : Int = 0,
	bucketItemProcessed : Int = 0,
	bucketCount : Int = 0,
	bucketProcessed : Int = 0,
	chapterCount : Int = 0,
	chapterProcessed : Int = 0,
	noteCount : Int = 0,
	noteProcessed : Int = 0,
	tagCount : Int = 0,
	tagProcessed : Int = 0,
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Importing Data",
		onDismissRequest = { /*TODO*/ }
	) {
		ProgressView(text = "Attachment", total = attachmentCount, processed = attachmentProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Bucket", total = bucketItemCount, processed = bucketItemProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Bucket item", total = bucketCount, processed = bucketProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Chapter", total = chapterCount, processed = chapterProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Note", total = noteCount, processed = noteProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Tag", total = tagCount, processed = tagProcessed)

		Spacer(modifier = Modifier.height(4.dp))

		LinearProgressIndicator(
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
