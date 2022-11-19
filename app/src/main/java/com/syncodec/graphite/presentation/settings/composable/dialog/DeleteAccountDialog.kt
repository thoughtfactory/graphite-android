package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.presentation.ui.DeleteContainer


@Composable
fun DeleteAccountDialog(
	showDialog: Boolean,
	onDelete: () -> Unit,
	onDismiss: () -> Unit,
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Delete Account",
		onDismissRequest = onDismiss
	) {
		Text(
			text = "Deleting your account will remove all your data from our servers. This action cannot be undone.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		DualActionButtons(
			primaryText = "Delete",
			secondaryText = "Dismiss",
			primaryColor = Color.Companion.DeleteContainer,
			onPrimaryClick = onDelete,
			onSecondaryClick = onDismiss
		)
	}
}
