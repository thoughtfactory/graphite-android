package com.syncodec.graphite.presentation.custom.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.syncodec.graphite.presentation.ui.dialogPadding


@Composable
fun GenericDialog(
	showDialog: Boolean,
	onDismissRequest: () -> Unit,
	content: @Composable () -> Unit
) {
	if (showDialog) {
		Dialog(
			onDismissRequest = onDismissRequest
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.wrapContentHeight()
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(dialogPadding)
				) {
					content()
				}
			}
		}
	}
}
