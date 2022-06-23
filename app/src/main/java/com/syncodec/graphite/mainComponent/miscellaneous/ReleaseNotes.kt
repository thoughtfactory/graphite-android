package com.syncodec.graphite.mainComponent.miscellaneous

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.syncodec.graphite.miscellaneous.DataStoreInstance
import com.tobianoapps.bulletin.components.BulletinScreen
import com.tobianoapps.bulletin.data.Bulletin
import com.tobianoapps.bulletin.data.Change
import com.tobianoapps.bulletin.data.ChangeType
import com.tobianoapps.bulletin.data.Release


@Composable
fun ReleaseNotes() {
	val context = LocalContext.current
	val dataStore = DataStoreInstance(context)

	val showReleaseNote by dataStore.showReleaseNotes.collectAsState(initial = null)

	val bulletin by rememberSaveable() {
		mutableStateOf(
			Bulletin(
				listOf(
					Release(
						time = 1655912742000,
						label = "1.2.0",
						changes = listOf(
							Change(
								changeType = ChangeType.NEW,
								summary = "Local Backup and Restore"
							),
							Change(
								changeType = ChangeType.NEW,
								summary = "Added Links Bucket"
							),
							Change(
								changeType = ChangeType.NEW,
								summary = "Added year progress bar"
							),
							Change(
								changeType = ChangeType.IMPROVED,
								summary = "Improved icons"
							),
							Change(
								changeType = ChangeType.IMPROVED,
								summary = "UI changes in Note Editor and Note Viewer"
							),
							Change(
								changeType = ChangeType.FIXED,
								summary = "Focusing on textfield shows cursor"
							),
							Change(
								changeType = ChangeType.FIXED,
								summary = "Color of Title in dark theme"
							),
							Change(
								changeType = ChangeType.FIXED,
								summary = "Fixes in Heading component in Note Viewer"
							)
						)
					),
					Release(
						time = 1654799400000,
						label = "1.1.0",
						changes = listOf(
							Change(
								changeType = ChangeType.NEW,
								summary = "Added release notes"
							),
							Change(
								changeType = ChangeType.IMPROVED,
								summary = "Added title update in bucket in notes"
							),
							Change(
								changeType = ChangeType.FIXED,
								summary = "Vault passcode couldn't be saved"
							),
							Change(
								changeType = ChangeType.FIXED,
								summary = "Fixes in Note Editor"
							),
							Change(
								changeType = ChangeType.FIXED,
								summary = "Fixes in Note Viewer"
							),
							Change(
								changeType = ChangeType.REMOVED,
								summary = "Illustrations from Atlas"
							),
							Change(
								changeType = ChangeType.IMPROVED,
								summary = "UI changes in Text Field"
							)
						)
					),
					Release(
						time = 1654021800000,
						label = "1.0.0",
						changes = listOf(
							Change(
								changeType = ChangeType.NEW,
								summary = "First Release"
							)
						)
					)
				)
			)
		)
	}

	if (showReleaseNote != false) {
		Dialog(onDismissRequest = { dataStore.setShowReleaseNotes(false) }) {
			Column(
				horizontalAlignment = Alignment.End,
				modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
			) {
				BulletinScreen(
					modifier = Modifier.weight(1f),
					bulletin = bulletin,
				)

				Spacer(modifier = Modifier.height(8.dp))

				Button(
					onClick = { dataStore.setShowReleaseNotes(false) },
					modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 8.dp)
				) {
					Text(
						text = "Okay",
						style = MaterialTheme.typography.labelLarge
					)
				}
			}
		}
	}
}
