package com.syncodec.graphite.mainComponent.miscellaneous

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.syncodec.graphite.miscellaneous.DataStoreInstance


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseNotes() {
	val context = LocalContext.current
	val dataStore = DataStoreInstance(context)

	val showReleaseNote by dataStore.showReleaseNotes.collectAsState(initial = null)

	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	if (showReleaseNote != false) {
		Dialog(
			onDismissRequest = { dataStore.setShowReleaseNotes(false) },
			properties = DialogProperties()
		) {
			OutlinedCard(
				border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
				modifier = Modifier
					.fillMaxWidth()
					.height(screenHeight * 2 / 3)
			) {
				LazyColumn(
					horizontalAlignment = Alignment.Start,
					modifier = Modifier
						.fillMaxSize()
						.padding(16.dp, 0.dp),
				) {
					item { Spacer(modifier = Modifier.height(16.dp)) }

					release(
						releaseVersion = "Release 1.1.0",
						releaseContentList = listOf(
							"Can update bucket title",
							"UI changes in text field",
							"Added release notes",
							"Bug fixes in vault",
							"Bug fixes in richtext editor",
							"Bug fixes in richtext viewer",
						)
					)

					release(
						releaseVersion = "Release 1.0.0",
						releaseContentList = listOf(
							"Initial release"
						)
					)
				}
			}
		}
	}
}

private fun LazyListScope.release(releaseVersion: String, releaseContentList: List<String>) {
	item {
		Text(
			text = releaseVersion,
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
	item { Spacer(modifier = Modifier.height(8.dp)) }
	releaseContentList.forEach {
		item {
			Text(
				text = it,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
				modifier = Modifier.padding(8.dp, 2.dp, 0.dp, 2.dp)
			)
		}
	}
	item { Spacer(modifier = Modifier.height(16.dp)) }
	item {
		Spacer(
			modifier = Modifier
				.fillMaxWidth()
				.height(2.dp)
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f))
		)
	}
	item { Spacer(modifier = Modifier.height(16.dp)) }
}
