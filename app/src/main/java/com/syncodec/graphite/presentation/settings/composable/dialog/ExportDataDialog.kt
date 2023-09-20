package com.syncodec.graphite.presentation.settings.composable.dialog

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.secureComposable.LocalIsRepoUnlocked
import com.syncodec.graphite.presentation.common.button.VaultButton
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults
import com.syncodec.graphite.presentation.settings.composable.viewModel.ExportDataViewModel
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun ExportDataDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
) {
	val context = LocalContext.current

	val scope = rememberCoroutineScope()

	val viewModel: ExportDataViewModel = koinViewModel()

	var isNotesSelected by remember { mutableStateOf(true) }
	var isBucketsSelected by remember { mutableStateOf(true) }
	var isTagsSelected by remember { mutableStateOf(true) }
	var isAttachmentsSelected by remember { mutableStateOf(true) }
	var includeLockedItems by remember { mutableStateOf(false) }

	var isExportingDialogVisible by remember { mutableStateOf(false) }

	val isRepoUnlocked = LocalIsRepoUnlocked.current

	fun exportData() {
		onDismissRequest()
		isExportingDialogVisible = true
		scope.launch(Dispatchers.IO) {
			val exportFile =
				viewModel.exportData(isNotesSelected = isNotesSelected, isBucketsSelected = isBucketsSelected, isTagsSelected = isTagsSelected, isAttachmentsSelected = isAttachmentsSelected, includeLockedItems = includeLockedItems)
			withContext(Dispatchers.Main) {
				exportFile?.share(context = context)
				isExportingDialogVisible = false
			}
		}
	}

	LaunchedEffect(key1 = isDialogVisible) {
		isNotesSelected = false
		isBucketsSelected = false
		isTagsSelected = false
		isAttachmentsSelected = false
		includeLockedItems = false
	}

	GenericDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_fa_export),
		title = stringResource(id = R.string.export_data),
		contentText = stringResource(id = R.string.select_items_to_export),
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(id = R.string.export), onClick = ::exportData),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
		onDismissRequest = onDismissRequest,
	) {
		ExportItem(
			title = stringResource(id = R.string.notes_and_notebooks),
			checked = isNotesSelected,
			onCheckedChange = { isNotesSelected = !isNotesSelected }
		)
		ExportItem(
			title = stringResource(id = R.string.buckets_and_bucket_items),
			checked = isBucketsSelected,
			onCheckedChange = { isBucketsSelected = !isBucketsSelected }
		)
		ExportItem(
			title = stringResource(id = R.string.tags),
			checked = isTagsSelected,
			onCheckedChange = { isTagsSelected = !isTagsSelected }
		)
		ExportItem(
			title = stringResource(id = R.string.attachments),
			checked = isAttachmentsSelected,
			onCheckedChange = { isAttachmentsSelected = !isAttachmentsSelected }
		)
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			Checkbox(
				checked = includeLockedItems,
				onCheckedChange = {
					if (isRepoUnlocked) includeLockedItems = !includeLockedItems
					else Toast.makeText(context, context.getText(R.string.toast_open_vault_to_include_selected), Toast.LENGTH_SHORT).show()
				}
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = stringResource(id = R.string.include_locked_items),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
			Spacer(modifier = Modifier.weight(1f))
			VaultButton { onDismissRequest() }
		}
	}

	GenericDialog2(
		isDialogVisible = isExportingDialogVisible,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_fa_export),
		title = stringResource(id = R.string.exporting),
		contentText = stringResource(id = R.string.please_wait_while_your_data_is_being_processed)
	) {
		LinearProgressIndicator(
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@Preview
@Composable
private fun ExportItem(
	title: String = "Notes and notebooks",
	checked: Boolean = true,
	onCheckedChange: () -> Unit = {}
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
	) {
		Checkbox(
			checked = checked,
			onCheckedChange = { onCheckedChange() }
		)
		Spacer(modifier = Modifier.width(12.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
