package com.syncodec.graphite.presentation.common.dialog.dialog2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ICON_SIZE


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GenericDialog2(
	isDialogVisible: Boolean = false,
	onDismissRequest: () -> Unit = {},
	icon: GenericDialogIcon? = null,
	title: String? = null,
	contentText: String? = null,
	innerPadding: PaddingValues = PaddingValues(24.dp),
	content: @Composable (ColumnScope.() -> Unit)? = null,
) {

	BackHandler(enabled = isDialogVisible) { onDismissRequest() }


	if (isDialogVisible) {
		BasicAlertDialog(
			onDismissRequest = onDismissRequest,
			properties = DialogProperties(
				dismissOnBackPress = false,
				dismissOnClickOutside = false,
			)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
//					.padding(innerPadding)
			) {
				icon?.let {
					Icon(
						painter = painterResource(id = it.icon),
						contentDescription = title,
						tint = it.tint,
						modifier = Modifier
							.requiredSize(24.dp)
							.align(Alignment.CenterHorizontally)
					)
					Spacer(modifier = Modifier.height(24.dp))
				}
				title?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
						textAlign = if (icon == null) TextAlign.Start else TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(modifier = Modifier.height(24.dp))
				}

				contentText?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}

				content?.let {
					Spacer(modifier = Modifier.height(24.dp))
					it()
				}
			}
		}
	}
}

@Preview
@Composable
private fun GenericDialog2Preview() {
	GenericDialog2(
		isDialogVisible = true,
		icon = GenericDialogIcon(
			icon = R.drawable.ic_fa_warning,
			tint = MaterialTheme.colorScheme.error
		),
		title = stringResource(id = R.string.clear_data),
		contentText = stringResource(id = R.string.clear_data_dialog_content),
	)
}
