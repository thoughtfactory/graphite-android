package com.syncodec.graphite.settingsComponent.miscellaneous

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.settingsComponent.SettingsActivity
import kotlinx.coroutines.delay


@Composable
fun TakeSnapshotDialog(
	timestamp: Long,
	totalNotebook: Int,
	processedNotebook: Int,
	totalChapter: Int,
	processedChapter: Int,
	totalNote: Int,
	processedNote: Int,
	totalAttachment: Int,
	processedAttachment: Int,
	totalBucket: Int,
	processedBucket: Int,
	totalBucketItem: Int,
	processedBucketItem: Int,
	totalTag: Int,
	processedTag: Int,
	isSnapshotting: Boolean,
	onComplete: () -> Unit
) {
	var init by remember { mutableStateOf(false) }
	val showDialog = isSnapshotting &&
			(processedNotebook != totalNotebook ||
					processedChapter != totalChapter ||
					processedNote != totalNote ||
					processedAttachment != totalAttachment ||
					processedBucket != totalBucket ||
					processedBucketItem != totalBucketItem ||
					processedTag != totalTag
					)

	LaunchedEffect(key1 = showDialog) {
		if (!showDialog && init) {
			onComplete()
		} else {
			init = true
		}
	}

	if (showDialog) {
		AlertDialog(
			containerColor = MaterialTheme.colorScheme.surface,
			onDismissRequest = { },
			title = {
				Text(
					text = "Snapshot",
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			text = {
				Column(modifier = Modifier.fillMaxWidth()) {
					SnapshotProgressor(
						text = "notebooks",
						totalItem = totalNotebook,
						processedItem = processedNotebook
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "chapters",
						totalItem = totalChapter,
						processedItem = processedChapter
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "notes",
						totalItem = totalNote,
						processedItem = processedNote
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "attachments",
						totalItem = totalAttachment,
						processedItem = processedAttachment
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "buckets",
						totalItem = totalBucket,
						processedItem = processedBucket
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "bucket items",
						totalItem = totalBucketItem,
						processedItem = processedBucketItem
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "tags",
						totalItem = totalTag,
						processedItem = processedTag
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "connections",
						totalItem = null,
						processedItem = null
					)
					Spacer(modifier = Modifier.height(4.dp))

					Spacer(modifier = Modifier.height(12.dp))
					Text(
						text = "Do not close or keep app in background while snapshot is being generated",
						color = MaterialTheme.colorScheme.onSurface
					)


					Spacer(modifier = Modifier.height(24.dp))

					LinearProgressIndicator(
						color = MaterialTheme.colorScheme.primary,
						trackColor = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.fillMaxWidth()
					)
				}
			},
			confirmButton = {},
			dismissButton = {}
		)
	}
}

@Composable
fun RestoreSnapshotDialog(
	timestamp: Long,
	totalNotebook: Int,
	processedNotebook: Int,
	totalChapter: Int,
	processedChapter: Int,
	totalNote: Int,
	processedNote: Int,
	totalAttachment: Int,
	processedAttachment: Int,
	totalBucket: Int,
	processedBucket: Int,
	totalBucketItem: Int,
	processedBucketItem: Int,
	totalTag: Int,
	processedTag: Int,
	isRestoring: Boolean,
	onComplete: () -> Unit
) {
	var init by remember { mutableStateOf(false) }
	val showDialog = isRestoring &&
			(processedNotebook != totalNotebook ||
					processedChapter != totalChapter ||
					processedNote != totalNote ||
					processedAttachment != totalAttachment ||
					processedBucket != totalBucket ||
					processedBucketItem != totalBucketItem
					)

	LaunchedEffect(key1 = showDialog) {
		if (!showDialog && init) {
			onComplete()
		} else {
			init = true
		}
	}

	if (showDialog) {
		AlertDialog(
			containerColor = MaterialTheme.colorScheme.surface,
			onDismissRequest = { },
			title = {
				Text(
					text = "Snapshot",
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			text = {
				Column(modifier = Modifier.fillMaxWidth()) {
					SnapshotProgressor(
						text = "notebooks",
						totalItem = totalNotebook,
						processedItem = processedNotebook
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "chapters",
						totalItem = totalChapter,
						processedItem = processedChapter
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "notes",
						totalItem = totalNote,
						processedItem = processedNote
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "attachments",
						totalItem = totalAttachment,
						processedItem = processedAttachment
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "buckets",
						totalItem = totalBucket,
						processedItem = processedBucket
					)
					Spacer(modifier = Modifier.height(4.dp))

					SnapshotProgressor(
						text = "bucket items",
						totalItem = totalBucketItem,
						processedItem = processedBucketItem
					)
					Spacer(modifier = Modifier.height(4.dp))

					Spacer(modifier = Modifier.height(12.dp))
					Text(
						text = "Do not close or keep app in background while snapshot is being generated",
						color = MaterialTheme.colorScheme.onSurface
					)


					Spacer(modifier = Modifier.height(24.dp))

					LinearProgressIndicator(
						color = MaterialTheme.colorScheme.primary,
						trackColor = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.fillMaxWidth()
					)
				}
			},
			confirmButton = {},
			dismissButton = {}
		)
	}
}

@Composable
private fun SnapshotProgressor(
	text: String,
	totalItem: Int?,
	processedItem: Int?
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (totalItem == null || processedItem == null) {
			CircularProgressIndicator(
				color = MaterialTheme.colorScheme.onSurface,
				strokeWidth = 2.dp,
				modifier = Modifier.requiredSize(16.dp)
			)
		} else {
			CircularProgressIndicator(
				color = MaterialTheme.colorScheme.onSurface,
				strokeWidth = 2.dp,
				progress = processedItem.toFloat() / totalItem,
				modifier = Modifier.requiredSize(16.dp)
			)
		}
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = if (totalItem == null || processedItem == null) "Processing $text" else "$processedItem/$totalItem $text",
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}

@Composable
fun RestoreSnapshotConsentDialog(
	showDialog: Boolean,
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	var isButtonEnabled by remember { mutableStateOf(false) }
	var timeRemaining by remember { mutableStateOf(10) }

	LaunchedEffect(key1 = showDialog) {
		isButtonEnabled = false
		timeRemaining = 10
		for (i in 0 until 10) {
			delay(1000)
			timeRemaining -= 1
		}
		isButtonEnabled = true
	}

	if (showDialog) {
		AlertDialog(
			containerColor = MaterialTheme.colorScheme.surface,
			onDismissRequest = { },
			title = {
				Text(
					text = "Restore Snapshot",
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			text = {
				Text(
					text = "Restoring snapshot is a destructive operation. That means existing data in the database will be deleted and will be replaced by data from snapshot. It is advisable to take current snapshot too before performing this action.",
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			confirmButton = {
				Button(
					enabled = isButtonEnabled,
					onClick = { onAction(SettingsActivity.Action.RESTORE_SNAPSHOT, null) }
				) {
					if (timeRemaining > 0)
						Text("Restore ($timeRemaining)")
					else
						Text("Restore")
				}
			},
			dismissButton = {
				OutlinedButton(
					onClick = { onAction(SettingsActivity.Action.DONT_RESTORE_SNAPSHOT, null) }
				) {
					Text("Dismiss")
				}
			}
		)
	}
}
