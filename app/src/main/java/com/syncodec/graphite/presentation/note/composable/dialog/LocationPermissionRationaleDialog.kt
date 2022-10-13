package com.syncodec.graphite.presentation.note.composable.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun LocationPermissionRationaleDialog(
	showDialog: Boolean,
	onRequestPermission: () -> Unit,
	onDismiss: () -> Unit,
) {
	GenericDialog(
		showDialog = showDialog,
		onDismissRequest = onDismiss
	) {
		Text(
			text = "Location",
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "Want to save your memories not only in time but also in space? Allow Graphite to access your location and we will do the rest.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(8.dp))

		DualActionButtons(
			primaryText = "Request",
			secondaryText = "Discard",
			onPrimaryClick = onRequestPermission,
			onSecondaryClick = onDismiss
		)
	}
}
