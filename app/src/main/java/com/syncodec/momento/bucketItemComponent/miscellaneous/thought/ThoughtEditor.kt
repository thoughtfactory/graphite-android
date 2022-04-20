package com.syncodec.momento.bucketItemComponent.miscellaneous.thought

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ThoughtEditor(
	text: String = "",
	isFirst: Boolean = false,
	onSave: (String) -> Unit,
	onDiscard: () -> Unit,
	onDelete: (() -> Unit)?
) {
	val context = LocalContext.current
	var thought by remember { mutableStateOf(text) }
	val focusRequester = remember { FocusRequester() }

	Column(
		modifier = Modifier
	) {
		BasicTextField(
			value = thought,
			onValueChange = { thought = it },
			textStyle = MaterialTheme.typography.bodyMedium.copy(
				color = MaterialTheme.colorScheme.onBackground
			),
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(64.dp, Dp.Infinity)
				.padding(16.dp, if (isFirst) 16.dp else 12.dp, 16.dp, 16.dp)
				.focusRequester(focusRequester),
		) { innerTextField ->
			if (thought.isEmpty()) {
				Text(
					"What are you thinking?",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.primaryContainer
				)
			}
			innerTextField()
		}
		Spacer(modifier = Modifier.height(8.dp))
		ThoughtController(
			onSave = {
				if (thought.isNotEmpty()) {
					onSave(thought)
					thought = ""
				} else Toast
					.makeText(context, "Don't keep the field empty", Toast.LENGTH_SHORT)
					.show()
			},
			onDiscard = onDiscard,
			onDelete = onDelete
		)
	}
}
