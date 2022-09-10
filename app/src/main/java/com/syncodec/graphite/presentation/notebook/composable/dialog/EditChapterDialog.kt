package com.syncodec.graphite.presentation.notebook.composable.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.syncodec.graphite.presentation.custom.dialog.buildingBlock.DialogTextField


@Composable
fun EditChapterDialog(
	title: String,
	description: String?,
	color: Color,
	showDialog: Boolean,
	onDismiss: () -> Unit,
) {
	var _title by remember { mutableStateOf(title) }
	var _description by remember { mutableStateOf(description) }

	LaunchedEffect(key1 = title) {
		_title = title
	}
	LaunchedEffect(key1 = description) {
		_description = description
	}

	if (showDialog) {

		Dialog(
			onDismissRequest = { onDismiss() }
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					Text(
						text = "Edit Chapter",
						style = MaterialTheme.typography.headlineMedium
					)

					Spacer(modifier = Modifier.height(12.dp))

					DialogTextField(
						text = _title,
						label = "Title",
						placeholder = "What do you want to it?",
					) { _title = it?:"" }

					Spacer(modifier = Modifier.height(8.dp))

					DialogTextField(
						text = _description,
						label = "Description",
						placeholder = "What is it about?",
					) { _description = it }

					Spacer(modifier = Modifier.height(8.dp))

					ActionButton(
						onDiscard = { onDismiss() },
						onSave = { onDismiss() },
					)
				}
			}
		}
	}
}



@Composable
private fun ActionButton(
	onDiscard: () -> Unit,
	onSave: () -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		Spacer(modifier = Modifier.weight(1f))
		OutlinedButton(
			onClick = onDiscard,
			modifier = Modifier,
			colors = ButtonDefaults.outlinedButtonColors(
				containerColor = MaterialTheme.colorScheme.background,
				contentColor = MaterialTheme.colorScheme.onSurface
			)
		) {
			Text(
				text = "Discard",
			)
		}

		Spacer(modifier = Modifier.width(4.dp))

		Button(
			onClick = onSave,
			modifier = Modifier,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary
			)
		) {
			Text(
				text = "Save",
				color = MaterialTheme.colorScheme.onPrimary
			)
		}
	}
}
