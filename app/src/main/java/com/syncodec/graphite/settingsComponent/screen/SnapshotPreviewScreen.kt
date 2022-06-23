package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.custom.ErrorView
import com.syncodec.graphite.settingsComponent.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
fun SnapshotPreviewScreen(
	documentFile: DocumentFile?,
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current

	var text by remember { mutableStateOf<String?>(null) }
	var jsonData by remember { mutableStateOf<JSONObject?>(null) }

	var jsonError by remember { mutableStateOf(false) }
	var showError by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = null) {
		withContext(Dispatchers.IO) {
			try {
				text = ""
				val inputStream =
					documentFile?.let { context.contentResolver.openInputStream(it.uri) }

				val r = BufferedReader(InputStreamReader(inputStream))
				var line: String?
				while (r.readLine().also { line = it } != null) {
					text += line
				}

				jsonData = text?.let { JSONObject(it) }
			} catch (jsonException: JSONException) {
				jsonError = true
			} catch (exception: Exception) {
				exception.printStackTrace()
				showError = true
			}
		}
	}

	if (documentFile == null || documentFile.isDirectory || showError) {
		ErrorView()
	} else if (jsonError) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp, 0.dp)
		) {
			Text(
				text = text ?: "Error loading data",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
		}
	} else {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp, 0.dp)
				.verticalScroll(rememberScrollState())
		) {
			Text(
				text = jsonData?.toString(4) ?: "Error loading data",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}
