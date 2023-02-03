package com.syncodec.graphite.presentation.common.bottomSheet

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BottomSheetKeyValueCard(
	key : String = "Key",
	value : String? = "Value",
	onClick : (() -> Unit)? = null
) {
	val context = LocalContext.current
	val clipboardManager : ClipboardManager = LocalClipboardManager.current

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(
				color = MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.31f), shape = MaterialTheme.shapes.medium
			)
			.clip(MaterialTheme.shapes.medium)
			.combinedClickable(
				enabled = onClick != null,
				onClick = { onClick?.invoke() },
				onLongClick = {
					value?.let { clipboardManager.setText(AnnotatedString(it)) }
						?: run {
							Toast
								.makeText(context, "No value to copy", Toast.LENGTH_SHORT)
								.show()
						}
				},
				onLongClickLabel = "Copy to clipboard",
			)
	) {
		Column(
			modifier = Modifier.padding(12.dp)
		) {
			Text(
				text = key,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface,
			)
			Text(
				text = if (value.isNullOrEmpty()) "No value" else value,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold,
				fontStyle = if (value.isNullOrEmpty()) FontStyle.Italic else null
			)
		}
	}
}
