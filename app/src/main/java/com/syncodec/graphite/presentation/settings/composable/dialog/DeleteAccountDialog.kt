package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.GenericDialogDefaults


@Preview
@Composable
fun DeleteAccountDialog(
	showDialog: Boolean = false,
	onConfirmDelete: () -> Unit = {},
	onDismiss: () -> Unit = {},
) {
	GenericDialog2(
		showDialog = showDialog,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_flat_delete_account, tint = MaterialTheme.colorScheme.error),
		title = "Delete account",
		contentText = "Deleting your account will remove all your data from the server. Are you sure you want to delete your account? This does not affect your local data or data on cloud providers which are uploaded for sync and backups.",
		primaryButton = GenericDialogDefaults.genericDialogButtonWarning(text = "Delete", onClick = onConfirmDelete,),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismiss),
	)
}
