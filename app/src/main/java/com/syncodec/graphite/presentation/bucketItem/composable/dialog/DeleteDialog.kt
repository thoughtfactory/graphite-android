package com.syncodec.graphite.presentation.bucketItem.composable.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun DeleteDialog(
	showDeleteDialog: Boolean,
	message: String? = null,
	onDismiss: () -> Unit,
	onDelete: () -> Unit
) {
	GenericDialog(
		showDialog = showDeleteDialog,
		onDismissRequest = onDismiss
	) {
		Text(
			text = "Delete",
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = message ?: "Are you sure you want to delete this item?",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		DualActionButtons(
			primaryText = "Delete",
			secondaryText = "Dismiss",
			onPrimaryClick = onDelete,
			onSecondaryClick = onDismiss
		)
	}
}
