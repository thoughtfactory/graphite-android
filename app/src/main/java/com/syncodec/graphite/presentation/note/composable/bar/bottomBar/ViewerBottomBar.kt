package com.syncodec.graphite.presentation.note.composable.bar.bottomBar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.MenuButton


@Composable
fun ViewerBottomBar(
	onCopy: () -> Unit,
	onExport: () -> Unit,
	onPrint: () -> Unit,
	onDelete: () -> Unit
) {
	LazyRow(modifier = Modifier) {
		item { Spacer(modifier = Modifier.width(16.dp)) }
		item {
			MenuButton(
				icon = R.drawable.ic_copy,
				contentDescription = "Copy Note",
				onClick = onCopy
			)
		}
		item {
			MenuButton(
				icon = R.drawable.ic_export,
				contentDescription = "Export Note",
				onClick = onExport
			)
		}
		item {
			MenuButton(
				icon = R.drawable.ic_printer,
				contentDescription = "Print Note",
				onClick = onPrint
			)
		}
		item {
			MenuButton(
				icon = R.drawable.ic_delete,
				contentDescription = "Delete",
				tint = Color(0xFFF05945),
				onClick = onDelete
			)
		}
	}
}
