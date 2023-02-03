package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Preview
@Composable
fun DeleteDialog(
	showDialog : Boolean = true,
	message : String? = null,
	onDismiss : () -> Unit = {},
	onDelete : () -> Unit = {},
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Delete",
		contentText = message ?: "Deletion is an irreversible action. Are you sure you want to delete selected items?",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Delete",
				secondaryText = "Cancel",
				primaryColor = MaterialTheme.colorScheme.errorContainer,
				onClickPrimary = onDelete,
				onClickSecondary = onDismiss,
			)
		},
		onDismissRequest = onDismiss
	)
}
