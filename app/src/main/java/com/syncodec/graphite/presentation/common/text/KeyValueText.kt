package com.syncodec.graphite.presentation.common.text

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyValueText(
	modifier : Modifier = Modifier,
	key : String,
	value : String?,
	containerColor : Color = MaterialTheme.colorScheme.surface,
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
	maxLines : Int = Int.MAX_VALUE,
	onClick : () -> Unit = { /*TODO*/ },
) {
	val context = LocalContext.current
	val clipboardManager : ClipboardManager = LocalClipboardManager.current

	Box(
		modifier = modifier
			.background(color = containerColor, shape = RoundedCornerShape(16.dp))
			.clip(RoundedCornerShape(16.dp))
			.combinedClickable(
				enabled = true,
				onClick = onClick,
				onLongClick = {
					if (value != null) clipboardManager.setText(AnnotatedString(value))
					else Toast.makeText(context, "No value to copy", Toast.LENGTH_SHORT).show()
				},
				onLongClickLabel = "Copy to clipboard",
			)
	) {
		Column(
			modifier = Modifier.padding(12.dp)
		) {
			Text(
				text = key,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = if (value.isNullOrEmpty()) "No $key" else value,
				style = MaterialTheme.typography.titleMedium,
				color = contentColor,
				fontStyle = if (value == null) FontStyle.Italic else null,
				fontWeight = if (value != null) FontWeight.Bold else null,
				maxLines = maxLines,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
