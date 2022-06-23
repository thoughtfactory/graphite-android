package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.ErrorView
import com.syncodec.graphite.database.snapshot.Snapshot
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton


@Composable
fun RestoreSnapshotScreen(
	snapshot: Snapshot?,
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	if (snapshot == null) {
		ErrorView()
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				Row(
					modifier = Modifier,
					verticalAlignment = Alignment.CenterVertically
				) {
					Spacer(modifier = Modifier.width(16.dp))
					Text(
						text = snapshot.title,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)
					Spacer(modifier = Modifier.width(16.dp))
				}
			}

			item { Spacer(modifier = Modifier.height(16.dp)) }

			item {
				SettingButton(
					title = "Restore this snapshot",
					leadingIcon = R.drawable.ic_history
				) { onAction(SettingsActivity.Action.RESTORE_SNAPSHOT_CONSENT, null) }
			}

			item { Spacer(modifier = Modifier.height(16.dp)) }

			item {
				ComponentSize(component = "Notebook", size = snapshot.notebookCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_NOTEBOOK
					)
				}
			}
			item {
				ComponentSize(component = "Chapter", size = snapshot.chapterCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_CHAPTER
					)
				}
			}
			item {
				ComponentSize(component = "Note", size = snapshot.noteCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_NOTE
					)
				}
			}
			item {
				ComponentSize(component = "Attachment", size = snapshot.attachmentCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_ATTACHMENT
					)
				}
			}
			item {
				ComponentSize(component = "Bucket", size = snapshot.bucketCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_BUCKET
					)
				}
			}
			item {
				ComponentSize(component = "Bucket Item", size = snapshot.bucketItemCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_BUCKET_ITEM
					)
				}
			}
			item {
				ComponentSize(component = "Tag", size = snapshot.tagCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_TAG
					)
				}
			}
			item {
				ComponentSize(component = "Connection", size = snapshot.connectionCount) {
					onAction(
						SettingsActivity.Action.NAVIGATION,
						SettingsActivity.Companion.Path.SNAPSHOT_CONNECTION
					)
				}
			}
		}
	}
}

@Composable
private fun ComponentSize(
	component: String,
	size: Int,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.clickable { onClick() },
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = component,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
		)

		Spacer(modifier = Modifier.weight(1f))

		if (size != -1) {
			Text(
				text = "$size",
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
			)
		}
		Spacer(modifier = Modifier.width(32.dp))
	}
}
