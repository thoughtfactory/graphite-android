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
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.ui.ProgressView
import com.syncodec.graphite.presentation.common.dialog.GenericDialog


@Composable
fun TakeSnapshotDialog(
	showDialog : Boolean,
	attachmentCount : Int,
	attachmentProcessed : Int,
	bucketItemCount : Int,
	bucketItemProcessed : Int,
	bucketCount : Int,
	bucketProcessed : Int,
	chapterCount : Int,
	chapterProcessed : Int,
	noteCount : Int,
	noteProcessed : Int,
	tagCount : Int,
	tagProcessed : Int,
	packageCount : Int,
	packageProcessed : Int,
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Taking snapshot...",
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

		Spacer(modifier = Modifier.height(8.dp))

		Text(
			text = "Packaging...",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			fontWeight = FontWeight.Bold,
		)
		Spacer(modifier = Modifier.height(2.dp))
		LinearProgressIndicator(
			progress = (packageProcessed.toFloat() / maxOf(1, packageCount).toFloat()),
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
