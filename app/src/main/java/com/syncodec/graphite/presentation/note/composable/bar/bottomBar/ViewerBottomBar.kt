package com.syncodec.graphite.presentation.note.composable.bar.bottomBar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@Composable
fun ViewerBottomBar(
	onCopy: () -> Unit,
	onExport: () -> Unit,
	onPrint: () -> Unit,
	onDelete: () -> Unit,
	onEdit: () -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(2.dp)
	) {
		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_copy,
			contentDescription = "Copy Note",
			onClick = onCopy
		)

		MenuButton(
			icon = R.drawable.ic_export,
			contentDescription = "Export Note",
			onClick = onExport
		)

		MenuButton(
			icon = R.drawable.ic_printer,
			contentDescription = "Print Note",
			onClick = onPrint
		)

		MenuButton(
			icon = R.drawable.ic_delete,
			contentDescription = "Delete",
			tint = Color(0xFFF05945),
			onClick = onDelete
		)

		Spacer(modifier = Modifier.weight(1f))

		MenuButton(
			icon = R.drawable.ic_pencil,
			contentDescription = "Edit Note",
			onClick = onEdit
		)

		Spacer(modifier = Modifier.width(16.dp))
	}
}
