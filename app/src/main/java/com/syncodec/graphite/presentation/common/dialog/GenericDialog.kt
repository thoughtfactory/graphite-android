package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.syncodec.graphite.presentation.ui.dialogPadding


@Composable
fun GenericDialog(
	showDialog: Boolean,
	title: String?,
	contentText: String? = null,
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
					if (title != null) {
						Text(
							text = title,
							style = MaterialTheme.typography.headlineMedium,
							color = MaterialTheme.colorScheme.onSurface,
						)

						Spacer(modifier = Modifier.height(12.dp))

						if (contentText != null) {
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
							) {
								Text(
									text = contentText,
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier.padding(12.dp)
								)
							}

							Spacer(modifier = Modifier.height(12.dp))
						}
					}
					content()
				}
			}
		}
	}
}
